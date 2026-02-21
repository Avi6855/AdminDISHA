package com.avinashpatil.app.admindisha.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.avinashpatil.app.admindisha.api.ApiService
import com.avinashpatil.app.admindisha.api.Batch
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BatchViewModel @Inject constructor(
    private val apiService: ApiService
) : ViewModel() {

    private val _batches = MutableStateFlow<List<Batch>>(emptyList())
    val batches: StateFlow<List<Batch>> = _batches

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _courseId = MutableStateFlow<String?>(null)
    val courseId: StateFlow<String?> = _courseId

    init {
        fetchBatches()
    }

    fun setCourseId(id: String?) {
        _courseId.value = id
        fetchBatches()
    }

    fun fetchBatches() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                val response = if (_courseId.value != null) {
                    apiService.getBatchesByCourseId(_courseId.value!!)
                } else {
                    apiService.getBatches()
                }
                if (!response.isSuccessful) {
                    _error.value = "Server error: ${response.code()} - ${response.message()}"
                    _batches.value = emptyList()
                    return@launch
                }
                val body = response.body()
                if (body == null) {
                    _error.value = "Empty response from server"
                    _batches.value = emptyList()
                    return@launch
                }
                _batches.value = body
                _error.value = null
            } catch (e: Exception) {
                _error.value = "Network error: ${e.message}"
                _batches.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadBatches(courseId: String) {
        _courseId.value = courseId
        fetchBatches()
    }

    fun createBatch(batch: Batch) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                if (batch.courseId.isBlank()) {
                    _error.value = "Course ID is required to create a batch"
                    return@launch
                }
                val response = apiService.createBatch(batch)
                if (!response.isSuccessful) {
                    _error.value = "Failed to create batch: ${response.code()} - ${response.message()}"
                    return@launch
                }
                val newBatch = response.body()
                if (newBatch == null) {
                    _error.value = "Server returned empty response"
                    return@launch
                }
                _batches.value = (_batches.value + newBatch) as List<Batch>
                _error.value = null
            } catch (e: Exception) {
                _error.value = "Error creating batch: ${e.message}"
            } finally {
                _isLoading.value = false
                fetchBatches() // Refresh the list to ensure consistency
            }
        }
    }

    fun updateBatch(batch: Batch) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                val response = apiService.updateBatch(batch.batchId, batch)
                if (!response.isSuccessful) {
                    _error.value = "Failed to update batch: ${response.code()} - ${response.message()}"
                    return@launch
                }
                val updatedBatch = response.body()
                if (updatedBatch == null) {
                    _error.value = "Server returned empty response"
                    return@launch
                }
                _batches.value = _batches.value.map { 
                    if (it.batchId == updatedBatch.batchId) updatedBatch else it 
                }
                _error.value = null
            } catch (e: Exception) {
                _error.value = "Error updating batch: ${e.message}"
            } finally {
                _isLoading.value = false
                fetchBatches() // Refresh the list to ensure consistency
            }
        }
    }

    fun deleteBatch(batch: Batch) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                val response = apiService.deleteBatch(batch.batchId)
                if (!response.isSuccessful) {
                    _error.value = "Failed to delete batch: ${response.code()} - ${response.message()}"
                    return@launch
                }
                _batches.value = _batches.value.filter { it.batchId != batch.batchId }
                _error.value = null
            } catch (e: Exception) {
                _error.value = "Error deleting batch: ${e.message}"
            } finally {
                _isLoading.value = false
                fetchBatches() // Refresh the list to ensure consistency
            }
        }
    }
}