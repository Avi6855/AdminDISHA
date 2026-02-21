package com.avinashpatil.app.admindisha.api

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

// Custom serializers for java.time types
object LocalDateSerializer : KSerializer<LocalDate> {
    private val formatter = DateTimeFormatter.ISO_LOCAL_DATE
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("LocalDate", PrimitiveKind.STRING)
    override fun serialize(encoder: Encoder, value: LocalDate) = encoder.encodeString(value.format(formatter))
    override fun deserialize(decoder: Decoder): LocalDate = LocalDate.parse(decoder.decodeString(), formatter)
}

object LocalTimeSerializer : KSerializer<LocalTime> {
    private val formatter = DateTimeFormatter.ISO_LOCAL_TIME
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("LocalTime", PrimitiveKind.STRING)
    override fun serialize(encoder: Encoder, value: LocalTime) = encoder.encodeString(value.format(formatter))
    override fun deserialize(decoder: Decoder): LocalTime = LocalTime.parse(decoder.decodeString(), formatter)
}

object LocalDateTimeSerializer : KSerializer<LocalDateTime> {
    private val formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("LocalDateTime", PrimitiveKind.STRING)
    override fun serialize(encoder: Encoder, value: LocalDateTime) = encoder.encodeString(value.format(formatter))
    override fun deserialize(decoder: Decoder): LocalDateTime = LocalDateTime.parse(decoder.decodeString(), formatter)
}

// Course and Batch related models

@Serializable
@Parcelize
data class Course(
    val courseId: String,
    val courseName: String,
    val courseDescription: String?,
    val courseDuration: String?,
    val coursePrice: String?,
    val courseOfferPrice: String?,
    val batches: List<Batch>? = null
) : Parcelable

@Serializable
@Parcelize
data class CourseStudentCount(
    val courseName: String,
    val totalUsersCount: Long
) : Parcelable

@Serializable
@Parcelize
data class Topic(
    val topicId: String? = null,
    val topicName: String,
    val courseId: String,
    val message: String? = null
) : Parcelable

@Serializable
@Parcelize
data class Batch( // Corresponds to BatchDto.java
    val batchId: String,
    val batchNo: String,
    @Serializable(with = LocalDateSerializer::class)
    val startDate: LocalDate?,
    @Serializable(with = LocalDateSerializer::class)
    val endDate: LocalDate?,
    @Serializable(with = LocalTimeSerializer::class)
    val timing: LocalTime?,
    val courseId: String, // Ensure this is present and used when creating a batch
    val studentIds: List<String>? = null,
    val isActive: Boolean = true, // This field is not in BatchDto.java, but can be kept if used client-side
    val batchUsersList: BatchUsersList? = null // Matches BatchDto.java
) : Parcelable

@Serializable
@Parcelize
data class BatchUsersList( // Corresponds to BatchUsersList.java
    val batchId: String,
    val batchUsers: List<BatchUser>
) : Parcelable

@Serializable
@Parcelize
data class BatchUser( // Corresponds to BatchUser.java
    val fullName: String // Only fullName as per BatchUser.java
    // Removed userId, email, phone to match backend
) : Parcelable

@Serializable
@Parcelize
data class CertificateResponse(
    val jobId: String,
    val courseId: String,
    val status: String,
    val startTime: String,
    val message: String
) : Parcelable

// Attendance models

@Serializable
@Parcelize
data class Attendance(
    val attendanceId: String,
    val batchId: String,
    val userId: String,
    @Serializable(with = LocalDateSerializer::class)
    val date: LocalDate,
    val isPresent: Boolean,
    val remarks: String? = null
) : Parcelable

@Serializable
@Parcelize
data class AttendanceRequest(
    val batchId: String,
    val userId: String,
    val date: String,
    val isPresent: Boolean,
    val remarks: String? = null
) : Parcelable

@Serializable
@Parcelize
data class AttendanceResponse(
    val attendanceId: String,
    val userId: String,
    val studentId: String, // Note: Spring DTOs don't show studentId here, verify if needed
    @Serializable(with = LocalDateSerializer::class)
    val date: LocalDate,
    val isPresent: Boolean,
    val remarks: String? = null
) : Parcelable

