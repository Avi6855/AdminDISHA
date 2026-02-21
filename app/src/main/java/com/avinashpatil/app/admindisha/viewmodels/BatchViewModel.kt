package com.avinashpatil.app.admindisha.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.avinashpatil.app.admindisha.api.ApiService
import com.avinashpatil.app.admindisha.api.Batch
import com.avinashpatil.app.admindisha.data.repository.HomeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

data class BatchUiState(
    val batches: List<Batch> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val selectedCourseId: String? = null
)

@HiltViewModel
class BatchViewModel @Inject constructor(
    private val apiService: ApiService,
    private val repository: HomeRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(BatchUiState())
    val uiState: StateFlow<BatchUiState> = _uiState.asStateFlow()

    private val _currentCourseId = MutableStateFlow<String?>(null)
    private val _searchQuery = MutableStateFlow("")

    @OptIn(ExperimentalCoroutinesApi::class)
    val batches: StateFlow<List<Batch>> = _currentCourseId
        .flatMapLatest { courseId ->
            combine(
                repository.getBatches(courseId),
                _searchQuery
            ) { batches, query ->
                if (query.isBlank()) {
                    batches
                } else {
                    batches.filter { batch ->
                        batch.batchNo.contains(query, ignoreCase = true) ||
                        batch.timing?.toString()?.contains(query, ignoreCase = true) == true
                    }
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            batches.collect { batchList ->
                _uiState.value = _uiState.value.copy(batches = batchList)
            }
        }
    }

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private var currentCourseId: String? = null

    fun setCourseId(courseId: String?) {
        currentCourseId = courseId
        _currentCourseId.value = courseId
        _uiState.value = _uiState.value.copy(selectedCourseId = courseId)
    }

    fun loadBatches(courseId: String) {
        setCourseId(courseId)
        refreshBatches(courseId)
    }

    fun fetchBatches() {
        setCourseId(null)
        refreshBatches(null)
    }

    private fun refreshBatches(courseId: String?) {
        viewModelScope.launch {
            _isLoading.value = true
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            _error.value = null
            try {
                repository.refreshBatches(courseId)
                _uiState.value = _uiState.value.copy(isLoading = false, error = null)
            } catch (e: Exception) {
                val errorMessage = "Failed to load batches: ${e.message}"
                _error.value = errorMessage
                _uiState.value = _uiState.value.copy(isLoading = false, error = errorMessage)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun createBatch(batch: Batch) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, error = null)
                _isLoading.value = true
                _error.value = null

                val response = apiService.createBatch(batch)
                if (response.isSuccessful) {
                    refreshBatches(currentCourseId)
                } else {
                    refreshBatches(currentCourseId)
                }
            } catch (e: Exception) {
                val errorMessage = "Failed to create batch: ${e.message}"
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = errorMessage
                )
                _error.value = errorMessage
                _isLoading.value = false
            }
        }
    }

    fun updateBatch(updatedBatch: Batch) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, error = null)
                _isLoading.value = true
                _error.value = null

                apiService.updateBatch(updatedBatch.batchId, updatedBatch)

                refreshBatches(currentCourseId)
            } catch (e: Exception) {
                val errorMessage = "Failed to update batch: ${e.message}"
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = errorMessage
                )
                _error.value = errorMessage
                _isLoading.value = false
            }
        }
    }

    fun deleteBatch(batch: Batch) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, error = null)
                _isLoading.value = true
                _error.value = null

                apiService.deleteBatch(batch.batchId)

                refreshBatches(currentCourseId)
            } catch (e: Exception) {
                val errorMessage = "Failed to delete batch: ${e.message}"
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = errorMessage
                )
                _error.value = errorMessage
                _isLoading.value = false
            }
        }
    }

    fun searchBatches(query: String) {
        _searchQuery.value = query
    }
}