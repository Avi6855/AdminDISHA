package com.avinashpatil.app.admindisha.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.avinashpatil.app.admindisha.api.ApiService
import com.avinashpatil.app.admindisha.api.UserDetailResponse
import com.avinashpatil.app.admindisha.api.Batch
import com.avinashpatil.app.admindisha.api.Course
import com.avinashpatil.app.admindisha.api.UserDTO
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class StudentDetailsUiState {
    object Loading : StudentDetailsUiState()
    data class Success(val userDetails: UserDetailResponse) : StudentDetailsUiState()
    data class Error(val message: String) : StudentDetailsUiState()
}

@HiltViewModel
class StudentDetailsViewModel @Inject constructor(
    private val apiService: ApiService
) : ViewModel() {

    private val _uiState = MutableStateFlow<StudentDetailsUiState>(StudentDetailsUiState.Loading)
    val uiState: StateFlow<StudentDetailsUiState> = _uiState.asStateFlow()

    // Editable fields state
    private val _editableFullName = MutableStateFlow("")
    val editableFullName: StateFlow<String> = _editableFullName.asStateFlow()

    private val _editableEmail = MutableStateFlow("")
    val editableEmail: StateFlow<String> = _editableEmail.asStateFlow()

    // Course and Batch selection
    private val _allCourses = MutableStateFlow<List<Course>>(emptyList())
    val allCourses: StateFlow<List<Course>> = _allCourses.asStateFlow()

    private val _batchesForSelectedCourse = MutableStateFlow<List<Batch>>(emptyList())
    val batchesForSelectedCourse: StateFlow<List<Batch>> = _batchesForSelectedCourse.asStateFlow()

    private val _selectedCourseId = MutableStateFlow<String?>(null)
    val selectedCourseId: StateFlow<String?> = _selectedCourseId.asStateFlow()
    
    private val _selectedCourseName = MutableStateFlow<String?>(null)
    val selectedCourseName: StateFlow<String?> = _selectedCourseName.asStateFlow()

    private val _selectedBatchId = MutableStateFlow<String?>(null)
    val selectedBatchId: StateFlow<String?> = _selectedBatchId.asStateFlow()
    
    private val _selectedBatchNo = MutableStateFlow<String?>(null)
    val selectedBatchNo: StateFlow<String?> = _selectedBatchNo.asStateFlow()

    // Payment fields
    private val _editableTotalPrice = MutableStateFlow("0")
    val editableTotalPrice: StateFlow<String> = _editableTotalPrice.asStateFlow()

    private val _editablePaidAmount = MutableStateFlow("0")
    val editablePaidAmount: StateFlow<String> = _editablePaidAmount.asStateFlow()
    
    private val _editablePendingAmount = MutableStateFlow("0")
    val editablePendingAmount: StateFlow<String> = _editablePendingAmount.asStateFlow()

    private val _editablePaymentStatus = MutableStateFlow("PARTIAL")
    val editablePaymentStatus: StateFlow<String> = _editablePaymentStatus.asStateFlow()

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()
    
    private val _saveMessage = MutableStateFlow<String?>(null)
    val saveMessage: StateFlow<String?> = _saveMessage.asStateFlow()
    
    private var currentUserId: String? = null
    private var currentUserDetails: UserDetailResponse? = null

    init {
        loadCourses()
    }

    fun loadUserDetails(userId: String) {
        currentUserId = userId
        _uiState.value = StudentDetailsUiState.Loading
        viewModelScope.launch {
            try {
                val response = apiService.getUserById(userId)
                if (response.isSuccessful && response.body() != null) {
                    val user = response.body()!!
                    currentUserDetails = user
                    _uiState.value = StudentDetailsUiState.Success(user)
                    
                    // Initialize editable fields
                    _editableFullName.value = user.fullName
                    _editableEmail.value = user.email
                    
                    // Initialize Course
                    // Assuming user.courseId is a single ID, if it's a list we take the first one or handle appropriately
                    // The response has courseIds: List<String> and courseId: String. using courseId.
                    _selectedCourseId.value = user.courseId
                    _selectedCourseName.value = user.courseName
                    
                    // Initialize Batch
                    _selectedBatchId.value = user.batchId
                    _selectedBatchNo.value = user.batchNo
                    
                    // Initialize Payment
                    val finalPrice = user.finalPrice?.toDoubleOrNull() ?: 0.0
                    val paidAmount = user.paidAmount?.toDoubleOrNull() ?: 0.0
                    _editableTotalPrice.value = finalPrice.toString()
                    _editablePaidAmount.value = paidAmount.toString()
                    updatePendingAmount()
                    _editablePaymentStatus.value = user.paymentStatus ?: "PARTIAL"

                    // Load batches if course is selected
                    user.courseId?.let { loadBatches(it) }

                } else {
                    _uiState.value = StudentDetailsUiState.Error("Failed to fetch user details: ${response.message()}")
                }
            } catch (e: Exception) {
                _uiState.value = StudentDetailsUiState.Error("Error: ${e.message}")
            }
        }
    }

    private fun loadCourses() {
        viewModelScope.launch {
            try {
                val response = apiService.getCourses()
                if (response.isSuccessful && response.body() != null) {
                    _allCourses.value = response.body()!!
                }
            } catch (e: Exception) {
                // Handle error silently or log
            }
        }
    }

    private fun loadBatches(courseId: String) {
        viewModelScope.launch {
            try {
                val response = apiService.getBatchesByCourseId(courseId)
                if (response.isSuccessful && response.body() != null) {
                    _batchesForSelectedCourse.value = response.body()!!
                } else {
                    _batchesForSelectedCourse.value = emptyList()
                }
            } catch (e: Exception) {
                _batchesForSelectedCourse.value = emptyList()
            }
        }
    }
    
    fun updateFullName(name: String) {
        _editableFullName.value = name
    }

    fun updateEmail(email: String) {
        _editableEmail.value = email
    }
    
    fun selectCourse(courseId: String, courseName: String) {
        _selectedCourseId.value = courseId
        _selectedCourseName.value = courseName
        // Reset batch when course changes
        _selectedBatchId.value = null
        _selectedBatchNo.value = null
        loadBatches(courseId)
    }
    
    fun selectBatch(batchId: String, batchNo: String) {
        _selectedBatchId.value = batchId
        _selectedBatchNo.value = batchNo
    }

    fun updateTotalPrice(price: String) {
        _editableTotalPrice.value = price
        updatePendingAmount()
    }

    fun updatePaidAmount(amount: String) {
        _editablePaidAmount.value = amount
        updatePendingAmount()
    }
    
    fun updatePendingAmount(amount: String) {
        // Reverse calculation: Paid = Total - Pending
        _editablePendingAmount.value = amount
        val total = _editableTotalPrice.value.toDoubleOrNull() ?: 0.0
        val pending = amount.toDoubleOrNull() ?: 0.0
        val newPaid = (total - pending).coerceAtLeast(0.0)
        _editablePaidAmount.value = newPaid.toString()
    }

    private fun updatePendingAmount() {
        val total = _editableTotalPrice.value.toDoubleOrNull() ?: 0.0
        val paid = _editablePaidAmount.value.toDoubleOrNull() ?: 0.0
        val pending = (total - paid).coerceAtLeast(0.0)
        _editablePendingAmount.value = pending.toString()
    }

    fun updatePaymentStatus(status: String) {
        _editablePaymentStatus.value = status
    }

    fun saveChanges() {
        val userId = currentUserId ?: return
        val details = currentUserDetails ?: return
        
        _isSaving.value = true
        _saveMessage.value = null
        
        viewModelScope.launch {
            try {
                // Create UserDTO with updated fields
                val userDTO = UserDTO(
                    userId = userId,
                    dishaId = details.dishaId,
                    email = _editableEmail.value,
                    fullName = _editableFullName.value,
                    role = details.role,
                    courseIds = if (_selectedCourseId.value != null) listOf(_selectedCourseId.value!!) else details.courseIds,
                    batchIds = if (_selectedBatchId.value != null) listOf(_selectedBatchId.value!!) else details.batchIds,
                    certificateIds = details.certificateIds,
                    paidAmount = _editablePaidAmount.value.toDoubleOrNull(),
                    finalPrice = _editableTotalPrice.value,
                    paymentStatus = _editablePaymentStatus.value,
                    isLocked = details.lock
                )
                
                val response = apiService.updateUser(userId, userDTO)
                if (response.isSuccessful && response.body() != null) {
                    _saveMessage.value = "Details updated successfully"
                    val updatedUser = response.body()!!
                    currentUserDetails = updatedUser
                    _uiState.value = StudentDetailsUiState.Success(updatedUser)
                    
                    // Update local state if needed, or rely on UI observing uiState
                    // We already updated uiState with Success(updatedUser), so the UI should reflect changes
                    // However, we might want to ensure editable fields are also synced if the backend changed something
                    _editableFullName.value = updatedUser.fullName
                    _editableEmail.value = updatedUser.email
                    _selectedCourseId.value = updatedUser.courseId
                    _selectedCourseName.value = updatedUser.courseName
                    _selectedBatchId.value = updatedUser.batchId
                    _selectedBatchNo.value = updatedUser.batchNo
                    
                    val finalPrice = updatedUser.finalPrice?.toDoubleOrNull() ?: 0.0
                    val paidAmount = updatedUser.paidAmount?.toDoubleOrNull() ?: 0.0
                    _editableTotalPrice.value = finalPrice.toString()
                    _editablePaidAmount.value = paidAmount.toString()
                    updatePendingAmount()
                    _editablePaymentStatus.value = updatedUser.paymentStatus ?: "PARTIAL"

                } else {
                    _saveMessage.value = "Failed to update: ${response.message()}"
                }
            } catch (e: Exception) {
                _saveMessage.value = "Error saving changes: ${e.message}"
            } finally {
                _isSaving.value = false
            }
        }
    }
    
    fun clearSaveMessage() {
        _saveMessage.value = null
    }

    fun reload() {
        currentUserId?.let { loadUserDetails(it) }
    }
}
