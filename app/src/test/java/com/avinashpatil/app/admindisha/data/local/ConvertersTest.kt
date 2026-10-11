package com.avinashpatil.app.admindisha.data.local

import com.avinashpatil.app.admindisha.api.Batch
import com.avinashpatil.app.admindisha.api.BatchUser
import com.avinashpatil.app.admindisha.api.BatchUsersList
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate
import java.time.LocalTime

/**
 * Batches are cached as serialized JSON through these converters. A broken round trip means the
 * offline batch list comes back empty, so each supported type is exercised.
 */
class ConvertersTest {

    private val converters = Converters()

    private fun batch() = Batch(
        batchId = "batch-1",
        batchNo = "07",
        startDate = LocalDate.of(2026, 1, 5),
        endDate = LocalDate.of(2026, 6, 5),
        timing = LocalTime.of(10, 30),
        courseId = "course-1",
        studentIds = listOf("user-1", "user-2")
    )

    @Test
    fun `batch list round trips including dates and timing`() {
        val restored = converters.toBatchList(converters.fromBatchList(listOf(batch())))

        assertEquals(listOf(batch()), restored)
    }

    @Test
    fun `batch users list round trips`() {
        val users = BatchUsersList(
            batchId = "batch-1",
            batchUsers = listOf(BatchUser(fullName = "Student One"), BatchUser(fullName = "Student Two"))
        )

        val restored = converters.toBatchUsersList(converters.fromBatchUsersList(users))

        assertEquals(users, restored)
    }

    @Test
    fun `string list round trips`() {
        val list = listOf("CCC", "Tally", "Android")

        assertEquals(list, converters.toStringList(converters.fromStringList(list)))
    }

    @Test
    fun `local date and time use iso formats`() {
        val date = LocalDate.of(2026, 3, 19)
        val time = LocalTime.of(9, 5)

        assertEquals("2026-03-19", converters.fromLocalDate(date))
        assertEquals(date, converters.toLocalDate("2026-03-19"))
        assertEquals("09:05:00", converters.fromLocalTime(time))
        assertEquals(time, converters.toLocalTime("09:05:00"))
    }

    @Test
    fun `null values stay null`() {
        assertNull(converters.fromBatchList(null))
        assertNull(converters.toBatchList(null))
        assertNull(converters.fromBatchUsersList(null))
        assertNull(converters.toBatchUsersList(null))
        assertNull(converters.fromStringList(null))
        assertNull(converters.toStringList(null))
        assertNull(converters.fromLocalDate(null))
        assertNull(converters.toLocalDate(null))
        assertNull(converters.fromLocalTime(null))
        assertNull(converters.toLocalTime(null))
    }
}
