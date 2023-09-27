package com.xbot.musifyze.ui.components

import androidx.annotation.FloatRange
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.SpringSpec
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.DraggableAnchors
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.anchoredDraggable
import androidx.compose.foundation.gestures.animateTo
import androidx.compose.foundation.gestures.snapTo
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.requiredHeightIn
import androidx.compose.foundation.layout.systemBars
import androidx.compose.material.BottomSheetScaffold
import androidx.compose.material.BottomSheetScaffoldDefaults
import androidx.compose.material.BottomSheetValue
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.FabPosition
import androidx.compose.material.MaterialTheme
import androidx.compose.material.SnackbarHost
import androidx.compose.material.SnackbarHostState
import androidx.compose.material.Surface
import androidx.compose.material.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.runtime.structuralEqualityPolicy
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.collapse
import androidx.compose.ui.semantics.expand
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.offset
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterialApi::class, ExperimentalFoundationApi::class)
@Composable
fun BottomSheetScaffold(
    sheetContent: @Composable ColumnScope.() -> Unit,
    modifier: Modifier = Modifier,
    scaffoldState: BottomSheetScaffoldState = rememberBottomSheetScaffoldState(),
    sheetPeekHeight: Dp = BottomSheetScaffoldDefaults.SheetPeekHeight,
    sheetContainerColor: Color = MaterialTheme.colors.surface,
    sheetContentColor: Color = contentColorFor(sheetContainerColor),
    sheetElevation: Dp = BottomSheetScaffoldDefaults.SheetElevation,
    sheetShape: Shape = RectangleShape,
    sheetGesturesEnabled: Boolean = true,
    topBar: @Composable () -> Unit = {},
    bottomBar: @Composable (bottomSheetOffset: Float) -> Unit = {},
    snackbarHost: @Composable (SnackbarHostState) -> Unit = { SnackbarHost(it) },
    floatingActionButton: @Composable () -> Unit = {},
    floatingActionButtonPosition: FabPosition = FabPosition.End,
    containerColor: Color = MaterialTheme.colors.background,
    contentColor: Color = contentColorFor(containerColor),
    contentWindowInsets: WindowInsets = BottomSheetScaffoldDefaults.contentWindowInsets,
    content: @Composable (PaddingValues) -> Unit
) {
    val density = LocalDensity.current
    SideEffect {
        scaffoldState.bottomSheetState.density = density
    }

    DimensionSubcomposeLayout(
        mainContent = { bottomBar(0f) }
    ) { size ->
        val peekHeightPx = with(LocalDensity.current) {
            sheetPeekHeight.toPx() + max(size.height, contentWindowInsets.getBottom(this).toFloat())
        }

        Surface(
            modifier = modifier,
            color = containerColor,
            contentColor = contentColor
        ) {
            BottomSheetScaffoldLayout(
                fabPosition = floatingActionButtonPosition,
                topBar = topBar,
                bottomBar = bottomBar,
                bottomBarSize = size,
                content = content,
                bottomSheet = { layoutHeight ->
                    val nestedScroll = if (sheetGesturesEnabled) {
                        Modifier
                            .nestedScroll(
                                remember(scaffoldState.bottomSheetState.anchoredDraggableState) {
                                    consumeSwipeWithinBottomSheetBoundsNestedScrollConnection(
                                        state = scaffoldState.bottomSheetState.anchoredDraggableState,
                                        orientation = Orientation.Vertical
                                    )
                                }
                            )
                    } else Modifier
                    StandardBottomSheet(
                        state = scaffoldState.bottomSheetState,
                        modifier = nestedScroll
                            .fillMaxWidth()
                            .requiredHeightIn(min = sheetPeekHeight),
                        calculateAnchors = { sheetSize ->
                            val sheetHeight = sheetSize.height.toFloat()
                            DraggableAnchors {
                                BottomSheetValue.Collapsed at layoutHeight - peekHeightPx
                                if (sheetHeight > 0f && sheetHeight != peekHeightPx) {
                                    BottomSheetValue.Expanded at layoutHeight - sheetHeight
                                }
                            }
                        },
                        containerColor = sheetContainerColor,
                        contentColor = sheetContentColor,
                        elevation = sheetElevation,
                        sheetGesturesEnabled = sheetGesturesEnabled,
                        shape = sheetShape,
                        content = sheetContent
                    )
                },
                snackbarHost = {
                    snackbarHost(scaffoldState.snackbarHostState)
                },
                contentWindowInsets = contentWindowInsets,
                fab = floatingActionButton,
                sheetPeekHeight = sheetPeekHeight,
                sheetOffset = { scaffoldState.bottomSheetState.requireOffset() },
                sheetState = scaffoldState.bottomSheetState
            )
        }
    }
}

