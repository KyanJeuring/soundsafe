package com.example.soundsafe.ui.compose

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController

sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    object Dashboard : Screen("dashboard", "Dashboard", Icons.Default.Dashboard)
    object Analytics : Screen("analytics", "Analytics", Icons.AutoMirrored.Filled.List)
    object Settings : Screen("settings", "Settings", Icons.Default.Settings)
}

@Composable
fun SoundSafeApp(
    currentDbLevel: String,
    isDarkModeEnabled: Boolean,
    onThemeToggle: (Boolean) -> Unit,
    isRecording: Boolean,
    onToggleRecording: () -> Unit,
    isAutoMediaEnabled: Boolean,
    onAutoMediaToggle: (Boolean) -> Unit,
    isAutoRingtoneEnabled: Boolean,
    onAutoRingtoneToggle: (Boolean) -> Unit
) {
    val navController = rememberNavController()
    val screens = listOf(Screen.Dashboard, Screen.Analytics, Screen.Settings)

    SoundSafeTheme(darkTheme = isDarkModeEnabled) {
        Scaffold(
            bottomBar = {
                NavigationBar {
                    val navBackStackEntry by navController.currentBackStackEntryAsState()
                    val currentDestination = navBackStackEntry?.destination
                    screens.forEach { screen ->
                        NavigationBarItem(
                            icon = { Icon(screen.icon, contentDescription = screen.label) },
                            label = { Text(screen.label) },
                            selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = Screen.Dashboard.route,
                modifier = Modifier.padding(innerPadding)
            ) {
                composable(Screen.Dashboard.route) {
                    DashboardScreen(
                        currentDbLevel = currentDbLevel,
                        isRecording = isRecording,
                        onToggleRecording = onToggleRecording
                    )
                }
                composable(Screen.Analytics.route) {
                    AnalyticsScreen()
                }
                composable(Screen.Settings.route) {
                    SettingsScreen(
                        isDarkModeEnabled = isDarkModeEnabled,
                        onThemeToggle = onThemeToggle,
                        isAutoMediaEnabled = isAutoMediaEnabled,
                        onAutoMediaToggle = onAutoMediaToggle,
                        isAutoRingtoneEnabled = isAutoRingtoneEnabled,
                        onAutoRingtoneToggle = onAutoRingtoneToggle
                    )
                }
            }
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
fun SoundSafeAppPreview() {
    SoundSafeApp(
        currentDbLevel = "45.5",
        isDarkModeEnabled = false,
        onThemeToggle = {},
        isRecording = true,
        onToggleRecording = {},
        isAutoMediaEnabled = true,
        onAutoMediaToggle = {},
        isAutoRingtoneEnabled = false,
        onAutoRingtoneToggle = {}
    )
}
