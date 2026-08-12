@file:OptIn(ExperimentalMaterial3Api::class)

package com.khatabook.clone.ui.home

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.khatabook.clone.data.remote.PartyDto
import com.khatabook.clone.data.remote.PartyType
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
import com.khatabook.clone.ui.theme.TextOnNavy
import com.khatabook.clone.ui.theme.TextOnNavyMuted
import com.khatabook.clone.ui.theme.TextSecondary

@Composable
fun HomeScreen(
    onOpenParty: (Int) -> Unit,
    onAddParty: (String) -> Unit,
    onOpenReports: () -> Unit,
    onOpenProfile: () -> Unit,
) {
    val viewModel: HomeViewModel = viewModel()
    val state = viewModel.state

    // Balances change on every ledger edit, so the list refreshes on return.
    LaunchedEffect(Unit) { viewModel.load(showSpinner = false) }

    Scaffold(
        containerColor = ScreenBg,
        topBar = {
            HomeHeader(
                businessName = state.businessName,
                searching = state.searching,
                query = state.query,
                onQueryChange = viewModel::onQueryChange,
                onToggleSearch = viewModel::toggleSearch,
                onOpenProfile = onOpenProfile,
            )
        },
        bottomBar = {
            HomeBottomBar(
                selected = 0,
                onHome = {},
                onReports = onOpenReports,
                onProfile = onOpenProfile,
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { onAddParty(state.tab) },
                containerColor = Navy,
                contentColor = TextOnNavy,
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(
                    text = if (state.tab == PartyType.CUSTOMER) "ADD CUSTOMER" else "ADD SUPPLIER",
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
            SummaryCard(
                youWillGet = state.summary.youWillGet,
                youWillGive = state.summary.youWillGive,
                onViewReport = onOpenReports,
            )

            TabRow(
                selectedTabIndex = if (state.tab == PartyType.CUSTOMER) 0 else 1,
                containerColor = com.khatabook.clone.ui.theme.SurfaceWhite,
                contentColor = Navy,
            ) {
                Tab(
                    selected = state.tab == PartyType.CUSTOMER,
                    onClick = { viewModel.selectTab(PartyType.CUSTOMER) },
                    text = { Text("Customers", style = MaterialTheme.typography.labelLarge) },
                    selectedContentColor = Navy,
                    unselectedContentColor = TextSecondary,
                )
                Tab(
                    selected = state.tab == PartyType.SUPPLIER,
                    onClick = { viewModel.selectTab(PartyType.SUPPLIER) },
                    text = { Text("Suppliers", style = MaterialTheme.typography.labelLarge) },
                    selectedContentColor = Navy,
                    unselectedContentColor = TextSecondary,
                )
            }

            when {
                state.loading -> LoadingBox(Modifier.weight(1f))
                state.error != null -> Column(Modifier.weight(1f)) { ScreenMessage(state.error) }
                state.parties.isEmpty() -> Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    EmptyState(
                        title = if (state.query.isBlank()) {
                            "No ${if (state.tab == PartyType.CUSTOMER) "customers" else "suppliers"} yet"
                        } else {
                            "Nothing matched \"${state.query}\""
                        },
                        message = "Add a name and start recording what you gave and what you got.",
                    )
                }

                else -> LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .background(com.khatabook.clone.ui.theme.SurfaceWhite),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 96.dp),
                ) {
                    items(state.parties, key = { it.id }) { party ->
                        PartyRow(party = party, onClick = { onOpenParty(party.id) })
                        RowDivider(Modifier.padding(start = 72.dp))
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
    onQueryChange: (String) -> Unit,
    onToggleSearch: () -> Unit,
    onOpenProfile: () -> Unit,
) {
    Surface(color = Navy) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (searching) {
                TextField(
                    value = query,
                    onValueChange = onQueryChange,
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Search by name or number", color = TextOnNavyMuted) },
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Navy,
                        unfocusedContainerColor = Navy,
                        focusedTextColor = TextOnNavy,
                        unfocusedTextColor = TextOnNavy,
                        focusedIndicatorColor = TextOnNavyMuted,
                        unfocusedIndicatorColor = TextOnNavyMuted,
                        cursorColor = TextOnNavy,
                    ),
                )
                IconButton(onClick = onToggleSearch) {
                    Icon(Icons.Default.Close, contentDescription = "Close search", tint = TextOnNavy)
                }
            } else {
                Box(
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .size(36.dp)
                        .background(TextOnNavy.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                        .clickable(onClick = onOpenProfile),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = businessName.take(1).uppercase(),
                        color = TextOnNavy,
                        fontWeight = FontWeight.Bold,
                    )
                }
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = businessName,
                        color = TextOnNavy,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                    )
                    Text(
                        text = "Your business ledger",
                        color = TextOnNavyMuted,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 1,
                    )
                }
                IconButton(onClick = onToggleSearch) {
                    Icon(Icons.Default.Search, contentDescription = "Search", tint = TextOnNavy)
                }
            }
        }
    }
}