// Certificates and Exams

@Serializable
@Parcelize
data class Certificate(
    val certificateId: String,
    val certificateNumber: String,
    @Serializable(with = LocalDateSerializer::class)
    val issueDate: LocalDate,
    val userId: String,
    val courseId: String,
    val batchId: String
) : Parcelable

@Serializable
@Parcelize
data class Exam(
    val examId: String,
    val title: String,
    @Serializable(with = LocalDateTimeSerializer::class)
    val scheduledDateTime: LocalDateTime,
    val durationMinutes: Int,
    val isSample: Boolean,
    val batchId: String,
    val questionIds: List<String>
) : Parcelable

// Questions and Quiz

@Serializable
@Parcelize
data class Question(
    val questionId: String,
    val questionEnglish: String,
    val questionMarathi1: String,
    val option1English: String,
    val option1Marathi: String,
    val option2English: String,
    val option2Marathi: String,
    val option3English: String,
    val option3Marathi: String,
    val option4English: String,
    val option4Marathi: String,
    val correctOption: Int,
    val marks: Int,
    val quizId: String
) : Parcelable

@Serializable
@Parcelize
data class QuestionResult(
    val questionId: Long,
    val questionEn: String,
    val questionMr: String,
    val correctAnswer: String
) : Parcelable

@Serializable
@Parcelize
data class Quiz(
    val quizId: String,
    val title: String,
    val durationMinutes: Int,
    @Serializable(with = LocalDateTimeSerializer::class)
    val scheduledMinutes: LocalDateTime?, // Corrected from scheduledMinutes to scheduledDateTime if it's a date-time
    val isSample: Boolean,
    val batchId: String,
    val questions: List<Question>
) : Parcelable

@Serializable
@Parcelize
data class QuizResult(
    val totalQuestions: Int,
    val correctAnswers: Int,
    val incorrectAnswers: Int,
    val questionResults: List<QuestionResult>
) : Parcelable

// Authentication models

@Serializable
@Parcelize
data class LoginRequest(
    val email: String,
    val dishaId: String,
    val deviceName: String
) : Parcelable

@Serializable
data class LockUserRequest(
    val refreshToken: String
)

@Serializable
@Parcelize
data class LoginResponse(
    val success: Boolean,
    val message: String,
    val user: User?, // Make sure User model matches backend User entity/DTO for auth
    val token: String?,
    val refreshToken: RefreshToken?
) : Parcelable

@Serializable
@Parcelize
data class RefreshToken(
    val id: Int?,
    val token: String,
    val expiryDate: String? = null
) : Parcelable

// Roles

@Serializable
@Parcelize
data class Role(
    val roleId: String,
    val name: String
) : Parcelable

// Payments and Enrollments

// Consolidated Payment Data Class - Single source of truth for payment data
@Serializable
@Parcelize
data class PaymentDto(
    val paymentId: String? = null,
    val paidAmount: Double,
    val paymentMode: String,
    val paymentReference: String? = null,
    val paymentDate: String = LocalDate.now().toString(),
    val status: String = "PAID"
) : Parcelable {
    
    // Helper function to get formatted amount
    fun getFormattedAmount(): String {
        return "₹${String.format("%.2f", paidAmount)}"
    }
    
    // Helper function to validate payment data
    fun isValid(): Boolean {
        return paidAmount > 0.0 && paymentMode.isNotBlank()
    }
}

@Serializable
@Parcelize
data class EnrollmentDTO( // Verify this against backend DTO for enrollments
    val enrollmentId: String? = null,
    val enrollmentDateTime: String? = null,
    val userName: String? = null,
    val email: String? = null,
    val courseName: String? = null,
    val batchName: String? = null,
    val finalPrice: String? = null,
    val paidAmount: Double? = null, // API returns as String, convert to Double when needed
    val paymentStatus: String? = null
) : Parcelable {
    // Helper function to get paidAmount as Double
    fun getPaidAmountAsDouble(): Double {
        return paidAmount?: 0.0
    }
}

