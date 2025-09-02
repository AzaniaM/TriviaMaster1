package com.example.triviamaster.data.trivia

import com.squareup.moshi.Json

data class QuestionResponse(
    @Json(name = "response_code") val code: Int,
    val results: List<QuestionDto>           // non-null
)

data class QuestionDto(
    val category: String,
    val type: String,
    val difficulty: String,
    val question: String,
    @Json(name = "correct_answer") val correct: String,
    @Json(name = "incorrect_answers") val incorrect: List<String>
)

data class CategoryResponse(
    @Json(name = "trivia_categories") val categories: List<CategoryDto>
)

data class CategoryDto(
    val id: Int,
    val name: String
)
