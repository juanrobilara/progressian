package com.jurobil.progressian.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.graphics.vector.ImageVector

data class BottomNavItem(
    val route: String,
    val title: String,
    val icon: ImageVector
)

object BottomNavItems {
    val list = listOf(
        BottomNavItem(
            route = Routes.Home.route,
            title = "Inicio",
            icon = Icons.Default.Home
        ),
        BottomNavItem(
            route = Routes.Feed.route,
            title = "Feed",
            icon = Icons.Default.Explore
        ),
        BottomNavItem(
            route = Routes.Profile.route,
            title = "Perfil",
            icon = Icons.Default.Person
        )
    )
}