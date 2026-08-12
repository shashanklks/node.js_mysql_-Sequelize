@file:OptIn(ExperimentalMaterial3Api::class)

package com.khatabook.clone.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.khatabook.clone.ui.auth.LabeledField
import com.khatabook.clone.ui.common.Avatar
import com.khatabook.clone.ui.common.KhataTopBar
import com.khatabook.clone.ui.home.HomeBottomBar
import com.khatabook.clone.ui.theme.Divider
import com.khatabook.clone.ui.theme.GreenGet
import com.khatabook.clone.ui.theme.Navy
import com.khatabook.clone.ui.theme.RedGive
import com.khatabook.clone.ui.theme.ScreenBg
import com.khatabook.clone.ui.theme.SurfaceWhite
import com.khatabook.clone.ui.theme.TextSecondary

@Composable
fun ProfileScreen(
    onBack: () -> Unit,
    onHome: () -> Unit,
    onReports: () -> Unit,
    onLoggedOut: () -> Unit,
) {
    val viewModel: ProfileViewModel = viewModel()
    val state = viewModel.state
    var confirmLogout by remember { mutableStateOf(false) }

    if (state.loggedOut) {
        androidx.compose.runtime.LaunchedEffect(Unit) { onLoggedOut() }
    }

    Scaffold(
        containerColor = ScreenBg,
        topBar = { KhataTopBar(title = "Profile & settings", onBack = onBack) },
        bottomBar = {
            HomeBottomBar(selected = 2, onHome = onHome, onReports = onReports, onProfile = {})
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState()),
        ) {
            Surface(color = Navy, modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Avatar(name = state.name.ifBlank { "?" }, size = 56)
                    Spacer(Modifier.width(14.dp))
                    Column {
                        Text(
                            text = state.name.ifBlank { "Your name" },
                            color = SurfaceWhite,
                            style = MaterialTheme.typography.titleLarge,
                        )
                        Text(
                            text = state.phone?.let { "+91 $it" } ?: "",
                            color = SurfaceWhite.copy(alpha = 0.8f),
                            style = MaterialTheme.typography.bodyLarge,
                        )
                    }
                }
            }

            SectionCard(title = "Your details") {
                LabeledField(
                    label = "Name",
                    value = state.name,
                    placeholder = "Your name",
                    onValueChange = viewModel::onNameChange,
                )
                Spacer(Modifier.height(12.dp))
                LabeledField(
                    label = "Business name",
                    value = state.businessName,
                    placeholder = "Your shop or firm",
                    onValueChange = viewModel::onBusinessChange,
                )
                Spacer(Modifier.height(14.dp))
                Button(
                    onClick = viewModel::saveProfile,
                    enabled = !state.saving,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Navy),
                ) {
                    Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Save changes", style = MaterialTheme.typography.labelLarge)
                }
            }

            SectionCard(title = "App") {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Language,
                        contentDescription = null,
                        tint = Navy,
                        modifier = Modifier.size(20.dp),
                    )
                    Spacer(Modifier.width(10.dp))
                    Text("Language", modifier = Modifier.weight(1f))
                    Text(
                        text = state.language.uppercase(),
                        color = TextSecondary,
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
                Spacer(Modifier.height(16.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Dns,
                        contentDescription = null,
                        tint = Navy,
                        modifier = Modifier.size(20.dp),
                    )
                    Spacer(Modifier.width(10.dp))
                    Text("API server", style = MaterialTheme.typography.titleMedium)
                }
                Spacer(Modifier.height(8.dp))
                LabeledField(
                    label = "Base URL",
                    value = state.baseUrl,
                    placeholder = "http://10.0.2.2:3002/",
                    onValueChange = viewModel::onBaseUrlChange,
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "Use 10.0.2.2 for the emulator, or your machine's LAN address on a real phone.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                )
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = viewModel::saveBaseUrl,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Navy),
                ) {
                    Text("Save server", style = MaterialTheme.typography.labelLarge)
                }
                if (state.message != null) {
                    Spacer(Modifier.height(10.dp))
                    Text(
                        text = state.message,
                        color = if (state.messageIsError) RedGive else GreenGet,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }

            Box(modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)) {
                Surface(
                    color = SurfaceWhite,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { confirmLogout = true }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.Logout,
                            contentDescription = null,
                            tint = RedGive,
                            modifier = Modifier.size(20.dp),
                        )
                        Spacer(Modifier.width(10.dp))
                        Text("Log out", color = RedGive, style = MaterialTheme.typography.titleMedium)
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
            Text(
                text = "Khatabook clone · v1.0",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            )
        }
    }

    if (confirmLogout) {
        AlertDialog(
            onDismissRequest = { confirmLogout = false },
            title = { Text("Log out?") },
            text = { Text("You will need to verify your mobile number again.") },
            confirmButton = {
                TextButton(onClick = {
                    confirmLogout = false
                    viewModel.logout()
                }) { Text("Log out", color = RedGive) }
            },
            dismissButton = {
                TextButton(onClick = { confirmLogout = false }) { Text("Cancel") }
            },
        )
    }
}

@Composable
private fun SectionCard(title: String, content: @Composable () -> Unit) {
    Column(modifier = Modifier.padding(12.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall,
            color = TextSecondary,
            modifier = Modifier.padding(start = 4.dp, bottom = 6.dp),
        )
        Surface(
            color = SurfaceWhite,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(modifier = Modifier.padding(16.dp)) { content() }
        }
    }
}
