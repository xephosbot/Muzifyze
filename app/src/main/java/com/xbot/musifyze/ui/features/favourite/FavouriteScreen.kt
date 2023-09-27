package com.xbot.musifyze.ui.features.favourite

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

@Composable
fun FavouriteScreenRoute(
    modifier: Modifier = Modifier
) {
    FavouriteScreenContent(modifier = modifier)
}

@Composable
private fun FavouriteScreenContent(
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = Color.Green
    ) {
        Box(modifier = Modifier.fillMaxSize())
    }
}
