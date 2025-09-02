package com.example.triviamaster.ui.screens

import com.example.triviamaster.data.trivia.UserStats
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*

class UserStatsRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    private fun uid(): String = requireNotNull(auth.currentUser?.uid)
    private fun doc() = db.collection("users").document(uid())

    /** Create a doc for the user if missing (called on first use) */
    suspend fun ensureProfile(displayName: String?) {
        val snap = doc().get().await()
        if (!snap.exists()) {
            doc().set(
                mapOf(
                    "displayName" to (displayName ?: ""),
                    "quizzes" to 0,
                    "correct" to 0,
                    "total" to 0,
                    "streak" to 0,
                    "lastQuizDay" to null,
                    "badges" to 0
                ),
                SetOptions.merge()
            ).await()
        }
    }

    /** Live stats flow for Dashboard */
    fun statsFlow(): Flow<UserStats> = callbackFlow {
        val reg = doc().addSnapshotListener { snap, err ->
            if (err != null) {
                close(err)
                return@addSnapshotListener
            }
            val model = snap?.toObject(UserStats::class.java) ?: UserStats()
            trySend(model)
        }
        awaitClose { reg.remove() }
    }

    /** Update stats after a quiz finishes */
    suspend fun recordQuiz(correct: Int, total: Int) {
        val today = currentDay()
        db.runTransaction { tx ->
            val ref = doc()
            val snap = tx.get(ref)
            val prev = snap.toObject(UserStats::class.java) ?: UserStats()

            val newTotal = prev.total + total
            val newCorrect = prev.correct + correct
            val newQuizzes = prev.quizzes + 1

            val newStreak = when (prev.lastQuizDay) {
                null -> 1
                today -> prev.streak            // same day; don’t change streak
                else -> if (isYesterday(prev.lastQuizDay, today)) prev.streak + 1 else 1
            }

            tx.set(
                ref,
                mapOf(
                    "quizzes" to newQuizzes,
                    "correct" to newCorrect,
                    "total" to newTotal,
                    "streak" to newStreak,
                    "lastQuizDay" to today
                ),
                SetOptions.merge()
            )
        }.await()
    }

    // ---- date helpers (UTC "yyyy-MM-dd") ----
    private fun currentDay(): String = formatDay(Date())

    private fun isYesterday(prev: String, today: String): Boolean {
        return try {
            val prevDate = parseDay(prev)
            val todayDate = parseDay(today)
            val cal = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply { time = prevDate; add(Calendar.DATE, 1) }
            formatDay(cal.time) == formatDay(todayDate)
        } catch (_: Exception) { false }
    }

    private fun parseDay(s: String): Date =
        SimpleDateFormat("yyyy-MM-dd", Locale.US).apply { timeZone = TimeZone.getTimeZone("UTC") }.parse(s)!!

    private fun formatDay(d: Date): String =
        SimpleDateFormat("yyyy-MM-dd", Locale.US).apply { timeZone = TimeZone.getTimeZone("UTC") }.format(d)
}
