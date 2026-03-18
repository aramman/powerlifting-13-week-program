package com.aram.benchpress13week.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
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
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(state.workouts) { workout ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Week ${workout.week} • ${workout.label}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    workout.date?.let { Text(it.format(DateTimeFormatter.ISO_DATE)) }
                    workout.sets.forEachIndexed { index, set ->
                        Text("${index + 1}. ${set.weightKg} kg • ${set.sets} × ${set.reps} • ${(set.percent * 100)}%")
                    }
                    HorizontalDivider()
                    Text("Tip: after week 4 or 8, you can re-enter a new 1RM or lower training max if bench stalls.")
                }
            }
        }
    }
}
