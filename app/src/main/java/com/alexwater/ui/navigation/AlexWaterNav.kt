package com.alexwater.ui.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.alexwater.ui.components.BottomNavBar
import com.alexwater.ui.home.HomeScreen
import com.alexwater.ui.reminders.RemindersScreen
import com.alexwater.ui.stats.StatsScreen
import com.alexwater.ui.history.HistoryScreen
import com.alexwater.ui.settings.SettingsScreen

sealed class Screen(val route: String, val title: String, val index: Int) {
    object Home : Screen("home", "主页", 0)
    object Reminders : Screen("reminders", "提醒", 1)
    object Stats : Screen("stats", "统计", 2)
    object History : Screen("history", "历史", 3)
    object Settings : Screen("settings", "设置", 4)
}

val bottomNavItems = listOf(
    Screen.Home,
    Screen.Reminders,
    Screen.Stats,
    Screen.History,
    Screen.Settings,
)

private val allScreens = bottomNavItems
private fun screenIndex(route: String?) = allScreens.find { it.route == route }?.index ?: 0

@Composable
fun AlexWaterApp() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: Screen.Home.route

    // Track previous index for direction-aware animation
    var previousIndex by remember { mutableIntStateOf(0) }
    val currentIndex = screenIndex(currentRoute)
    val goingRight = currentIndex >= previousIndex

    // Update previousIndex after using it
    LaunchedEffect(currentRoute) {
        previousIndex = currentIndex
    }

    Scaffold(
        bottomBar = {
            BottomNavBar(
                currentTab = currentRoute,
                onTabSelected = { route ->
                    if (route != currentRoute) {
                        navController.navigate(route) {
                            popUpTo(Screen.Home.route) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding),
            enterTransition = {
                if (goingRight) {
                    slideIntoContainer(
                        towards = AnimatedContentTransitionScope.SlideDirection.Left,
                        animationSpec = tween(220)
                    ) + fadeIn(animationSpec = tween(220))
                } else {
                    slideIntoContainer(
                        towards = AnimatedContentTransitionScope.SlideDirection.Right,
                        animationSpec = tween(220)
                    ) + fadeIn(animationSpec = tween(220))
                }
            },
            exitTransition = {
                if (goingRight) {
                    slideOutOfContainer(
                        towards = AnimatedContentTransitionScope.SlideDirection.Left,
                        animationSpec = tween(220)
                    ) + fadeOut(animationSpec = tween(220))
                } else {
                    slideOutOfContainer(
                        towards = AnimatedContentTransitionScope.SlideDirection.Right,
                        animationSpec = tween(220)
                    ) + fadeOut(animationSpec = tween(220))
                }
            },
            popEnterTransition = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Right,
                    animationSpec = tween(220)
                ) + fadeIn(animationSpec = tween(220))
            },
            popExitTransition = {
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Right,
                    animationSpec = tween(220)
                ) + fadeOut(animationSpec = tween(220))
            },
        ) {
            composable(Screen.Home.route) { HomeScreen(viewModel = viewModel()) }
            composable(Screen.Reminders.route) { RemindersScreen(viewModel = viewModel()) }
            composable(Screen.Stats.route) { StatsScreen(viewModel = viewModel()) }
            composable(Screen.History.route) { HistoryScreen(viewModel = viewModel()) }
            composable(Screen.Settings.route) { SettingsScreen(viewModel = viewModel()) }
        }
    }
}
