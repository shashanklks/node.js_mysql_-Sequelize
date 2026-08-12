@file:OptIn(ExperimentalMaterial3Api::class)

package com.khatabook.clone.ui.common

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.khatabook.clone.ui.theme.AvatarBg
import com.khatabook.clone.ui.theme.GreenGet
import com.khatabook.clone.ui.theme.Navy
import com.khatabook.clone.ui.theme.RedGive
import com.khatabook.clone.ui.theme.TextOnNavy
import com.khatabook.clone.ui.theme.TextSecondary

/** The navy bar used on every inner screen. */
@Composable
fun KhataTopBar(
    title: String,
    subtitle: String? = null,
    onBack: (() -> Unit)? = null,
    actions: @Composable () -> Unit = {},
) {
    TopAppBar(
        title = {
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    color = TextOnNavy,
                    maxLines = 1,
                )
                if (!subtitle.isNullOrBlank()) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextOnNavy.copy(alpha = 0.8f),
                        maxLines = 1,
                    )
                }
            }
        },
        navigationIcon = {
            if (onBack != null) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextOnNavy)
                }
            }
        },
        actions = { actions() },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Navy,
            titleContentColor = TextOnNavy,
            navigationIconContentColor = TextOnNavy,
            actionIconContentColor = TextOnNavy,
        ),
    )
}

/** Round monogram used in every party row. */
@Composable
fun Avatar(name: String, size: Int = 44, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(size.dp)
            .clip(CircleShape)
            .background(AvatarBg),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = initials(name),
            color = Navy,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

/**
 * Money, coloured by direction. A positive balance is money you will get.
 */
@Composable
fun AmountText(
    amount: Double,
    modifier: Modifier = Modifier,
    positiveIsGet: Boolean = true,
    style: androidx.compose.ui.text.TextStyle = MaterialTheme.typography.titleMedium,
) {
    val isGet = if (positiveIsGet) amount >= 0 else amount < 0
    Text(
        text = rupees(amount),
        color = if (amount == 0.0) TextSecondary else if (isGet) GreenGet else RedGive,
        style = style,
        fontWeight = FontWeight.SemiBold,
        modifier = modifier,
    )
}

@Composable
fun LoadingBox(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = Navy)
    }
}

@Composable
fun EmptyState(
    title: String,
    message: String,
    modifier: Modifier = Modifier,
    action: @Composable () -> Unit = {},
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = TextSecondary,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(16.dp))
        action()
    }
}

/** Small rounded chip used for "GAVE"/"GOT" and the customer/supplier badge. */
@Composable
fun Pill(text: String, background: Color, contentColor: Color, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(background)
            .padding(horizontal = 8.dp, vertical = 3.dp),
    ) {
        Text(
            text = text,
            color = contentColor,
            style = MaterialTheme.typography.labelSmall,
        )
    }
}

@Composable
fun DirectionLabel(isGet: Boolean, modifier: Modifier = Modifier) {
    Text(
        text = if (isGet) "You will get" else "You will give",
        color = if (isGet) GreenGet else RedGive,
        style = MaterialTheme.typography.labelSmall,
        modifier = modifier,
    )
}

@Composable
fun RowDivider(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(com.khatabook.clone.ui.theme.Divider),
    )
}

@Composable
fun ScreenMessage(text: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.Center,
    ) {
        Text(text = text, color = RedGive, style = MaterialTheme.typography.bodyLarge)
    }
}
