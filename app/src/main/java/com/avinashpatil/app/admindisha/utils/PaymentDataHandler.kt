package com.avinashpatil.app.admindisha.utils

import com.avinashpatil.app.admindisha.api.ApiService
import com.avinashpatil.app.admindisha.api.EnrollmentDetails
import com.avinashpatil.app.admindisha.api.PaymentDto
import com.avinashpatil.app.admindisha.api.UserAllDetails
import kotlinx.coroutines.delay
import retrofit2.HttpException
import java.io.IOException

/**
 * Utility class for handling payment data fetching with proper error handling and retry mechanisms
 */
class PaymentDataHandler(private val apiService: ApiService) {
    
    companion object {
        private const val MAX_RETRY_ATTEMPTS = 3
        private const val RETRY_DELAY_MS = 1000L
        private const val DEFAULT_FALLBACK_AMOUNT = 0.0
    }
    
    /**
     * Sealed class to represent different payment fetch results
     */
    sealed class PaymentFetchResult {
        data class Success(val amount: Double, val source: String) : PaymentFetchResult()
        data class Error(val message: String, val fallbackAmount: Double = DEFAULT_FALLBACK_AMOUNT) : PaymentFetchResult()
    }
    
    /**
     * Comprehensive method to fetch paidAmount with proper error handling
     */
    suspend fun fetchPaidAmountWithErrorHandling(
        userId: String,
        userDetails: UserAllDetails? = null,
        enrollmentDetails: List<EnrollmentDetails> = emptyList()
    ): PaymentFetchResult {
        println("PaymentDataHandler: Starting comprehensive paidAmount fetch for user: $userId")
        
        // Strategy 1: Use provided UserAllDetails if available
        userDetails?.let { user ->
            val userPaidAmount = user.getPaidAmountAsDouble()
            if (userPaidAmount > 0.0) {
                println("PaymentDataHandler: Success from UserAllDetails: $userPaidAmount")
                return PaymentFetchResult.Success(userPaidAmount, "UserAllDetails")
            }
        }
        
        // Strategy 2: Try to fetch from user payments API
        try {
            val paymentResult = fetchFromUserPaymentsAPI(userId)
            if (paymentResult is PaymentFetchResult.Success) {
                return paymentResult
            }
        } catch (e: Exception) {
            println("PaymentDataHandler: Error fetching from user payments API: ${e.message}")
        }
        
        // Strategy 3: Try to fetch from enrollment API
        if (enrollmentDetails.isNotEmpty()) {
            try {
                val enrollmentResult = fetchFromEnrollmentAPI(userId, enrollmentDetails.first())
                if (enrollmentResult is PaymentFetchResult.Success) {
                    return enrollmentResult
                }
            } catch (e: Exception) {
                println("PaymentDataHandler: Error fetching from enrollment API: ${e.message}")
            }
        }
        
        // Strategy 4: Calculate from course pricing
        if (enrollmentDetails.isNotEmpty()) {
            try {
                val coursePricingResult = calculateFromCoursePricing(enrollmentDetails)
                if (coursePricingResult is PaymentFetchResult.Success) {
                    return coursePricingResult
                }
            } catch (e: Exception) {
                println("PaymentDataHandler: Error calculating from course pricing: ${e.message}")
            }
        }
        
        // Strategy 5: Try payment summary API
        try {
            val summaryResult = fetchFromPaymentSummaryAPI(userId)
            if (summaryResult is PaymentFetchResult.Success) {
                return summaryResult
            }
        } catch (e: Exception) {
            println("PaymentDataHandler: Error fetching from payment summary API: ${e.message}")
        }
        
        // All strategies failed
        println("PaymentDataHandler: All strategies failed for user: $userId")
        return PaymentFetchResult.Error(
            "Unable to fetch payment data from any source",
            DEFAULT_FALLBACK_AMOUNT
        )
    }
    
    /**
     * Fetch payment data from user payments API with retry mechanism
     */
    private suspend fun fetchFromUserPaymentsAPI(userId: String): PaymentFetchResult {
        return executeWithRetry("UserPaymentsAPI") {
            val response = apiService.getUserPayments(userId)
            if (response.isSuccessful) {
                val payments = response.body() ?: emptyList()
                val totalPaid = payments.sumOf { it.paidAmount }
                if (totalPaid > 0.0) {
                    PaymentFetchResult.Success(totalPaid, "UserPaymentsAPI")
                } else {
                    PaymentFetchResult.Error("No payments found in user payments API")
                }
            } else {
                PaymentFetchResult.Error("User payments API failed: ${response.code()}")
            }
        }
    }
    