@Composable
private fun SummaryCard(youWillGet: Double, youWillGive: Double, onViewReport: () -> Unit) {
    Surface(
        color = com.khatabook.clone.ui.theme.SurfaceWhite,
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        shape = RoundedCornerShape(12.dp),
        shadowElevation = 2.dp,
    ) {
        Column {
            Row(modifier = Modifier.height(78.dp)) {
                SummaryCell(
                    label = "You will get",
                    amount = youWillGet,
                    color = GreenGet,
                    modifier = Modifier.weight(1f),
                )
                Box(
                    Modifier
                        .width(1.dp)
                        .height(78.dp)
                        .background(Divider)
                )
                SummaryCell(
                    label = "You will give",
                    amount = youWillGive,
                    color = RedGive,
                    modifier = Modifier.weight(1f),
                )
            }
            RowDivider()
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onViewReport)
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    Icons.Default.PieChart,
                    contentDescription = null,
                    tint = Navy,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "VIEW REPORT",
                    color = Navy,
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        }
    }
}

@Composable
private fun SummaryCell(
    label: String,
    amount: Double,
    color: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.Center,
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
        Spacer(Modifier.height(4.dp))
        Text(
            text = rupees(amount),
            style = MaterialTheme.typography.titleLarge,
            color = color,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun PartyRow(party: PartyDto, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Avatar(name = party.name)
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = party.name,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
            )
            Text(
                text = party.lastEntryDate?.let { friendlyDate(it) } ?: "No entries yet",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
            )
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = rupees(party.balance),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = when {
                    party.balance > 0 -> GreenGet
                    party.balance < 0 -> RedGive
                    else -> TextSecondary
                },
            )
            if (party.balance != 0.0) {
                Text(
                    text = if (party.balance > 0) "You will get" else "You will give",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary,
                )
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
    NavigationBar(containerColor = com.khatabook.clone.ui.theme.SurfaceWhite) {
        val colors = NavigationBarItemDefaults.colors(
            selectedIconColor = Navy,
            selectedTextColor = Navy,
            unselectedIconColor = TextSecondary,
            unselectedTextColor = TextSecondary,
            indicatorColor = com.khatabook.clone.ui.theme.AvatarBg,
        )
        NavigationBarItem(
            selected = selected == 0,
            onClick = onHome,
            icon = { Icon(Icons.Default.Home, contentDescription = null) },
            label = { Text("Home") },
            colors = colors,
        )
        NavigationBarItem(
            selected = selected == 1,
            onClick = onReports,
            icon = { Icon(Icons.Default.PieChart, contentDescription = null) },
            label = { Text("Reports") },
            colors = colors,
        )
        NavigationBarItem(
            selected = selected == 2,
            onClick = onProfile,
            icon = { Icon(Icons.Default.Person, contentDescription = null) },
            label = { Text("Profile") },
            colors = colors,
        )
    }
}
