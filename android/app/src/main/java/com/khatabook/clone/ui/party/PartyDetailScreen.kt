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
import com.khatabook.clone.ui.common.Avatar
import com.khatabook.clone.ui.common.EmptyState
import com.khatabook.clone.ui.common.LoadingBox
import com.khatabook.clone.ui.common.RowDivider
import com.khatabook.clone.ui.common.ScreenMessage
import com.khatabook.clone.ui.common.friendlyDate
import com.khatabook.clone.ui.common.rupees
import com.khatabook.clone.ui.theme.Divider
import com.khatabook.clone.ui.theme.GreenGet
import com.khatabook.clone.ui.theme.Navy
import com.khatabook.clone.ui.theme.RedGive
import com.khatabook.clone.ui.theme.ScreenBg
import com.khatabook.clone.ui.theme.SurfaceWhite
import com.khatabook.clone.ui.theme.TextOnNavy
import com.khatabook.clone.ui.theme.TextOnNavyMuted
import com.khatabook.clone.ui.theme.TextSecondary

@Composable
fun PartyDetailScreen(
    partyId: Int,
    onBack: () -> Unit,
    onAddEntry: (partyId: Int, type: String) -> Unit,
) {
    val viewModel: PartyDetailViewModel = viewModel()
    val state = viewModel.state
    val context = LocalContext.current
    var pendingDelete by remember { mutableStateOf<EntryDto?>(null) }
    var confirmDeleteParty by remember { mutableStateOf(false) }

    // Reloads whenever the screen comes back to the front after saving an entry.
    LaunchedEffect(partyId) { viewModel.load(partyId) }

    LaunchedEffect(state.deleted) {
        if (state.deleted) onBack()
    }

    Scaffold(
        containerColor = ScreenBg,
        topBar = {
            PartyHeader(
                name = state.party.name,
                phone = state.party.phone,
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
            BalanceCard(balance = state.summary.balance)
            LedgerColumnHeader()

            when {
                state.loading -> LoadingBox(Modifier.weight(1f))
                state.error != null -> Column(Modifier.weight(1f)) { ScreenMessage(state.error) }
                state.entries.isEmpty() -> Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    EmptyState(
                        title = "No entries yet",
                        message = "Use YOU GAVE for credit you hand out and YOU GOT when you are paid.",
                    )
                }

                else -> LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .background(SurfaceWhite),
                ) {
                    items(state.entries, key = { it.id }) { entry ->
                        EntryRow(entry = entry, onLongPress = { pendingDelete = entry })
                        RowDivider()
                    }
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
                }) { Text("Delete", color = RedGive) }
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
                }) { Text("Delete", color = RedGive) }
            },
            dismissButton = {
                TextButton(onClick = { confirmDeleteParty = false }) { Text("Cancel") }
            },
        )
    }
}

@Composable
private fun PartyHeader(
    name: String,
    phone: String?,
    onBack: () -> Unit,
    onCall: () -> Unit,
    onDelete: () -> Unit,
) {
    Surface(color = Navy) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = TextOnNavy,
                )
            }
            Avatar(name = name.ifBlank { "?" }, size = 38)
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = name,
                    color = TextOnNavy,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                )
                Text(
                    text = phone?.let { "+91 $it" } ?: "No phone number",
                    color = TextOnNavyMuted,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                )
            }
            if (!phone.isNullOrBlank()) {
                IconButton(onClick = onCall) {
                    Icon(Icons.Default.Call, contentDescription = "Call", tint = TextOnNavy)
                }
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete account", tint = TextOnNavy)
            }
        }
    }
}

@Composable
private fun BalanceCard(balance: Double) {
    val isGet = balance >= 0
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        shape = RoundedCornerShape(12.dp),
        color = SurfaceWhite,
        shadowElevation = 2.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = when {
                    balance == 0.0 -> "All settled"
                    isGet -> "You will get"
                    else -> "You will give"
                },
                style = MaterialTheme.typography.titleMedium,
                color = if (balance == 0.0) TextSecondary else if (isGet) GreenGet else RedGive,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = rupees(balance),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = if (balance == 0.0) TextSecondary else if (isGet) GreenGet else RedGive,
            )
        }
    }
}

@Composable
private fun LedgerColumnHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(com.khatabook.clone.ui.theme.AvatarBg)
            .padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        Text(
            text = "ENTRIES",
            style = MaterialTheme.typography.labelSmall,
            color = TextSecondary,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = "YOU GAVE",
            style = MaterialTheme.typography.labelSmall,
            color = TextSecondary,
            textAlign = TextAlign.End,
            modifier = Modifier.width(90.dp),
        )
        Text(
            text = "YOU GOT",
            style = MaterialTheme.typography.labelSmall,
            color = TextSecondary,
            textAlign = TextAlign.End,
            modifier = Modifier.width(90.dp),
        )
    }
}

@Composable
private fun EntryRow(entry: EntryDto, onLongPress: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(onClick = {}, onLongClick = onLongPress)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = friendlyDate(entry.entryDate),
                style = MaterialTheme.typography.bodyLarge,
            )
            Text(
                text = entry.note?.takeIf { it.isNotBlank() }
                    ?: "Bal. ${rupees(entry.runningBalance)}",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                maxLines = 1,
            )
        }
        Text(
            text = if (entry.type == EntryType.GAVE) rupees(entry.amount) else "",
            style = MaterialTheme.typography.titleMedium,
            color = RedGive,
            textAlign = TextAlign.End,
            modifier = Modifier.width(90.dp),
        )
        Text(
            text = if (entry.type == EntryType.GOT) rupees(entry.amount) else "",
            style = MaterialTheme.typography.titleMedium,
            color = GreenGet,
            textAlign = TextAlign.End,
            modifier = Modifier.width(90.dp),
        )
    }
}

@Composable
private fun LedgerButtons(onGave: () -> Unit, onGot: () -> Unit) {
    Surface(color = SurfaceWhite, shadowElevation = 8.dp) {
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
                    .height(50.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = RedGive),
            ) {
                Text("YOU GAVE ₹", style = MaterialTheme.typography.labelLarge)
            }
            Button(
                onClick = onGot,
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = GreenGet),
            ) {
                Text("YOU GOT ₹", style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}
