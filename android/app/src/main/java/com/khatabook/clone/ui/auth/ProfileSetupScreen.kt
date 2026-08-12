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
import com.khatabook.clone.ui.theme.Divider
import com.khatabook.clone.ui.theme.Navy
import com.khatabook.clone.ui.theme.RedGive
import com.khatabook.clone.ui.theme.ScreenBg
import com.khatabook.clone.ui.theme.SurfaceWhite
import com.khatabook.clone.ui.theme.TextSecondary

@Composable
fun ProfileSetupScreen(onDone: () -> Unit) {
    val viewModel: ProfileSetupViewModel = viewModel()
    val state = viewModel.state

    LaunchedEffect(state.saved) {
        if (state.saved) onDone()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ScreenBg)
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
            color = TextSecondary,
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
            Text(text = state.error, color = RedGive, style = MaterialTheme.typography.bodyMedium)
        }

        Spacer(Modifier.weight(1f))
        Button(
            onClick = viewModel::save,
            enabled = state.canSubmit,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Navy),
        ) {
            if (state.loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(22.dp),
                    color = SurfaceWhite,
                    strokeWidth = 2.dp,
                )
            } else {
                Text("Continue", style = MaterialTheme.typography.labelLarge)
            }
        }
        Spacer(Modifier.height(12.dp))
    }
}

@Composable
fun LabeledField(
    label: String,
    value: String,
    placeholder: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    keyboardOptions: androidx.compose.foundation.text.KeyboardOptions =
        androidx.compose.foundation.text.KeyboardOptions.Default,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = TextSecondary,
        )
        Spacer(Modifier.height(6.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(placeholder, color = TextSecondary) },
            singleLine = true,
            shape = RoundedCornerShape(10.dp),
            keyboardOptions = keyboardOptions,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = SurfaceWhite,
                unfocusedContainerColor = SurfaceWhite,
                focusedBorderColor = Navy,
                unfocusedBorderColor = Divider,
            ),
        )
    }
}
