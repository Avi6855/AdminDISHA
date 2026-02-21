package com.avinashpatil.app.admindisha.api

import com.avinashpatil.app.admindisha.data.model.QuizResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface QuizApiService {
    @GET("quiz/practice")
    suspend fun getPracticeQuiz(
        @Query("category") category: String? = null,
        @Query("difficulty") difficulty: String? = null,
        @Query("count") count: Int = 25
    ): Response<QuizResponse>
}