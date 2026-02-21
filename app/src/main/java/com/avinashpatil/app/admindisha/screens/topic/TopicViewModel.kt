package com.avinashpatil.app.admindisha.screens.topic

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.avinashpatil.app.admindisha.api.ApiService
import com.avinashpatil.app.admindisha.api.Topic
import com.avinashpatil.app.admindisha.data.repository.HomeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TopicViewModel @Inject constructor(
    private val repository: HomeRepository,
    private val apiService: ApiService
) : ViewModel() {

    private val _currentCourseId = MutableStateFlow<String?>(null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val topics: StateFlow<List<Topic>> = _currentCourseId
        .flatMapLatest { courseId ->
            if (courseId != null) {
                repository.getTopics(courseId)
            } else {
                flowOf(emptyList())
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun fetchTopics(courseId: String) {
        _currentCourseId.value = courseId
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                repository.refreshTopics(courseId)
            } catch (e: Exception) {
                _error.value = "Error: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun createTopic(courseId: String, topicName: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val request = Topic(topicName = topicName, courseId = courseId)
                val response = apiService.createTopic(request)
                if (response.isSuccessful) {
                    fetchTopics(courseId) // Refresh list
                } else {
                    _error.value = "Failed to create topic: ${response.message()}"
                }
            } catch (e: Exception) {
                _error.value = "Error: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateTopic(topicId: String, courseId: String, topicName: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val request = Topic(topicId = topicId, topicName = topicName, courseId = courseId)
                val response = apiService.updateTopic(topicId, request)
                if (response.isSuccessful) {
                    fetchTopics(courseId) // Refresh list
                } else {
                    _error.value = "Failed to update topic: ${response.message()}"
                }
            } catch (e: Exception) {
                _error.value = "Error: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteTopic(topicId: String, courseId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = apiService.deleteTopic(topicId)
                if (response.isSuccessful) {
                    fetchTopics(courseId) // Refresh list
                } else {
                    _error.value = "Failed to delete topic: ${response.message()}"
                }
            } catch (e: Exception) {
                _error.value = "Error: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
}
