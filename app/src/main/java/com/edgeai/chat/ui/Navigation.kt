package com.edgeai.chat.ui

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.edgeai.chat.ui.screens.MainScreen
import com.edgeai.chat.ui.screens.ModelManagementScreen
import com.edgeai.chat.ui.screens.SettingsScreen
import com.edgeai.chat.viewmodel.ChatViewModel

sealed class Screen(val route: String) {
    object Chat : Screen("chat")
    object ModelManager : Screen("model_manager")
    object Settings : Screen("settings")
}

@Composable
fun AppNavigation(viewModel: ChatViewModel) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Screen.Chat.route) {
        composable(Screen.Chat.route) {
            MainScreen(
                viewModel = viewModel,
                onNavigateToModelManager = { navController.navigate(Screen.ModelManager.route) },
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) }
            )
        }
        composable(Screen.ModelManager.route) {
            ModelManagementScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }
        composable(Screen.Settings.route) {
            SettingsScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
