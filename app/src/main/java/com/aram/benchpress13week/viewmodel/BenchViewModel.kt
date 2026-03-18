package com.aram.benchpress13week.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.aram.benchpress13week.data.BenchProgramGenerator
import com.aram.benchpress13week.data.BenchProgramParser
import com.aram.benchpress13week.data.GeneratedWorkoutDay
import com.aram.benchpress13week.data.ProfileRepository
import com.aram.benchpress13week.data.UserProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

class BenchViewModel(application: Application) : AndroidViewModel(application) {
    private val repo = ProfileRepository(application)
    private val savedMessage = MutableStateFlow("")
    private val program = application.assets.open("bench_program_full_13weeks_ru.txt")
        .bufferedReader()
        .use { reader -> BenchProgramParser.parse(reader.readText()) }
    private val accessoryExerciseNames = BenchProgramGenerator.accessoryExerciseNames(program)

    val uiState: StateFlow<BenchUiState> = combine(
        repo.profileFlow,
        repo.progressFlow,
        savedMessage,
    ) { profile, progress, message ->
        val workouts = BenchProgramGenerator.generate(program, profile)
        val currentIndex = progress.currentWorkoutIndex.coerceIn(0, workouts.size)
        BenchUiState(
            profile = profile,
            workouts = workouts,
            currentWorkout = workouts.getOrNull(currentIndex),
            currentWorkoutIndex = currentIndex,
            isPaused = progress.isPaused,
            accessoryExerciseNames = accessoryExerciseNames,
            message = message,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), BenchUiState())

    fun saveProfile(
        benchMaxKg: Double,
        squatMaxKg: Double,
        deadliftMaxKg: Double,
        pressMaxKg: Double,
        accessoryWeightsKg: Map<String, Double>,
        roundingStepKg: Double,
        startDate: LocalDate,
    ) {
        viewModelScope.launch {
            repo.saveProfile(
                UserProfile(
                    benchMaxKg = benchMaxKg,
                    squatMaxKg = squatMaxKg,
                    deadliftMaxKg = deadliftMaxKg,
                    pressMaxKg = pressMaxKg,
                    accessoryWeightsKg = accessoryWeightsKg,
                    roundingStepKg = roundingStepKg,
                    startDate = startDate,
                )
            )
            savedMessage.value = "Profile saved"
        }
    }

    fun completeWorkout() {
        viewModelScope.launch {
            repo.completeWorkout(program.size)
            savedMessage.value = "Moved to next workout"
        }
    }

    fun previousWorkout() {
        viewModelScope.launch {
            repo.previousWorkout()
            savedMessage.value = "Moved back one workout"
        }
    }

    fun togglePaused() {
        viewModelScope.launch {
            val paused = !uiState.value.isPaused
            repo.setPaused(paused)
            savedMessage.value = if (paused) "Program paused" else "Program resumed"
        }
    }

    fun resetProgress() {
        viewModelScope.launch {
            repo.resetProgress()
            savedMessage.value = "Program reset to week 1"
        }
    }

    fun clearMessage() {
        savedMessage.value = ""
    }
}

data class BenchUiState(
    val profile: UserProfile = UserProfile(),
    val workouts: List<GeneratedWorkoutDay> = emptyList(),
    val currentWorkout: GeneratedWorkoutDay? = null,
    val currentWorkoutIndex: Int = 0,
    val isPaused: Boolean = false,
    val accessoryExerciseNames: List<String> = emptyList(),
    val message: String = "",
)
