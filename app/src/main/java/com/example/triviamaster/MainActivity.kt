package com.example.triviamaster

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.triviamaster.ui.navigation.Route
import com.example.triviamaster.ui.screens.AchievementsScreen
import com.example.triviamaster.ui.screens.DailyChallengeScreen
import com.example.triviamaster.ui.screens.DashboardScreen
import com.example.triviamaster.ui.screens.LeaderboardScreen
import com.example.triviamaster.ui.screens.QuizHistoryScreen
import com.example.triviamaster.ui.screens.QuizScreen
import com.example.triviamaster.ui.screens.QuizSetupScreen
import com.example.triviamaster.ui.screens.RegisterScreen
import com.example.triviamaster.ui.screens.ResultsScreen
import com.example.triviamaster.ui.screens.SignInScreen
import com.example.triviamaster.ui.screens.SpeedModeScreen
import com.example.triviamaster.ui.screens.StatisticsScreen
import com.google.firebase.auth.FirebaseAuth

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val nav = rememberNavController()

            // Start on SignIn unless a user is already authenticated
            val startDestination = if (FirebaseAuth.getInstance().currentUser != null) {
                Route.Dashboard.route
            } else {
                Route.SignIn.route
            }

            MaterialTheme {
                Scaffold(

                ) { innerPadding ->
                    NavHost(
                        navController = nav,
                        startDestination = startDestination,
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable(Route.SignIn.route)   { SignInScreen(nav) }
                        composable(Route.Register.route) { RegisterScreen(nav) }
                        composable(Route.Dashboard.route){ DashboardScreen(nav) }
                        composable(Route.QuizSetup.route){ QuizSetupScreen(nav) }
                        composable(Route.Quiz.route)     { QuizScreen(nav) }
                        composable(Route.Results.route)   { ResultsScreen(nav) }

                        composable(Route.Leaderboard.route)     { LeaderboardScreen(nav) }
                        composable(Route.Achievements.route)    { AchievementsScreen(nav) }
                        composable(Route.Statistics.route)      { StatisticsScreen(nav) }
                        composable(Route.QuizHistory.route)     { QuizHistoryScreen(nav) }
                        composable(Route.SpeedMode.route)       { SpeedModeScreen(nav) }
                        composable(Route.DailyChallenge.route)  { DailyChallengeScreen(nav) }
                    }
                }
            }
        }
    }
}