@OptIn(ExperimentalMaterialApi::class, ExperimentalFoundationApi::class)
@Composable
private fun StandardBottomSheet(
    modifier: Modifier = Modifier,
    state: BottomSheetState,
    sheetGesturesEnabled: Boolean,
    calculateAnchors: (sheetSize: IntSize) -> DraggableAnchors<BottomSheetValue>,
    shape: Shape,
    containerColor: Color,
    contentColor: Color,
    elevation: Dp,
    content: @Composable ColumnScope.() -> Unit
) {
    val scope = rememberCoroutineScope()

    Surface(
        modifier = modifier
            .anchoredDraggable(
                state = state.anchoredDraggableState,
                orientation = Orientation.Vertical,
                enabled = sheetGesturesEnabled
            )
            .onSizeChanged { layoutSize ->
                val newAnchors = calculateAnchors(layoutSize)
                val newTarget = when (state.targetValue) {
                    BottomSheetValue.Collapsed -> BottomSheetValue.Collapsed
                    BottomSheetValue.Expanded -> if (newAnchors.hasAnchorFor(BottomSheetValue.Expanded)) BottomSheetValue.Expanded else BottomSheetValue.Collapsed
                }
                state.anchoredDraggableState.updateAnchors(newAnchors, newTarget)
            }
            .semantics {
                // If we don't have anchors yet, or have only one anchor we don't want any
                // accessibility actions
                if (state.anchoredDraggableState.anchors.size > 1) {
                    if (state.isCollapsed) {
                        expand {
                            scope.launch { state.expand() }
                            true
                        }
                    } else {
                        collapse {
                            scope.launch { state.collapse() }
                            true
                        }
                    }
                }
            },
        shape = shape,
        elevation = elevation,
        color = containerColor,
        contentColor = contentColor,
        content = { Column(content = content) }
    )
}


