package com.avinashpatil.app.admindisha.screens.exams

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.avinashpatil.app.admindisha.api.Quiz
import com.avinashpatil.app.admindisha.viewmodels.ExamViewModel
import java.time.format.DateTimeFormatter

@Composable
fun ExamScreen(
    navController: NavController,
    courseId: String?,
    viewModel: ExamViewModel = hiltViewModel()
) {
    // Background Gradient Colors
    val gradientColors = listOf(
        Color(0xFFA78BFA), // #a78bfa
        Color(0xFF6366F1), // #6366f1
        Color(0xFF22D3EE)  // #22d3ee
    )

    val quizzes by viewModel.quizzes.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    var showDeleteDialog by remember { mutableStateOf(false) }
    var quizToDelete by remember { mutableStateOf<Quiz?>(null) }

    LaunchedEffect(courseId) {
        if (courseId != null) {
            viewModel.getQuizzesByCourseId(courseId)
        }
    }

    if (showDeleteDialog && quizToDelete != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Quiz") },
            text = { Text("Are you sure you want to delete this quiz?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (courseId != null) {
                            viewModel.deleteQuiz(quizToDelete!!.quizId, courseId)
                        }
                        showDeleteDialog = false
                    }
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.linearGradient(
                    colors = gradientColors,
                    start = androidx.compose.ui.geometry.Offset(0f, 0f),
                    end = androidx.compose.ui.geometry.Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY),
                    tileMode = TileMode.Clamp
                )
            )
    ) {
        // Glow Dots (Decorative background elements)
        // Top Left
        GlowDot(
            modifier = Modifier
                .offset(x = (-80).dp, y = (-80).dp)
                .size(300.dp)
        )
        // Center Right
        GlowDot(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .offset(x = 80.dp) // Adjusted for visual match
                .size(300.dp)
        )
        // Bottom Left
        GlowDot(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .offset(x = 100.dp, y = 80.dp)
                .size(300.dp)
        )

        // Main Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier
                        .glassCard(RoundedCornerShape(12.dp))
                        .size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Text(
                    text = "Exams",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            // Search Bar (Visual only for now, can be wired up later)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
                    .glassCard(RoundedCornerShape(16.dp))
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = Color.White.copy(alpha = 0.6f)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Search exams...",
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 16.sp
                )
            }

            // Exams List
            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color.White)
                }
            } else if (error != null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = error ?: "Unknown error", color = Color.Red)
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(quizzes) { quiz ->
                        ExamCard(
                            title = quiz.title,
                            duration = "${quiz.durationMinutes} mins",
                            date = quiz.scheduledMinutes?.format(DateTimeFormatter.ofPattern("dd-MM-yyyy")) ?: "N/A",
                            scheduledTime = quiz.scheduledMinutes?.format(DateTimeFormatter.ofPattern("HH:mm:ss")) ?: "N/A",
                            onDeleteClick = {
                                quizToDelete = quiz
                                showDeleteDialog = true
                            }
                        )
                    }
                }
            }
        }
        }
    }

@Composable
fun GlowDot(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.2f))
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.4f),
                        Color.Transparent
                    )
                )
            )
    )
}

fun Modifier.glassCard(shape: androidx.compose.ui.graphics.Shape) = this
    .clip(shape)
    .background(Color.White.copy(alpha = 0.12f))
    .border(1.dp, Color.White.copy(alpha = 0.2f), shape)

@Composable
fun ExamCard(
    title: String,
    duration: String,
    date: String,
    scheduledTime: String,
    onDeleteClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .glassCard(RoundedCornerShape(16.dp))
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "$duration • $date • $scheduledTime",
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.8f)
            )
        }
        
        IconButton(
            onClick = onDeleteClick,
            modifier = Modifier
                .size(40.dp)
                .background(Color.White.copy(alpha = 0.2f), CircleShape)
        ) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Delete",
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
