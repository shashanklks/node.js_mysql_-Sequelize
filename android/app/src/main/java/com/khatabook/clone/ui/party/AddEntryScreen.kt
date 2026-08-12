@file:OptIn(ExperimentalMaterial3Api::class)

package com.khatabook.clone.ui.party

import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.khatabook.clone.data.remote.EntryType
import com.khatabook.clone.ui.common.KhataTopBar
import com.khatabook.clone.ui.common.dateToIso
import com.khatabook.clone.ui.common.isoToDate
import com.khatabook.clone.ui.common.longDate
import com.khatabook.clone.ui.theme.Divider
import com.khatabook.clone.ui.theme.GreenGet
import com.khatabook.clone.ui.theme.Navy
import com.khatabook.clone.ui.theme.RedGive
import com.khatabook.clone.ui.theme.ScreenBg
import com.khatabook.clone.ui.theme.SurfaceWhite
import com.khatabook.clone.ui.theme.TextSecondary
import java.util.Calendar

@Composable
fun AddEntryScreen(
    partyId: Int,
    type: String,
    onBack: () -> Unit,
    onSaved: () -> Unit,
) {
    val viewModel: AddEntryViewModel = viewModel()
    val state = viewModel.state
    val context = LocalContext.current
    val isGave = type == EntryType.GAVE
    val accent = if (isGave) RedGive else GreenGet

    LaunchedEffect(partyId) { viewModel.loadParty(partyId) }
    LaunchedEffect(state.saved) {
        if (state.saved) onSaved()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ScreenBg),
    ) {
        KhataTopBar(
            title = if (isGave) "You gave ₹" else "You got ₹",
            subtitle = state.partyName.takeIf { it.isNotBlank() },
            onBack = onBack,
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(16.dp),
        ) {
            Surface(
                color = SurfaceWhite,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Amount",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary,
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "₹",
                            fontSize = 30.sp,
                            fontWeight = FontWeight.Bold,
                            color = accent,
                        )
                        Spacer(Modifier.width(8.dp))
                        OutlinedTextField(
                            value = state.amount,
                            onValueChange = viewModel::onAmountChange,
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("0", fontSize = 28.sp, color = TextSecondary) },
                            singleLine = true,
                            textStyle = MaterialTheme.typography.headlineSmall.copy(
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold,
                                color = accent,
                            ),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = SurfaceWhite,
                                unfocusedContainerColor = SurfaceWhite,
                                focusedBorderColor = SurfaceWhite,
                                unfocusedBorderColor = SurfaceWhite,
                                cursorColor = accent,
                            ),
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))
            Surface(
                color = SurfaceWhite,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                OutlinedTextField(
                    value = state.note,
                    onValueChange = viewModel::onNoteChange,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text("Enter details (items, bill no., quantity)", color = TextSecondary)
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = SurfaceWhite,
                        unfocusedContainerColor = SurfaceWhite,
                        focusedBorderColor = SurfaceWhite,
                        unfocusedBorderColor = SurfaceWhite,
                        cursorColor = Navy,
                    ),
                )
            }

            Spacer(Modifier.height(12.dp))
            Surface(
                color = SurfaceWhite,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            val calendar = Calendar.getInstance()
                            isoToDate(state.date)?.let { calendar.time = it }
                            DatePickerDialog(
                                context,
                                { _, year, month, day ->
                                    val picked = Calendar.getInstance().apply {
                                        set(year, month, day, 0, 0, 0)
                                    }
                                    viewModel.onDateChange(dateToIso(picked.time))
                                },
                                calendar.get(Calendar.YEAR),
                                calendar.get(Calendar.MONTH),
                                calendar.get(Calendar.DAY_OF_MONTH),
                            ).apply {
                                // A ledger cannot be written into the future.
                                datePicker.maxDate = System.currentTimeMillis()
                            }.show()
                        }
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.CalendarMonth,
                            contentDescription = null,
                            tint = Navy,
                            modifier = Modifier.size(20.dp),
                        )
                        Spacer(Modifier.width(10.dp))
                        Text(longDate(state.date), style = MaterialTheme.typography.bodyLarge)
                    }
                    Text("Change", color = Navy, style = MaterialTheme.typography.labelLarge)
                }
            }

            if (state.error != null) {
                Spacer(Modifier.height(12.dp))
                Text(text = state.error, color = RedGive, style = MaterialTheme.typography.bodyMedium)
            }
        }

        Surface(color = SurfaceWhite, shadowElevation = 8.dp) {
            Button(
                onClick = { viewModel.save(partyId, type) },
                enabled = state.canSubmit,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
                    .height(52.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = accent,
                    disabledContainerColor = Divider,
                ),
            ) {
                if (state.loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        color = SurfaceWhite,
                        strokeWidth = 2.dp,
                    )
                } else {
                    Text("SAVE", style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }
}
