@file:OptIn(ExperimentalMaterial3Api::class)

package com.khatabook.clone.ui.party

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.khatabook.clone.data.remote.PartyType
import com.khatabook.clone.ui.auth.LabeledField
import com.khatabook.clone.ui.common.KhataTopBar
import com.khatabook.clone.ui.theme.Divider
import com.khatabook.clone.ui.theme.Navy
import com.khatabook.clone.ui.theme.RedGive
import com.khatabook.clone.ui.theme.ScreenBg
import com.khatabook.clone.ui.theme.SurfaceWhite
import com.khatabook.clone.ui.theme.TextOnNavy
import com.khatabook.clone.ui.theme.TextSecondary

@Composable
fun AddPartyScreen(
    initialType: String,
    onBack: () -> Unit,
    onCreated: (partyId: Int) -> Unit,
) {
    val viewModel: AddPartyViewModel = viewModel()
    val state = viewModel.state

    LaunchedEffect(initialType) { viewModel.setType(initialType) }
    LaunchedEffect(state.createdId) {
        state.createdId?.let(onCreated)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ScreenBg),
    ) {
        KhataTopBar(
            title = if (state.type == PartyType.CUSTOMER) "Add customer" else "Add supplier",
            onBack = onBack,
        )

        Column(modifier = Modifier.padding(16.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                TypeChip(
                    label = "Customer",
                    selected = state.type == PartyType.CUSTOMER,
                    onClick = { viewModel.setType(PartyType.CUSTOMER) },
                )
                TypeChip(
                    label = "Supplier",
                    selected = state.type == PartyType.SUPPLIER,
                    onClick = { viewModel.setType(PartyType.SUPPLIER) },
                )
            }

            Spacer(Modifier.height(20.dp))
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
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "A number lets you call them straight from the ledger.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
            )

            if (state.error != null) {
                Spacer(Modifier.height(12.dp))
                Text(text = state.error, color = RedGive, style = MaterialTheme.typography.bodyMedium)
            }

            Spacer(Modifier.height(24.dp))
            Button(
                onClick = viewModel::save,
                enabled = state.canSubmit,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Navy,
                    disabledContainerColor = Divider,
                ),
            ) {
                if (state.loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        color = SurfaceWhite,
                        strokeWidth = 2.dp,
                    )
                } else {
                    Text("Save", style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }
}

@Composable
private fun TypeChip(label: String, selected: Boolean, onClick: () -> Unit) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label) },
        shape = RoundedCornerShape(8.dp),
        colors = FilterChipDefaults.filterChipColors(
            containerColor = SurfaceWhite,
            selectedContainerColor = Navy,
            labelColor = TextSecondary,
            selectedLabelColor = TextOnNavy,
        ),
    )
}
