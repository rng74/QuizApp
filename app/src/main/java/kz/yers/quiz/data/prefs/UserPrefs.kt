package kz.yers.quiz.data.prefs

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.userPrefsDataStore: DataStore<Preferences> by preferencesDataStore(name = "user_prefs")

class UserPrefs(private val context: Context) {
    private object Keys {
        val USER_NAME = stringPreferencesKey("userName")
        val CURRENT_STREAK_DAYS = intPreferencesKey("currentStreakDays")
        val LAST_PLAYED_EPOCH_DAY = longPreferencesKey("lastPlayedEpochDay")
        val TUTORIAL_COMPLETED = booleanPreferencesKey("tutorialCompleted")
        val POSTER_ENABLED = booleanPreferencesKey("posterEnabled")
        val SOUND_ENABLED = booleanPreferencesKey("soundEnabled")
        val A11Y_REDUCE_MOTION = booleanPreferencesKey("a11yReduceMotion")
    }

    val userName: Flow<String> = context.userPrefsDataStore.data.map { it[Keys.USER_NAME] ?: "Игрок" }

    val currentStreakDays: Flow<Int> = context.userPrefsDataStore.data.map { it[Keys.CURRENT_STREAK_DAYS] ?: 0 }

    val lastPlayedEpochDay: Flow<Long> = context.userPrefsDataStore.data.map { it[Keys.LAST_PLAYED_EPOCH_DAY] ?: 0L }

    val tutorialCompleted: Flow<Boolean> = context.userPrefsDataStore.data.map { it[Keys.TUTORIAL_COMPLETED] ?: false }

    val posterEnabled: Flow<Boolean> = context.userPrefsDataStore.data.map { it[Keys.POSTER_ENABLED] ?: true }

    val soundEnabled: Flow<Boolean> = context.userPrefsDataStore.data.map { it[Keys.SOUND_ENABLED] ?: true }

    val reduceMotion: Flow<Boolean> = context.userPrefsDataStore.data.map { it[Keys.A11Y_REDUCE_MOTION] ?: false }

    suspend fun setUserName(value: String) {
        context.userPrefsDataStore.edit { it[Keys.USER_NAME] = value }
    }

    suspend fun setStreak(
        currentStreakDays: Int,
        lastPlayedEpochDay: Long,
    ) {
        context.userPrefsDataStore.edit {
            it[Keys.CURRENT_STREAK_DAYS] = currentStreakDays
            it[Keys.LAST_PLAYED_EPOCH_DAY] = lastPlayedEpochDay
        }
    }

    suspend fun setTutorialCompleted(value: Boolean) {
        context.userPrefsDataStore.edit { it[Keys.TUTORIAL_COMPLETED] = value }
    }

    suspend fun setPosterEnabled(value: Boolean) {
        context.userPrefsDataStore.edit { it[Keys.POSTER_ENABLED] = value }
    }

    suspend fun setSoundEnabled(value: Boolean) {
        context.userPrefsDataStore.edit { it[Keys.SOUND_ENABLED] = value }
    }

    suspend fun setReduceMotion(value: Boolean) {
        context.userPrefsDataStore.edit { it[Keys.A11Y_REDUCE_MOTION] = value }
    }
}
