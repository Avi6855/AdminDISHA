package com.avinashpatil.app.admindisha.screens.student

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Step 1 of the student onboarding wizard is gated by isValidEmail. Rejecting a real address
 * blocks admissions at the counter, so both directions are covered here.
 */
class StepUpFormValidationTest {

    @Test
    fun `normal institute email addresses are accepted`() {
        assertTrue(isValidEmail("student@gmail.com"))
        assertTrue(isValidEmail("avinash.patil@disha.co.in"))
        assertTrue(isValidEmail("a@b.c"))
        assertTrue(isValidEmail("first_last+tag@example.org"))
    }

    @Test
    fun `incomplete addresses are rejected`() {
        assertFalse(isValidEmail("student@gmail"))
        assertFalse(isValidEmail("student.gmail.com"))
        assertFalse(isValidEmail("student@.com"))
        assertFalse(isValidEmail("student@gmail."))
        assertFalse(isValidEmail(""))
    }

    @Test
    fun `known limitation - the current pattern accepts a doubled at sign`() {
        // The regex only checks for "something@something.something". This test documents today's
        // behaviour so that tightening the rule is a deliberate change rather than an accident.
        assertTrue(isValidEmail("student@@gmail.com"))
        assertTrue(isValidEmail("student@gmail..com"))
    }

    @Test
    fun `address must start with a letter`() {
        assertFalse(isValidEmail("1student@gmail.com"))
        assertFalse(isValidEmail("@gmail.com"))
        assertFalse(isValidEmail(".student@gmail.com"))
    }
}
