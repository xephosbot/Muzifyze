package com.xbot.musifyze.ui.theme

import android.graphics.BitmapFactory
import androidx.annotation.DrawableRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.palette.graphics.Palette
import androidx.palette.graphics.Target

@Immutable
data class DynamicColors(
    val lightVibrant: Color,
    val vibrant: Color,
    val darkVibrant: Color,
    val lightMuted: Color,
    val muted: Color,
    val darkMuted: Color,
    val content: Color
)

val LocalDynamicColors = staticCompositionLocalOf {
    DynamicColors(
        lightVibrant = Color.Unspecified,
        vibrant = Color.Unspecified,
        darkVibrant = Color.Unspecified,
        lightMuted = Color.Unspecified,
        muted = Color.Unspecified,
        darkMuted = Color.Unspecified,
        content = Color.Unspecified
    )
}

val DARK = Target.Builder().setMinimumLightness(0f)
    .setTargetLightness(0.26f)
    .setMaximumLightness(0.5f)
    .setMinimumSaturation(0.1f)
    .setTargetSaturation(0.6f)
    .setMaximumSaturation(1f)
    .setPopulationWeight(0.18f)
    .setSaturationWeight(0.22f)
    .setLightnessWeight(0.60f)
    .setExclusive(false)
    .build()

@Composable
fun DynamicTheme(
    @DrawableRes id: Int,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current

    val bitmap = remember {
        BitmapFactory.decodeResource(context.resources, id)
    }

    val palette = remember {
        Palette.from(bitmap).generate()
    }

    val dynamicColors = DynamicColors(
        lightVibrant = palette.lightVibrantSwatch?.let { Color(it.rgb) } ?: Color.Unspecified,
        vibrant = palette.vibrantSwatch?.let { Color(it.rgb) } ?: Color.Unspecified,
        darkVibrant = palette.darkVibrantSwatch?.let { Color(it.rgb) } ?: Color.Unspecified,
        lightMuted = palette.lightMutedSwatch?.let { Color(it.rgb) } ?: Color.Unspecified,
        muted = palette.mutedSwatch?.let { Color(it.rgb) } ?: Color.Unspecified,
        darkMuted = palette.darkMutedSwatch?.let { Color(it.rgb) } ?: Color.Unspecified,
        content = Color.White
    )

    CompositionLocalProvider(
        LocalDynamicColors provides dynamicColors,
        content = content
    )
}

object DynamicTheme {
    val colors: DynamicColors
        @Composable
        get() = LocalDynamicColors.current
}