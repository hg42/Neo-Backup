package com.machiav3lli.backup.ui.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.layout
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import com.machiav3lli.backup.preferences.pref_altBlockLayout
import com.machiav3lli.backup.traceFlows
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.max

fun Modifier.vertical() = layout { measurable, constraints ->
    val placeable = measurable.measure(constraints)
    layout(placeable.height, placeable.width) {
        placeable.place(
            x = -(placeable.width / 2 - placeable.height / 2),
            y = -(placeable.height / 2 - placeable.width / 2)
        )
    }
}

@Composable
inline fun Modifier.ifThen(
    boolean: Boolean,
    crossinline modifier: @Composable Modifier.() -> Modifier,
): Modifier {
    return if (boolean) {
        modifier.invoke(this)
    } else {
        this
    }
}

@Composable
inline fun Modifier.ifThenElse(
    boolean: Boolean,
    crossinline modifier: @Composable Modifier.() -> Modifier,
    crossinline elseModifier: @Composable Modifier.() -> Modifier,
): Modifier {
    return if (boolean) {
        modifier.invoke(this)
    } else {
        elseModifier.invoke(this)
    }
}

fun Modifier.blockBorder(style: Boolean? = null) = composed {
    val altBlockStyle = style ?: (if(LocalInspectionMode.current) false else pref_altBlockLayout.value)
    this
        .clip(MaterialTheme.shapes.extraLarge)
        .ifThenElse(altBlockStyle,
            modifier = {
                border(
                    1.dp,
                    MaterialTheme.colorScheme.outline,
                    MaterialTheme.shapes.extraLarge,
                )
            },
            elseModifier = {
                background(MaterialTheme.colorScheme.surfaceContainer)
            }
        )
}

fun Modifier.blockShadow(altStyle: Boolean = pref_altBlockLayout.value) =
    composed {
        this
            .ifThenElse(altStyle,
                modifier = {
                    border(
                        1.dp,
                        MaterialTheme.colorScheme.outline,
                        MaterialTheme.shapes.extraLarge,
                    )
                },
                elseModifier = {
                    shadow(elevation = 1.dp, shape = MaterialTheme.shapes.extraLarge)
                        .background(MaterialTheme.colorScheme.surfaceContainer)
                }
            )
    }

@Composable
fun <T> ObservedEffect(flow: Flow<T?>, onChange: (T?) -> Unit) {
    val lcOwner = LocalLifecycleOwner.current
    LaunchedEffect(flow, lcOwner.lifecycle) {
        lcOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            flow.collect(onChange)
        }
    }
}

@Composable
fun ObservedEffect(onChange: () -> Unit) {
    val lcOwner = LocalLifecycleOwner.current
    LaunchedEffect(lcOwner.lifecycle) {
        lcOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            onChange()
        }
    }
}


class MutableComposableSharedFlow<T>(
    var initial: T,
    val scope: CoroutineScope,
    val label: String = "ComposableSharedFlow",
) {
    var flow = MutableSharedFlow<T>()

    var state = flow
        .stateIn(
            scope,
            SharingStarted.Eagerly,
            initial
        )

    var value: T
        get() {
            val value = state.value
            if (value is String)
                traceFlows { "*** $label => '$value'" }
            else
                traceFlows { "*** $label => $value" }
            return value
        }
        set(value: T) {
            if (value is String)
                traceFlows { "*** $label <= '$value'" }
            else
                traceFlows { "*** $label <= $value" }
            initial = value
            scope.launch { flow.emit(value) }
        }

    init {
        value = initial
    }
}

class MutableComposableStateFlow<T>(
    var initial: T,
    val scope: CoroutineScope,
    val label: String = "ComposableStateFlow",
) {
    var flow = MutableStateFlow<T>(initial)

    val state = flow.asStateFlow()

    var value: T
        get() {
            val value = state.value
            if (value is String)
                traceFlows { "*** $label => '$value'" }
            else
                traceFlows { "*** $label => $value" }
            return value
        }
        set(value: T) {
            if (value is String)
                traceFlows { "*** $label <= '$value'" }
            else
                traceFlows { "*** $label <= $value" }
            //initial = value
            scope.launch { flow.update { value } }
        }

    init {
        value = initial
    }
}

//typealias MutableComposableFlow<T> = MutableComposableSharedFlow<T>
typealias MutableComposableFlow<T> = MutableComposableStateFlow<T>


@Composable
fun LazyListState.isAtTop() = remember {
    derivedStateOf {
        firstVisibleItemIndex == 0 && firstVisibleItemScrollOffset == 0
    }
}.value

@Composable
fun LazyListState.isAtBottom() = remember {
    derivedStateOf {
        try {
            layoutInfo.visibleItemsInfo.last().index >= layoutInfo.totalItemsCount - 1
        } catch (_: Throwable) {
            true
        }
    }
}.value


