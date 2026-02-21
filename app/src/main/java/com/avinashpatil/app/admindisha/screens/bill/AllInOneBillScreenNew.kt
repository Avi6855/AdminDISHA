package com.avinashpatil.app.admindisha.screens.bill

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import com.avinashpatil.app.admindisha.api.EnrollmentDTO
import com.avinashpatil.app.admindisha.api.PaymentDto
import com.avinashpatil.app.admindisha.screens.course.CourseViewModel
import com.avinashpatil.app.admindisha.viewmodels.BillViewModel
import com.avinashpatil.app.admindisha.viewmodels.BatchViewModel
import com.avinashpatil.app.admindisha.viewmodels.StudentViewModel
import com.avinashpatil.app.admindisha.viewmodels.StudentUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AllInOneBillScreenNew(
    navController: NavController,
    userId: String,
    viewModel: BillViewModel = hiltViewModel(),
    courseViewModel: CourseViewModel = hiltViewModel(),
    batchViewModel: BatchViewModel = hiltViewModel(),
    studentViewModel: StudentViewModel = hiltViewModel()
) {
    val billUiState by viewModel.uiState.collectAsState()
    val courses by courseViewModel.courses.collectAsState()
    val batchUiState by batchViewModel.uiState.collectAsState()
    val studentUiState by studentViewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(billUiState.snackbarMessage) {
        billUiState.snackbarMessage?.let { snackbarHostState.showSnackbar(it) }
    }

    var userName by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var enrolledCourse by rememberSaveable { mutableStateOf("") }
    var assignedBatch by rememberSaveable { mutableStateOf("") }
    var coursePrice by rememberSaveable { mutableStateOf("") }
    var courseOfferPrice by rememberSaveable { mutableStateOf("") }
    var paidAmount by rememberSaveable { mutableDoubleStateOf(0.0) }
    var enrollmentId by rememberSaveable { mutableStateOf("") }

    LaunchedEffect(userId) {
        if (userId.isNotBlank()) {
            viewModel.loadStudentData(userId)
            courseViewModel.fetchCourses()
            studentViewModel.loadStudents()
        }
    }

    LaunchedEffect(billUiState.studentData) {
        val data = billUiState.studentData
        if (data != null) {
            userName = data.fullName
            email = data.email
            paidAmount = data.paidAmount
            enrolledCourse = data.enrolledCourse.firstOrNull()?.enrolledCourse ?: ""
            assignedBatch = data.assignedBatch.firstOrNull()?.assignedBatch ?: assignedBatch
            val courseName = data.enrolledCourse.firstOrNull()?.enrolledCourse ?: ""
            val courseIdForEnrollment = courses.find { it.courseName == courseName }?.courseId ?: ""
            if (courseIdForEnrollment.isNotBlank()) {
                try {
                    val resp = viewModel.apiService.getEnrollmentByUserAndCourse(userId, courseIdForEnrollment)
                    if (resp.isSuccessful) {
                        val dto: EnrollmentDTO? = resp.body()
                        dto?.let {
                            println("AllInOneBillScreenNew: EnrollmentDTO fetched course=${it.courseName} batch=${it.batchName} paid=${it.paidAmount} id=${it.enrollmentId}")
                            userName = it.userName ?: userName
                            email = it.email ?: email
                            enrolledCourse = it.courseName ?: courseName
                            assignedBatch = it.batchName ?: assignedBatch
                            paidAmount = it.getPaidAmountAsDouble()
                            enrollmentId = it.enrollmentId ?: ""
                        }
                    } else {
                        println("AllInOneBillScreenNew: getEnrollmentByUserAndCourse failed code=${resp.code()} userId=$userId courseId=$courseIdForEnrollment")
                    }
                } catch (_: Exception) {}
            }
        }
    }

    LaunchedEffect(batchUiState.batches, billUiState.studentData) {
        val data = billUiState.studentData
        if (data != null && assignedBatch.isBlank() && batchUiState.batches.isNotEmpty()) {
            val batchId = data.batches.firstOrNull()
            if (!batchId.isNullOrBlank()) {
                batchUiState.batches.find { it.batchId == batchId }?.let { b ->
                    println("AllInOneBillScreenNew: Fallback mapped batchId=$batchId to batchNo=${b.batchNo}")
                    assignedBatch = b.batchNo
                }
            }
        }
    }

    LaunchedEffect(enrolledCourse, courses) {
        if (enrolledCourse.isNotBlank() && courses.isNotEmpty()) {
            val c = courses.find { it.courseName == enrolledCourse }
            if (c != null) {
                coursePrice = c.coursePrice?.toString() ?: ""
                courseOfferPrice = c.courseOfferPrice?.toString() ?: ""
                batchViewModel.loadBatches(c.courseId)
            }
        }
    }

    LaunchedEffect(userId, enrolledCourse, courses) {
        if (userId.isNotBlank() && enrolledCourse.isNotBlank() && enrollmentId.isBlank() && (assignedBatch.isBlank() || paidAmount <= 0.0)) {
            val c = courses.find { it.courseName == enrolledCourse }
            if (c != null) {
                try {
                    val resp = viewModel.apiService.getEnrollmentByUserAndCourse(userId, c.courseId)
                    if (resp.isSuccessful) {
                        val dto = resp.body()
                        if (dto != null) {
                            if (!dto.batchName.isNullOrBlank()) assignedBatch = dto.batchName
                            val amt = dto.getPaidAmountAsDouble()
                            if (amt > 0.0) paidAmount = amt
                            if (!dto.enrollmentId.isNullOrBlank()) enrollmentId = dto.enrollmentId
                        }
                    }
                } catch (_: Exception) {}
            }
        }
    }

    LaunchedEffect(studentUiState, batchUiState.batches, userId) {
        if (studentUiState is StudentUiState.Success) {
            val student = (studentUiState as StudentUiState.Success).students.find { it.userId == userId }
            if (student != null) {
                if (email.isBlank() && !student.email.isNullOrBlank()) {
                    println("AllInOneBillScreenNew: Using email from StudentVm=${student.email}")
                    email = student.email ?: ""
                }
                val missingBatch = assignedBatch.isBlank() || assignedBatch.contains("null", ignoreCase = true)
                if (missingBatch) {
                    val batchId = student.batchIds?.firstOrNull()
                    if (!batchId.isNullOrBlank() && batchUiState.batches.isNotEmpty()) {
                        batchUiState.batches.find { it.batchId == batchId }?.let { b ->
                            println("AllInOneBillScreenNew: StudentVm fallback batchId=$batchId -> batchNo=${b.batchNo}")
                            assignedBatch = b.batchNo
                        }
                    }
                }
                if ((paidAmount <= 0.0)) {
                    val amt = student.getPaidAmountAsDouble()
                    if (amt > 0.0) {
                        println("AllInOneBillScreenNew: Using paidAmount from StudentVm=$amt")
                        paidAmount = amt
                    }
                }
            }
        }
    }

    LaunchedEffect(email, userId) {
        if (email.isBlank() && userId.isNotBlank()) {
            try {
                val resp = viewModel.apiService.getUserById(userId)
                if (resp.isSuccessful) {
                    val dto = resp.body()
                    if (dto != null && dto.email.isNotBlank()) {
                        println("AllInOneBillScreenNew: Email fetched from User API=${dto.email}")
                        email = dto.email
                    }
                } else {
                    println("AllInOneBillScreenNew: getUserById failed code=${resp.code()} for userId=$userId")
                }
            } catch (_: Exception) {}
        }
    }

    LaunchedEffect(paidAmount, userId) {
        if (paidAmount <= 0.0 && userId.isNotBlank()) {
            try {
                val resp = viewModel.apiService.getPaymentSummary(userId)
                if (resp.isSuccessful) {
                    val summary = resp.body()
                    if (summary != null && summary.totalPaidAmount > 0.0) {
                        println("AllInOneBillScreenNew: Payment summary total=${summary.totalPaidAmount}")
                        paidAmount = summary.totalPaidAmount
                    }
                } else {
                    println("AllInOneBillScreenNew: getPaymentSummary failed code=${resp.code()} for userId=$userId")
                }
            } catch (_: Exception) {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Student Information") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.Start
        ) {
            Text(text = "Student Name")
            OutlinedTextField(
                value = userName,
                onValueChange = { userName = it },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = "Email")
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = "Enrolled Course")
            OutlinedTextField(
                value = enrolledCourse,
                onValueChange = { },
                modifier = Modifier.fillMaxWidth(),
                enabled = false
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = "Assigned Batch")
            OutlinedTextField(
                value = assignedBatch,
                onValueChange = { },
                modifier = Modifier.fillMaxWidth(),
                enabled = false
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = "Course Price")
            OutlinedTextField(
                value = coursePrice,
                onValueChange = { },
                modifier = Modifier.fillMaxWidth(),
                enabled = false
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = "Offer Price")
            OutlinedTextField(
                value = courseOfferPrice,
                onValueChange = { },
                modifier = Modifier.fillMaxWidth(),
                enabled = false
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = "Paid Amount")
            OutlinedTextField(
                value = paidAmount.toString(),
                onValueChange = { v ->
                    v.toDoubleOrNull()?.let { paidAmount = it }
                },
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    val paymentDto = PaymentDto(
                        paidAmount = paidAmount,
                        paymentMode = "CASH",
                        status = "PAID"
                    )
                    if (enrollmentId.isNotBlank()) {
                        viewModel.viewModelScope.launch {
                            try {
                                val res = viewModel.apiService.updatePayment(enrollmentId, paymentDto)
                                if (res.isSuccessful) {
                                    val newPaid = res.body()?.getPaidAmountAsDouble() ?: paidAmount
                                    paidAmount = newPaid
                                    viewModel.showSnackbar("पेमेंट अपडेट झाले")
                                    viewModel.loadStudentData(userId, forceRefresh = true)
                                } else {
                                    viewModel.showSnackbar("पेमेंट अपडेट अयशस्वी")
                                }
                            } catch (_: Exception) {
                                viewModel.showSnackbar("त्रुटी आली")
                            }
                        }
                    } else {
                        viewModel.showSnackbar("एनरोलमेंट माहिती उपलब्ध नाही")
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Icon(imageVector = Icons.Filled.Save, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Save Changes")
            }
        }
    }
}


