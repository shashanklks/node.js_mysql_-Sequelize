@file:OptIn(ExperimentalMaterial3Api::class)

package com.khatabook.clone.ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.khatabook.clone.data.remote.EntryType
import com.khatabook.clone.data.remote.PartyDto
import com.khatabook.clone.data.remote.PartyType
import com.khatabook.clone.ui.common.AnimatedMoneyText
import com.khatabook.clone.ui.common.Avatar
import com.khatabook.clone.ui.common.EmptyState
import com.khatabook.clone.ui.common.HeaderSurface
import com.khatabook.clone.ui.common.KhataCard
import com.khatabook.clone.ui.common.LoadingBox
import com.khatabook.clone.ui.common.MoneyText
import com.khatabook.clone.ui.common.Pill
import com.khatabook.clone.ui.common.ProportionBar
import com.khatabook.clone.ui.common.RowDivider
import com.khatabook.clone.ui.common.ScreenMessage
import com.khatabook.clone.ui.common.SegmentedTabs
import com.khatabook.clone.ui.common.friendlyDate
import com.khatabook.clone.ui.theme.KhataTheme

@Composable
fun HomeScreen(
    onOpenParty: (Int) -> Unit,
    onAddParty: (String) -> Unit,
    onQuickEntry: (partyId: Int, type: String) -> Unit,
    onOpenReports: () -> Unit,
    onOpenProfile: () -> Unit,
) {
    val viewModel: HomeViewModel = viewModel()
    val state = viewModel.state
    val palette = KhataTheme.colors

    // Balances change on every ledger edit, so the list refreshes on return.
    LaunchedEffect(Unit) { viewModel.load(showSpinner = false) }

    Scaffold(
        containerColor = palette.screen,
        topBar = {
            HomeHeader(
                businessName = state.businessName,
                searching = state.searching,
                query = state.query,
                sort = state.sort,
                onQueryChange = viewModel::onQueryChange,
                onToggleSearch = viewModel::toggleSearch,
                onSort = viewModel::setSort,
                onOpenProfile = onOpenProfile,
                summary = {
                    SummaryCard(
                        youWillGet = state.summary.youWillGet,
                        youWillGive = state.summary.youWillGive,
                        onViewReport = onOpenReports,
                    )
                },
            )
        },
        bottomBar = {
            HomeBottomBar(selected = 0, onHome = {}, onReports = onOpenReports, onProfile = onOpenProfile)
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { onAddParty(state.tab) },
                containerColor = palette.brand,
                contentColor = palette.onHeader,
                shape = RoundedCornerShape(16.dp),
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(
                    text = if (state.isCustomerTab) "Add customer" else "Add supplier",
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            SegmentedTabs(
                options = listOf("Customers", "Suppliers"),
                selectedIndex = if (state.isCustomerTab) 0 else 1,
                onSelect = {
                    viewModel.selectTab(if (it == 0) PartyType.CUSTOMER else PartyType.SUPPLIER)
                },
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            )

            when {
                state.loading -> LoadingBox(Modifier.weight(1f))

                state.error != null -> Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    ScreenMessage(state.error) {
                        Button(
                            onClick = { viewModel.load() },
                            colors = ButtonDefaults.buttonColors(containerColor = palette.brand),
                            shape = RoundedCornerShape(10.dp),
                        ) { Text("Try again") }
                    }
                }

                state.parties.isEmpty() -> Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    EmptyState(
                        icon = if (state.query.isBlank()) "📒" else "🔍",
                        title = if (state.query.isBlank()) {
                            "No ${if (state.isCustomerTab) "customers" else "suppliers"} yet"
                        } else {
                            "Nothing matched “${state.query}”"
                        },
                        message = if (state.query.isBlank()) {
                            "Add a name, then record what you gave and what you got."
                        } else {
                            "Try a different name or phone number."
                        },
                        action = {
                            if (state.query.isBlank()) {
                                Button(
                                    onClick = { onAddParty(state.tab) },
                                    colors = ButtonDefaults.buttonColors(containerColor = palette.brand),
                                    shape = RoundedCornerShape(10.dp),
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text(
                                        if (state.isCustomerTab) "Add first customer" else "Add first supplier",
                                        style = MaterialTheme.typography.labelLarge,
                                    )
                                }
                            }
                        },
                    )
                }

                else -> LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 12.dp),
                    contentPadding = PaddingValues(bottom = 96.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(state.parties, key = { it.id }) { party ->
                        PartyCard(
                            party = party,
                            onClick = { onOpenParty(party.id) },
                            onQuickEntry = { type -> onQuickEntry(party.id, type) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HomeHeader(
    businessName: String,
    searching: Boolean,
    query: String,
    sort: PartySort,
    onQueryChange: (String) -> Unit,
    onToggleSearch: () -> Unit,
    onSort: (PartySort) -> Unit,
    onOpenProfile: () -> Unit,
    summary: @Composable () -> Unit,
) {
    val palette = KhataTheme.colors
    var sortOpen by remember { mutableStateOf(false) }

    HeaderSurface {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(62.dp)
                    .padding(horizontal = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (searching) {
                    TextField(
                        value = query,
                        onValueChange = onQueryChange,
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Search name or number", color = palette.onHeaderMuted) },
                        singleLine = true,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = androidx.compose.ui.graphics.Color.Transparent,
                            unfocusedContainerColor = androidx.compose.ui.graphics.Color.Transparent,
                            focusedTextColor = palette.onHeader,
                            unfocusedTextColor = palette.onHeader,
                            focusedIndicatorColor = palette.onHeaderMuted,
                            unfocusedIndicatorColor = palette.onHeaderMuted,
                            cursorColor = palette.onHeader,
                        ),
                    )
                    IconButton(onClick = onToggleSearch) {
                        Icon(Icons.Default.Close, contentDescription = "Close search", tint = palette.onHeader)
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(palette.onHeader.copy(alpha = 0.16f))
                            .clickable(onClick = onOpenProfile),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = businessName.take(1).uppercase(),
                            color = palette.onHeader,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = businessName,
                            color = palette.onHeader,
                            style = MaterialTheme.typography.titleMedium,
                            maxLines = 1,
                        )
                        Text(
                            text = sort.label,
                            color = palette.onHeaderMuted,
                            style = MaterialTheme.typography.bodyMedium,
                            maxLines = 1,
                        )
                    }
                    Box {
                        IconButton(onClick = { sortOpen = true }) {
                            Icon(Icons.Default.SwapVert, contentDescription = "Sort", tint = palette.onHeader)
                        }
                        DropdownMenu(expanded = sortOpen, onDismissRequest = { sortOpen = false }) {
                            PartySort.entries.forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(option.label) },
                                    onClick = {
                                        onSort(option)
                                        sortOpen = false
                                    },
                                )
                            }
                        }
                    }
                    IconButton(onClick = onToggleSearch) {
                        Icon(Icons.Default.Search, contentDescription = "Search", tint = palette.onHeader)
                    }
                }
            }

            // The summary rides inside the header so the card overlaps the
            // gradient edge instead of floating on the grey below it.
            AnimatedVisibility(
                visible = !searching,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically(),
            ) {
                summary()
            }
        }
    }
}

