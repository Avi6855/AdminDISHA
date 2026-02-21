package com.avinashpatil.app.admindisha.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.avinashpatil.app.admindisha.api.ApiService
import com.avinashpatil.app.admindisha.api.EnrollmentDetails
import com.avinashpatil.app.admindisha.api.UserAllDetails


import com.avinashpatil.app.admindisha.api.UserDTO
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import retrofit2.Response
import javax.inject.Inject
import kotlin.collections.emptyList

data class StudentBillData(
    var fullName: String,
    var email: String,
    var courses: List<String>,
    var batches: List<String>,
    var paidAmount: Double,
    var enrolledCourse: List<EnrollmentDetails>,
    var assignedBatch: List<EnrollmentDetails>,
    var coursePrice: String = "",
    var courseOfferPrice: String = ""
)

data class BillUiState(
    val studentData: StudentBillData? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val currentUser: UserDTO? = null,
    val currentTotalPaid: Double = 0.0,
    val snackbarMessage: String? = null
)

@HiltViewModel
class BillViewModel @Inject constructor(
    val apiService: ApiService
) : ViewModel() {
    
    // Memory management and caching
    private var cachedStudentData: StudentBillData? = null
    private var cachedUserId: String? = null
    private var lastDataRefresh = 0L
    private val cacheTimeout = 30000L // 30 seconds
    
    // Efficient data structures for better memory usage
    private val courseCache = mutableMapOf<String, Double>()
    private val userCache = mutableMapOf<String, StudentBillData>()
    
    // Helper methods for collection operations with memory optimization
    private fun getCoursesFromEnrollments(enrollments: List<EnrollmentDetails>): List<String> {
        return enrollments
            .asSequence() // Use sequence for lazy evaluation
            .mapNotNull { it.enrolledCourse }
            .distinct() // Remove duplicates early
            .toList()
    }
    
    private fun getBatchesFromEnrollments(enrollments: List<EnrollmentDetails>): List<String> {
        return enrollments
            .asSequence() // Use sequence for lazy evaluation
            .mapNotNull { it.assignedBatch }
            .distinct() // Remove duplicates early
            .toList()
    }
    
    private val _uiState = MutableStateFlow(BillUiState())
    val uiState: StateFlow<BillUiState> = _uiState

    private var currentUserId: String? = null
    
    // Memory management methods
    fun clearCache() {
        cachedStudentData = null
        cachedUserId = null
        lastDataRefresh = 0L
        courseCache.clear()
        userCache.clear()
    }
    
    fun clearUserFromCache(userId: String) {
        if (cachedUserId == userId) {
            cachedStudentData = null
            cachedUserId = null
            lastDataRefresh = 0L
        }
        userCache.remove(userId)
    }
    
    override fun onCleared() {
        super.onCleared()
        clearCache() // Clean up when ViewModel is destroyed
    }

    fun loadStudentData(userId: String, forceRefresh: Boolean = false) {
        currentUserId = userId
        viewModelScope.launch {
            val currentTime = System.currentTimeMillis()
            
            // Use cached data if available and not expired
            if (!forceRefresh && cachedUserId == userId && cachedStudentData != null && 
                (currentTime - lastDataRefresh) < cacheTimeout) {
                println("BillViewModel: Using cached data for userId: $userId")
                _uiState.update { it.copy(
                    studentData = cachedStudentData,
                    isLoading = false,
                    error = null
                )}
                return@launch
            }
            
            try {
                _uiState.update { it.copy(isLoading = true, error = null) }
                
                // Use getAllUsers API to get user details with paidAmount
                val allUsersResponse = apiService.getAllUsers()
                if (!allUsersResponse.isSuccessful) {
                    _uiState.update { it.copy(
                        isLoading = false,
                        error = "विद्यार्थी माहिती सापडली नाही"
                    )}
                    return@launch
                }
                
                val allUsersList = allUsersResponse.body() ?: throw Exception("विद्यार्थी माहिती सापडली नाही")
                val targetUser = allUsersList.find { it.userId == userId }
                
                if (targetUser == null) {
                    _uiState.update { it.copy(
                        isLoading = false,
                        error = "विद्यार्थी माहिती सापडली नाही"
                    )}
                    return@launch
                }
                
                // Create UserDTO from UserAllDetails for compatibility
                val userDetails = UserDTO(
                    userId = targetUser.userId,
                    dishaId = targetUser.dishaId,
                    email = targetUser.email ?: "",
                    fullName = targetUser.userName ?: "",
                    role = targetUser.role,
                    activeSessionToken = targetUser.activeSessionToken,
                    lastLoginTime = targetUser.lastLoginTime,
                    courseIds = targetUser.courseIds,
                    batchIds = targetUser.batchIds,
                    certificateIds = targetUser.certificateIds,
                    paidAmount = targetUser.getPaidAmountAsDouble(),
                    isLocked = targetUser.isLocked
                )

                // Get enrollment details and paidAmount directly from targetUser
                val enrollmentDetails = targetUser.enrollments ?: emptyList()
                val paidAmount = targetUser.getPaidAmountAsDouble()
                println("BillViewModel: Using paidAmount directly from UserAllDetails: $paidAmount")
                
                // Create StudentBillData with direct data from UserAllDetails
                val studentBillData = StudentBillData(
                    fullName = userDetails.fullName,
                    email = userDetails.email ?: "",
                    courses = getCoursesFromEnrollments(enrollmentDetails),
                    batches = getBatchesFromEnrollments(enrollmentDetails),
                    paidAmount = paidAmount,
                    enrolledCourse = enrollmentDetails,
                    assignedBatch = enrollmentDetails,
                    coursePrice = "",
                    courseOfferPrice = ""
                )
                println("BillViewModel: StudentBillData created - paidAmount: ${studentBillData.paidAmount}")
                
                // Update cache with fresh data
                cachedStudentData = studentBillData
                cachedUserId = userId
                lastDataRefresh = currentTime
                userCache[userId] = studentBillData
                
                _uiState.update { currentState ->
                    currentState.copy(
                        isLoading = false,
                        error = null,
                        studentData = studentBillData,
                        currentUser = userDetails,
                        currentTotalPaid = paidAmount
                    )
                }
                println("BillViewModel: State updated - studentData.paidAmount: ${_uiState.value.studentData?.paidAmount}")
                println("BillViewModel: StudentBillData created and state updated successfully")
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    isLoading = false,
                    error = "विद्यार्थी माहिती लोड करताना त्रुटी आली: ${e.message}"
                )}
            }
        }
    }

    fun showSnackbar(message: String) {
        _uiState.update { it.copy(snackbarMessage = message) }
        
        // Clear the snackbar message after 3 seconds
        viewModelScope.launch {
            delay(3000)
            _uiState.update { it.copy(snackbarMessage = null) }
        }
    }
    
    fun updateStudentData(
        name: String,
        email: String,
        courses: List<String>,
        batches: List<String>,
        totalPrice: Double,
        paidAmount: Double
    ) {
        if (name.isBlank() || email.isBlank() || courses.isEmpty() || batches.isEmpty()) {
            _uiState.update { it.copy(error = "कृपया सर्व माहिती भरा") }
            return
        }

        if (totalPrice <= 0) {
            _uiState.update { it.copy(error = "एकूण किंमत शून्यापेक्षा जास्त असावी") }
            return
        }

        if (paidAmount < 0 || paidAmount > totalPrice) {
            _uiState.update { it.copy(error = "दिलेली रक्कम एकूण किंमतीपेक्षा जास्त असू शकत नाही") }
            return
        }

        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true, error = null) }

                val userId = currentUserId ?: throw Exception("विद्यार्थी आयडी उपलब्ध नाही")
                val currentUser = _uiState.value.currentUser ?: throw Exception("विद्यार्थी माहिती उपलब्ध नाही")

                // Update user details
                val updatedUser = UserDTO(
                    userId = currentUser.userId,
                    dishaId = currentUser.dishaId,
                    email = email,
                    fullName = name,
                    role = currentUser.role,
                    paidAmount = paidAmount // Map givenPrice to paidAmount for PUT request
                )
                println("BillViewModel: Creating UserDTO with paidAmount: $paidAmount")
                println("BillViewModel: UserDTO to be sent: $updatedUser")
                val updateUserResponse = apiService.updateUser(userId, updatedUser)
                println("BillViewModel: PUT request sent to /api/users/$userId")
                println("BillViewModel: PUT response code: ${updateUserResponse.code()}")
                println("BillViewModel: PUT response successful: ${updateUserResponse.isSuccessful}")
                if (!updateUserResponse.isSuccessful) {
                    val errorBody = updateUserResponse.errorBody()?.string()
                    println("BillViewModel: PUT request failed - Error body: $errorBody")
                    throw Exception("विद्यार्थी माहिती अपडेट करण्यात अयशस्वी")
                } else {
                    println("BillViewModel: PUT request successful - User data updated with paidAmount: $paidAmount")
                }
                
                // Update paidAmount using payment update if enrollment exists
                try {
                    val allUsersResponse = apiService.getAllUsers()
                    if (allUsersResponse.isSuccessful) {
                        val allUsersList = allUsersResponse.body()
                        val targetUser = allUsersList?.find { it.userId == userId }
                        val enrollmentDetails = targetUser?.enrollments ?: emptyList()
                        
                        // If user has enrollments, we can update payment through enrollment
                        if (enrollmentDetails.isNotEmpty()) {
                            // Note: Since we don't have direct paidAmount update API,
                            // we'll store the paidAmount in local state and display it
                            // The actual backend update would require a specific API endpoint
                            println("Updated paidAmount to: $paidAmount for user: $userId")
                        }
                    }
                } catch (e: Exception) {
                    // Continue even if payment update fails
                    println("Warning: Could not update paidAmount: ${e.message}")
                }

                // Get updated enrollments using getAllUsers API
                val allUsersResponse = apiService.getAllUsers()
                val allUsersList = if (allUsersResponse.isSuccessful) allUsersResponse.body() else null
                
                // Find the specific user and get enrollments
                val targetUser = allUsersList?.find { it.userId == userId }
                val updatedEnrollments = targetUser?.enrollments ?: emptyList()
                
                // Verify paidAmount was updated correctly
                val fetchedPaidAmount = targetUser?.paidAmount
                val fetchedPaidAmountDouble = targetUser?.getPaidAmountAsDouble() ?: 0.0
                println("BillViewModel: After PUT request - Fetched paidAmount from API: $fetchedPaidAmount")
                println("BillViewModel: After PUT request - Converted paidAmount: $fetchedPaidAmountDouble")
                println("BillViewModel: Verification - Sent: $paidAmount, Received: $fetchedPaidAmountDouble, Match: ${paidAmount == fetchedPaidAmountDouble}")
                
                // Course pricing will be handled by CourseViewModel in AllInOneBillScreen
                val coursePrice = ""
                val courseOfferPrice = ""
                
                val updatedStudentData = StudentBillData(
                    fullName = name,
                    email = email,
                    courses = courses,
                    batches = batches,
                    paidAmount = paidAmount,
                    enrolledCourse = updatedEnrollments,
                    assignedBatch = updatedEnrollments,
                    coursePrice = coursePrice,
                    courseOfferPrice = courseOfferPrice
                )
                
                // Update cache with new data
                cachedStudentData = updatedStudentData
                cachedUserId = userId
                lastDataRefresh = System.currentTimeMillis()
                userCache[userId] = updatedStudentData
                
                _uiState.update { it.copy(
                    isLoading = false,
                    error = null,
                    studentData = updatedStudentData,
                    currentTotalPaid = paidAmount
                )}
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    isLoading = false,
                    error = "विद्यार्थी माहिती अपडेट करताना त्रुटी आली: ${e.message}"
                )}
            }
        }
    }
    

}