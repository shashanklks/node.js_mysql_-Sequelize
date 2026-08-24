package com.khatabook.clone.ui.auth

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.khatabook.clone.R
import com.khatabook.clone.ServiceLocator
import com.khatabook.clone.ui.theme.KhataTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first

@Composable
fun SplashScreen(onLoggedIn: () -> Unit, onLoggedOut: (languageChosen: Boolean) -> Unit) {
    val palette = KhataTheme.colors
    var settled by remember { mutableStateOf(false) }

    // The mark springs in while the stored session is read, so the wait is
    // spent on something rather than on a held frame.
    val scale by animateFloatAsState(
        targetValue = if (settled) 1f else 0.82f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "logo",
    )
    val fade by animateFloatAsState(
        targetValue = if (settled) 1f else 0f,
        label = "fade",
    )

    LaunchedEffect(Unit) {
        settled = true
        delay(750)
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
            .background(Brush.verticalGradient(listOf(palette.headerTop, palette.headerBottom))),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.scale(scale),
        ) {
            Image(
                painter = painterResource(R.drawable.ic_launcher_foreground),
                contentDescription = null,
                modifier = Modifier.size(116.dp),
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = "Khatabook",
                color = palette.onHeader,
                fontWeight = FontWeight.Bold,
                fontSize = 30.sp,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = "Your udhaar, in order",
                color = palette.onHeaderMuted,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.alpha(fade),
            )
        }

        Text(
            text = "Made in India",
            color = palette.onHeaderMuted,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 36.dp)
                .alpha(fade),
        )
    }
}
