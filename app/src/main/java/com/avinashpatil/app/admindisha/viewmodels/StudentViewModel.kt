package com.avinashpatil.app.admindisha.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.avinashpatil.app.admindisha.api.ApiService
import com.avinashpatil.app.admindisha.api.Batch
import com.avinashpatil.app.admindisha.api.UserAllDetails
import com.avinashpatil.app.admindisha.api.UserDTO
import com.avinashpatil.app.admindisha.api.PaymentDto
import com.avinashpatil.app.admindisha.api.Course
import com.avinashpatil.app.admindisha.api.EnrollmentDetails
import com.avinashpatil.app.admindisha.api.Enrollments
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.avinashpatil.app.admindisha.api.LockUserRequest
import com.avinashpatil.app.admindisha.data.store.TokenManager
import kotlinx.coroutines.flow.first

// Placeholder for missing data class
data class UserStatistics(
    val dummyField: String = "" // Adjust according to actual API response
)

// Placeholder for missing data class
data class ExamResult(
    val dummyField: String = "" // Adjust according to actual API response
)

sealed class StudentUiState {
    object Loading : StudentUiState()
    data class Success(val students: List<UserAllDetails>) : StudentUiState()
    data class Error(val message: String) : StudentUiState()
}

sealed class CourseUiState {
    object Loading : CourseUiState()
    data class Success(val courses: List<Course>) : CourseUiState()
    data class Error(val message: String) : CourseUiState()
}

sealed class EnrollmentStatus {
    object Initial : EnrollmentStatus()
    object Loading : EnrollmentStatus()
    object Success : EnrollmentStatus()
    data class Error(val message: String) : EnrollmentStatus()
}

sealed class LockUnlockStatus {
    object Initial : LockUnlockStatus()
    object Loading : LockUnlockStatus()
    object Success : LockUnlockStatus()
    data class Error(val message: String) : LockUnlockStatus()
}

sealed class BatchUiState2 {
    object Loading : BatchUiState2()
    data class Success(val batches: List<Batch>) : BatchUiState2()
    data class Error(val message: String) : BatchUiState2()
}

