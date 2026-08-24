@file:OptIn(ExperimentalMaterial3Api::class)

package com.khatabook.clone.ui.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.khatabook.clone.R
import com.khatabook.clone.ui.common.LabeledField
import com.khatabook.clone.ui.theme.KhataTheme

@Composable
fun LoginScreen(onOtpSent: (phone: String, devOtp: String?) -> Unit) {
    val viewModel: LoginViewModel = viewModel()
    val state = viewModel.state
    val palette = KhataTheme.colors

    LaunchedEffect(state.otpSent) {
        if (state.otpSent) {
            viewModel.consumeOtpSent()
            onOtpSent(state.phone, state.devOtp)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(palette.screen),
    ) {
        // A short gradient band instead of a full navy screen: the brand is
        // present, the form still sits on the light ground where it belongs.
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(196.dp)
                .background(
                    Brush.verticalGradient(listOf(palette.headerTop, palette.headerBottom))
                ),
            contentAlignment = Alignment.Center,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Image(
                    painter = painterResource(R.drawable.ic_launcher_foreground),
                    contentDescription = null,
                    modifier = Modifier.size(84.dp),
                )
                Text(
                    text = "Khatabook",
                    color = palette.onHeader,
                    style = MaterialTheme.typography.headlineSmall,
                )
            }
        }

        Column(modifier = Modifier.padding(24.dp)) {
            Spacer(Modifier.height(6.dp))
            Text(
                text = "Enter your mobile number",
                style = MaterialTheme.typography.headlineSmall,
                color = palette.textPrimary,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = "We will send a 6 digit code to verify it is you.",
                style = MaterialTheme.typography.bodyLarge,
                color = palette.textSecondary,
            )

            Spacer(Modifier.height(24.dp))
            LabeledField(
                label = "Mobile number",
                value = state.phone,
                placeholder = "10 digit number",
                onValueChange = viewModel::onPhoneChange,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                prefix = { Text("+91  ", color = palette.textSecondary) },
            )

            if (state.error != null) {
                Spacer(Modifier.height(10.dp))
                Text(
                    text = state.error,
                    color = palette.give,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }

            Spacer(Modifier.height(22.dp))
            Button(
                onClick = viewModel::sendOtp,
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
                    Text("Continue", style = MaterialTheme.typography.labelLarge)
                }
            }
        }

        Spacer(Modifier.weight(1f))
        Text(
            text = "By continuing you agree to the Terms of Service and Privacy Policy",
            style = MaterialTheme.typography.bodyMedium,
            color = palette.textFaint,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp, vertical = 20.dp),
        )
    }
}
