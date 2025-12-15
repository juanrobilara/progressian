package com.jurobil.progressian.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.jurobil.progressian.ui.screens.habitDetailScreen.HabitDetailScreen
import com.jurobil.progressian.ui.screens.homeScreen.HomeScreen
import com.jurobil.progressian.ui.screens.loginScreen.LoginScreen
import com.jurobil.progressian.ui.screens.missionDetailScreen.MissionDetailScreen
import com.jurobil.progressian.ui.screens.registerScreen.RegisterScreen

@Composable
fun NavigationWrapper() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val showBottomBar = currentDestination?.route in listOf(
        Routes.Home.route,
        Routes.Feed.route,
        Routes.Profile.route
    )

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    BottomNavItems.list.forEach { item ->
                        val isSelected = currentDestination?.hierarchy?.any { it.route == item.route } == true

                        NavigationBarItem(
                            icon = { Icon(item.icon, contentDescription = item.title) },
                            label = { Text(item.title) },
                            selected = isSelected,
                            onClick = {
                                navController.navigate(item.route) {
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
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = Routes.Home.route,
            modifier = Modifier.padding(paddingValues)
        ) {

            composable(route = Routes.Home.route) {
                HomeScreen(
                    onHabitClick = { habitId ->
                        navController.navigate(Routes.HabitDetail(habitId).route)
                    },
                    onSettingsClick = {
                        navController.navigate(Routes.Settings.route)
                    },
                    onNavigateToLogin = {
                        navController.navigate(Routes.Login.route)
                    }
                )
            }

            composable(route = Routes.Feed.route) {
                Text("Pantalla de Feed en construcción")
            }

            composable(route = Routes.Profile.route) {
                Text("Pantalla de Perfil en construcción")
            }

            composable(
                route = Routes.HabitDetail.ROUTE,
            ) {
                HabitDetailScreen(
                    onBack = { navController.popBackStack() },
                    onMissionClick = { missionId ->
                        navController.navigate(Routes.MissionDetail(missionId).route)
                    }
                )
            }

            composable(route = Routes.MissionDetail.ROUTE) {
                MissionDetailScreen(
                    onBack = { navController.popBackStack() }
                )
            }

            composable(route = Routes.Login.route) {
                LoginScreen(
                    onLoginSuccess = {
                        navController.navigate(Routes.Home.route) {
                            popUpTo(Routes.Home.route) { inclusive = true }
                        }
                    },
                    onNavigateToRegister = {
                        navController.navigate(Routes.Register.route)
                    },
                    onBack = { navController.popBackStack() }
                )
            }

            composable(route = Routes.Register.route) {
                RegisterScreen(
                    onRegisterSuccess = {
                        navController.navigate(Routes.Home.route) {
                            popUpTo(Routes.Home.route) { inclusive = true }
                        }
                    },
                    onBack = { navController.popBackStack() }
                )
            }

            composable(route = Routes.Settings.route) {
                // SettingsScreen(...)
            }
        }
    }
}