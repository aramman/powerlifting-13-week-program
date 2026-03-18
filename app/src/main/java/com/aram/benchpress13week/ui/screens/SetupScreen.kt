package com.aram.benchpress13week.ui.screens

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
    onSave: (Double, Double, Double, Double, Map<String, Double>, Double, LocalDate) -> Unit,
    onResetProgress: () -> Unit,
) {
    var benchMax by remember(state.profile.benchMaxKg) { mutableStateOf(state.profile.benchMaxKg.toString()) }
    var squatMax by remember(state.profile.squatMaxKg) { mutableStateOf(state.profile.squatMaxKg.toString()) }
    var deadliftMax by remember(state.profile.deadliftMaxKg) { mutableStateOf(state.profile.deadliftMaxKg.toString()) }
    var pressMax by remember(state.profile.pressMaxKg) { mutableStateOf(state.profile.pressMaxKg.toString()) }
    var rounding by remember(state.profile.roundingStepKg) { mutableStateOf(state.profile.roundingStepKg.toString()) }
    var startDate by remember(state.profile.startDate) { mutableStateOf(state.profile.startDate.toString()) }
    var accessoryValues by remember(state.profile.accessoryWeightsKg, state.accessoryExerciseNames) {
        mutableStateOf(
            state.accessoryExerciseNames.associateWith { name ->
                state.profile.accessoryWeightsKg[name]?.toString().orEmpty()
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Training Setup", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                OutlinedTextField(value = benchMax, onValueChange = { benchMax = it }, label = { Text("Bench max kg") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = squatMax, onValueChange = { squatMax = it }, label = { Text("Squat max kg") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = deadliftMax, onValueChange = { deadliftMax = it }, label = { Text("Deadlift max kg") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = pressMax, onValueChange = { pressMax = it }, label = { Text("Overhead press max kg") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = rounding, onValueChange = { rounding = it }, label = { Text("Rounding step kg") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = startDate, onValueChange = { startDate = it }, label = { Text("Start date YYYY-MM-DD") }, modifier = Modifier.fillMaxWidth())
                Text("Accessory Exact KG", fontWeight = FontWeight.Bold)
                state.accessoryExerciseNames.forEach { name ->
                    OutlinedTextField(
                        value = accessoryValues[name].orEmpty(),
                        onValueChange = { value -> accessoryValues = accessoryValues.toMutableMap().also { it[name] = value } },
                        label = { Text(name) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                Button(
                    onClick = {
                        onSave(
                            benchMax.toDoubleOrNull() ?: state.profile.benchMaxKg,
                            squatMax.toDoubleOrNull() ?: state.profile.squatMaxKg,
                            deadliftMax.toDoubleOrNull() ?: state.profile.deadliftMaxKg,
                            pressMax.toDoubleOrNull() ?: state.profile.pressMaxKg,
                            accessoryValues.mapNotNull { (name, value) ->
                                value.toDoubleOrNull()?.let { name to it }
                            }.toMap(),
                            rounding.toDoubleOrNull() ?: state.profile.roundingStepKg,
                            runCatching { LocalDate.parse(startDate) }.getOrDefault(state.profile.startDate),
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Save Maxes")
                }
                Button(
                    onClick = onResetProgress,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Restart Program From Week 1")
                }
            }
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("How progression works", fontWeight = FontWeight.Bold)
                Text("The app stays on the current workout until you mark it done.")
                Text("Pause stops accidental progress changes, but your place in the plan is always preserved.")
                Text("Any exercise marked as self-selected in the source program can be given an exact kg here.")
            }
        }
    }
}
