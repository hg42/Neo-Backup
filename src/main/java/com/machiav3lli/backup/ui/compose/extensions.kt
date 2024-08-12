package com.machiav3lli.backup.ui.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
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
fun SelectionContainerX(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    SelectionContainer(modifier = modifier.fillMaxWidth(), content = content)
    //content()
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


// implement a row layout that balances height (and width)
// of wrapping elements that are marked with balancedWrap()
// * marked elements are usually Text (remove other modifiers like weight and width)
// * spare space is distributed evenly between marked elements
// * the area of marked elements is determined
// * the width of the marked elements is adjusted so that they get a similar height as far as this is possible in one

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

            val minBalancedWidth = 10.dp.toPx().toInt()

            val isText = measurables.map {
                it to (it.layoutId.toString() == BALANCED_WRAP)
            }.toMap()
            val nTexts = max(1,  isText.count { it.value })

            // measure with initial constraints
            val maxWidths =
                measurables.map { it to it.maxIntrinsicWidth(0) }
            val widths = maxWidths.toMap().toMutableMap()
            // calculate area of elements to be balanced in height
            val areas = measurables.map {
                val width = max(minBalancedWidth, it.maxIntrinsicWidth(0))
                val height = it.maxIntrinsicHeight(width)
                it to (width * height)
            }.toMap()
            val totalBalancedArea = areas.map { (measurable, area) ->
                if (isText[measurable] == true)
                    area
                else
                    0
            }.sum()

            // get width of fixed and balanced elements
            val maxWidth = constraints.maxWidth
            val totalFixedWidth = maxWidths.map { (measurable, minWidth) ->
                if (isText[measurable] == true) 0 else minWidth
            }.sum()
            val totalBalancedWidth = maxWidths.map { (measurable, minWidth) ->
                if (isText[measurable] == true)
                    max(minBalancedWidth, minWidth)
                else
                    0
            }.sum()
            val maxBalancedWidth = maxWidth - totalFixedWidth
            //val totalWidth = totalFixedWidth + totalTextWidth
            val balancedHeight = (totalBalancedArea / maxBalancedWidth)

            // determine new widths
            if (totalBalancedWidth > maxBalancedWidth) {
                measurables.map { measurable ->
                    val width = (widths[measurable] ?: 0)
                    // use a factor because it's estimated
                    val factor = 1.25f
                    val adjustedWidth = if (isText[measurable] == true)
                        (areas[measurable]!! / (balancedHeight * factor)).toInt()
                    else
                        width
                    widths[measurable] = adjustedWidth
                }
                val finalTotalBalancedWidth = widths.map { (measurable, width) ->
                    if (isText[measurable] == true)
                        max(minBalancedWidth, width)
                    else
                        0
                }.sum()
                val addSpace = (maxBalancedWidth - finalTotalBalancedWidth) / nTexts
                measurables.map { measurable ->
                    val width = (widths[measurable] ?: 0)
                    val adjustedWidth = if (isText[measurable] == true)
                        max(0, width + addSpace)
                    else
                        width
                    widths[measurable] = adjustedWidth
                }
            } else {
                val addSpace = (maxBalancedWidth - totalBalancedWidth) / nTexts
                measurables.map { measurable ->
                    val width = (widths[measurable] ?: 0)
                    val adjustedWidth = if (isText[measurable] == true)
                        width + addSpace
                    else
                        width
                    widths[measurable] = adjustedWidth
                }
            }
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
