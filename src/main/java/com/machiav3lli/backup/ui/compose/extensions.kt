package com.machiav3lli.backup.ui.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.layout
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.unit.dp
import androidx.core.graphics.ColorUtils
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import com.machiav3lli.backup.preferences.pref_altBlockLayout
import com.machiav3lli.backup.preferences.traceFlows
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
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

fun Modifier.blockBorderBottom(altStyle: Boolean = pref_altBlockLayout.value) =
    composed {
        this
            .padding(2.dp)
            .clip(BlockBottomShape)
            .ifThenElse(altStyle,
                modifier = {
                    border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outline,
                        shape = BlockBottomShape,
                    )
                },
                elseModifier = {
                    background(color = MaterialTheme.colorScheme.surfaceContainerLow)
                }
            )
    }

fun Modifier.blockBorderTop(altStyle: Boolean = pref_altBlockLayout.value) =
    composed {
        this
            .padding(2.dp)
            .clip(BlockTopShape)
            .ifThenElse(altStyle,
                modifier = {
                    border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outline,
                        shape = BlockTopShape,
                    )
                },
                elseModifier = {
                    background(color = MaterialTheme.colorScheme.surfaceContainerLow)
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

val BlockTopShape
    @Composable @ReadOnlyComposable get() = RoundedCornerShape(
        topStart = MaterialTheme.shapes.extraLarge.topStart,
        topEnd = MaterialTheme.shapes.extraLarge.topEnd,
        bottomEnd = MaterialTheme.shapes.extraSmall.bottomEnd,
        bottomStart = MaterialTheme.shapes.extraSmall.bottomStart,
    )

val BlockBottomShape
    @Composable @ReadOnlyComposable get() = RoundedCornerShape(
        topStart = MaterialTheme.shapes.extraSmall.topStart,
        topEnd = MaterialTheme.shapes.extraSmall.topEnd,
        bottomEnd = MaterialTheme.shapes.extraLarge.bottomEnd,
        bottomStart = MaterialTheme.shapes.extraLarge.bottomStart,
    )

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
                val addSpace =
                    if (nBWraps > 0) (maxBWrapWidth - finalTotalBWrapWidth) / nBWraps else 0
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

fun Color.mix(with: Color, factor: Float = 0.5f) = Color(
    red = (red * (1f - factor) + with.red * factor).coerceIn(0f, 1f),
    green = (green * (1f - factor) + with.green * factor).coerceIn(0f, 1f),
    blue = (blue * (1f - factor) + with.blue * factor).coerceIn(0f, 1f),
    alpha = alpha
)

@Composable
fun Color.flatten(factor: Float = 0.5f, surface: Color = MaterialTheme.colorScheme.surface) =
    mix(surface, factor)

fun Color.brighter(rate: Float): Color {
    val hslVal = FloatArray(3)
    ColorUtils.colorToHSL(this.toArgb(), hslVal)
    hslVal[2] += rate * (1 - hslVal[2])
    hslVal[2] = hslVal[2].coerceIn(0f..1f)
    return Color(ColorUtils.HSLToColor(hslVal))
}

fun Color.darker(rate: Float): Color {
    val hslVal = FloatArray(3)
    ColorUtils.colorToHSL(this.toArgb(), hslVal)
    hslVal[2] -= rate * hslVal[2]
    hslVal[2] = hslVal[2].coerceIn(0f..1f)
    return Color(ColorUtils.HSLToColor(hslVal))
}

fun <T> LazyListScope.gridItems(
    items: List<T>,
    columns: Int,
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    itemContent: @Composable BoxScope.(T) -> Unit,
) {
    val itemsCount = items.count()
    val rows = when {
        itemsCount >= 1 -> 1 + (itemsCount - 1) / columns
        else            -> 0
    }
    items(rows, key = { it.hashCode() }) { rowIndex ->
        Row(
            horizontalArrangement = horizontalArrangement,
            modifier = modifier
        ) {
            (0 until columns).forEach { columnIndex ->
                val itemIndex = columns * rowIndex + columnIndex
                if (itemIndex < itemsCount) {
                    Box(
                        modifier = Modifier.weight(1f),
                        propagateMinConstraints = true
                    ) {
                        itemContent(items[itemIndex])
                    }
                } else {
                    Spacer(Modifier.weight(1f))
                }
            }
        }
    }
}

// TODO make easy callable from different contexts
fun SnackbarHostState.show(
    coroutineScope: CoroutineScope,
    message: String,
    actionText: String? = null,
    onAction: () -> Unit = {},
) {
    coroutineScope.launch {
        showSnackbar(message = message, actionLabel = actionText, withDismissAction = true).apply {
            if (this == SnackbarResult.ActionPerformed) onAction()
        }
    }
}