package com.avinashpatil.app.admindisha.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.avinashpatil.app.admindisha.api.Topic

@Entity(tableName = "topics")
data class TopicEntity(
    @PrimaryKey val topicId: String,
    val topicName: String,
    val courseId: String,
    val message: String?
)

fun Topic.toEntity(): TopicEntity {
    return TopicEntity(
        topicId = topicId ?: "", // Handle potential null ID if necessary, though fetched topics usually have IDs
        topicName = topicName,
        courseId = courseId,
        message = message
    )
}

fun TopicEntity.toDomainModel(): Topic {
    return Topic(
        topicId = topicId,
        topicName = topicName,
        courseId = courseId,
        message = message
    )
}
