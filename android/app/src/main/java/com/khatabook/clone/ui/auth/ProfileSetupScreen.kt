@file:OptIn(ExperimentalMaterial3Api::class)

package com.khatabook.clone.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.khatabook.clone.ui.common.LabeledField
import com.khatabook.clone.ui.theme.KhataTheme

@Composable
fun ProfileSetupScreen(onDone: () -> Unit) {
    val viewModel: ProfileSetupViewModel = viewModel()
    val state = viewModel.state
    val palette = KhataTheme.colors

    LaunchedEffect(state.saved) {
        if (state.saved) onDone()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(palette.screen)
            .padding(24.dp),
    ) {
        Spacer(Modifier.height(48.dp))
        Text(
            text = "Tell us about you",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = "This name shows up on the receipts you share with customers",
            style = MaterialTheme.typography.bodyLarge,
            color = palette.textSecondary,
        )

        Spacer(Modifier.height(28.dp))
        LabeledField(
            label = "Your name",
            value = state.name,
            placeholder = "e.g. Ramesh Kumar",
            onValueChange = viewModel::onNameChange,
        )
        Spacer(Modifier.height(16.dp))
        LabeledField(
            label = "Business name (optional)",
            value = state.businessName,
            placeholder = "e.g. Ramesh General Store",
            onValueChange = viewModel::onBusinessChange,
        )

        if (state.error != null) {
            Spacer(Modifier.height(12.dp))
            Text(text = state.error, color = palette.give, style = MaterialTheme.typography.bodyMedium)
        }

        Spacer(Modifier.weight(1f))
        Button(
            onClick = viewModel::save,
            enabled = state.canSubmit,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(containerColor = palette.brand),
        ) {
            if (state.loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(22.dp),
                    color = palette.surface,
                    strokeWidth = 2.dp,
                )
            } else {
                Text("Continue", style = MaterialTheme.typography.labelLarge)
            }
        }
        Spacer(Modifier.height(12.dp))
    }
}
