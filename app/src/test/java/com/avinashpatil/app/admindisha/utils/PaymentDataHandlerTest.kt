package com.avinashpatil.app.admindisha.utils

import com.avinashpatil.app.admindisha.api.ApiService
import com.avinashpatil.app.admindisha.api.Course
import com.avinashpatil.app.admindisha.api.EnrollmentDTO
import com.avinashpatil.app.admindisha.api.EnrollmentDetails
import com.avinashpatil.app.admindisha.api.PaymentDto
import com.avinashpatil.app.admindisha.api.PaymentSummaryResponse
import com.avinashpatil.app.admindisha.api.UserAllDetails
import com.avinashpatil.app.admindisha.utils.PaymentDataHandler.PaymentFetchResult
import com.avinashpatil.app.admindisha.utils.PaymentDataHandler.ValidationResult
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Response
import java.io.IOException

/**
 * The bill screen trusts PaymentDataHandler to find the amount a student has paid, trying several
 * sources in order. These tests pin that order, the fallbacks and the retry behaviour, because a
 * silent regression here shows the wrong pending balance to the institute.
 */
class PaymentDataHandlerTest {

    private val apiService = mockk<ApiService>()
    private val handler = PaymentDataHandler(apiService)

    private fun <T> serverError(): Response<T> =
        Response.error(500, "{}".toResponseBody("application/json".toMediaType()))

    private fun user(paidAmount: String?) = UserAllDetails(
        userId = "user-1",
        dishaId = "DISHA-001",
        email = "student@example.com",
        fullName = "Student One",
        role = "STUDENT",
        userName = "student1",
        paidAmount = paidAmount
    )

    private fun payment(amount: Double) = PaymentDto(paidAmount = amount, paymentMode = "CASH")

    private fun summary(totalPaid: Double) = PaymentSummaryResponse(
        userId = "user-1",
        totalPaidAmount = totalPaid,
        totalDueAmount = 0.0,
        paymentCount = 1,
        lastPaymentDate = null,
        paymentStatus = "PAID",
        payments = emptyList()
    )

    private fun enrollment(course: String) = EnrollmentDetails(enrolledCourse = course, assignedBatch = "B-01")

    private fun course(id: String, price: String?, offerPrice: String?) = Course(
        courseId = id,
        courseName = "CCC",
        courseDescription = null,
        courseDuration = "3 months",
        coursePrice = price,
        courseOfferPrice = offerPrice
    )

    @Test
    fun `already known user details win and the api is never called`() = runTest {
        val result = handler.fetchPaidAmountWithErrorHandling(
            userId = "user-1",
            userDetails = user(paidAmount = "5000.50")
        )

        assertEquals(PaymentFetchResult.Success(5000.50, "UserAllDetails"), result)
        coVerify(exactly = 0) { apiService.getUserPayments(any()) }
        coVerify(exactly = 0) { apiService.getPaymentSummary(any()) }
        coVerify(exactly = 0) { apiService.getCourseById(any()) }
    }

    @Test
    fun `zero amount in user details falls through to the payments api`() = runTest {
        coEvery { apiService.getUserPayments("user-1") } returns
            Response.success(listOf(payment(2000.0), payment(1500.0)))

        val result = handler.fetchPaidAmountWithErrorHandling("user-1", user(paidAmount = "0.0"))

        assertEquals(PaymentFetchResult.Success(3500.0, "UserPaymentsAPI"), result)
    }

    @Test
    fun `enrollment lookup is used when the payments api has nothing`() = runTest {
        coEvery { apiService.getUserPayments("user-1") } returns Response.success(emptyList())
        coEvery { apiService.getEnrollmentByUserAndCourse("user-1", "course-1") } returns
            Response.success(EnrollmentDTO(paidAmount = 3000.0))

        val result = handler.fetchPaidAmountWithErrorHandling(
            userId = "user-1",
            enrollmentDetails = listOf(enrollment("course-1"))
        )

        assertEquals(PaymentFetchResult.Success(3000.0, "EnrollmentAPI"), result)
    }

