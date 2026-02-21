package com.avinashpatil.app.admindisha.navigation

import androidx.compose.animation.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.avinashpatil.app.admindisha.screens.course.CourseViewModel
import java.util.Locale

@Composable
fun BottomNavigation(
    navController: NavController,
    visible: Boolean,
    modifier: Modifier = Modifier,
    viewModel: CourseViewModel = hiltViewModel()
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(initialOffsetY = { it }),
        exit = slideOutVertically(targetOffsetY = { it })
    ) {
        NavigationBar(
            modifier = modifier,
            containerColor = Color(0xFFBAE6FD)
        ) {
            Screen.bottomNavItems.forEach { screen ->
                NavigationBarItem(
                    icon = {
                        Icon(
                            imageVector = when (screen) {
                                Screen.Home -> Icons.Default.Home
                                Screen.Topics -> Icons.AutoMirrored.Filled.MenuBook
                                Screen.Batches -> Icons.AutoMirrored.Filled.List
                                Screen.Students -> Icons.Default.Person
                                Screen.Exams -> Icons.Default.Edit // Using Edit icon for Exams for now
                                //Screen.Rank -> Icons.Default.Star
                                else -> Icons.Default.Home
                            },
                            contentDescription = screen.route
                        )
                    },
                    label = { Text(text = screen.route.replaceFirstChar {
                        if (it.isLowerCase()) it.titlecase(
                            Locale.ROOT
                        ) else it.toString()
                    }.toString()) },
                    selected = currentRoute == screen.route,
                    onClick = {
                        if (currentRoute != screen.route) {
                            if (screen == Screen.Batches) {
                                // Fetch courses when Batches is clicked
                                viewModel.fetchCourses()
                            }
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.startDestinationId) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    }
                )
            }
        }
    }
}