// User models

@Serializable
@Parcelize
data class User( // General User model, verify against backend User DTO
    val userId: String,
    val fullName: String?,
    val dishaId: String,
    val email: String?,
    val role: String?,
    val activeSessionToken: String?,
    val lastLoginTime: String?, // Consider consistent date/time type e.g. LocalDateTime or String
    val courseIds: List<String>?,
    val batchIds: List<String>?,
    val certificateIds: List<String>?,
    val enrollments: List<EnrollmentDetails> = emptyList(), // Consider a more specific type than Any if possible
    val isLocked: Boolean = false
) : Parcelable

@Serializable
@Parcelize
data class Enrollments( // Specific Enrollments model, verify against backend
    val enrollmentId: String,
    val enrollmentDateTime: String, // Consider LocalDateTime or a formatted String pattern
    val userName: String,
    val email: String,
    val courseName: String?,
    val batchName: String?,
    val finalPrice: String?, // Consider numeric type if appropriate
    val paidAmount: Double?, // API returns as String, convert to Double when needed
    val paymentStatus: String
) : Parcelable {
    // Helper function to get paidAmount as Double
    fun getPaidAmountAsDouble(): Double {
        return paidAmount ?: 0.0
    }
}

@Serializable
@Parcelize
data class UserAllDetails(
    val userId: String,
    val dishaId: String,
    val email: String?,
    val fullName: String?,
    val role: String?,
    val userName: String?,
    val enrollmentId: String? = null,
    val enrollmentDateTime: String? = null,
    val courseId: String? = null,
    val courseName: String? = null,
    val courseDescription: String? = null,
    val courseDuration: String? = null,
    val coursePrice: Double? = null,
    val courseOfferPrice: Double? = null,
    val batchId: String? = null,
    val batchName: String? = null,
    val batchNo: String? = null,
    val startDate: String? = null,
    val endDate: String? = null,
    val timing: String? = null,
    val finalPrice: String? = null,
    val paidAmount: String? = null,
    val pendingAmount: String? = null,
    val paymentStatus: String? = null,
    val deviceName: String? = null,
    @SerialName("lock")
    val isLocked: Boolean = false,
    val courseIds: List<String>? = null,
    val batchIds: List<String>? = null,
    val certificateIds: List<String>? = null,
    // Maintaining compatibility fields if they are still needed internally, 
    // or mapping from the new fields if logic requires them
    val activeSessionToken: String? = null,
    val lastLoginTime: String? = null,
    val enrollments: List<EnrollmentDetails>? = emptyList() // Keeping this for now to avoid breaking other code immediately, but it might need to be populated from the flat structure if needed
) : Parcelable {
    // Helper function to get paidAmount as Double
    fun getPaidAmountAsDouble(): Double {
        return paidAmount?.toDoubleOrNull() ?: 0.0
    }
}

@Serializable
@Parcelize
data class EnrollmentDetails(
    val enrolledCourse: String,
    val assignedBatch: String
) : Parcelable

@Serializable
@Parcelize
data class UserDTO( // User DTO for create/update operations, verify with backend
    val userId: String? = null,
    val dishaId: String? = null,
    val email: String,
    val fullName: String,
    val role: String? = null,
    val activeSessionToken: String? = null,
    val lastLoginTime: String? = null,
    val courseIds: List<String>? = null,
    val batchIds: List<String>? = null,
    val certificateIds: List<String>? = null,
    val paidAmount: Double? = 0.0, // Added to support paidAmount in PUT requests
    val finalPrice: String? = null, // Added to support finalPrice in PUT requests
    val paymentStatus: String? = null, // Added to support paymentStatus in PUT requests
    val isLocked: Boolean = false
) : Parcelable

