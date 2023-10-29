package com.xbot.musifyze.ui

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navigation
import com.xbot.musifyze.ui.components.AppBarScrollBehavior
import com.xbot.musifyze.ui.components.BottomNavigation
import com.xbot.musifyze.ui.components.TopAppBar
import com.xbot.musifyze.ui.features.favourite.FavouriteScreenRoute
import com.xbot.musifyze.ui.features.home.CategoriesScreenRoute
import com.xbot.musifyze.ui.features.home.FolderScreenRoute
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
        navigation(
            startDestination = NavigationItem.Home.Categories.route,
            route = NavigationItem.Home.route
        ) {
            composable(NavigationItem.Home.Categories.route) {
                CategoriesScreenRoute {
                    navController.navigate(NavigationItem.Home.Folder.withArgs(it))
                }
            }
            composable(NavigationItem.Home.Folder.route) {
                FolderScreenRoute()
            }
        }
        composable(NavigationItem.Radio.route) {
            RadioScreenRoute()
        }
        composable(NavigationItem.Favourite.route) {
            FavouriteScreenRoute()
        }
    }
}

@Composable
fun NavigationBar(
    modifier: Modifier = Modifier,
    destinations: List<NavigationItem>,
    navController: NavHostController
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val navBackStack by navController.currentBackStackAsState()
    val navigationRoutes = destinations.map(NavigationItem::route)

    BottomNavigation(
        modifier = modifier,
        backgroundColor = MaterialTheme.colors.surface,
        contentColor = MaterialTheme.colors.onSurface
    ) {
        destinations.forEach { destination ->
            BottomNavigationItem(
                selected = currentDestination?.hierarchy?.any { it.route == destination.route } == true,
                onClick = {
                    val firstTopLevelDestination = navBackStack
                        ?.firstOrNull { navigationRoutes.contains(it.destination.route) }
                        ?.destination

                    navController.navigate(destination.route) {
                        if (firstTopLevelDestination != null) {
                            popUpTo(firstTopLevelDestination.id) {
                                inclusive = true
                                saveState = true
                            }
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = {
                    Icon(
                        imageVector = destination.icon!!,
                        contentDescription = destination.title
                    )
                }
            )
        }
    }
}

@Composable
fun NavigationTopAppBar(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    title: @Composable (String?) -> Unit,
    navigationIcon: @Composable (Boolean) -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
    scrollBehavior: AppBarScrollBehavior? = null
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val titleArg = navBackStackEntry?.arguments?.getString(NavigationItem.TITLE_ARG)
    val topDestination = navController.previousBackStackEntry == null

    TopAppBar(
        modifier = modifier,
        title = { title(titleArg) },
        navigationIcon = { navigationIcon(topDestination) },
        actions = actions,
        scrollBehavior = scrollBehavior
    )
}

val TopLevelDestinations = listOf(NavigationItem.Home, NavigationItem.Radio, NavigationItem.Favourite)

sealed class NavigationItem(
    val title: String? = null,
    val icon: ImageVector? = null,
    val route: String
) {
    data object Home : NavigationItem(
        title = "Home",
        icon = Icons.Default.QueueMusic,
        route = "home"
    ) {
        data object Categories : NavigationItem(
            route = "categories"
        )

        data object Folder : NavigationItem(
            route = "folder/{$TITLE_ARG}"
        )
    }

    data object Radio : NavigationItem(
        title = "Radio",
        icon = Icons.Default.GraphicEq,
        route = "radio"
    )

    data object Favourite : NavigationItem(
        title = "Favourite",
        icon = Icons.Default.FavoriteBorder,
        route = "favourite"
    )

    fun withArgs(vararg args: String): String {
        var tempRoute = route
        val argPattern = "\\{.*?\\}".toRegex()
        val matches = argPattern.findAll(tempRoute).toList()

        if (matches.size != args.size) {
            throw IllegalArgumentException("Number of arguments does not match the number of placeholders in the route.")
        }

        matches.forEachIndexed { index, match ->
            tempRoute = tempRoute.replaceFirst(match.value, args[index])
        }

        return tempRoute
    }

    companion object {
        const val TITLE_ARG = "title"
    }
}

/**
 * Returns the current backStack as a state.
 *
 * This is a temporary solution until a public API is available.
 * Uses Jetpack Compose Navigation's internal API, hence the lint warning suppression.
 *
 * @return A state representing the current backStack.
 */
@SuppressLint("RestrictedApi")
@Composable
fun NavController.currentBackStackAsState(): State<List<NavBackStackEntry>?> {
    return currentBackStack.collectAsState(null)
}
