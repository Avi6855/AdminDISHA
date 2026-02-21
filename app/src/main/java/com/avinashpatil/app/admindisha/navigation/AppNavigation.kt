package com.avinashpatil.app.admindisha.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.compose.animation.AnimatedContentTransitionScope
import com.avinashpatil.app.admindisha.screens.attendance.AttendanceScreen
import com.avinashpatil.app.admindisha.screens.batch.AddNewBatchScreen
import com.avinashpatil.app.admindisha.screens.batch.AllBatchScreen
import com.avinashpatil.app.admindisha.screens.bill.AllInOneBillScreenNew
//import com.avinashpatil.app.admindisha.screens.AllInOneBillScreen
import com.avinashpatil.app.admindisha.screens.course.AddNewCourseScreen
import com.avinashpatil.app.admindisha.screens.home.HomeScreen
import com.avinashpatil.app.admindisha.screens.splash.SplashScreen
import com.avinashpatil.app.admindisha.screens.ranking.RankingScreen
import com.avinashpatil.app.admindisha.screens.course.AllCourseScreen
import com.avinashpatil.app.admindisha.screens.exams.ExamScreen
import com.avinashpatil.app.admindisha.screens.dishaAI.DishaAIScreen
import com.avinashpatil.app.admindisha.screens.dishaID.SeeDISHAIDScreen
import com.avinashpatil.app.admindisha.screens.login.ModernAnimatedLoginForm
import com.avinashpatil.app.admindisha.screens.student.AllStudentScreen
import com.avinashpatil.app.admindisha.screens.student.AllStudentListScreen
import com.avinashpatil.app.admindisha.screens.student.StepUpForm
import com.avinashpatil.app.admindisha.screens.studentDetails.StudentDetailsScreen
import com.avinashpatil.app.admindisha.viewmodels.AttendanceViewModel
import com.avinashpatil.app.admindisha.viewmodels.BatchViewModel
import com.avinashpatil.app.admindisha.viewmodels.BillViewModel

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
    val showBottomBar = remember(currentRoute) {
        when (currentRoute) {
            Screen.Splash.route,
            Screen.Login.route,
            Screen.FinalExam.route, Screen.QuizResult.route,
            Screen.CCCInstructions.route, "exam_result/{examId}" -> false
            else -> true
        }
    }

    Scaffold(
        bottomBar = {
            BottomNavigation(
                navController = navController,
                visible = showBottomBar
            )
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = Screen.Splash.route,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(Screen.Splash.route) {
                SplashScreen(navController)
            }

            composable(Screen.Login.route) {
                ModernAnimatedLoginForm(navController)
            }

            composable(Screen.Home.route) {
                HomeScreen(
                    navController = navController
                )
            }

            composable(Screen.Topics.route) {
                AllCourseScreen(
                    navController = navController,
                    onCourseClick = { courseId ->
                        navController.navigate("${Screen.TopicDetails.route}/$courseId")
                    }
                )
            }

            composable(Screen.Exams.route) {
                AllCourseScreen(
                    navController = navController,
                    onCourseClick = { courseId ->
                        navController.navigate("${Screen.ExamList.route}/$courseId")
                    }
                )
            }

            composable(
                route = "${Screen.ExamList.route}/{courseId}",
                arguments = listOf(
                    navArgument("courseId") { type = NavType.StringType }
                ),
                enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left) },
                exitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right) }
            ) { backStackEntry ->
                val courseId = backStackEntry.arguments?.getString("courseId")
                ExamScreen(
                    navController = navController,
                    courseId = courseId
                )
            }

            composable(
                route = "${Screen.TopicDetails.route}/{courseId}",
                arguments = listOf(
                    navArgument("courseId") { type = NavType.StringType }
                ),
                enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left) },
                exitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right) }
            ) { backStackEntry ->
                val courseId = backStackEntry.arguments?.getString("courseId")
                // We will update TopicsScreen to accept courseId
                com.avinashpatil.app.admindisha.screens.topic.TopicsScreen(
                    navController = navController,
                    courseId = courseId
                )
            }

            composable(Screen.Rank.route) {
                RankingScreen(navController)
            }

            composable(Screen.Batches.route) {
                AllCourseScreen(
                    navController = navController,
                    onCourseClick = { courseId ->
                        navController.navigate("${Screen.AllBatch.route}?courseId=$courseId")
                    }
                )
            }

            composable(Screen.Students.route) {
                AllStudentListScreen(navController)
            }

            composable(
                route = "${Screen.StudentDetails.route}/{userId}",
                arguments = listOf(
                    navArgument("userId") { type = NavType.StringType }
                ),
                enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left) },
                exitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right) }
            ) { backStackEntry ->
                val userId = backStackEntry.arguments?.getString("userId") ?: return@composable
                StudentDetailsScreen(
                    navController = navController,
                    userId = userId
                )
            }

            composable(
                route = Screen.StepUpForm.route,
                enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left) },
                exitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right) }
            ) {
                StepUpForm(navController = navController)
            }

            composable(
                route = "edit_student/{userId}",
                arguments = listOf(
                    navArgument("userId") { type = NavType.StringType }
                ),
                enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left) },
                exitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right) }
            ) { backStackEntry ->
                val userId = backStackEntry.arguments?.getString("userId")
                StepUpForm(
                    navController = navController
                )
            }

            composable(
                route = "${Screen.Batches.route}/{batchId}",
                arguments = listOf(
                    navArgument("batchId") {
                        type = NavType.StringType
                        defaultValue = "0"
                    }
                ),
                enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left) },
                exitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right) }
            ) { backStackEntry ->
                val batchId = backStackEntry.arguments?.getString("batchId") ?: "0"
                AttendanceScreen(
                    navController = navController,
                    batchId = batchId,
                    viewModel = hiltViewModel<AttendanceViewModel>()
                )
            }

            composable(
                route = "${Screen.AllInOneBillScreen.route}/{userId}?operation={operation}",
                arguments = listOf(
                    navArgument("userId") { type = NavType.StringType },
                    navArgument("operation") {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    }
                ),
                enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left) },
                exitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right) }
            ) { backStackEntry ->
                val userId = backStackEntry.arguments?.getString("userId") ?: return@composable
                val operation = backStackEntry.arguments?.getString("operation")
                AllInOneBillScreenNew(
                    navController = navController,
                    userId = userId,
                    viewModel = hiltViewModel<BillViewModel>(),
                    //isEditing = operation == "edit"
                )
            }

            composable(
                route = Screen.DishaAI.route,
                enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left) },
                exitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right) }
            ) { backStackEntry ->
                DishaAIScreen(navController = navController)
            }

            composable(
                route = Screen.AddNewStudent.route,
                enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left) },
                exitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right) }
            ) {
                StepUpForm(navController)
            }

            composable(
                route = Screen.SeeDISHAID.route,
                enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left) },
                exitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right) }
            ) {
                SeeDISHAIDScreen(navController = navController)
            }

            composable(
                route = "${Screen.AllBatch.route}?courseId={courseId}",
                arguments = listOf(
                    navArgument("courseId") {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    }
                ),
                enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left) },
                exitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right) }
            ) { backStackEntry ->
                val courseId = backStackEntry.arguments?.getString("courseId")
                AllBatchScreen(
                    navController = navController,
                    courseId = courseId,
                    viewModel = hiltViewModel<BatchViewModel>()
                )
            }

            composable(
                route = "${Screen.AllBatch.route}/{operation}/{batchId}?courseId={courseId}",
                arguments = listOf(
                    navArgument("operation") { type = NavType.StringType },
                    navArgument("batchId") { type = NavType.StringType },
                    navArgument("courseId") {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    }
                )
            ) { backStackEntry ->
                val operation = backStackEntry.arguments?.getString("operation")
                val batchId = backStackEntry.arguments?.getString("batchId")
                val courseId = backStackEntry.arguments?.getString("courseId")
                when (operation) {
                    "edit" -> AddNewBatchScreen(
                        navController = navController,
                        courseId = courseId,
                        batchId = batchId,
                        viewModel = hiltViewModel<BatchViewModel>()
                    )
                    else -> AllBatchScreen(
                        navController = navController,
                        courseId = courseId,
                        viewModel = hiltViewModel<BatchViewModel>()
                    )
                }
            }

            composable(
                route = "${Screen.AddNewBatch.route}?courseId={courseId}&batchId={batchId}",
                arguments = listOf(
                    navArgument("courseId") {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    },
                    navArgument("batchId") {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    }
                ),
                enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left) },
                exitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right) }
            ) { backStackEntry ->
                val courseId = backStackEntry.arguments?.getString("courseId")
                val batchId = backStackEntry.arguments?.getString("batchId")
                AddNewBatchScreen(
                    navController = navController,
                    courseId = courseId,
                    batchId = batchId,
                    viewModel = hiltViewModel<BatchViewModel>()
                )
            }

            composable(
                route = Screen.AddNewCourse.route,
                enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left) },
                exitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right) }
            ) {
                AddNewCourseScreen(navController)
            }

            composable(
                route = "edit_batch/{batchId}?courseId={courseId}",
                arguments = listOf(
                    navArgument("batchId") { type = NavType.StringType },
                    navArgument("courseId") {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    }
                ),
                enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left) },
                exitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right) }
            ) { backStackEntry ->
                val batchId = backStackEntry.arguments?.getString("batchId")
                val courseId = backStackEntry.arguments?.getString("courseId")
                AddNewBatchScreen(
                    navController = navController,
                    batchId = batchId,
                    courseId = courseId,
                    viewModel = hiltViewModel<BatchViewModel>()
                )
            }

            composable(
                route = "edit_course/{courseId}",
                arguments = listOf(
                    navArgument("courseId") { type = NavType.StringType }
                ),
                enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left) },
                exitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right) }
            ) { backStackEntry ->
                val courseId = backStackEntry.arguments?.getString("courseId")
                AddNewCourseScreen(
                    navController = navController,
                    courseId = courseId
                )
            }
        }
    }
}