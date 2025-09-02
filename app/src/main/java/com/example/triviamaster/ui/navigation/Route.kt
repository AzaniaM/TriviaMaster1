package com.example.triviamaster.ui.navigation

sealed class Route(val route: String) {
    data object SignIn    : Route("sign_in")
    data object Register  : Route("register")
    data object Dashboard : Route("dashboard")
    data object QuizSetup : Route("quiz_setup")
    data object Quiz      : Route("quiz")
    data  object Results    : Route("results")

    data object Leaderboard    : Route("leaderboard")
   data  object Achievements   : Route("achievements")
    data object Statistics     : Route("statistics")
   data object QuizHistory    : Route("quiz_history")
    data object SpeedMode      : Route("speed_mode")
    data object DailyChallenge : Route("daily_challenge")
}
