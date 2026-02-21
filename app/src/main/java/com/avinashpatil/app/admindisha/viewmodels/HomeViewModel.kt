package com.avinashpatil.app.admindisha.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.avinashpatil.app.admindisha.api.Course
import com.avinashpatil.app.admindisha.api.CourseStudentCount
import com.avinashpatil.app.admindisha.data.repository.HomeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: HomeRepository
) : ViewModel() {

    val studentCounts: StateFlow<List<CourseStudentCount>> = repository.studentCounts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val courses: StateFlow<List<Course>> = repository.courses
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _sendCertificateStatus = MutableStateFlow<SendCertificateStatus>(SendCertificateStatus.Idle)
    val sendCertificateStatus: StateFlow<SendCertificateStatus> = _sendCertificateStatus.asStateFlow()

    init {
        refreshData()
    }

    fun refreshData() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val job1 = launch { repository.refreshStudentCounts() }
                val job2 = launch { repository.refreshCourses() }
                joinAll(job1, job2)
            } catch (e: Exception) {
                _error.value = "Error updating data: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun fetchStudentCounts() {
        viewModelScope.launch {
            try {
                repository.refreshStudentCounts()
            } catch (e: Exception) {
                _error.value = "Error: ${e.message}"
            }
        }
    }

    fun fetchCourses() {
        viewModelScope.launch {
            try {
                repository.refreshCourses()
            } catch (e: Exception) {
                // Silent fail or log
            }
        }
    }

    fun sendCertificates(courseId: String) {
        viewModelScope.launch {
            _sendCertificateStatus.value = SendCertificateStatus.Loading
            try {
                val response = repository.sendCertificates(courseId)
                if (response.isSuccessful && response.body() != null) {
                    _sendCertificateStatus.value = SendCertificateStatus.Success(response.body()!!.message)
                } else {
                    _sendCertificateStatus.value = SendCertificateStatus.Error("Failed: ${response.message()}")
                }
            } catch (e: Exception) {
                _sendCertificateStatus.value = SendCertificateStatus.Error("Error: ${e.message}")
            }
        }
    }

    fun resetSendCertificateStatus() {
        _sendCertificateStatus.value = SendCertificateStatus.Idle
    }
}

sealed class SendCertificateStatus {
    object Idle : SendCertificateStatus()
    object Loading : SendCertificateStatus()
    data class Success(val message: String) : SendCertificateStatus()
    data class Error(val message: String) : SendCertificateStatus()
}
