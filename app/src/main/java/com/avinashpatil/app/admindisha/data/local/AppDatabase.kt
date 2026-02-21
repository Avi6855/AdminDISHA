package com.avinashpatil.app.admindisha.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.avinashpatil.app.admindisha.data.local.dao.HomeDao
import com.avinashpatil.app.admindisha.data.local.entity.BatchEntity
import com.avinashpatil.app.admindisha.data.local.entity.CourseEntity
import com.avinashpatil.app.admindisha.data.local.entity.CourseStudentCountEntity
import com.avinashpatil.app.admindisha.data.local.entity.TopicEntity

@Database(
    entities = [CourseStudentCountEntity::class, CourseEntity::class, TopicEntity::class, BatchEntity::class],
    version = 3,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun homeDao(): HomeDao
}
