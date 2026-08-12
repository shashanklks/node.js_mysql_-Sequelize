@file:OptIn(ExperimentalMaterial3Api::class)

package com.khatabook.clone.ui.auth

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.khatabook.clone.R
import com.khatabook.clone.ui.theme.Divider
import com.khatabook.clone.ui.theme.Navy
import com.khatabook.clone.ui.theme.RedGive
import com.khatabook.clone.ui.theme.ScreenBg
import com.khatabook.clone.ui.theme.SurfaceWhite
import com.khatabook.clone.ui.theme.TextSecondary

@Composable
fun LoginScreen(onOtpSent: (phone: String, devOtp: String?) -> Unit) {
    val viewModel: LoginViewModel = viewModel()
    val state = viewModel.state

    LaunchedEffect(state.otpSent) {
        if (state.otpSent) {
            viewModel.consumeOtpSent()
            onOtpSent(state.phone, state.devOtp)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ScreenBg)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(48.dp))
        Box(
            modifier = Modifier
                .size(96.dp)
                .background(Navy, RoundedCornerShape(24.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Image(
                painter = painterResource(R.drawable.ic_launcher_foreground),
                contentDescription = null,
                modifier = Modifier.size(80.dp),
            )
        }

        Spacer(Modifier.height(28.dp))
        Text(
            text = "Enter your mobile number",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = "We will send you a 6 digit verification code",
            style = MaterialTheme.typography.bodyLarge,
            color = TextSecondary,
            textAlign = TextAlign.Center,
        )

        Spacer(Modifier.height(28.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .height(58.dp)
                    .width(72.dp)
                    .background(SurfaceWhite, RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Text("+91", style = MaterialTheme.typography.titleMedium)
            }
            Spacer(Modifier.width(10.dp))
            OutlinedTextField(
                value = state.phone,
                onValueChange = viewModel::onPhoneChange,
                modifier = Modifier
                    .weight(1f)
                    .height(58.dp),
                placeholder = { Text("Mobile number") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = SurfaceWhite,
                    unfocusedContainerColor = SurfaceWhite,
                    focusedBorderColor = Navy,
                    unfocusedBorderColor = Divider,
                ),
            )
        }

        if (state.error != null) {
            Spacer(Modifier.height(10.dp))
            Text(
                text = state.error,
                color = RedGive,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.fillMaxWidth(),
            )
        }

        Spacer(Modifier.height(24.dp))
        Button(
            onClick = viewModel::sendOtp,
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

        Spacer(Modifier.weight(1f))
        Text(
            text = "By continuing you agree to the Terms of Service and Privacy Policy",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 16.dp),
        )
    }
}
