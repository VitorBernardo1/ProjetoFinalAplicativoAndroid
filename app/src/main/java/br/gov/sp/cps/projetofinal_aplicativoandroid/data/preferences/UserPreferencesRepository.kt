package br.gov.sp.cps.projetofinal_aplicativoandroid.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

data class UserPreferences(
    val isDarkMode: Boolean,
    val dailyCardGoal: Int,
    val studiedTodayCount: Int,
    val lastStudyDate: String,
    val streakCount: Int,
    val isReminderEnabled: Boolean,
    val reminderHour: Int,
    val reminderMinute: Int
)

class UserPreferencesRepository(private val context: Context) {

    private object PreferencesKeys {
        val IS_DARK_MODE = booleanPreferencesKey("is_dark_mode")
        val DAILY_CARD_GOAL = intPreferencesKey("daily_card_goal")
        val STUDIED_TODAY_COUNT = intPreferencesKey("studied_today_count")
        val LAST_STUDY_DATE = stringPreferencesKey("last_study_date")
        val STREAK_COUNT = intPreferencesKey("streak_count")
        val IS_REMINDER_ENABLED = booleanPreferencesKey("is_reminder_enabled")
        val REMINDER_HOUR = intPreferencesKey("reminder_hour")
        val REMINDER_MINUTE = intPreferencesKey("reminder_minute")
    }

    val userPreferencesFlow: Flow<UserPreferences> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            val todayDate = getTodayDateString()
            val lastDate = preferences[PreferencesKeys.LAST_STUDY_DATE] ?: ""
            val rawStudiedToday = preferences[PreferencesKeys.STUDIED_TODAY_COUNT] ?: 0

            // Reset studiedToday count if date has changed
            val studiedToday = if (lastDate == todayDate) rawStudiedToday else 0

            UserPreferences(
                isDarkMode = preferences[PreferencesKeys.IS_DARK_MODE] ?: false,
                dailyCardGoal = preferences[PreferencesKeys.DAILY_CARD_GOAL] ?: 10,
                studiedTodayCount = studiedToday,
                lastStudyDate = lastDate,
                streakCount = preferences[PreferencesKeys.STREAK_COUNT] ?: 1,
                isReminderEnabled = preferences[PreferencesKeys.IS_REMINDER_ENABLED] ?: true,
                reminderHour = preferences[PreferencesKeys.REMINDER_HOUR] ?: 20,
                reminderMinute = preferences[PreferencesKeys.REMINDER_MINUTE] ?: 0
            )
        }

    suspend fun setDarkMode(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.IS_DARK_MODE] = enabled
        }
    }

    suspend fun setDailyGoal(goal: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.DAILY_CARD_GOAL] = goal
        }
    }

    suspend fun incrementStudiedToday(count: Int = 1) {
        val todayDate = getTodayDateString()
        context.dataStore.edit { preferences ->
            val lastDate = preferences[PreferencesKeys.LAST_STUDY_DATE] ?: ""
            val currentStudied = if (lastDate == todayDate) {
                preferences[PreferencesKeys.STUDIED_TODAY_COUNT] ?: 0
            } else {
                0
            }
            val newStudied = currentStudied + count
            preferences[PreferencesKeys.STUDIED_TODAY_COUNT] = newStudied

            // Update streak
            val currentStreak = preferences[PreferencesKeys.STREAK_COUNT] ?: 1
            if (lastDate != todayDate) {
                preferences[PreferencesKeys.LAST_STUDY_DATE] = todayDate
                if (isYesterday(lastDate)) {
                    preferences[PreferencesKeys.STREAK_COUNT] = currentStreak + 1
                } else if (lastDate.isEmpty()) {
                    preferences[PreferencesKeys.STREAK_COUNT] = 1
                } else if (lastDate != todayDate) {
                    preferences[PreferencesKeys.STREAK_COUNT] = 1
                }
            }
        }
    }

    suspend fun setReminderSettings(enabled: Boolean, hour: Int, minute: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.IS_REMINDER_ENABLED] = enabled
            preferences[PreferencesKeys.REMINDER_HOUR] = hour
            preferences[PreferencesKeys.REMINDER_MINUTE] = minute
        }
    }

    companion object {
        fun getTodayDateString(): String {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            return sdf.format(Date())
        }

        private fun isYesterday(dateStr: String): Boolean {
            if (dateStr.isEmpty()) return false
            return try {
                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val date = sdf.parse(dateStr) ?: return false
                val yesterday = Date(System.currentTimeMillis() - 24 * 60 * 60 * 1000)
                val yesterdayStr = sdf.format(yesterday)
                dateStr == yesterdayStr
            } catch (e: Exception) {
                false
            }
        }
    }
}