// hg42: this implements a row layout that balances height (and width)
// of the wrapping elements marked by balancedWrap()
// * marked elements are usually Text
// * remove other modifiers that disturb the layout like weight and width
// * the width of the marked elements is adjusted so that they get a similar height
// * spare space is distributed evenly between marked elements
// * for balancing the height
//   [A] the area of all marked elements is determined (intrinsic should usually be in unwrapped state)
//   [W] the individual and total width of the marked elements is determined
//   [H] target height is total area / total width of the marked elements
//   [w] each marked element is set to width[i] = area[i]/height
//   [F] each element is fianally measured with the resulting width as constraint
// problem:
//   I don't see a way how to find marked sub-elements in the tree
//   (to say soemthing like: "this element *contains* a marked element")


const val BALANCED_WRAP = "BalancedWrap"

@Composable
fun Modifier.balancedWrap(): Modifier = this then Modifier.layoutId(BALANCED_WRAP)

@Composable
fun BalancedWrapRow(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    //val density = LocalDensity.current

    Layout(
        content = content,
        modifier = modifier,
        measurePolicy = { measurables, constraints ->

            val minBWrapWidth = 10.dp.toPx().toInt()

            val isBWrap = measurables.map {
                it to (it.layoutId.toString() == BALANCED_WRAP)
            }.toMap()
            val nBWraps = isBWrap.count { it.value }

            // measure with initial constraints
            val maxWidths =
                measurables.map { it to it.maxIntrinsicWidth(0) }
            val widths = maxWidths.toMap().toMutableMap()

            // [A] calculate area of elements to be balanced in height
            val areas = measurables.map {
                val width = max(minBWrapWidth, it.maxIntrinsicWidth(0))
                val height = it.maxIntrinsicHeight(width)
                it to (width * height)
            }.toMap()
            val totalBWrapArea = areas.map { (measurable, area) ->
                if (isBWrap[measurable] == true)
                    area
                else
                    0
            }.sum()

            // [W] get total width of fixed and balanced elements
            val maxWidth = constraints.maxWidth
            val totalFixedWidth = maxWidths.map { (measurable, minWidth) ->
                if (isBWrap[measurable] == true) 0 else minWidth
            }.sum()
            val totalBWrapWidth = maxWidths.map { (measurable, minWidth) ->
                if (isBWrap[measurable] == true)
                    max(minBWrapWidth, minWidth)
                else
                    0
            }.sum()

            // maximum width of balanced elements within constraints
            val maxBWrapWidth = maxWidth - totalFixedWidth
            //val totalWidth = totalFixedWidth + totalBalancedWidth

            // [H] estimate target height
            // the factor puts the the estimation on the better looking side
            val balancedHeight = (totalBWrapArea / maxBWrapWidth) * 1.25f

            // [w] determine new widths
            if (totalBWrapWidth > maxBWrapWidth) {
                // not enough space, so we need to wrap the balanced elements
                measurables.map { measurable ->
                    val width = (widths[measurable] ?: 0)
                    val adjustedWidth = if (isBWrap[measurable] == true)
                        (areas[measurable]!! / balancedHeight).toInt()   // <--------
                    else
                        width
                    widths[measurable] = adjustedWidth
                }
                val finalTotalBWrapWidth = widths.map { (measurable, width) ->
                    if (isBWrap[measurable] == true)
                        max(minBWrapWidth, width)
                    else
                        0
                }.sum()
                val addSpace = if (nBWraps > 0) (maxBWrapWidth - finalTotalBWrapWidth) / nBWraps else 0
                measurables.map { measurable ->
                    val width = (widths[measurable] ?: 0)
                    val adjustedWidth = if (isBWrap[measurable] == true)
                        max(0, width + addSpace)
                    else
                        width
                    widths[measurable] = adjustedWidth
                }
            } else {
                // spare space is distributed evenly
                val addSpace = if (nBWraps > 0) (maxBWrapWidth - totalBWrapWidth) / nBWraps else 0
                measurables.map { measurable ->
                    val width = (widths[measurable] ?: 0)
                    val adjustedWidth = if (isBWrap[measurable] == true)
                        width + addSpace
                    else
                        width
                    widths[measurable] = adjustedWidth
                }
            }

            // [F] final measure with the new widths as constraints
            val placeables =
                measurables.map { measurable ->
                    val width = (widths[measurable] ?: 0)
                    measurable.measure(
                        constraints.copy(
                            minWidth = width,
                            maxWidth = width
                        )
                    )
                }

            // get final height
            val maxHeight = placeables.map { it.height }.maxOrNull() ?: 0

            // place all elements
            layout(maxWidth, maxHeight) {
                var xPosition = 0
                placeables.forEach {
                    it.placeRelative(x = xPosition, y = 0)
                    xPosition += it.width
                }
            }
        }
    )
}

@Composable
fun Color.flatten(factor: Float = 0.5f, surface: Color = MaterialTheme.colorScheme.surface) = Color(
    red = (red * (1f-factor) + surface.red*factor).coerceIn(0f, 1f),
    green = (green * (1f-factor) + surface.green*factor).coerceIn(0f, 1f),
    blue = (blue * (1f-factor) + surface.blue*factor).coerceIn(0f, 1f),
    alpha = alpha
)
