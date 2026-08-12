package com.khatabook.clone.ui.common

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.abs

private val indianAmount = java.text.DecimalFormat("#,##,##0")
private val indianAmountWithPaise = java.text.DecimalFormat("#,##,##0.00")

/** "₹ 1,20,500" — paise are only shown when they are not zero, as in the app. */
fun rupees(amount: Double, withSymbol: Boolean = true): String {
    val value = abs(amount)
    val formatted = if (value % 1.0 == 0.0) indianAmount.format(value) else indianAmountWithPaise.format(value)
    return if (withSymbol) "₹ $formatted" else formatted
}

private val isoFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
private val displayFormat = SimpleDateFormat("dd MMM yy", Locale.US)
private val longDisplayFormat = SimpleDateFormat("dd MMM yyyy", Locale.US)

fun todayIso(): String = isoFormat.format(Date())

fun isoToDate(iso: String): Date? = runCatching { isoFormat.parse(iso) }.getOrNull()

fun dateToIso(date: Date): String = isoFormat.format(date)

/** "Today" / "Yesterday" / "04 Aug 25", the way ledger rows are stamped. */
fun friendlyDate(iso: String?): String {
    if (iso.isNullOrBlank()) return ""
    val date = isoToDate(iso) ?: return iso
    return when (daysFromToday(date)) {
        0 -> "Today"
        1 -> "Yesterday"
        else -> displayFormat.format(date)
    }
}

fun longDate(iso: String?): String {
    if (iso.isNullOrBlank()) return ""
    val date = isoToDate(iso) ?: return iso
    return longDisplayFormat.format(date)
}

private fun daysFromToday(date: Date): Int {
    val start = Calendar.getInstance().apply { time = date }.atMidnight()
    val today = Calendar.getInstance().atMidnight()
    val diff = today.timeInMillis - start.timeInMillis
    return (diff / (24 * 60 * 60 * 1000L)).toInt()
}

private fun Calendar.atMidnight(): Calendar = apply {
    set(Calendar.HOUR_OF_DAY, 0)
    set(Calendar.MINUTE, 0)
    set(Calendar.SECOND, 0)
    set(Calendar.MILLISECOND, 0)
}

/** First letter of each of the first two words, for the round avatar. */
fun initials(name: String): String {
    val parts = name.trim().split(" ").filter { it.isNotBlank() }
    return when {
        parts.isEmpty() -> "?"
        parts.size == 1 -> parts[0].take(1).uppercase(Locale.getDefault())
        else -> (parts[0].take(1) + parts[1].take(1)).uppercase(Locale.getDefault())
    }
}
