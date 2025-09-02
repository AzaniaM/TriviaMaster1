package com.example.triviamaster.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.core.text.HtmlCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.triviamaster.ui.navigation.Route
import com.example.triviamaster.ui.quiz.QuizViewModel
import kotlin.math.roundToInt

@Composable
fun QuizScreen(nav: NavController) {
    // Use the SAME ViewModel instance as the screen you navigated from
    val owner = remember(nav) {
        // fallback to current if there's no previous (e.g. deep link)
        nav.previousBackStackEntry ?: nav.currentBackStackEntry!!
    }
    val vm: QuizViewModel = viewModel(viewModelStoreOwner = owner)
    val state by vm.state.collectAsState()

    // ⬇️ Navigate to Results when the quiz finishes
    LaunchedEffect(state.showResult) {
        if (state.showResult) {
            nav.navigate(Route.Results.route) {
                popUpTo(Route.Quiz.route) { inclusive = true }
            }
        }
    }

    val bg = remember { Brush.verticalGradient(listOf(Color(0xFFEFF3FF), Color(0xFFF9FBFF))) }

    when {
        state.loading -> Box(
            Modifier
                .fillMaxSize()
                .background(bg),
            contentAlignment = Alignment.Center
        ) { CircularProgressIndicator() }

        state.error != null -> Column(
            Modifier
                .fillMaxSize()
                .background(bg)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Error: ${state.error}", color = MaterialTheme.colorScheme.error)
            Spacer(Modifier.height(12.dp))
            Button(onClick = { vm.retry() }) { Text("Try again") }
            Spacer(Modifier.height(8.dp))
            OutlinedButton(onClick = { nav.popBackStack() }) { Text("Back") }
        }

        state.questions.isEmpty() -> Box(
            Modifier
                .fillMaxSize()
                .background(bg),
            contentAlignment = Alignment.Center
        ) { CircularProgressIndicator() }

        else -> {
            val index = state.current.coerceIn(0, state.questions.lastIndex)
            val q = state.questions[index]
            val answers = remember(q) { q.answers }
            val cleanedQuestion = remember(q) {
                HtmlCompat.fromHtml(q.question, HtmlCompat.FROM_HTML_MODE_LEGACY).toString()
            }
            val selected = state.selected

            val scroll = rememberScrollState()

            Box(
                Modifier
                    .fillMaxSize()
                    .background(bg)
                    .statusBarsPadding()
                    .navigationBarsPadding()
            ) {
                Column(
                    Modifier
                        .fillMaxSize()
                        .verticalScroll(scroll)          // real scroll
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Header row: "Question X" + pill "X of N"
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Question ${index + 1}",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                            modifier = Modifier.weight(1f)
                        )
                        Surface(
                            shape = RoundedCornerShape(999.dp),
                            color = Color(0xFFEFF2FA)
                        ) {
                            Text(
                                text = "${index + 1} of ${state.questions.size}",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }

                    // Thin rounded progress
                    LinearProgressIndicator(
                        progress = { (index + 1f) / state.questions.size },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(6.dp))
                    )

                    // Question card
                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Text(
                                cleanedQuestion,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(Modifier.height(10.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                TagChip(text = q.category)
                                TagChip(
                                    text = q.difficulty,
                                    bg = Color(0xFFFFF3CC),
                                    fg = Color(0xFF7A6221)
                                )
                            }
                        }
                    }

                    // Answers
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        answers.forEachIndexed { i, raw ->
                            val text = remember(q, i) {
                                HtmlCompat.fromHtml(raw, HtmlCompat.FROM_HTML_MODE_LEGACY).toString()
                            }
                            AnswerRow(
                                text = text,
                                selected = selected == i,
                                onClick = { vm.selectAnswer(i) }
                            )
                        }
                    }

                    Spacer(Modifier.height(4.dp))

                    // Big CTA
                    val darkCta = Color(0xFF0B0B1A)
                    Button(
                        enabled = selected != null,
                        onClick = { vm.next() },
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selected != null) darkCta else Color(0xFF8C8F99),
                            contentColor = Color.White,
                            disabledContainerColor = Color(0xFF8C8F99),
                            disabledContentColor = Color.White
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) { Text("Next Question", fontWeight = FontWeight.SemiBold) }

                    Spacer(Modifier.height(12.dp))
                }

                // Slim side scrollbar indicator
                SideScrollbar(
                    scrollState = scroll,
                    knobHeight = 48.dp,
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 4.dp, top = 12.dp, bottom = 12.dp)
                        .fillMaxHeight()
                )
            }
        }
    }
}

/* ---------- Small pieces to match the mock ---------- */

@Composable
private fun TagChip(
    text: String,
    bg: Color = Color(0xFFE9F0FF),
    fg: Color = Color(0xFF1F3C88)
) {
    Surface(shape = RoundedCornerShape(50), color = bg) {
        Text(
            text = text,
            color = fg,
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun AnswerRow(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val border = if (selected) Color(0xFF0B0B1A) else Color(0xFFE2E6F0)
    val circleFill = if (selected) Color(0xFF0B0B1A) else Color.Transparent

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = Color.White,
        border = BorderStroke(1.dp, border),
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 54.dp)
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left radio circle
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFF4F6FB), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(circleFill)
                )
            }
            Spacer(Modifier.width(10.dp))
            Text(
                text,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

/* ---------- Simple, robust side scrollbar (no BoxWithConstraints) ---------- */

@Composable
private fun SideScrollbar(
    scrollState: androidx.compose.foundation.ScrollState,
    modifier: Modifier = Modifier,
    knobHeight: Dp = 48.dp
) {
    val density = LocalDensity.current
    val knobPx = with(density) { knobHeight.roundToPx() }

    // Track height in px (measured at runtime)
    var trackPx by remember { mutableStateOf(0) }

    // 0..1 fraction
    val fraction = if (scrollState.maxValue > 0)
        scrollState.value.toFloat() / scrollState.maxValue
    else 0f

    val yPx = ((trackPx - knobPx).coerceAtLeast(0) * fraction).roundToInt()

    Box(
        modifier = modifier
            .width(12.dp) // little gutter
            .onSizeChanged { trackPx = it.height }
    ) {
        // Track
        Box(
            Modifier
                .align(Alignment.CenterEnd)
                .width(4.dp)
                .fillMaxHeight()
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0x14000000))
        )

        // Knob
        Box(
            Modifier
                .align(Alignment.TopEnd)
                .offset { IntOffset(0, yPx) }
                .width(8.dp)
                .height(knobHeight)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0x55000000))
        )
    }
}
