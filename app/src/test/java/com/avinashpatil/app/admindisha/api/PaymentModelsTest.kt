package com.avinashpatil.app.admindisha.api

import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.Locale

/**
 * The bill and student-detail screens read paid amounts from three different model shapes.
 * These helpers are what keep those screens from showing "null" or crashing on a bad payload.
 */
class PaymentModelsTest {

    private lateinit var originalLocale: Locale

    @Before
    fun setUp() {
        originalLocale = Locale.getDefault()
        Locale.setDefault(Locale.US)
    }

    @After
    fun tearDown() {
        Locale.setDefault(originalLocale)
    }

    @Test
    fun `payment dto formats the amount for the bill`() {
        val payment = PaymentDto(paidAmount = 1500.0, paymentMode = "UPI")

        assertEquals("₹1500.00", payment.getFormattedAmount())
    }

    @Test
    fun `payment dto is only valid with an amount and a mode`() {
        assertTrue(PaymentDto(paidAmount = 1.0, paymentMode = "CASH").isValid())
        assertFalse(PaymentDto(paidAmount = 0.0, paymentMode = "CASH").isValid())
        assertFalse(PaymentDto(paidAmount = -10.0, paymentMode = "CASH").isValid())
        assertFalse(PaymentDto(paidAmount = 500.0, paymentMode = "").isValid())
    }

    @Test
    fun `enrollment dto tolerates missing payment data`() {
        assertEquals(2500.0, EnrollmentDTO(paidAmount = 2500.0).getPaidAmountAsDouble(), 0.0)
        assertEquals(0.0, EnrollmentDTO(paidAmount = null).getPaidAmountAsDouble(), 0.0)
    }

    @Test
    fun `user details parse the string amount and never crash`() {
        assertEquals(5000.5, userWith(paidAmount = "5000.50").getPaidAmountAsDouble(), 0.0)
        assertEquals(0.0, userWith(paidAmount = null).getPaidAmountAsDouble(), 0.0)
        assertEquals(0.0, userWith(paidAmount = "not-a-number").getPaidAmountAsDouble(), 0.0)
        assertEquals(0.0, userWith(paidAmount = "").getPaidAmountAsDouble(), 0.0)
    }

    @Test
    fun `payment summary totals the individual payments`() {
        val summary = PaymentSummaryResponse(
            userId = "user-1",
            totalPaidAmount = 6200.0,
            totalDueAmount = 1300.0,
            paymentCount = 3,
            lastPaymentDate = "2026-03-01",
            paymentStatus = "PARTIAL",
            payments = listOf(
                PaymentDto(paidAmount = 2000.0, paymentMode = "CASH"),
                PaymentDto(paidAmount = 3000.0, paymentMode = "UPI"),
                PaymentDto(paidAmount = 1200.0, paymentMode = "CARD")
            )
        )

        assertEquals(6200.0, summary.calculateTotalFromPayments(), 0.0)
        assertEquals("₹6200.00", summary.getFormattedTotalPaid())
    }

    @Test
    fun `payment summary with no payments totals to zero`() {
        val summary = PaymentSummaryResponse(
            userId = "user-1",
            totalPaidAmount = 0.0,
            totalDueAmount = 5000.0,
            paymentCount = 0,
            lastPaymentDate = null,
            paymentStatus = "PENDING",
            payments = emptyList()
        )

        assertEquals(0.0, summary.calculateTotalFromPayments(), 0.0)
    }

    private fun userWith(paidAmount: String?) = UserAllDetails(
        userId = "user-1",
        dishaId = "DISHA-001",
        email = "student@example.com",
        fullName = "Student One",
        role = "STUDENT",
        userName = "student1",
        paidAmount = paidAmount
    )
}
