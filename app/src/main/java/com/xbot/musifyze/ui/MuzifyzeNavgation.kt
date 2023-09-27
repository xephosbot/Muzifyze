package com.xbot.musifyze.ui

import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import com.xbot.musifyze.ui.components.BottomNavigation
import com.xbot.musifyze.ui.features.favourite.FavouriteScreenRoute
import com.xbot.musifyze.ui.features.home.HomeScreenRoute
import com.xbot.musifyze.ui.features.radio.RadioScreenRoute

@Composable
fun NavigationGraph(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    startDestination: String
) {
    NavHost(
        modifier = modifier,
        navController = navController,
        startDestination = startDestination
    ) {
        composable(BottomNavItem.Home.route) {
            HomeScreenRoute()
        }
        composable(BottomNavItem.Radio.route) {
            RadioScreenRoute()
        }
        composable(BottomNavItem.Favourite.route) {
            FavouriteScreenRoute()
        }
    }
}

@Composable
fun NavigationBar(
    modifier: Modifier = Modifier,
    destinations: List<BottomNavItem>,
    navController: NavHostController
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    BottomNavigation(
        modifier = modifier,
        backgroundColor = MaterialTheme.colors.surface,
        contentColor = MaterialTheme.colors.onSurface
    ) {
        destinations.forEach { destination ->
            BottomNavigationItem(
                selected = currentRoute == destination.route,
                onClick = {
                    navController.navigate(destination.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = {
                    Icon(
                        imageVector = destination.icon,
                        contentDescription = destination.title
                    )
                }
            )
        }
    }
}

val DefaultDestinations = listOf(BottomNavItem.Home, BottomNavItem.Radio, BottomNavItem.Favourite)

sealed class BottomNavItem(
    val title: String,
    val icon: ImageVector,
    val route: String
) {
    object Home : BottomNavItem(
        title = "Home",
        icon = Icons.Default.QueueMusic,
        route = "home"
    )

    object Radio : BottomNavItem(
        title = "Radio",
        icon = Icons.Default.GraphicEq,
        route = "radio"
    )

    object Favourite : BottomNavItem(
        title = "Favourite",
        icon = Icons.Default.FavoriteBorder,
        route = "favourite"
    )
}