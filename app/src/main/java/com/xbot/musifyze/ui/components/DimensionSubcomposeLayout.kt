package com.xbot.musifyze.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.layout.SubcomposeMeasureScope
import androidx.compose.ui.unit.Constraints

/**
 * SubcomposeLayout that [SubcomposeMeasureScope.subcompose] [mainContent]
 * and gets total size of [mainContent] and passes this size to [dependentContent].
 * This layout passes exact size of content unlike
 * BoxWithConstraints which returns [Constraints] that doesn't match Composable dimensions under
 * some circumstances
 *
 * @param mainContent Composable is used for calculating size and pass it
 * to Composables that depend on it
 *
 * @param dependentContent Composable requires dimensions of [mainContent] to set its size.
 * One example for this is overlay over Composable that should match [dependentContent] size.
 *
 */
@Composable
internal fun DimensionSubcomposeLayout(
    modifier: Modifier = Modifier,
    mainContent: @Composable () -> Unit,
    dependentContent: @Composable (Size) -> Unit
) {
    SubcomposeLayout(
        modifier = modifier
    ) { constraints: Constraints ->

        // Subcompose(compose only a section) main content and get Placeable
        val mainPlaceable = subcompose(SlotsEnum.Main, mainContent).map {
            it.measure(constraints.copy(minWidth = 0, minHeight = 0))
        }.maxByOrNull { it.height }

        val dependentContentSize = if (mainPlaceable == null) {
            Size(0f, 0f)
        } else {
            Size(mainPlaceable.width.toFloat(), mainPlaceable.height.toFloat())
        }

        val dependentPlaceable: Placeable = subcompose(SlotsEnum.Dependent) {
            dependentContent(dependentContentSize)
        }.map { measurable: Measurable ->
            measurable.measure(constraints)
        }.first()

        layout(dependentPlaceable.width, dependentPlaceable.height) {
            dependentPlaceable.placeRelative(0, 0)
        }
    }
}

internal enum class SlotsEnum { Main, Dependent }