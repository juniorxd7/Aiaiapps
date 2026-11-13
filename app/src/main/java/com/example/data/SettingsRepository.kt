package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.sorteiaDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "sorteia_preferences"
)

class SettingsRepository(private val context: Context) {

    private object Keys {
        val AVOID_REPEATS = booleanPreferencesKey("avoid_repeats")
    }

    val avoidRepeats: Flow<Boolean> = context.sorteiaDataStore.data.map { prefs ->
        prefs[Keys.AVOID_REPEATS] ?: false
    }

    suspend fun setAvoidRepeats(enabled: Boolean) {
        context.sorteiaDataStore.edit { prefs ->
            prefs[Keys.AVOID_REPEATS] = enabled
        }
    }
}
