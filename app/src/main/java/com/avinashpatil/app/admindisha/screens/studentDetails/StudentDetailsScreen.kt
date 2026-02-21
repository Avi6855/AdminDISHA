package com.avinashpatil.app.admindisha.screens.studentDetails

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.avinashpatil.app.admindisha.api.Batch
import com.avinashpatil.app.admindisha.api.Course
import com.avinashpatil.app.admindisha.api.UserDetailResponse
import com.avinashpatil.app.admindisha.viewmodels.StudentDetailsUiState
import com.avinashpatil.app.admindisha.viewmodels.StudentDetailsViewModel

// Colors
private val PrimaryIndigo = Color(0xFF6366F1)
private val Indigo200 = Color(0xFFC7D2FE)
private val Purple600 = Color(0xFF9333EA)
private val Emerald400 = Color(0xFF34D399)
private val Teal600 = Color(0xFF0D9488)
private val Emerald100 = Color(0xFFD1FAE5)
private val GradientOrangeStart = Color(0xFFFF7E5F)
private val AccentRose = Color(0xFFF43F5E)
private val Rose100 = Color(0xFFFFE4E6)

@Composable
fun StudentDetailsScreen(
    navController: NavController,
    userId: String,
    viewModel: StudentDetailsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val editableFullName by viewModel.editableFullName.collectAsState()
    val editableEmail by viewModel.editableEmail.collectAsState()
    
    val allCourses by viewModel.allCourses.collectAsState()
    val batchesForSelectedCourse by viewModel.batchesForSelectedCourse.collectAsState()
    val selectedCourseName by viewModel.selectedCourseName.collectAsState()
    val selectedBatchNo by viewModel.selectedBatchNo.collectAsState()
    
    val editableTotalPrice by viewModel.editableTotalPrice.collectAsState()
    val editablePaidAmount by viewModel.editablePaidAmount.collectAsState()
    val editablePendingAmount by viewModel.editablePendingAmount.collectAsState()
    val editablePaymentStatus by viewModel.editablePaymentStatus.collectAsState()

    val isSaving by viewModel.isSaving.collectAsState()
    val saveMessage by viewModel.saveMessage.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current

    LaunchedEffect(userId) {
        viewModel.loadUserDetails(userId)
    }
    
    LaunchedEffect(saveMessage) {
        saveMessage?.let {
            android.widget.Toast.makeText(context, it, android.widget.Toast.LENGTH_SHORT).show()
            viewModel.clearSaveMessage()
        }
    }

    StudentDetailsScreenContent(
        uiState = uiState,
        editableFullName = editableFullName,
        editableEmail = editableEmail,
        allCourses = allCourses,
        batchesForSelectedCourse = batchesForSelectedCourse,
        selectedCourseName = selectedCourseName,
        selectedBatchNo = selectedBatchNo,
        editableTotalPrice = editableTotalPrice,
        editablePaidAmount = editablePaidAmount,
        editablePendingAmount = editablePendingAmount,
        editablePaymentStatus = editablePaymentStatus,
        isSaving = isSaving,
        onFullNameChange = viewModel::updateFullName,
        onEmailChange = viewModel::updateEmail,
        onCourseSelected = viewModel::selectCourse,
        onBatchSelected = viewModel::selectBatch,
        onTotalPriceChange = viewModel::updateTotalPrice,
        onPaidAmountChange = viewModel::updatePaidAmount,
        onPendingAmountChange = viewModel::updatePendingAmount,
        onPaymentStatusChange = viewModel::updatePaymentStatus,
        onSaveChanges = viewModel::saveChanges,
        onBackClick = { navController.popBackStack() },
        onReload = { viewModel.reload() }
    )
}

