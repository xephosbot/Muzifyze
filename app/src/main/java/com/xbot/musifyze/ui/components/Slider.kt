package com.xbot.musifyze.ui.components

import androidx.annotation.IntRange
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.MutatePriority
import androidx.compose.foundation.MutatorMutex
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.DragScope
import androidx.compose.foundation.gestures.DraggableState
import androidx.compose.foundation.gestures.GestureCancellationException
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.PressGestureScope
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.DragInteraction
import androidx.compose.foundation.interaction.Interaction
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.requiredSizeIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.progressSemantics
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.SliderColors
import androidx.compose.material.SliderDefaults
import androidx.compose.material.minimumInteractiveComponentSize
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PointMode
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.debugInspectorInfo
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.setProgress
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.offset
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

@Composable
fun Slider(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onValueChangeFinished: (() -> Unit)? = null,
    colors: SliderColors = SliderDefaults.colors(),
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    @IntRange(from = 0)
    steps: Int = 0,
    thumb: @Composable (SliderState) -> Unit = {
        SliderDefaults.Thumb(
            interactionSource = interactionSource,
            colors = colors,
            enabled = enabled
        )
    },
    track: @Composable (SliderState) -> Unit = { sliderState ->
        SliderDefaults.Track(
            colors = colors,
            enabled = enabled,
            sliderState = sliderState
        )
    },
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f
) {
    val state = remember(
        steps,
        valueRange
    ) {
        SliderState(
            value,
            onValueChange,
            steps,
            valueRange,
            onValueChangeFinished
        )
    }
    state.value = value
    state.onValueChange = onValueChange
    state.onValueChangeFinished = onValueChangeFinished

    Slider(
        state = state,
        modifier = modifier,
        enabled = enabled,
        interactionSource = interactionSource,
        thumb = thumb,
        track = track
    )
}

@Composable
fun Slider(
    state: SliderState,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: SliderColors = SliderDefaults.colors(),
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    thumb: @Composable (SliderState) -> Unit = {
        SliderDefaults.Thumb(
            interactionSource = interactionSource,
            colors = colors,
            enabled = enabled
        )
    },
    track: @Composable (SliderState) -> Unit = { sliderState ->
        SliderDefaults.Track(
            colors = colors,
            enabled = enabled,
            sliderState = sliderState
        )
    }
) {
    require(state.steps >= 0) { "steps should be >= 0" }

    SliderImpl(
        state = state,
        modifier = modifier,
        enabled = enabled,
        interactionSource = interactionSource,
        thumb = thumb,
        track = track
    )
}

@Composable
private fun SliderImpl(
    modifier: Modifier,
    state: SliderState,
    enabled: Boolean,
    interactionSource: MutableInteractionSource,
    thumb: @Composable (SliderState) -> Unit,
    track: @Composable (SliderState) -> Unit
) {
    state.isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl
    val press = Modifier.sliderTapModifier(
        state,
        interactionSource,
        enabled
    )
    val drag = Modifier.draggable(
        orientation = Orientation.Horizontal,
        reverseDirection = state.isRtl,
        enabled = enabled,
        interactionSource = interactionSource,
        onDragStopped = { state.gestureEndAction() },
        startDragImmediately = state.draggableState.isDragging,
        state = state.draggableState
    )

    Layout(
        {
            Box(modifier = Modifier.layoutId(SliderComponents.THUMB)) {
                thumb(state)
            }
            Box(modifier = Modifier.layoutId(SliderComponents.TRACK)) {
                track(state)
            }
        },
        modifier = modifier
            //.minimumInteractiveComponentSize()
            .requiredSizeIn(
                minWidth = ThumbWidth,
                minHeight = ThumbHeight
            )
            .sliderSemantics(
                state,
                enabled
            )
            .focusable(enabled, interactionSource)
            .then(press)
            .then(drag)
    ) { measurables, constraints ->

        val thumbPlaceable = measurables.first {
            it.layoutId == SliderComponents.THUMB
        }.measure(constraints)

        val trackPlaceable = measurables.first {
            it.layoutId == SliderComponents.TRACK
        }.measure(
            constraints.offset(
                horizontal = -thumbPlaceable.width
            ).copy(minHeight = 0)
        )

        val sliderWidth = thumbPlaceable.width + trackPlaceable.width
        val sliderHeight = max(trackPlaceable.height, thumbPlaceable.height)

        state.updateDimensions(
            thumbPlaceable.width.toFloat(),
            sliderWidth
        )

        val trackOffsetX = thumbPlaceable.width / 2
        val thumbOffsetX = ((trackPlaceable.width) * state.coercedValueAsFraction).roundToInt()
        val trackOffsetY = (sliderHeight - trackPlaceable.height) / 2
        val thumbOffsetY = (sliderHeight - thumbPlaceable.height) / 2

        layout(
            sliderWidth,
            sliderHeight
        ) {
            trackPlaceable.placeRelative(
                trackOffsetX,
                trackOffsetY
            )
            thumbPlaceable.placeRelative(
                thumbOffsetX,
                thumbOffsetY
            )
        }
    }
}

