package com.aram.benchpress13week.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.aram.benchpress13week.viewmodel.BenchUiState
import java.time.format.DateTimeFormatter

@Composable
fun ProgramScreen(state: BenchUiState) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(state.workouts) { workout ->
            val status = when {
                workout.index < state.currentWorkoutIndex -> "Completed"
                workout.index == state.currentWorkoutIndex -> if (state.isPaused) "Current • Paused" else "Current"
                else -> "Upcoming"
            }
            val progress = if (workout.totalSets == 0) 0f else workout.completedSets.toFloat() / workout.totalSets.toFloat()
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = when {
                        workout.index == state.currentWorkoutIndex -> MaterialTheme.colorScheme.primaryContainer
                        workout.index < state.currentWorkoutIndex -> MaterialTheme.colorScheme.secondaryContainer
                        else -> MaterialTheme.colorScheme.surface
                    }
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        color = MaterialTheme.colorScheme.surface,
                        shape = MaterialTheme.shapes.small
                    ) {
                        Text(
                            text = status,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text("Week ${workout.week} • ${workout.dayLabel}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text(workout.date.format(DateTimeFormatter.ISO_DATE))
                    LinearProgressIndicator(
                        progress = { progress.coerceIn(0f, 1f) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text("${workout.completedSets}/${workout.totalSets} sets complete", style = MaterialTheme.typography.bodySmall)
                    workout.exercises.forEachIndexed { index, exercise ->
                        Text("${index + 1}. ${exercise.name}", fontWeight = FontWeight.Bold)
                        exercise.prescriptions.forEach { line -> Text(line.summary) }
                        exercise.notes.forEach { note -> Text(note, style = MaterialTheme.typography.bodySmall) }
                        exercise.alternative?.let { alternative ->
                            Text("Alternative: ${alternative.name}", style = MaterialTheme.typography.bodyMedium)
                            alternative.prescriptions.forEach { line -> Text(line.summary) }
                            alternative.notes.forEach { note -> Text(note, style = MaterialTheme.typography.bodySmall) }
                        }
                    }
                }
            }
        }
    }
}
