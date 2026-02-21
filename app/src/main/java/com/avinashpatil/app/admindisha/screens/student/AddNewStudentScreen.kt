package com.avinashpatil.app.admindisha.screens.student

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddNewStudentScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    studentId: String? = null
) {
    val context = LocalContext.current
    val isEditMode = studentId != null

    // State variables for user inputs
    var studentName by remember { mutableStateOf("") }
    var studentEmail by remember { mutableStateOf("") }
    var selectedCourse by remember { mutableStateOf("") }
    var selectedBatch by remember { mutableStateOf("") }
    var givenPrice by remember { mutableStateOf("") }
    var coursePrice by remember { mutableStateOf("") }
    var remainingPrice by remember { mutableStateOf("") }

    // Load student data if in edit mode
    LaunchedEffect(studentId) {
        if (isEditMode && studentId != null) {
            // TODO: Load student data using studentId
            // viewModel.getStudentById(studentId)
        }
    }
    // Courses and Batches data (mocked for demonstration)
    val courses = listOf("Android Development", "iOS Development", "Web Development", "Data Science")
    val batches = remember(selectedCourse) {
        when (selectedCourse) {
            "Android Development" -> listOf("Batch A", "Batch B")
            "iOS Development" -> listOf("Batch C", "Batch D")
            "Web Development" -> listOf("Batch E", "Batch F")
            "Data Science" -> listOf("Batch G", "Batch H")
            else -> emptyList()
        }
    }
    // Mock API call to fetch course price
    fun fetchCoursePrice(course: String): String {
        return when (course) {
            "Android Development" -> "₹10,000"
            "iOS Development" -> "₹12,000"
            "Web Development" -> "₹8,000"
            "Data Science" -> "₹15,000"
            else -> ""
        }
    }
    // Dropdown state variables
    var expandedCourse by remember { mutableStateOf(false) }
    var expandedBatch by remember { mutableStateOf(false) }
    // Dialog state for confirmation
    var showDialog by remember { mutableStateOf(false) }
    var showDISHAID by remember { mutableStateOf(false) }
    val dishaID = remember { UUID.randomUUID().toString().substring(0, 8).uppercase() } // Auto-generated DISHA ID
    // Calculate Remaining Price
    LaunchedEffect(givenPrice, coursePrice) {
        try {
            val coursePriceValue = coursePrice.replace("₹", "").replace(",", "").toIntOrNull() ?: 0
            val givenPriceValue = givenPrice.toIntOrNull() ?: 0
            remainingPrice = "₹${(coursePriceValue - givenPriceValue).toString()}"
        } catch (e: Exception) {
            remainingPrice = "₹0"
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            // Top App Bar - updated to match course screen style
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
                        text = "ADD NEW STUDENT",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(48.dp)) // Balance the layout
                }
            }

            // Main Content (kept exactly the same as original)
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Student Name Input
                OutlinedTextField(
                    value = studentName,
                    onValueChange = { studentName = it },
                    label = { Text("Student Name", color = Color(0xFF64748B)) },
                    placeholder = { Text("Enter student name...", color = Color(0xFFCBD5E1)) },
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
                // Student Email Input
                OutlinedTextField(
                    value = studentEmail,
                    onValueChange = { studentEmail = it },
                    label = { Text("Student Email", color = Color(0xFF64748B)) },
                    placeholder = { Text("Enter student email...", color = Color(0xFFCBD5E1)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
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
                // Select Course ComboBox
                ExposedDropdownMenuBox(
                    expanded = expandedCourse,
                    onExpandedChange = { expandedCourse = !expandedCourse },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = selectedCourse,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Select Course", color = Color(0xFF64748B)) },
                        trailingIcon = { Icon(Icons.Default.ArrowDropDown, contentDescription = null) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                            .shadow(1.dp, shape = RoundedCornerShape(12.dp)),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF4A6FA5),
                            unfocusedBorderColor = Color(0xFFE2E8F0),
                            focusedLabelColor = Color(0xFF4A6FA5),
                            unfocusedLabelColor = Color(0xFF64748B)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                    DropdownMenu(
                        expanded = expandedCourse,
                        onDismissRequest = { expandedCourse = false },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        courses.forEach { course ->
                            DropdownMenuItem(
                                text = { Text(course, color = Color(0xFF1E293B)) },
                                onClick = {
                                    selectedCourse = course
                                    coursePrice = fetchCoursePrice(course) // Fetch course price when course is selected
                                    expandedCourse = false
                                }
                            )
                        }
                    }
                }
                // Select Batch ComboBox
                if (selectedCourse.isNotEmpty()) {
                    ExposedDropdownMenuBox(
                        expanded = expandedBatch,
                        onExpandedChange = { expandedBatch = !expandedBatch },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = selectedBatch,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Select Batch", color = Color(0xFF64748B)) },
                            trailingIcon = { Icon(Icons.Default.ArrowDropDown, contentDescription = null) },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                                .shadow(1.dp, shape = RoundedCornerShape(12.dp)),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF4A6FA5),
                                unfocusedBorderColor = Color(0xFFE2E8F0),
                                focusedLabelColor = Color(0xFF4A6FA5),
                                unfocusedLabelColor = Color(0xFF64748B)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                        DropdownMenu(
                            expanded = expandedBatch,
                            onDismissRequest = { expandedBatch = false },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            batches.forEach { batch ->
                                DropdownMenuItem(
                                    text = { Text(batch, color = Color(0xFF1E293B)) },
                                    onClick = {
                                        selectedBatch = batch
                                        expandedBatch = false
                                    }
                                )
                            }
                        }
                    }
                }
                // Course Price Field
                if (coursePrice.isNotEmpty()) {
                    OutlinedTextField(
                        value = coursePrice,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Course Price", color = Color(0xFF64748B)) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(1.dp, shape = RoundedCornerShape(12.dp)),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF4A6FA5),
                            unfocusedBorderColor = Color(0xFFE2E8F0),
                            focusedLabelColor = Color(0xFF4A6FA5),
                            unfocusedLabelColor = Color(0xFF64748B)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
                // Given Price Field
                OutlinedTextField(
                    value = givenPrice,
                    onValueChange = { givenPrice = it },
                    label = { Text("Given Price", color = Color(0xFF64748B)) },
                    placeholder = { Text("Enter paid amount...", color = Color(0xFFCBD5E1)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
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
                // Remaining Price Field
                if (coursePrice.isNotEmpty()) {
                    OutlinedTextField(
                        value = remainingPrice,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Remaining Price", color = Color(0xFF64748B)) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(1.dp, shape = RoundedCornerShape(12.dp)),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF4A6FA5),
                            unfocusedBorderColor = Color(0xFFE2E8F0),
                            focusedLabelColor = Color(0xFF4A6FA5),
                            unfocusedLabelColor = Color(0xFF64748B)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
                // Buttons Section - Second Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    GradientButton(
                        text = "Enroll Student",
                        gradientColors = listOf(Color(0xFF22C55E), Color(0xFF16A34A)),
                        onClick = {
                            showDialog = true
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }

    // Confirmation Dialog (kept exactly the same as original)
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            confirmButton = {
                TextButton(
                    onClick = { showDialog = false },
                    colors = ButtonDefaults.textButtonColors(contentColor = Color.Red)
                ) {
                    Text("Close", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                }
            },
            title = {
                Text(
                    "The student has been added successfully",
                    color = Color(0xFF1E293B),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 24.sp
                )
            },
            text = {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "DISHA ID : ",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E293B)
                    )
                    if (showDISHAID) {
                        Text(
                            text = dishaID,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF4A6FA5)
                        )
                    }
                    IconButton(
                        onClick = { showDISHAID = !showDISHAID },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = if (showDISHAID) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = "Toggle DISHA ID Visibility",
                            tint = Color(0xFF4A6FA5)
                        )
                    }
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(16.dp),
            properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = true)
        )
    }
}

// Custom Gradient Button (unchanged)
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

// Preview Function (unchanged)
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun AddNewStudentScreenPreview() {
    val navController = rememberNavController()
    AddNewStudentScreen(navController = navController)
}