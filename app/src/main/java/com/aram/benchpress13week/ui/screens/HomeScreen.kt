package com.aram.benchpress13week.ui.screens

import androidx.compose.foundation.clickable
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.aram.benchpress13week.data.GeneratedExercise
import com.aram.benchpress13week.data.GeneratedSet
import com.aram.benchpress13week.viewmodel.BenchUiState
import java.time.format.DateTimeFormatter

@Composable
fun HomeScreen(
    state: BenchUiState,
    onTogglePaused: () -> Unit,
    onCompleteWorkout: () -> Unit,
    onPreviousWorkout: () -> Unit,
    onToggleSetCompleted: (String) -> Unit,
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
                    workout?.let { Text("Sets ${it.completedSets} / ${it.totalSets}") }
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
                            enabled = !state.isPaused && state.currentWorkoutAllSetsCompleted,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Next Workout")
                        }
                        if (!state.currentWorkoutAllSetsCompleted) {
                            Text("Complete every expanded set first to unlock the next workout.", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }

            items(workout.exercises) { exercise ->
                ExerciseCard(
                    exercise = exercise,
                    onToggleSetCompleted = onToggleSetCompleted,
                )
            }
        }
    }
}

@Composable
private fun ExerciseCard(
    exercise: GeneratedExercise,
    onToggleSetCompleted: (String) -> Unit,
) {
    var expanded by rememberSaveable(exercise.name) { mutableStateOf(false) }
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded },
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(exercise.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("${exercise.completedSets} / ${exercise.totalSets} sets done", style = MaterialTheme.typography.bodySmall)
                }
                Text(if (expanded) "Hide" else "Expand", style = MaterialTheme.typography.bodyMedium)
            }
            if (expanded) {
                exercise.prescriptions.forEach { prescription ->
                    Text(prescription.summary)
                    prescription.sets.forEach { set ->
                        SetRow(set = set, onToggleSetCompleted = onToggleSetCompleted)
                    }
                }
                exercise.notes.forEach { note ->
                    Text(note, style = MaterialTheme.typography.bodySmall)
                }
                exercise.alternative?.let { alternative ->
                    Text("Alternative Option", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                    Text(alternative.name, fontWeight = FontWeight.Bold)
                    alternative.prescriptions.forEach { prescription ->
                        Text(prescription.summary)
                        prescription.sets.forEach { set ->
                            SetRow(set = set, onToggleSetCompleted = onToggleSetCompleted)
                        }
                    }
                    alternative.notes.forEach { note ->
                        Text(note, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}

@Composable
private fun SetRow(
    set: GeneratedSet,
    onToggleSetCompleted: (String) -> Unit,
) {
    Surface(
        tonalElevation = if (set.isCompleted) 4.dp else 0.dp,
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggleSetCompleted(set.id) }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(set.label)
            Text(if (set.isCompleted) "Done" else "Tap to mark")
        }
    }
}
