package com.avinashpatil.app.admindisha.api

import com.avinashpatil.app.admindisha.api.models.LoginRequest
import com.avinashpatil.app.admindisha.api.models.LoginResponse
import com.avinashpatil.app.admindisha.api.models.RefreshToken
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

data class RefreshRequest(
    val refreshToken: String
)

data class RefreshResponse(
    val token: String,
    val refreshToken: String
)

data class DataResponse(
    val data: String
)

interface AuthApi {
    @POST("api/auth/login")
    suspend fun login(@Body loginRequest: LoginRequest): Response<LoginResponse>

    @POST("api/auth/refresh")
    suspend fun refreshToken(@Body refreshRequest: RefreshRequest): Response<RefreshResponse>
}

interface ProtectedApi {
    @GET("protected/data")
    suspend fun getProtectedData(): Response<DataResponse>
}