package com.aram.benchpress13week.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.aram.benchpress13week.data.ActiveWorkoutSession
import com.aram.benchpress13week.data.BenchProgramGenerator
import com.aram.benchpress13week.data.BenchProgramParser
import com.aram.benchpress13week.data.GeneratedExercise
import com.aram.benchpress13week.data.GeneratedSet
import com.aram.benchpress13week.data.GeneratedWorkoutDay
import com.aram.benchpress13week.data.ProfileRepository
import com.aram.benchpress13week.data.UserProfile
import com.aram.benchpress13week.data.WorkoutSummary
import kotlinx.coroutines.delay
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
    private val currentTimeMillis = MutableStateFlow(System.currentTimeMillis())
    private val program = application.assets.open("bench_program_full_13weeks_ru.txt")
        .bufferedReader()
        .use { reader -> BenchProgramParser.parse(reader.readText()) }
    private val accessoryExerciseNames = BenchProgramGenerator.accessoryExerciseNames(program)

    init {
        viewModelScope.launch {
            while (true) {
                currentTimeMillis.value = System.currentTimeMillis()
                delay(1_000)
            }
        }
    }

    val uiState: StateFlow<BenchUiState> = combine(
        repo.profileFlow,
        repo.progressFlow,
        savedMessage,
        currentTimeMillis,
    ) { profile, progress, message, nowMillis ->
        val workouts = BenchProgramGenerator.generate(program, profile, progress.completedSetIds)
        val currentIndex = progress.currentWorkoutIndex.coerceIn(0, workouts.size)
        val currentWorkout = workouts.getOrNull(currentIndex)
        val activeSession = progress.activeSession
        val activeWorkout = activeSession?.let { workouts.getOrNull(it.workoutIndex) }
        val activeWorkoutElapsedMillis = if (activeSession != null) {
            (nowMillis - activeSession.startedAtMillis).coerceAtLeast(0L)
        } else {
            0L
        }
        val canSkipWeek = currentWorkout != null && workouts.any { it.index > currentWorkout.index && it.week > currentWorkout.week }

        BenchUiState(
            profile = profile,
            workouts = workouts,
            currentWorkout = currentWorkout,
            currentWorkoutIndex = currentIndex,
            isPaused = progress.isPaused,
            accessoryExerciseNames = accessoryExerciseNames,
            currentWorkoutAllSetsCompleted = currentWorkout?.let { it.totalSets > 0 && it.completedSets == it.totalSets } ?: false,
            completedSetIds = progress.completedSetIds,
            activeWorkoutSession = activeSession,
            activeWorkout = activeWorkout,
            activeWorkoutElapsedMillis = activeWorkoutElapsedMillis,
            lastWorkoutSummary = progress.lastWorkoutSummary,
            canSkipWeek = canSkipWeek,
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

    fun skipWeek() {
        val state = uiState.value
        val currentWorkout = state.currentWorkout ?: return
        val nextWeekWorkout = state.workouts.firstOrNull { it.index > currentWorkout.index && it.week > currentWorkout.week }
            ?: return

        viewModelScope.launch {
            repo.setCurrentWorkoutIndex(nextWeekWorkout.index)
            savedMessage.value = "Skipped to week ${nextWeekWorkout.week}"
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

    fun toggleSetCompleted(setId: String) {
        viewModelScope.launch {
            repo.toggleSetCompleted(setId)
        }
    }

    fun startWorkout() {
        val state = uiState.value
        val workout = state.currentWorkout ?: return

        viewModelScope.launch {
            repo.startWorkoutSession(
                workoutIndex = workout.index,
                completedSetIds = state.completedSetIds,
                startedAtMillis = System.currentTimeMillis(),
            )
            savedMessage.value = "Workout started"
        }
    }

    fun endWorkout() {
        val state = uiState.value
        val session = state.activeWorkoutSession ?: return
        val workout = state.activeWorkout ?: return
        val finishedAtMillis = System.currentTimeMillis()
        val summary = buildWorkoutSummary(
            workout = workout,
            session = session,
            finishedAtMillis = finishedAtMillis,
        )

        viewModelScope.launch {
            repo.endWorkoutSession(summary)
            savedMessage.value = "Workout ended"
        }
    }

    fun dismissWorkoutSummary() {
        viewModelScope.launch {
            repo.clearLastWorkoutSummary()
        }
    }

    private fun buildWorkoutSummary(
        workout: GeneratedWorkoutDay,
        session: ActiveWorkoutSession,
        finishedAtMillis: Long,
    ): WorkoutSummary {
        val newlyCompletedSets = workout.exercises
            .flatMap { it.allSets() }
            .filter { set -> set.isCompleted && set.id !in session.completedSetIdsAtStart }
        val totalVolumeKg = newlyCompletedSets.sumOf { set ->
            (set.reps ?: 0) * (set.weightKg ?: 0.0)
        }

        return WorkoutSummary(
            workoutIndex = workout.index,
            finishedAtMillis = finishedAtMillis,
            durationMillis = (finishedAtMillis - session.startedAtMillis).coerceAtLeast(0L),
            completedSets = newlyCompletedSets.size,
            totalVolumeKg = totalVolumeKg,
        )
    }
}

data class BenchUiState(
    val profile: UserProfile = UserProfile(),
    val workouts: List<GeneratedWorkoutDay> = emptyList(),
    val currentWorkout: GeneratedWorkoutDay? = null,
    val currentWorkoutIndex: Int = 0,
    val isPaused: Boolean = false,
    val accessoryExerciseNames: List<String> = emptyList(),
    val currentWorkoutAllSetsCompleted: Boolean = false,
    val completedSetIds: Set<String> = emptySet(),
    val activeWorkoutSession: ActiveWorkoutSession? = null,
    val activeWorkout: GeneratedWorkoutDay? = null,
    val activeWorkoutElapsedMillis: Long = 0L,
    val lastWorkoutSummary: WorkoutSummary? = null,
    val canSkipWeek: Boolean = false,
    val message: String = "",
)

private fun GeneratedExercise.allSets(): List<GeneratedSet> {
    val primarySets = prescriptions.flatMap { it.sets }
    val alternativeSets = alternative?.prescriptions?.flatMap { it.sets }.orEmpty()
    return primarySets + alternativeSets
}
