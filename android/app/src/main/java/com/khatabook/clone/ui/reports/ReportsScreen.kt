@file:OptIn(ExperimentalMaterial3Api::class)

package com.khatabook.clone.ui.reports

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.khatabook.clone.data.remote.EntryType
import com.khatabook.clone.data.remote.PartyType
import com.khatabook.clone.data.remote.ReportEntryDto
import com.khatabook.clone.ui.common.AnimatedMoneyText
import com.khatabook.clone.ui.common.Avatar
import com.khatabook.clone.ui.common.ChartLegend
import com.khatabook.clone.ui.common.DailyFlowChart
import com.khatabook.clone.ui.common.EmptyState
import com.khatabook.clone.ui.common.KhataCard
import com.khatabook.clone.ui.common.KhataTopBar
import com.khatabook.clone.ui.common.LoadingBox
import com.khatabook.clone.ui.common.MoneyText
import com.khatabook.clone.ui.common.ProportionBar
import com.khatabook.clone.ui.common.RowDivider
import com.khatabook.clone.ui.common.ScreenMessage
import com.khatabook.clone.ui.common.SectionLabel
import com.khatabook.clone.ui.common.friendlyDate
import com.khatabook.clone.ui.home.HomeBottomBar
import com.khatabook.clone.ui.theme.KhataTheme

@Composable
fun ReportsScreen(
    onBack: () -> Unit,
    onHome: () -> Unit,
    onProfile: () -> Unit,
) {
    val viewModel: ReportsViewModel = viewModel()
    val state = viewModel.state
    val palette = KhataTheme.colors

    Scaffold(
        containerColor = palette.screen,
        topBar = { KhataTopBar(title = "Reports", subtitle = "Your cashbook", onBack = onBack) },
        bottomBar = {
            HomeBottomBar(selected = 1, onHome = onHome, onReports = {}, onProfile = onProfile)
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ReportRange.entries.forEach { option ->
                        Chip(
                            label = option.label,
                            selected = state.range == option,
                            modifier = Modifier.weight(1f),
                        ) { viewModel.setRange(option) }
                    }
                }
            }

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Chip("All parties", state.partyType == null, Modifier.weight(1f)) {
                        viewModel.setPartyType(null)
                    }
                    Chip("Customers", state.partyType == PartyType.CUSTOMER, Modifier.weight(1f)) {
                        viewModel.setPartyType(PartyType.CUSTOMER)
                    }
                    Chip("Suppliers", state.partyType == PartyType.SUPPLIER, Modifier.weight(1f)) {
                        viewModel.setPartyType(PartyType.SUPPLIER)
                    }
                }
            }

            item { PositionCard(state.overallGet, state.overallGive) }

            item {
                FlowCard(
                    gave = state.gave,
                    got = state.got,
                    net = state.net,
                    days = state.days,
                    rangeLabel = state.range.label,
                )
            }

            when {
                state.loading -> item { LoadingBox(Modifier.height(160.dp)) }

                state.error != null -> item {
                    ScreenMessage(state.error) {
                        Button(
                            onClick = viewModel::retry,
                            colors = ButtonDefaults.buttonColors(containerColor = palette.brand),
                            shape = RoundedCornerShape(10.dp),
                        ) { Text("Try again") }
                    }
                }

                state.entries.isEmpty() -> item {
                    EmptyState(
                        icon = "📊",
                        title = "Nothing in this period",
                        message = "Entries you record show up here with the totals for the window.",
                    )
                }

                else -> {
                    item { SectionLabel("Every entry", Modifier.padding(top = 6.dp)) }
                    items(state.entries, key = { it.id }) { entry ->
                        KhataCard { ReportRow(entry) }
                    }
                }
            }
        }
    }
}

/** Where the book stands overall — independent of the date filter. */
@Composable
private fun PositionCard(youWillGet: Double, youWillGive: Double) {
    val palette = KhataTheme.colors
    KhataCard {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Your position",
                style = MaterialTheme.typography.labelSmall,
                color = palette.textFaint,
            )
            Spacer(Modifier.height(10.dp))
            Row {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "You will get",
                        style = MaterialTheme.typography.bodyMedium,
                        color = palette.textSecondary,
                    )
                    AnimatedMoneyText(
                        amount = youWillGet,
                        style = MaterialTheme.typography.headlineSmall,
                        color = palette.get,
                    )
                }
                Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                    Text(
                        text = "You will give",
                        style = MaterialTheme.typography.bodyMedium,
                        color = palette.textSecondary,
                    )
                    AnimatedMoneyText(
                        amount = youWillGive,
                        style = MaterialTheme.typography.headlineSmall,
                        color = palette.give,
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
            ProportionBar(get = youWillGet, give = youWillGive)
        }
    }
}

/** What actually moved during the selected window, day by day. */
@Composable
private fun FlowCard(
    gave: Double,
    got: Double,
    net: Double,
    days: List<com.khatabook.clone.ui.common.DayFlow>,
    rangeLabel: String,
) {
    val palette = KhataTheme.colors
    KhataCard {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Cash flow · $rangeLabel",
                    style = MaterialTheme.typography.labelSmall,
                    color = palette.textFaint,
                    modifier = Modifier.weight(1f),
                )
                ChartLegend()
            }

            Spacer(Modifier.height(14.dp))
            if (days.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(96.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "No movement in this window",
                        style = MaterialTheme.typography.bodyMedium,
                        color = palette.textFaint,
                    )
                }
            } else {
                DailyFlowChart(days = days)
            }

            Spacer(Modifier.height(14.dp))
            RowDivider()
            Spacer(Modifier.height(12.dp))
            Row {
                Cell("You gave", gave, palette.give, Modifier.weight(1f))
                Cell("You got", got, palette.get, Modifier.weight(1f))
                Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Net",
                        style = MaterialTheme.typography.bodyMedium,
                        color = palette.textFaint,
                    )
                    MoneyText(amount = net, signed = true)
                }
            }
        }
    }
}

@Composable
private fun Cell(label: String, amount: Double, color: Color, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = KhataTheme.colors.textFaint,
        )
        MoneyText(amount = amount, color = color)
    }
}

@Composable
private fun Chip(
    label: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val palette = KhataTheme.colors
    Box(
        modifier = modifier
            .height(34.dp)
            .clip(RoundedCornerShape(9.dp))
            .background(if (selected) palette.brand else palette.surface)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = if (selected) palette.onHeader else palette.textSecondary,
            maxLines = 1,
        )
    }
}

@Composable
private fun ReportRow(entry: ReportEntryDto) {
    val palette = KhataTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Avatar(name = entry.partyName ?: "?", size = 38)
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = entry.partyName ?: "Unknown",
                style = MaterialTheme.typography.titleMedium,
                color = palette.textPrimary,
                maxLines = 1,
            )
            Text(
                text = listOfNotNull(
                    friendlyDate(entry.entryDate),
                    entry.note?.takeIf { it.isNotBlank() },
                ).joinToString(" · "),
                style = MaterialTheme.typography.bodyMedium,
                color = palette.textFaint,
                maxLines = 1,
            )
        }
        MoneyText(
            amount = entry.amount,
            color = if (entry.type == EntryType.GAVE) palette.give else palette.get,
        )
    }
}
