package com.avinashpatil.app.admindisha.screens.login

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.avinashpatil.app.admindisha.navigation.Screen


@Composable
fun ModernAnimatedLoginForm(
    navController: NavController,
    viewModel: LoginViewModel = hiltViewModel()
) {
    var isExpanded by remember { mutableStateOf(false) }
    var email by remember { mutableStateOf("") }
    var dishaId by remember { mutableStateOf("") }
    var dishaIdVisible by remember { mutableStateOf(false) }
    
    val loginState by viewModel.loginState.collectAsState()
    val deviceName by viewModel.deviceName.collectAsState()
    
    LaunchedEffect(loginState) {
        if (loginState is LoginState.Success) {
            navController.navigate(Screen.Home.route) {
                popUpTo(Screen.Login.route) { inclusive = true }
            }
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "rotation")
    val rotation = infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    val boxWidth by animateFloatAsState(
        targetValue = if (isExpanded) 350f else 280f,
        animationSpec = tween(500),
        label = "width"
    )

    val boxHeight by animateFloatAsState(
        targetValue = if (isExpanded) 400f else 120f,
        animationSpec = tween(500),
        label = "height"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()                          
            .background(Color(0xFF1A1A1A))
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .width(boxWidth.dp)
                .height(boxHeight.dp)
                .clip(RoundedCornerShape(16.dp))
                .clickable { isExpanded = !isExpanded }
                .drawBehind {
                    rotate(rotation.value) {
                        drawCircle(
                            brush = Brush.sweepGradient(
                                colors = listOf(
                                    Color(0xFFFF2770),
                                    Color(0xFFFF2770).copy(alpha = 0.6f),
                                    Color(0xFFFF2770).copy(alpha = 0.3f),
                                    Color.Transparent,
                                    Color.Transparent
                                )
                            ),
                            radius = size.maxDimension
                        )
                    }
                    rotate(-rotation.value) {
                        drawCircle(
                            brush = Brush.sweepGradient(
                                colors = listOf(
                                    Color(0xFF45F3FF),
                                    Color(0xFF45F3FF).copy(alpha = 0.6f),
                                    Color(0xFF45F3FF).copy(alpha = 0.3f),
                                    Color.Transparent,
                                    Color.Transparent
                                )
                            ),
                            radius = size.maxDimension
                        )
                    }
                }
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(3.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFF2A2A2A))
                    .padding(8.dp)
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(if (isExpanded) 32.dp else 16.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0x33000000))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    Text(
                        text = "⮕ LOGIN ❤",
                        color = Color(0xFFFF2770),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(vertical = if (isExpanded) 0.dp else 8.dp)
                    )
                    if (isExpanded) {
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            placeholder = { Text("Email ID", color = Color(0xFFBBBBBB)) },
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedTextColor = Color.White,
                                focusedTextColor = Color.White,
                                unfocusedContainerColor = Color(0x1A000000),
                                focusedContainerColor = Color(0x1A000000),
                                unfocusedBorderColor = Color(0xFF666666),
                                focusedBorderColor = Color(0xFF45F3FF),
                                cursorColor = Color(0xFF45F3FF)
                            ),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Email,
                                imeAction = ImeAction.Next
                            ),
                            modifier = Modifier
                                .fillMaxWidth(0.85f)
                                .height(52.dp)
                        )

                        OutlinedTextField(
                            value = dishaId,
                            onValueChange = { dishaId = it },
                            placeholder = { Text("DISHA ID", color = Color(0xFFBBBBBB)) },
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedTextColor = Color.White,
                                focusedTextColor = Color.White,
                                unfocusedContainerColor = Color(0x1A000000),
                                focusedContainerColor = Color(0x1A000000),
                                unfocusedBorderColor = Color(0xFF666666),
                                focusedBorderColor = Color(0xFF45F3FF),
                                cursorColor = Color(0xFF45F3FF)
                            ),
                            visualTransformation = if (dishaIdVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            trailingIcon = {
                                IconButton(
                                    onClick = { dishaIdVisible = !dishaIdVisible }
                                ) {
                                    Icon(
                                        imageVector = if (dishaIdVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                        contentDescription = if (dishaIdVisible) "Hide DISHA ID" else "Show DISHA ID",
                                        tint = Color(0xFF45F3FF)
                                    )
                                }
                            },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Done
                            ),
                            modifier = Modifier
                                .fillMaxWidth(0.85f)
                                .height(52.dp)
                        )
                        /*
                        OutlinedTextField(
                            value = deviceName,
                            onValueChange = { },
                            placeholder = { Text("Device Name", color = Color(0xFFBBBBBB)) },
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedTextColor = Color.White,
                                focusedTextColor = Color.White,
                                unfocusedContainerColor = Color(0x1A000000),
                                focusedContainerColor = Color(0x1A000000),
                                unfocusedBorderColor = Color(0xFF666666),
                                focusedBorderColor = Color(0xFF45F3FF),
                                cursorColor = Color(0xFF45F3FF)
                            ),
                            enabled = false,
                            modifier = Modifier
                                .fillMaxWidth(0.85f)
                                .height(52.dp)
                        )


                         */
                        when (loginState) {
                            is LoginState.Error -> {
                                Text(
                                    text = (loginState as LoginState.Error).message,
                                    color = Color(0xFFFF2770),
                                    fontSize = 14.sp,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier
                                        .fillMaxWidth(0.85f)
                                        .padding(bottom = 8.dp)
                                )
                            }
                            is LoginState.Loading -> {
                                CircularProgressIndicator(
                                    color = Color(0xFF45F3FF),
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            else -> {}
                        }

                        Button(
                            onClick = { 
                                if (email.trim().isEmpty() || dishaId.trim().isEmpty()) {
                                    return@Button
                                }
                                viewModel.login(email.trim(), dishaId.trim())
                            },
                            enabled = email.trim().isNotEmpty() && dishaId.trim().isNotEmpty() && loginState !is LoginState.Loading,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF45F3FF)
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth(0.85f)
                                .height(48.dp)
                        ) {
                            if (loginState is LoginState.Loading) {
                                CircularProgressIndicator(
                                    color = Color(0xFF111111),
                                    modifier = Modifier.size(24.dp)
                                )
                            } else {
                                Text(
                                    text = "LOGIN",
                                    color = Color(0xFF111111),
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        
                        if (loginState is LoginState.Error) {
                            Text(
                                text = (loginState as LoginState.Error).message,
                                color = Color(0xFFFF2770),
                                fontSize = 14.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .fillMaxWidth(0.85f)
                                    .padding(top = 8.dp)
                            )
                        }
                        }
                    }
                }
            }
        }
    }

// Compose Preview
@Preview(showBackground = true)
@Composable
fun PreviewModernAnimatedLoginForm() {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFF25252B)
    ) {
        ModernAnimatedLoginForm(
            navController = rememberNavController()
        )
    }
}