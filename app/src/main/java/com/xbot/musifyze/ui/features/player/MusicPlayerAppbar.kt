package com.xbot.musifyze.ui.features.player

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.ListItem
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextAlign
import com.xbot.musifyze.ui.components.BottomSheetState
import com.xbot.musifyze.ui.components.IconToggleButton

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun MusicPlayerAppbar(
    modifier: Modifier = Modifier,
    state: BottomSheetState,
    title: String,
    onFavorite: () -> Unit = {},
    onPause: (Boolean) -> Unit = {}
) {
    Surface(
        modifier = modifier
            .fillMaxSize()
            .graphicsLayer {
                alpha = state.progress
            }
    ) {
        Box {
            MusicPlayerAppbarContent(
                title = title,
                onFavorite = onFavorite,
                onPause = onPause
            )
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun MusicPlayerAppbarContent(
    modifier: Modifier = Modifier,
    title: String,
    onFavorite: () -> Unit = {},
    onPause: (Boolean) -> Unit = {},
) {
    ListItem(
        modifier = modifier.height(MusicPlayerMinHeight),
        icon = {
            var state by remember { mutableStateOf(false) }

            IconToggleButton(
                checked = state,
                onCheckedChange = {
                    state = it
                    onFavorite()
                }
            ) {
                Icon(
                    imageVector = if (state) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "Favorite"
                )
            }
        },
        text = {
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = title,
                textAlign = TextAlign.Center,
                maxLines = 1
            )
        },
        trailing = {
            var state by remember { mutableStateOf(false) }

            IconToggleButton(
                checked = state,
                onCheckedChange = {
                    state = it
                    onPause(it)
                }
            ) {
                Icon(
                    imageVector = if (state) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = "Pause"
                )
            }
        }
    )
}
