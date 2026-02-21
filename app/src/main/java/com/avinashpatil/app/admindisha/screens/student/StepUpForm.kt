package com.avinashpatil.app.admindisha.screens.student

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.avinashpatil.app.admindisha.api.Batch
import com.avinashpatil.app.admindisha.api.Course
import com.avinashpatil.app.admindisha.api.PaymentDto
import com.avinashpatil.app.admindisha.api.UserDTO
import com.avinashpatil.app.admindisha.screens.course.CourseViewModel
import com.avinashpatil.app.admindisha.viewmodels.BatchViewModel
import com.avinashpatil.app.admindisha.viewmodels.StudentViewModel
import com.avinashpatil.app.admindisha.viewmodels.EnrollmentStatus

data class FormData(
    val fullName: String = "",
    val email: String = "",
    val userId: String? = null,
    val dishaId: String? = null,
    val enrolledCourse: String = "",
    val assignedBatch: String = "",
    val paidAmount: Double = 0.0, // Default test value
    val paymentMode: String = "" // Default to Cash
)

sealed class Screen(val route: String) {
    object Step1 : Screen("step1")
    object Step2 : Screen("step2")
    object Step3 : Screen("step3")
}

fun isValidEmail(email: String): Boolean {
    val emailRegex = "^[A-Za-z](.*)([@]{1})(.{1,})(\\.)(.{1,})"
    return email.matches(emailRegex.toRegex())
}

@Composable
fun PaidAmountErrorText() {
    Text("Please enter a valid amount", color = Color.Red)
}

@Composable
fun PaymentModeErrorText() {
    Text("Please select a payment mode", color = Color.Red)
}

@Composable
fun EmailErrorText() {
    Text("Please enter a valid email", color = Color.Red)
}