@Composable
fun StudentDetailsScreenContent(
    uiState: StudentDetailsUiState,
    editableFullName: String,
    editableEmail: String,
    allCourses: List<Course>,
    batchesForSelectedCourse: List<Batch>,
    selectedCourseName: String?,
    selectedBatchNo: String?,
    editableTotalPrice: String,
    editablePaidAmount: String,
    editablePendingAmount: String,
    editablePaymentStatus: String,
    isSaving: Boolean,
    onFullNameChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onCourseSelected: (String, String) -> Unit,
    onBatchSelected: (String, String) -> Unit,
    onTotalPriceChange: (String) -> Unit,
    onPaidAmountChange: (String) -> Unit,
    onPendingAmountChange: (String) -> Unit,
    onPaymentStatusChange: (String) -> Unit,
    onSaveChanges: () -> Unit,
    onBackClick: () -> Unit,
    onReload: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Header
            Header(
                onBackClick = onBackClick,
                onReload = onReload
            )

            when (val state = uiState) {
                is StudentDetailsUiState.Loading -> {
                    Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                }
                is StudentDetailsUiState.Error -> {
                    CenteredErrorDisplay(
                        message = state.message,
                        onRetry = onReload
                    )
                }
                is StudentDetailsUiState.Success -> {
                    val user = state.userDetails
                    val finalPrice = user.finalPrice?.toDoubleOrNull() ?: 0.0
                    val paidAmount = user.paidAmount?.toDoubleOrNull() ?: 0.0

                    // Content
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Student Information Card
                        GradientCard(
                            colors = listOf(PrimaryIndigo, Purple600),
                            shadowColor = Indigo200,
                            icon = Icons.Rounded.Person,
                            title = "Student Information"
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                EditableInfoItem(
                                    label = "Student Name",
                                    value = editableFullName,
                                    onValueChange = onFullNameChange,
                                    isLarge = true
                                )
                                EditableInfoItem(
                                    label = "Email Address",
                                    value = editableEmail,
                                    onValueChange = onEmailChange
                                )
                            }
                        }

                        // Selected Course Card
                        GradientCard(
                            colors = listOf(Emerald400, Teal600),
                            shadowColor = Emerald100,
                            icon = Icons.Rounded.School,
                            title = "Selected Course"
                        ) {
                            SelectionDropdown(
                                label = "Select Course",
                                selectedValue = selectedCourseName ?: "Select Course",
                                options = allCourses.map { it.courseName },
                                onOptionSelected = { index -> 
                                    if (index in allCourses.indices) {
                                        val course = allCourses[index]
                                        onCourseSelected(course.courseId, course.courseName)
                                    }
                                }
                            )
                        }

                        // Selected Batch Card
                        GradientCard(
                            colors = listOf(GradientOrangeStart, AccentRose),
                            shadowColor = Rose100,
                            icon = Icons.Rounded.Groups,
                            title = "Selected Batch"
                        ) {
                            SelectionDropdown(
                                label = "Select Batch",
                                selectedValue = selectedBatchNo ?: "Select Batch",
                                options = batchesForSelectedCourse.map { it.batchNo },
                                onOptionSelected = { index -> 
                                    if (index in batchesForSelectedCourse.indices) {
                                        val batch = batchesForSelectedCourse[index]
                                        onBatchSelected(batch.batchId, batch.batchNo)
                                    }
                                }
                            )
                        }

                        // Payment Details Card
                        EditablePaymentDetailsCard(
                            totalPrice = editableTotalPrice,
                            paidAmount = editablePaidAmount,
                            pendingAmount = editablePendingAmount,
                            paymentStatus = editablePaymentStatus,
                            onTotalPriceChange = onTotalPriceChange,
                            onPaidAmountChange = onPaidAmountChange,
                            onPendingAmountChange = onPendingAmountChange,
                            onPaymentStatusChange = onPaymentStatusChange
                        )
                        
                        // Save Changes Button
                        Button(
                            onClick = onSaveChanges,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            enabled = !isSaving,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            ),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            if (isSaving) {
                                CircularProgressIndicator(
                                    color = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            } else {
                                Text(
                                    text = "Save Changes",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun Header(
    onBackClick: () -> Unit,
    onReload: () -> Unit
) {
    val backgroundColor = MaterialTheme.colorScheme.background.copy(alpha = 0.8f)
    val borderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)
    val contentColor = MaterialTheme.colorScheme.onBackground
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .background(backgroundColor)
            .drawBottomBorder(borderColor)
            .padding(horizontal = 16.dp)
    ) {
        IconButton(
            onClick = onBackClick,
            modifier = Modifier
                .align(Alignment.CenterStart)
                .size(40.dp)
                .background(Color.Transparent, CircleShape)
        ) {
            Icon(
                imageVector = Icons.Rounded.ArrowBack,
                contentDescription = "Back",
                tint = contentColor
            )
        }

        Text(
            text = "STUDENT DETAILS",
            color = contentColor,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = (-0.025).em,
            modifier = Modifier.align(Alignment.Center)
        )

        IconButton(
            onClick = onReload,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .size(40.dp)
                .background(Color.Transparent, CircleShape)
        ) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = "Reload",
                tint = contentColor
            )
        }
    }
}

@Composable
private fun Modifier.drawBottomBorder(color: Color): Modifier = drawBehind {
    val strokeWidth = 1.dp.toPx()
    val y = size.height - strokeWidth / 2
    drawLine(
        color = color,
        start = Offset(0f, y),
        end = Offset(size.width, y),
        strokeWidth = strokeWidth
    )
}

@Composable
fun GradientCard(
    colors: List<Color>,
    shadowColor: Color,
    icon: ImageVector,
    title: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp, RoundedCornerShape(24.dp), ambientColor = shadowColor, spotColor = shadowColor),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(
                        colors = colors,
                        start = Offset(0f, 0f),
                        end = Offset(1000f, 1000f)
                    )
                )
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(Color.White.copy(alpha = 0.2f), CircleShape)
                            .padding(10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 20.sp
                        )
                    )
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White.copy(alpha = 0.9f), RoundedCornerShape(16.dp))
                        .padding(20.dp)
                ) {
                    content()
                }
            }
        }
    }
}