@Composable
fun SliderDefaults.Thumb(
    interactionSource: MutableInteractionSource,
    modifier: Modifier = Modifier,
    colors: SliderColors = colors(),
    enabled: Boolean = true,
    thumbSize: DpSize = ThumbSize
) {
    val interactions = remember { mutableStateListOf<Interaction>() }
    LaunchedEffect(interactionSource) {
        interactionSource.interactions.collect { interaction ->
            when (interaction) {
                is PressInteraction.Press -> interactions.add(interaction)
                is PressInteraction.Release -> interactions.remove(interaction.press)
                is PressInteraction.Cancel -> interactions.remove(interaction.press)
                is DragInteraction.Start -> interactions.add(interaction)
                is DragInteraction.Stop -> interactions.remove(interaction.start)
                is DragInteraction.Cancel -> interactions.remove(interaction.start)
            }
        }
    }

    val elevation = if (interactions.isNotEmpty()) {
        ThumbPressedElevation
    } else {
        ThumbDefaultElevation
    }
    val shape = CircleShape

    Spacer(
        modifier
            .size(thumbSize)
            .indication(
                interactionSource = interactionSource,
                indication = rememberRipple(
                    bounded = false,
                    radius = StateLayerSize / 2
                )
            )
            .hoverable(interactionSource = interactionSource)
            .shadow(if (enabled) elevation else 0.dp, shape, clip = false)
            .background(colors.thumbColor(enabled).value, shape)
    )
}

private fun Modifier.sliderSemantics(
    state: SliderState,
    enabled: Boolean
): Modifier {
    val coerced = state.value.coerceIn(state.valueRange.start, state.valueRange.endInclusive)
    return semantics {
        if (!enabled) disabled()
        setProgress(
            action = { targetValue ->
                var newValue = targetValue.coerceIn(
                    state.valueRange.start,
                    state.valueRange.endInclusive
                )
                val originalVal = newValue
                val resolvedValue = if (state.steps > 0) {
                    var distance: Float = newValue
                    for (i in 0..state.steps + 1) {
                        val stepValue = lerp(
                            state.valueRange.start,
                            state.valueRange.endInclusive,
                            i.toFloat() / (state.steps + 1)
                        )
                        if (abs(stepValue - originalVal) <= distance) {
                            distance = abs(stepValue - originalVal)
                            newValue = stepValue
                        }
                    }
                    newValue
                } else {
                    newValue
                }

                // This is to keep it consistent with AbsSeekbar.java: return false if no
                // change from current.
                if (resolvedValue == coerced) {
                    false
                } else {
                    state.onValueChange(resolvedValue)
                    state.onValueChangeFinished?.invoke()
                    true
                }
            }
        )
    }.progressSemantics(
        state.value,
        state.valueRange.start..state.valueRange.endInclusive,
        state.steps
    )
}

@Composable
fun SliderDefaults.Track(
    sliderState: SliderState,
    modifier: Modifier = Modifier,
    colors: SliderColors = colors(),
    enabled: Boolean = true
) {
    val inactiveTrackColor by colors.trackColor(enabled, active = false)
    val activeTrackColor by colors.trackColor(enabled, active = true)
    val inactiveTickColor by colors.tickColor(enabled, active = false)
    val activeTickColor by colors.tickColor(enabled, active = true)
    Canvas(
        modifier
            .fillMaxWidth()
            .height(TrackHeight)
    ) {
        drawTrack(
            sliderState.tickFractions,
            0f,
            sliderState.coercedValueAsFraction,
            inactiveTrackColor,
            activeTrackColor,
            inactiveTickColor,
            activeTickColor
        )
    }
}

