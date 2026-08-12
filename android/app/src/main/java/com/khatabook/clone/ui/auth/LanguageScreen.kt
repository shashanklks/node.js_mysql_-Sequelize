@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.khatabook.clone.ui.auth

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.khatabook.clone.ui.theme.Navy
import com.khatabook.clone.ui.theme.ScreenBg
import com.khatabook.clone.ui.theme.SurfaceWhite
import com.khatabook.clone.ui.theme.TextSecondary

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
    var selected by remember { mutableStateOf("en") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ScreenBg)
            .padding(20.dp),
    ) {
        Spacer(Modifier.height(24.dp))
        Text(
            text = "Choose your language",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = "अपनी भाषा चुनें",
            style = MaterialTheme.typography.bodyLarge,
            color = TextSecondary,
        )
        Spacer(Modifier.height(20.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(languages) { language ->
                val isSelected = language.code == selected
                Card(
                    onClick = { selected = language.code },
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                    border = BorderStroke(
                        width = if (isSelected) 2.dp else 1.dp,
                        color = if (isSelected) Navy else com.khatabook.clone.ui.theme.Divider,
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 18.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            text = language.native,
                            style = MaterialTheme.typography.titleMedium,
                            color = if (isSelected) Navy else MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center,
                        )
                        Text(
                            text = language.english,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary,
                        )
                    }
                }
            }
        }

        Button(
            onClick = { viewModel.choose(selected, onContinue) },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Navy),
        ) {
            Text("Continue", style = MaterialTheme.typography.labelLarge)
        }
        Spacer(Modifier.height(12.dp))
    }
}
