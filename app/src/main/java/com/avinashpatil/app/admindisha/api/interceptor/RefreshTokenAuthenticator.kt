package com.avinashpatil.app.admindisha.api.interceptor

import com.avinashpatil.app.admindisha.data.store.TokenManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import retrofit2.http.Body
import retrofit2.http.POST
import java.time.Instant
import javax.inject.Inject

class RefreshTokenAuthenticator @Inject constructor(
    private val tokenManager: TokenManager,
    private val refreshTokenApi: RefreshTokenApi
) : Authenticator {

    companion object {
        private const val AUTHORIZATION = "Authorization"
        private const val BEARER = "Bearer "
        private const val RETRY_WITH_REFRESH = "Retry-With-Refresh-Token"
        private const val MAX_RETRIES = 3
    }

    override fun authenticate(route: Route?, response: Response): Request? {
        // Check if we've already tried refreshing the token
        val retryCount = response.request.header(RETRY_WITH_REFRESH)?.toIntOrNull() ?: 0
        if (retryCount >= MAX_RETRIES) {
            android.util.Log.w("RefreshTokenAuthenticator", "Max retry attempts reached")
            return null
        }

        return runBlocking {
            try {
                val refreshToken = tokenManager.refreshToken.first()
                if (refreshToken == null) {
                    android.util.Log.w("RefreshTokenAuthenticator", "No refresh token available")
                    tokenManager.clearTokens()
                    return@runBlocking null
                }

                try {
                    val refreshResponse = refreshTokenApi.refreshToken(RefreshTokenRequest(refreshToken))
                    if (!refreshResponse.isSuccessful || refreshResponse.body() == null) {
                        android.util.Log.e("RefreshTokenAuthenticator", "Token refresh failed: ${refreshResponse.code()}")
                        tokenManager.clearTokens()
                        return@runBlocking null
                    }
                    
                    // Check if body is actually available to avoid NPE
                    val responseBody = refreshResponse.body()
                    if (responseBody == null) {
                        android.util.Log.e("RefreshTokenAuthenticator", "Token refresh response body is null")
                        tokenManager.clearTokens()
                        return@runBlocking null
                    }

                    val newTokens = responseBody
                    tokenManager.saveTokens(
                        accessToken = newTokens.accessToken,
                        refreshToken = newTokens.refreshToken,
                        expiryTimestamp = newTokens.expiryTimestamp
                    )
    
                    // Retry the original request with the new token
                    response.request.newBuilder()
                        .header(AUTHORIZATION, BEARER + newTokens.accessToken)
                        .header(RETRY_WITH_REFRESH, (retryCount + 1).toString())
                        .build()
                } catch (e: Exception) {
                    android.util.Log.e("RefreshTokenAuthenticator", "Exception during token refresh", e)
                    tokenManager.clearTokens()
                    return@runBlocking null
                }

            } catch (e: Exception) {
                android.util.Log.e("RefreshTokenAuthenticator", "Token refresh failed with exception", e)
                tokenManager.clearTokens()
                null
            }
        }
    }
}

interface RefreshTokenApi {
    @POST("api/auth/refresh")
    suspend fun refreshToken(@Body request: RefreshTokenRequest): retrofit2.Response<TokenResponse>
}

data class RefreshTokenRequest(
    val refreshToken: String
)

data class TokenResponse(
    val accessToken: String,
    val refreshToken: String,
    val expiryTimestamp: Long
)