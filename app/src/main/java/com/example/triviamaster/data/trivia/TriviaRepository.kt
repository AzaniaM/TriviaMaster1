package com.example.triviamaster.data.trivia

import androidx.core.text.HtmlCompat
import com.example.triviamaster.ui.quiz.QuizQuestion  // adjust if your QuizQuestion is elsewhere

class TriviaRepository(private val api: TriviaApi) {

    suspend fun getCategories(): List<Pair<Int, String>> {
        val res = api.getCategories()
        return res.categories.map { it.id to it.name }
    }

    suspend fun getQuestions(
        amount: Int,
        categoryId: Int?,
        difficulty: String?
    ): List<QuizQuestion> {
        // Calls body-returning API; no .body() here
        val res = api.getQuestions(
            amount = amount,
            category = categoryId,
            difficulty = difficulty?.lowercase(),
            type = "multiple"
        )
        val list = res.results

        return list.map { dto ->
            val decodedQuestion  = dto.question.decodeHtml()
            val decodedCorrect   = dto.correct.decodeHtml()
            val decodedIncorrect = dto.incorrect.map { it.decodeHtml() }
            val answers = (decodedIncorrect + decodedCorrect).shuffled()
            val correctIdx = answers.indexOf(decodedCorrect)

            QuizQuestion(
                id = "${decodedQuestion.hashCode()}_${dto.category.hashCode()}",
                category = dto.category,
                difficulty = dto.difficulty.replaceFirstChar { it.uppercase() },
                question = decodedQuestion,
                answers = answers,
                correctIndex = correctIdx
            )
        }
    }

    private fun String.decodeHtml(): String =
        HtmlCompat.fromHtml(this, HtmlCompat.FROM_HTML_MODE_LEGACY).toString()
}
