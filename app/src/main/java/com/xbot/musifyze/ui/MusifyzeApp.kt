package com.xbot.musifyze.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.AppBarDefaults
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import com.xbot.musifyze.R
import com.xbot.musifyze.ui.components.BottomSheetScaffold
import com.xbot.musifyze.ui.components.IconButton
import com.xbot.musifyze.ui.components.TopAppBar
import com.xbot.musifyze.ui.components.pinnedScrollBehavior
import com.xbot.musifyze.ui.components.rememberBottomSheetScaffoldState
import com.xbot.musifyze.ui.features.player.MusicPlayerBottomSheet
import com.xbot.musifyze.ui.features.player.MusicPlayerMinHeight
import com.xbot.musifyze.ui.theme.MusifyzeTheme

@Composable
fun MusifyzeApp(
    modifier: Modifier = Modifier
) {
    MusifyzeAppContent(modifier = modifier)
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun MusifyzeAppContent(
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()
    val scaffoldState = rememberBottomSheetScaffoldState()
    val scrollBehavior = AppBarDefaults.pinnedScrollBehavior()

    BottomSheetScaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        scaffoldState = scaffoldState,
        sheetContent = {
            MusicPlayerBottomSheet(
                state = scaffoldState.bottomSheetState
            )
        },
        sheetPeekHeight = MusicPlayerMinHeight,
        topBar = {
            TopAppBar(
                title = {
                    Text(text = stringResource(id = R.string.app_name))
                },
                navigationIcon = {
                    IconButton(onClick = { /*TODO*/ }) {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = "Account"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { /*TODO*/ }) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search"
                        )
                    }
                },
                scrollBehavior = scrollBehavior
            )
        },
        bottomBar = {
            NavigationBar(
                modifier = Modifier
                    .graphicsLayer {
                        translationY = it
                    },
                destinations = DefaultDestinations,
                navController = navController
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { /*TODO*/ }) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search"
                )
            }
        }
    ) { contentPadding ->
        NavigationGraph(
            modifier = Modifier.padding(contentPadding),
            navController = navController,
            startDestination = BottomNavItem.Radio.route
        )
    }
}


@Preview
@Composable
fun MusifyzeAppPreview() {
    MusifyzeTheme {
        MusifyzeApp()
    }
}