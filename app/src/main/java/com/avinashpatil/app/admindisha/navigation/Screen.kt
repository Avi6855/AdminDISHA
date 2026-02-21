package com.avinashpatil.app.admindisha.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Login : Screen("login")
    object Home : Screen("home")
    object Quiz : Screen("quiz")
    object PracticeQuiz : Screen("practice_quiz")
    object CCCInstructions : Screen("ccc_instructions")
    object FinalExam : Screen("final_exam")
    object QuizResult : Screen("quiz_result")
    object Rank : Screen("rank")
    object Batches : Screen("batches")
    object Students : Screen("students")
    object AddNewStudent : Screen("add_new_student")
    object AllStudent : Screen("all_student")
    object AddNewBatch : Screen("add_new_batch")
    object AllBatch : Screen("all_batch")
    object AddNewCourse : Screen("add_new_course")
    object AllCourse : Screen("all_course")
    object DishaAI : Screen("disha_ai")
    object SeeDISHAID : Screen("see_disha_id")
    object AddNewProduct : Screen("add_new_product")
    object SellingProducts : Screen("selling_products")
    object AllInOneBill : Screen("all_in_one_bill")
    object StepUpForm : Screen("step_up_form")
    object AllInOneBillScreen : Screen("all_in_one_bill_screen")
    object Topics : Screen("topics")
    object Exams : Screen("exams")
    object ExamList : Screen("exam_list")
    object TopicDetails : Screen("topic_details")
    object StudentDetails : Screen("student_details")

    companion object {
        val bottomNavItems = listOf(
            Home,
            Topics,
            Batches,
            Students,
            Exams
        )
    }
}