    /**
     * Fetch payment data from enrollment API with retry mechanism
     */
    private suspend fun fetchFromEnrollmentAPI(userId: String, enrollment: EnrollmentDetails): PaymentFetchResult {
        return executeWithRetry("EnrollmentAPI") {
            val response = apiService.getEnrollmentByUserAndCourse(userId, enrollment.enrolledCourse)
            if (response.isSuccessful) {
                val enrollmentData = response.body()
                val paidAmount = enrollmentData?.getPaidAmountAsDouble() ?: 0.0
                if (paidAmount > 0.0) {
                    PaymentFetchResult.Success(paidAmount, "EnrollmentAPI")
                } else {
                    PaymentFetchResult.Error("No payment data in enrollment API")
                }
            } else {
                PaymentFetchResult.Error("Enrollment API failed: ${response.code()}")
            }
        }
    }
    
    /**
     * Calculate payment from course pricing with error handling
     */
    private suspend fun calculateFromCoursePricing(enrollmentDetails: List<EnrollmentDetails>): PaymentFetchResult {
        return executeWithRetry("CoursePricing") {
            val enrolledCourses = enrollmentDetails.map { it.enrolledCourse }.distinct()
            var totalAmount = 0.0
            var successfulFetches = 0
            
            for (courseId in enrolledCourses) {
                try {
                    val courseResponse = apiService.getCourseById(courseId)
                    if (courseResponse.isSuccessful) {
                        val course = courseResponse.body()
                        val courseOfferPrice = course?.courseOfferPrice?.toDoubleOrNull() ?: 0.0
                        val coursePrice = course?.coursePrice?.toDoubleOrNull() ?: 0.0
                        val priceToUse = if (courseOfferPrice > 0.0) courseOfferPrice else coursePrice
                        
                        if (priceToUse > 0.0) {
                            totalAmount += priceToUse
                            successfulFetches++
                        }
                    }
                } catch (e: Exception) {
                    println("PaymentDataHandler: Error fetching course $courseId: ${e.message}")
                }
            }
            
            if (totalAmount > 0.0 && successfulFetches > 0) {
                PaymentFetchResult.Success(totalAmount, "CoursePricing")
            } else {
                PaymentFetchResult.Error("Unable to calculate from course pricing")
            }
        }
    }
    
    /**
     * Fetch payment data from payment summary API
     */
    private suspend fun fetchFromPaymentSummaryAPI(userId: String): PaymentFetchResult {
        return executeWithRetry("PaymentSummaryAPI") {
            val response = apiService.getPaymentSummary(userId)
            if (response.isSuccessful) {
                val summary = response.body()
                val totalPaid = summary?.totalPaidAmount ?: 0.0
                if (totalPaid > 0.0) {
                    PaymentFetchResult.Success(totalPaid, "PaymentSummaryAPI")
                } else {
                    PaymentFetchResult.Error("No payment data in summary API")
                }
            } else {
                PaymentFetchResult.Error("Payment summary API failed: ${response.code()}")
            }
        }
    }
    
    /**
     * Execute a suspend function with retry mechanism and proper error handling
     */
    private suspend fun <T> executeWithRetry(
        operationName: String,
        operation: suspend () -> T
    ): T {
        var lastException: Exception? = null
        
        repeat(MAX_RETRY_ATTEMPTS) { attempt ->
            try {
                println("PaymentDataHandler: Attempting $operationName (attempt ${attempt + 1}/$MAX_RETRY_ATTEMPTS)")
                return operation()
            } catch (e: IOException) {
                println("PaymentDataHandler: Network error in $operationName: ${e.message}")
                lastException = e
                if (attempt < MAX_RETRY_ATTEMPTS - 1) {
                    delay(RETRY_DELAY_MS * (attempt + 1)) // Exponential backoff
                }
            } catch (e: HttpException) {
                println("PaymentDataHandler: HTTP error in $operationName: ${e.code()} - ${e.message()}")
                lastException = e
                if (e.code() in 500..599 && attempt < MAX_RETRY_ATTEMPTS - 1) {
                    delay(RETRY_DELAY_MS * (attempt + 1)) // Retry on server errors
                } else {
                    return@repeat // Don't retry on client errors (4xx)
                }
            } catch (e: Exception) {
                println("PaymentDataHandler: Unexpected error in $operationName: ${e.message}")
                lastException = e
                return@repeat // Don't retry on unexpected errors
            }
        }
        
        throw lastException ?: Exception("Unknown error in $operationName")
    }
    
    /**
     * Validate PaymentDto data before processing
     */
    fun validatePaymentDto(paymentDto: PaymentDto): ValidationResult {
        val errors = mutableListOf<String>()
        
        if (paymentDto.paidAmount <= 0.0) {
            errors.add("Paid amount must be greater than 0")
        }
        
        if (paymentDto.paymentMode.isBlank()) {
            errors.add("Payment mode cannot be blank")
        }
        
        if (paymentDto.status.isBlank()) {
            errors.add("Payment status cannot be blank")
        }
        
        return if (errors.isEmpty()) {
            ValidationResult.Valid
        } else {
            ValidationResult.Invalid(errors)
        }
    }
    
    /**
     * Sealed class for validation results
     */
    sealed class ValidationResult {
        object Valid : ValidationResult()
        data class Invalid(val errors: List<String>) : ValidationResult()
    }
}
