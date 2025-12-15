package com.jurobil.progressian.ui.navigation

sealed class Routes(val route: String) {
    object Home : Routes("home")
    object Settings : Routes("settings")

    object Login : Routes("login")
    object Register : Routes("register")

    data class HabitDetail(val habitId: String) : Routes("habit_detail/$habitId") {
        companion object {
            const val ROUTE = "habit_detail/{habitId}"
        }
    }

    data class MissionDetail(val missionId: String) : Routes("mission_detail/$missionId") {
        companion object {
            const val ROUTE = "mission_detail/{missionId}"
        }
    }
}