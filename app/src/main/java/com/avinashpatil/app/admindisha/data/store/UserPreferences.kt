package com.avinashpatil.app.admindisha.data.store

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import javax.inject.Inject
import javax.inject.Singleton

private val Context.userPreferencesDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "user_preferences"
)

@Singleton
class UserPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val dataStore = context.userPreferencesDataStore

    companion object {
        private val USER_ID = stringPreferencesKey("user_id")
        private val DISHA_ID = stringPreferencesKey("disha_id")
        private val EMAIL = stringPreferencesKey("email")
        private val FULL_NAME = stringPreferencesKey("full_name")
        private val SESSION_TOKEN = stringPreferencesKey("session_token")
        private val IS_USER_LOCKED = booleanPreferencesKey("is_user_locked")
    }

    suspend fun saveUserData(
        userId: String,
        dishaId: String,
        email: String,
        fullName: String,
        sessionToken: String
    ) {
        android.util.Log.d("UserPreferences", "Saving user data - UserId: $userId, DishaId: $dishaId, Email: $email, FullName: $fullName, SessionToken: $sessionToken")
        dataStore.edit { preferences ->
            preferences[USER_ID] = userId
            preferences[DISHA_ID] = dishaId
            preferences[EMAIL] = email
            preferences[FULL_NAME] = fullName
            preferences[SESSION_TOKEN] = sessionToken
        }
        android.util.Log.d("UserPreferences", "User data saved successfully")
    }

    suspend fun saveUserLockStatus(isLocked: Boolean) {
        android.util.Log.d("UserPreferences", "Saving user lock status: $isLocked")
        dataStore.edit { preferences ->
            preferences[IS_USER_LOCKED] = isLocked
        }
    }

    fun getUserLockStatus(): Boolean {
        return runBlocking {
            dataStore.data.map { preferences ->
                preferences[IS_USER_LOCKED] ?: false
            }.first()
        }
    }

    val userId: Flow<String?> = dataStore.data.map { preferences ->
        preferences[USER_ID]
    }

    val dishaId: Flow<String?> = dataStore.data.map { preferences ->
        preferences[DISHA_ID]
    }

    val email: Flow<String?> = dataStore.data.map { preferences ->
        preferences[EMAIL]
    }

    val fullName: Flow<String?> = dataStore.data.map { preferences ->
        preferences[FULL_NAME]
    }

    val sessionToken: Flow<String?> = dataStore.data.map { preferences ->
        preferences[SESSION_TOKEN]
    }

    val isUserLocked: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[IS_USER_LOCKED] ?: false
    }

    suspend fun clearUserData() {
        android.util.Log.d("UserPreferences", "Clearing all user data")
        dataStore.edit { preferences ->
            preferences.clear()
        }
        android.util.Log.d("UserPreferences", "User data cleared successfully")
    }
}