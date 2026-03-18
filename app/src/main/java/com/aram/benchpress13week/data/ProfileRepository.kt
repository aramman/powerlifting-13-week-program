package com.aram.benchpress13week.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
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
        val COMPLETED_SET_IDS = stringSetPreferencesKey("completed_set_ids")
        val ACTIVE_SESSION_WORKOUT_INDEX = intPreferencesKey("active_session_workout_index")
        val ACTIVE_SESSION_STARTED_AT = longPreferencesKey("active_session_started_at")
        val ACTIVE_SESSION_COMPLETED_SET_IDS = stringSetPreferencesKey("active_session_completed_set_ids")
        val LAST_SUMMARY_WORKOUT_INDEX = intPreferencesKey("last_summary_workout_index")
        val LAST_SUMMARY_FINISHED_AT = longPreferencesKey("last_summary_finished_at")
        val LAST_SUMMARY_DURATION = longPreferencesKey("last_summary_duration")
        val LAST_SUMMARY_COMPLETED_SETS = intPreferencesKey("last_summary_completed_sets")
        val LAST_SUMMARY_TOTAL_VOLUME = doublePreferencesKey("last_summary_total_volume")
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
        val activeSessionStartedAt = prefs[Keys.ACTIVE_SESSION_STARTED_AT]
        val activeSessionWorkoutIndex = prefs[Keys.ACTIVE_SESSION_WORKOUT_INDEX]
        val lastSummaryFinishedAt = prefs[Keys.LAST_SUMMARY_FINISHED_AT]
        WorkoutProgress(
            currentWorkoutIndex = prefs[Keys.CURRENT_INDEX] ?: 0,
            isPaused = prefs[Keys.PAUSED] ?: false,
            completedSetIds = prefs[Keys.COMPLETED_SET_IDS] ?: emptySet(),
            activeSession = if (activeSessionStartedAt != null && activeSessionWorkoutIndex != null) {
                ActiveWorkoutSession(
                    workoutIndex = activeSessionWorkoutIndex,
                    startedAtMillis = activeSessionStartedAt,
                    completedSetIdsAtStart = prefs[Keys.ACTIVE_SESSION_COMPLETED_SET_IDS] ?: emptySet(),
                )
            } else {
                null
            },
            lastWorkoutSummary = if (lastSummaryFinishedAt != null) {
                WorkoutSummary(
                    workoutIndex = prefs[Keys.LAST_SUMMARY_WORKOUT_INDEX] ?: 0,
                    finishedAtMillis = lastSummaryFinishedAt,
                    durationMillis = prefs[Keys.LAST_SUMMARY_DURATION] ?: 0L,
                    completedSets = prefs[Keys.LAST_SUMMARY_COMPLETED_SETS] ?: 0,
                    totalVolumeKg = prefs[Keys.LAST_SUMMARY_TOTAL_VOLUME] ?: 0.0,
                )
            } else {
                null
            },
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

    suspend fun setCurrentWorkoutIndex(index: Int) {
        context.dataStore.edit { prefs ->
            prefs[Keys.CURRENT_INDEX] = index.coerceAtLeast(0)
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
            prefs[Keys.COMPLETED_SET_IDS] = emptySet()
            prefs.remove(Keys.ACTIVE_SESSION_WORKOUT_INDEX)
            prefs.remove(Keys.ACTIVE_SESSION_STARTED_AT)
            prefs.remove(Keys.ACTIVE_SESSION_COMPLETED_SET_IDS)
            prefs.remove(Keys.LAST_SUMMARY_WORKOUT_INDEX)
            prefs.remove(Keys.LAST_SUMMARY_FINISHED_AT)
            prefs.remove(Keys.LAST_SUMMARY_DURATION)
            prefs.remove(Keys.LAST_SUMMARY_COMPLETED_SETS)
            prefs.remove(Keys.LAST_SUMMARY_TOTAL_VOLUME)
        }
    }

    suspend fun toggleSetCompleted(setId: String) {
        context.dataStore.edit { prefs ->
            val current = (prefs[Keys.COMPLETED_SET_IDS] ?: emptySet()).toMutableSet()
            if (!current.add(setId)) current.remove(setId)
            prefs[Keys.COMPLETED_SET_IDS] = current
        }
    }

    suspend fun startWorkoutSession(workoutIndex: Int, completedSetIds: Set<String>, startedAtMillis: Long) {
        context.dataStore.edit { prefs ->
            prefs[Keys.ACTIVE_SESSION_WORKOUT_INDEX] = workoutIndex
            prefs[Keys.ACTIVE_SESSION_STARTED_AT] = startedAtMillis
            prefs[Keys.ACTIVE_SESSION_COMPLETED_SET_IDS] = completedSetIds
            prefs.remove(Keys.LAST_SUMMARY_WORKOUT_INDEX)
            prefs.remove(Keys.LAST_SUMMARY_FINISHED_AT)
            prefs.remove(Keys.LAST_SUMMARY_DURATION)
            prefs.remove(Keys.LAST_SUMMARY_COMPLETED_SETS)
            prefs.remove(Keys.LAST_SUMMARY_TOTAL_VOLUME)
        }
    }

    suspend fun endWorkoutSession(summary: WorkoutSummary) {
        context.dataStore.edit { prefs ->
            prefs.remove(Keys.ACTIVE_SESSION_WORKOUT_INDEX)
            prefs.remove(Keys.ACTIVE_SESSION_STARTED_AT)
            prefs.remove(Keys.ACTIVE_SESSION_COMPLETED_SET_IDS)
            prefs[Keys.LAST_SUMMARY_WORKOUT_INDEX] = summary.workoutIndex
            prefs[Keys.LAST_SUMMARY_FINISHED_AT] = summary.finishedAtMillis
            prefs[Keys.LAST_SUMMARY_DURATION] = summary.durationMillis
            prefs[Keys.LAST_SUMMARY_COMPLETED_SETS] = summary.completedSets
            prefs[Keys.LAST_SUMMARY_TOTAL_VOLUME] = summary.totalVolumeKg
        }
    }

    suspend fun clearLastWorkoutSummary() {
        context.dataStore.edit { prefs ->
            prefs.remove(Keys.LAST_SUMMARY_WORKOUT_INDEX)
            prefs.remove(Keys.LAST_SUMMARY_FINISHED_AT)
            prefs.remove(Keys.LAST_SUMMARY_DURATION)
            prefs.remove(Keys.LAST_SUMMARY_COMPLETED_SETS)
            prefs.remove(Keys.LAST_SUMMARY_TOTAL_VOLUME)
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