@OptIn(ExperimentalMaterialApi::class, ExperimentalFoundationApi::class)
@Composable
private fun BottomSheetScaffoldLayout(
    fabPosition: FabPosition,
    topBar: @Composable () -> Unit,
    content: @Composable (PaddingValues) -> Unit,
    bottomSheet: @Composable (Int) -> Unit,
    snackbarHost: @Composable () -> Unit,
    fab: @Composable () -> Unit,
    contentWindowInsets: WindowInsets,
    bottomBar: @Composable (Float) -> Unit,
    bottomBarSize: Size,
    sheetPeekHeight: Dp,
    sheetOffset: () -> Float,
    sheetState: BottomSheetState
) {
    SideEffect {

    }

    SubcomposeLayout { constraints ->
        val layoutWidth = constraints.maxWidth
        val layoutHeight = constraints.maxHeight

        val looseConstraints = constraints.copy(minWidth = 0, minHeight = 0)

        val sheetPlaceables = subcompose(ScaffoldLayoutContent.Sheet) {
            bottomSheet(layoutHeight)
        }.map { it.measure(looseConstraints) }

        val sheetOffsetY = sheetOffset().roundToInt()

        val sheetWidth = sheetPlaceables.maxByOrNull { it.width }?.width ?: 0
        val sheetOffsetX = max(0, (layoutWidth - sheetWidth) / 2)

        val topBarPlaceables = subcompose(ScaffoldLayoutContent.TopBar, topBar).map {
            it.measure(looseConstraints)
        }
        val topBarHeight = topBarPlaceables.maxByOrNull { it.height }?.height ?: 0

        val snackbarPlaceables = subcompose(ScaffoldLayoutContent.Snackbar, snackbarHost).map {
            // respect only bottom and horizontal for snackbar and fab
            val leftInset = contentWindowInsets
                .getLeft(this@SubcomposeLayout, layoutDirection)
            val rightInset = contentWindowInsets
                .getRight(this@SubcomposeLayout, layoutDirection)
            val bottomInset = contentWindowInsets.getBottom(this@SubcomposeLayout)
            // offset the snackbar constraints by the insets values
            it.measure(
                looseConstraints.offset(
                    -leftInset - rightInset,
                    -bottomInset
                )
            )
        }

        val snackbarHeight = snackbarPlaceables.maxByOrNull { it.height }?.height ?: 0
        val snackbarWidth = snackbarPlaceables.maxByOrNull { it.width }?.width ?: 0

        val fabPlaceables =
            subcompose(ScaffoldLayoutContent.Fab, fab).mapNotNull { measurable ->
                // respect only bottom and horizontal for snackbar and fab
                val leftInset =
                    contentWindowInsets.getLeft(this@SubcomposeLayout, layoutDirection)
                val rightInset =
                    contentWindowInsets.getRight(this@SubcomposeLayout, layoutDirection)
                val bottomInset = contentWindowInsets.getBottom(this@SubcomposeLayout)
                measurable.measure(
                    looseConstraints.offset(
                        -leftInset - rightInset,
                        -bottomInset
                    )
                )
                    .takeIf { it.height != 0 && it.width != 0 }
            }

        val fabPlacement = if (fabPlaceables.isNotEmpty()) {
            val fabWidth = fabPlaceables.maxByOrNull { it.width }!!.width
            val fabHeight = fabPlaceables.maxByOrNull { it.height }!!.height
            // FAB distance from the left of the layout, taking into account LTR / RTL
            val fabLeftOffset = if (fabPosition == FabPosition.End) {
                if (layoutDirection == LayoutDirection.Ltr) {
                    layoutWidth - FabSpacing.roundToPx() - fabWidth
                } else {
                    FabSpacing.roundToPx()
                }
            } else {
                (layoutWidth - fabWidth) / 2
            }

            FabPlacement(
                left = fabLeftOffset,
                width = fabWidth,
                height = fabHeight
            )
        } else {
            null
        }

        val bottomBarPlaceables = subcompose(ScaffoldLayoutContent.BottomBar) {
            CompositionLocalProvider(
                LocalFabPlacement provides fabPlacement
            ) {
                val maxBottomSheetOffset = layoutHeight - sheetPeekHeight.roundToPx() - bottomBarSize.height
                bottomBar(
                    maxBottomSheetOffset - sheetState.anchoredDraggableState.offset
                )
            }
        }.map { it.measure(looseConstraints) }

        val bottomBarHeight = bottomBarPlaceables.maxByOrNull { it.height }?.height
        val fabOffsetFromBottom = fabPlacement?.let {
            if (bottomBarHeight == null) {
                it.height + FabSpacing.roundToPx() +
                        contentWindowInsets.getBottom(this@SubcomposeLayout) + sheetPeekHeight.roundToPx()
            } else {
                // Total height is the bottom bar height + the FAB height + the padding
                // between the FAB and bottom bar
                bottomBarHeight + it.height + FabSpacing.roundToPx() + sheetPeekHeight.roundToPx()
            }
        }

        val snackbarOffsetFromBottom = if (snackbarHeight != 0) {
            snackbarHeight +
                    (fabOffsetFromBottom ?: bottomBarHeight
                    ?: contentWindowInsets.getBottom(this@SubcomposeLayout))
        } else {
            0
        }

        val bodyContentPlaceables = subcompose(ScaffoldLayoutContent.MainContent) {
            val insets = contentWindowInsets.asPaddingValues(this@SubcomposeLayout)
            val innerPadding = PaddingValues(
                top =
                if (topBarPlaceables.isEmpty()) {
                    insets.calculateTopPadding()
                } else {
                    topBarHeight.toDp()
                },
                bottom =
                if (bottomBarPlaceables.isEmpty() || bottomBarHeight == null) {
                    insets.calculateBottomPadding()
                } else {
                    bottomBarHeight.toDp()
                },
                start = insets.calculateStartPadding((this@SubcomposeLayout).layoutDirection),
                end = insets.calculateEndPadding((this@SubcomposeLayout).layoutDirection)
            )
            content(innerPadding)
        }.map { it.measure(looseConstraints) }

        layout(layoutWidth, layoutHeight) {
            // Placing to control drawing order to match default elevation of each placeable
            bodyContentPlaceables.forEach {
                it.place(0, 0)
            }
            topBarPlaceables.forEach {
                it.place(0, 0)
            }
            // Explicitly not using placeRelative here as `leftOffset` already accounts for RTL
            fabPlacement?.let { placement ->
                fabPlaceables.forEach {
                    it.place(placement.left, layoutHeight - fabOffsetFromBottom!!)
                }
            }
            sheetPlaceables.forEach {
                it.placeRelative(sheetOffsetX, sheetOffsetY)
            }
            snackbarPlaceables.forEach {
                it.place(
                    (layoutWidth - snackbarWidth) / 2 +
                            contentWindowInsets.getLeft(this@SubcomposeLayout, layoutDirection),
                    layoutHeight - snackbarOffsetFromBottom
                )
            }
            // The bottom bar is always at the bottom of the layout
            bottomBarPlaceables.forEach {
                it.place(0, layoutHeight - (bottomBarHeight ?: 0))
            }
        }
    }
}