@Composable
private fun SummaryCard(youWillGet: Double, youWillGive: Double, onViewReport: () -> Unit) {
    val palette = KhataTheme.colors
    KhataCard(modifier = Modifier.padding(start = 12.dp, end = 12.dp, bottom = 12.dp)) {
        Column {
            Row(modifier = Modifier.padding(16.dp)) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "You will get",
                        style = MaterialTheme.typography.bodyMedium,
                        color = palette.textSecondary,
                    )
                    Spacer(Modifier.height(3.dp))
                    AnimatedMoneyText(
                        amount = youWillGet,
                        style = MaterialTheme.typography.headlineSmall,
                        color = palette.get,
                    )
                }
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.End,
                ) {
                    Text(
                        text = "You will give",
                        style = MaterialTheme.typography.bodyMedium,
                        color = palette.textSecondary,
                    )
                    Spacer(Modifier.height(3.dp))
                    AnimatedMoneyText(
                        amount = youWillGive,
                        style = MaterialTheme.typography.headlineSmall,
                        color = palette.give,
                    )
                }
            }

            ProportionBar(
                get = youWillGet,
                give = youWillGive,
                modifier = Modifier.padding(horizontal = 16.dp),
            )

            Spacer(Modifier.height(14.dp))
            RowDivider()
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onViewReport)
                    .padding(vertical = 13.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    Icons.Default.PieChart,
                    contentDescription = null,
                    tint = palette.brand,
                    modifier = Modifier.size(17.dp),
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "View report",
                    color = palette.brand,
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        }
    }
}

