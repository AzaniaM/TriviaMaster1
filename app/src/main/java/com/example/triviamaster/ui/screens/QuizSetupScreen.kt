// app/src/main/java/com/example/triviamaster/ui/screens/QuizSetupScreen.kt
package com.example.triviamaster.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.triviamaster.ui.navigation.Route
import com.example.triviamaster.ui.quiz.QuizViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizSetupScreen(
    nav: NavController,
    vm: QuizViewModel = viewModel()
) {
    val state by vm.state.collectAsState()

    // Load categories once when screen opens
    LaunchedEffect(Unit) { vm.loadCategories() }

    // Navigate to Quiz ONLY after questions are ready
    LaunchedEffect(state.started, state.loading, state.questions) {
        if (state.started && !state.loading && state.questions.isNotEmpty()) {
            nav.navigate(Route.Quiz.route)
        }
    }

    val bg = remember {
        Brush.verticalGradient(listOf(Color(0xFFEAF0FF), Color(0xFFF5F7FF)))
    }
    val darkCta = Color(0xFF0B0B1A)

    // -------- Local UI state --------
    val categories = remember(state.categories) { listOf(null to "Any Category") + state.categories }
    var catExpanded by remember { mutableStateOf(false) }
    var catIndex by remember { mutableStateOf(0) }

    val diffs = listOf("Any Difficulty", "Easy", "Medium", "Hard")
    var diffExpanded by remember { mutableStateOf(false) }
    var diffIndex by remember { mutableStateOf(0) }

    val counts = listOf(5, 10, 15, 20, 25, 30)
    var countExpanded by remember { mutableStateOf(false) }
    var countIndex by remember { mutableStateOf(1) } // default 10

    var speedMode by remember { mutableStateOf(false) }
    var launching by remember { mutableStateOf(false) } // show spinner in Start button

    val canStart = !state.loading && !launching

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bg)
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Quiz Setup",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold)
        )
        Text(
            "Customize your quiz experience",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // ----- Category -----
                Text("Category", style = MaterialTheme.typography.labelLarge)

                when {
                    state.loadingCategories -> {
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
                            Spacer(Modifier.width(10.dp))
                            Text("Loading categories…", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    state.error != null && state.categories.isEmpty() -> {
                        Row(
                            Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text("Failed to load categories", color = MaterialTheme.colorScheme.error)
                                Spacer(Modifier.height(2.dp))
                                Text(
                                    state.error ?: "",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            OutlinedButton(onClick = { vm.loadCategories() }) { Text("Retry") }
                        }
                    }
                    else -> {
                        ExposedDropdownMenuBox(
                            expanded = catExpanded,
                            onExpandedChange = { catExpanded = it }
                        ) {
                            OutlinedTextField(
                                value = categories[catIndex].second,
                                onValueChange = {},
                                readOnly = true,
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = catExpanded) },
                                modifier = Modifier
                                    .menuAnchor()
                                    .fillMaxWidth()
                            )
                            ExposedDropdownMenu(
                                expanded = catExpanded,
                                onDismissRequest = { catExpanded = false }
                            ) {
                                categories.forEachIndexed { i, pair ->
                                    DropdownMenuItem(
                                        text = { Text(pair.second) },
                                        onClick = { catIndex = i; catExpanded = false }
                                    )
                                }
                            }
                        }
                    }
                }

                // ----- Difficulty -----
                Text("Difficulty", style = MaterialTheme.typography.labelLarge)
                ExposedDropdownMenuBox(expanded = diffExpanded, onExpandedChange = { diffExpanded = it }) {
                    OutlinedTextField(
                        value = diffs[diffIndex],
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = diffExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = diffExpanded,
                        onDismissRequest = { diffExpanded = false }
                    ) {
                        diffs.forEachIndexed { i, label ->
                            DropdownMenuItem(
                                text = { Text(label) },
                                onClick = { diffIndex = i; diffExpanded = false }
                            )
                        }
                    }
                }

                // ----- Number of Questions -----
                Text("Number of Questions", style = MaterialTheme.typography.labelLarge)
                ExposedDropdownMenuBox(expanded = countExpanded, onExpandedChange = { countExpanded = it }) {
                    OutlinedTextField(
                        value = "${counts[countIndex]} Questions",
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = countExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = countExpanded,
                        onDismissRequest = { countExpanded = false }
                    ) {
                        counts.forEachIndexed { i, c ->
                            DropdownMenuItem(
                                text = { Text("$c Questions") },
                                onClick = { countIndex = i; countExpanded = false }
                            )
                        }
                    }
                }

                // ----- Speed Mode -----
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFFF9F3FF),
                    border = BorderStroke(1.dp, Color(0xFFE7D8FF)),
                    tonalElevation = 0.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("⚡ Speed Mode", style = MaterialTheme.typography.labelLarge)
                                Spacer(Modifier.width(8.dp))
                                AssistChip(onClick = {}, label = { Text("New!") }, enabled = false)
                            }
                            Spacer(Modifier.height(4.dp))
                            Text(
                                "Race against the clock for extra challenge",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(checked = speedMode, onCheckedChange = { speedMode = it })
                    }
                }

                // ----- Start / Back -----
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        onClick = {
                            val selectedCategoryId: Int? = categories[catIndex].first
                            val selectedDifficulty: String? = when (diffIndex) {
                                1 -> "easy"; 2 -> "medium"; 3 -> "hard"; else -> null
                            }
                            val count = counts[countIndex]

                            launching = true
                            vm.startQuiz(
                                categoryId = selectedCategoryId,
                                difficulty = selectedDifficulty,
                                count = count,
                                speedMode = speedMode
                            )
                            // No direct nav here; LaunchedEffect above will handle it.
                        },
                        enabled = canStart,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = darkCta,
                            contentColor = Color.White
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                    ) {
                        if (state.loading || launching) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                                color = Color.White
                            )
                            Spacer(Modifier.width(10.dp))
                        }
                        Text("Start Quiz", fontWeight = FontWeight.SemiBold)
                    }

                    OutlinedButton(
                        onClick = { nav.popBackStack() },
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                    ) { Text("Back") }
                }
            }
        }
    }
}
