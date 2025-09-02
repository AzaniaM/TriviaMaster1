package com.example.triviamaster.ui.quiz

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.triviamaster.data.trivia.TriviaRepository
import com.example.triviamaster.di.ServiceLocator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class QuizQuestion(
    val id: String,
    val category: String,
    val difficulty: String,
    val question: String,
    val answers: List<String>,
    val correctIndex: Int
)

data class QuizState(
    val loading: Boolean = false,
    val error: String? = null,

    // setup data
    val categories: List<Pair<Int, String>> = emptyList(),
    val loadingCategories: Boolean = false,

    // run-time
    val started: Boolean = false,
    val questions: List<QuizQuestion> = emptyList(),
    val current: Int = 0,            // 0-based question index
    val selected: Int? = null,
    val correctCount: Int = 0,
    val showResult: Boolean = false,

    // options
    val speedMode: Boolean = false,
    val secondsElapsed: Int = 0
)

class QuizViewModel(
    private val repo: TriviaRepository = ServiceLocator.triviaRepo
) : ViewModel() {

    private val _state = MutableStateFlow(QuizState())
    val state = _state.asStateFlow()

    // user choices per question (null until answered)
    private var chosen: MutableList<Int?> = mutableListOf()

    // remember the last request so the UI can call retry()
    private var lastRequest: (() -> Unit)? = null

    fun loadCategories() {
        if (_state.value.loadingCategories) return
        viewModelScope.launch {
            _state.value = _state.value.copy(loadingCategories = true, error = null)
            try {
                val cats = repo.getCategories()
                _state.value = _state.value.copy(loadingCategories = false, categories = cats)
            } catch (t: Throwable) {
                _state.value = _state.value.copy(loadingCategories = false, error = t.message)
            }
        }
    }

    fun startQuiz(categoryId: Int?, difficulty: String?, count: Int, speedMode: Boolean) {
        // allow retrying this exact request
        lastRequest = { startQuiz(categoryId, difficulty, count, speedMode) }

        viewModelScope.launch {
            _state.value = _state.value.copy(loading = true, error = null)
            try {
                val qs = repo.getQuestions(count, categoryId, difficulty)
                chosen = MutableList(qs.size) { null }
                _state.value = _state.value.copy(
                    loading = false,
                    started = true,
                    questions = qs,
                    current = 0,
                    selected = null,
                    correctCount = 0,
                    showResult = false,
                    speedMode = speedMode,
                    secondsElapsed = 0
                )
            } catch (t: Throwable) {
                _state.value = _state.value.copy(
                    loading = false,
                    error = t.message ?: "Failed to load questions"
                )
            }
        }
    }

    /** Called by the error UI to repeat the last quiz request. */
    fun retry() {
        lastRequest?.invoke() ?: startQuiz(null, null, count = 10, speedMode = false)
    }

    fun selectAnswer(i: Int) {
        _state.value = _state.value.copy(selected = i)
    }

    fun next() {
        val s = _state.value
        if (s.questions.isEmpty()) return

        val sel = s.selected
        if (sel != null) chosen[s.current] = sel
        val wasCorrect = sel != null && sel == s.questions[s.current].correctIndex

        if (s.current + 1 < s.questions.size) {
            _state.value = s.copy(
                current = s.current + 1,
                selected = chosen[s.current + 1],
                correctCount = s.correctCount + if (wasCorrect) 1 else 0
            )
        } else {
            _state.value = s.copy(
                correctCount = s.correctCount + if (wasCorrect) 1 else 0,
                showResult = true
            )
        }
    }

    fun previous() {
        val s = _state.value
        if (s.current == 0) return
        _state.value = s.copy(current = s.current - 1, selected = chosen[s.current - 1])
    }

    /** For result screen: returns the user's chosen answer index (or null). */
    fun chosenAt(i: Int): Int? = chosen.getOrNull(i)

    fun reset() {
        _state.value = QuizState(categories = _state.value.categories) // keep cached categories
        chosen.clear()
        lastRequest = null
    }


}
