package com.aram.benchpress13week.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.aram.benchpress13week.data.GeneratedExercise
import com.aram.benchpress13week.viewmodel.BenchUiState
import java.time.format.DateTimeFormatter

@Composable
fun HomeScreen(
    state: BenchUiState,
    onTogglePaused: () -> Unit,
    onCompleteWorkout: () -> Unit,
    onPreviousWorkout: () -> Unit,
) {
    val workout = state.currentWorkout
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text("Current Workout", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Text("Progress ${state.currentWorkoutIndex + 1} / ${state.workouts.size}")
                    Text(if (state.isPaused) "Status: paused" else "Status: active")
                    Text("Bench ${state.profile.benchMaxKg} kg • Squat ${state.profile.squatMaxKg} kg")
                    Text("Deadlift ${state.profile.deadliftMaxKg} kg • Press ${state.profile.pressMaxKg} kg")
                    Text("Rounding ${state.profile.roundingStepKg} kg")
                    Text("Workouts do not auto-skip. You stay on the current session until you mark it done.")
                }
            }
        }

        if (workout == null) {
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("Program complete", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text("You reached the end of the 13-week plan.")
                    }
                }
            }
        } else {
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("Week ${workout.week} • ${workout.dayLabel}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text(workout.date.format(DateTimeFormatter.ISO_DATE))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = onTogglePaused,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(if (state.isPaused) "Resume" else "Pause")
                            }
                            OutlinedButton(
                                onClick = onPreviousWorkout,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Previous")
                            }
                        }
                        Button(
                            onClick = onCompleteWorkout,
                            enabled = !state.isPaused,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Mark Workout Done")
                        }
                    }
                }
            }

            items(workout.exercises) { exercise ->
                ExerciseCard(exercise = exercise)
            }
        }
    }
}

@Composable
private fun ExerciseCard(exercise: GeneratedExercise) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(exercise.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            exercise.prescriptions.forEach { line ->
                Text(line)
            }
            exercise.notes.forEach { note ->
                Text(note, style = MaterialTheme.typography.bodySmall)
            }
            exercise.alternative?.let { alternative ->
                Text("Alternative", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                Text(alternative.name)
                alternative.prescriptions.forEach { line ->
                    Text(line)
                }
                alternative.notes.forEach { note ->
                    Text(note, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}
