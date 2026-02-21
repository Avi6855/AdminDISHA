package com.avinashpatil.app.admindisha.api.interceptor

import com.avinashpatil.app.admindisha.data.store.TokenManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class AuthInterceptor @Inject constructor(
    private val tokenManager: TokenManager
) : Interceptor {

    companion object {
        private const val AUTHORIZATION = "Authorization"
        private const val BEARER = "Bearer "
        private val SKIP_URLS = listOf("/api/auth/login", "/api/auth/refresh", "/api/auth/register")
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val url = request.url.toString()

        // Skip authentication for login, register and refresh endpoints
        if (SKIP_URLS.any { url.contains(it) }) {
            return chain.proceed(request)
        }

        val token = runBlocking {
            tokenManager.accessToken.first()
        }

        if (token == null) {
            // If no token is available, proceed without authentication
            return chain.proceed(request)
        }

        // Add access token to the request
        val authenticatedRequest = request.newBuilder()
            .header(AUTHORIZATION, BEARER + token)
            .build()

        return chain.proceed(authenticatedRequest)
    }
}