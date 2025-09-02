package com.example.triviamaster.ui.quiz

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.triviamaster.data.trivia.UserStats
import com.example.triviamaster.ui.screens.UserStatsRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

data class StatsUiState(
    val loading: Boolean = true,
    val stats: UserStats = UserStats(),
    val displayName: String? = null,
    val error: String? = null
)

class UserStatsViewModel(
    private val repo: UserStatsRepository = UserStatsRepository(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) : ViewModel() {

    private val _ui = MutableStateFlow(StatsUiState())
    val ui = _ui.asStateFlow()

    init {
        viewModelScope.launch {
            repo.ensureProfile(auth.currentUser?.displayName)
            repo.statsFlow()
                .catch { e -> _ui.value = _ui.value.copy(loading = false, error = e.message) }
                .collect { stats ->
                    _ui.value = StatsUiState(
                        loading = false,
                        stats = stats,
                        displayName = auth.currentUser?.displayName
                            ?: auth.currentUser?.email?.substringBefore("@")
                    )
                }
        }
    }

    /** Call this from Results screen after a quiz completes */
    fun recordQuiz(correct: Int, total: Int) {
        viewModelScope.launch { repo.recordQuiz(correct, total) }
    }
}
