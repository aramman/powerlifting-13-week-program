package com.aram.benchpress13week.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.aram.benchpress13week.data.GeneratedWorkoutSet
import com.aram.benchpress13week.viewmodel.BenchUiState
import java.time.format.DateTimeFormatter

@Composable
fun HomeScreen(state: BenchUiState) {
    val workout = state.todayWorkout
    if (workout == null) {
        Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
            Text("No workout generated yet")
        }
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Today", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Text("Week ${workout.week} • ${workout.label}")
                    workout.date?.let {
                        Text(it.format(DateTimeFormatter.ISO_DATE))
                    }
                    Text(
                        text = "1RM ${state.profile.oneRepMaxKg} kg • TM ${state.profile.trainingMaxPercent}% • Round ${state.profile.roundingStepKg} kg",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
        items(workout.sets) { set ->
            SetCard(set)
        }
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Suggestion", fontWeight = FontWeight.Bold)
                    Text("Use training max at 95% instead of raw 1RM when fatigue is high. It usually makes the cycle more repeatable and keeps bar speed cleaner.")
                }
            }
        }
    }
}

@Composable
private fun SetCard(set: GeneratedWorkoutSet) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("${set.weightKg} kg", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text("${(set.percent * 100)}% of training max")
            }
            Text("${set.sets} × ${set.reps}", style = MaterialTheme.typography.titleMedium)
        }
    }
}
