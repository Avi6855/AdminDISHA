package com.avinashpatil.app.admindisha.screens.course

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.filled.ArrowBack
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.avinashpatil.app.admindisha.api.Course

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddNewCourseScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    id: String? = null,
    name: String? = null,
    description: String? = null,
    mrpPrice: String? = null,
    offerPrice: String? = null,
    viewModel: CourseViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val isEditMode = id != null

    // State variables for user inputs
    var courseName by remember { mutableStateOf(name ?: "") }
    var courseDescription by remember { mutableStateOf(description ?: "") }
    var courseMRP by remember { mutableStateOf(mrpPrice ?: "") }
    var courseOfferPrice by remember { mutableStateOf(offerPrice ?: "") }

    // Dialog state for confirmation
    var showDialog by remember { mutableStateOf(false) }
    var dialogMessage by remember { mutableStateOf("") }
    var dialogAction by remember { mutableStateOf<(() -> Unit)?>(null) }

    // Error state
    val error by viewModel.error.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    // Effect to show error message
    LaunchedEffect(error) {
        error?.let {
            context.showToast(it)
        }
    }

    // Validation function
    fun validateInputs(context: Context): Boolean {
        if (courseName.isBlank()) {
            context.showToast("Please enter course name")
            return false
        }
        if (courseDescription.isBlank()) {
            context.showToast("Please enter course description")
            return false
        }
        if (courseMRP.isBlank()) {
            context.showToast("Please enter course price")
            return false
        }
        if (courseOfferPrice.isBlank()) {
            context.showToast("Please enter offer price")
            return false
        }

        val mrp = courseMRP.toDoubleOrNull()
        val offer = courseOfferPrice.toDoubleOrNull()

        if (mrp == null) {
            context.showToast("Please enter a valid course price")
            return false
        }
        if (offer == null) {
            context.showToast("Please enter a valid offer price")
            return false
        }
        if (mrp < 0 || offer < 0) {
            context.showToast("Price cannot be negative")
            return false
        }
        if (offer > mrp) {
            context.showToast("Offer price cannot be greater than course price")
            return false
        }
        return true
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color(0xFFF8FAFC),
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
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
                        text = if (isEditMode) "EDIT COURSE" else "ADD NEW COURSE",
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
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Course Name Input
                OutlinedTextField(
                    value = courseName,
                    onValueChange = { courseName = it },
                    label = { Text("Course Name", color = Color(0xFF64748B)) },
                    placeholder = { Text("Enter course name...", color = Color(0xFFCBD5E1)) },
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

                // Course Description Input
                OutlinedTextField(
                    value = courseDescription,
                    onValueChange = { courseDescription = it },
                    label = { Text("Course Description", color = Color(0xFF64748B)) },
                    placeholder = { Text("Enter course description...", color = Color(0xFFCBD5E1)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
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
                    maxLines = 4,
                    shape = RoundedCornerShape(12.dp)
                )

                // Course Fee Input
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "₹",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF4A6FA5),
                        modifier = Modifier.padding(end = 12.dp)
                    )
                    OutlinedTextField(
                        value = courseMRP,
                        onValueChange = { newValue ->
                            if (newValue.all { it.isDigit() || it == '.' }) {
                                courseMRP = newValue
                            }
                        },
                        label = { Text("Course Price", color = Color(0xFF64748B)) },
                        placeholder = { Text("Enter MRP Price...", color = Color(0xFFCBD5E1)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .weight(1f)
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
                }

                // Course Fee Input
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "₹",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF4A6FA5),
                        modifier = Modifier.padding(end = 12.dp)
                    )
                    OutlinedTextField(
                        value = courseOfferPrice,
                        onValueChange = { newValue ->
                            if (newValue.all { it.isDigit() || it == '.' }) {
                                courseOfferPrice = newValue
                            }
                        },
                        label = { Text("Offer Price", color = Color(0xFF64748B)) },
                        placeholder = { Text("Enter Offer Price...", color = Color(0xFFCBD5E1)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .weight(1f)
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
                }

                if (isEditMode) {
                    // Buttons Section - First Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        GradientButton(
                            text = "Delete",
                            gradientColors = listOf(Color(0xFFEF4444), Color(0xFFDC2626)),
                            onClick = {
                                dialogMessage = "Are you sure you want to delete this course?"
                                dialogAction = {
                                    viewModel.deleteCourse(id!!)
                                    navController.popBackStack()
                                }
                                showDialog = true
                            },
                            modifier = Modifier.weight(1f)
                        )

                        // Update mode button
                        GradientButton(
                            text = "Update",
                            gradientColors = listOf(Color(0xFFF59E0B), Color(0xFFD97706)),
                            onClick = {
                                if (validateInputs(context)) {
                                    val course = Course(
                                        courseId = id!!,
                                        courseName = courseName,
                                        courseDescription = courseDescription,
                                        courseDuration = "", // Add appropriate value
                                        coursePrice = courseMRP,
                                        courseOfferPrice = courseOfferPrice,
                                        batches = emptyList() // Add appropriate value
                                    )
                                    viewModel.updateCourse(id, course)
                                    navController.popBackStack()
                                }
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                } else {
                    // Buttons Section - Second Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        GradientButton(
                            text = "Cancel",
                            gradientColors = listOf(Color(0xFF64748B), Color(0xFF475569)),
                            onClick = { navController.popBackStack() },
                            modifier = Modifier.weight(1f)
                        )

                        GradientButton(
                            text = " submit",
                            gradientColors = listOf(Color(0xFF22C55E), Color(0xFF16A34A)),
                            onClick = {
                                if (validateInputs(context)) {
                                    val course = Course(
                                        courseId = "", // Backend will generate ID
                                        courseName = courseName,
                                        courseDescription = courseDescription,
                                        courseDuration = "", // Add appropriate value
                                        coursePrice =  courseMRP,
                                        courseOfferPrice = courseOfferPrice,
                                        batches = emptyList() // Add appropriate value
                                    )
                                    viewModel.addCourse(course)
                                    navController.popBackStack()
                                }
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        // Confirmation Dialog
        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                confirmButton = {
                    TextButton(
                        onClick = {
                            dialogAction?.invoke()
                            showDialog = false
                        },
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
    }
}
}

// Custom Gradient Button
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

// Utility function to show toast messages
fun Context.showToast(message: String) {
    android.widget.Toast.makeText(this, message, android.widget.Toast.LENGTH_SHORT).show()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddNewCourseScreen(
    navController: NavController,
    viewModel: CourseViewModel = hiltViewModel(),
    courseId: String? = null
) {
    var courseName by remember { mutableStateOf("") }
    var courseDescription: String? by remember { mutableStateOf("") }
    var courseDuration: String? by remember { mutableStateOf("") }
    var coursePrice by remember { mutableStateOf("") }
    var courseOfferPrice by remember { mutableStateOf("") }
    var showErrorDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    LaunchedEffect(courseId) {
        if (courseId != null) {
            // Load existing course data for editing
            try {
                val response = viewModel.apiService.getCourseById(courseId)
                if (response.isSuccessful) {
                    response.body()?.let { course ->
                        courseName = course.courseName
                        courseDescription = course.courseDescription
                        courseDuration = course.courseDuration
                        coursePrice = course.coursePrice.toString()
                        courseOfferPrice = course.courseOfferPrice.toString()
                    }
                }
            } catch (e: Exception) {
                errorMessage = "Failed to load course: ${e.message}"
                showErrorDialog = true
            }
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = { Text(if (courseId == null) "Add New Course" else "Edit Course") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = courseName,
                onValueChange = { courseName = it },
                label = { Text("Course Name") },
                modifier = Modifier.fillMaxWidth(),
                isError = courseName.isBlank()
            )

            OutlinedTextField(
                value = courseDescription ?: "",
                onValueChange = { courseDescription = it },
                label = { Text("Course Description") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )

            OutlinedTextField(
                value = courseDuration ?: "",
                onValueChange = { courseDuration = it },
                label = { Text("Duration") },
                modifier = Modifier.fillMaxWidth(),
                isError = courseDuration?.isBlank() == true
            )

            OutlinedTextField(
                value = coursePrice,
                onValueChange = { coursePrice = it },
                label = { Text("Price") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                isError = coursePrice.isBlank() || coursePrice.toDoubleOrNull() == null
            )

            OutlinedTextField(
                value = courseOfferPrice,
                onValueChange = { courseOfferPrice = it },
                label = { Text("Offer Price") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            Button(
                onClick = {
                    if (validateInputs(courseName, courseDuration, coursePrice)) {
                        val course = Course(
                            courseId = courseId ?: "", // Empty for new courses
                            courseName = courseName,
                            courseDescription = courseDescription,
                            courseDuration = courseDuration,
                            coursePrice = coursePrice,
                            courseOfferPrice = courseOfferPrice,
                            batches = emptyList()
                        )

                        if (courseId == null) {
                            viewModel.addCourse(course)
                        } else {
                            viewModel.updateCourse(courseId, course)
                        }
                        navController.popBackStack()
                    } else {
                        errorMessage = "Please fill in all required fields correctly"
                        showErrorDialog = true
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color.White)
                } else {
                    Text(if (courseId == null) "Add Course" else "Update Course")
                }
            }
        }

        if (showErrorDialog) {
            AlertDialog(
                onDismissRequest = { showErrorDialog = false },
                title = { Text("Error") },
                text = { Text(errorMessage) },
                confirmButton = {
                    TextButton(onClick = { showErrorDialog = false }) {
                        Text("OK")
                    }
                }
            )
        }
    }
}

private fun validateInputs(courseName: String, courseDuration: String?, coursePrice: String): Boolean {
    return courseName.isNotBlank() &&
           courseDuration?.isNotBlank() == true &&
           coursePrice.isNotBlank() &&
           coursePrice.toDoubleOrNull() != null
}