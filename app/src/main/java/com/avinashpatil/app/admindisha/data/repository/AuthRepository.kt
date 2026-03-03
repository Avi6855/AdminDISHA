package com.avinashpatil.app.admindisha.data.repository

import com.avinashpatil.app.admindisha.api.ApiService
import com.avinashpatil.app.admindisha.api.LoginRequest
import com.avinashpatil.app.admindisha.api.LoginResponse
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val apiService: ApiService
) {

    suspend fun login(
        email: String,
        dishaId: String,
        deviceName: String,
        rememberMe: Boolean = true
    ): Response<LoginResponse> {
        val request = LoginRequest(
            email = email,
            dishaId = dishaId,
            rememberMe = rememberMe,
            deviceName = deviceName
        )
        return apiService.login(request)
    }
}

