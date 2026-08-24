@file:OptIn(ExperimentalMaterial3Api::class)

package com.khatabook.clone.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.khatabook.clone.ui.theme.KhataTheme

private data class Language(val code: String, val native: String, val english: String)

private val languages = listOf(
    Language("en", "English", "English"),
    Language("hi", "हिन्दी", "Hindi"),
    Language("mr", "मराठी", "Marathi"),
    Language("gu", "ગુજરાતી", "Gujarati"),
    Language("bn", "বাংলা", "Bengali"),
    Language("ta", "தமிழ்", "Tamil"),
    Language("te", "తెలుగు", "Telugu"),
    Language("kn", "ಕನ್ನಡ", "Kannada"),
    Language("ml", "മലയാളം", "Malayalam"),
    Language("pa", "ਪੰਜਾਬੀ", "Punjabi"),
)

@Composable
fun LanguageScreen(onContinue: () -> Unit) {
    val viewModel: LanguageViewModel = viewModel()
    val palette = KhataTheme.colors
    var selected by remember { mutableStateOf("en") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(palette.screen)
            .padding(20.dp),
    ) {
        Spacer(Modifier.height(28.dp))
        Text(
            text = "Choose your language",
            style = MaterialTheme.typography.headlineSmall,
            color = palette.textPrimary,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = "अपनी भाषा चुनें",
            style = MaterialTheme.typography.bodyLarge,
            color = palette.textSecondary,
        )
        Spacer(Modifier.height(22.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(languages) { language ->
                val isSelected = language.code == selected
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(palette.surface)
                        .border(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) palette.brand else palette.line,
                            shape = RoundedCornerShape(12.dp),
                        )
                        .clickable { selected = language.code }
                        .padding(vertical = 18.dp),
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            text = language.native,
                            style = MaterialTheme.typography.titleLarge,
                            color = if (isSelected) palette.brand else palette.textPrimary,
                            textAlign = TextAlign.Center,
                        )
                        Text(
                            text = language.english,
                            style = MaterialTheme.typography.bodyMedium,
                            color = palette.textFaint,
                        )
                    }
                    if (isSelected) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(8.dp)
                                .size(20.dp)
                                .clip(CircleShape)
                                .background(palette.brand),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = null,
                                tint = palette.onHeader,
                                modifier = Modifier.size(13.dp),
                            )
                        }
                    }
                }
            }
        }

        Button(
            onClick = { viewModel.choose(selected, onContinue) },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = palette.brand),
        ) {
            Text("Continue", style = MaterialTheme.typography.labelLarge)
        }
        Spacer(Modifier.height(12.dp))
    }
}
