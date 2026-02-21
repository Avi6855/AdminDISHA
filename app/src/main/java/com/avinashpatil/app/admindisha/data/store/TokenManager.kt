package com.avinashpatil.app.admindisha.data.store

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

private val Context.tokenDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "token_preferences"
)

@Singleton
class TokenManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val dataStore = context.tokenDataStore

    companion object {
        private val ACCESS_TOKEN = stringPreferencesKey("access_token")
        private val REFRESH_TOKEN = stringPreferencesKey("refresh_token")
        private val TOKEN_EXPIRY = longPreferencesKey("token_expiry")
        private const val TOKEN_EXPIRY_BUFFER_SECONDS = 300L // 5 minutes
    }

    suspend fun saveTokens(accessToken: String, refreshToken: String, expiryTimestamp: Long) {
        android.util.Log.d("TokenManager", "Saving tokens - Access: $accessToken, Refresh: $refreshToken, Expiry: $expiryTimestamp")
        dataStore.edit { preferences ->
            preferences[ACCESS_TOKEN] = accessToken
            preferences[REFRESH_TOKEN] = refreshToken
            preferences[TOKEN_EXPIRY] = expiryTimestamp
        }
    }

    val accessToken: Flow<String?> = dataStore.data.map { preferences ->
        val token = preferences[ACCESS_TOKEN]
        val expiry = preferences[TOKEN_EXPIRY] ?: 0L
        val currentTime = Instant.now().epochSecond

        // Return null if token is expired or missing
        if (token == null || expiry <= currentTime) {
            null
        } else {
            token
        }
    }

    val refreshToken: Flow<String?> = dataStore.data.map { preferences ->
        preferences[REFRESH_TOKEN]
    }

    val tokenExpiry: Flow<Long?> = dataStore.data.map { preferences ->
        preferences[TOKEN_EXPIRY]
    }

    suspend fun clearTokens() {
        android.util.Log.d("TokenManager", "Clearing all tokens")
        dataStore.edit { preferences ->
            preferences.clear()
        }
    }

    suspend fun isTokenExpiringSoon(): Boolean {
        val expiry = tokenExpiry.first() ?: return true
        val currentTime = Instant.now().epochSecond
        return (expiry - currentTime) <= TOKEN_EXPIRY_BUFFER_SECONDS
    }

    suspend fun hasValidToken(): Boolean {
        val token = accessToken.first()
        val expiry = tokenExpiry.first() ?: return false
        val currentTime = Instant.now().epochSecond
        return token != null && expiry > currentTime
    }
}