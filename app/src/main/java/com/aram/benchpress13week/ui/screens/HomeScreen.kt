package com.aram.benchpress13week.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.aram.benchpress13week.data.GeneratedExercise
import com.aram.benchpress13week.data.GeneratedSet
import com.aram.benchpress13week.viewmodel.BenchUiState
import java.time.Duration
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

@Composable
fun HomeScreen(
    state: BenchUiState,
    onTogglePaused: () -> Unit,
    onStartWorkout: () -> Unit,
    onEndWorkout: () -> Unit,
    onDismissWorkoutSummary: () -> Unit,
    onCompleteWorkout: () -> Unit,
    onPreviousWorkout: () -> Unit,
    onSkipWeek: () -> Unit,
    onToggleSetCompleted: (String) -> Unit,
) {
    val workout = state.currentWorkout
    val activeSession = state.activeWorkoutSession
    val isCurrentWorkoutActive = activeSession?.workoutIndex == workout?.index
    val progressFraction = if (state.workouts.isEmpty()) 0f else state.currentWorkoutIndex.toFloat() / state.workouts.size.toFloat()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("Current Block", style = MaterialTheme.typography.headlineSmall)
                    Text(
                        text = when {
                            isCurrentWorkoutActive -> "Workout in progress. Timer is running."
                            state.isPaused -> "Paused. Your place is saved."
                            else -> "Live cycle. Complete every set to advance."
                        },
                        style = MaterialTheme.typography.bodyLarge
                    )
                    LinearProgressIndicator(
                        progress = { progressFraction.coerceIn(0f, 1f) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        MetricPill("Workout", "${state.currentWorkoutIndex + 1}/${state.workouts.size}")
                        workout?.let { MetricPill("Sets", "${it.completedSets}/${it.totalSets}") }
                        MetricPill(
                            "Status",
                            when {
                                isCurrentWorkoutActive -> "Training"
                                state.isPaused -> "Paused"
                                else -> "Ready"
                            }
                        )
                    }
                }
            }
        }

        state.lastWorkoutSummary?.let { summary ->
            val summaryWorkout = state.workouts.getOrNull(summary.workoutIndex)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text("Last Workout Summary", style = MaterialTheme.typography.titleLarge)
                        Text(
                            summaryWorkout?.let { "Week ${it.week} • ${it.dayLabel}" } ?: "Completed workout",
                            fontWeight = FontWeight.Bold
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            MetricPill("Time", formatDuration(summary.durationMillis))
                            MetricPill("Sets", summary.completedSets.toString())
                            MetricPill("Volume", formatKg(summary.totalVolumeKg))
                        }
                        OutlinedButton(
                            onClick = onDismissWorkoutSummary,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Dismiss Summary")
                        }
                    }
                }
            }
        }

        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Maxes", style = MaterialTheme.typography.titleLarge)
                    Text("Bench ${state.profile.benchMaxKg} kg • Squat ${state.profile.squatMaxKg} kg")
                    Text("Deadlift ${state.profile.deadliftMaxKg} kg • Press ${state.profile.pressMaxKg} kg")
                    Text("Rounding ${state.profile.roundingStepKg} kg", style = MaterialTheme.typography.bodySmall)
                }
            }
        }

        if (workout == null) {
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text("Program complete", style = MaterialTheme.typography.titleLarge)
                        Text("You reached the end of the 13-week cycle. Reset the program or update your maxes to run the next block.")
                    }
                }
            }
        } else {
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("Today", style = MaterialTheme.typography.titleLarge)
                        Text("Week ${workout.week} • ${workout.dayLabel}", fontWeight = FontWeight.Bold)
                        Text(workout.date.format(DateTimeFormatter.ISO_DATE))
                        if (isCurrentWorkoutActive) {
                            Text(
                                "Workout timer: ${formatDuration(state.activeWorkoutElapsedMillis)}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        LinearProgressIndicator(
                            progress = {
                                if (workout.totalSets == 0) 0f else workout.completedSets.toFloat() / workout.totalSets.toFloat()
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
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
                                enabled = activeSession == null,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Previous")
                            }
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = if (isCurrentWorkoutActive) onEndWorkout else onStartWorkout,
                                enabled = !state.isPaused && (activeSession == null || isCurrentWorkoutActive),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(if (isCurrentWorkoutActive) "End Workout" else "Start Workout")
                            }
                            OutlinedButton(
                                onClick = onSkipWeek,
                                enabled = !state.isPaused && activeSession == null && state.canSkipWeek,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Skip Week")
                            }
                        }
                        Button(
                            onClick = onCompleteWorkout,
                            enabled = !state.isPaused && activeSession == null && state.currentWorkoutAllSetsCompleted,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Next Workout")
                        }
                        when {
                            isCurrentWorkoutActive -> {
                                Text(
                                    "Timer is running. End the workout to save time and volume stats.",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }

                            !state.currentWorkoutAllSetsCompleted -> {
                                Text(
                                    "Finish every set below to unlock the next workout, or skip the week if you want to move ahead.",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
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
private fun MetricPill(label: String, value: String) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = MaterialTheme.shapes.medium
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(label, style = MaterialTheme.typography.bodySmall)
            Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun ExerciseCard(
    exercise: GeneratedExercise,
    onToggleSetCompleted: (String) -> Unit,
) {
    var expanded by rememberSaveable(exercise.name) { mutableStateOf(false) }
    val progress = if (exercise.totalSets == 0) 0f else exercise.completedSets.toFloat() / exercise.totalSets.toFloat()

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(exercise.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("${exercise.completedSets} / ${exercise.totalSets} sets complete", style = MaterialTheme.typography.bodySmall)
                }
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = if (expanded) "Hide" else "Expand",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            LinearProgressIndicator(
                progress = { progress.coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth()
            )

            if (expanded) {
                exercise.prescriptions.forEach { prescription ->
                    Text(prescription.summary, fontWeight = FontWeight.SemiBold)
                    prescription.sets.forEach { set ->
                        SetRow(set = set, onToggleSetCompleted = onToggleSetCompleted)
                    }
                }

                exercise.notes.forEach { note ->
                    Text(note, style = MaterialTheme.typography.bodySmall)
                }

                exercise.alternative?.let { alternative ->
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("Alternative Option", fontWeight = FontWeight.Bold)
                            Text(alternative.name, fontWeight = FontWeight.SemiBold)
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
    }
}

@Composable
private fun SetRow(
    set: GeneratedSet,
    onToggleSetCompleted: (String) -> Unit,
) {
    Surface(
        color = if (set.isCompleted) {
            MaterialTheme.colorScheme.secondaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceVariant
        },
        tonalElevation = if (set.isCompleted) 3.dp else 0.dp,
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggleSetCompleted(set.id) }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 11.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(set.label, fontWeight = FontWeight.SemiBold)
                Text(
                    if (set.isCompleted) "Logged" else "Tap to mark complete",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Box(
                modifier = Modifier
                    .height(32.dp)
                    .padding(start = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(if (set.isCompleted) "Done" else "Open")
            }
        }
    }
}

private fun formatDuration(durationMillis: Long): String {
    val duration = Duration.ofMillis(durationMillis)
    val hours = duration.toHours()
    val minutes = duration.toMinutesPart()
    val seconds = duration.toSecondsPart()
    return if (hours > 0) {
        "%d:%02d:%02d".format(hours, minutes, seconds)
    } else {
        "%02d:%02d".format(duration.toMinutes(), seconds)
    }
}

private fun formatKg(value: Double): String {
    val rounded = (value * 10).roundToInt() / 10.0
    return if (rounded % 1.0 == 0.0) "${rounded.toInt()} kg" else "$rounded kg"
}
