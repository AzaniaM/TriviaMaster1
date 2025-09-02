package com.example.triviamaster.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.triviamaster.R
import com.example.triviamaster.auth.AuthViewModel
import com.example.triviamaster.ui.navigation.Route
import com.example.triviamaster.ui.quiz.UserStatsViewModel
import java.util.Calendar

@Composable
fun DashboardScreen(
    nav: NavController,
    authVm: AuthViewModel = viewModel(),
    statsVm: UserStatsViewModel = viewModel()
) {
    val auth by authVm.state.collectAsState()
    val stats by statsVm.ui.collectAsState()

    val name = remember(stats.displayName, auth.displayName, auth.email) {
        stats.displayName
            ?: auth.displayName
            ?: auth.email?.substringBefore("@")
            ?: "player"
    }

    DashboardContent(
        nav = nav,
        displayName = name,
        onStartNewQuiz = { nav.navigate(Route.QuizSetup.route) },
        onLogout = {
            authVm.signOut()
            nav.navigate(Route.SignIn.route) {
                popUpTo(Route.Dashboard.route) { inclusive = true }
            }
        },
        quizzes = stats.stats.quizzes,
        accuracyPct = stats.stats.accuracyPct,
        streak = stats.stats.streak,
        badges = stats.stats.badges
    )
}

/* ----------------------- Pure UI (scrollable) ----------------------- */

@Composable
private fun DashboardContent(
    nav: NavController,
    displayName: String,
    onStartNewQuiz: () -> Unit,
    onLogout: () -> Unit,
    quizzes: Int,
    accuracyPct: Int,
    streak: Int,
    badges: Int
) {
    val bg = remember {
        Brush.verticalGradient(
            listOf(Color(0xFFEAF0FF), Color(0xFFEAF0FF), Color(0xFFF7F9FF))
        )
    }
    val darkCta = Color(0xFF0B0B1A)
    val scroll = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bg)
            .verticalScroll(scroll)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Header
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
            elevation = CardDefaults.cardElevation(0.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(R.drawable.ic_owl_logo),
                    contentDescription = "Logo",
                    modifier = Modifier
                        .size(22.dp)
                        .clip(CircleShape)
                )
                Spacer(Modifier.width(8.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        text = "Trivia Master",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                    )
                    Text(
                        text = "${greeting()}, $displayName!",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                OutlinedButton(
                    onClick = onLogout,
                    shape = RoundedCornerShape(20.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text("Logout")
                }
            }
        }

        // 2x2 stat cards — bound to Firestore values
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatMiniCard("Quizzes", quizzes.toString(), Modifier.weight(1f))
            StatMiniCard("Accuracy", "$accuracyPct%", Modifier.weight(1f))
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatMiniCard("Streak", streak.toString(), Modifier.weight(1f))
            StatMiniCard("Badges", badges.toString(), Modifier.weight(1f))
        }

        // Ready for a Challenge card
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("🎯", style = MaterialTheme.typography.headlineMedium)
                Spacer(Modifier.height(6.dp))
                Text(
                    "Ready for a Challenge?",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "Start your streak today!",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = onStartNewQuiz,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = darkCta,
                        contentColor = Color.White
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                ) { Text("Start New Quiz", fontWeight = FontWeight.SemiBold) }
            }
        }

        // Feature tiles (clickable)
        FeatureTile("Leaderboard", "See top players globally", "🏆") {
            nav.navigate(Route.Leaderboard.route)
        }
        FeatureTile("Achievements", "Unlock badges & rewards", "🏅") {
            nav.navigate(Route.Achievements.route)
        }
        FeatureTile("Statistics", "Track your progress", "📊") {
            nav.navigate(Route.Statistics.route)
        }
        FeatureTile("Quiz History", "Review past attempts", "🧾") {
            nav.navigate(Route.QuizHistory.route)
        }
        FeatureTile("Speed Mode", "Race against the clock", "⚡", tag = "Active") {
            nav.navigate(Route.SpeedMode.route)
        }
        FeatureTile("Daily Challenge", "Special daily quiz", "🎲", tag = "Available") {
            nav.navigate(Route.DailyChallenge.route)
        }

        Spacer(Modifier.height(8.dp))
    }
}

/* ----------------------------- Pieces ----------------------------- */

@Composable
private fun StatMiniCard(title: String, value: String, modifier: Modifier = Modifier) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = modifier.height(86.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(2.dp))
            Text(
                title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun FeatureTile(
    title: String,
    subtitle: String,
    emoji: String,
    tag: String? = null,
    onClick: () -> Unit
) {
    OutlinedCard(
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.outlinedCardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFE3E7F2))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(emoji, style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(6.dp))
            Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(2.dp))
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (tag != null) {
                Spacer(Modifier.height(10.dp))
                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = Color(0xFFEFF2FA)
                ) {
                    Text(
                        tag,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}

/* ------------------------- Helpers & Preview ------------------------- */

private fun greeting(): String {
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    return when (hour) {
        in 5..11 -> "Good morning"
        in 12..16 -> "Good afternoon"
        else -> "Good evening"
    }
}

@Preview(showBackground = true, widthDp = 300, heightDp = 640)
@Composable
private fun DashboardPreview() {
    MaterialTheme {
        // Simple preview of the content; navigation is not wired in preview.
        // Replace the stat numbers with sample values.
        // You can ignore the 'nav' param in preview by providing a dummy lambda.
        // (Or make a Preview-only overload if you prefer.)
        // This preview is for layout only.
        // If you want a live preview with NavController, create a separate @Preview using
        // rememberNavController() in a previewable composable.
    }
}
