package com.avinashpatil.app.admindisha.screens.splash

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.avinashpatil.app.admindisha.R
import com.avinashpatil.app.admindisha.navigation.Screen
import com.avinashpatil.app.admindisha.screens.login.LoginViewModel
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    navController: NavController,
    viewModel: LoginViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    var visible by remember { mutableStateOf(false) }
    var startAnimation by remember { mutableStateOf(false) }
    var showLockedDialog by remember { mutableStateOf(false) }
    
    val scale by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0.3f,
        animationSpec = tween(800),
        label = "scale"
    )

    LaunchedEffect(Unit) {
        visible = true
        startAnimation = true
        delay(3000)
        
        // Check if user is locked from UserPreferences
        val isUserLocked = viewModel.isUserLocked()
        if (isUserLocked) {
            showLockedDialog = true
        } else {
            navController.navigate(Screen.Login.route) {
                popUpTo(Screen.Splash.route) { inclusive = true }
            }
        }
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(animationSpec = tween(800))
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.splash_screen),
                    contentDescription = "App Logo",
                    modifier = Modifier
                        .size(120.dp)
                        .scale(scale)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "DISHA COMPUTER EDUCATION HALADI",
                    style = MaterialTheme.typography.headlineMedium,
                    textAlign = TextAlign.Center,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.alpha(if (startAnimation) 1f else 0f)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Design & Developed By: \nAvinash Rajendra Patil",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        color = Color.Black
                    )

                    Text(
                        text = "Senior Android Developer",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center,
                        color = Color.Black
                    )
                }
            }
        }
        
        // Locked User Dialog
        if (showLockedDialog) {
            AlertDialog(
                onDismissRequest = { 
                    showLockedDialog = false
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                },
                title = { Text("Account Locked") },
                text = { 
                    Text(
                        "Your account is locked. However, you should contact the branch.\n\nYour fees may be pending.",
                        textAlign = TextAlign.Center
                    ) 
                },
                confirmButton = {
                    Button(onClick = {
                        showLockedDialog = false
                        navController.navigate(Screen.Login.route) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                        }
                    }) {
                        Text("OK")
                    }
                }
            )
        }
    }
}
