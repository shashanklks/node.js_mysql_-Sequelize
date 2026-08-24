@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)

package com.khatabook.clone.ui.party

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.CallMade
import androidx.compose.material.icons.automirrored.filled.CallReceived
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.khatabook.clone.data.remote.EntryDto
import com.khatabook.clone.data.remote.EntryType
import com.khatabook.clone.ui.common.AnimatedMoneyText
import com.khatabook.clone.ui.common.Avatar
import com.khatabook.clone.ui.common.EmptyState
import com.khatabook.clone.ui.common.HeaderSurface
import com.khatabook.clone.ui.common.KhataCard
import com.khatabook.clone.ui.common.LoadingBox
import com.khatabook.clone.ui.common.MoneyText
import com.khatabook.clone.ui.common.ScreenMessage
import com.khatabook.clone.ui.common.friendlyDate
import com.khatabook.clone.ui.common.rupees
import com.khatabook.clone.ui.theme.KhataTheme

@Composable
fun PartyDetailScreen(
    partyId: Int,
    onBack: () -> Unit,
    onAddEntry: (partyId: Int, type: String) -> Unit,
) {
    val viewModel: PartyDetailViewModel = viewModel()
    val state = viewModel.state
    val palette = KhataTheme.colors
    val context = LocalContext.current
    var pendingDelete by remember { mutableStateOf<EntryDto?>(null) }
    var confirmDeleteParty by remember { mutableStateOf(false) }

    LaunchedEffect(partyId) { viewModel.load(partyId) }
    LaunchedEffect(state.deleted) { if (state.deleted) onBack() }

    Scaffold(
        containerColor = palette.screen,
        topBar = {
            PartyHeader(
                name = state.party.name,
                phone = state.party.phone,
                balance = state.summary.balance,
                gave = state.summary.gave,
                got = state.summary.got,
                onBack = onBack,
                onCall = {
                    state.party.phone?.let {
                        context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$it")))
                    }
                },
                onDelete = { confirmDeleteParty = true },
            )
        },
        bottomBar = {
            LedgerButtons(
                onGave = { onAddEntry(partyId, EntryType.GAVE) },
                onGot = { onAddEntry(partyId, EntryType.GOT) },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            when {
                state.loading -> LoadingBox(Modifier.weight(1f))

                state.error != null -> Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    ScreenMessage(state.error) {
                        Button(
                            onClick = { viewModel.load(partyId) },
                            colors = ButtonDefaults.buttonColors(containerColor = palette.brand),
                            shape = RoundedCornerShape(10.dp),
                        ) { Text("Try again") }
                    }
                }

                state.entries.isEmpty() -> Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    EmptyState(
                        icon = "🧾",
                        title = "No entries yet",
                        message = "Tap YOU GAVE for credit you hand out, or YOU GOT when you are paid.",
                    )
                }

                else -> {
                    LedgerColumnHeader()
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(bottom = 12.dp),
                    ) {
                        // Entries arrive newest first; grouping keeps that order
                        // and only breaks the run when the date changes.
                        state.entries
                            .groupBy { it.entryDate }
                            .forEach { (date, entries) ->
                                stickyHeader(key = "h_$date") { DateHeader(date) }
                                items(entries, key = { it.id }) { entry ->
                                    EntryRow(entry = entry, onLongPress = { pendingDelete = entry })
                                }
                            }
                    }
                    Text(
                        text = "Long-press an entry to delete it",
                        style = MaterialTheme.typography.bodyMedium,
                        color = palette.textFaint,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                    )
                }
            }
        }
    }

    pendingDelete?.let { entry ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text("Delete this entry?") },
            text = {
                Text("${entryTypeLabel(entry.type)} ${rupees(entry.amount)} on ${friendlyDate(entry.entryDate)}")
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteEntry(entry.id)
                    pendingDelete = null
                }) { Text("Delete", color = palette.give) }
            },
            dismissButton = {
                TextButton(onClick = { pendingDelete = null }) { Text("Cancel") }
            },
        )
    }

    if (confirmDeleteParty) {
        AlertDialog(
            onDismissRequest = { confirmDeleteParty = false },
            title = { Text("Delete ${state.party.name}?") },
            text = { Text("The whole ledger for this account will be removed. This cannot be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteParty()
                    confirmDeleteParty = false
                }) { Text("Delete", color = palette.give) }
            },
            dismissButton = {
                TextButton(onClick = { confirmDeleteParty = false }) { Text("Cancel") }
            },
        )
    }
}

/**
 * The header carries the balance itself. The old layout put the name on the
 * navy and the balance on a card below it, which meant the one number you open
 * this screen for started one scroll-length down the page.
 */
