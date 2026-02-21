package com.avinashpatil.app.admindisha.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.text.font.FontStyle
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.delay

import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.avinashpatil.app.admindisha.viewmodels.BillViewModel
import com.avinashpatil.app.admindisha.screens.course.CourseViewModel
import com.avinashpatil.app.admindisha.viewmodels.BatchViewModel
import com.avinashpatil.app.admindisha.viewmodels.StudentViewModel
import com.avinashpatil.app.admindisha.api.EnrollmentDetails
import com.avinashpatil.app.admindisha.api.PaymentDto
import com.avinashpatil.app.admindisha.viewmodels.StudentUiState
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AllInOneBillScreen(
    navController: NavController,
    userId: String,
    viewModel: BillViewModel = hiltViewModel(),
    courseViewModel: CourseViewModel = hiltViewModel(),
    batchViewModel: BatchViewModel = hiltViewModel(),
    studentViewModel: StudentViewModel = hiltViewModel(),
) {
    val billUiState by viewModel.uiState.collectAsState()
    val courses by courseViewModel.courses.collectAsState()
    val batchUiState by batchViewModel.uiState.collectAsState()
    val studentUiState by studentViewModel.uiState.collectAsState()
    
    // For showing snackbar messages
    val snackbarHostState = remember { SnackbarHostState() }
    
    // Show snackbar when message is available
    LaunchedEffect(billUiState.snackbarMessage) {
        billUiState.snackbarMessage?.let {
            snackbarHostState.showSnackbar(it)
        }
    }

    var selectedCourseId by rememberSaveable { mutableStateOf("") }
    var selectedBatchId by rememberSaveable { mutableStateOf("") }
    var userName by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var paidAmount by rememberSaveable { mutableDoubleStateOf(0.0) }
    
    // Instant loading with cached data
    var cachedStudentData by rememberSaveable { mutableStateOf<String?>(null) }
    var cachedEmail by rememberSaveable { mutableStateOf("") }
    var cachedPaidAmount by rememberSaveable { mutableDoubleStateOf(0.0) }
    var cachedUserName by rememberSaveable { mutableStateOf("") }
    var isRefreshing by remember { mutableStateOf(false) }
    var lastRefreshTime by remember { mutableStateOf(0L) }
    
    // Enhanced state management for better tracking
    var isDataLoaded by remember { mutableStateOf(false) }
    var emailSource by remember { mutableStateOf("") }
    var paidAmountSource by remember { mutableStateOf("") }
    var lastUpdatedTime by remember { mutableStateOf("") }
    
    // Display cached data immediately for instant loading
    val shouldShowCachedData = cachedStudentData == userId && (cachedUserName.isNotEmpty() || cachedEmail.isNotEmpty() || cachedPaidAmount > 0.0)

    // Enhanced debugging with better tracking
    LaunchedEffect(paidAmount) {
        println("AllInOneBillScreen: [${System.currentTimeMillis()}] paidAmount state changed to: $paidAmount (Source: $paidAmountSource)")
    }

    LaunchedEffect(email) {
        println("AllInOneBillScreen: [${System.currentTimeMillis()}] email state changed to: '$email' (Source: $emailSource)")
    }

    // Enhanced billUiState tracking
    LaunchedEffect(billUiState) {
        println("AllInOneBillScreen: [${System.currentTimeMillis()}] billUiState changed - isLoading: ${billUiState.isLoading}, error: ${billUiState.error}, studentData: ${billUiState.studentData != null}")
        if (billUiState.studentData != null) {
            println("AllInOneBillScreen: StudentData available - paidAmount: ${billUiState.studentData?.paidAmount}, email: '${billUiState.studentData?.email}'")
        }
    }
    var enrolledCourse by remember { mutableStateOf("") }
    var assignedBatch by remember { mutableStateOf("") }
    var coursePrice by remember { mutableStateOf("") }
    var courseOfferPrice by remember { mutableStateOf("") }
    var isCoursesExpanded by remember { mutableStateOf(false) }
    var isBatchesExpanded by remember { mutableStateOf(false) }
    var enrollmentDetails by remember { mutableStateOf<List<EnrollmentDetails>>(emptyList()) }

    // Instant loading: Load cached data first, then refresh in background
    LaunchedEffect(userId) {
        if (userId.isBlank()) {
            println("AllInOneBillScreen: userId is blank, skipping data load")
            return@LaunchedEffect
        }
        
        // Load cached data immediately for instant display
        if (shouldShowCachedData) {
            userName = cachedUserName
            email = cachedEmail
            paidAmount = cachedPaidAmount
            println("AllInOneBillScreen: Loaded cached data instantly - userName: $userName, email: $email, paidAmount: $paidAmount")
        }
        
        val currentTime = System.currentTimeMillis()
        // Only refresh if cache is older than 30 seconds or empty
        if (!shouldShowCachedData || (currentTime - lastRefreshTime) > 30000) {
            if (shouldShowCachedData) {
                // Background refresh without showing loading
                isRefreshing = true
                delay(1) // Minimal delay for instant display
            }
            
            println("AllInOneBillScreen: Loading student data for userId: $userId")
            println("AllInOneBillScreen: Initial paidAmount value: $paidAmount")

            // Load data from both ViewModels to ensure we have complete information
            viewModel.loadStudentData(userId)
            studentViewModel.loadStudents() // Ensure StudentViewModel has fresh data
            courseViewModel.fetchCourses() // Ensure CourseViewModel has fresh data
            lastRefreshTime = currentTime
        }
    }

    LaunchedEffect(courses) {
        if (courses.isNotEmpty() && selectedCourseId.isEmpty()) {
            selectedCourseId = courses[0].courseId
            batchViewModel.loadBatches(selectedCourseId)
        }
    }

    // Enhanced StudentViewModel data loading with proper priority system
    LaunchedEffect(studentUiState, userId) {
        if (studentUiState is StudentUiState.Success) {
            val student = (studentUiState as StudentUiState.Success).students.find { it.userId == userId }
            if (student != null) {
                println("AllInOneBillScreen: [${System.currentTimeMillis()}] StudentViewModel data found for userId: $userId")
                
                // Update enrollmentDetails
                enrollmentDetails = student.enrollments ?: emptyList()
                println("AllInOneBillScreen: Enrollment details loaded: ${enrollmentDetails.size} enrollments")

                // Update enrolled course and assigned batch display
                if (enrollmentDetails.isNotEmpty()) {
                    enrolledCourse = enrollmentDetails.joinToString(", ") { it.enrolledCourse }
                    assignedBatch = enrollmentDetails.joinToString(", ") { it.assignedBatch }
                    println("AllInOneBillScreen: Updated enrolledCourse: '$enrolledCourse', assignedBatch: '$assignedBatch'")

                    val firstEnrollment = enrollmentDetails[0]
                    courses.find { it.courseName == firstEnrollment.enrolledCourse }?.let {
                        selectedCourseId = it.courseId
                        batchViewModel.loadBatches(selectedCourseId)
                        println("AllInOneBillScreen: Updated selectedCourseId from enrollment: $selectedCourseId")
                    }
                    
                    val missingBatch = assignedBatch.isBlank() || assignedBatch.contains("null", ignoreCase = true)
                    if (missingBatch) {
                        val fallbackBatch = student.batchIds?.firstOrNull()?.let { id ->
                            batchUiState.batches.find { b -> b.batchId == id }
                        }
                        fallbackBatch?.let { b ->
                            assignedBatch = b.batchNo
                            selectedBatchId = b.batchId
                            println("AllInOneBillScreen: Applied fallback batch from batchIds: ${b.batchNo} (${b.batchId})")
                        }
                    }
                }

                // Priority 1: Update userName if empty (StudentViewModel has priority for userName)
                if (userName.isEmpty()) {
                    userName = student.userName ?: ""
                    println("AllInOneBillScreen: Set userName from StudentViewModel: '$userName'")
                }
                
                // Priority 2: Update email with fallback mechanism (StudentViewModel as secondary source)
                val studentEmail = student.email?.trim() ?: ""
                if (email.isEmpty() && studentEmail.isNotEmpty()) {
                    email = studentEmail
                    emailSource = "StudentViewModel"
                    println("AllInOneBillScreen: Set email from StudentViewModel: '$email' (Source: $emailSource)")
                } else if (studentEmail.isNotEmpty() && email != studentEmail) {
                    println("AllInOneBillScreen: Email conflict - Current: '$email', StudentViewModel: '$studentEmail' - Keeping current")
                }
                
                // Get paidAmount from StudentViewModel for comparison (but don't override BillViewModel)
                val studentPaidAmount = student.getPaidAmountAsDouble()
                println("AllInOneBillScreen: StudentViewModel paidAmount: $studentPaidAmount (Current UI: $paidAmount)")
                
                lastUpdatedTime = System.currentTimeMillis().toString()
                println("AllInOneBillScreen: StudentViewModel data processing completed at: $lastUpdatedTime")
            } else {
                println("AllInOneBillScreen: [${System.currentTimeMillis()}] No student found in StudentViewModel for userId: $userId")
            }
        } else {
            println("AllInOneBillScreen: [${System.currentTimeMillis()}] StudentViewModel state: ${studentUiState::class.simpleName}")
        }
    }

    // Effect to update selectedBatchId when batches are loaded and enrollmentDetails are available
    LaunchedEffect(batchUiState.batches, enrollmentDetails) {
        if (enrollmentDetails.isNotEmpty() && batchUiState.batches.isNotEmpty()) {
            val firstEnrollment = enrollmentDetails[0]
            batchUiState.batches.find { it.batchNo == firstEnrollment.assignedBatch }?.let {
                selectedBatchId = it.batchId
            }
        }
    }

    // Get course pricing from CourseViewModel
    LaunchedEffect(courses, selectedCourseId) {
        if (selectedCourseId.isNotEmpty()) {
            val selectedCourse = courses.find { it.courseId == selectedCourseId }
            selectedCourse?.let {
                coursePrice = it.coursePrice.toString()
                courseOfferPrice = it.courseOfferPrice.toString()
            }
        }
    }

    // Enhanced BillViewModel data loading with proper priority and validation
    LaunchedEffect(billUiState.studentData) {
        println("AllInOneBillScreen: [${System.currentTimeMillis()}] LaunchedEffect(billUiState.studentData) triggered")
        println("AllInOneBillScreen: billUiState.studentData is null: ${billUiState.studentData == null}")

        billUiState.studentData?.let { student ->
            println("AllInOneBillScreen: [${System.currentTimeMillis()}] BillViewModel studentData received")
            println("AllInOneBillScreen: BillViewModel data - paidAmount: ${student.paidAmount}, email: '${student.email}', fullName: '${student.fullName}'")

            // Priority 1: Update userName from BillViewModel if empty (BillViewModel has secondary priority)
            if (userName.isEmpty()) {
                userName = student.fullName
                println("AllInOneBillScreen: Set userName from BillViewModel: '$userName'")
            }
            
            // Priority 2: Update email with BillViewModel as PRIMARY source (highest priority)
            val billEmail = student.email?.trim() ?: ""
            if (billEmail.isNotEmpty()) {
                if (email.isEmpty() || emailSource != "BillViewModel") {
                    val previousEmail = email
                    email = billEmail
                    emailSource = "BillViewModel"
                    println("AllInOneBillScreen: Updated email from BillViewModel: '$previousEmail' -> '$email' (Source: $emailSource)")
                } else {
                    println("AllInOneBillScreen: Email already set from BillViewModel: '$email'")
                }
            } else {
                println("AllInOneBillScreen: BillViewModel email is empty, keeping current: '$email' (Source: $emailSource)")
            }

            // Priority 3: Update paidAmount from BillViewModel as PRIMARY source (database authority)
            val billPaidAmount = student.paidAmount
            println("AllInOneBillScreen: BEFORE paidAmount UPDATE - BillViewModel: $billPaidAmount, Current UI: $paidAmount, Source: $paidAmountSource")
            
            if (billPaidAmount >= 0.0) { // Accept 0.0 as valid value from database
                val previousPaidAmount = paidAmount
                paidAmount = billPaidAmount
                paidAmountSource = "BillViewModel (Database)"
                isDataLoaded = true
                lastUpdatedTime = System.currentTimeMillis().toString()
                
                println("AllInOneBillScreen: AFTER paidAmount UPDATE - Updated from $previousPaidAmount to $paidAmount (Source: $paidAmountSource)")
                println("AllInOneBillScreen: paidAmount update successful: ${paidAmount != previousPaidAmount}, isDataLoaded: $isDataLoaded")
            } else {
                println("AllInOneBillScreen: Invalid paidAmount from BillViewModel: $billPaidAmount, keeping current: $paidAmount")
            }

            // Update enrolled course and assigned batch if not already set
            if (enrolledCourse.isEmpty() && student.enrolledCourse.isNotEmpty()) {
                enrolledCourse = student.enrolledCourse.joinToString(", ") { it.enrolledCourse }
                println("AllInOneBillScreen: Updated enrolledCourse from BillViewModel: '$enrolledCourse'")
            }
            if (assignedBatch.isEmpty() && student.assignedBatch.isNotEmpty()) {
                assignedBatch = student.assignedBatch.joinToString(", ") { it.assignedBatch }
                println("AllInOneBillScreen: Updated assignedBatch from BillViewModel: '$assignedBatch'")
            }

            if (selectedCourseId.isEmpty()) {
                val courseNameFromEnrollment = student.enrolledCourse.firstOrNull()?.enrolledCourse
                courseNameFromEnrollment?.let { name ->
                    courses.find { it.courseName == name }?.let { c ->
                        selectedCourseId = c.courseId
                        batchViewModel.loadBatches(selectedCourseId)
                        println("AllInOneBillScreen: Selected course from BillViewModel enrollment: ${c.courseName} (${c.courseId})")
                    }
                }
                if (selectedCourseId.isEmpty() && courses.isNotEmpty()) {
                    selectedCourseId = courses[0].courseId
                    batchViewModel.loadBatches(selectedCourseId)
                    println("AllInOneBillScreen: Set default selectedCourseId: $selectedCourseId")
                }
            }
            if (selectedBatchId.isEmpty()) {
                val batchNoFromEnrollment = student.assignedBatch.firstOrNull()?.assignedBatch
                batchNoFromEnrollment?.let { bn ->
                    batchUiState.batches.find { it.batchNo == bn }?.let { b ->
                        selectedBatchId = b.batchId
                        println("AllInOneBillScreen: Selected batch from BillViewModel enrollment: ${b.batchNo} (${b.batchId})")
                    }
                }
                if (selectedBatchId.isEmpty()) {
                    val fallbackByIds = student.batches.firstOrNull()
                    if (!fallbackByIds.isNullOrEmpty()) {
                        selectedBatchId = fallbackByIds
                        println("AllInOneBillScreen: Selected batch from BillViewModel batches list: $selectedBatchId")
                    } else if (batchUiState.batches.isNotEmpty()) {
                        selectedBatchId = batchUiState.batches[0].batchId
                        println("AllInOneBillScreen: Set default selectedBatchId: $selectedBatchId")
                    }
                }
            }
            
            println("AllInOneBillScreen: [${System.currentTimeMillis()}] BillViewModel data processing completed")
            
            // Update cache with fresh data
            cachedStudentData = userId
            cachedUserName = userName
            cachedEmail = email
            cachedPaidAmount = paidAmount
            isRefreshing = false
        } ?: run {
            println("AllInOneBillScreen: [${System.currentTimeMillis()}] BillViewModel studentData is null")
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("विद्यार्थी माहिती")
                        if (isRefreshing) {
                            Spacer(modifier = Modifier.width(8.dp))
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "मागे जा")
                    }
                }
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Only show loading if no cached data is available
            if (billUiState.isLoading && !shouldShowCachedData) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("विद्यार्थी माहिती लोड करत आहे...")
                }
            } else if (billUiState.error != null && !shouldShowCachedData) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Error,
                        contentDescription = "त्रुटी",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "विद्यार्थ्याची नोंदणी माहिती सापडली नाही",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { viewModel.loadStudentData(userId) }
                    ) {
                        Text("पुन्हा प्रयत्न करा")
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Student Information
                    OutlinedTextField(
                        value = userName,
                        onValueChange = { userName = it },
                        label = { Text("विद्यार्थ्याचे नाव") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.tertiaryContainer,
                            unfocusedContainerColor = MaterialTheme.colorScheme.tertiaryContainer,
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Enhanced Email Field with source tracking and validation
                    OutlinedTextField(
                        value = email,
                        onValueChange = { newEmail ->
                            email = newEmail.trim()
                            emailSource = "User Input"
                            println("AllInOneBillScreen: Email updated by user: '$email' (Source: $emailSource)")
                        },
                        label = { Text("ईमेल") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Email,
                                contentDescription = "Email",
                                tint = if (email.isNotEmpty() && email.contains("@")) 
                                    MaterialTheme.colorScheme.primary 
                                else 
                                    MaterialTheme.colorScheme.error
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            focusedBorderColor = if (email.isNotEmpty() && email.contains("@")) 
                                MaterialTheme.colorScheme.primary 
                            else 
                                MaterialTheme.colorScheme.error,
                            unfocusedBorderColor = if (email.isNotEmpty() && email.contains("@")) 
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.7f) 
                            else 
                                MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                        ),
                        supportingText = {
                            if (email.isNotEmpty()) {
                                if (email.contains("@")) {
                                    Text(
                                        "✓ वैध ईमेल (स्रोत: $emailSource)",
                                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                                    )
                                } else {
                                    Text(
                                        "⚠ अवैध ईमेल फॉर्मेट",
                                        color = MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
                                    )
                                }
                            } else {
                                Text(
                                    if (billUiState.isLoading) "डेटाबेसमधून ईमेल लोड करत आहे..." else "ईमेल एंटर करा",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Display enrolled course information
                    OutlinedTextField(
                        value = enrolledCourse,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("नोंदणी केलेला कोर्स") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            unfocusedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            disabledContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Display assigned batch information
                    OutlinedTextField(
                        value = assignedBatch,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("नियुक्त केलेला बॅच") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                            unfocusedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                            disabledContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Course Dropdown
                    ExposedDropdownMenuBox(
                        expanded = isCoursesExpanded,
                        onExpandedChange = { isCoursesExpanded = it },
                    ) {
                        OutlinedTextField(
                            value = courses.find { it.courseId == selectedCourseId }?.courseName ?: "",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("नोंदणी केलेला कोर्स") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isCoursesExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(
                                    type = MenuAnchorType.PrimaryNotEditable,
                                    enabled = true
                                )
                        )
                        ExposedDropdownMenu(
                            expanded = isCoursesExpanded,
                            onDismissRequest = { isCoursesExpanded = false }
                        ) {
                            courses.forEach { course ->
                                DropdownMenuItem(
                                    text = { Text(course.courseName) },
                                    onClick = {
                                        selectedCourseId = course.courseId
                                        batchViewModel.loadBatches(course.courseId)
                                        // Reset batch selection when course changes
                                        selectedBatchId = ""
                                        isCoursesExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // Batch Dropdown
                    ExposedDropdownMenuBox(
                        expanded = isBatchesExpanded,
                        onExpandedChange = { isBatchesExpanded = it },
                    ) {
                        OutlinedTextField(
                            value = batchUiState.batches.find { it.batchId == selectedBatchId }?.batchNo ?: "",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("नियुक्त केलेला बॅच") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isBatchesExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(
                                    type = MenuAnchorType.PrimaryNotEditable,
                                    enabled = true
                                )
                        )
                        ExposedDropdownMenu(
                            expanded = isBatchesExpanded,
                            onDismissRequest = { isBatchesExpanded = false }
                        ) {
                            batchUiState.batches.forEach { batch ->
                                DropdownMenuItem(
                                    text = { Text(batch.batchNo) },
                                    onClick = {
                                        selectedBatchId = batch.batchId
                                        isBatchesExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // Price Information
                    OutlinedTextField(
                        value = coursePrice,
                        onValueChange = { coursePrice = it },
                        label = { Text("कोर्स किंमत") },
                        readOnly = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = courseOfferPrice,
                        onValueChange = { courseOfferPrice = it },
                        label = { Text("कोर्स ऑफर किंमत") },
                        readOnly = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Enhanced Database PaidAmount Display Field with comprehensive tracking
                    OutlinedTextField(
                        value = when {
                            billUiState.isLoading && !isDataLoaded -> "लोड करत आहे..."
                            paidAmount > 0.0 -> "₹${String.format(Locale.getDefault(), "%.2f", paidAmount)}"
                            paidAmount == 0.0 && isDataLoaded -> "₹0.00 (कोणतीही रक्कम भरलेली नाही)"
                            else -> "डेटा उपलब्ध नाही"
                        },
                        onValueChange = { },
                        label = { Text("डेटाबेसमधील पेड रक्कम (Live)") },
                        readOnly = true,
                        leadingIcon = {
                            if (billUiState.isLoading && !isDataLoaded) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.AccountBalance,
                                    contentDescription = "Database Amount",
                                    tint = when {
                                        paidAmount > 0.0 -> MaterialTheme.colorScheme.primary
                                        paidAmount == 0.0 && isDataLoaded -> MaterialTheme.colorScheme.secondary
                                        else -> MaterialTheme.colorScheme.error
                                    }
                                )
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = when {
                                paidAmount > 0.0 -> MaterialTheme.colorScheme.primary
                                paidAmount == 0.0 && isDataLoaded -> MaterialTheme.colorScheme.secondary
                                else -> MaterialTheme.colorScheme.error
                            },
                            unfocusedBorderColor = when {
                                paidAmount > 0.0 -> MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                                paidAmount == 0.0 && isDataLoaded -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.7f)
                                else -> MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                            },
                            focusedLabelColor = when {
                                paidAmount > 0.0 -> MaterialTheme.colorScheme.primary
                                paidAmount == 0.0 && isDataLoaded -> MaterialTheme.colorScheme.secondary
                                else -> MaterialTheme.colorScheme.error
                            },
                            unfocusedLabelColor = when {
                                paidAmount > 0.0 -> MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                                paidAmount == 0.0 && isDataLoaded -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.7f)
                                else -> MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                            },
                            disabledTextColor = when {
                                paidAmount > 0.0 -> MaterialTheme.colorScheme.primary
                                paidAmount == 0.0 && isDataLoaded -> MaterialTheme.colorScheme.secondary
                                else -> MaterialTheme.colorScheme.error
                            },
                            disabledBorderColor = when {
                                paidAmount > 0.0 -> MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                                paidAmount == 0.0 && isDataLoaded -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.7f)
                                else -> MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                            },
                            disabledLabelColor = when {
                                paidAmount > 0.0 -> MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                                paidAmount == 0.0 && isDataLoaded -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.7f)
                                else -> MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                            }
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        supportingText = {
                            Text(
                                when {
                                    billUiState.isLoading && !isDataLoaded -> "🔄 डेटाबेसमधून paidAmount लोड करत आहे..."
                                    paidAmount > 0.0 -> "✓ डेटाबेसमधून confirmed रक्कम (स्रोत: $paidAmountSource)"
                                    paidAmount == 0.0 && isDataLoaded -> "ℹ डेटाबेसमध्ये कोणतीही payment नोंद नाही (स्रोत: $paidAmountSource)"
                                    billUiState.error != null -> "❌ डेटा लोड करताना त्रुटी: ${billUiState.error}"
                                    else -> "⚠ डेटाबेसमधून paidAmount अजूनही लोड होत नाही"
                                },
                                color = when {
                                    billUiState.isLoading && !isDataLoaded -> MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                                    paidAmount > 0.0 -> MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                                    paidAmount == 0.0 && isDataLoaded -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.8f)
                                    else -> MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
                                }
                            )
                        }
                    )

                    // Enhanced Editable PaidAmount Field with validation and formatting
                    OutlinedTextField(
                        value = if (paidAmount == 0.0) "" else String.format(Locale.getDefault(), "%.2f", paidAmount),
                        onValueChange = { newValue ->
                            // Remove currency symbols and spaces for parsing
                            val cleanValue = newValue.replace("₹", "").replace(",", "").trim()
                            val parsedValue = cleanValue.toDoubleOrNull()
                            
                            if (parsedValue != null && parsedValue >= 0.0) {
                                paidAmount = parsedValue
                                paidAmountSource = "User Input (Manual)"
                                println("AllInOneBillScreen: User updated paidAmount to: $paidAmount (Source: $paidAmountSource)")
                            } else if (cleanValue.isEmpty()) {
                                paidAmount = 0.0
                                paidAmountSource = "User Input (Cleared)"
                                println("AllInOneBillScreen: User cleared paidAmount (Source: $paidAmountSource)")
                            } else {
                                println("AllInOneBillScreen: Invalid paidAmount input: '$newValue' -> '$cleanValue'")
                            }
                        },
                        label = { Text("दिलेली रक्कम (संपादन करा)") },
                        leadingIcon = {
                            Text(
                                "₹",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.tertiary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.7f),
                            focusedLabelColor = MaterialTheme.colorScheme.tertiary,
                            unfocusedLabelColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.7f)
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        supportingText = {
                            when {
                                paidAmount > 0.0 -> {
                                    Text(
                                        "✓ वैध रक्कम: ₹${String.format(Locale.getDefault(), "%.2f", paidAmount)} (स्रोत: $paidAmountSource)",
                                        color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.8f)
                                    )
                                }
                                paidAmount == 0.0 && paidAmountSource.contains("User") -> {
                                    Text(
                                        "रक्कम एंटर करा (उदा: 1000, 1500.50)",
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                    )
                                }
                                else -> {
                                    Text(
                                        "डेटाबेसमधील रक्कम बदलण्यासाठी नवीन रक्कम एंटर करा",
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                    )
                                }
                            }
                        }
                    )

                    // Display current enrollment information
                    if (enrollmentDetails.isNotEmpty()) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            ),
                            elevation = CardDefaults.cardElevation(
                                defaultElevation = 4.dp
                            ),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Info,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        "सध्याची नोंदणी माहिती",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                                HorizontalDivider(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                                Spacer(modifier = Modifier.height(12.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "विद्यार्थ्याचे नाव: $userName",
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Email,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = "ईमेल: $email",
                                            style = MaterialTheme.typography.bodyLarge,
                                            fontWeight = FontWeight.Medium,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                        if (emailSource.isNotEmpty()) {
                                            Text(
                                                text = "स्रोत: $emailSource",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f)
                                            )
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.School,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "नोंदणी केलेला कोर्स: $enrolledCourse",
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Group,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "नियुक्त केलेला बॅच: $assignedBatch",
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.AccountBalance,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = "पेड रक्कम: ₹${String.format(Locale.getDefault(), "%.2f", paidAmount)}",
                                            style = MaterialTheme.typography.bodyLarge,
                                            fontWeight = FontWeight.Medium,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                        if (paidAmountSource.isNotEmpty()) {
                                            Text(
                                                text = "स्रोत: $paidAmountSource",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f)
                                            )
                                        }
                                    }
                                }
                                if (lastUpdatedTime.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(12.dp))
                                    HorizontalDivider(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "शेवटचे अपडेट: ${java.text.SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(java.util.Date(lastUpdatedTime.toLongOrNull() ?: 0L))}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.5f),
                                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // Save Button
                    Button(
                        onClick = {
                            // Create PaymentDto for the enrollStudent API
                            val paymentDto = PaymentDto(
                                paidAmount = paidAmount,
                                paymentMode = "CASH", // Default payment mode
                                paymentReference = "Manual Update",
                                status = "PAID"
                            )
                            
                            // Launch coroutine to call the API
                            viewModel.viewModelScope.launch {
                                try {
                                    // First update student data using BillViewModel
                                    viewModel.updateStudentData(
                                        name = userName,
                                        email = email,
                                        courses = listOf(selectedCourseId),
                                        batches = listOf(selectedBatchId),
                                        totalPrice = coursePrice.toDoubleOrNull() ?: 0.0,
                                        paidAmount = paidAmount
                                    )
                                    
                                    // Then call enrollStudent API to update payment
                                    val enrollResponse = viewModel.apiService.enrollStudent(
                                        studentId = userId,
                                        courseId = selectedCourseId,
                                        batchId = selectedBatchId,
                                        paymentDto = paymentDto
                                    )
                                    
                                    if (enrollResponse.isSuccessful) {
                                        println("AllInOneBillScreen: Payment updated successfully")
                                        // Get the enrollmentDTO from the response
                                        val enrollmentDTO = enrollResponse.body()
                                        println("AllInOneBillScreen: Received enrollmentDTO: $enrollmentDTO")
                                        
                                        // Get paidAmount directly from enrollmentDTO
                                        val updatedPaidAmount = enrollmentDTO?.getPaidAmountAsDouble() ?: 0.0
                                        println("AllInOneBillScreen: Updated paidAmount directly from enrollmentDTO: $updatedPaidAmount")
                                        
                                        // Update the UI with the new paidAmount
                                        paidAmount = updatedPaidAmount
                                        
                                        // Enhanced success feedback with comprehensive information
                                        val successMessage = "✅ पेमेंट यशस्वीरित्या अपडेट झाले!\n" +
                                                "नवीन पेड रक्कम: ₹${String.format(Locale.getDefault(), "%.2f", updatedPaidAmount)}\n" +
                                                "अपडेट वेळ: ${java.text.SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(java.util.Date())}"
                                        
                                        viewModel.showSnackbar(successMessage)
                                        
                                        // Update local state immediately for better UX
                                        paidAmount = updatedPaidAmount
                                        paidAmountSource = "API Response (Updated)"
                                        isDataLoaded = true
                                        lastUpdatedTime = System.currentTimeMillis().toString()
                                        
                                        println("AllInOneBillScreen: Payment update successful - UI state updated immediately")
                                        
                                        // Force comprehensive data refresh
                                        viewModel.viewModelScope.launch {
                                            println("AllInOneBillScreen: Refreshing all data after successful payment update")
                                            viewModel.loadStudentData(userId)
                                            studentViewModel.loadStudents()
                                        }
                                    } else {
                                        val errorMessage = "❌ पेमेंट अपडेट अयशस्वी!\n" +
                                                "Error Code: ${enrollResponse.code()}\n" +
                                                "कृपया पुन्हा प्रयत्न करा"
                                        
                                        viewModel.showSnackbar(errorMessage)
                                        println("AllInOneBillScreen: [${System.currentTimeMillis()}] Failed to update payment: ${enrollResponse.code()}")
                                        println("AllInOneBillScreen: Response body: ${enrollResponse.errorBody()?.string()}")
                                    }
                                } catch (e: Exception) {
                                    val errorMessage = "❌ पेमेंट अपडेट करताना त्रुटी!\n" +
                                            "Error: ${e.message}\n" +
                                            "कृपया इंटरनेट कनेक्शन तपासा"
                                    
                                    viewModel.showSnackbar(errorMessage)
                                    println("AllInOneBillScreen: [${System.currentTimeMillis()}] Exception updating payment: ${e.message}")
                                    e.printStackTrace()
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        elevation = ButtonDefaults.buttonElevation(
                            defaultElevation = 4.dp,
                            pressedElevation = 8.dp
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Save,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "बदल जतन करा",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}
