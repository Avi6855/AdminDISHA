package com.avinashpatil.app.admindisha.screens.student

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.School
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.avinashpatil.app.admindisha.api.UserAllDetails
import com.avinashpatil.app.admindisha.navigation.Screen
import com.avinashpatil.app.admindisha.screens.course.CourseViewModel
import com.avinashpatil.app.admindisha.viewmodels.BatchViewModel
import com.avinashpatil.app.admindisha.viewmodels.BillViewModel
import com.avinashpatil.app.admindisha.viewmodels.StudentUiState
import com.avinashpatil.app.admindisha.viewmodels.StudentViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// Define colors from HTML
private val ColorPrimary = Color(0xFF6366f1)
private val ColorBgLight = Color(0xFFf8fafc)
private val ColorBgDark = Color(0xFF0f172a)
private val ColorSlate900 = Color(0xFF0f172a)
private val ColorSlate800 = Color(0xFF1e293b)
private val ColorSlate500 = Color(0xFF64748b)
private val GradientColors = listOf(Color(0xFF6366f1), Color(0xFFa855f7), Color(0xFF06b6d4))
private val FabGradient = listOf(Color(0xFFec4899), Color(0xFFf97316))

@Composable
fun AllStudentListScreen(
    navController: NavController,
    viewModel: StudentViewModel = hiltViewModel(),
    batchViewModel: BatchViewModel = hiltViewModel(),
    courseViewModel: CourseViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val lockUnlockStatus by viewModel.lockUnlockStatus.collectAsState()
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var showDeleteConfirmation by remember { mutableStateOf<String?>(null) }
    var showLockUnlockDialog by remember { mutableStateOf(false) }
    var studentToLockUnlock by remember { mutableStateOf<UserAllDetails?>(null) }

    // Instant loading with cached data (Reused from AllStudentScreen)
    var cachedStudents by rememberSaveable { mutableStateOf<List<UserAllDetails>>(emptyList()) }
    var isRefreshing by remember { mutableStateOf(false) }
    var lastRefreshTime by remember { mutableStateOf(0L) }
    val batchUiState by batchViewModel.uiState.collectAsState()

    val displayStudents = when {
        cachedStudents.isNotEmpty() -> cachedStudents
        uiState is StudentUiState.Success -> (uiState as StudentUiState.Success).students
        else -> emptyList()
    }

    LaunchedEffect(Unit) {
        val currentTime = System.currentTimeMillis()
        batchViewModel.fetchBatches()
        courseViewModel.fetchCourses()
        if (cachedStudents.isEmpty() || (currentTime - lastRefreshTime) > 30000) {
            if (cachedStudents.isNotEmpty()) {
                isRefreshing = true
                delay(1)
            }
            viewModel.loadStudents()
            lastRefreshTime = currentTime
        }
    }

    LaunchedEffect(uiState) {
        if (uiState is StudentUiState.Success) {
            val newStudents = (uiState as StudentUiState.Success).students
            if (newStudents != cachedStudents) {
                cachedStudents = newStudents
                isRefreshing = false
            }
        }
    }
    
    // Filtering Logic
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ColorBgLight)
    ) {
        // Fixed Top Gradient Background
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                //.height(256.dp)
                .background(Brush.linearGradient(colors = GradientColors))
        )

        Scaffold(
            containerColor = Color.Transparent,
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            floatingActionButton = {
                // Gradient FAB
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .shadow(10.dp, shape = RoundedCornerShape(16.dp), spotColor = Color(0xFFec4899))
                        .clip(RoundedCornerShape(16.dp))
                        .background(Brush.linearGradient(colors = FabGradient))
                        .clickable { navController.navigate(Screen.StepUpForm.route) },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Student",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(bottom = 100.dp)
            ) {
                // Header Section
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp, start = 24.dp, end = 24.dp, bottom = 24.dp)
                    ) {
                        // Title Row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 24.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "All Students",
                                style = TextStyle(
                                    fontSize = 36.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White,
                                    letterSpacing = (-1).sp
                                )
                            )
                            
                            // Refresh Button
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.2f))
                                    .clickable {
                                        isRefreshing = true
                                        cachedStudents = emptyList()
                                        viewModel.loadStudents()
                                        lastRefreshTime = System.currentTimeMillis()
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Refresh",
                                    tint = Color.White
                                )
                            }
                        }

                        // Glassy Search Bar
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color.White.copy(alpha = 0.7f))
                                .border(1.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search",
                                tint = ColorSlate500,
                                modifier = Modifier.padding(end = 12.dp)
                            )
                            BasicTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                textStyle = TextStyle(
                                    color = ColorSlate800,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium
                                ),
                                singleLine = true,
                                decorationBox = { innerTextField ->
                                    if (searchQuery.isEmpty()) {
                                        Text(
                                            text = "Search students...",
                                            style = TextStyle(
                                                color = ColorSlate500,
                                                fontSize = 16.sp,
                                                fontWeight = FontWeight.Medium
                                            )
                                        )
                                    }
                                    innerTextField()
                                },
                                cursorBrush = SolidColor(ColorSlate800),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }

                // Student List
                items(filteredStudents, key = { it.userId }) { student ->
                    StudentListCard(
                        student = student,
                        batches = if (batchUiState.batches.isNotEmpty()) batchUiState.batches else emptyList(),
                        onLockToggleClick = {
                            studentToLockUnlock = student
                            showLockUnlockDialog = true
                        },
                        onDeleteClick = { showDeleteConfirmation = student.userId },
                        onCardClick = { navController.navigate("${Screen.StudentDetails.route}/${student.userId}") }
                    )
                }
            }
        }
        
        // Delete Confirmation Dialog
        if (showDeleteConfirmation != null) {
            AlertDialog(
                onDismissRequest = { showDeleteConfirmation = null },
                title = { Text("Delete Student") },
                text = { Text("Are you sure you want to delete this student?") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showDeleteConfirmation?.let { userId -> viewModel.deleteStudent(userId) }
                            showDeleteConfirmation = null
                        }
                    ) { Text("Delete") }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteConfirmation = null }) { Text("Cancel") }
                }
            )
        }

        // Lock/Unlock Confirmation Dialog
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
                    ) { Text(actionText.replaceFirstChar { it.uppercase() }) }
                },
                dismissButton = {
                    TextButton(onClick = { showLockUnlockDialog = false }) { Text("Cancel") }
                }
            )
        }
    }
}

