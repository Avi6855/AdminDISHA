package com.avinashpatil.app.admindisha.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.avinashpatil.app.admindisha.api.CourseStudentCount

@Entity(tableName = "student_counts")
data class CourseStudentCountEntity(
    @PrimaryKey val courseName: String,
    val totalUsersCount: Long
)

fun CourseStudentCountEntity.toDomainModel(): CourseStudentCount {
    return CourseStudentCount(
        courseName = courseName,
        totalUsersCount = totalUsersCount
    )
}

fun CourseStudentCount.toEntity(): CourseStudentCountEntity {
    return CourseStudentCountEntity(
        courseName = courseName,
        totalUsersCount = totalUsersCount
    )
}
