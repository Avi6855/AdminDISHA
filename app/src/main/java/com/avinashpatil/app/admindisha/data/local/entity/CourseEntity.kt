package com.avinashpatil.app.admindisha.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.avinashpatil.app.admindisha.api.Batch
import com.avinashpatil.app.admindisha.api.Course

@Entity(tableName = "courses")
data class CourseEntity(
    @PrimaryKey val courseId: String,
    val courseName: String,
    val courseDescription: String?,
    val courseDuration: String?,
    val coursePrice: String?,
    val courseOfferPrice: String?,
    val batches: List<Batch>?
)

fun CourseEntity.toDomainModel(): Course {
    return Course(
        courseId = courseId,
        courseName = courseName,
        courseDescription = courseDescription,
        courseDuration = courseDuration,
        coursePrice = coursePrice,
        courseOfferPrice = courseOfferPrice,
        batches = batches
    )
}

fun Course.toEntity(): CourseEntity {
    return CourseEntity(
        courseId = courseId,
        courseName = courseName,
        courseDescription = courseDescription,
        courseDuration = courseDuration,
        coursePrice = coursePrice,
        courseOfferPrice = courseOfferPrice,
        batches = batches
    )
}
