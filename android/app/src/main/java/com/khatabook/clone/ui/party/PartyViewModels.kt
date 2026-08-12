package com.khatabook.clone.ui.party

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.khatabook.clone.ServiceLocator
import com.khatabook.clone.data.remote.EntryDto
import com.khatabook.clone.data.remote.EntryType
import com.khatabook.clone.data.remote.LedgerSummary
import com.khatabook.clone.data.remote.PartyDto
import com.khatabook.clone.data.remote.PartyType
import com.khatabook.clone.ui.common.todayIso
import kotlinx.coroutines.launch

data class PartyDetailUiState(
    val party: PartyDto = PartyDto(),
    val entries: List<EntryDto> = emptyList(),
    val summary: LedgerSummary = LedgerSummary(),
    val loading: Boolean = true,
    val error: String? = null,
    val deleted: Boolean = false,
)

class PartyDetailViewModel : ViewModel() {

    private val repo = ServiceLocator.repository
    private var partyId: Int = 0

    var state by mutableStateOf(PartyDetailUiState())
        private set

    fun load(id: Int, showSpinner: Boolean = true) {
        partyId = id
        if (showSpinner) state = state.copy(loading = true)
        viewModelScope.launch {
            repo.entries(id)
                .onSuccess {
                    state = state.copy(
                        party = it.party,
                        entries = it.entries.reversed(), // newest first, like the app
                        summary = it.summary,
                        loading = false,
                        error = null,
                    )
                }
                .onFailure { state = state.copy(loading = false, error = it.message) }
        }
    }

    fun deleteEntry(entryId: Int) {
        viewModelScope.launch {
            repo.deleteEntry(entryId)
                .onSuccess { load(partyId, showSpinner = false) }
                .onFailure { state = state.copy(error = it.message) }
        }
    }

    fun deleteParty() {
        viewModelScope.launch {
            repo.deleteParty(partyId)
                .onSuccess { state = state.copy(deleted = true) }
                .onFailure { state = state.copy(error = it.message) }
        }
    }
}

data class AddEntryUiState(
    val amount: String = "",
    val note: String = "",
    val date: String = todayIso(),
    val loading: Boolean = false,
    val error: String? = null,
    val saved: Boolean = false,
    val partyName: String = "",
) {
    val amountValue: Double get() = amount.toDoubleOrNull() ?: 0.0
    val canSubmit: Boolean get() = amountValue > 0 && !loading
}

class AddEntryViewModel : ViewModel() {

    private val repo = ServiceLocator.repository

    var state by mutableStateOf(AddEntryUiState())
        private set

    fun loadParty(partyId: Int) {
        if (state.partyName.isNotBlank()) return
        viewModelScope.launch {
            repo.entries(partyId).onSuccess { state = state.copy(partyName = it.party.name) }
        }
    }

    fun onAmountChange(value: String) {
        // One optional decimal point, two decimals, nothing else.
        val cleaned = value.filter { it.isDigit() || it == '.' }
        val parts = cleaned.split('.')
        val normalized = when {
            parts.size <= 1 -> cleaned
            else -> parts[0] + "." + parts[1].take(2)
        }
        state = state.copy(amount = normalized, error = null)
    }

    fun onNoteChange(value: String) {
        state = state.copy(note = value)
    }

    fun onDateChange(iso: String) {
        state = state.copy(date = iso)
    }

    fun save(partyId: Int, type: String) {
        if (!state.canSubmit) return
        state = state.copy(loading = true, error = null)
        viewModelScope.launch {
            repo.addEntry(
                partyId = partyId,
                amount = state.amountValue,
                type = type,
                note = state.note,
                entryDate = state.date,
            )
                .onSuccess { state = state.copy(loading = false, saved = true) }
                .onFailure { state = state.copy(loading = false, error = it.message) }
        }
    }
}

data class AddPartyUiState(
    val name: String = "",
    val phone: String = "",
    val type: String = PartyType.CUSTOMER,
    val loading: Boolean = false,
    val error: String? = null,
    val createdId: Int? = null,
) {
    val canSubmit: Boolean get() = name.isNotBlank() && !loading
}

class AddPartyViewModel : ViewModel() {

    private val repo = ServiceLocator.repository

    var state by mutableStateOf(AddPartyUiState())
        private set

    fun setType(type: String) {
        state = state.copy(type = type)
    }

    fun onNameChange(value: String) {
        state = state.copy(name = value, error = null)
    }

    fun onPhoneChange(value: String) {
        state = state.copy(phone = value.filter { it.isDigit() }.take(10), error = null)
    }

    fun save() {
        if (!state.canSubmit) return
        state = state.copy(loading = true, error = null)
        viewModelScope.launch {
            repo.createParty(name = state.name, phone = state.phone, type = state.type)
                .onSuccess { state = state.copy(loading = false, createdId = it.id) }
                .onFailure { state = state.copy(loading = false, error = it.message) }
        }
    }
}

/** Shared by the two ledger buttons so their labels never drift apart. */
fun entryTypeLabel(type: String): String =
    if (type == EntryType.GAVE) "You gave" else "You got"