@Suppress("Deprecation")
@ExperimentalMaterialApi
@Stable
fun BottomSheetState(
    initialValue: BottomSheetValue,
    density: Density,
    animationSpec: AnimationSpec<Float> = SpringSpec(),
    confirmValueChange: (BottomSheetValue) -> Boolean = { true }
) = BottomSheetState(initialValue, animationSpec, confirmValueChange).also {
    it.density = density
}

/**
 * State of the persistent bottom sheet in [BottomSheetScaffold].
 *
 * @param initialValue The initial value of the state.
 * @param animationSpec The default animation that will be used to animate to a new state.
 * @param confirmValueChange Optional callback invoked to confirm or veto a pending state change.
 */
@ExperimentalMaterialApi
@OptIn(ExperimentalFoundationApi::class)
@Stable
class BottomSheetState @Deprecated(
    "This constructor is deprecated. Density must be provided by the component. " +
            "Please use the constructor that provides a [Density].",
    ReplaceWith(
        """
            BottomSheetState(
                initialValue = initialValue,
                density = LocalDensity.current,
                animationSpec = animationSpec,
                confirmValueChange = confirmValueChange
            )
            """
    )
) constructor(
    initialValue: BottomSheetValue,
    animationSpec: AnimationSpec<Float> = SpringSpec(),
    confirmValueChange: (BottomSheetValue) -> Boolean = { true }
) {

    internal val anchoredDraggableState = AnchoredDraggableState(
        initialValue = initialValue,
        animationSpec = animationSpec,
        confirmValueChange = confirmValueChange,
        positionalThreshold = {
            with(requireDensity()) {
                BottomSheetScaffoldPositionalThreshold.toPx()
            }
        },
        velocityThreshold = {
            with(requireDensity()) {
                BottomSheetScaffoldVelocityThreshold.toPx()
            }
        }
    )

    /**
     * The current value of the [BottomSheetState].
     */
    val currentValue: BottomSheetValue
        get() = anchoredDraggableState.currentValue

    /**
     * The target value the state will settle at once the current interaction ends, or the
     * [currentValue] if there is no interaction in progress.
     */
    val targetValue: BottomSheetValue
        get() = anchoredDraggableState.targetValue

    /**
     * Whether the bottom sheet is expanded.
     */
    val isExpanded: Boolean
        get() = anchoredDraggableState.currentValue == BottomSheetValue.Expanded

    /**
     * Whether the bottom sheet is collapsed.
     */
    val isCollapsed: Boolean
        get() = anchoredDraggableState.currentValue == BottomSheetValue.Collapsed

    /**
     * The fraction of the progress, within [0f..1f] bounds, or 1f if the [AnchoredDraggableState]
     * is in a settled state.
     */
    @get:FloatRange(from = 0.0, to = 1.0)
    val progress: Float by derivedStateOf(structuralEqualityPolicy()) {
        val a = anchoredDraggableState.anchors.positionOf(BottomSheetValue.Collapsed)
        val b = anchoredDraggableState.anchors.positionOf(BottomSheetValue.Expanded)

        val distance = abs(b - a)
        if (!distance.isNaN() && distance > 1e-6f) {
            val progress = requireOffset() / distance

            // If we are very close to 0f or 1f, we round to the closest
            if (progress < 1e-6f) 0f else if (progress > 1 - 1e-6f) 1f else progress
        } else 1f
    }

    /**
     * Expand the bottom sheet with an animation and suspend until the animation finishes or is
     * cancelled.
     * Note: If the peek height is equal to the sheet height, this method will animate to the
     * [Collapsed] state.
     *
     * This method will throw [CancellationException] if the animation is interrupted.
     */
    suspend fun expand() {
        val target = if (anchoredDraggableState.anchors.hasAnchorFor(BottomSheetValue.Expanded)) {
            BottomSheetValue.Expanded
        } else {
            BottomSheetValue.Collapsed
        }
        anchoredDraggableState.animateTo(target)
    }

    /**
     * Collapse the bottom sheet with animation and suspend until it if fully collapsed or animation
     * has been cancelled. This method will throw [CancellationException] if the animation is
     * interrupted.
     */
    suspend fun collapse() = anchoredDraggableState.animateTo(BottomSheetValue.Collapsed)

    @Deprecated(
        message = "Use requireOffset() to access the offset.",
        replaceWith = ReplaceWith("requireOffset()")
    )
    val offset: Float get() = error("Use requireOffset() to access the offset.")

    /**
     * Require the current offset.
     *
     * @throws IllegalStateException If the offset has not been initialized yet
     */
    fun requireOffset() = anchoredDraggableState.requireOffset()

    internal suspend fun animateTo(
        target: BottomSheetValue,
        velocity: Float = anchoredDraggableState.lastVelocity
    ) = anchoredDraggableState.animateTo(target, velocity)

    internal suspend fun snapTo(target: BottomSheetValue) = anchoredDraggableState.snapTo(target)

    internal var density: Density? = null
    private fun requireDensity() = requireNotNull(density) {
        "The density on BottomSheetState ($this) was not set. Did you use BottomSheetState with " +
                "the BottomSheetScaffold composable?"
    }

    internal val lastVelocity: Float get() = anchoredDraggableState.lastVelocity

    companion object {

        /**
         * The default [Saver] implementation for [BottomSheetState].
         */
        fun Saver(
            animationSpec: AnimationSpec<Float>,
            confirmStateChange: (BottomSheetValue) -> Boolean,
            density: Density
        ): Saver<BottomSheetState, *> = Saver(
            save = { it.anchoredDraggableState.currentValue },
            restore = {
                BottomSheetState(
                    initialValue = it,
                    density = density,
                    animationSpec = animationSpec,
                    confirmValueChange = confirmStateChange
                )
            }
        )

        /**
         * The default [Saver] implementation for [BottomSheetState].
         */
        @Deprecated(
            message = "This function is deprecated. Please use the overload where Density is" +
                    " provided.",
            replaceWith = ReplaceWith(
                "Saver(animationSpec, confirmStateChange, density)"
            )
        )
        @Suppress("Deprecation")
        fun Saver(
            animationSpec: AnimationSpec<Float>,
            confirmStateChange: (BottomSheetValue) -> Boolean
        ): Saver<BottomSheetState, *> = Saver(
            save = { it.anchoredDraggableState.currentValue },
            restore = {
                BottomSheetState(
                    initialValue = it,
                    animationSpec = animationSpec,
                    confirmValueChange = confirmStateChange
                )
            }
        )
    }
}