private fun DrawScope.drawTrack(
    tickFractions: FloatArray,
    activeRangeStart: Float,
    activeRangeEnd: Float,
    inactiveTrackColor: Color,
    activeTrackColor: Color,
    inactiveTickColor: Color,
    activeTickColor: Color
) {
    val isRtl = layoutDirection == LayoutDirection.Rtl
    val sliderLeft = Offset(0f, center.y)
    val sliderRight = Offset(size.width, center.y)
    val sliderStart = if (isRtl) sliderRight else sliderLeft
    val sliderEnd = if (isRtl) sliderLeft else sliderRight
    val tickSize = TickSize.toPx()
    val trackStrokeWidth = TrackHeight.toPx()
    drawLine(
        inactiveTrackColor,
        sliderStart,
        sliderEnd,
        trackStrokeWidth,
        StrokeCap.Round
    )
    val sliderValueEnd = Offset(
        sliderStart.x +
                (sliderEnd.x - sliderStart.x) * activeRangeEnd,
        center.y
    )

    val sliderValueStart = Offset(
        sliderStart.x +
                (sliderEnd.x - sliderStart.x) * activeRangeStart,
        center.y
    )

    drawLine(
        activeTrackColor,
        sliderValueStart,
        sliderValueEnd,
        trackStrokeWidth,
        StrokeCap.Round
    )
    tickFractions.groupBy {
        it > activeRangeEnd ||
                it < activeRangeStart
    }.forEach { (outsideFraction, list) ->
        drawPoints(
            list.map {
                Offset(androidx.compose.ui.geometry.lerp(sliderStart, sliderEnd, it).x, center.y)
            },
            PointMode.Points,
            (if (outsideFraction) inactiveTickColor else activeTickColor),
            tickSize,
            StrokeCap.Round
        )
    }
}

private fun Modifier.sliderTapModifier(
    state: SliderState,
    interactionSource: MutableInteractionSource,
    enabled: Boolean
) = composed(
    factory = {
        if (enabled) {
            val scope = rememberCoroutineScope()
            pointerInput(state.draggableState, interactionSource, state.totalWidth, state.isRtl) {
                detectTapGestures(
                    onPress = state.press,
                    onTap = {
                        scope.launch {
                            state.draggableState.drag(MutatePriority.UserInput) {
                                // just trigger animation, press offset will be applied
                                dragBy(0f)
                            }
                            state.gestureEndAction()
                        }
                    }
                )
            }
        } else {
            this
        }
    },
    inspectorInfo = debugInspectorInfo {
        name = "sliderTapModifier"
        properties["state"] = state
        properties["interactionSource"] = interactionSource
        properties["enabled"] = enabled
    })

