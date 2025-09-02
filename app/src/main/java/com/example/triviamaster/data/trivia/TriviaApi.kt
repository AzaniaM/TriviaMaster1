package com.example.triviamaster.data.trivia

import retrofit2.http.GET
import retrofit2.http.Query

interface TriviaApi {
    @GET("api.php")
    suspend fun getQuestions(
        @Query("amount") amount: Int,
        @Query("category") category: Int? = null,
        @Query("difficulty") difficulty: String? = null,
        @Query("type") type: String = "multiple"
    ): QuestionResponse   // returns body directly (not Response<>)

    @GET("api_category.php")
    suspend fun getCategories(): CategoryResponse
}