/**
 * Create a [BottomSheetState] and [remember] it.
 *
 * @param initialValue The initial value of the state.
 * @param animationSpec The default animation that will be used to animate to a new state.
 * @param confirmStateChange Optional callback invoked to confirm or veto a pending state change.
 */
@Composable
@ExperimentalMaterialApi
fun rememberBottomSheetState(
    initialValue: BottomSheetValue,
    animationSpec: AnimationSpec<Float> = SpringSpec(),
    confirmStateChange: (BottomSheetValue) -> Boolean = { true }
): BottomSheetState {
    val density = LocalDensity.current
    return rememberSaveable(
        animationSpec,
        saver = BottomSheetState.Saver(
            animationSpec = animationSpec,
            confirmStateChange = confirmStateChange,
            density = density
        )
    ) {
        BottomSheetState(
            initialValue = initialValue,
            animationSpec = animationSpec,
            confirmValueChange = confirmStateChange,
            density = density
        )
    }
}

@ExperimentalMaterialApi
class BottomSheetScaffoldState(
    val bottomSheetState: BottomSheetState,
    val snackbarHostState: SnackbarHostState
)

@Composable
@ExperimentalMaterialApi
fun rememberBottomSheetScaffoldState(
    bottomSheetState: BottomSheetState = rememberBottomSheetState(BottomSheetValue.Collapsed),
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
): BottomSheetScaffoldState {
    return remember(bottomSheetState, snackbarHostState) {
        BottomSheetScaffoldState(
            bottomSheetState = bottomSheetState,
            snackbarHostState = snackbarHostState
        )
    }
}

