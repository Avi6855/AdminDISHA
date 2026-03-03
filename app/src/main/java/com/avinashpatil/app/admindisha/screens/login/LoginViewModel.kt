package com.avinashpatil.app.admindisha.screens.login

import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.avinashpatil.app.admindisha.data.repository.AuthRepository
import com.avinashpatil.app.admindisha.data.store.TokenManager
import com.avinashpatil.app.admindisha.data.store.UserPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.IOException
import java.net.SocketTimeoutException
import java.time.Instant
import javax.inject.Inject

sealed class LoginState {
    object Idle : LoginState()
    object Loading : LoginState()
    object Success : LoginState()
    data class Error(val message: String) : LoginState()
}

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userPreferences: UserPreferences,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _loginState = MutableStateFlow<LoginState>(LoginState.Idle)
    val loginState: StateFlow<LoginState> = _loginState

    private val _deviceName = MutableStateFlow(Build.MODEL)
    val deviceName: StateFlow<String> = _deviceName
    
    // Function to check if user is locked
    fun isUserLocked(): Boolean {
        return userPreferences.getUserLockStatus()
    }

    fun login(email: String, dishaId: String) {
        viewModelScope.launch {
            try {
                _loginState.value = LoginState.Loading

                val response = authRepository.login(
                    email = email,
                    dishaId = dishaId,
                    deviceName = deviceName.value
                )

                when (response.code()) {
                    200 -> {
                        val loginResponse = response.body()
                        android.util.Log.d("LoginViewModel", "Login Response: $loginResponse")
                        android.util.Log.d("LoginViewModel", "Response Code: ${response.code()}, Raw Response: ${response.raw()}")

                        if (loginResponse == null) {
                            _loginState.value = LoginState.Error("Server returned empty response. Please try again.")
                            return@launch
                        }

                        if (loginResponse.user == null) {
                            _loginState.value = LoginState.Error("User data not found in response.")
                            return@launch
                        }
                        
                        // Check if user is locked
                        if (loginResponse.user.isLocked == true) {
                            userPreferences.saveUserLockStatus(true)
                            _loginState.value = LoginState.Error("Your account is locked. Please contact the branch.")
                            return@launch
                        }

                        // Store user data in preferences
                        userPreferences.saveUserData(
                            userId = loginResponse.user.userId,
                            dishaId = loginResponse.user.dishaId,
                            fullName = loginResponse.user.fullName ?: "",
                            email = loginResponse.user.email ?: "",
                            sessionToken = loginResponse.token ?: ""
                        )
                        
                        // Save lock status
                        userPreferences.saveUserLockStatus(loginResponse.user.isLocked ?: false)

                        // Store tokens with expiry (1 hour from now)
                        val expiryTime = Instant.now().plusSeconds(3600).epochSecond
                        tokenManager.saveTokens(
                            accessToken = loginResponse.token ?: "",
                            refreshToken = loginResponse.refreshToken?.token ?: "",
                            expiryTimestamp = expiryTime
                        )

                        _loginState.value = LoginState.Success
                    }
                    401 -> {
                        val errorBody = response.errorBody()?.string()
                        android.util.Log.d("LoginViewModel", "401 Error Body: $errorBody")
                        _loginState.value = LoginState.Error("Invalid email or DISHA ID. Please check your credentials and try again.")
                    }
                    500 -> {
                        val errorBody = response.errorBody()?.string()
                        android.util.Log.d("LoginViewModel", "500 Error Body: $errorBody")
                        if (errorBody?.contains("already logged in") == true) {
                            _loginState.value = LoginState.Error("You are already logged in from another device. Please log out first.")
                        } else {
                            _loginState.value = LoginState.Error("Server error occurred. Please try again later.")
                        }
                    }
                    else -> {
                        val errorBody = response.errorBody()?.string()
                        android.util.Log.d("LoginViewModel", "Other Error Body: $errorBody")
                        _loginState.value = LoginState.Error("An unexpected error occurred. Please try again.")
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("LoginViewModel", "Login Exception: ${e.message}")
                val userMessage = when (e) {
                    is SocketTimeoutException -> "Connection timed out. Please check your internet and try again."
                    is IOException -> "Unable to reach the server. Please check your internet connection or try again later."
                    else -> "An unexpected error occurred: ${e.message}"
                }
                _loginState.value = LoginState.Error(userMessage)
            }
        }
    }
}
