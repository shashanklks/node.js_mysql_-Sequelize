package com.khatabook.clone.ui.home

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.khatabook.clone.ServiceLocator
import com.khatabook.clone.data.remote.BookSummary
import com.khatabook.clone.data.remote.PartyDto
import com.khatabook.clone.data.remote.PartyType
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

enum class PartySort(val api: String, val label: String) {
    RECENT("recent", "Most recent"),
    HIGHEST("highest", "Highest amount"),
    NAME("name", "Name (A–Z)"),
}

data class HomeUiState(
    val tab: String = PartyType.CUSTOMER,
    val parties: List<PartyDto> = emptyList(),
    val summary: BookSummary = BookSummary(),
    val query: String = "",
    val searching: Boolean = false,
    val sort: PartySort = PartySort.RECENT,
    val loading: Boolean = true,
    val error: String? = null,
    val businessName: String = "My Business",
    val ownerName: String = "",
) {
    val isCustomerTab: Boolean get() = tab == PartyType.CUSTOMER
}

class HomeViewModel : ViewModel() {

    private val repo = ServiceLocator.repository
    private val session = ServiceLocator.session
    private var searchJob: Job? = null

    var state by mutableStateOf(HomeUiState())
        private set

    init {
        viewModelScope.launch {
            val stored = session.profile.first()
            state = state.copy(
                businessName = stored.businessName?.takeIf { it.isNotBlank() }
                    ?: stored.name?.takeIf { it.isNotBlank() }
                    ?: "My Business",
                ownerName = stored.name.orEmpty(),
            )
        }
        load()
    }

    fun selectTab(tab: String) {
        if (tab == state.tab) return
        state = state.copy(tab = tab, parties = emptyList(), loading = true)
        load()
    }

    fun setSort(sort: PartySort) {
        if (sort == state.sort) return
        state = state.copy(sort = sort)
        load(showSpinner = false)
    }

    fun toggleSearch() {
        state = if (state.searching) {
            state.copy(searching = false, query = "")
        } else {
            state.copy(searching = true)
        }
        if (!state.searching) load()
    }

    fun onQueryChange(value: String) {
        state = state.copy(query = value)
        // Debounced so typing does not fire a request per keystroke.
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(300)
            load(showSpinner = false)
        }
    }

    fun load(showSpinner: Boolean = true) {
        if (showSpinner) state = state.copy(loading = true)
        viewModelScope.launch {
            repo.parties(type = state.tab, search = state.query, sort = state.sort.api)
                .onSuccess {
                    state = state.copy(
                        parties = it.parties,
                        summary = it.summary,
                        loading = false,
                        error = null,
                    )
                }
                .onFailure { state = state.copy(loading = false, error = it.message) }
        }
    }
}
