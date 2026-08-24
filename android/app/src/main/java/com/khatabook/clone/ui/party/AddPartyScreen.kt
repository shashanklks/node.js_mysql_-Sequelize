@file:OptIn(ExperimentalMaterial3Api::class)

package com.khatabook.clone.ui.party

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.khatabook.clone.data.remote.PartyType
import com.khatabook.clone.ui.common.KhataTopBar
import com.khatabook.clone.ui.common.LabeledField
import com.khatabook.clone.ui.common.SegmentedTabs
import com.khatabook.clone.ui.theme.KhataTheme

@Composable
fun AddPartyScreen(
    initialType: String,
    onBack: () -> Unit,
    onCreated: (partyId: Int) -> Unit,
) {
    val viewModel: AddPartyViewModel = viewModel()
    val state = viewModel.state
    val palette = KhataTheme.colors

    LaunchedEffect(initialType) { viewModel.setType(initialType) }
    LaunchedEffect(state.createdId) { state.createdId?.let(onCreated) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(palette.screen),
    ) {
        KhataTopBar(
            title = if (state.type == PartyType.CUSTOMER) "Add customer" else "Add supplier",
            onBack = onBack,
        )

        Column(modifier = Modifier.padding(16.dp)) {
            SegmentedTabs(
                options = listOf("Customer", "Supplier"),
                selectedIndex = if (state.type == PartyType.CUSTOMER) 0 else 1,
                onSelect = {
                    viewModel.setType(if (it == 0) PartyType.CUSTOMER else PartyType.SUPPLIER)
                },
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = if (state.type == PartyType.CUSTOMER) {
                    "Someone who buys from you and may owe you money."
                } else {
                    "Someone you buy from and may owe money to."
                },
                style = MaterialTheme.typography.bodyMedium,
                color = palette.textFaint,
            )

            Spacer(Modifier.height(22.dp))
            LabeledField(
                label = "Name",
                value = state.name,
                placeholder = "e.g. Suresh Traders",
                onValueChange = viewModel::onNameChange,
            )
            Spacer(Modifier.height(16.dp))
            LabeledField(
                label = "Mobile number (optional)",
                value = state.phone,
                placeholder = "10 digit number",
                onValueChange = viewModel::onPhoneChange,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                prefix = { Text("+91  ", color = palette.textSecondary) },
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "A number lets you call them straight from the ledger.",
                style = MaterialTheme.typography.bodyMedium,
                color = palette.textFaint,
            )

            if (state.error != null) {
                Spacer(Modifier.height(12.dp))
                Text(
                    text = state.error,
                    color = palette.give,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }

            Spacer(Modifier.height(26.dp))
            Button(
                onClick = viewModel::save,
                enabled = state.canSubmit,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = palette.brand,
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
                    Text("Save and open ledger", style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }
}
