package com.avinashpatil.app.admindisha.screens.dishaID

import android.content.Context
import android.os.Parcelable
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.avinashpatil.app.admindisha.utils.showToast
import kotlinx.android.parcel.Parcelize

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeeDISHAIDScreen(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var searchText by remember { mutableStateOf("") }
    var active by remember { mutableStateOf(false) }
    var selectedCourse by remember { mutableStateOf("All Courses") }
    val courseList = listOf("All Courses", "Course A", "Course B", "Course C")

    val students = remember {
        listOf(
            Student("1", "Avinash Rajendra Patil", "Course A", "Batch 1", "DISH001"),
            Student("2", "Dhiraj Shripati Patil", "Course B", "Batch 2", "DISH002"),
            Student("3", "Prathamesh Dattatray Patil", "Course A", "Batch 1", "DISH003"),
            Student("4", "Aniruddha Rajendra Patil", "Course C", "Batch 3", "DISH004"),
            Student("5", "DISHA Computer Education", "Course A", "Batch 1", "DISH005")
        ).sortedBy { it.name }
    }

    // Sample list of students
    val allStudents = remember {
        listOf(
            Student("1", "Avinash Rajendra Patil", "Course A", "Batch 1", "DISHA001"),
            Student("2", "Dhiraj Shripati Patil", "Course B", "Batch 2", "DISHA002"),
            Student("3", "Prathamesh Dattatray Patil", "Course A", "Batch 1", "DISHA003"),
            Student("4", "Aniruddha Rajendra Patil", "Course C", "Batch 3", "DISHA004"),
            Student("5", "DISHA Computer Education", "Course A", "Batch 1", "DISHA005")
        ).sortedBy { it.name }
    }

    // Filtered students based on selected course and search text
    val filteredStudents = if (selectedCourse == "All Courses") {
        allStudents.filter {
            it.srNo.contains(searchText, ignoreCase = true) ||
                    it.name.contains(searchText, ignoreCase = true) ||
                    it.courseName.contains(searchText, ignoreCase = true) ||
                    it.batchNo.contains(searchText, ignoreCase = true) ||
                    it.dishaId.contains(searchText, ignoreCase = true)
        }
    } else {
        allStudents.filter {
            (it.courseName == selectedCourse) &&
                    (
                            it.srNo.contains(searchText, ignoreCase = true) ||
                                    it.name.contains(searchText, ignoreCase = true) ||
                                    it.courseName.contains(searchText, ignoreCase = true) ||
                                    it.batchNo.contains(searchText, ignoreCase = true) ||
                                    it.dishaId.contains(searchText, ignoreCase = true)
                            )
        }
    }

    // Reassign Sr No dynamically based on filtered students
    val sortedStudentsWithSrNo = filteredStudents.mapIndexed { index, student ->
        student.copy(srNo = (index + 1).toString())
    }

    // Suggestions based on search text
    val suggestions = if (searchText.isNotEmpty()) {
        allStudents.filter {
            it.srNo.contains(searchText, ignoreCase = true) ||
                    it.name.contains(searchText, ignoreCase = true) ||
                    it.courseName.contains(searchText, ignoreCase = true) ||
                    it.batchNo.contains(searchText, ignoreCase = true) ||
                    it.dishaId.contains(searchText, ignoreCase = true)
        }.take(3)
    } else {
        emptyList()
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
            // Top App Bar - updated to match reference style
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
                        text = "SEE DISHA ID",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )

                    // Course dropdown
                    var expanded by remember { mutableStateOf(false) }
                    Box(modifier = Modifier.wrapContentSize(Alignment.TopEnd)) {
                        IconButton(
                            onClick = { expanded = true },
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = "Filter",
                                tint = Color.White
                            )
                        }
                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false },
                            modifier = Modifier.background(Color.White)
                        ) {
                            courseList.forEach { course ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = course,
                                            color = if (course == selectedCourse) Color(0xFF4A6FA5) else Color(
                                                0xFF1E293B
                                            )
                                        )
                                    },
                                    onClick = {
                                        selectedCourse = course
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Search Bar with suggestions (updated to match reference style)
            SearchBar(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                query = searchText,
                onQueryChange = { searchText = it },
                onSearch = { active = false },
                active = active,
                onActiveChange = { active = it },
                placeholder = { Text("Search students by name, DISHA ID, course or batch...") },
                leadingIcon = {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = "Search",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                colors = SearchBarDefaults.colors(
                    containerColor = Color.White,
                    dividerColor = Color.Transparent
                )
            ) {
                // Search suggestions
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                ) {
                    suggestions.forEach { student ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    searchText = student.name
                                    active = false
                                }
                                .padding(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                tint = Color(0xFF64748B)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = student.name,
                                    color = Color(0xFF1E293B)
                                )
                                Text(
                                    text = "${student.courseName} • ${student.batchNo} • ${student.dishaId}",
                                    color = Color(0xFF64748B),
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }

            // List of Students
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(sortedStudentsWithSrNo.size) { index ->
                    val student = sortedStudentsWithSrNo[index]
                    StudentCard(student = student)
                }
            }
        }
    }
}

@Parcelize
data class Student(
    val srNo: String,
    val name: String,
    val courseName: String,
    val batchNo: String,
    val dishaId: String
) : Parcelable

@Composable
fun StudentCard(student: Student, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(elevation = 4.dp, shape = RoundedCornerShape(12.dp))
            .clip(RoundedCornerShape(12.dp)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Row 1: Sr No and Name
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Sr No: ${student.srNo}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF1E293B)
                )
                Text(
                    text = student.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF4A6FA5)
                )
            }
            // Row 2: Course Name with Unique Color
            Text(
                text = "Course: ${student.courseName}",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFFE67E22)
            )
            // Row 3: Batch Name
            Text(
                text = "Batch: ${student.batchNo}",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF22C55E)
            )
            // Row 4: DISHA ID
            Text(
                text = "DISHA ID: ${student.dishaId}",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF4A6FA5)
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun SeeDISHAIDScreenPreview() {
    val navController = rememberNavController()
    SeeDISHAIDScreen(navController = navController)
}