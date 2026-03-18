package com.aram.benchpress13week.ui.navigation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.aram.benchpress13week.ui.screens.HomeScreen
import com.aram.benchpress13week.ui.screens.ProgramScreen
import com.aram.benchpress13week.ui.screens.SetupScreen
import com.aram.benchpress13week.viewmodel.BenchViewModel

sealed class Screen(val route: String, val title: String) {
    data object Home : Screen("home", "Today")
    data object Program : Screen("program", "Program")
    data object Setup : Screen("setup", "Setup")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BenchApp(viewModel: BenchViewModel) {
    val navController = rememberNavController()
    val snackbarHostState = remember { SnackbarHostState() }
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(state.message) {
        if (state.message.isNotBlank()) {
            snackbarHostState.showSnackbar(state.message)
            viewModel.clearMessage()
        }
    }

    val screens = listOf(Screen.Home, Screen.Program, Screen.Setup)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Powerlifting 13 Week Program", fontWeight = FontWeight.Bold)
                        Text("Track every set. Progress one session at a time.", modifier = Modifier.padding(top = 2.dp))
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
            BottomAppBar {
                screens.forEach { screen ->
                    val selected = currentRoute == screen.route
                    Surface(
                        color = if (selected) {
                            androidx.compose.material3.MaterialTheme.colorScheme.primaryContainer
                        } else {
                            androidx.compose.material3.MaterialTheme.colorScheme.surface
                        },
                        tonalElevation = if (selected) 2.dp else 0.dp,
                        shape = androidx.compose.material3.MaterialTheme.shapes.medium,
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 4.dp, vertical = 6.dp)
                            .clickable {
                                navController.navigate(screen.route) {
                                    launchSingleTop = true
                                }
                            }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .padding(horizontal = 12.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                        ) {
                            Text(
                                text = screen.title,
                                modifier = Modifier.fillMaxWidth(),
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    ) { padding ->
        NavHost(navController = navController, startDestination = Screen.Home.route, modifier = Modifier.padding(padding)) {
            composable(Screen.Home.route) {
                HomeScreen(
                    state = state,
                    onTogglePaused = viewModel::togglePaused,
                    onCompleteWorkout = viewModel::completeWorkout,
                    onPreviousWorkout = viewModel::previousWorkout,
                    onToggleSetCompleted = viewModel::toggleSetCompleted,
                )
            }
            composable(Screen.Program.route) {
                ProgramScreen(state = state)
            }
            composable(Screen.Setup.route) {
                SetupScreen(
                    state = state,
                    onSave = viewModel::saveProfile,
                    onResetProgress = viewModel::resetProgress,
                )
            }
        }
    }
}
