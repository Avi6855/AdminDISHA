package com.avinashpatil.app.admindisha.api

import com.avinashpatil.app.admindisha.data.model.ExamResult // Assuming this path is correct
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    // USER APIs
    // Note: getUserDetails was removed as it was duplicate of getUserById
    // getUsersByEnrollment was removed - use getEnrollmentsByEnrollmentId with known enrollmentId instead
    // 
    // For paidAmount data, multiple approaches are implemented:
    // 1. UserAllDetails.paidAmount (from getAllUsers API) - primary source
    // 2. Course pricing calculation (from getCourseById API) - fallback
    // 3. getUsersByCourse API - alternative source
    // 4. getEnrollmentByUserAndCourse API - NEW: get enrollment data using userId and courseId
    // 5. getEnrollmentsByEnrollmentId API - if enrollmentId is known
    // 6. Default amount for enrolled users - final fallback



    // AUTHENTICATION
    @POST("api/auth/login") // Auth login endpoint
    suspend fun login(@Body loginRequest: LoginRequest): Response<LoginResponse>

    @POST("api/auth/refresh") // Auth refresh endpoint
    suspend fun refreshToken(@Header("Authorization") refreshToken: String): Response<LoginResponse>

    @POST("api/auth/lock-user")
    suspend fun lockUser(
        @Query("userId") userId: String,
        @Query("lock") lock: Boolean,
        @Body request: LockUserRequest?
    ): Response<Unit>

    @POST("api/auth/lock-user")
    suspend fun unlockUser(
        @Query("userId") userId: String,
        @Query("lock") lock: Boolean
    ): Response<Unit>

    // BATCH - Updated to match BatchController.java
    @GET("api/batches/course/{courseId}")
    suspend fun getBatchesByCourseId(@Path("courseId") courseId: String): Response<List<Batch>> // Batch is Kotlin equivalent of BatchDto

    @POST("api/batches") // Changed endpoint, removed courseId from path
    suspend fun createBatch(@Body batch: Batch): Response<Batch> // Body is Batch (BatchDto equivalent)

    @GET("api/batches/{batchId}") // Renamed for clarity, returns full Batch details
    suspend fun getBatchDetails(@Path("batchId") batchId: String): Response<Batch> // Returns Batch (BatchDto)

    @DELETE("api/batches/{batchId}")
    suspend fun deleteBatch(@Path("batchId") batchId: String): Response<Unit>

    @PUT("api/batches/{batchId}")
    suspend fun updateBatch(
        @Path("batchId") batchId: String,
        @Body batch: Batch // Body is Batch (BatchDto equivalent)
    ): Response<Batch>

    // ATTENDANCE
    @POST("api/attendance")
    suspend fun markAttendance(@Body attendanceRequest: AttendanceRequest): Response<AttendanceResponse>

    @GET("api/batches/{batchId}")
    suspend fun getBatchAttendance(
        @Path("batchId") batchId: String
    ): Response<Batch>

    // ENROLLMENT
    // Note: Two enrollUser POST endpoints were present. Clarified one for student enrollment.
    // The original enrollUser POST("enrollments/enroll") suspend fun enrollUser(@Body enrollment: Enrollments): Response<Unit> seems general.
    // The one below is more specific. Ensure your backend matches one of these.
    @POST("api/enrollments/enroll") // Enrollment endpoint
    suspend fun enrollStudent( // Specific enrollment with payment
        @Query("studentId") studentId: String?,
        @Query("courseId") courseId: String,
        @Query("batchId") batchId: String,
        @Body paymentDto: PaymentDto
    ): Response<EnrollmentDTO>

    @POST("api/enrollments/enroll-user") // General enrollment endpoint with different path
    suspend fun enrollUser(@Body enrollment: Enrollments): Response<EnrollmentDTO>

    @PUT("api/enrollments/{enrollmentId}/update-payment") // Update payment endpoint
    suspend fun updatePayment(
        @Path("enrollmentId") enrollmentId: String,
        @Body paymentDto: PaymentDto
    ): Response<Enrollments>
    
    // Additional payment-related endpoints for better data handling
    @GET("api/payments/user/{userId}")
    suspend fun getUserPayments(@Path("userId") userId: String): Response<List<PaymentDto>>
    
    @GET("api/payments/summary/{userId}")
    suspend fun getPaymentSummary(@Path("userId") userId: String): Response<PaymentSummaryResponse>
    
    @POST("api/payments/calculate")
    suspend fun calculatePayment(@Body request: PaymentCalculationRequest): Response<PaymentCalculationResponse>


    // QUESTION - Assuming these might also be under /api/
    @GET("api/questions/practice")
    suspend fun getPracticeQuestions(): Response<List<Question>>

    @GET("api/questions/exam")
    suspend fun getExamQuestions(): Response<List<Question>>

    @GET("api/quizzes/courses/{courseId}")
    suspend fun getQuizzesByCourseId(@Path("courseId") courseId: String): Response<List<Quiz>>

    @DELETE("api/quizzes/{quizId}")
    suspend fun deleteQuiz(@Path("quizId") quizId: String): Response<Unit>

    @GET("api/leaderboard")
    suspend fun getLeaderboard(): Response<List<User>>

    @POST("api/quiz/submit")
    suspend fun submitQuizResult(@Body result: QuizResult): Response<Unit>

    @GET("api/users/{userId}/statistics")
    suspend fun getUserStatistics(@Path("userId") userId: String): Response<UserStatistics> // UserStatistics model not provided, define it

    @GET("api/exam/result/{examId}")
    suspend fun getExamResult(@Path("examId") examId: String): Response<ExamResult>

    // User Management
    @GET("api/users/all-users")
    suspend fun getAllUsers(): Response<List<UserAllDetails>>

    @GET("api/users/{userId}")
    suspend fun getUserById(@Path("userId") userId: String): Response<UserDetailResponse>

    @POST("api/users/register")
    suspend fun createUser(@Body userDTO: UserDTO): Response<UserDTO>

    @PUT("api/users/{userId}")
    suspend fun updateUser(
        @Path("userId") userId: String,
        @Body userDTO: UserDTO
        //@Body userDetailResponse: UserDetailResponse
    ): Response<UserDetailResponse>
    
    @PUT("api/users/{userId}/lock")
    suspend fun lockUser(@Path("userId") userId: String): Response<Unit>

    @PUT("api/users/{userId}/unlock")
    suspend fun unlockUser(@Path("userId") userId: String): Response<Unit>

    @DELETE("api/users/{userId}")
    suspend fun deleteUser(@Path("userId") userId: String): Response<Unit>

    // Course
    @GET("api/courses")
    suspend fun getCourses(): Response<List<Course>>

    @GET("api/courses/{courseId}")
    suspend fun getCourseById(@Path("courseId") courseId: String): Response<Course>

    @GET("api/courses/students-count")
    suspend fun getCourseStudentCounts(): Response<List<CourseStudentCount>>

    @POST("api/courses")
    suspend fun addCourse(@Body course: Course): Response<Course>

    @PUT("api/courses/{courseId}")
    suspend fun updateCourse(
        @Path("courseId") courseId: String,
        @Body course: Course
    ): Response<Course>

    @DELETE("api/courses/{courseId}")
    suspend fun deleteCourse(@Path("courseId") courseId: String): Response<Unit>

    // Topic APIs
    @GET("api/courses/{courseId}/topics")
    suspend fun getTopicsByCourseId(@Path("courseId") courseId: String): Response<List<Topic>>

    @POST("api/topics")
    suspend fun createTopic(@Body topic: Topic): Response<Topic>

    @PUT("api/topics/{topicId}")
    suspend fun updateTopic(
        @Path("topicId") topicId: String,
        @Body topic: Topic
    ): Response<Topic>

    @DELETE("api/topics/{topicId}")
    suspend fun deleteTopic(@Path("topicId") topicId: String): Response<Unit>

    // This endpoint was POST("courses/{courseId}/batches"), which is not in BatchController.java
    @POST("api/batches")
     suspend fun addBatchToCourse(
        @Path("courseId") courseId: String,
        @Body batch: Batch
     ): Response<Course>

    // This endpoint GET("batches") to get ALL batches is not in BatchController.java
    // BatchController has GET("api/batches/course/{courseId}")
    // If you need all batches, the backend endpoint GET /api/batches needs to be implemented.
    // For now, calls to this will fail if not implemented.
    @GET("api/batches")
    suspend fun getBatches(): Response<List<Batch>>


    @GET("api/users/course/{courseId}")
    suspend fun getUsersByCourse(@Path("courseId") courseId: String): Response<List<UserAllDetails>>

    // Note: getUsersByEnrollment was removed - use getEnrollmentsByEnrollmentId with known enrollmentId instead

    @GET("api/enrollments/{enrollmentId}")
    suspend fun getEnrollmentsByEnrollmentId(@Path("enrollmentId") enrollmentId: String): Response<EnrollmentDTO>

    @GET("api/enrollments/user/{userId}/course/{courseId}")
    suspend fun getEnrollmentByUserAndCourse(
        @Path("userId") userId: String,
        @Path("courseId") courseId: String
    ): Response<EnrollmentDTO>

    @GET("api/users/batch/{batchId}")
    suspend fun getUsersByBatch(@Path("batchId") batchId: String): Response<List<UserAllDetails>>

    @POST("api/certificates/courses/{courseId}")
    suspend fun sendCertificates(@Path("courseId") courseId: String): Response<CertificateResponse>
}

