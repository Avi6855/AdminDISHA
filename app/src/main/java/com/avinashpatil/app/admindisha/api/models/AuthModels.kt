package com.avinashpatil.app.admindisha.api.models

data class LoginRequest(
    val email: String,
    val dishaId: String,
    val deviceName: String
)

data class LoginResponse(
    val token: String,
    val user: User,
    val refreshToken: RefreshToken
)

data class User(
    val userId: String,
    val dishaId: String,
    val email: String,
    val userName: String,
    val role: String?,
    val activeSessionToken: String?,
    val lastLoginTime: String?,
    val courseIds: List<String>?,
    val batchIds: List<String>?,
    val certificateIds: List<String>?,
    val enrolledCourse: List<String>?,
    val assignedBatch: List<String>?,
    val paidAmount: String?, // API returns as String, convert to Double when needed
    val coursePrice: String?,
    val courseOfferPrice: String?
)

data class RefreshToken(
    val id: Int,
    val token: String,
    val expiryDate: String
)