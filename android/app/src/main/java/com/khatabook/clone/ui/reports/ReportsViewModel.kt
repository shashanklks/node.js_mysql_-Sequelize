package com.khatabook.clone.ui.reports

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.khatabook.clone.ServiceLocator
import com.khatabook.clone.data.remote.ReportEntryDto
import com.khatabook.clone.ui.common.dateToIso
import com.khatabook.clone.ui.common.todayIso
import kotlinx.coroutines.launch
import java.util.Calendar

enum class ReportRange { MONTH, WEEK, ALL }

data class ReportsUiState(
    val range: ReportRange = ReportRange.MONTH,
    val partyType: String? = null,
    val entries: List<ReportEntryDto> = emptyList(),
    val gave: Double = 0.0,
    val got: Double = 0.0,
    val overallGet: Double = 0.0,
    val overallGive: Double = 0.0,
    val loading: Boolean = true,
    val error: String? = null,
)

class ReportsViewModel : ViewModel() {

    private val repo = ServiceLocator.repository

    var state by mutableStateOf(ReportsUiState())
        private set

    init {
        load()
    }

    fun setRange(range: ReportRange) {
        if (range == state.range) return
        state = state.copy(range = range)
        load()
    }

    fun setPartyType(type: String?) {
        if (type == state.partyType) return
        state = state.copy(partyType = type)
        load()
    }

    private fun load() {
        state = state.copy(loading = true)
        viewModelScope.launch {
            val (from, to) = rangeBounds(state.range)

            repo.summary().onSuccess {
                state = state.copy(
                    overallGet = it.overall.youWillGet,
                    overallGive = it.overall.youWillGive,
                )
            }

            repo.transactions(from = from, to = to, partyType = state.partyType)
                .onSuccess {
                    state = state.copy(
                        entries = it.entries,
                        gave = it.summary.gave,
                        got = it.summary.got,
                        loading = false,
                        error = null,
                    )
                }
                .onFailure { state = state.copy(loading = false, error = it.message) }
        }
    }

    private fun rangeBounds(range: ReportRange): Pair<String?, String?> = when (range) {
        ReportRange.ALL -> null to null
        ReportRange.MONTH -> {
            val start = Calendar.getInstance().apply { set(Calendar.DAY_OF_MONTH, 1) }
            dateToIso(start.time) to todayIso()
        }

        ReportRange.WEEK -> {
            val start = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -6) }
            dateToIso(start.time) to todayIso()
        }
    }
}
