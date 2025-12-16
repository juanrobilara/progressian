package com.jurobil.progressian.data.repository

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

val Context.dataStore by preferencesDataStore(name = "settings")

@Singleton
class SettingsRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val IS_FIRST_TIME_KEY = booleanPreferencesKey("is_first_time")

    val isFirstTime: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[IS_FIRST_TIME_KEY] ?: true
    }

    suspend fun completeOnboarding() {
        context.dataStore.edit { settings ->
            settings[IS_FIRST_TIME_KEY] = false
        }
    }
}