@Composable
fun FullNameErrorText() {
    Text("Please enter a full name", color = Color.Red)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StepUpForm(
    navController: NavHostController,
    studentViewModel: StudentViewModel = hiltViewModel(),
    courseViewModel: CourseViewModel = hiltViewModel(),
    batchViewModel: BatchViewModel = hiltViewModel()
) {
    var formData by remember { mutableStateOf(FormData()) }
    val courses by courseViewModel.courses.collectAsState()
    val isLoading by courseViewModel.isLoading.collectAsState()
    val error by courseViewModel.error.collectAsState()

    val batchUiState by batchViewModel.uiState.collectAsState()
    val enrollmentStatus by studentViewModel.enrollmentStatus.collectAsState()
    val innerNavController = rememberNavController()
    var showEmailError by remember { mutableStateOf(false) }
    var showFullNameError by remember { mutableStateOf(false) }
    var showPaidAmountError by remember { mutableStateOf(false) }
    var showPaymentModeError by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        courseViewModel.fetchCourses()
    }

    LaunchedEffect(formData.enrolledCourse) {
        if (formData.enrolledCourse.isNotBlank()) {
            val selectedCourse = courses.find { it.courseName == formData.enrolledCourse }
            selectedCourse?.let { course ->
                batchViewModel.loadBatches(course.courseId)
            }
        }
    }

    LaunchedEffect(enrollmentStatus) {
        when (enrollmentStatus) {
            is EnrollmentStatus.Success -> navController.popBackStack()
            is EnrollmentStatus.Error -> {}
            else -> {}
        }
    }

    fun updateFormData(newData: FormData) {
        formData = newData
    }

    fun isStep1Valid(): Boolean {
        val isFullNameValid = formData.fullName.isNotBlank()
        val isEmailValid = isValidEmail(formData.email)
        showFullNameError = !isFullNameValid
        showEmailError = !isEmailValid
        return isFullNameValid && isEmailValid
    }

    fun isStep2Valid(): Boolean = formData.enrolledCourse.isNotBlank() && formData.assignedBatch.isNotBlank()

    fun isStep3Valid(): Boolean {
        val isPaidAmountValid = formData.paidAmount > 0.0
        val isPaymentModeValid = formData.paymentMode.isNotBlank()
        showPaidAmountError = !isPaidAmountValid
        showPaymentModeError = !isPaymentModeValid
        return isPaidAmountValid && isPaymentModeValid
    }

    fun submitStep1() {
        if (isStep1Valid()) {
            val userDTO = UserDTO(
                fullName = formData.fullName,
                email = formData.email,
                role = "STUDENT", // Default role for student registration
                isLocked = false // Default to unlocked
            )
            studentViewModel.createStudent(userDTO) { userId, dishaId ->
                updateFormData(formData.copy(userId = userId, dishaId = dishaId))
                innerNavController.navigate(Screen.Step2.route)
            }
        }
    }

    fun submitForm() {
        println("StepUpForm: submitForm() called")
        println("StepUpForm: FormData validation:")
        println("  - fullName: ${formData.fullName}")
        println("  - email: ${formData.email}")
        println("  - userId: ${formData.userId}")
        println("  - enrolledCourse: ${formData.enrolledCourse}")
        println("  - assignedBatch: ${formData.assignedBatch}")
        println("  - paidAmount: ${formData.paidAmount}")
        println("  - paymentMode: ${formData.paymentMode}")
        
        val userId = formData.userId
        if (userId.isNullOrBlank()) {
            println("StepUpForm: ERROR - User ID is missing")
            studentViewModel.setEnrollmentError("User ID is missing")
            return
        }
        
        val paidAmount = formData.paidAmount
        if (paidAmount <= 0.0) {
            println("StepUpForm: ERROR - Invalid payment amount: $paidAmount")
            studentViewModel.setEnrollmentError("Invalid payment amount")
            return
        }

        if (formData.paymentMode.isBlank()) {
            println("StepUpForm: ERROR - Payment mode is required")
            studentViewModel.setEnrollmentError("Payment mode is required")
            return
        }
        
        println("StepUpForm: Finding course by name: ${formData.enrolledCourse}")
        val course = courses.find { it.courseName == formData.enrolledCourse }
        if (course == null) {
            println("StepUpForm: ERROR - Selected course not found: ${formData.enrolledCourse}")
            println("StepUpForm: Available courses: ${courses.map { it.courseName }}")
            studentViewModel.setEnrollmentError("Selected course not found")
            return
        }
        println("StepUpForm: Course found - ID: ${course.courseId}, Name: ${course.courseName}")
        
        println("StepUpForm: Finding batch by number: ${formData.assignedBatch}")
        val batch = batchUiState.batches.find { it.batchNo == formData.assignedBatch }
        if (batch == null) {
            println("StepUpForm: ERROR - Selected batch not found: ${formData.assignedBatch}")
            println("StepUpForm: Available batches: ${batchUiState.batches.map { it.batchNo }}")
            studentViewModel.setEnrollmentError("Selected batch not found or batches are still loading")
            return
        }
        println("StepUpForm: Batch found - ID: ${batch.batchId}, Number: ${batch.batchNo}")

        println("StepUpForm: Creating PaymentDto...")
        println("  - paidAmount: $paidAmount (type: ${paidAmount::class.simpleName})")
        println("  - paymentMode: ${formData.paymentMode}")
        println("  - status: PENDING")
        
        val paymentDto = PaymentDto(
            paidAmount = paidAmount,
            paymentMode = formData.paymentMode,
            status = "PENDING"
        )
        
        println("StepUpForm: PaymentDto created successfully:")
        println("  - PaymentDto object: $paymentDto")
        println("  - paidAmount field: ${paymentDto.paidAmount}")
        println("  - paymentMode field: ${paymentDto.paymentMode}")
        println("  - status field: ${paymentDto.status}")
        
        println("StepUpForm: Calling enrollStudent API...")
        println("  - userId: $userId")
        println("  - courseId: ${course.courseId}")
        println("  - batchId: ${batch.batchId}")
        println("  - paymentDto: $paymentDto")
        
        studentViewModel.enrollStudent(userId = userId, courseId = course.courseId, batchId = batch.batchId, paymentDto = paymentDto)
        println("StepUpForm: enrollStudent call completed")
    }

    Scaffold(
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(Color(0xFF6B7280), Color(0xFF4B5563))
                        )
                    )
                    .shadow(
                        8.dp,
                        shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(
                        onClick = { navController.popBackStack() },
                        modifier = Modifier.padding(start = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                    Text(
                        text = "STUDENT REGISTRATION",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(48.dp))
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = innerNavController,
            startDestination = Screen.Step1.route,
            modifier = Modifier.padding(padding)
        ) {
            composable(Screen.Step1.route) {
                Step1(
                    formData = formData,
                    showEmailError = showEmailError,
                    showFullNameError = showFullNameError,
                    enrollmentStatus = enrollmentStatus,
                    onUpdateFormData = ::updateFormData,
                    onSubmit = ::submitStep1
                )
            }
            composable(Screen.Step2.route) {
                Step2(
                    formData = formData,
                    courses = courses,
                    isLoading = isLoading,
                    error = error,
                    batches = batchUiState.batches,
                    onUpdateFormData = ::updateFormData,
                    onNext = { innerNavController.navigate(Screen.Step3.route) },
                    onBack = { innerNavController.popBackStack() }
                )
            }
            composable(Screen.Step3.route) {
                Step3(
                    formData = formData,
                    showPaidAmountError = showPaidAmountError,
                    showPaymentModeError = showPaymentModeError,
                    enrollmentStatus = enrollmentStatus,
                    onUpdateFormData = ::updateFormData,
                    onSubmit = ::submitForm,
                    onBack = { innerNavController.popBackStack() }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourseDropdown(
    courses: List<Course>,
    isLoading: Boolean,
    selectedCourse: String,
    onCourseSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { if (!isLoading) expanded = it }
    ) {
        OutlinedTextField(
            value = selectedCourse,
            onValueChange = {},
            readOnly = true,
            label = { Text("Select Course") },
            trailingIcon = {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                } else {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                }
            },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )

        if (!isLoading && courses.isNotEmpty()) {
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                courses.forEach { course ->
                    DropdownMenuItem(
                        text = { Text(course.courseName) },
                        onClick = {
                            onCourseSelected(course.courseName)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BatchDropdown(
    batches: List<Batch>,
    selectedBatch: String,
    onBatchSelected: (String) -> Unit,
    isLoading: Boolean,
    enabled: Boolean
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { if (enabled && !isLoading) expanded = it }
    ) {
        OutlinedTextField(
            value = if (selectedBatch.isBlank() && batches.isNotEmpty()) "Select Batch" else selectedBatch,
            onValueChange = {},
            readOnly = true,
            enabled = enabled && batches.isNotEmpty(),
            label = { Text("Select Batch") },
            trailingIcon = {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                } else {
                    Icon(
                        if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = "Toggle batch dropdown"
                    )
                }
            },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )

        if (!isLoading && enabled) {
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                if (batches.isEmpty()) {
                    DropdownMenuItem(
                        text = { Text("No batches available") },
                        onClick = { expanded = false }
                    )
                } else {
                    batches.forEach { batch ->
                        DropdownMenuItem(
                            text = { 
                                Text(
                                    "${batch.batchNo} (${batch.timing} - ${batch.endDate})",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            },
                            onClick = {
                                onBatchSelected(batch.batchNo)
                                expanded = false
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun Step2(
    formData: FormData,
    courses: List<Course>,
    isLoading: Boolean,
    error: String?,
    batches: List<Batch>,
    onUpdateFormData: (FormData) -> Unit,
    onNext: () -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Step 2: Course Selection",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )

        if (error != null) {
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        CourseDropdown(
            courses = courses,
            isLoading = isLoading,
            selectedCourse = formData.enrolledCourse,
            onCourseSelected = { courseName ->
                onUpdateFormData(formData.copy(enrolledCourse = courseName, assignedBatch = ""))
            }
        )

        if (formData.enrolledCourse.isNotBlank()) {
            BatchDropdown(
                batches = batches,
                selectedBatch = formData.assignedBatch,
                onBatchSelected = { batchNo ->
                    onUpdateFormData(formData.copy(assignedBatch = batchNo))
                },
                isLoading = isLoading,
                enabled = formData.enrolledCourse.isNotBlank()
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(
                onClick = onBack,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary
                )
            ) {
                Text("Back")
            }

            Button(
                onClick = onNext,
                enabled = formData.enrolledCourse.isNotBlank() && formData.assignedBatch.isNotBlank()
            ) {
                Text("Next")
            }
        }
    }
}

@Composable
fun Step1(
    formData: FormData,
    showEmailError: Boolean,
    showFullNameError: Boolean,
    enrollmentStatus: EnrollmentStatus,
    onUpdateFormData: (FormData) -> Unit,
    onSubmit: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Step 1: Personal Information",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )

        OutlinedTextField(
            value = formData.fullName,
            onValueChange = { onUpdateFormData(formData.copy(fullName = it)) },
            label = { Text("Full Name") },
            modifier = Modifier.fillMaxWidth(),
            isError = showFullNameError,
            supportingText = { if (showFullNameError) FullNameErrorText() }
        )

        OutlinedTextField(
            value = formData.email,
            onValueChange = { onUpdateFormData(formData.copy(email = it)) },
            label = { Text("Email") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth(),
            isError = showEmailError,
            supportingText = { if (showEmailError) EmailErrorText() }
        )

        Button(
            onClick = onSubmit,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            enabled = enrollmentStatus !is EnrollmentStatus.Loading
        ) {
            if (enrollmentStatus is EnrollmentStatus.Loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = Color.White
                )
            } else {
                Text("Next")
            }
        }

        when (enrollmentStatus) {
            is EnrollmentStatus.Error -> {
                Text(
                    text = (enrollmentStatus as EnrollmentStatus.Error).message,
                    color = Color.Red,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            else -> {}
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Step3(
    formData: FormData,
    showPaidAmountError: Boolean,
    showPaymentModeError: Boolean,
    enrollmentStatus: EnrollmentStatus,
    onUpdateFormData: (FormData) -> Unit,
    onSubmit: () -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Step 3: Payment Information",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )

        OutlinedTextField(
            value = if (formData.paidAmount == 0.0) "" else formData.paidAmount.toString(),
            onValueChange = { newValue ->
                val doubleValue = if (newValue.isEmpty()) 0.0 else newValue.toDoubleOrNull() ?: 0.0
                onUpdateFormData(formData.copy(paidAmount = doubleValue))
            },
            label = { Text("Paid Amount") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            isError = showPaidAmountError,
            supportingText = { if (showPaidAmountError) PaidAmountErrorText() }
        )

        var expanded by remember { mutableStateOf(false) }
        val paymentModes = listOf("CASH", "ONLINE")

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it }
        ) {
            OutlinedTextField(
                value = formData.paymentMode,
                onValueChange = {},
                readOnly = true,
                label = { Text("Payment Mode") },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth(),
                isError = showPaymentModeError,
                supportingText = { if (showPaymentModeError) PaymentModeErrorText() }
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                paymentModes.forEach { mode ->
                    DropdownMenuItem(
                        text = { Text(mode) },
                        onClick = {
                            onUpdateFormData(formData.copy(paymentMode = mode))
                            expanded = false
                        }
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(
                onClick = onBack,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary
                )
            ) {
                Text("Back")
            }

            Button(
                onClick = onSubmit,
                enabled = enrollmentStatus !is EnrollmentStatus.Loading
            ) {
                if (enrollmentStatus is EnrollmentStatus.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White
                    )
                } else {
                    Text("Submit")
                }
            }
        }

        when (enrollmentStatus) {
            is EnrollmentStatus.Error -> {
                Text(
                    text = (enrollmentStatus as EnrollmentStatus.Error).message,
                    color = Color.Red,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            else -> {}
        }
    }
}