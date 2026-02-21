package com.avinashpatil.app.admindisha.screens.attendance

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.avinashpatil.app.admindisha.api.BatchUser
import com.avinashpatil.app.admindisha.api.UserAllDetails
import com.avinashpatil.app.admindisha.navigation.Screen
import com.avinashpatil.app.admindisha.viewmodels.AttendanceViewModel
import java.time.Instant
import java.time.ZoneId
import kotlin.collections.filter
@Composable

fun StudentCard(
    student: UserAllDetails,
    isPresent: Boolean?,
    onPresenceChange: (Boolean) -> Unit,
    navController: NavController,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .shadow(4.dp, RoundedCornerShape(12.dp))
            .clip(RoundedCornerShape(12.dp))
            .clickable { navController.navigate("${Screen.AllInOneBill.route}/${student.userId}") },
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = student.userName ?: "",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF1E293B),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ModernToggleButton(
                    isPresent = isPresent,
                    onToggle = onPresenceChange
                )
            }
        }
    }
}


@Composable
fun BatchUserCard(
    batchUser: BatchUser,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .shadow(4.dp, RoundedCornerShape(12.dp))
            .clip(RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = batchUser.fullName,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF1E293B),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun ModernToggleButton(
    isPresent: Boolean?,
    onToggle: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xFFF1F5F9))
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Button(
            onClick = { onToggle(true) },
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isPresent == true) Color(0xFF22C55E) else Color.Transparent,
                contentColor = if (isPresent == true) Color.White else Color(0xFF64748B)
            ),
            modifier = Modifier.height(32.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("Present", fontSize = 12.sp)
        }
        Button(
            onClick = { onToggle(false) },
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isPresent == false) Color(0xFFEF4444) else Color.Transparent,
                contentColor = if (isPresent == false) Color.White else Color(0xFF64748B)
            ),
            modifier = Modifier.height(32.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("Absent", fontSize = 12.sp)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttendanceScreen(
    navController: NavController,
    viewModel: AttendanceViewModel = hiltViewModel(),
    batchId: String
) {
    val students by viewModel.students.collectAsState()
    val batchUsers by viewModel.batchUsers.collectAsState()
    val attendanceMap by viewModel.attendanceMap.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val searchText by viewModel.searchQuery.collectAsState()
    val filterStatus by viewModel.filterStatus.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()

    var showDatePicker by remember { mutableStateOf(false) }
    var expandedDropdown by remember { mutableStateOf(false) }
    var selectedFilter by remember { mutableStateOf("All") }
    var showBatchUsers by remember { mutableStateOf(false) }

    LaunchedEffect(batchId) {
        viewModel.loadStudentsForBatch(batchId)
        //viewModel.loadAttendanceForDate(batchId, selectedDate)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Tab Row for switching between Students and Batch Users
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(
                onClick = { showBatchUsers = false },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (!showBatchUsers) Color(0xFF4A6FA5) else Color(0xFFE2E8F0),
                    contentColor = if (!showBatchUsers) Color.White else Color(0xFF1E293B)
                ),
                modifier = Modifier.weight(1f)
            ) {
                Text("Students")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = { showBatchUsers = true },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (showBatchUsers) Color(0xFF4A6FA5) else Color(0xFFE2E8F0),
                    contentColor = if (showBatchUsers) Color.White else Color(0xFF1E293B)
                ),
                modifier = Modifier.weight(1f)
            ) {
                Text("Batch Users")
            }
        }

        // Search and Filter Row (only show for Students tab)
        if (!showBatchUsers) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Search Field
                OutlinedTextField(
                    value = searchText,
                    onValueChange = { viewModel.updateSearchQuery(it) },
                    placeholder = { Text("Search students") },
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF4A6FA5),
                        unfocusedBorderColor = Color(0xFFE2E8F0)
                    )
                )

                Spacer(modifier = Modifier.width(8.dp))

                // Date Picker Button
                Button(
                    onClick = { showDatePicker = true },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4A6FA5))
                ) {
                    Text(selectedDate.toString())
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Filter Dropdown
                ExposedDropdownMenuBox(
                    expanded = expandedDropdown,
                    onExpandedChange = { expandedDropdown = it },
                    modifier = Modifier.width(120.dp)
                ) {
                    OutlinedTextField(
                        value = selectedFilter,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { Icon(Icons.Default.ArrowDropDown, "Dropdown") },
                        modifier = Modifier.menuAnchor(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF4A6FA5),
                            unfocusedBorderColor = Color(0xFFE2E8F0)
                        )
                    )

                    ExposedDropdownMenu(
                        expanded = expandedDropdown,
                        onDismissRequest = { expandedDropdown = false }
                    ) {
                        listOf("All", "Present", "Absent").forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option) },
                                onClick = {
                                    selectedFilter = option
                                    viewModel.filterStudents(option)
                                    expandedDropdown = false
                                }
                            )
                        }
                    }
                }
            }
        } else {
            // Simple search for Batch Users tab
            OutlinedTextField(
                value = searchText,
                onValueChange = { viewModel.updateSearchQuery(it) },
                placeholder = { Text("Search batch users") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF4A6FA5),
                    unfocusedBorderColor = Color(0xFFE2E8F0)
                )
            )
        }





        // Date Picker Dialog
        if (showDatePicker) {
            val datePickerState = rememberDatePickerState(
                initialSelectedDateMillis = selectedDate.atStartOfDay()
                    .atZone(java.time.ZoneId.systemDefault())
                    .toInstant()
                    .toEpochMilli()
            )

            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    Button(onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val newDate = Instant.ofEpochMilli(millis)
                                .atZone(ZoneId.systemDefault())
                                .toLocalDate()
                            viewModel.updateSelectedDate(newDate)
                        }
                        showDatePicker = false
                    }) {
                        Text("Confirm")
                    }
                },
                dismissButton = {
                    Button(onClick = { showDatePicker = false }) {
                        Text("Cancel")
                    }
                }
            ) {
                DatePicker(state = datePickerState)
            }
        }

        // Student or Batch User List
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (error != null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(error ?: "Unknown error occurred", color = Color.Red)
            }
        } else {
            if (!showBatchUsers) {
                // Students List
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val filteredStudents = students.filter {
                        it.userName?.contains(searchText, ignoreCase = true) ?: false
                    }

                    items(filteredStudents.size) { index ->
                        val student = filteredStudents[index]
                        StudentCard(
                            student = student,
                            isPresent = attendanceMap[student.userId],
                            onPresenceChange = { isPresent ->
                                viewModel.markAttendance(batchId, student.userId, selectedDate, isPresent)
                            },
                            navController = navController
                        )
                    }
                }
            } else {
                // Batch Users List
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (batchUsers.isEmpty()) {
                        item {
                            Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                                Text("No batch users found", color = Color.Gray)
                            }
                        }
                    } else {
                        val filteredBatchUsers = batchUsers.filter { user ->
                            user.fullName.contains(searchText, ignoreCase = true)
                        }

                        if (filteredBatchUsers.isEmpty()) {
                            item {
                                Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                                    Text("No matching batch users found", color = Color.Gray)
                                }
                            }
                        } else {
                            items(filteredBatchUsers) { batchUser ->
                                BatchUserCard(batchUser = batchUser)
                            }
                        }
                    }
                }
            }
        }
    }
}