package com.xbot.musifyze.ui.features.home

import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ListItem
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun HomeScreenRoute(
    viewModel: HomeViewModel = hiltViewModel()
) {
    val state = viewModel.state.collectAsStateWithLifecycle()

    HomeScreenContent(
        state = state.value,
        play = viewModel::play
    )
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun HomeScreenContent(
    modifier: Modifier = Modifier,
    state: HomeScreenState,
    play: (Uri) -> Unit
) {
    Surface(modifier = Modifier.fillMaxSize()) {
        LazyColumn {
            items(state.audioList) {
                ListItem(
                    modifier = Modifier.fillMaxWidth()
                        .clickable {
                            play(it.uri)
                        },
                    text = {
                        Text(text = it.name)
                    }
                )
            }
        }
    }
}
