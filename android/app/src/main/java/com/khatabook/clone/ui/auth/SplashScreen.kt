package com.khatabook.clone.ui.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.khatabook.clone.R
import com.khatabook.clone.ServiceLocator
import com.khatabook.clone.ui.theme.Navy
import com.khatabook.clone.ui.theme.TextOnNavy
import com.khatabook.clone.ui.theme.TextOnNavyMuted
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first

@Composable
fun SplashScreen(onLoggedIn: () -> Unit, onLoggedOut: (languageChosen: Boolean) -> Unit) {
    LaunchedEffect(Unit) {
        delay(900)
        val token = ServiceLocator.awaitToken()
        if (token.isNullOrBlank()) {
            onLoggedOut(ServiceLocator.session.language.first() != null)
        } else {
            onLoggedIn()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Navy),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Image(
                painter = painterResource(R.drawable.ic_launcher_foreground),
                contentDescription = null,
                modifier = Modifier.size(120.dp),
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Khatabook",
                color = TextOnNavy,
                fontWeight = FontWeight.Bold,
                fontSize = 30.sp,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = "Digital ledger for your business",
                color = TextOnNavyMuted,
                style = MaterialTheme.typography.bodyLarge,
            )
        }

        Text(
            text = "Made in India",
            color = TextOnNavyMuted,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 36.dp),
        )
    }
}