@Composable
fun StudentListCard(
    student: UserAllDetails,
    batches: List<com.avinashpatil.app.admindisha.api.Batch>,
    onLockToggleClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onCardClick: () -> Unit
) {
    val billVm: BillViewModel = hiltViewModel()
    val courseVm: CourseViewModel = hiltViewModel()
    val courses by courseVm.courses.collectAsState()
    var batchResolved by remember { mutableStateOf<String?>(null) }
    
    // Batch resolution logic
    LaunchedEffect(student, courses) {
        val firstCourseName = student.courseName ?: student.enrollments?.firstOrNull()?.enrolledCourse
        if (!firstCourseName.isNullOrBlank() && (batchResolved.isNullOrBlank())) {
            val courseId = courses.find { it.courseName == firstCourseName }?.courseId
            if (!courseId.isNullOrBlank()) {
                billVm.viewModelScope.launch {
                    try {
                        val resp = billVm.apiService.getEnrollmentByUserAndCourse(student.userId, courseId)
                        if (resp.isSuccessful) batchResolved = resp.body()?.batchName
                    } catch (_: Exception) {}
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp)
            .shadow(2.dp, RoundedCornerShape(16.dp), spotColor = Color(0xFF6366f1).copy(alpha = 0.1f))
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White.copy(alpha = 0.7f))
            .border(1.dp, Color.White.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
            .clickable { onCardClick() }
            .padding(24.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(
                        text = student.userName ?: "",
                        style = TextStyle(
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = ColorSlate800,
                            letterSpacing = (-0.5).sp
                        )
                    )
                    Text(
                        text = "ID: ${student.dishaId ?: ""}",
                        style = TextStyle(
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = ColorPrimary
                        ),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            // Course Info
            Row(
                modifier = Modifier
                    .padding(top = 24.dp, bottom = 24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                 // Determine course and batch text
                val firstEnrollment = student.enrollments?.firstOrNull()
                val courseText = student.courseName ?: firstEnrollment?.enrolledCourse
                
                if (courseText != null) {
                     val derivedBatchText = student.batchIds
                        ?.mapNotNull { id -> batches.find { it.batchId == id }?.batchNo }
                        ?.distinct()?.joinToString(", ")
                    val derivedFromMembership = batches
                        .filter { it.studentIds?.contains(student.userId) == true }
                        .map { it.batchNo }.distinct().joinToString(", ")
                        .ifEmpty { null }
                    val sanitizedAssigned = firstEnrollment?.assignedBatch?.takeIf {
                        it.isNotBlank() && !it.contains("null", true) && !it.contains("Unknown", true)
                    }
                    val batchText = student.batchName ?: student.batchNo ?: sanitizedAssigned ?: derivedBatchText ?: derivedFromMembership ?: batchResolved ?: "Unknown"

                    Icon(
                        imageVector = Icons.Outlined.School,
                        contentDescription = null,
                        tint = ColorSlate500,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = " Course: $courseText, Batch: $batchText",
                        style = TextStyle(
                            fontSize = 14.sp,
                            color = ColorSlate500
                        ),
                        modifier = Modifier.padding(start = 4.dp)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = Color(0xFF94a3b8),
                        modifier = Modifier.size(18.dp)
                    )
                     Text(
                        text = " No active enrollments",
                        style = TextStyle(
                            fontSize = 14.sp,
                            color = Color(0xFF94a3b8),
                            fontStyle = FontStyle.Italic
                        ),
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
            }

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Lock/Unlock Button
                val isLocked = student.isLocked == true
                val lockBgColor = if (isLocked) Color(0xFFfee2e2) else Color(0xFFdcfce7) // red-100 : green-100
                val lockIconColor = if (isLocked) Color(0xFFdc2626) else Color(0xFF16a34a) // red-600 : green-600
                
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(lockBgColor)
                        .clickable { onLockToggleClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isLocked) Icons.Default.Lock else Icons.Default.LockOpen,
                        contentDescription = "Lock Toggle",
                        tint = lockIconColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
                
                Spacer(modifier = Modifier.width(12.dp))

                // Delete Button
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFfee2e2)) // red-100
                        .clickable { onDeleteClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = Color(0xFFdc2626), // red-600
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
