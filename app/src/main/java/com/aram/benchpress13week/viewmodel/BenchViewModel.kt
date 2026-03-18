package com.aram.benchpress13week.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.aram.benchpress13week.data.BenchProgramGenerator
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

    val uiState: StateFlow<BenchUiState> = combine(repo.profileFlow, savedMessage) { profile, message ->
        val workouts = BenchProgramGenerator.generate(profile)
        BenchUiState(
            profile = profile,
            workouts = workouts,
            todayWorkout = BenchProgramGenerator.currentWorkout(profile),
            message = message,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), BenchUiState())

    fun saveProfile(oneRm: Double, tmPercent: Int, rounding: Double, startDate: LocalDate) {
        viewModelScope.launch {
            repo.saveProfile(
                UserProfile(
                    oneRepMaxKg = oneRm,
                    trainingMaxPercent = tmPercent,
                    roundingStepKg = rounding,
                    startDate = startDate,
                )
            )
            savedMessage.value = "Plan updated"
        }
    }

    fun clearMessage() {
        savedMessage.value = ""
    }
}

data class BenchUiState(
    val profile: UserProfile = UserProfile(),
    val workouts: List<GeneratedWorkoutDay> = emptyList(),
    val todayWorkout: GeneratedWorkoutDay? = null,
    val message: String = "",
)