@HiltViewModel
class StudentViewModel @Inject constructor(
    val apiService: ApiService,
    private val tokenManager: TokenManager
) : ViewModel() {
    private val _uiState = MutableStateFlow<StudentUiState>(StudentUiState.Loading)
    val uiState: StateFlow<StudentUiState> = _uiState

    private val _courseUiState = MutableStateFlow<CourseUiState>(CourseUiState.Loading)
    val courseUiState: StateFlow<CourseUiState> = _courseUiState

    private val _batchUiState = MutableStateFlow<BatchUiState2>(BatchUiState2.Loading)
    val batchUiState: StateFlow<BatchUiState2> = _batchUiState

    private val _selectedCourseId = MutableStateFlow<String?>(null)
    val selectedCourseId: StateFlow<String?> = _selectedCourseId

    private val _selectedStudent = MutableStateFlow<UserDTO?>(null)
    val selectedStudent: StateFlow<UserDTO?> = _selectedStudent

    private val _enrollmentStatus = MutableStateFlow<EnrollmentStatus>(EnrollmentStatus.Initial)
    val enrollmentStatus: StateFlow<EnrollmentStatus> = _enrollmentStatus
    
    private val _lockUnlockStatus = MutableStateFlow<LockUnlockStatus>(LockUnlockStatus.Initial)
    val lockUnlockStatus: StateFlow<LockUnlockStatus> = _lockUnlockStatus
    
    // Caching for performance optimization
    private var cachedStudents: List<UserAllDetails>? = null
    private var cachedCourses: List<Course>? = null
    private var cachedBatches: List<Batch>? = null
    private var lastStudentsRefresh = 0L
    private var lastCoursesRefresh = 0L
    private var lastBatchesRefresh = 0L
    private val cacheTimeout = 30000L // 30 seconds cache timeout
    
    // Cache management methods
    fun clearAllCache() {
        cachedStudents = null
        cachedCourses = null
        cachedBatches = null
        lastStudentsRefresh = 0L
        lastCoursesRefresh = 0L
        lastBatchesRefresh = 0L
    }
    
    fun refreshAllData() {
        clearAllCache()
        loadStudents(forceRefresh = true)
        loadCourses(forceRefresh = true)
        loadBatches(forceRefresh = true)
    }

    init {
        loadStudents()
        loadCourses()
        loadBatches()
    }

    fun loadBatches(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            val currentTime = System.currentTimeMillis()
            
            // Use cached data if available and not expired
            if (!forceRefresh && cachedBatches != null && (currentTime - lastBatchesRefresh) < cacheTimeout) {
                println("StudentViewModel: Using cached batches data (${cachedBatches!!.size} batches)")
                _batchUiState.value = BatchUiState2.Success(cachedBatches!!)
                return@launch
            }
            
            _batchUiState.value = BatchUiState2.Loading
            try {
                val response = apiService.getBatches()
                if (response.isSuccessful) {
                    val batches = response.body() ?: emptyList()
                    
                    // Update cache
                    cachedBatches = batches
                    lastBatchesRefresh = currentTime
                    
                    _batchUiState.value = BatchUiState2.Success(batches)
                } else {
                    _batchUiState.value = BatchUiState2.Error("Failed to load batches: ${response.message()}")
                }
            } catch (e: Exception) {
                _batchUiState.value = BatchUiState2.Error("Error loading batches: ${e.message}")
            }
        }
    }

    fun loadStudents(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            val currentTime = System.currentTimeMillis()
            
            // Use cached data if available and not expired
            if (!forceRefresh && cachedStudents != null && (currentTime - lastStudentsRefresh) < cacheTimeout) {
                println("StudentViewModel: Using cached students data (${cachedStudents!!.size} students)")
                _uiState.value = StudentUiState.Success(cachedStudents!!)
                return@launch
            }
            
            _uiState.value = StudentUiState.Loading
            try {
                println("StudentViewModel: Loading students from API...")
                val response = apiService.getAllUsers()
                if (response.isSuccessful) {
                    val students = response.body() ?: emptyList()
                    println("StudentViewModel: Received ${students.size} students from API")
                    
                    // Update cache
                    cachedStudents = students
                    lastStudentsRefresh = currentTime
                    
                    // Direct student loading without paidAmount fetching to improve performance
                    val filteredStudents = if (_selectedCourseId.value != null) {
                        students.filter { student ->
                            student.enrollments?.any { enrollment ->
                                enrollment.enrolledCourse == _selectedCourseId.value
                            } == true
                        }
                    } else {
                        students
                    }
                    println("StudentViewModel: Filtered to ${filteredStudents.size} students")
                    _uiState.value = StudentUiState.Success(filteredStudents)
                } else {
                    println("StudentViewModel: API call failed - ${response.code()}: ${response.message()}")
                    _uiState.value = StudentUiState.Error("Failed to load students: ${response.message()}")
                }
            } catch (e: Exception) {
                println("StudentViewModel: Exception in loadStudents: ${e.message}")
                _uiState.value = StudentUiState.Error("Error loading students: ${e.message}")
            }
        }
    }

    private fun loadCourses(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            val currentTime = System.currentTimeMillis()
            
            // Use cached data if available and not expired
            if (!forceRefresh && cachedCourses != null && (currentTime - lastCoursesRefresh) < cacheTimeout) {
                println("StudentViewModel: Using cached courses data (${cachedCourses!!.size} courses)")
                _courseUiState.value = CourseUiState.Success(cachedCourses!!)
                return@launch
            }
            
            _courseUiState.value = CourseUiState.Loading
            println("Loading courses...")
            try {
                println("Making API call to getCourses()")
                val response = apiService.getCourses()
                println("API Response - Code: ${response.code()}, Message: ${response.message()}")
                println("Raw Response Body: ${response.body()}")
                
                if (response.isSuccessful) {
                    val courses = response.body() ?: emptyList()
                    println("Loaded ${courses.size} courses:")
                    
                    // Update cache
                    cachedCourses = courses
                    lastCoursesRefresh = currentTime
                    courses.forEach { course ->
                        println("Course: ${course.courseName} (${course.courseId})")
                        println("  Description: ${course.courseDescription}")
                        println("  Duration: ${course.courseDuration}")
                        println("  Price: ${course.coursePrice}")
                        println("  Offer Price: ${course.courseOfferPrice}")
                        println("  Batches: ${course.batches?.size ?: 0}")
                    }
                    _courseUiState.value = CourseUiState.Success(courses)
                    println("Updated state to Success with ${courses.size} courses")
                } else {
                    println("API call failed - Code: ${response.code()}, Message: ${response.message()}")
                    println("Error Body: ${response.errorBody()?.string()}")
                    _courseUiState.value = CourseUiState.Error("Failed to load courses: ${response.message()}")
                }
            } catch (e: Exception) {
                println("Exception in loadCourses: ${e.javaClass.simpleName} - ${e.message}")
                e.printStackTrace()
                _courseUiState.value = CourseUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun deleteStudent(userId: String) {
        viewModelScope.launch {
            try {
                val response = apiService.deleteUser(userId)
                if (response.isSuccessful) {
                    // Invalidate cache and force refresh
                    cachedStudents = null
                    loadStudents(forceRefresh = true)
                } else {
                    _uiState.value = StudentUiState.Error("Failed to delete student: ${response.message()}")
                }
            } catch (e: Exception) {
                _uiState.value = StudentUiState.Error("Error deleting student: ${e.message}")
            }
        }
    }

    fun deleteCourse(courseId: String) {
        viewModelScope.launch {
            try {
                val response = apiService.deleteCourse(courseId)
                if (response.isSuccessful) {
                    loadCourses()
                } else {
                    _courseUiState.value = CourseUiState.Error("Failed to delete course: ${response.message()}")
                }
            } catch (e: Exception) {
                _courseUiState.value = CourseUiState.Error("Error deleting course: ${e.message}")
            }
        }
    }



    fun createStudent(userDTO: UserDTO, onSuccess: (String, String) -> Unit) {
        viewModelScope.launch {
            try {
                _enrollmentStatus.value = EnrollmentStatus.Loading
                println("Creating student with UserDTO: $userDTO")
                val response = apiService.createUser(userDTO)
                println("API Response - Code: ${response.code()}, Message: ${response.message()}")
                
                if (response.isSuccessful) {
                    val createdUser = response.body()
                    println("Created user response: $createdUser")
                    if (createdUser?.userId != null && createdUser.dishaId != null) {
                        println("User created successfully - UserId: ${createdUser.userId}, DishaId: ${createdUser.dishaId}")
                        onSuccess(createdUser.userId, createdUser.dishaId)
                        _enrollmentStatus.value = EnrollmentStatus.Initial // Reset status after successful creation
                    } else {
                        println("Error: Created user missing userId or dishaId")
                        _enrollmentStatus.value = EnrollmentStatus.Error("Failed to retrieve created user details")
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    println("API Error - Code: ${response.code()}, Error Body: $errorBody")
                    _enrollmentStatus.value = EnrollmentStatus.Error("Failed to create student: ${response.message()} - $errorBody")
                }
            } catch (e: Exception) {
                println("Exception in createStudent: ${e.javaClass.simpleName} - ${e.message}")
                e.printStackTrace()
                _enrollmentStatus.value = EnrollmentStatus.Error("Error creating student: ${e.message}")
            }
        }
    }

    fun enrollStudent(userId: String, courseId: String, batchId: String, paymentDto: PaymentDto) {
        viewModelScope.launch {
            try {
                _enrollmentStatus.value = EnrollmentStatus.Loading
                println("StudentViewModel: enrollStudent() called")
                println("StudentViewModel: userId=$userId, courseId=$courseId, batchId=$batchId")
                println("StudentViewModel: PaymentDto details:")
                println("  - paidAmount: ${paymentDto.paidAmount}")
                println("  - paymentMode: ${paymentDto.paymentMode}")
                println("  - paymentReference: ${paymentDto.paymentReference}")
                println("  - paymentDate: ${paymentDto.paymentDate}")
                println("  - status: ${paymentDto.status}")
                
                println("StudentViewModel: Making API call to enrollStudent...")
                val response = apiService.enrollStudent(studentId = userId, courseId = courseId, batchId = batchId, paymentDto = paymentDto)
                
                println("StudentViewModel: API Response received")
                println("  - Response code: ${response.code()}")
                println("  - Response message: ${response.message()}")
                println("  - Response successful: ${response.isSuccessful}")
                
                if (response.isSuccessful) {
                    val enrollmentResponse = response.body()
                    println("StudentViewModel: Enrollment successful!")
                    println("  - EnrollmentDTO: $enrollmentResponse")
                    if (enrollmentResponse != null) {
                        println("  - Enrollment ID: ${enrollmentResponse.enrollmentId}")
                        println("  - Final Price: ${enrollmentResponse.finalPrice}")
                        println("  - Paid Amount in response: ${enrollmentResponse.paidAmount}")
                        println("  - Payment Status: ${enrollmentResponse.paymentStatus}")
                        
                        // Verify if sent paidAmount matches received paidAmount
                        val sentPaidAmount = paymentDto.paidAmount
                        val receivedPaidAmount = enrollmentResponse.getPaidAmountAsDouble()
                        println("  - PaidAmount verification: sent=$sentPaidAmount, received=$receivedPaidAmount")
                        if (sentPaidAmount != receivedPaidAmount) {
                            println("  - WARNING: PaidAmount mismatch detected!")
                        } else {
                            println("  - SUCCESS: PaidAmount matches perfectly")
                        }
                    }
                    _enrollmentStatus.value = EnrollmentStatus.Success
                    // Invalidate cache and force refresh after enrollment
                    cachedStudents = null
                    loadStudents(forceRefresh = true) // Refresh student list after successful enrollment
                } else {
                    val errorBody = response.errorBody()?.string()
                    println("StudentViewModel: Enrollment failed")
                    println("  - Error body: $errorBody")
                    _enrollmentStatus.value = EnrollmentStatus.Error("Failed to enroll student: ${response.message()}")
                }
            } catch (e: Exception) {
                println("StudentViewModel: Exception in enrollStudent: ${e.javaClass.simpleName} - ${e.message}")
                e.printStackTrace()
                _enrollmentStatus.value = EnrollmentStatus.Error("Error enrolling student: ${e.message}")
            }
        }
    }

    fun updateStudent(userId: String, userDTO: UserDTO) {
        viewModelScope.launch {
            try {
                _enrollmentStatus.value = EnrollmentStatus.Loading
                val response = apiService.updateUser(userId, userDTO)
                if (response.isSuccessful) {
                    _enrollmentStatus.value = EnrollmentStatus.Success
                    loadStudents()
                } else {
                    _enrollmentStatus.value = EnrollmentStatus.Error("Failed to update student: ${response.message()}")
                }
            } catch (e: Exception) {
                _enrollmentStatus.value = EnrollmentStatus.Error("Error updating student: ${e.message}")
            }
        }
    }

    fun registerAndEnroll(userDTO: UserDTO, courseId: String, batchId: String, paymentDto: PaymentDto) {
        viewModelScope.launch {
            try {
                _enrollmentStatus.value = EnrollmentStatus.Loading
                val createResponse = apiService.createUser(userDTO)
                if (createResponse.isSuccessful) {
                    val createdUser = createResponse.body()
                    if (createdUser != null) {
                        val enrollResponse = apiService.enrollStudent(
                            studentId = createdUser.userId,
                            courseId = courseId,
                            batchId = batchId,
                            paymentDto = paymentDto
                        )
                        if (enrollResponse.isSuccessful) {
                            _enrollmentStatus.value = EnrollmentStatus.Success
                            loadStudents()
                        } else {
                            _enrollmentStatus.value = EnrollmentStatus.Error("Failed to enroll student: ${enrollResponse.message()}")
                        }
                    } else {
                        _enrollmentStatus.value = EnrollmentStatus.Error("Failed to retrieve created user details")
                    }
                } else {
                    _enrollmentStatus.value = EnrollmentStatus.Error("Failed to create student: ${createResponse.message()}")
                }
            } catch (e: Exception) {
                _enrollmentStatus.value = EnrollmentStatus.Error("Error during registration and enrollment: ${e.message}")
            }
        }
    }

    fun filterBySelectedCourse(courseId: String?) {
        _selectedCourseId.value = courseId
        loadStudents()
    }

    fun setEnrollmentError(message: String) {
        _enrollmentStatus.value = EnrollmentStatus.Error(message)
    }
    
    fun toggleUserLock(student: UserAllDetails) {
        val newLockStatus = !student.isLocked
        
        // Optimistic Update
        val currentList = (_uiState.value as? StudentUiState.Success)?.students
        if (currentList != null) {
            val updatedList = currentList.map { 
                if (it.userId == student.userId) it.copy(isLocked = newLockStatus) else it 
            }
            _uiState.value = StudentUiState.Success(updatedList)
        }

        viewModelScope.launch {
            try {
                _lockUnlockStatus.value = LockUnlockStatus.Loading
                
                // Get refresh token from TokenManager (Login Details)
                val refreshToken = tokenManager.refreshToken.first() ?: ""
                
                val response = if (newLockStatus) {
                    // Lock User: Send refreshToken in body
                    val request = LockUserRequest(refreshToken = refreshToken)
                    apiService.lockUser(
                        userId = student.userId,
                        lock = true,
                        request = request
                    )
                } else {
                    // Unlock User: Send NO body (to prevent 500 error)
                    apiService.unlockUser(
                        userId = student.userId,
                        lock = false
                    )
                }

                if (response.isSuccessful) {
                    _lockUnlockStatus.value = LockUnlockStatus.Success
                } else {
                    _lockUnlockStatus.value = LockUnlockStatus.Error("Failed to update lock status: ${response.message()}")
                    // Revert optimistic update
                    if (currentList != null) {
                         _uiState.value = StudentUiState.Success(currentList)
                    }
                }
            } catch (e: Exception) {
                _lockUnlockStatus.value = LockUnlockStatus.Error("Error updating lock status: ${e.message}")
                 // Revert optimistic update
                if (currentList != null) {
                     _uiState.value = StudentUiState.Success(currentList)
                }
            }
        }
    }
    


}
