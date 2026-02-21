package com.avinashpatil.app.admindisha.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.avinashpatil.app.admindisha.data.local.entity.BatchEntity
import com.avinashpatil.app.admindisha.data.local.entity.CourseEntity
import com.avinashpatil.app.admindisha.data.local.entity.CourseStudentCountEntity
import com.avinashpatil.app.admindisha.data.local.entity.TopicEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HomeDao {
    @Query("SELECT * FROM student_counts")
    fun getStudentCounts(): Flow<List<CourseStudentCountEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    @JvmSuppressWildcards
    suspend fun insertStudentCounts(counts: List<CourseStudentCountEntity>): LongArray

    @Query("DELETE FROM student_counts")
    @JvmSuppressWildcards
    suspend fun clearStudentCounts(): Int

    @Query("SELECT * FROM courses")
    fun getCourses(): Flow<List<CourseEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    @JvmSuppressWildcards
    suspend fun insertCourses(courses: List<CourseEntity>): LongArray

    @Query("DELETE FROM courses")
    @JvmSuppressWildcards
    suspend fun clearCourses(): Int

    @Query("SELECT * FROM topics WHERE courseId = :courseId")
    fun getTopics(courseId: String): Flow<List<TopicEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    @JvmSuppressWildcards
    suspend fun insertTopics(topics: List<TopicEntity>): LongArray

    @Query("DELETE FROM topics WHERE courseId = :courseId")
    @JvmSuppressWildcards
    suspend fun clearTopics(courseId: String): Int

    @Query("SELECT * FROM batches WHERE courseId = :courseId")
    fun getBatchesByCourseId(courseId: String): Flow<List<BatchEntity>>

    @Query("SELECT * FROM batches")
    fun getAllBatches(): Flow<List<BatchEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    @JvmSuppressWildcards
    suspend fun insertBatches(batches: List<BatchEntity>): LongArray

    @Query("DELETE FROM batches WHERE courseId = :courseId")
    @JvmSuppressWildcards
    suspend fun clearBatchesByCourseId(courseId: String): Int

    @Query("DELETE FROM batches")
    @JvmSuppressWildcards
    suspend fun clearAllBatches(): Int
}
