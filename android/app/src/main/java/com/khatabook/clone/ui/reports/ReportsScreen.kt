@file:OptIn(ExperimentalMaterial3Api::class)

package com.khatabook.clone.ui.reports

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.khatabook.clone.data.remote.EntryType
import com.khatabook.clone.data.remote.PartyType
import com.khatabook.clone.data.remote.ReportEntryDto
import com.khatabook.clone.ui.common.Avatar
import com.khatabook.clone.ui.common.EmptyState
import com.khatabook.clone.ui.common.KhataTopBar
import com.khatabook.clone.ui.common.LoadingBox
import com.khatabook.clone.ui.common.RowDivider
import com.khatabook.clone.ui.common.ScreenMessage
import com.khatabook.clone.ui.common.friendlyDate
import com.khatabook.clone.ui.common.rupees
import com.khatabook.clone.ui.home.HomeBottomBar
import com.khatabook.clone.ui.theme.GreenGet
import com.khatabook.clone.ui.theme.Navy
import com.khatabook.clone.ui.theme.RedGive
import com.khatabook.clone.ui.theme.ScreenBg
import com.khatabook.clone.ui.theme.SurfaceWhite
import com.khatabook.clone.ui.theme.TextSecondary

@Composable
fun ReportsScreen(
    onBack: () -> Unit,
    onHome: () -> Unit,
    onProfile: () -> Unit,
) {
    val viewModel: ReportsViewModel = viewModel()
    val state = viewModel.state

    Scaffold(
        containerColor = ScreenBg,
        topBar = { KhataTopBar(title = "Reports", subtitle = "Your cashbook", onBack = onBack) },
        bottomBar = {
            HomeBottomBar(selected = 1, onHome = onHome, onReports = {}, onProfile = onProfile)
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            TotalsCard(
                youWillGet = state.overallGet,
                youWillGive = state.overallGive,
                gave = state.gave,
                got = state.got,
            )

            Row(
                modifier = Modifier.padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                RangeChip("This month", state.range == ReportRange.MONTH) {
                    viewModel.setRange(ReportRange.MONTH)
                }
                RangeChip("Last 7 days", state.range == ReportRange.WEEK) {
                    viewModel.setRange(ReportRange.WEEK)
                }
                RangeChip("All time", state.range == ReportRange.ALL) {
                    viewModel.setRange(ReportRange.ALL)
                }
            }

            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                RangeChip("All parties", state.partyType == null) { viewModel.setPartyType(null) }
                RangeChip("Customers", state.partyType == PartyType.CUSTOMER) {
                    viewModel.setPartyType(PartyType.CUSTOMER)
                }
                RangeChip("Suppliers", state.partyType == PartyType.SUPPLIER) {
                    viewModel.setPartyType(PartyType.SUPPLIER)
                }
            }
            Spacer(Modifier.height(8.dp))

            when {
                state.loading -> LoadingBox(Modifier.weight(1f))
                state.error != null -> Column(Modifier.weight(1f)) { ScreenMessage(state.error) }
                state.entries.isEmpty() -> Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    EmptyState(
                        title = "Nothing in this period",
                        message = "Entries you record show up here with the running totals.",
                    )
                }

                else -> LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .background(SurfaceWhite),
                ) {
                    items(state.entries, key = { it.id }) { entry ->
                        ReportRow(entry)
                        RowDivider(Modifier.padding(start = 72.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun TotalsCard(youWillGet: Double, youWillGive: Double, gave: Double, got: Double) {
    Surface(
        color = SurfaceWhite,
        shape = RoundedCornerShape(12.dp),
        shadowElevation = 2.dp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row {
                TotalCell("You will get", youWillGet, GreenGet, Modifier.weight(1f))
                TotalCell("You will give", youWillGive, RedGive, Modifier.weight(1f))
            }
            Spacer(Modifier.height(14.dp))
            RowDivider()
            Spacer(Modifier.height(14.dp))
            Row {
                TotalCell("Given in period", gave, RedGive, Modifier.weight(1f))
                TotalCell("Received in period", got, GreenGet, Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun TotalCell(label: String, amount: Double, color: Color, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
        Spacer(Modifier.height(2.dp))
        Text(
            text = rupees(amount),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = color,
        )
    }
}

@Composable
private fun RangeChip(label: String, selected: Boolean, onClick: () -> Unit) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label, style = MaterialTheme.typography.labelSmall) },
        shape = RoundedCornerShape(8.dp),
        colors = FilterChipDefaults.filterChipColors(
            containerColor = SurfaceWhite,
            selectedContainerColor = Navy,
            labelColor = TextSecondary,
            selectedLabelColor = SurfaceWhite,
        ),
    )
}

@Composable
private fun ReportRow(entry: ReportEntryDto) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Avatar(name = entry.partyName ?: "?", size = 40)
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = entry.partyName ?: "Unknown",
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
            )
            Text(
                text = listOfNotNull(
                    friendlyDate(entry.entryDate),
                    entry.note?.takeIf { it.isNotBlank() },
                ).joinToString(" · "),
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                maxLines = 1,
            )
        }
        Text(
            text = rupees(entry.amount),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = if (entry.type == EntryType.GAVE) RedGive else GreenGet,
            textAlign = TextAlign.End,
        )
    }
}
