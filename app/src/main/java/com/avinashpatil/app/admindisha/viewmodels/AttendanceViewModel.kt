package com.avinashpatil.app.admindisha.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.avinashpatil.app.admindisha.api.ApiService
import com.avinashpatil.app.admindisha.api.AttendanceRequest
import com.avinashpatil.app.admindisha.api.BatchUser
import com.avinashpatil.app.admindisha.api.BatchUsersList
import com.avinashpatil.app.admindisha.api.UserAllDetails
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class AttendanceViewModel @Inject constructor(
    private val apiService: ApiService
) : ViewModel() {
    private val _students = MutableStateFlow<List<UserAllDetails>>(emptyList())
    val students: StateFlow<List<UserAllDetails>> = _students.asStateFlow()

    private val _batchUsers = MutableStateFlow<List<BatchUser>>(emptyList())
    val batchUsers: StateFlow<List<BatchUser>> = _batchUsers.asStateFlow()

    private val _attendanceMap = MutableStateFlow<Map<String, Boolean>>(emptyMap())
    val attendanceMap: StateFlow<Map<String, Boolean>> = _attendanceMap.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _filterStatus = MutableStateFlow("All")
    val filterStatus: StateFlow<String> = _filterStatus.asStateFlow()

    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate: StateFlow<LocalDate> = _selectedDate.asStateFlow()

    fun loadStudentsForBatch(batchId: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null

                // First try to get batch users from API
                try {
                    val response = apiService.getUsersByBatch(batchId)
                    println("API Response: ${response.isSuccessful}, Code: ${response.code()}, Message: ${response.message()}")

                    if (response.isSuccessful && response.body() != null) {
                        val usersList = response.body()!!
                        if (usersList.isNotEmpty()) {
                            // Convert UserAllDetails to BatchUser for compatibility
                            val batchUsers = usersList.map { user ->
                                BatchUser(fullName = user.userName ?: "Unknown")
                            }
                            println("Raw API Response: $usersList")
                            println("BatchUsers count: ${batchUsers.size}")
                            _batchUsers.value = batchUsers
                            println("Loaded ${batchUsers.size} batch users from API")
                        } else {
                            // API returned empty users list
                            _batchUsers.value = emptyList()
                            println("API returned empty users list")
                        }
                    } else {
                        // API call failed
                        _batchUsers.value = emptyList()
                        _error.value = "Failed to load batch users: ${response.code()} ${response.message()}"
                    }
                } catch (e: Exception) {
                    println("Exception in API call: ${e.message}")
                    e.printStackTrace()
                    // API call threw exception
                    _batchUsers.value = emptyList()
                    _error.value = "Failed to load batch users: ${e.message}"
                }

                // Also load attendance for date
                loadAttendanceForDate(batchId)

                // Also load students for the batch
                loadStudentsForBatchFromApi(batchId)
            } catch (e: Exception) {
                _error.value = "Failed to load students: ${e.message}"
                println("Exception loading batch users: ${e.message}")
                e.printStackTrace()
                // Ensure we always have at least an empty list
                _batchUsers.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun loadStudentsForBatchFromApi(batchId: String) {
        viewModelScope.launch {
            try {
                val response = apiService.getUsersByBatch(batchId)
                if (response.isSuccessful && response.body() != null) {
                    val usersList = response.body()!!
                    if (usersList.isNotEmpty()) {
                        // Use the UserAllDetails directly from the API response
                        _students.value = usersList
                        println("Loaded ${usersList.size} students from API")
                    } else {
                        _students.value = emptyList()
                        println("No users found in the response")
                    }
                } else {
                    _students.value = emptyList()
                    println("Failed to load students: ${response.code()} ${response.message()}")
                }
            } catch (e: Exception) {
                _error.value = "Failed to load students: ${e.message}"
                _students.value = emptyList()
            }
        }
    }

    fun loadBatchUsers(batchId: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null

                val response = apiService.getUsersByBatch(batchId)
                if (response.isSuccessful && response.body() != null) {
                    val usersList = response.body()!!
                    if (usersList.isNotEmpty()) {
                        // Convert UserAllDetails to BatchUser for compatibility
                        val batchUsers = usersList.map { user ->
                            BatchUser(fullName = user.userName ?: "Unknown")
                        }
                        _batchUsers.value = batchUsers
                        println("Loaded batch users successfully")
                    } else {
                        _batchUsers.value = emptyList()
                        println("No users found in the response")
                    }
                    // Also load attendance for the current date
                    loadAttendanceForDate(batchId)
                } else {
                    _batchUsers.value = emptyList()
                    _error.value = "Failed to load batch users: ${response.code()} ${response.message()}"
                }
            } catch (e: Exception) {
                _error.value = "Failed to load batch users: ${e.message}"
                _batchUsers.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadAttendanceForDate(batchId: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                val response = apiService.getBatchAttendance(batchId)
                println("Attendance API Response: ${response.isSuccessful}, Code: ${response.code()}, Message: ${response.message()}")

                // Continue processing even if response is not successful
                if (response.isSuccessful && response.body() != null) {
                    // For now, we're not processing attendance data from the batch response
                    // When the backend provides attendance data in the batch response, we can update this
                    _attendanceMap.value = emptyMap()
                    println("Attendance data not available in batch response yet")
                } else {
                    // If response is not successful, just log it but don't block the UI
                    println("Failed to load attendance: ${response.code()} ${response.message()}")
                    // Keep the existing attendance map or set to empty
                    _attendanceMap.value = emptyMap()
                }
            } catch (e: Exception) {
                _error.value = "Failed to load attendance: ${e.message}"
                println("Exception loading attendance: ${e.message}")
                e.printStackTrace()
                // Keep the existing attendance map or set to empty
                _attendanceMap.value = emptyMap()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun updateFilterStatus(status: String) {
        _filterStatus.value = status
    }

    fun updateSelectedDate(date: LocalDate) {
        _selectedDate.value = date
    }

    fun getFilteredStudents(): List<UserAllDetails> {
        val query = _searchQuery.value.lowercase()
        val status = _filterStatus.value
        val attendanceMap = _attendanceMap.value

        return _students.value.filter { student ->
            val matchesSearch = student.userName?.lowercase()?.contains(query) ?: false ||
                    student.email?.lowercase()?.contains(query) ?: false // Added safe call operator for nullable email

            val matchesFilter = when (status) {
                "Present" -> attendanceMap[student.userId] == true
                "Absent" -> attendanceMap[student.userId] == false
                else -> true
            }

            matchesSearch && matchesFilter
        }
    }

    fun filterStudents(filter: String) {
        _filterStatus.value = filter
        viewModelScope.launch {
            val filteredList = when (filter) {
                "Present" -> _students.value.filter { student -> _attendanceMap.value[student.userId] == true }
                "Absent" -> _students.value.filter { student -> _attendanceMap.value[student.userId] == false }
                else -> _students.value
            }
            _students.value = filteredList
        }
    }

    fun markAttendance(attendanceRequest: AttendanceRequest) {
        viewModelScope.launch {
            try {
                _error.value = null
                apiService.markAttendance(attendanceRequest)

                // Update local state
                val currentMap = _attendanceMap.value.toMutableMap()
                currentMap[attendanceRequest.userId] = attendanceRequest.isPresent
                _attendanceMap.value = currentMap
            } catch (e: Exception) {
                _error.value = "Failed to mark attendance: ${e.message}"
            }
        }
    }

    fun markAttendance(batchId: String, userId: String, date: LocalDate, isPresent: Boolean) {
        viewModelScope.launch {
            try {
                _error.value = null
                val request = AttendanceRequest(
                    batchId = batchId,
                    userId = userId,
                    date = date.toString(),
                    isPresent = isPresent,
                    remarks = ""
                )
                apiService.markAttendance(request)

                // Update local state
                val currentMap = _attendanceMap.value.toMutableMap()
                currentMap[userId] = isPresent
                _attendanceMap.value = currentMap
            } catch (e: Exception) {
                _error.value = "Failed to mark attendance: ${e.message}"
            }
        }
    }
}