package com.example.triviamaster.data.trivia

import kotlin.math.roundToInt

data class UserStats(
    val quizzes: Int = 0,
    val correct: Int = 0,
    val total: Int = 0,
    val streak: Int = 0,
    val lastQuizDay: String? = null,   // "yyyy-MM-dd" UTC
    val badges: Int = 0
) {
    val accuracyPct: Int
        get() = if (total == 0) 0 else ((correct.toDouble() / total) * 100).roundToInt()
}
