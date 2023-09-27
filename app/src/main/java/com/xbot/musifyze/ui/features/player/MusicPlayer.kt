package com.xbot.musifyze.ui.features.player

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ContentAlpha
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ProvideTextStyle
import androidx.compose.material.Scaffold
import androidx.compose.material.SliderColors
import androidx.compose.material.SliderDefaults
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import com.xbot.musifyze.R
import com.xbot.musifyze.ui.components.BottomSheetState
import com.xbot.musifyze.ui.components.IconButton
import com.xbot.musifyze.ui.components.Slider
import com.xbot.musifyze.ui.components.TopAppBar
import com.xbot.musifyze.ui.theme.DynamicTheme
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun MusicPlayerBottomSheet(
    modifier: Modifier = Modifier,
    state: BottomSheetState
) {
    val scope = rememberCoroutineScope()

    Box(modifier = modifier.fillMaxSize()) {
        DynamicTheme(R.drawable.album_cover04) {
            MusicPlayerContent(
                albumTitle = "PVL IS BACK",
                onCollapse = {
                    scope.launch { state.collapse() }
                }
            )
        }
        if (state.progress > 0f) {
            MusicPlayerAppbar(
                state = state,
                title = "PVL IS BACK - Gasoline"
            )
        }
    }
}

@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
private fun MusicPlayerContent(
    modifier: Modifier = Modifier,
    backgroundColor: Color = DynamicTheme.colors.muted,
    contentColor: Color = MaterialTheme.colors.onSurface,
    albumTitle: String,
    onCollapse: () -> Unit = {},
    onMore: () -> Unit = {}
) {
    GradientBackground {
        Scaffold(
            modifier = modifier,
            topBar = {
                TopAppBar(
                    navigationIcon = {
                        IconButton(onClick = onCollapse) {
                            Icon(
                                imageVector = Icons.Default.ExpandMore,
                                contentDescription = "Collapse"
                            )
                        }
                    },
                    title = {
                        Text(text = albumTitle)
                    },
                    actions = {
                        IconButton(onClick = onMore) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "More"
                            )
                        }
                    },
                    backgroundColor = Color.Transparent,
                    centeredTitle = true,
                    elevation = 0.dp
                )
            },
            backgroundColor = Color.Transparent,
            contentColor = contentColor
        ) { innerPadding ->
            ConstraintLayout(
                modifier = Modifier
                    .fillMaxSize()
                    .navigationBarsPadding()
                    .padding(innerPadding)
                    .padding(horizontal = 32.dp)
            ) {
                val (albumCover, title, controls) = createRefs()

                AlbumCover(
                    modifier = Modifier
                        .constrainAs(albumCover) {
                            top.linkTo(parent.top, margin = 16.dp)
                            start.linkTo(parent.start)
                            end.linkTo(parent.end)
                        },
                    painter = painterResource(R.drawable.album_cover04)
                )

                AlbumTitleContainer(
                    modifier = Modifier
                        .constrainAs(title) {
                            top.linkTo(albumCover.bottom, margin = 16.dp)
                            start.linkTo(parent.start)
                            end.linkTo(parent.end)
                        },
                    title = { Text(text = "Сказать") },
                    subtitle = { Text(text = "Скриптонит, ALBLACK 52") }
                )

                Column(
                    modifier = Modifier
                        .constrainAs(controls) {
                            bottom.linkTo(parent.bottom, margin = 64.dp)
                            start.linkTo(parent.start)
                            end.linkTo(parent.end)
                        },
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    var progress by remember { mutableFloatStateOf(0f) }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        IconButton(onClick = { /*TODO*/ }) {
                            Icon(
                                imageVector = Icons.Default.QueueMusic,
                                contentDescription = "Queue"
                            )
                        }
                        IconButton(
                            onClick = { /*TODO*/ },
                        ) {
                            Icon(
                                imageVector = Icons.Default.FavoriteBorder,
                                contentDescription = "Favorite"
                            )
                        }
                        IconButton(onClick = { /*TODO*/ }) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add"
                            )
                        }
                    }

                    DynamicSlider(
                        value = progress,
                        onValueChange = { progress = it }
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        IconButton(onClick = { /*TODO*/ }) {
                            Icon(
                                imageVector = Icons.Default.Shuffle,
                                contentDescription = "Shuffle"
                            )
                        }
                        androidx.compose.material.IconButton(onClick = { /*TODO*/ }) {
                            Icon(
                                modifier = Modifier.size(36.dp),
                                imageVector = Icons.Default.SkipPrevious,
                                contentDescription = "Previous"
                            )
                        }
                        androidx.compose.material.IconButton(onClick = { /*TODO*/ },) {
                            Icon(
                                modifier = Modifier.size(36.dp),
                                imageVector = Icons.Default.Pause,
                                contentDescription = "Pause"
                            )
                        }
                        androidx.compose.material.IconButton(onClick = { /*TODO*/ }) {
                            Icon(
                                modifier = Modifier.size(36.dp),
                                imageVector = Icons.Default.SkipNext,
                                contentDescription = "Next"
                            )
                        }
                        IconButton(onClick = { /*TODO*/ }) {
                            Icon(
                                imageVector = Icons.Default.Repeat,
                                contentDescription = "Repeat"
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AlbumCover(
    modifier: Modifier = Modifier,
    painter: Painter
) {
    Image(
        modifier = modifier
            .aspectRatio(1.0f)
            .fillMaxSize()
            .clip(RoundedCornerShape(12.dp)),
        painter = painter,
        contentDescription = "Album Cover"
    )
}

@Composable
private fun AlbumTitleContainer(
    modifier: Modifier = Modifier,
    titleTextStyle: TextStyle = MaterialTheme.typography.h6,
    subtitleTextStyle: TextStyle = MaterialTheme.typography.subtitle1,
    title: @Composable () -> Unit,
    subtitle: @Composable () -> Unit,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ProvideTextStyle(value = titleTextStyle) {
            CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.high) {
                title()
            }
        }
        ProvideTextStyle(value = subtitleTextStyle) {
            CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                subtitle()
            }
        }
    }
}

@Composable
private fun DynamicSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    enabled: Boolean = true,
    onValueChangeFinished: (() -> Unit)? = null,
    colors: SliderColors = SliderDefaults.colors(
        thumbColor = MaterialTheme.colors.onSurface,
        activeTrackColor = MaterialTheme.colors.onSurface
    ),
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
) {
    Slider(
        value = value,
        onValueChange = onValueChange,
        enabled = enabled,
        onValueChangeFinished = onValueChangeFinished,
        colors = colors,
        interactionSource = interactionSource
    )
}

internal val MusicPlayerMinHeight = 48.dp
