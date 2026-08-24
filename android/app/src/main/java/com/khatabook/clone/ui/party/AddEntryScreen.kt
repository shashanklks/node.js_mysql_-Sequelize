@file:OptIn(ExperimentalMaterial3Api::class)

package com.khatabook.clone.ui.party

import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.khatabook.clone.data.remote.EntryType
import com.khatabook.clone.ui.common.AmountKeypad
import com.khatabook.clone.ui.common.KhataTopBar
import com.khatabook.clone.ui.common.dateToIso
import com.khatabook.clone.ui.common.isoToDate
import com.khatabook.clone.ui.common.longDate
import com.khatabook.clone.ui.common.todayIso
import com.khatabook.clone.ui.common.yesterdayIso
import com.khatabook.clone.ui.theme.KhataTheme
import java.util.Calendar

@Composable
fun AddEntryScreen(
    partyId: Int,
    type: String,
    onBack: () -> Unit,
    onSaved: () -> Unit,
) {
    val viewModel: AddEntryViewModel = viewModel()
    val state = viewModel.state
    val palette = KhataTheme.colors
    val context = LocalContext.current
    val isGave = type == EntryType.GAVE
    val accent = if (isGave) palette.give else palette.get

    LaunchedEffect(partyId) { viewModel.loadParty(partyId) }
    LaunchedEffect(state.saved) { if (state.saved) onSaved() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(palette.screen),
    ) {
        KhataTopBar(
            title = if (isGave) "You gave" else "You got",
            subtitle = state.partyName.takeIf { it.isNotBlank() },
            onBack = onBack,
        )

        // Amount, tinted by direction so the screen itself says which way the
        // money moved before you read a word of it.
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(accent.copy(alpha = if (palette.isDark) 0.12f else 0.06f))
                .padding(horizontal = 20.dp, vertical = 22.dp),
        ) {
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = "₹",
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold,
                    color = accent,
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = state.amount.ifEmpty { "0" },
                    fontSize = 40.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (state.amount.isEmpty()) accent.copy(alpha = 0.35f) else accent,
                    style = MaterialTheme.typography.displaySmall.copy(fontFeatureSettings = "tnum"),
                )
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
        ) {
            Spacer(Modifier.height(12.dp))
            Surface(
                color = palette.surface,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Box(modifier = Modifier.padding(horizontal = 14.dp, vertical = 13.dp)) {
                    if (state.note.isEmpty()) {
                        Text(
                            text = "Add a note — items, bill no., quantity",
                            color = palette.textFaint,
                            style = MaterialTheme.typography.bodyLarge,
                        )
                    }
                    BasicTextField(
                        value = state.note,
                        onValueChange = viewModel::onNoteChange,
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        textStyle = MaterialTheme.typography.bodyLarge.copy(color = palette.textPrimary),
                        cursorBrush = SolidColor(accent),
                    )
                }
            }

            Spacer(Modifier.height(10.dp))
            // Almost every entry is written the day it happens, or the day
            // after, so those two are one tap and the picker is the fallback.
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                DateChip(
                    label = "Today",
                    selected = state.date == todayIso(),
                    onClick = { viewModel.onDateChange(todayIso()) },
                )
                DateChip(
                    label = "Yesterday",
                    selected = state.date == yesterdayIso(),
                    onClick = { viewModel.onDateChange(yesterdayIso()) },
                )
                DateChip(
                    label = if (state.date == todayIso() || state.date == yesterdayIso()) {
                        "Pick date"
                    } else {
                        longDate(state.date)
                    },
                    selected = state.date != todayIso() && state.date != yesterdayIso(),
                    icon = true,
                    onClick = {
                        val calendar = Calendar.getInstance()
                        isoToDate(state.date)?.let { calendar.time = it }
                        DatePickerDialog(
                            context,
                            { _, year, month, day ->
                                val picked = Calendar.getInstance().apply { set(year, month, day, 0, 0, 0) }
                                viewModel.onDateChange(dateToIso(picked.time))
                            },
                            calendar.get(Calendar.YEAR),
                            calendar.get(Calendar.MONTH),
                            calendar.get(Calendar.DAY_OF_MONTH),
                        ).apply {
                            // A ledger cannot be written into the future.
                            datePicker.maxDate = System.currentTimeMillis()
                        }.show()
                    },
                )
            }

            if (state.error != null) {
                Spacer(Modifier.height(10.dp))
                Text(
                    text = state.error,
                    color = palette.give,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }

        Surface(color = palette.surface, shadowElevation = 10.dp) {
            Column {
                AmountKeypad(
                    value = state.amount,
                    onValueChange = viewModel::onAmountChange,
                    accent = accent,
                )
                Button(
                    onClick = { viewModel.save(partyId, type) },
                    enabled = state.canSubmit,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                        .height(54.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = accent,
                        disabledContainerColor = palette.line,
                        disabledContentColor = palette.textFaint,
                    ),
                ) {
                    if (state.loading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(22.dp),
                            color = palette.surface,
                            strokeWidth = 2.dp,
                        )
                    } else {
                        Text("Save", style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
        }
    }
}

@Composable
private fun DateChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    icon: Boolean = false,
) {
    val palette = KhataTheme.colors
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (selected) palette.brand else palette.surface)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icon) {
            Icon(
                Icons.Default.CalendarMonth,
                contentDescription = null,
                tint = if (selected) palette.onHeader else palette.textSecondary,
                modifier = Modifier.size(15.dp),
            )
            Spacer(Modifier.width(6.dp))
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = if (selected) palette.onHeader else palette.textSecondary,
        )
    }
}