@Composable
private fun PartyHeader(
    name: String,
    phone: String?,
    balance: Double,
    gave: Double,
    got: Double,
    onBack: () -> Unit,
    onCall: () -> Unit,
    onDelete: () -> Unit,
) {
    val palette = KhataTheme.colors
    HeaderSurface {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .padding(horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = palette.onHeader,
                    )
                }
                Avatar(name = name.ifBlank { "?" }, size = 36)
                Spacer(Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = name,
                        color = palette.onHeader,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                    )
                    Text(
                        text = phone?.let { "+91 $it" } ?: "No phone number",
                        color = palette.onHeaderMuted,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 1,
                    )
                }
                if (!phone.isNullOrBlank()) {
                    IconButton(onClick = onCall) {
                        Icon(Icons.Default.Call, contentDescription = "Call", tint = palette.onHeader)
                    }
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete account", tint = palette.onHeader)
                }
            }

            KhataCard(modifier = Modifier.padding(start = 12.dp, end = 12.dp, bottom = 12.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = when {
                                balance == 0.0 -> "All settled"
                                balance > 0 -> "You will get"
                                else -> "You will give"
                            },
                            style = MaterialTheme.typography.titleMedium,
                            color = palette.money(balance),
                            modifier = Modifier.weight(1f),
                        )
                        AnimatedMoneyText(
                            amount = balance,
                            style = MaterialTheme.typography.headlineSmall,
                        )
                    }
                    Spacer(Modifier.height(10.dp))
                    Row {
                        TotalChip("Total you gave", gave, palette.give, Modifier.weight(1f))
                        TotalChip("Total you got", got, palette.get, Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun TotalChip(
    label: String,
    amount: Double,
    color: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier,
) {
    val palette = KhataTheme.colors
    Column(modifier = modifier) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium, color = palette.textFaint)
        MoneyText(amount = amount, color = color, style = MaterialTheme.typography.titleMedium)
    }
}

@Composable
private fun LedgerColumnHeader() {
    val palette = KhataTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(palette.surfaceAlt)
            .padding(horizontal = 16.dp, vertical = 7.dp),
    ) {
        Text(
            text = "ENTRIES",
            style = MaterialTheme.typography.labelSmall,
            color = palette.textFaint,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = "YOU GAVE",
            style = MaterialTheme.typography.labelSmall,
            color = palette.textFaint,
            textAlign = TextAlign.End,
            modifier = Modifier.width(84.dp),
        )
        Text(
            text = "YOU GOT",
            style = MaterialTheme.typography.labelSmall,
            color = palette.textFaint,
            textAlign = TextAlign.End,
            modifier = Modifier.width(84.dp),
        )
    }
}

@Composable
private fun DateHeader(date: String) {
    val palette = KhataTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(palette.screen)
            .padding(horizontal = 16.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = friendlyDate(date),
            style = MaterialTheme.typography.labelSmall,
            color = palette.textSecondary,
        )
        Spacer(Modifier.width(10.dp))
        Box(
            Modifier
                .weight(1f)
                .height(1.dp)
                .background(palette.line)
        )
    }
}

@Composable
private fun EntryRow(entry: EntryDto, onLongPress: () -> Unit) {
    val palette = KhataTheme.colors
    val isGave = entry.type == EntryType.GAVE
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(palette.surface)
            .combinedClickable(onClick = {}, onLongClick = onLongPress)
            .padding(horizontal = 16.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = if (isGave) {
                Icons.AutoMirrored.Filled.CallMade
            } else {
                Icons.AutoMirrored.Filled.CallReceived
            },
            contentDescription = null,
            tint = if (isGave) palette.give else palette.get,
            modifier = Modifier.size(16.dp),
        )
        Spacer(Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = entry.note?.takeIf { it.isNotBlank() } ?: entryTypeLabel(entry.type),
                style = MaterialTheme.typography.bodyLarge,
                color = palette.textPrimary,
                maxLines = 1,
            )
            Text(
                text = "Balance ${rupees(entry.runningBalance)}",
                style = MaterialTheme.typography.bodyMedium,
                color = palette.textFaint,
            )
        }
        Box(modifier = Modifier.width(84.dp), contentAlignment = Alignment.CenterEnd) {
            if (isGave) MoneyText(amount = entry.amount, color = palette.give)
        }
        Box(modifier = Modifier.width(84.dp), contentAlignment = Alignment.CenterEnd) {
            if (!isGave) MoneyText(amount = entry.amount, color = palette.get)
        }
    }
}

@Composable
private fun LedgerButtons(onGave: () -> Unit, onGot: () -> Unit) {
    val palette = KhataTheme.colors
    Surface(color = palette.surface, shadowElevation = 10.dp) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Button(
                onClick = onGave,
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = palette.give),
            ) {
                Text("YOU GAVE ₹", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
            }
            Button(
                onClick = onGot,
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = palette.get),
            ) {
                Text("YOU GOT ₹", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
            }
        }
    }
}
