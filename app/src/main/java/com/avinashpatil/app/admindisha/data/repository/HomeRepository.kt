package com.avinashpatil.app.admindisha.data.repository

import androidx.room.withTransaction
import com.avinashpatil.app.admindisha.api.ApiService
import com.avinashpatil.app.admindisha.api.Batch
import com.avinashpatil.app.admindisha.api.Course
import com.avinashpatil.app.admindisha.api.CourseStudentCount
import com.avinashpatil.app.admindisha.api.Topic
import com.avinashpatil.app.admindisha.data.local.AppDatabase
import com.avinashpatil.app.admindisha.data.local.entity.toDomainModel
import com.avinashpatil.app.admindisha.data.local.entity.toEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HomeRepository @Inject constructor(
    private val apiService: ApiService,
    private val database: AppDatabase
) {
    private val homeDao = database.homeDao()

    val studentCounts: Flow<List<CourseStudentCount>> = homeDao.getStudentCounts()
        .map { entities -> entities.map { it.toDomainModel() } }

    val courses: Flow<List<Course>> = homeDao.getCourses()
        .map { entities -> entities.map { it.toDomainModel() } }

    fun getTopics(courseId: String): Flow<List<Topic>> = homeDao.getTopics(courseId)
        .map { entities -> entities.map { it.toDomainModel() } }

    fun getBatches(courseId: String?): Flow<List<Batch>> {
        return if (courseId != null) {
            homeDao.getBatchesByCourseId(courseId).map { entities -> entities.map { it.toDomainModel() } }
        } else {
            homeDao.getAllBatches().map { entities -> entities.map { it.toDomainModel() } }
        }
    }

    suspend fun refreshStudentCounts() {
        try {
            val response = apiService.getCourseStudentCounts()
            if (response.isSuccessful && response.body() != null) {
                val counts = response.body()!!
                database.withTransaction {
                    homeDao.clearStudentCounts()
                    homeDao.insertStudentCounts(counts.map { it.toEntity() })
                }
            }
        } catch (e: Exception) {
            throw e
        }
    }

    suspend fun refreshCourses() {
        try {
            val response = apiService.getCourses()
            if (response.isSuccessful && response.body() != null) {
                val coursesList = response.body()!!
                database.withTransaction {
                    homeDao.clearCourses()
                    homeDao.insertCourses(coursesList.map { it.toEntity() })
                }
            }
        } catch (e: Exception) {
             throw e
        }
    }

    suspend fun refreshTopics(courseId: String) {
        try {
            val response = apiService.getTopicsByCourseId(courseId)
            if (response.isSuccessful && response.body() != null) {
                val topics = response.body()!!
                database.withTransaction {
                    homeDao.clearTopics(courseId)
                    homeDao.insertTopics(topics.map { it.toEntity() })
                }
            }
        } catch (e: Exception) {
            throw e
        }
    }

    suspend fun refreshBatches(courseId: String?) {
        try {
            if (courseId != null) {
                val response = apiService.getBatchesByCourseId(courseId)
                if (response.isSuccessful && response.body() != null) {
                    val batches = response.body()!!
                    database.withTransaction {
                        homeDao.clearBatchesByCourseId(courseId)
                        homeDao.insertBatches(batches.map { it.toEntity() })
                    }
                }
            } else {
                val response = apiService.getBatches()
                if (response.isSuccessful && response.body() != null) {
                    val batches = response.body()!!
                    database.withTransaction {
                        homeDao.clearAllBatches()
                        homeDao.insertBatches(batches.map { it.toEntity() })
                    }
                }
            }
        } catch (e: Exception) {
            throw e
        }
    }
    
    suspend fun sendCertificates(courseId: String) = apiService.sendCertificates(courseId)
}
