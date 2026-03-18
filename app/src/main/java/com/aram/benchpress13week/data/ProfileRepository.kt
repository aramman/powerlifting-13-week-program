package com.aram.benchpress13week.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate

private val Context.dataStore by preferencesDataStore(name = "bench_profile")

class ProfileRepository(private val context: Context) {
    private object Keys {
        val BENCH_MAX = doublePreferencesKey("bench_max")
        val SQUAT_MAX = doublePreferencesKey("squat_max")
        val DEADLIFT_MAX = doublePreferencesKey("deadlift_max")
        val PRESS_MAX = doublePreferencesKey("press_max")
        val ROUNDING = doublePreferencesKey("rounding")
        val START_EPOCH = intPreferencesKey("start_epoch")
        val ACCESSORY_WEIGHTS = stringPreferencesKey("accessory_weights")
        val CURRENT_INDEX = intPreferencesKey("current_index")
        val PAUSED = booleanPreferencesKey("paused")
    }

    val profileFlow: Flow<UserProfile> = context.dataStore.data.map { prefs ->
        UserProfile(
            benchMaxKg = prefs[Keys.BENCH_MAX] ?: 100.0,
            squatMaxKg = prefs[Keys.SQUAT_MAX] ?: 140.0,
            deadliftMaxKg = prefs[Keys.DEADLIFT_MAX] ?: 170.0,
            pressMaxKg = prefs[Keys.PRESS_MAX] ?: 60.0,
            accessoryWeightsKg = decodeAccessoryWeights(prefs[Keys.ACCESSORY_WEIGHTS].orEmpty()),
            roundingStepKg = prefs[Keys.ROUNDING] ?: 2.5,
            startDate = LocalDate.ofEpochDay((prefs[Keys.START_EPOCH] ?: LocalDate.now().toEpochDay().toInt()).toLong()),
        )
    }

    val progressFlow: Flow<WorkoutProgress> = context.dataStore.data.map { prefs ->
        WorkoutProgress(
            currentWorkoutIndex = prefs[Keys.CURRENT_INDEX] ?: 0,
            isPaused = prefs[Keys.PAUSED] ?: false,
        )
    }

    suspend fun saveProfile(profile: UserProfile) {
        context.dataStore.edit { prefs ->
            prefs[Keys.BENCH_MAX] = profile.benchMaxKg
            prefs[Keys.SQUAT_MAX] = profile.squatMaxKg
            prefs[Keys.DEADLIFT_MAX] = profile.deadliftMaxKg
            prefs[Keys.PRESS_MAX] = profile.pressMaxKg
            prefs[Keys.ACCESSORY_WEIGHTS] = encodeAccessoryWeights(profile.accessoryWeightsKg)
            prefs[Keys.ROUNDING] = profile.roundingStepKg
            prefs[Keys.START_EPOCH] = profile.startDate.toEpochDay().toInt()
        }
    }

    suspend fun completeWorkout(totalWorkouts: Int) {
        context.dataStore.edit { prefs ->
            val current = prefs[Keys.CURRENT_INDEX] ?: 0
            prefs[Keys.CURRENT_INDEX] = (current + 1).coerceAtMost(totalWorkouts)
        }
    }

    suspend fun previousWorkout() {
        context.dataStore.edit { prefs ->
            val current = prefs[Keys.CURRENT_INDEX] ?: 0
            prefs[Keys.CURRENT_INDEX] = (current - 1).coerceAtLeast(0)
        }
    }

    suspend fun setPaused(paused: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[Keys.PAUSED] = paused
        }
    }

    suspend fun resetProgress() {
        context.dataStore.edit { prefs ->
            prefs[Keys.CURRENT_INDEX] = 0
            prefs[Keys.PAUSED] = false
        }
    }

    private fun encodeAccessoryWeights(weights: Map<String, Double>): String {
        return weights.entries
            .sortedBy { it.key }
            .joinToString("\n") { "${it.key}=${it.value}" }
    }

    private fun decodeAccessoryWeights(raw: String): Map<String, Double> {
        if (raw.isBlank()) return emptyMap()
        return raw.lines()
            .mapNotNull { line ->
                val index = line.lastIndexOf('=')
                if (index <= 0) return@mapNotNull null
                val name = line.substring(0, index)
                val weight = line.substring(index + 1).toDoubleOrNull() ?: return@mapNotNull null
                name to weight
            }
            .toMap()
    }
}
