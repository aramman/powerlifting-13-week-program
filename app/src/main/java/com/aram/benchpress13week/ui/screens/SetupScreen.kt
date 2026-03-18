package com.aram.benchpress13week.ui.screens

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
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
    val accessoryGroups = remember(state.accessoryExerciseNames) {
        state.accessoryExerciseNames.groupBy(::accessoryGroupFor)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Setup Your Training Block", style = MaterialTheme.typography.headlineSmall)
                Text("Use true maxes for the main lifts, then set exact accessory kg where needed.", style = MaterialTheme.typography.bodyLarge)
            }
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Primary Lifts", style = MaterialTheme.typography.titleLarge)
                MetricField("Bench max kg", benchMax) { benchMax = it }
                MetricField("Squat max kg", squatMax) { squatMax = it }
                MetricField("Deadlift max kg", deadliftMax) { deadliftMax = it }
                MetricField("Overhead press max kg", pressMax) { pressMax = it }
                MetricField("Rounding step kg", rounding) { rounding = it }
                OutlinedTextField(
                    value = startDate,
                    onValueChange = { startDate = it },
                    label = { Text("Start date YYYY-MM-DD") },
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Accessory Exact KG", style = MaterialTheme.typography.titleLarge)
                accessoryGroups.forEach { (groupName, exercises) ->
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(groupName, fontWeight = FontWeight.Bold)
                            exercises.forEach { name ->
                                OutlinedTextField(
                                    value = accessoryValues[name].orEmpty(),
                                    onValueChange = { value ->
                                        accessoryValues = accessoryValues.toMutableMap().also { it[name] = value }
                                    },
                                    label = { Text(name) },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
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
                OutlinedActionButton(
                    onClick = onResetProgress,
                    label = "Restart Program From Week 1"
                )
            }
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Training Logic", fontWeight = FontWeight.Bold)
                HelpChip("Main lifts use exact kg from your maxes")
                HelpChip("Accessory exact kg can be set here")
                HelpChip("Progress only moves when you finish the workout")
                HelpChip("Pause protects your current place in the cycle")
            }
        }
    }
}

@Composable
private fun MetricField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun OutlinedActionButton(
    onClick: () -> Unit,
    label: String,
) {
    androidx.compose.material3.OutlinedButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(label)
    }
}

@Composable
private fun HelpChip(text: String) {
    Surface(
        color = MaterialTheme.colorScheme.secondaryContainer,
        shape = MaterialTheme.shapes.small
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            style = MaterialTheme.typography.bodySmall
        )
    }
}

private fun accessoryGroupFor(name: String): String {
    return when {
        name.contains("Curl", ignoreCase = true) || name.contains("Triceps", ignoreCase = true) || name.contains("French Press", ignoreCase = true) -> "Arms"
        name.contains("Row", ignoreCase = true) || name.contains("Pulldown", ignoreCase = true) || name.contains("Pull-Ups", ignoreCase = true) -> "Back"
        name.contains("Fly", ignoreCase = true) || name.contains("Press", ignoreCase = true) || name.contains("Lateral Raise", ignoreCase = true) -> "Chest & Shoulders"
        name.contains("Leg Extension", ignoreCase = true) || name.contains("Hyperextensions", ignoreCase = true) -> "Lower Body"
        name.contains("Abs", ignoreCase = true) || name.contains("Wrist", ignoreCase = true) -> "Support Work"
        else -> "Other"
    }
}
