package com.avinashpatil.app.admindisha.screens.topic

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Grain
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.avinashpatil.app.admindisha.api.Topic
import com.avinashpatil.app.admindisha.ui.theme.AdminDISHATheme

// Colors derived from HTML/Tailwind config
private val PrimaryColor = Color(0xFF3713EC)
private val BgGradientStart = Color(0xFFF0F4FF)
private val BgGradientMid = Color(0xFFE0E7FF)
private val BgGradientEnd = Color(0xFFDBEAFE)
private val FabGradientStart = Color(0xFFFF3D77)
private val FabGradientEnd = Color(0xFFFF914D)

// Text Colors
private val TextSlate900 = Color(0xFF0F172A)
private val TextSlate800 = Color(0xFF1E293B)
private val TextSlate400 = Color(0xFF94A3B8)

// Card Colors
private val GlassBg = Color.White.copy(alpha = 0.4f)
private val GlassBorder = Color.White.copy(alpha = 0.6f)

@Composable
fun TopicsScreen(
    navController: NavController,
    courseId: String? = null,
    viewModel: TopicViewModel = hiltViewModel()
) {
    val topics by viewModel.topics.collectAsState()
    val sortedTopics = remember(topics) { topics.sortedBy { it.topicName } }
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf<Topic?>(null) }
    var showDeleteDialog by remember { mutableStateOf<Topic?>(null) }

    LaunchedEffect(courseId) {
        if (courseId != null) {
            viewModel.fetchTopics(courseId)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(BgGradientStart, BgGradientMid, BgGradientEnd)
                )
            )
    ) {
        // Background Glow Blobs
        // Purple blob (Top-Left)
        /*
        Box(
            modifier = Modifier
                .offset(x = (-80).dp, y = (-80).dp)
                .size(256.dp)
                .clip(CircleShape)
                .background(Color(0xFFD8B4FE).copy(alpha = 0.6f)) // purple-300
        )
        // Blue blob (Right-Center)
        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .offset(x = 80.dp)
                .size(320.dp)
                .clip(CircleShape)
                .background(Color(0xFF93C5FD).copy(alpha = 0.6f)) // blue-300
        )
        // Cyan blob (Bottom-Left)
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .offset(x = 100.dp, y = 100.dp)
                .size(288.dp)
                .clip(CircleShape)
                .background(Color(0xFFA5F3FC).copy(alpha = 0.6f)) // cyan-200
        )

         */

        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                GlassTopBar(
                    title = "Topics",
                    onBackClick = { navController.popBackStack() }
                )
            },
            floatingActionButton = {
                // Custom FAB to match the gradient design
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(FabGradientStart, FabGradientEnd)
                            )
                        )
                        .clickable { showAddDialog = true }
                        .padding(16.dp), // Adjust padding for icon size
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Topic",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        ) { paddingValues ->
            Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                } else if (error != null) {
                    Text(
                        text = error ?: "Unknown error",
                        color = Color.Red,
                        modifier = Modifier.align(Alignment.Center)
                    )
                } else if (topics.isEmpty()) {
                    Text(
                        text = "No topics found",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.align(Alignment.Center)
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 24.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp)
                    ) {
                        items(sortedTopics) { topic ->
                            TopicCard(
                                title = topic.topicName,
                                icon = Icons.Default.Memory, // Default icon as API doesn't provide one
                                iconBgColor = PrimaryColor.copy(alpha = 0.1f),
                                iconTint = PrimaryColor,
                                onEditClick = { showEditDialog = topic },
                                onDeleteClick = { showDeleteDialog = topic }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddTopicDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { topicName ->
                if (courseId != null) {
                    viewModel.createTopic(courseId, topicName)
                }
                showAddDialog = false
            }
        )
    }

    if (showEditDialog != null) {
        EditTopicDialog(
            topic = showEditDialog!!,
            onDismiss = { showEditDialog = null },
            onConfirm = { updatedName ->
                viewModel.updateTopic(showEditDialog!!.topicId!!, showEditDialog!!.courseId, updatedName)
                showEditDialog = null
            }
        )
    }

    if (showDeleteDialog != null) {
        DeleteTopicConfirmationDialog(
            topicName = showDeleteDialog!!.topicName,
            onDismiss = { showDeleteDialog = null },
            onConfirm = {
                if (courseId != null) {
                    viewModel.deleteTopic(showDeleteDialog!!.topicId!!, courseId)
                }
                showDeleteDialog = null
            }
        )
    }
}

@Composable
fun GlassTopBar(
    title: String,
    onBackClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White.copy(alpha = 0.2f)) // Glass nav bg
            .padding(horizontal = 24.dp, vertical = 24.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Back Button
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.4f))
                .border(1.dp, GlassBorder, CircleShape)
                .clickable { onBackClick() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = PrimaryColor,
                modifier = Modifier.size(24.dp)
            )
        }

        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                color = TextSlate800
            )
        )

        // Empty box for alignment balance
        Box(modifier = Modifier.size(40.dp))
    }
}

@Composable
fun TopicCard(
    title: String,
    icon: ImageVector,
    iconBgColor: Color,
    iconTint: Color,
    onEditClick: () -> Unit = {},
    onDeleteClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(GlassBg)
            .border(1.dp, GlassBorder, RoundedCornerShape(16.dp))
            .padding(20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Icon Container
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(iconBgColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(28.dp)
            )
        }

        // Title
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = TextSlate900
            ),
            modifier = Modifier.weight(1f)
        )

        // Action Buttons
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            IconButton(
                onClick = onEditClick,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit",
                    tint = TextSlate400,
                    modifier = Modifier.size(20.dp)
                )
            }
            IconButton(
                onClick = onDeleteClick,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = TextSlate400, // Hover to red not implemented in mobile touch
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun AddTopicDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var topicName by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add New Topic") },
        text = {
            OutlinedTextField(
                value = topicName,
                onValueChange = { topicName = it },
                label = { Text("Topic Name") },
                singleLine = true
            )
        },
        confirmButton = {
            Button(
                onClick = {
                    if (topicName.isNotBlank()) {
                        onConfirm(topicName)
                    }
                }
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun EditTopicDialog(
    topic: Topic,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var topicName by remember { mutableStateOf(topic.topicName) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Topic") },
        text = {
            OutlinedTextField(
                value = topicName,
                onValueChange = { topicName = it },
                label = { Text("Topic Name") },
                singleLine = true
            )
        },
        confirmButton = {
            Button(
                onClick = {
                    if (topicName.isNotBlank()) {
                        onConfirm(topicName)
                    }
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun DeleteTopicConfirmationDialog(
    topicName: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Delete Topic") },
        text = { Text("Are you sure you want to delete topic \"$topicName\"?") },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
            ) {
                Text("Delete")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun TopicsScreenPreview() {
    AdminDISHATheme {
        TopicsScreen(navController = rememberNavController())
    }
}
