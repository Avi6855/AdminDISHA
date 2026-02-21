package com.avinashpatil.app.admindisha.screens.home

import androidx.compose.animation.animateColor
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.material3.MenuAnchorType
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.*
import androidx.hilt.navigation.compose.hiltViewModel
import com.avinashpatil.app.admindisha.viewmodels.HomeViewModel
import com.avinashpatil.app.admindisha.viewmodels.SendCertificateStatus
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.avinashpatil.app.admindisha.navigation.Screen
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.platform.LocalContext
import android.widget.Toast

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel()
) {
    var showMenu by remember { mutableStateOf(false) }
    val studentCounts by viewModel.studentCounts.collectAsState()
    val courses by viewModel.courses.collectAsState()
    val sendCertificateStatus by viewModel.sendCertificateStatus.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    var showSendCertDialog by remember { mutableStateOf(false) }
    var showConfirmSendDialog by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var selectedCourse by remember { mutableStateOf<com.avinashpatil.app.admindisha.api.Course?>(null) }
    var expandedDropdown by remember { mutableStateOf(false) }

    val context = LocalContext.current

    LaunchedEffect(sendCertificateStatus) {
        when (sendCertificateStatus) {
            is SendCertificateStatus.Success -> {
                showSuccessDialog = true
                showSendCertDialog = false
                showConfirmSendDialog = false
                selectedCourse = null
            }
            is SendCertificateStatus.Error -> {
                Toast.makeText(context, (sendCertificateStatus as SendCertificateStatus.Error).message, Toast.LENGTH_LONG).show()
                viewModel.resetSendCertificateStatus()
            }
            else -> {}
        }
    }

    if (sendCertificateStatus is SendCertificateStatus.Loading) {
        Dialog(onDismissRequest = {}) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(100.dp)
                    .background(Color.White, shape = RoundedCornerShape(8.dp))
            ) {
                CircularProgressIndicator()
            }
        }
    }

    if (showSuccessDialog) {
        val successMessage = (sendCertificateStatus as? SendCertificateStatus.Success)?.message ?: "Certificates sent successfully."
        AlertDialog(
            onDismissRequest = { 
                showSuccessDialog = false 
                viewModel.resetSendCertificateStatus()
            },
            title = { Text("Success") },
            text = { Text(successMessage) },
            confirmButton = {
                TextButton(onClick = { 
                    showSuccessDialog = false
                    viewModel.resetSendCertificateStatus()
                }) {
                    Text("OK")
                }
            }
        )
    }

    if (showSendCertDialog) {
        AlertDialog(
            onDismissRequest = { showSendCertDialog = false },
            title = { Text("Send Certificates") },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Select a course to send certificates to all students:")
                    
                    ExposedDropdownMenuBox(
                        expanded = expandedDropdown,
                        onExpandedChange = { expandedDropdown = !expandedDropdown },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = selectedCourse?.courseName ?: "Select Course",
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedDropdown) },
                            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                            modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled = true).fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = expandedDropdown,
                            onDismissRequest = { expandedDropdown = false }
                        ) {
                            courses.forEach { course ->
                                DropdownMenuItem(
                                    text = { Text(course.courseName) },
                                    onClick = {
                                        selectedCourse = course
                                        expandedDropdown = false
                                    }
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { 
                        if (selectedCourse != null) {
                            showConfirmSendDialog = true 
                        }
                    },
                    enabled = selectedCourse != null
                ) {
                    Text("Send Certificates")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSendCertDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showConfirmSendDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmSendDialog = false },
            title = { Text("Confirm Action") },
            text = { Text("Are you sure you want to send certificates to all students of ${selectedCourse?.courseName}?") },
            confirmButton = {
                Button(
                    onClick = {
                        selectedCourse?.let { viewModel.sendCertificates(it.courseId) }
                        showConfirmSendDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Send")
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmSendDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    val gradientList = listOf(
        listOf(Color(0xFF34D399), Color(0xFF10B981)), // Green
        listOf(Color(0xFFFBBF24), Color(0xFFF59E0B)), // Amber
        listOf(Color(0xFFF472B6), Color(0xFFEC4899)), // Pink
        listOf(Color(0xFF60A5FA), Color(0xFF3B82F6)), // Blue
        listOf(Color(0xFFA78BFA), Color(0xFF8B5CF6))  // Purple
    )

    val iconColorList = listOf(
        Color(0xFF10B981),
        Color(0xFFF59E0B),
        Color(0xFFEC4899),
        Color(0xFF3B82F6),
        Color(0xFF8B5CF6)
    )

    Scaffold(
        modifier = modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .background(Color(0xFFF5F7FA))
        ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            // Top App Bar with centered text
            Box(
                modifier = Modifier.fillMaxWidth()
            ) {
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
                            onClick = { showMenu = !showMenu },
                            modifier = Modifier.padding(start = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Menu",
                                tint = Color.White
                            )
                        }
                        Text(
                            text = "DISHA COMPUTER",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(48.dp)) // Balance the layout
                    }
                }

                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false },
                    modifier = Modifier
                        .background(Color.White)
                        .align(Alignment.TopStart)
                ) {
                    DropdownMenuItem(
                        text = { Text("Add New Student") },
                        onClick = { navController.navigate(Screen.StepUpForm.route) }
                    )
                    DropdownMenuItem(
                        text = { Text("Send Certificates") },
                        onClick = { 
                            showMenu = false
                            showSendCertDialog = true 
                        }
                    )
                    /*
                    DropdownMenuItem(
                        text = { Text("See DISHA ID") },
                        onClick = { navController.navigate(Screen.SeeDISHAID.route) }
                    )

                    DropdownMenuItem(
                        text = { Text("About") },
                        onClick = { navController.navigate(Screen.SeeDISHAID.route) }
                    )
                    */
                }
            }

            // Main content
            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (error != null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = error ?: "Unknown error", color = Color.Red)
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight()
                        .padding(horizontal = 20.dp, vertical = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    itemsIndexed(studentCounts) { index, item ->
                        val gradient = gradientList[index % gradientList.size]
                        val iconColor = iconColorList[index % iconColorList.size]
                        
                        StatisticsCard(
                            title = item.courseName,
                            count = item.totalUsersCount.toString(),
                            gradientColors = gradient,
                            iconColor = iconColor
                        )
                    }
                }
            }
        }
/*
        // FAB for DISHA AI with faint gradient
        FloatingActionButton(
            onClick = { navController.navigate(Screen.DishaAI.route) },
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color(0xFFE8F0FE), // Light blue
                                Color(0xFFD1E3FA), // Faint blue
                                Color(0xFFF5F7FA)  // Very light gray
                            ),
                            start = Offset(0f, 0f),
                            end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                        )
                    )
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "DISHA AI",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF1A73E8) // Blue text color similar to Meta AI
                    )
                }
            }
        }

 */
    }
}
}

@Composable
fun StatisticsCard(
    title: String,
    count: String,
    gradientColors: List<Color>,
    iconColor: Color,
    modifier: Modifier = Modifier
) {
    var isHovered by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isHovered) 1.05f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy)
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { isHovered = !isHovered }
            .scale(scale)
            .shadow(6.dp, shape = RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .background(
                    brush = Brush.horizontalGradient(colors = gradientColors)
                )
                .padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = iconColor,
                            modifier = Modifier.size(30.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = title,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = count,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    }
                }
            }
        }
    }
}

private enum class FabState { Collapsed, Expanded }