/**
 * A party row carries its own "you got paid" shortcut. Collecting a payment is
 * the most common thing that happens at the counter, and it used to take three
 * taps to reach; here it takes one.
 */
@Composable
private fun PartyCard(party: PartyDto, onClick: () -> Unit, onQuickEntry: (String) -> Unit) {
    val palette = KhataTheme.colors
    val settled = party.balance == 0.0

    KhataCard(onClick = onClick) {
        Row(
            modifier = Modifier.padding(start = 14.dp, end = 8.dp, top = 12.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Avatar(name = party.name, size = 42)
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = party.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = palette.textPrimary,
                    maxLines = 1,
                )
                Spacer(Modifier.height(2.dp))
                if (settled) {
                    Pill(
                        text = "SETTLED",
                        background = palette.surfaceAlt,
                        contentColor = palette.textSecondary,
                    )
                } else {
                    Text(
                        text = party.lastEntryDate?.let { friendlyDate(it) } ?: "No entries yet",
                        style = MaterialTheme.typography.bodyMedium,
                        color = palette.textFaint,
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                MoneyText(amount = party.balance)
                if (!settled) {
                    Text(
                        text = if (party.balance > 0) "You will get" else "You will give",
                        style = MaterialTheme.typography.bodyMedium,
                        color = palette.textFaint,
                    )
                }
            }

            IconButton(onClick = { onQuickEntry(EntryType.GOT) }) {
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(palette.getSoft),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "Record payment received from ${party.name}",
                        tint = palette.get,
                        modifier = Modifier.size(17.dp),
                    )
                }
            }
        }
    }
}

@Composable
fun HomeBottomBar(
    selected: Int,
    onHome: () -> Unit,
    onReports: () -> Unit,
    onProfile: () -> Unit,
) {
    val palette = KhataTheme.colors
    NavigationBar(containerColor = palette.surface, tonalElevation = 0.dp) {
        val colors = NavigationBarItemDefaults.colors(
            selectedIconColor = palette.brand,
            selectedTextColor = palette.brand,
            unselectedIconColor = palette.textFaint,
            unselectedTextColor = palette.textFaint,
            indicatorColor = palette.avatarBg,
        )
        NavigationBarItem(
            selected = selected == 0,
            onClick = onHome,
            icon = { Icon(Icons.Default.Home, contentDescription = null) },
            label = { Text("Home", style = MaterialTheme.typography.labelMedium) },
            colors = colors,
        )
        NavigationBarItem(
            selected = selected == 1,
            onClick = onReports,
            icon = { Icon(Icons.Default.PieChart, contentDescription = null) },
            label = { Text("Reports", style = MaterialTheme.typography.labelMedium) },
            colors = colors,
        )
        NavigationBarItem(
            selected = selected == 2,
            onClick = onProfile,
            icon = { Icon(Icons.Default.Person, contentDescription = null) },
            label = { Text("Profile", style = MaterialTheme.typography.labelMedium) },
            colors = colors,
        )
    }
}
