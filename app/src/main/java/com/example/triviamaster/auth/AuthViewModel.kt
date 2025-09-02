package com.example.triviamaster.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.google.firebase.auth.ktx.userProfileChangeRequest
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.flow.update



data class AuthUiState(
    val loading: Boolean = false,
    val error: String? = null,
    val isAuthed: Boolean = false,
    val displayName: String? = null,
    val email: String? = null,
    val photoUrl: String? = null
)

class AuthViewModel : ViewModel() {
    private val repo = AuthRepository(FirebaseAuth.getInstance())
    private val _state = MutableStateFlow(AuthUiState())
    val state = _state.asStateFlow()

    init { updateFromUser() }

    private fun updateFromUser() {
        val u = repo.currentUser
        _state.value = _state.value.copy(
            isAuthed = u != null,
            displayName = u?.displayName,
            email = u?.email,
            photoUrl = u?.photoUrl?.toString(),
            loading = false,
            error = null
        )
    }

    fun signOut() {
        repo.signOut()
        updateFromUser()
    }

    fun signInWithGoogle(idToken: String) = viewModelScope.launch {
        _state.value = _state.value.copy(loading = true, error = null)
        try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            repo.signInWithCredential(credential)
            updateFromUser()
        } catch (e: Exception) {
            _state.value = _state.value.copy(loading = false, error = e.message)
        }
    }

    fun signUpEmail(email: String, password: String, username: String) = viewModelScope.launch {
        // basic validation
        if (email.isBlank() || password.isBlank() || username.isBlank()) {
            _state.update { it.copy(error = "Please fill in all fields") }
            return@launch
        }

        _state.update { it.copy(loading = true, error = null) }
        try {
            // existing repo call that creates the account
            repo.signUpEmail(email, password)

            // set displayName to the chosen username
            FirebaseAuth.getInstance().currentUser?.let { user ->
                val updates = userProfileChangeRequest { displayName = username }
                user.updateProfile(updates).await()
            }

            // whatever you already do to refresh state / navigate
            updateFromUser()
        } catch (e: Exception) {
            _state.update { it.copy(loading = false, error = e.message ?: "Sign up failed") }
        }
    }

    fun signInEmail(email: String, password: String) = viewModelScope.launch {
        _state.value = _state.value.copy(loading = true, error = null)
        try {
            repo.signInEmail(email, password)
            updateFromUser()
        } catch (e: Exception) {
            _state.value = _state.value.copy(loading = false, error = e.message)
        }
    }
}