@Composable
fun EditableInfoItem(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    isLarge: Boolean = false
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium.copy(
                color = Color.Gray,
                fontWeight = FontWeight.Medium
            )
        )
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            textStyle = if (isLarge) 
                MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            else 
                MaterialTheme.typography.bodyLarge,
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedContainerColor = Color.White.copy(alpha = 0.5f),
                focusedContainerColor = Color.White
            ),
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
        )
    }
}

@Composable
fun InfoItem(
    label: String,
    value: String,
    isLarge: Boolean = false,
    letterSpacing: androidx.compose.ui.unit.TextUnit = androidx.compose.ui.unit.TextUnit.Unspecified
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium.copy(
                color = Color.Gray,
                fontWeight = FontWeight.Medium
            )
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = if (isLarge) 
                MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 18.sp
                )
            else 
                MaterialTheme.typography.bodyLarge.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                ),
            letterSpacing = letterSpacing
        )
    }
}

@Composable
fun EditablePaymentDetailsCard(
    totalPrice: String,
    paidAmount: String,
    pendingAmount: String,
    paymentStatus: String,
    onTotalPriceChange: (String) -> Unit,
    onPaidAmountChange: (String) -> Unit,
    onPendingAmountChange: (String) -> Unit,
    onPaymentStatusChange: (String) -> Unit
) {
    val cardBg = MaterialTheme.colorScheme.surface
    val borderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
    val contentColor = MaterialTheme.colorScheme.onSurface

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 1.dp,
                shape = RoundedCornerShape(20.dp)
            )
            .clip(RoundedCornerShape(20.dp))
            .background(cardBg)
            .border(1.dp, borderColor, RoundedCornerShape(20.dp))
            .padding(24.dp)
    ) {
        Column {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.padding(bottom = 24.dp)
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(PrimaryIndigo.copy(alpha = 0.1f))
                        .padding(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.AccountBalanceWallet,
                        contentDescription = null,
                        tint = PrimaryIndigo,
                        modifier = Modifier.size(24.dp)
                    )
                }
                
                Text(
                    text = "Payment Details",
                    color = contentColor,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Details List
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                EditablePaymentRow(
                    label = "Total Price",
                    value = totalPrice,
                    onValueChange = onTotalPriceChange
                )
                
                HorizontalDivider(color = borderColor, thickness = 1.dp)

                EditablePaymentRow(
                    label = "Paid Amount",
                    value = paidAmount,
                    onValueChange = onPaidAmountChange,
                    valueColor = Color(0xFF10B981) // Green
                )
                
                HorizontalDivider(color = borderColor, thickness = 1.dp)
                
                 EditablePaymentRow(
                    label = "Pending Amount",
                    value = pendingAmount,
                    onValueChange = onPendingAmountChange,
                    valueColor = Color(0xFFEF4444) // Red
                )

                 HorizontalDivider(color = borderColor, thickness = 1.dp)
                 
                 // Status Dropdown
                 Column(modifier = Modifier.fillMaxWidth()) {
                     Text(
                        text = "Status",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    var expanded by remember { mutableStateOf(false) }
                    
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = paymentStatus,
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = {
                                IconButton(onClick = { expanded = true }) {
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = "Dropdown")
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                                focusedBorderColor = MaterialTheme.colorScheme.primary
                            )
                        )
                        
                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            listOf("PARTIAL", "COMPLETED").forEach { status ->
                                DropdownMenuItem(
                                    text = { Text(status) },
                                    onClick = {
                                        onPaymentStatusChange(status)
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                 }
            }
        }
    }
}

@Composable
fun EditablePaymentRow(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    valueColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium.copy(
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            textStyle = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = valueColor
            ),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                focusedBorderColor = MaterialTheme.colorScheme.primary
            )
        )
    }
}

@Composable
fun SelectionDropdown(
    label: String,
    selectedValue: String,
    options: List<String>,
    onOptionSelected: (Int) -> Unit // Index of selected option
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = selectedValue,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = {
                IconButton(onClick = { expanded = true }) {
                    Icon(Icons.Default.ArrowDropDown, contentDescription = "Dropdown")
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant, // Adjusted for white background card if needed, but generic here
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedContainerColor = Color.White.copy(alpha = 0.9f),
                focusedContainerColor = Color.White
            )
        )
        
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.fillMaxWidth() // Ideally should match width of TextField
        ) {
            options.forEachIndexed { index, option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onOptionSelected(index)
                        expanded = false
                    }
                )
            }
        }
    }
}


@Composable
fun CenteredErrorDisplay(
    message: String,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.Warning,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(48.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onRetry) {
            Text("Retry")
        }
    }
}
