package com.aram.benchpress13week.data

import android.content.Context
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate

private val Context.dataStore by preferencesDataStore(name = "bench_profile")

class ProfileRepository(private val context: Context) {
    private object Keys {
        val ONE_RM = doublePreferencesKey("one_rm")
        val TM_PERCENT = intPreferencesKey("tm_percent")
        val ROUNDING = doublePreferencesKey("rounding")
        val START_EPOCH = intPreferencesKey("start_epoch")
    }

    val profileFlow: Flow<UserProfile> = context.dataStore.data.map { prefs ->
        UserProfile(
            oneRepMaxKg = prefs[Keys.ONE_RM] ?: 100.0,
            trainingMaxPercent = prefs[Keys.TM_PERCENT] ?: 100,
            roundingStepKg = prefs[Keys.ROUNDING] ?: 2.5,
            startDate = LocalDate.ofEpochDay((prefs[Keys.START_EPOCH] ?: LocalDate.now().toEpochDay().toInt()).toLong())
        )
    }

    suspend fun saveProfile(profile: UserProfile) {
        context.dataStore.edit { prefs ->
            prefs[Keys.ONE_RM] = profile.oneRepMaxKg
            prefs[Keys.TM_PERCENT] = profile.trainingMaxPercent
            prefs[Keys.ROUNDING] = profile.roundingStepKg
            prefs[Keys.START_EPOCH] = profile.startDate.toEpochDay().toInt()
        }
    }
}
