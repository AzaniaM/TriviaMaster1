package com.example.triviamaster.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.triviamaster.ui.navigation.Route
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun DailyChallengeScreen(nav: NavController) {
    DailyChallengeContent(
        onBack = { nav.popBackStack() },
        onStart = { nav.navigate(Route.QuizSetup.route) } // start daily challenge → go to setup for now
    )
}

@Composable
private fun DailyChallengeContent(
    onBack: () -> Unit,
    onStart: () -> Unit
) {
    val bg = remember {
        Brush.verticalGradient(listOf(Color(0xFFEAF0FF), Color(0xFFF6F8FF)))
    }
    val scroll = rememberScrollState()

    // ---- “today” strings using legacy java.util to avoid desugaring ----
    val cal = remember { Calendar.getInstance() }
    val dayName = remember {
        cal.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.getDefault()) ?: ""
    }
    val dateStr = remember {
        SimpleDateFormat("MMMM d", Locale.getDefault()).format(cal.time)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bg)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .verticalScroll(scroll),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Daily Challenge",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f)
            )
            OutlinedButton(
                onClick = onBack,
                shape = RoundedCornerShape(14.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) { Text("←  Back") }
        }
        Text(
            text = "Complete daily challenges for special rewards",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // Challenge Streak card
        OutlinedCard(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(shape = RoundedCornerShape(999.dp), color = Color(0xFFFFF1E6)) {
                    Text(
                        "🔥  Challenge Streak",
                        color = Color(0xFF8A3C11),
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
                Spacer(Modifier.weight(1f))
                Text(
                    "0",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                "Start your streak today!",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 2.dp)
            )
            Spacer(Modifier.height(10.dp))
        }

        // Today’s challenge card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(16.dp)) {
                // Title row with "Available" tag
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "⭐  $dayName",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Pill(text = "Available", bg = Color(0xFFEFF6FF), fg = Color(0xFF1E4E8C))
                }
                Text(
                    text = dateStr,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(Modifier.height(12.dp))
                Text(
                    "Journey through the pages of history!",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(Modifier.height(8.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Chip(text = "medium", bg = Color(0xFFFFF5D9), fg = Color(0xFF7A5E13))
                    Chip(text = "History", bg = Color(0xFFEFF6FF), fg = Color(0xFF1E4E8C))
                    Chip(text = "10 Questions", bg = Color(0xFFF3F4F6), fg = Color(0xFF4B5563))
                }

                Spacer(Modifier.height(12.dp))

                // Reward box
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFFF9F5FF),
                    border = BorderStroke(1.dp, Color(0xFFE9D8FD)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(Modifier.padding(12.dp)) {
                        Text(
                            "🎁  Reward",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF553C9A)
                        )
                        Spacer(Modifier.height(8.dp))
                        Bullet(text = "75 bonus points")
                        Bullet(text = "Challenge streak boost")
                    }
                }

                Spacer(Modifier.height(14.dp))

                // Start button
                Button(
                    onClick = onStart,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF0B0B1A),
                        contentColor = Color.White
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                ) { Text("🚀  Start Challenge", fontWeight = FontWeight.SemiBold) }
            }
        }

        // Rules card
        OutlinedCard(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "📋  Challenge Rules",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Spacer(Modifier.height(12.dp))
                Bullet("Daily challenges reset every day at midnight")
                Bullet("Each challenge contains 10 carefully selected questions")
                Bullet("Complete challenges to earn bonus points and maintain your streak")
                Bullet("Special badges and rewards are available for certain challenges")
                Bullet("Missed days will break your challenge streak")
            }
        }

        Spacer(Modifier.height(8.dp))
    }
}

/* ---------- Tiny UI helpers ---------- */

@Composable
private fun Chip(
    text: String,
    bg: Color,
    fg: Color
) {
    Surface(shape = RoundedCornerShape(999.dp), color = bg) {
        Text(
            text = text,
            color = fg,
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            maxLines = 1
        )
    }
}

@Composable
private fun Pill(
    text: String,
    bg: Color,
    fg: Color
) {
    Surface(shape = RoundedCornerShape(999.dp), color = bg) {
        Text(
            text = text,
            color = fg,
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            maxLines = 1
        )
    }
}

@Composable
private fun Bullet(text: String) {
    Row(
        verticalAlignment = Alignment.Top,
        modifier = Modifier.padding(vertical = 6.dp)
    ) {
        Text("•  ", color = MaterialTheme.colorScheme.onSurface)
        Text(
            text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Preview(showBackground = true, widthDp = 320, heightDp = 640)
@Composable
private fun DailyChallengePreview() {
    MaterialTheme { DailyChallengeContent(onBack = {}, onStart = {}) }
}