    @Test
    fun `course offer price is the next fallback`() = runTest {
        coEvery { apiService.getUserPayments("user-1") } returns Response.success(emptyList())
        coEvery { apiService.getEnrollmentByUserAndCourse("user-1", "course-1") } returns
            Response.success(EnrollmentDTO(paidAmount = null))
        coEvery { apiService.getCourseById("course-1") } returns
            Response.success(course("course-1", price = "5000", offerPrice = "4500"))

        val result = handler.fetchPaidAmountWithErrorHandling(
            userId = "user-1",
            enrollmentDetails = listOf(enrollment("course-1"))
        )

        assertEquals(PaymentFetchResult.Success(4500.0, "CoursePricing"), result)
    }

    @Test
    fun `course list price is used when there is no offer price`() = runTest {
        coEvery { apiService.getUserPayments("user-1") } returns Response.success(emptyList())
        coEvery { apiService.getEnrollmentByUserAndCourse("user-1", "course-1") } returns
            Response.success(EnrollmentDTO(paidAmount = null))
        coEvery { apiService.getCourseById("course-1") } returns
            Response.success(course("course-1", price = "5000", offerPrice = "0"))

        val result = handler.fetchPaidAmountWithErrorHandling(
            userId = "user-1",
            enrollmentDetails = listOf(enrollment("course-1"))
        )

        assertEquals(PaymentFetchResult.Success(5000.0, "CoursePricing"), result)
    }

    @Test
    fun `payment summary is the last successful source`() = runTest {
        coEvery { apiService.getUserPayments("user-1") } returns Response.success(emptyList())
        coEvery { apiService.getPaymentSummary("user-1") } returns Response.success(summary(1200.0))

        val result = handler.fetchPaidAmountWithErrorHandling("user-1")

        assertEquals(PaymentFetchResult.Success(1200.0, "PaymentSummaryAPI"), result)
    }

    @Test
    fun `every source failing returns the explicit fallback error`() = runTest {
        coEvery { apiService.getUserPayments("user-1") } returns serverError()
        coEvery { apiService.getEnrollmentByUserAndCourse(any(), any()) } returns serverError()
        coEvery { apiService.getCourseById(any()) } returns serverError()
        coEvery { apiService.getPaymentSummary("user-1") } returns serverError()

        val result = handler.fetchPaidAmountWithErrorHandling(
            userId = "user-1",
            enrollmentDetails = listOf(enrollment("course-1"))
        )

        assertEquals(
            PaymentFetchResult.Error("Unable to fetch payment data from any source", 0.0),
            result
        )
    }

    @Test
    fun `network failures are retried three times before giving up`() = runTest {
        coEvery { apiService.getUserPayments("user-1") } throws IOException("socket closed")
        coEvery { apiService.getPaymentSummary("user-1") } throws IOException("socket closed")

        val result = handler.fetchPaidAmountWithErrorHandling("user-1")

        assertTrue(result is PaymentFetchResult.Error)
        coVerify(exactly = 3) { apiService.getUserPayments("user-1") }
        coVerify(exactly = 3) { apiService.getPaymentSummary("user-1") }
    }

    @Test
    fun `a complete payment passes validation`() {
        val validation = handler.validatePaymentDto(
            PaymentDto(paidAmount = 1000.0, paymentMode = "UPI", status = "PAID")
        )

        assertEquals(ValidationResult.Valid, validation)
    }

    @Test
    fun `validation collects every problem with the payment`() {
        val validation = handler.validatePaymentDto(
            PaymentDto(paidAmount = 0.0, paymentMode = "  ", status = "")
        )

        assertEquals(
            ValidationResult.Invalid(
                listOf(
                    "Paid amount must be greater than 0",
                    "Payment mode cannot be blank",
                    "Payment status cannot be blank"
                )
            ),
            validation
        )
    }
}
