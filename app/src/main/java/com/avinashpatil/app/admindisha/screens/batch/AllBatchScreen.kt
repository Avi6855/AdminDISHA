package com.avinashpatil.app.admindisha.screens.batch

import android.content.Context
import android.widget.Toast
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import androidx.hilt.navigation.compose.hiltViewModel
import com.avinashpatil.app.admindisha.api.Batch
import com.avinashpatil.app.admindisha.navigation.Screen
import com.avinashpatil.app.admindisha.screens.course.showToast
import com.avinashpatil.app.admindisha.viewmodels.BatchViewModel
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AllBatchScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    viewModel: BatchViewModel = hiltViewModel(),
    courseId: String? = null
) {
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var batchToDelete by remember { mutableStateOf<Batch?>(null) }
    var showEditBatchDialog by remember { mutableStateOf(false) }
    var batchToEdit by remember { mutableStateOf<Batch?>(null) }
    var editBatchNo by remember { mutableStateOf("") }
    var editBatchTiming by remember { mutableStateOf("") }
    var editBatchStartDate by remember { mutableStateOf("") }
    var editBatchEndDate by remember { mutableStateOf("") }
    val context = LocalContext.current
    var searchText by remember { mutableStateOf("") }

    LaunchedEffect(courseId) {
        if (courseId != null) {
            viewModel.loadBatches(courseId)
        } else {
            viewModel.fetchBatches()
        }
    }

    val batches by viewModel.batches.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF5F7FA))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            var showCreateBatchDialog by remember { mutableStateOf(false) }
            var newBatchNo by remember { mutableStateOf("") }
            var newBatchTiming by remember { mutableStateOf("") }
            var newBatchStartDate by remember { mutableStateOf("") }
            var newBatchEndDate by remember { mutableStateOf("") }

            // Top App Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xFF6B7280),
                                Color(0xFF4B5563)
                            )
                        )
                    )
                    .shadow(8.dp, shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp)),
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
                        text = "ALL BATCHES",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                    IconButton(
                        onClick = { showCreateBatchDialog = true },
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Batch",
                            tint = Color.White
                        )
                    }
                }
            }

            // Create Batch Dialog
            if (showCreateBatchDialog) {
                AlertDialog(
                    onDismissRequest = { showCreateBatchDialog = false },
                    title = { Text("Create New Batch") },
                    text = {
                        Column {
                            OutlinedTextField(
                                value = newBatchNo,
                                onValueChange = { newBatchNo = it },
                                label = { Text("Batch Name") },
                                modifier = Modifier.fillMaxWidth(),
                                isError = newBatchNo.isBlank(),
                                supportingText = { if (newBatchNo.isBlank()) Text("Batch name is required") }
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            var showTimePicker by remember { mutableStateOf(false) }
                            OutlinedTextField(
                                value = newBatchTiming,
                                onValueChange = { newBatchTiming = it },
                                label = { Text("Timing (HH:mm:ss)") },
                                modifier = Modifier.fillMaxWidth(),
                                isError = !isValidTime(newBatchTiming),
                                supportingText = { if (!isValidTime(newBatchTiming)) Text("Enter valid time in HH:mm:ss format") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                trailingIcon = {
                                    IconButton(onClick = { showTimePicker = true }) {
                                        Icon(Icons.Default.Schedule, contentDescription = "Select Time")
                                    }
                                },
                                readOnly = true
                            )

                            if (showTimePicker) {
                                val timePickerState = rememberTimePickerState()
                                AlertDialog(
                                    onDismissRequest = { showTimePicker = false },
                                    title = { Text("Select Time") },
                                    text = {
                                        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                                            TimePicker(state = timePickerState)
                                        }
                                    },
                                    confirmButton = {
                                        Button(onClick = {
                                            val hour = timePickerState.hour
                                            val minute = timePickerState.minute
                                            val formattedTime = String.format("%02d:%02d:00", hour, minute)
                                            newBatchTiming = formattedTime
                                            showTimePicker = false
                                        }) {
                                            Text("OK")
                                        }
                                    },
                                    dismissButton = {
                                        Button(onClick = { showTimePicker = false }) {
                                            Text("Cancel")
                                        }
                                    }
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            var showStartDatePicker by remember { mutableStateOf(false) }
                            OutlinedTextField(
                                value = newBatchStartDate,
                                onValueChange = { newBatchStartDate = it },
                                label = { Text("Start Date (YYYY-MM-DD)") },
                                modifier = Modifier.fillMaxWidth(),
                                isError = !isValidDate(newBatchStartDate),
                                supportingText = { if (!isValidDate(newBatchStartDate)) Text("Enter valid date in YYYY-MM-DD format") },
                                trailingIcon = {
                                    IconButton(onClick = { showStartDatePicker = true }) {
                                        Icon(Icons.Default.DateRange, contentDescription = "Select Date")
                                    }
                                },
                                readOnly = true
                            )

                            if (showStartDatePicker) {
                                val datePickerState = rememberDatePickerState()
                                DatePickerDialog(
                                    onDismissRequest = { showStartDatePicker = false },
                                    confirmButton = {
                                        Button(onClick = {
                                            datePickerState.selectedDateMillis?.let { millis ->
                                                val localDate = java.time.Instant.ofEpochMilli(millis)
                                                    .atZone(java.time.ZoneId.systemDefault())
                                                    .toLocalDate()
                                                newBatchStartDate = localDate.toString()
                                            }
                                            showStartDatePicker = false
                                        }) {
                                            Text("OK")
                                        }
                                    },
                                    dismissButton = {
                                        Button(onClick = { showStartDatePicker = false }) {
                                            Text("Cancel")
                                        }
                                    }
                                ) {
                                    DatePicker(state = datePickerState)
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            var showEndDatePicker by remember { mutableStateOf(false) }
                            OutlinedTextField(
                                value = newBatchEndDate,
                                onValueChange = { newBatchEndDate = it },
                                label = { Text("End Date (YYYY-MM-DD)") },
                                modifier = Modifier.fillMaxWidth(),
                                isError = !isValidDate(newBatchEndDate),
                                supportingText = { if (!isValidDate(newBatchEndDate)) Text("Enter valid date in YYYY-MM-DD format") },
                                trailingIcon = {
                                    IconButton(onClick = { showEndDatePicker = true }) {
                                        Icon(Icons.Default.DateRange, contentDescription = "Select Date")
                                    }
                                },
                                readOnly = true
                            )

                            if (showEndDatePicker) {
                                val datePickerState = rememberDatePickerState()
                                DatePickerDialog(
                                    onDismissRequest = { showEndDatePicker = false },
                                    confirmButton = {
                                        Button(onClick = {
                                            datePickerState.selectedDateMillis?.let { millis ->
                                                val localDate = java.time.Instant.ofEpochMilli(millis)
                                                    .atZone(java.time.ZoneId.systemDefault())
                                                    .toLocalDate()
                                                newBatchEndDate = localDate.toString()
                                            }
                                            showEndDatePicker = false
                                        }) {
                                            Text("OK")
                                        }
                                    },
                                    dismissButton = {
                                        Button(onClick = { showEndDatePicker = false }) {
                                            Text("Cancel")
                                        }
                                    }
                                ) {
                                    DatePicker(state = datePickerState)
                                }
                            }
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                if (validateBatchInput(newBatchNo, newBatchTiming, newBatchStartDate, newBatchEndDate, context)) {
                                    try {
                                        val timing = LocalTime.parse(newBatchTiming, DateTimeFormatter.ofPattern("HH:mm:ss"))
                                        val startDate = LocalDate.parse(newBatchStartDate)
                                        val endDate = LocalDate.parse(newBatchEndDate)

                                        if (endDate.isBefore(startDate)) {
                                            context.showToast("End date cannot be before start date")
                                            return@Button
                                        }

                                        val batch = Batch(
                                            batchId = UUID.randomUUID().toString(),
                                            courseId = courseId ?: "",
                                            batchNo = newBatchNo,
                                            timing = timing,
                                            startDate = startDate,
                                            endDate = endDate,
                                            studentIds = emptyList(),
                                            isActive = true
                                        )

                                        if (courseId != null) {
                                            viewModel.createBatch(batch)
                                            showCreateBatchDialog = false
                                            newBatchNo = ""
                                            newBatchTiming = ""
                                            newBatchStartDate = ""
                                            newBatchEndDate = ""
                                        } else {
                                            context.showToast("Course ID is required to create a batch")
                                        }
                                    } catch (e: Exception) {
                                        context.showToast("Invalid date/time format. Use HH:mm for time and YYYY-MM-DD for date")
                                    }
                                }
                            }
                        ) {
                            Text("Create")
                        }
                    },
                    dismissButton = {
                        Button(onClick = { showCreateBatchDialog = false }) {
                            Text("Cancel")
                        }
                    }
                )
            }

            // Edit Batch Dialog
            if (showEditBatchDialog && batchToEdit != null) {
                AlertDialog(
                    onDismissRequest = {
                        showEditBatchDialog = false
                        batchToEdit = null
                    },
                    title = { Text("Edit Batch") },
                    text = {
                        Column {
                            OutlinedTextField(
                                value = editBatchNo,
                                onValueChange = { editBatchNo = it },
                                label = { Text("Batch Number") },
                                modifier = Modifier.fillMaxWidth(),
                                isError = editBatchNo.isBlank(),
                                supportingText = { if (editBatchNo.isBlank()) Text("Batch number is required") }
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            var showEditTimePicker by remember { mutableStateOf(false) }
                            OutlinedTextField(
                                value = editBatchTiming,
                                onValueChange = { editBatchTiming = it },
                                label = { Text("Timing (HH:mm:ss)") },
                                modifier = Modifier.fillMaxWidth(),
                                isError = !isValidTime(editBatchTiming),
                                supportingText = { if (!isValidTime(editBatchTiming)) Text("Enter valid time in HH:mm:ss format") },
                                trailingIcon = {
                                    IconButton(onClick = { showEditTimePicker = true }) {
                                        Icon(Icons.Default.Schedule, contentDescription = "Select Time")
                                    }
                                },
                                readOnly = true
                            )

                            if (showEditTimePicker) {
                                val timePickerState = rememberTimePickerState()
                                AlertDialog(
                                    onDismissRequest = { showEditTimePicker = false },
                                    title = { Text("Select Time") },
                                    text = {
                                        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                                            TimePicker(state = timePickerState)
                                        }
                                    },
                                    confirmButton = {
                                        Button(onClick = {
                                            val hour = timePickerState.hour
                                            val minute = timePickerState.minute
                                            val formattedTime = String.format("%02d:%02d:00", hour, minute)
                                            editBatchTiming = formattedTime
                                            showEditTimePicker = false
                                        }) {
                                            Text("OK")
                                        }
                                    },
                                    dismissButton = {
                                        Button(onClick = { showEditTimePicker = false }) {
                                            Text("Cancel")
                                        }
                                    }
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            var showEditStartDatePicker by remember { mutableStateOf(false) }
                            OutlinedTextField(
                                value = editBatchStartDate,
                                onValueChange = { editBatchStartDate = it },
                                label = { Text("Start Date (YYYY-MM-DD)") },
                                modifier = Modifier.fillMaxWidth(),
                                isError = !isValidDate(editBatchStartDate),
                                supportingText = { if (!isValidDate(editBatchStartDate)) Text("Enter valid date in YYYY-MM-DD format") },
                                trailingIcon = {
                                    IconButton(onClick = { showEditStartDatePicker = true }) {
                                        Icon(Icons.Default.DateRange, contentDescription = "Select Date")
                                    }
                                },
                                readOnly = true
                            )

                            if (showEditStartDatePicker) {
                                val datePickerState = rememberDatePickerState()
                                DatePickerDialog(
                                    onDismissRequest = { showEditStartDatePicker = false },
                                    confirmButton = {
                                        Button(onClick = {
                                            datePickerState.selectedDateMillis?.let { millis ->
                                                val localDate = java.time.Instant.ofEpochMilli(millis)
                                                    .atZone(java.time.ZoneId.systemDefault())
                                                    .toLocalDate()
                                                editBatchStartDate = localDate.toString()
                                            }
                                            showEditStartDatePicker = false
                                        }) {
                                            Text("OK")
                                        }
                                    },
                                    dismissButton = {
                                        Button(onClick = { showEditStartDatePicker = false }) {
                                            Text("Cancel")
                                        }
                                    }
                                ) {
                                    DatePicker(state = datePickerState)
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            var showEditEndDatePicker by remember { mutableStateOf(false) }
                            OutlinedTextField(
                                value = editBatchEndDate,
                                onValueChange = { editBatchEndDate = it },
                                label = { Text("End Date (YYYY-MM-DD)") },
                                modifier = Modifier.fillMaxWidth(),
                                isError = !isValidDate(editBatchEndDate),
                                supportingText = { if (!isValidDate(editBatchEndDate)) Text("Enter valid date in YYYY-MM-DD format") },
                                trailingIcon = {
                                    IconButton(onClick = { showEditEndDatePicker = true }) {
                                        Icon(Icons.Default.DateRange, contentDescription = "Select Date")
                                    }
                                },
                                readOnly = true
                            )

                            if (showEditEndDatePicker) {
                                val datePickerState = rememberDatePickerState()
                                DatePickerDialog(
                                    onDismissRequest = { showEditEndDatePicker = false },
                                    confirmButton = {
                                        Button(onClick = {
                                            datePickerState.selectedDateMillis?.let { millis ->
                                                val localDate = java.time.Instant.ofEpochMilli(millis)
                                                    .atZone(java.time.ZoneId.systemDefault())
                                                    .toLocalDate()
                                                editBatchEndDate = localDate.toString()
                                            }
                                            showEditEndDatePicker = false
                                        }) {
                                            Text("OK")
                                        }
                                    },
                                    dismissButton = {
                                        Button(onClick = { showEditEndDatePicker = false }) {
                                            Text("Cancel")
                                        }
                                    }
                                ) {
                                    DatePicker(state = datePickerState)
                                }
                            }
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                if (validateBatchInput(editBatchNo, editBatchTiming, editBatchStartDate, editBatchEndDate, context)) {
                                    try {
                                        val timing = LocalTime.parse(editBatchTiming, DateTimeFormatter.ofPattern("HH:mm:ss"))
                                        val startDate = LocalDate.parse(editBatchStartDate)
                                        val endDate = LocalDate.parse(editBatchEndDate)

                                        if (endDate.isBefore(startDate)) {
                                            context.showToast("End date cannot be before start date")
                                            return@Button
                                        }

                                        val updatedBatch = batchToEdit!!.copy(
                                            batchNo = editBatchNo,
                                            timing = timing,
                                            startDate = startDate,
                                            endDate = endDate
                                        )

                                        viewModel.updateBatch(updatedBatch)
                                        showEditBatchDialog = false
                                        batchToEdit = null
                                    } catch (e: Exception) {
                                        context.showToast("Invalid date/time format. Use HH:mm for time and YYYY-MM-DD for date")
                                    }
                                }
                            }
                        ) {
                            Text("Update")
                        }
                    },
                    dismissButton = {
                        Button(
                            onClick = {
                                showEditBatchDialog = false
                                batchToEdit = null
                            }
                        ) {
                            Text("Cancel")
                        }
                    }
                )
            }

            // Delete Confirmation Dialog
            if (showDeleteConfirmDialog && batchToDelete != null) {
                AlertDialog(
                    onDismissRequest = {
                        showDeleteConfirmDialog = false
                        batchToDelete = null
                    },
                    title = { Text("Delete Batch") },
                    text = { Text("Are you sure you want to delete this batch?") },
                    confirmButton = {
                        Button(
                            onClick = {
                                viewModel.deleteBatch(batchToDelete!!)
                                showDeleteConfirmDialog = false
                                batchToDelete = null
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                        ) {
                            Text("Delete")
                        }
                    },
                    dismissButton = {
                        Button(
                            onClick = {
                                showDeleteConfirmDialog = false
                                batchToDelete = null
                            }
                        ) {
                            Text("Cancel")
                        }
                    }
                )
            }

            // Search Bar
            OutlinedTextField(
                value = searchText,
                onValueChange = {
                    searchText = it
                    viewModel.searchBatches(it)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                placeholder = { Text("Search batches...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true
            )

            // Batch List
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (error != null) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(error!!)
                }
            } else if (batches.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No batches found")
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(batches) { batch ->
                        BatchCard(
                            batch = batch,
                            onEditClick = {
                                batchToEdit = batch
                                editBatchNo = batch.batchNo
                                editBatchTiming = batch.timing?.format(DateTimeFormatter.ofPattern("HH:mm:ss")) ?: ""
                                editBatchStartDate = batch.startDate?.toString() ?: ""
                                editBatchEndDate = batch.endDate?.toString() ?: ""
                                showEditBatchDialog = true
                            },
                            onDeleteClick = {
                                batchToDelete = batch
                                showDeleteConfirmDialog = true
                            },
                            onAttendanceClick = {
                                navController.navigate(Screen.Batches.route + "/${batch.batchId}")
                            }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BatchCard(
    batch: Batch,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onAttendanceClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (expanded) 1.02f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .scale(scale)
            .clickable { expanded = !expanded },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = " ${batch.batchNo}",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E293B)
                    )
                    Text(
                        text = "⏰ ${batch.timing?.format(DateTimeFormatter.ofPattern("HH:mm")) ?: "Time not set"}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF4A6FA5)
                    )
                }
                Row {
                    IconButton(onClick = onEditClick) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit",
                            tint = Color(0xFF64748B)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Schedule,
                    contentDescription = "Timing",
                    tint = Color(0xFF4A6FA5),
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Timing: ${batch.timing?.format(DateTimeFormatter.ofPattern("HH:mm")) ?: "Not set"}",
                    fontSize = 16.sp,
                    color = Color(0xFF1E293B),
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = "Start Date",
                    tint = Color(0xFF4A6FA5),
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Start: ${batch.startDate?.format(DateTimeFormatter.ofPattern("dd-MM-yyyy")) ?: "Not set"}",
                    fontSize = 14.sp,
                    color = Color(0xFF64748B)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Event,
                    contentDescription = "End Date",
                    tint = Color(0xFF4A6FA5),
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "End: ${batch.endDate?.format(DateTimeFormatter.ofPattern("dd-MM-yyyy")) ?: "Not set"}",
                    fontSize = 14.sp,
                    color = Color(0xFF64748B)
                )
            }

            if (expanded) {
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Button(
                        onClick = onAttendanceClick,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4A6FA5))
                    ) {
                        Text("Attendance")
                    }
                    Button(
                        onClick = onDeleteClick,
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                    ) {
                        Text("Delete")
                    }
                }
            }
        }
    }
}

private fun isValidTime(time: String): Boolean {
    return try {
        // Try to parse with HH:mm:ss format first
        try {
            LocalTime.parse(time, DateTimeFormatter.ofPattern("HH:mm:ss"))
            return true
        } catch (e: DateTimeParseException) {
            // If that fails, try with HH:mm format
            LocalTime.parse(time, DateTimeFormatter.ofPattern("HH:mm"))
            return true
        }
    } catch (e: DateTimeParseException) {
        false
    }
}

private fun isValidDate(date: String): Boolean {
    return try {
        LocalDate.parse(date)
        true
    } catch (e: DateTimeParseException) {
        false
    }
}

private fun validateBatchInput(
    batchNo: String,
    timing: String,
    startDate: String,
    endDate: String,
    context: Context
): Boolean {
    when {
        batchNo.isBlank() -> {
            context.showToast("Batch number is required")
            return false
        }
        !isValidTime(timing) -> {
            context.showToast("Invalid time format. Use HH:mm:ss")
            return false
        }
        !isValidDate(startDate) -> {
            context.showToast("Invalid start date format. Use YYYY-MM-DD")
            return false
        }
        !isValidDate(endDate) -> {
            context.showToast("Invalid end date format. Use YYYY-MM-DD")
            return false
        }
    }
    return true
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun AllBatchScreenPreview() {
    val navController = rememberNavController()
    AllBatchScreen(navController = navController)
}