val BottomSheetScaffoldDefaults.contentWindowInsets: WindowInsets
    @Composable
    get() = WindowInsets.systemBars

@Immutable
internal class FabPlacement(
    val left: Int,
    val width: Int,
    val height: Int
)

internal val LocalFabPlacement = staticCompositionLocalOf<FabPlacement?> { null }

@OptIn(ExperimentalFoundationApi::class)
private fun consumeSwipeWithinBottomSheetBoundsNestedScrollConnection(
    state: AnchoredDraggableState<*>,
    orientation: Orientation
): NestedScrollConnection = object : NestedScrollConnection {
    override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
        val delta = available.toFloat()
        return if (delta < 0 && source == NestedScrollSource.Drag) {
            state.dispatchRawDelta(delta).toOffset()
        } else {
            Offset.Zero
        }
    }

    override fun onPostScroll(
        consumed: Offset,
        available: Offset,
        source: NestedScrollSource
    ): Offset {
        return if (source == NestedScrollSource.Drag) {
            state.dispatchRawDelta(available.toFloat()).toOffset()
        } else {
            Offset.Zero
        }
    }

    override suspend fun onPreFling(available: Velocity): Velocity {
        val toFling = available.toFloat()
        val currentOffset = state.requireOffset()
        return if (toFling < 0 && currentOffset > state.anchors.minAnchor()) {
            state.settle(velocity = toFling)
            // since we go to the anchor with tween settling, consume all for the best UX
            available
        } else {
            Velocity.Zero
        }
    }

    override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
        state.settle(velocity = available.toFloat())
        return available
    }

    private fun Float.toOffset(): Offset = Offset(
        x = if (orientation == Orientation.Horizontal) this else 0f,
        y = if (orientation == Orientation.Vertical) this else 0f
    )

    @JvmName("velocityToFloat")
    private fun Velocity.toFloat() = if (orientation == Orientation.Horizontal) x else y

    @JvmName("offsetToFloat")
    private fun Offset.toFloat(): Float = if (orientation == Orientation.Horizontal) x else y
}

// FAB spacing above the bottom bar / bottom of the Scaffold
private val FabSpacing = 16.dp
private val BottomSheetScaffoldPositionalThreshold = 56.dp
private val BottomSheetScaffoldVelocityThreshold = 125.dp
private val BottomSheetMaxWidth = 640.dp

private enum class ScaffoldLayoutContent { TopBar, MainContent, Sheet, Snackbar, Fab, BottomBar }

