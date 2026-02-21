package com.avinashpatil.app.admindisha.screens.batch

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DisplayMode
import androidx.compose.material3.rememberDatePickerState
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import androidx.hilt.navigation.compose.hiltViewModel
import com.avinashpatil.app.admindisha.api.Batch
import com.avinashpatil.app.admindisha.viewmodels.BatchViewModel
import java.util.UUID
import java.time.Instant
import java.time.ZoneId

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddNewBatchScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    viewModel: BatchViewModel = hiltViewModel(),
    isEditMode: Boolean = false,
    batchId: String? = null,
    courseId: String? = null
) {
    val context = LocalContext.current
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val batches by viewModel.batches.collectAsState()

    // State variables for user inputs
    var batchNoState by remember { mutableStateOf("") }
    var batchEndDate by remember { mutableStateOf(LocalDate.now()) }
    var batchTime by remember { mutableStateOf("") }
    var batchStartDate by remember { mutableStateOf(LocalDate.now()) }

    // Load data when screen opens
    LaunchedEffect(courseId) {
        if (courseId != null) {
            viewModel.loadBatches(courseId)
        } else {
            viewModel.fetchBatches()
        }
    }

    // Load batch data if in edit mode
    LaunchedEffect(batchId, batches) {
        if (isEditMode && batchId != null) {
            val batch = batches.find { it.batchId == batchId }
            batch?.let {
                batchNoState = it.batchNo ?: ""
                batchTime = it.timing?.format(DateTimeFormatter.ofPattern("HH:mm")) ?: ""
                batchStartDate = it.startDate?.let { date ->
                    try { LocalDate.parse(date.toString(), DateTimeFormatter.ISO_DATE) }
                    catch (e: Exception) { LocalDate.now() }
                } ?: LocalDate.now()
                batchEndDate = it.endDate?.let { date ->
                    try { LocalDate.parse(date.toString(), DateTimeFormatter.ISO_DATE) }
                    catch (e: Exception) { LocalDate.now() }
                } ?: LocalDate.now()
            }
        }
    }

    // Dialog state for confirmation
    var showDialog by remember { mutableStateOf(false) }
    var dialogMessage by remember { mutableStateOf("") }

    // Date Picker States
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }
    val startDatePickerState = rememberDatePickerState(
        initialSelectedDateMillis = System.currentTimeMillis(),
        initialDisplayMode = DisplayMode.Picker
    )
    val endDatePickerState = rememberDatePickerState(
        initialSelectedDateMillis = System.currentTimeMillis(),
        initialDisplayMode = DisplayMode.Picker
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
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
                        text = if (!isEditMode) "ADD NEW BATCH" else "EDIT BATCH",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(48.dp))
                }
            }

            // Main Content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Batch Number Input
                OutlinedTextField(
                    value = batchNoState,
                    onValueChange = { batchNoState = it },
                    label = { Text("Batch Name", color = Color(0xFF64748B)) },
                    placeholder = { Text("Enter batch name...", color = Color(0xFFCBD5E1)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(1.dp, shape = RoundedCornerShape(12.dp)),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF4A6FA5),
                        unfocusedBorderColor = Color(0xFFE2E8F0),
                        cursorColor = Color(0xFF4A6FA5),
                        focusedLabelColor = Color(0xFF4A6FA5),
                        unfocusedLabelColor = Color(0xFF64748B),
                        focusedTextColor = Color(0xFF1E293B),
                        unfocusedTextColor = Color(0xFF1E293B)
                    ),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                // Batch Time Input
                OutlinedTextField(
                    value = batchTime,
                    onValueChange = { batchTime = it },
                    label = { Text("Batch Time (HH:mm)", color = Color(0xFF64748B)) },
                    placeholder = { Text("Enter time (e.g., 09:00)...", color = Color(0xFFCBD5E1)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(1.dp, shape = RoundedCornerShape(12.dp)),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF4A6FA5),
                        unfocusedBorderColor = Color(0xFFE2E8F0),
                        cursorColor = Color(0xFF4A6FA5),
                        focusedLabelColor = Color(0xFF4A6FA5),
                        unfocusedLabelColor = Color(0xFF64748B),
                        focusedTextColor = Color(0xFF1E293B),
                        unfocusedTextColor = Color(0xFF1E293B)
                    ),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                // Batch Start Date Input
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Start Date:",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF4A6FA5),
                        modifier = Modifier.padding(end = 12.dp)
                    )
                    Text(
                        text = batchStartDate.format(DateTimeFormatter.ofPattern("dd MMM yyyy")),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Normal,
                        color = Color(0xFF1E293B),
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                showStartDatePicker = true
                            }
                    )
                }

                // Batch End Date Input
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "End Date:",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF4A6FA5),
                        modifier = Modifier.padding(end = 12.dp)
                    )
                    Text(
                        text = batchEndDate.format(DateTimeFormatter.ofPattern("dd MMM yyyy")),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Normal,
                        color = Color(0xFF1E293B),
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                showEndDatePicker = true
                            }
                    )
                }

                // Buttons Section
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    if (isEditMode) {
                        GradientButton(
                            text = "Delete",
                            gradientColors = listOf(Color(0xFFEF4444), Color(0xFFDC2626)),
                            onClick = {
                                batchId?.let { id ->
                                    val batchToDelete = batches.find { it.batchId == id }
                                    if (batchToDelete != null) {
                                        viewModel.deleteBatch(batchToDelete)
                                        android.widget.Toast.makeText(context, "Batch deleted successfully", android.widget.Toast.LENGTH_SHORT).show()
                                        viewModel.fetchBatches() // Refresh data
                                        navController.popBackStack()
                                    }
                                }
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    GradientButton(
                        text = if (batchId == null) "Create Batch" else "Update Batch",
                        gradientColors = listOf(Color(0xFF4A6FA5), Color(0xFF4A6FA5)),
                        onClick = {
                            try {
                                val timing = if (batchTime.isNotBlank()) {
                                    LocalTime.parse(batchTime, DateTimeFormatter.ofPattern("HH:mm"))
                                } else {
                                    throw IllegalArgumentException("Batch time cannot be empty")
                                }
                                val batch = Batch(
                                    batchId = batchId ?: UUID.randomUUID().toString(),
                                    courseId = courseId ?: "",
                                    batchNo = batchNoState,
                                    timing = timing,
                                    startDate = batchStartDate,
                                    endDate = batchEndDate,
                                    studentIds = emptyList(),
                                    isActive = true
                                )
                                if (batchId == null) {
                                    viewModel.createBatch(batch)
                                    android.widget.Toast.makeText(context, "Batch created successfully", android.widget.Toast.LENGTH_SHORT).show()
                                } else {
                                    viewModel.updateBatch(batch)
                                    android.widget.Toast.makeText(context, "Batch updated successfully", android.widget.Toast.LENGTH_SHORT).show()
                                }
                                viewModel.fetchBatches() // Refresh data
                                navController.popBackStack()
                            } catch (e: Exception) {
                                android.widget.Toast.makeText(context, "Invalid time format. Please use HH:mm format (e.g., 09:00)", android.widget.Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )
                }

                // Show loading indicator
                if (isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }

                // Show error message
                if (error != null) {
                    android.widget.Toast.makeText(context, error ?: "An error occurred", android.widget.Toast.LENGTH_SHORT).show()
                }
            }

            // Confirmation Dialog
            if (showDialog) {
                AlertDialog(
                    onDismissRequest = { showDialog = false },
                    confirmButton = {
                        TextButton(
                            onClick = { showDialog = false },
                            colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFF4A6FA5))
                        ) {
                            Text("OK", fontWeight = FontWeight.Bold)
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = { showDialog = false },
                            colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFEF4444))
                        ) {
                            Text("Cancel", fontWeight = FontWeight.Bold)
                        }
                    },
                    title = {
                        Text(
                            "Confirmation",
                            color = Color(0xFF1E293B),
                            fontWeight = FontWeight.Bold
                        )
                    },
                    text = {
                        Text(
                            dialogMessage,
                            color = Color(0xFF64748B)
                        )
                    },
                    containerColor = Color.White,
                    shape = RoundedCornerShape(16.dp),
                    properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = true)
                )
            }

            // Start Date Picker Dialog
            if (showStartDatePicker) {
                DatePickerDialog(
                    onDismissRequest = { showStartDatePicker = false },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                startDatePickerState.selectedDateMillis?.let { millis ->
                                    batchStartDate = Instant.ofEpochMilli(millis)
                                        .atZone(ZoneId.systemDefault())
                                        .toLocalDate()
                                }
                                showStartDatePicker = false
                            },
                            colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFF4A6FA5))
                        ) {
                            Text("Confirm", fontWeight = FontWeight.Bold)
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = { showStartDatePicker = false },
                            colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFEF4444))
                        ) {
                            Text("Cancel", fontWeight = FontWeight.Bold)
                        }
                    }
                ) {
                    DatePicker(state = startDatePickerState)
                }
            }

            // End Date Picker Dialog
            if (showEndDatePicker) {
                DatePickerDialog(
                    onDismissRequest = { showEndDatePicker = false },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                endDatePickerState.selectedDateMillis?.let { millis ->
                                    batchEndDate = Instant.ofEpochMilli(millis)
                                        .atZone(ZoneId.systemDefault())
                                        .toLocalDate()
                                }
                                showEndDatePicker = false
                            },
                            colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFF4A6FA5))
                        ) {
                            Text("Confirm", fontWeight = FontWeight.Bold)
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = { showEndDatePicker = false },
                            colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFEF4444))
                        ) {
                            Text("Cancel", fontWeight = FontWeight.Bold)
                        }
                    }
                ) {
                    DatePicker(state = endDatePickerState)
                }
            }
        }
    }
}

@Composable
fun GradientButton(
    text: String,
    gradientColors: List<Color>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(48.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(
                Brush.horizontalGradient(gradientColors),
                shape = RoundedCornerShape(12.dp)
            )
            .clickable { onClick() }
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(12.dp),
                spotColor = gradientColors[0].copy(alpha = 0.3f)
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun AddNewBatchScreenPreview() {
    val navController = rememberNavController()
    AddNewBatchScreen(navController = navController)
}