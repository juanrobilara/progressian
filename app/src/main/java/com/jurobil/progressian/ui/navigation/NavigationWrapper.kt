package com.jurobil.progressian.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.jurobil.progressian.ui.screens.habitDetailScreen.HabitDetailScreen
import com.jurobil.progressian.ui.screens.homeScreen.HomeScreen
import com.jurobil.progressian.ui.screens.loginScreen.LoginScreen
import com.jurobil.progressian.ui.screens.missionDetailScreen.MissionDetailScreen
import com.jurobil.progressian.ui.screens.registerScreen.RegisterScreen

@Composable
fun NavigationWrapper() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Routes.Home.route
    ) {

        composable(route = Routes.Home.route) {
            HomeScreen(
                onHabitClick = { habitId ->
                    // Aquí usamos la función helper que creaste en Routes
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

        composable(
            route = Routes.HabitDetail.ROUTE,
            // Argumentos definidos en Routes
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
                    // Al loguearse exitosamente, volvemos al Home y limpiamos el stack de login
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