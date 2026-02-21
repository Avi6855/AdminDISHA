package com.avinashpatil.app.admindisha.screens.student

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.avinashpatil.app.admindisha.api.UserAllDetails
import com.avinashpatil.app.admindisha.navigation.Screen
import com.avinashpatil.app.admindisha.viewmodels.StudentUiState
import com.avinashpatil.app.admindisha.viewmodels.StudentViewModel
import com.avinashpatil.app.admindisha.viewmodels.LockUnlockStatus
import com.avinashpatil.app.admindisha.viewmodels.BillViewModel
import com.avinashpatil.app.admindisha.screens.course.CourseViewModel
import kotlinx.coroutines.delay
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AllStudentScreen(
    navController: NavController,
    viewModel: StudentViewModel = hiltViewModel(),
    batchViewModel: com.avinashpatil.app.admindisha.viewmodels.BatchViewModel = hiltViewModel(),
    courseViewModel: com.avinashpatil.app.admindisha.screens.course.CourseViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val lockUnlockStatus by viewModel.lockUnlockStatus.collectAsState()
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var showDeleteConfirmation by remember { mutableStateOf<String?>(null) }
    var showLockUnlockDialog by remember { mutableStateOf(false) }
    var studentToLockUnlock by remember { mutableStateOf<UserAllDetails?>(null) }
    
    // Instant loading with cached data
    var cachedStudents by rememberSaveable { mutableStateOf<List<UserAllDetails>>(emptyList()) }
    var isRefreshing by remember { mutableStateOf(false) }
    var lastRefreshTime by remember { mutableStateOf(0L) }
    val batchUiState by batchViewModel.uiState.collectAsState()
    
    // Display cached data immediately for instant loading
    val displayStudents = when {
        cachedStudents.isNotEmpty() -> cachedStudents
        uiState is StudentUiState.Success -> (uiState as StudentUiState.Success).students
        else -> emptyList()
    }
    
    // Instant loading: Load cached data first, then refresh in background
    LaunchedEffect(Unit) {
        val currentTime = System.currentTimeMillis()
        batchViewModel.fetchBatches()
        courseViewModel.fetchCourses()
        // Only refresh if cache is older than 30 seconds or empty
        if (cachedStudents.isEmpty() || (currentTime - lastRefreshTime) > 30000) {
            if (cachedStudents.isNotEmpty()) {
                // Background refresh without showing loading
                isRefreshing = true
                delay(1) // Minimal delay for instant display
            }
            viewModel.loadStudents()
            lastRefreshTime = currentTime
        }
    }
    
    // Update cache when new data arrives
    LaunchedEffect(uiState) {
        if (uiState is StudentUiState.Success) {
            val newStudents = (uiState as StudentUiState.Success).students
            if (newStudents != cachedStudents) {
                cachedStudents = newStudents
                isRefreshing = false
            }
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = { 
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Students List")
                        if (isRefreshing) {
                            Spacer(modifier = Modifier.width(8.dp))
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = Color.White
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White
                ),
                actions = {
                    IconButton(
                        onClick = { 
                            isRefreshing = true
                            cachedStudents = emptyList() // Force refresh
                            viewModel.loadStudents()
                            lastRefreshTime = System.currentTimeMillis()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh",
                            tint = Color.White
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate(Screen.StepUpForm.route) },
                containerColor = MaterialTheme.colorScheme.secondary // Changed to secondary color
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Student", tint = Color.White)
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            TextField( // Changed from OutlinedTextField to TextField
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                placeholder = { Text("Search students...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = MaterialTheme.colorScheme.onSurface) },
                singleLine = true,
                colors = TextFieldDefaults.colors( // Added colors for BHIM style
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    disabledContainerColor = MaterialTheme.colorScheme.surface,
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                    cursorColor = MaterialTheme.colorScheme.primary,
                    focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                    unfocusedIndicatorColor = Color.Gray,
                    focusedPlaceholderColor = Color.Gray,
                    unfocusedPlaceholderColor = Color.Gray,
                    focusedLeadingIconColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedLeadingIconColor = MaterialTheme.colorScheme.onSurface
                )
            )

            // Observe lock/unlock status
            LaunchedEffect(lockUnlockStatus) {
                when (lockUnlockStatus) {
                    is LockUnlockStatus.Error -> {
                        val errorMessage = (lockUnlockStatus as LockUnlockStatus.Error).message
                        // Show error message
                        // You can use a Snackbar or Toast here
                    }
                    is LockUnlockStatus.Success -> {
                        // Refresh students list is already handled in the ViewModel
                    }
                    else -> {}
                }
            }
            
            // Instant display: Always show data if available, no loading states
            if (displayStudents.isNotEmpty()) {
                // Optimized filtering with memoization
                val filteredStudents = remember(displayStudents, searchQuery) {
                    if (searchQuery.isBlank()) {
                        displayStudents.sortedBy { it.userName }
                    } else {
                        displayStudents.filter { student ->
                            student.userName?.contains(searchQuery, ignoreCase = true) ?: false ||
                                    student.dishaId?.contains(searchQuery, ignoreCase = true) ?: false
                        }.sortedBy { it.userName ?: "" }
                    }
                }

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(
                        items = filteredStudents,
                        key = { student -> student.userId } // Optimize recomposition
                    ) { user ->
                        StudentCard(
                            student = user,
                            batches = if (batchUiState.batches.isNotEmpty()) batchUiState.batches else emptyList(),
                            onEditClick = { navController.navigate("edit_student/${user.userId}") },
                            onDeleteClick = { showDeleteConfirmation = user.userId },
                            onCardClick = { navController.navigate("${Screen.StudentDetails.route}/${user.userId}") },
                            onLockToggleClick = {
                                studentToLockUnlock = user
                                showLockUnlockDialog = true
                            }
                        )
                    }
                }
            } else {
                // Only show loading for first time or error states
                when (uiState) {
                    is StudentUiState.Loading -> {
                        if (cachedStudents.isEmpty()) {
                            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator()
                            }
                        }
                    }
                    is StudentUiState.Error -> {
                        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                            Text(
                                text = (uiState as StudentUiState.Error).message,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                    else -> {
                        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                            Text("विद्यार्थी यादी लोड करत आहे...")
                        }
                    }
                }
            }
        }

        // Student Delete Confirmation Dialog
        if (showDeleteConfirmation != null) {
            AlertDialog(
                onDismissRequest = { showDeleteConfirmation = null },
                title = { Text("Delete Student") },
                text = { Text("Are you sure you want to delete this student?") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showDeleteConfirmation?.let { userId ->
                                viewModel.deleteStudent(userId)
                            }
                            showDeleteConfirmation = null
                        }
                    ) {
                        Text("Delete")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteConfirmation = null }) {
                        Text("Cancel")
                    }
                }
            )
        }
        
        // Lock/Unlock confirmation dialog
        if (showLockUnlockDialog && studentToLockUnlock != null) {
            val isLocked = studentToLockUnlock?.isLocked ?: false
            val actionText = if (isLocked) "unlock" else "lock"
            
            AlertDialog(
                onDismissRequest = { showLockUnlockDialog = false },
                title = { Text("Confirm ${actionText.replaceFirstChar { it.uppercase() }}") },
                text = { Text("Are you sure you want to $actionText ${studentToLockUnlock?.userName ?: ""}'s account?") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            studentToLockUnlock?.let { 
                                viewModel.toggleUserLock(it)
                            }
                            showLockUnlockDialog = false
                            studentToLockUnlock = null
                        }
                    ) {
                        Text(actionText.replaceFirstChar { it.uppercase() })
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showLockUnlockDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
fun StudentCard(
    student: UserAllDetails,
    batches: List<com.avinashpatil.app.admindisha.api.Batch> = emptyList(),
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onCardClick: () -> Unit = {},
    onLockToggleClick: () -> Unit = {}
) {
    val billVm: BillViewModel = hiltViewModel()
    val courseVm: CourseViewModel = hiltViewModel()
    val courses by courseVm.courses.collectAsState()
    var batchResolved by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(student, courses) {
        val firstCourseName = student.courseName ?: student.enrollments?.firstOrNull()?.enrolledCourse
        if (!firstCourseName.isNullOrBlank() && (batchResolved.isNullOrBlank())) {
            val courseId = courses.find { it.courseName == firstCourseName }?.courseId
            if (!courseId.isNullOrBlank()) {
                billVm.viewModelScope.launch {
                    try {
                        val resp = billVm.apiService.getEnrollmentByUserAndCourse(student.userId, courseId)
                        if (resp.isSuccessful) {
                            val bn = resp.body()?.batchName
                            println("AllStudentScreen: API batch resolve for ${student.userName} -> $bn")
                            batchResolved = bn
                        } else {
                            println("AllStudentScreen: API batch resolve failed code=${resp.code()} for ${student.userId}")
                        }
                    } catch (_: Exception) {}
                }
            }
        }
    }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable { onCardClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp) // Reduced elevation
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Student name - Always visible and prominent
            Text(
                text = student.userName ?: "",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            // Student ID
            Text(
                text = "ID: ${student.dishaId ?: ""}",
                fontSize = 14.sp,
                color = Color.Gray
            )
            
            // Use BoxWithConstraints to make layout responsive
            BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                val isCompactWidth = maxWidth < 400.dp
                
                if (isCompactWidth) {
                    // Compact layout (stacked vertically)
                    Column {
                        // Student info
                        Column(modifier = Modifier.fillMaxWidth()) {
                            // Email (if available)
                            student.email?.let {
                                Text(
                                    text = "Email: $it",
                                    fontSize = 14.sp,
                                    color = Color.Gray
                                )
                            }
                            
                            // Enrollments
                            val hasEnrollment = !student.courseName.isNullOrBlank() || student.enrollments?.isNotEmpty() == true
                            if (hasEnrollment) {
                                val derivedBatchText = student.batchIds
                                    ?.mapNotNull { id -> batches.find { it.batchId == id }?.batchNo }
                                    ?.distinct()
                                    ?.joinToString(", ")
                                val derivedFromMembership = batches
                                    .filter { it.studentIds?.contains(student.userId) == true }
                                    .map { it.batchNo }
                                    .distinct()
                                    .joinToString(", ")
                                    .ifEmpty { null }
                                val firstEnrollment = student.enrollments?.firstOrNull()
                                val sanitizedAssigned = firstEnrollment?.assignedBatch?.takeIf {
                                    it.isNotBlank() && !it.contains("null", true) && !it.contains("Unknown", true)
                                }
                                val batchText = student.batchName ?: student.batchNo ?: sanitizedAssigned
                                    ?: derivedBatchText
                                    ?: derivedFromMembership
                                    ?: batchResolved
                                    ?: "Unknown"
                                val courseText = student.courseName ?: firstEnrollment?.enrolledCourse ?: "Unknown"
                                Text(
                                    text = "Course: ${courseText}, Batch: ${batchText}",
                                    fontSize = 14.sp,
                                    color = Color.Gray
                                )
                            } else {
                                Text(
                                    text = "No enrollments",
                                    fontSize = 14.sp,
                                    color = Color.Gray
                                )
                            }
                        }
                        
                        // Action buttons in a row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            horizontalArrangement = Arrangement.End
                        ) {
                            IconButton(onClick = onLockToggleClick) {
                                Icon(
                                    imageVector = if (student.isLocked == true) Icons.Default.Lock else Icons.Default.LockOpen,
                                    contentDescription = if (student.isLocked == true) "Unlock Student" else "Lock Student",
                                    tint = if (student.isLocked == true) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                                )
                            }
                            IconButton(onClick = onDeleteClick) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete")
                            }
                        }
                    }
                } else {
                    // Regular layout (side by side)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            // Email (if available)
                            student.email?.let {
                                Text(
                                    text = "Email: $it",
                                    fontSize = 14.sp,
                                    color = Color.Gray
                                )
                            }
                            
                            // Enrollments
                            val hasEnrollment = !student.courseName.isNullOrBlank() || student.enrollments?.isNotEmpty() == true
                            if (hasEnrollment) {
                                val derivedBatchText = student.batchIds
                                    ?.mapNotNull { id -> batches.find { it.batchId == id }?.batchNo }
                                    ?.distinct()
                                    ?.joinToString(", ")
                                val derivedFromMembership = batches
                                    .filter { it.studentIds?.contains(student.userId) == true }
                                    .map { it.batchNo }
                                    .distinct()
                                    .joinToString(", ")
                                    .ifEmpty { null }
                                val firstEnrollment = student.enrollments?.firstOrNull()
                                val sanitizedAssigned = firstEnrollment?.assignedBatch?.takeIf {
                                    it.isNotBlank() && !it.contains("null", true) && !it.contains("Unknown", true)
                                }
                                val batchText = student.batchName ?: student.batchNo ?: sanitizedAssigned
                                    ?: derivedBatchText
                                    ?: derivedFromMembership
                                    ?: batchResolved
                                    ?: "Unknown"
                                val courseText = student.courseName ?: firstEnrollment?.enrolledCourse ?: "Unknown"
                                Text(
                                    text = "Course: ${courseText}, Batch: ${batchText}",
                                    fontSize = 14.sp,
                                    color = Color.Gray
                                )
                            } else {
                                Text(
                                    text = "No enrollments",
                                    fontSize = 14.sp,
                                    color = Color.Gray
                                )
                            }
                        }
                        Row {
                            IconButton(onClick = onLockToggleClick) {
                                Icon(
                                    imageVector = if (student.isLocked == true) Icons.Default.Lock else Icons.Default.LockOpen,
                                    contentDescription = if (student.isLocked == true) "Unlock Student" else "Lock Student",
                                    tint = if (student.isLocked == true) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                                )
                            }
                            IconButton(onClick = onDeleteClick) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete")
                            }
                        }
                    }
                }
            }
        }
    }
}
