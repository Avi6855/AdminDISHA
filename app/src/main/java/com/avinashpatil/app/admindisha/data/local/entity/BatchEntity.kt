package com.avinashpatil.app.admindisha.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.avinashpatil.app.admindisha.api.Batch
import com.avinashpatil.app.admindisha.api.BatchUsersList
import java.time.LocalDate
import java.time.LocalTime

@Entity(tableName = "batches")
data class BatchEntity(
    @PrimaryKey val batchId: String,
    val batchNo: String,
    val startDate: LocalDate?,
    val endDate: LocalDate?,
    val timing: LocalTime?,
    val courseId: String,
    val studentIds: List<String>?,
    val isActive: Boolean,
    val batchUsersList: BatchUsersList?
)

fun Batch.toEntity(): BatchEntity {
    return BatchEntity(
        batchId = batchId,
        batchNo = batchNo,
        startDate = startDate,
        endDate = endDate,
        timing = timing,
        courseId = courseId,
        studentIds = studentIds,
        isActive = isActive,
        batchUsersList = batchUsersList
    )
}

fun BatchEntity.toDomainModel(): Batch {
    return Batch(
        batchId = batchId,
        batchNo = batchNo,
        startDate = startDate,
        endDate = endDate,
        timing = timing,
        courseId = courseId,
        studentIds = studentIds,
        isActive = isActive,
        batchUsersList = batchUsersList
    )
}
