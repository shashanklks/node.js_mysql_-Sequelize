@file:OptIn(ExperimentalMaterial3Api::class)

package com.khatabook.clone.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.khatabook.clone.ui.common.KhataTopBar
import com.khatabook.clone.ui.theme.KhataTheme

@Composable
fun OtpScreen(
    phone: String,
    devOtp: String?,
    onBack: () -> Unit,
    onVerified: (needsProfile: Boolean) -> Unit,
) {
    val viewModel: OtpViewModel = viewModel()
    val state = viewModel.state
    val palette = KhataTheme.colors
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        viewModel.startResendTimer()
        // The dev server hands back the code, so the field autofills the way
        // SMS retriever would on a real device.
        if (!devOtp.isNullOrBlank()) viewModel.onCodeChange(devOtp)
        focusRequester.requestFocus()
    }

    LaunchedEffect(state.verified) {
        if (state.verified) onVerified(state.needsProfile)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(palette.screen),
    ) {
        KhataTopBar(title = "Verify mobile number", onBack = onBack)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(24.dp))
            Text(
                text = "Enter the OTP sent to",
                style = MaterialTheme.typography.bodyLarge,
                color = palette.textSecondary,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "+91 $phone",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )

            Spacer(Modifier.height(28.dp))
            OtpBoxes(
                code = state.code,
                onCodeChange = viewModel::onCodeChange,
                focusRequester = focusRequester,
            )

            if (state.error != null) {
                Spacer(Modifier.height(12.dp))
                Text(text = state.error, color = palette.give, style = MaterialTheme.typography.bodyMedium)
            }

            Spacer(Modifier.height(16.dp))
            if (state.resendSeconds > 0) {
                Text(
                    text = "Resend OTP in 00:${state.resendSeconds.toString().padStart(2, '0')}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = palette.textSecondary,
                )
            } else {
                TextButton(onClick = { viewModel.resend(phone) }) {
                    Text("Resend OTP", color = palette.brand, style = MaterialTheme.typography.labelLarge)
                }
            }

            Spacer(Modifier.height(24.dp))
            Button(
                onClick = { viewModel.verify(phone) },
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
                    Text("Verify", style = MaterialTheme.typography.labelLarge)
                }
            }

            Spacer(Modifier.weight(1f))
            Text(
                text = "Did not receive the code? Check that the API server is reachable.",
                style = MaterialTheme.typography.bodyMedium,
                color = palette.textSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 12.dp),
            )
        }
    }
}

/** One hidden field behind six boxes — the usual OTP input trick. */
@Composable
private fun OtpBoxes(
    code: String,
    onCodeChange: (String) -> Unit,
    focusRequester: FocusRequester,
) {
    val palette = KhataTheme.colors
    BasicTextField(
        value = code,
        onValueChange = onCodeChange,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
        modifier = Modifier.focusRequester(focusRequester),
        decorationBox = {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                repeat(6) { index ->
                    val char = code.getOrNull(index)
                    Box(
                        modifier = Modifier
                            .size(width = 44.dp, height = 54.dp)
                            .background(palette.surface, RoundedCornerShape(8.dp))
                            .border(
                                width = if (char != null) 2.dp else 1.dp,
                                color = if (char != null) palette.brand else palette.line,
                                shape = RoundedCornerShape(8.dp),
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = char?.toString() ?: "",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
            }
        },
    )
}