@Stable
class SliderState(
    initialValue: Float = 0f,
    initialOnValueChange: ((Float) -> Unit)? = null,
    @IntRange(from = 0)
    val steps: Int = 0,
    val valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    var onValueChangeFinished: (() -> Unit)? = null
) {
    private var valueState by mutableFloatStateOf(initialValue)

    /**
     * [Float] that indicates the current value that the thumb
     * currently is in respect to the track.
     */
    var value: Float
        set(newVal) {
            val coercedValue = newVal.coerceIn(valueRange.start, valueRange.endInclusive)
            val snappedValue = snapValueToTick(
                coercedValue,
                tickFractions,
                valueRange.start,
                valueRange.endInclusive
            )
            valueState = snappedValue
        }
        get() = valueState

    /**
     * callback in which value should be updated
     */
    internal var onValueChange: (Float) -> Unit = {
        if (it != value) {
            initialOnValueChange?.invoke(it) ?: defaultOnValueChange(it)
        }
    }

    internal val tickFractions = stepsToTickFractions(steps)

    private var thumbWidth by mutableFloatStateOf(ThumbWidth.value)
    internal var totalWidth by mutableIntStateOf(0)

    private var rawOffset by mutableFloatStateOf(scaleToOffset(0f, 0f, value))
    private var pressOffset by mutableFloatStateOf(0f)

    internal var isRtl = false

    internal val coercedValueAsFraction
        get() = calcFraction(
            valueRange.start,
            valueRange.endInclusive,
            value.coerceIn(valueRange.start, valueRange.endInclusive)
        )

    internal val draggableState =
        SliderDraggableState {
            val maxPx = max(totalWidth - thumbWidth / 2, 0f)
            val minPx = min(thumbWidth / 2, maxPx)
            rawOffset = (rawOffset + it + pressOffset)
            pressOffset = 0f
            val offsetInTrack = snapValueToTick(rawOffset, tickFractions, minPx, maxPx)
            onValueChange(scaleToUserValue(minPx, maxPx, offsetInTrack))
        }

    internal val gestureEndAction = {
        if (!draggableState.isDragging) {
            // check isDragging in case the change is still in progress (touch -> drag case)
            onValueChangeFinished?.invoke()
        }
    }

    internal val press: suspend PressGestureScope.(Offset) -> Unit = { pos ->
        val to = if (isRtl) totalWidth - pos.x else pos.x
        pressOffset = to - rawOffset
        try {
            awaitRelease()
        } catch (_: GestureCancellationException) {
            pressOffset = 0f
        }
    }

    internal fun updateDimensions(
        newThumbWidth: Float,
        newTotalWidth: Int
    ) {
        thumbWidth = newThumbWidth
        totalWidth = newTotalWidth
    }

    private fun defaultOnValueChange(newVal: Float) {
        value = newVal
    }

    private fun scaleToUserValue(minPx: Float, maxPx: Float, offset: Float) =
        scale(minPx, maxPx, offset, valueRange.start, valueRange.endInclusive)

    private fun scaleToOffset(minPx: Float, maxPx: Float, userValue: Float) =
        scale(valueRange.start, valueRange.endInclusive, userValue, minPx, maxPx)
}

internal class SliderDraggableState(
    val onDelta: (Float) -> Unit
) : DraggableState {

    var isDragging by mutableStateOf(false)
        private set

    private val dragScope: DragScope = object : DragScope {
        override fun dragBy(pixels: Float): Unit = onDelta(pixels)
    }

    private val scrollMutex = MutatorMutex()

    override suspend fun drag(
        dragPriority: MutatePriority,
        block: suspend DragScope.() -> Unit
    ): Unit = coroutineScope {
        isDragging = true
        scrollMutex.mutateWith(dragScope, dragPriority, block)
        isDragging = false
    }

    override fun dispatchRawDelta(delta: Float) {
        return onDelta(delta)
    }
}

private enum class SliderComponents {
    THUMB,
    TRACK
}

private fun snapValueToTick(
    current: Float,
    tickFractions: FloatArray,
    minPx: Float,
    maxPx: Float
): Float {
    // target is a closest anchor to the `current`, if exists
    return tickFractions
        .minByOrNull { abs(lerp(minPx, maxPx, it) - current) }
        ?.run { lerp(minPx, maxPx, this) }
        ?: current
}

private fun stepsToTickFractions(steps: Int): FloatArray {
    return if (steps == 0) floatArrayOf() else FloatArray(steps + 2) { it.toFloat() / (steps + 1) }
}

private fun scale(a1: Float, b1: Float, x1: Float, a2: Float, b2: Float) =
    lerp(a2, b2, calcFraction(a1, b1, x1))

private fun calcFraction(a: Float, b: Float, pos: Float) =
    (if (b - a == 0f) 0f else (pos - a) / (b - a)).coerceIn(0f, 1f)

private fun lerp(a: Float, b: Float, f: Float): Float {
    return (a * (1.0 - f) + b * f).toFloat()
}

private val ThumbWidth = 10.0.dp
private val ThumbHeight = 10.0.dp
private val ThumbSize = DpSize(ThumbWidth, ThumbHeight)
private val ThumbDefaultElevation = 1.dp
private val ThumbPressedElevation = 6.dp
private val StateLayerSize = 30.0.dp
private val TickSize = 2.0.dp
private val TrackHeight = 2.0.dp
