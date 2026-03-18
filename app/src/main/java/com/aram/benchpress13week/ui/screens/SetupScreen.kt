package com.aram.benchpress13week.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.aram.benchpress13week.viewmodel.BenchUiState
import java.time.LocalDate

@Composable
fun SetupScreen(
    state: BenchUiState,
    onSave: (Double, Int, Double, LocalDate) -> Unit,
) {
    var oneRm by remember(state.profile.oneRepMaxKg) { mutableStateOf(state.profile.oneRepMaxKg.toString()) }
    var tmPercent by remember(state.profile.trainingMaxPercent) { mutableStateOf(state.profile.trainingMaxPercent.toString()) }
    var rounding by remember(state.profile.roundingStepKg) { mutableStateOf(state.profile.roundingStepKg.toString()) }
    var startDate by remember(state.profile.startDate) { mutableStateOf(state.profile.startDate.toString()) }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Setup", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                OutlinedTextField(value = oneRm, onValueChange = { oneRm = it }, label = { Text("Bench 1RM kg") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = tmPercent, onValueChange = { tmPercent = it }, label = { Text("Training max %") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = rounding, onValueChange = { rounding = it }, label = { Text("Rounding step kg") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = startDate, onValueChange = { startDate = it }, label = { Text("Start date YYYY-MM-DD") }, modifier = Modifier.fillMaxWidth())
                Button(
                    onClick = {
                        onSave(
                            oneRm.toDoubleOrNull() ?: state.profile.oneRepMaxKg,
                            tmPercent.toIntOrNull() ?: state.profile.trainingMaxPercent,
                            rounding.toDoubleOrNull() ?: state.profile.roundingStepKg,
                            runCatching { LocalDate.parse(startDate) }.getOrDefault(state.profile.startDate)
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Save and rebuild")
                }
            }
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("My recommendation", fontWeight = FontWeight.Bold)
                Text("Default training max should be 95%, not 100%. Add optional notes/history later so you can track whether the cycle actually worked and not only what was planned.")
            }
        }
    }
}
