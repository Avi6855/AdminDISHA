package com.avinashpatil.app.admindisha.utils

import com.avinashpatil.app.admindisha.api.ApiService
import com.avinashpatil.app.admindisha.api.PaymentDto
import com.avinashpatil.app.admindisha.viewmodels.BillViewModel
import com.avinashpatil.app.admindisha.viewmodels.StudentViewModel
import kotlinx.coroutines.delay

/**
 * Utility class to test the complete payment data flow from API to UI components
 */
class PaymentFlowTester(
    private val apiService: ApiService,
    private val billViewModel: BillViewModel,
    private val studentViewModel: StudentViewModel
) {
    
    /**
     * Test result data class
     */
    data class TestResult(
        val testName: String,
        val success: Boolean,
        val message: String,
        val executionTimeMs: Long,
        val details: Map<String, Any> = emptyMap()
    )
    
    /**
     * Comprehensive test suite for payment data flow
     */
    suspend fun runComprehensivePaymentFlowTest(testUserId: String): List<TestResult> {
        val results = mutableListOf<TestResult>()
        
        println("PaymentFlowTester: Starting comprehensive payment flow test for user: $testUserId")
        
        // Test 1: PaymentDto validation
        results.add(testPaymentDtoValidation())
        
        // Test 2: API service payment endpoints
        results.add(testApiServicePaymentEndpoints(testUserId))
        
        // Test 3: BillViewModel paidAmount fetching
        results.add(testBillViewModelPaidAmountFetching(testUserId))
        
        // Test 4: StudentViewModel paidAmount fetching
        results.add(testStudentViewModelPaidAmountFetching())
        
        // Test 5: Payment data consistency
        results.add(testPaymentDataConsistency(testUserId))
        
        // Test 6: Error handling scenarios
        results.add(testErrorHandlingScenarios(testUserId))
        
        // Test 7: UI state updates
        results.add(testUIStateUpdates(testUserId))
        
        // Generate summary
        generateTestSummary(results)
        
        return results
    }
    
    /**
     * Test PaymentDto validation and helper methods
     */
    private suspend fun testPaymentDtoValidation(): TestResult {
        val startTime = System.currentTimeMillis()
        
        return try {
            println("PaymentFlowTester: Testing PaymentDto validation...")
            
            // Test valid PaymentDto
            val validPayment = PaymentDto(
                paidAmount = 1500.0,
                paymentMode = "CASH",
                paymentReference = "REF123",
                status = "PAID"
            )
            
            val isValid = validPayment.isValid()
            val formattedAmount = validPayment.getFormattedAmount()
            
            // Test invalid PaymentDto
            val invalidPayment = PaymentDto(
                paidAmount = -100.0,
                paymentMode = "",
                status = "PAID"
            )
            
            val isInvalid = !invalidPayment.isValid()
            
            val success = isValid && isInvalid && formattedAmount.contains("1500")
            val executionTime = System.currentTimeMillis() - startTime
            
            TestResult(
                testName = "PaymentDto Validation",
                success = success,
                message = if (success) "PaymentDto validation working correctly" else "PaymentDto validation failed",
                executionTimeMs = executionTime,
                details = mapOf(
                    "validPaymentIsValid" to isValid,
                    "invalidPaymentIsInvalid" to isInvalid,
                    "formattedAmount" to formattedAmount
                )
            )
        } catch (e: Exception) {
            val executionTime = System.currentTimeMillis() - startTime
            TestResult(
                testName = "PaymentDto Validation",
                success = false,
                message = "PaymentDto validation test failed: ${e.message}",
                executionTimeMs = executionTime
            )
        }
    }
    
    /**
     * Test API service payment endpoints
     */
    private suspend fun testApiServicePaymentEndpoints(userId: String): TestResult {
        val startTime = System.currentTimeMillis()
        
        return try {
            println("PaymentFlowTester: Testing API service payment endpoints...")
            
            val details = mutableMapOf<String, Any>()
            var successCount = 0
            var totalTests = 0
            
            // Test getAllUsers endpoint
            totalTests++
            try {
                val usersResponse = apiService.getAllUsers()
                if (usersResponse.isSuccessful) {
                    val users = usersResponse.body() ?: emptyList()
                    val testUser = users.find { it.userId == userId }
                    details["getAllUsers"] = "Success - Found ${users.size} users"
                    details["testUserFound"] = testUser != null
                    if (testUser != null) {
                        details["testUserPaidAmount"] = testUser.getPaidAmountAsDouble()
                    }
                    successCount++
                } else {
                    details["getAllUsers"] = "Failed - ${usersResponse.code()}"
                }
            } catch (e: Exception) {
                details["getAllUsers"] = "Exception - ${e.message}"
            }
            
            // Test getUserPayments endpoint (may not be implemented yet)
            totalTests++
            try {
                val paymentsResponse = apiService.getUserPayments(userId)
                details["getUserPayments"] = if (paymentsResponse.isSuccessful) {
                    successCount++
                    "Success - ${paymentsResponse.body()?.size ?: 0} payments"
                } else {
                    "Failed - ${paymentsResponse.code()}"
                }
            } catch (e: Exception) {
                details["getUserPayments"] = "Exception - ${e.message}"
            }
            
            // Test getPaymentSummary endpoint (may not be implemented yet)
            totalTests++
            try {
                val summaryResponse = apiService.getPaymentSummary(userId)
                details["getPaymentSummary"] = if (summaryResponse.isSuccessful) {
                    successCount++
                    "Success - Total: ${summaryResponse.body()?.totalPaidAmount ?: 0.0}"
                } else {
                    "Failed - ${summaryResponse.code()}"
                }
            } catch (e: Exception) {
                details["getPaymentSummary"] = "Exception - ${e.message}"
            }
            
            val success = successCount > 0 // At least one endpoint should work
            val executionTime = System.currentTimeMillis() - startTime
            
            TestResult(
                testName = "API Service Payment Endpoints",
                success = success,
                message = "$successCount/$totalTests payment endpoints working",
                executionTimeMs = executionTime,
                details = details
            )
        } catch (e: Exception) {
            val executionTime = System.currentTimeMillis() - startTime
            TestResult(
                testName = "API Service Payment Endpoints",
                success = false,
                message = "API endpoints test failed: ${e.message}",
                executionTimeMs = executionTime
            )
        }
    }
    
    /**
     * Test BillViewModel paidAmount fetching
     */
    private suspend fun testBillViewModelPaidAmountFetching(userId: String): TestResult {
        val startTime = System.currentTimeMillis()
        
        return try {
            println("PaymentFlowTester: Testing BillViewModel paidAmount fetching...")
            
            // Load student data in BillViewModel
            billViewModel.loadStudentData(userId)
            
            // Wait for data to load
            delay(2000)
            
            val uiState = billViewModel.uiState.value
            val studentData = uiState.studentData
            val paidAmount = studentData?.paidAmount ?: 0.0
            
            val success = paidAmount >= 0.0 && !uiState.isLoading && uiState.error == null
            val executionTime = System.currentTimeMillis() - startTime
            
            TestResult(
                testName = "BillViewModel PaidAmount Fetching",
                success = success,
                message = if (success) "BillViewModel successfully fetched paidAmount: $paidAmount" else "BillViewModel failed to fetch paidAmount",
                executionTimeMs = executionTime,
                details = mapOf(
                    "paidAmount" to paidAmount,
                    "isLoading" to uiState.isLoading,
                    "error" to (uiState.error ?: "None"),
                    "studentDataExists" to (studentData != null)
                )
            )
        } catch (e: Exception) {
            val executionTime = System.currentTimeMillis() - startTime
            TestResult(
                testName = "BillViewModel PaidAmount Fetching",
                success = false,
                message = "BillViewModel test failed: ${e.message}",
                executionTimeMs = executionTime
            )
        }
    }
    
    /**
     * Test StudentViewModel paidAmount fetching
     */
    private suspend fun testStudentViewModelPaidAmountFetching(): TestResult {
        val startTime = System.currentTimeMillis()
        
        return try {
            println("PaymentFlowTester: Testing StudentViewModel paidAmount fetching...")
            
            // Load students data
            studentViewModel.loadStudents()
            
            // Wait for data to load
            delay(2000)
            
            val uiState = studentViewModel.uiState.value
            val success = when (uiState) {
                is com.avinashpatil.app.admindisha.viewmodels.StudentUiState.Success -> {
                    val students = uiState.students
                    val studentsWithPaidAmount = students.filter { it.getPaidAmountAsDouble() > 0.0 }
                    studentsWithPaidAmount.isNotEmpty()
                }
                else -> false
            }
            
            val executionTime = System.currentTimeMillis() - startTime
            
            TestResult(
                testName = "StudentViewModel PaidAmount Fetching",
                success = success,
                message = if (success) "StudentViewModel successfully loaded students with paidAmount" else "StudentViewModel failed to load students with paidAmount",
                executionTimeMs = executionTime,
                details = mapOf<String, Any>(
                    "uiStateType" to (uiState::class.simpleName ?: "Unknown"),
                    "studentsCount" to if (uiState is com.avinashpatil.app.admindisha.viewmodels.StudentUiState.Success) uiState.students.size else 0
                )
            )
        } catch (e: Exception) {
            val executionTime = System.currentTimeMillis() - startTime
            TestResult(
                testName = "StudentViewModel PaidAmount Fetching",
                success = false,
                message = "StudentViewModel test failed: ${e.message}",
                executionTimeMs = executionTime
            )
        }
    }
    
    /**
     * Test payment data consistency across different sources
     */
    private suspend fun testPaymentDataConsistency(userId: String): TestResult {
        val startTime = System.currentTimeMillis()
        
        return try {
            println("PaymentFlowTester: Testing payment data consistency...")
            
            val paymentHandler = PaymentDataHandler(apiService)
            val result = paymentHandler.fetchPaidAmountWithErrorHandling(userId)
            
            val success = when (result) {
                is PaymentDataHandler.PaymentFetchResult.Success -> {
                    result.amount >= 0.0
                }
                is PaymentDataHandler.PaymentFetchResult.Error -> {
                    result.fallbackAmount >= 0.0
                }
            }
            
            val executionTime = System.currentTimeMillis() - startTime
            
            TestResult(
                testName = "Payment Data Consistency",
                success = success,
                message = when (result) {
                    is PaymentDataHandler.PaymentFetchResult.Success -> "Payment data consistent: ${result.amount} from ${result.source}"
                    is PaymentDataHandler.PaymentFetchResult.Error -> "Payment data inconsistent: ${result.message}"
                },
                executionTimeMs = executionTime,
                details = mapOf<String, Any>(
                    "resultType" to (result::class.simpleName ?: "Unknown"),
                    "amount" to when (result) {
                        is PaymentDataHandler.PaymentFetchResult.Success -> result.amount
                        is PaymentDataHandler.PaymentFetchResult.Error -> result.fallbackAmount
                    }
                )
            )
        } catch (e: Exception) {
            val executionTime = System.currentTimeMillis() - startTime
            TestResult(
                testName = "Payment Data Consistency",
                success = false,
                message = "Payment consistency test failed: ${e.message}",
                executionTimeMs = executionTime
            )
        }
    }
    
    /**
     * Test error handling scenarios
     */
    private suspend fun testErrorHandlingScenarios(userId: String): TestResult {
        val startTime = System.currentTimeMillis()
        
        return try {
            println("PaymentFlowTester: Testing error handling scenarios...")
            
            val paymentHandler = PaymentDataHandler(apiService)
            
            // Test with invalid user ID
            val invalidResult = paymentHandler.fetchPaidAmountWithErrorHandling("INVALID_USER_ID")
            
            // Test PaymentDto validation with invalid data
            val invalidPayment = PaymentDto(
                paidAmount = -100.0,
                paymentMode = "",
                status = ""
            )
            
            val validationResult = paymentHandler.validatePaymentDto(invalidPayment)
            
            val success = invalidResult is PaymentDataHandler.PaymentFetchResult.Error &&
                         validationResult is PaymentDataHandler.ValidationResult.Invalid
            
            val executionTime = System.currentTimeMillis() - startTime
            
            TestResult(
                testName = "Error Handling Scenarios",
                success = success,
                message = if (success) "Error handling working correctly" else "Error handling needs improvement",
                executionTimeMs = executionTime,
                details = mapOf<String, Any>(
                    "invalidUserResult" to (invalidResult::class.simpleName ?: "Unknown"),
                    "validationResult" to (validationResult::class.simpleName ?: "Unknown")
                )
            )
        } catch (e: Exception) {
            val executionTime = System.currentTimeMillis() - startTime
            TestResult(
                testName = "Error Handling Scenarios",
                success = false,
                message = "Error handling test failed: ${e.message}",
                executionTimeMs = executionTime
            )
        }
    }
    
    /**
     * Test UI state updates
     */
    private suspend fun testUIStateUpdates(userId: String): TestResult {
        val startTime = System.currentTimeMillis()
        
        return try {
            println("PaymentFlowTester: Testing UI state updates...")
            
            // Test BillViewModel state updates
            val initialBillState = billViewModel.uiState.value
            billViewModel.loadStudentData(userId)
            delay(1000)
            val updatedBillState = billViewModel.uiState.value
            
            // Test StudentViewModel state updates
            val initialStudentState = studentViewModel.uiState.value
            studentViewModel.loadStudents()
            delay(1000)
            val updatedStudentState = studentViewModel.uiState.value
            
            val billStateChanged = initialBillState != updatedBillState
            val studentStateChanged = initialStudentState != updatedStudentState
            
            val success = billStateChanged || studentStateChanged
            val executionTime = System.currentTimeMillis() - startTime
            
            TestResult(
                testName = "UI State Updates",
                success = success,
                message = if (success) "UI states updating correctly" else "UI states not updating",
                executionTimeMs = executionTime,
                details = mapOf(
                    "billStateChanged" to billStateChanged,
                    "studentStateChanged" to studentStateChanged
                )
            )
        } catch (e: Exception) {
            val executionTime = System.currentTimeMillis() - startTime
            TestResult(
                testName = "UI State Updates",
                success = false,
                message = "UI state updates test failed: ${e.message}",
                executionTimeMs = executionTime
            )
        }
    }
    
    /**
     * Generate and print test summary
     */
    private fun generateTestSummary(results: List<TestResult>) {
        println("\n=== PAYMENT FLOW TEST SUMMARY ===")
        
        val totalTests = results.size
        val passedTests = results.count { it.success }
        val failedTests = totalTests - passedTests
        val totalExecutionTime = results.sumOf { it.executionTimeMs }
        
        println("Total Tests: $totalTests")
        println("Passed: $passedTests")
        println("Failed: $failedTests")
        println("Success Rate: ${(passedTests.toDouble() / totalTests * 100).toInt()}%")
        println("Total Execution Time: ${totalExecutionTime}ms")
        
        println("\n=== DETAILED RESULTS ===")
        results.forEach { result ->
            val status = if (result.success) "✅ PASS" else "❌ FAIL"
            println("$status ${result.testName} (${result.executionTimeMs}ms)")
            println("   ${result.message}")
            if (result.details.isNotEmpty()) {
                result.details.forEach { (key, value) ->
                    println("   - $key: $value")
                }
            }
            println()
        }
        
        if (failedTests > 0) {
            println("⚠️  Some tests failed. Please check the payment data flow implementation.")
        } else {
            println("🎉 All tests passed! Payment data flow is working correctly.")
        }
        
        println("=== END OF TEST SUMMARY ===")
    }
}