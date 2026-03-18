package com.aram.benchpress13week.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
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
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
            NavigationBar {
                screens.forEach { screen ->
                    NavigationBarItem(
                        selected = currentRoute == screen.route,
                        onClick = { navController.navigate(screen.route) },
                        label = { Text(screen.title) },
                        icon = {}
                    )
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
