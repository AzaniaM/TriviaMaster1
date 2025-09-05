package com.example.triviamaster.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.text.HtmlCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.triviamaster.ui.navigation.Route
import com.example.triviamaster.ui.quiz.QuizQuestion
import com.example.triviamaster.ui.quiz.QuizViewModel
import com.example.triviamaster.ui.quiz.UserStatsViewModel
import kotlin.math.max
import kotlin.math.roundToInt

@Composable
fun ResultsScreen(nav: NavController) {
    // Use the SAME VM instance used by Quiz/Setup so the finished state is present.
    val owner = remember(nav) {
        runCatching { nav.getBackStackEntry(Route.QuizSetup.route) }.getOrNull()
            ?: nav.previousBackStackEntry
            ?: nav.currentBackStackEntry!!
    }
    val vm: QuizViewModel = viewModel(viewModelStoreOwner = owner)
    val state by vm.state.collectAsState()

    // NEW: Stats VM to record quiz completion
    val statsVm: UserStatsViewModel = viewModel()

    val bg = remember { Brush.verticalGradient(listOf(Color(0xFFEFF3FF), Color(0xFFF9FBFF))) }

    val total = state.questions.size
    val correct = state.correctCount
    val percent = if (total > 0) ((correct * 100f) / total).roundToInt() else 0

    // NEW: One-shot write to Firestore when results arrive
    var sent by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(correct, total) {
        if (!sent && total > 0) {
            statsVm.recordQuiz(correct, total)
            sent = true
        }
    }

    val secs = max(state.secondsElapsed, 0)
    val mins = secs / 60
    val rem = secs % 60

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bg)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(12.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Card(
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                Modifier.padding(horizontal = 18.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("💪", style = MaterialTheme.typography.headlineMedium)
                Spacer(Modifier.height(6.dp))
                Text(
                    "Quiz Complete!",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(10.dp))

                Text(
                    "$correct/$total",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(4.dp))
                Text("$percent% Correct", color = MaterialTheme.colorScheme.onSurfaceVariant)

                Spacer(Modifier.height(8.dp))
                Surface(shape = RoundedCornerShape(999.dp), color = Color(0xFFEFF2FA)) {
                    Text(
                        text = "⏱  ${mins}m ${rem}s",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }

                Spacer(Modifier.height(8.dp))
                Text(
                    "Don't give up! Practice makes perfect!",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(Modifier.height(14.dp))
                Text(
                    "Review Your Answers:",
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.align(Alignment.Start)
                )
                Spacer(Modifier.height(8.dp))

                // List all Q&As
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    state.questions.forEachIndexed { i, q ->
                        ReviewItem(
                            index = i,
                            question = q,
                            chosen = vm.chosenAt(i) // null if not answered
                        )
                    }
                }

                Spacer(Modifier.height(14.dp))

                val darkCta = Color(0xFF0B0B1A)
                Button(
                    onClick = {
                        vm.reset()
                        nav.navigate(Route.QuizSetup.route) {
                            popUpTo(Route.Results.route) { inclusive = true }
                        }
                    },
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = darkCta,
                        contentColor = Color.White
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) { Text("Play Again", fontWeight = FontWeight.SemiBold) }

                Spacer(Modifier.height(8.dp))

                OutlinedButton(
                    onClick = {
                        vm.reset()
                        nav.navigate(Route.Dashboard.route) {
                            popUpTo(Route.Results.route) { inclusive = true }
                        }
                    },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) { Text("Go Home") }
            }
        }

        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun ReviewItem(index: Int, question: QuizQuestion, chosen: Int?) {
    val isCorrect = chosen != null && chosen == question.correctIndex
    val chipBg = if (isCorrect) Color(0xFFE8F6EE) else Color(0xFFFDECEF)
    val chipFg = if (isCorrect) Color(0xFF166A3B) else Color(0xFFB3261E)
    val badge = if (isCorrect) "✓" else "✕"

    // HTML decode question/answers (OpenTDB often contains entities)
    val qText = remember(question) { htmlDecode(question.question) }
    val yourAnswer = remember(question, chosen) {
        chosen?.let { question.answers.getOrNull(it) }?.let(::htmlDecode) ?: "—"
    }
    val correctAnswer = remember(question) {
        question.answers.getOrNull(question.correctIndex)?.let(::htmlDecode) ?: "—"
    }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFFF9FAFF),
        tonalElevation = 0.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(12.dp)) {
            Surface(shape = RoundedCornerShape(999.dp), color = chipBg) {
                Text(
                    text = " $badge  Question ${index + 1}",
                    color = chipFg,
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
            Spacer(Modifier.height(8.dp))
            Text(
                qText,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 6,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(8.dp))

            Text(
                "Your answer: $yourAnswer",
                color = if (isCorrect) Color(0xFF0F7A43) else Color(0xFFB00020)
            )
            if (!isCorrect) {
                Spacer(Modifier.height(4.dp))
                Text("Correct answer: $correctAnswer", color = Color(0xFF0F7A43))
            }
        }
    }
}

/* ---------- Helpers ---------- */

private fun htmlDecode(src: String): String =
    HtmlCompat.fromHtml(src, HtmlCompat.FROM_HTML_MODE_LEGACY).toString()