@Serializable
@Parcelize
data class UserDetailResponse(
    val userId: String,
    val dishaId: String,
    val email: String,
    val fullName: String,
    val role: String,
    val enrollmentId: String? = null,
    val enrollmentDateTime: String? = null,
    val userName: String? = null,
    val courseId: String? = null,
    val courseName: String? = null,
    val courseDescription: String? = null,
    val courseDuration: String? = null,
    val coursePrice: Double? = null,
    val courseOfferPrice: Double? = null,
    val batchId: String? = null,
    val batchName: String? = null,
    val batchNo: String? = null,
    val startDate: String? = null,
    val endDate: String? = null,
    val timing: String? = null,
    val finalPrice: String? = null,
    val paidAmount: String? = null,
    val paymentStatus: String? = null,
    val deviceName: String? = null,
    val lock: Boolean = false,
    val courseIds: List<String>? = null,
    val batchIds: List<String>? = null,
    val certificateIds: List<String>? = null
) : Parcelable



@Serializable
@Parcelize
data class UserCourseDetail( // User detail specific to a course
    val userId: String,
    val userName: String?,
    val dishaId: String
) : Parcelable

// Exam Result model from your ApiService.kt
@Serializable
@Parcelize
data class ExamResult(
    val exampleProperty: String // Placeholder, define actual properties
) : Parcelable

// Enhanced Payment Models for comprehensive payment handling
@Serializable
@Parcelize
data class PaymentSummaryResponse(
    val userId: String,
    val totalPaidAmount: Double,
    val totalDueAmount: Double,
    val paymentCount: Int,
    val lastPaymentDate: String?,
    val paymentStatus: String,
    val payments: List<PaymentDto>
) : Parcelable {
    // Helper function to get formatted total paid amount
    fun getFormattedTotalPaid(): String {
        return "₹${String.format("%.2f", totalPaidAmount)}"
    }
    
    // Helper function to calculate total from payments list
    fun calculateTotalFromPayments(): Double {
        return payments.sumOf { it.paidAmount }
    }
}

@Serializable
@Parcelize
data class PaymentCalculationRequest(
    val userId: String,
    val enrollmentIds: List<String>? = null,
    val includePartialPayments: Boolean = true
) : Parcelable

@Serializable
@Parcelize
data class PaymentCalculationResponse(
    val userId: String,
    val calculatedPaidAmount: Double,
    val enrollmentBreakdown: List<EnrollmentPaymentBreakdown>,
    val calculationTimestamp: String,
    val isAccurate: Boolean = true
) : Parcelable {
    // Helper function to get formatted calculated amount
    fun getFormattedCalculatedAmount(): String {
        return "₹${String.format("%.2f", calculatedPaidAmount)}"
    }
}

@Serializable
@Parcelize
data class EnrollmentPaymentBreakdown(
    val enrollmentId: String,
    val courseName: String?,
    val batchName: String?,
    val totalPaid: Double,
    val paymentCount: Int,
    val lastPaymentDate: String?
) : Parcelable

// Enhanced PaymentDto with additional optional fields for comprehensive payment tracking
@Serializable
@Parcelize
data class PaymentDtoEnhanced(
    val paymentId: String? = null,
    val userId: String? = null,
    val enrollmentId: String? = null,
    val paidAmount: Double,
    val paymentMode: String,
    val paymentReference: String? = null,
    val paymentDate: String = LocalDate.now().toString(),
    val status: String = "PAID",
    val courseName: String? = null,
    val batchName: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null
) : Parcelable {
    // Helper function to get formatted amount
    fun getFormattedAmount(): String {
        return "₹${String.format("%.2f", paidAmount)}"
    }
    
    // Convert to basic PaymentDto
    fun toPaymentDto(): PaymentDto {
        return PaymentDto(
            paymentId = paymentId,
            paidAmount = paidAmount,
            paymentMode = paymentMode,
            paymentReference = paymentReference,
            paymentDate = paymentDate,
            status = status
        )
    }
    
    // Helper function to check if payment is successful
    fun isSuccessful(): Boolean {
        return status.equals("PAID", ignoreCase = true) || 
               status.equals("COMPLETED", ignoreCase = true)
    }
}
