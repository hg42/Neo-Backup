package com.machiav3lli.backup.ui.compose.item

import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SelectableChipColors
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.machiav3lli.backup.R
import com.machiav3lli.backup.entity.ChipItem
import com.machiav3lli.backup.entity.InfoChipItem
import com.machiav3lli.backup.ui.compose.icons.Phosphor
import com.machiav3lli.backup.ui.compose.icons.phosphor.Checks
import com.machiav3lli.backup.ui.compose.ifThen

private enum class SelectionState { Unselected, Selected }

class SelectableChipTransition constructor(
    cornerRadius: State<Dp>,
) {
    val cornerRadius by cornerRadius
}

@Composable
fun selectableChipTransition(selected: Boolean): SelectableChipTransition {
    val transition = updateTransition(
        targetState = if (selected) SelectionState.Selected else SelectionState.Unselected,
        label = "chip_transition"
    )
    val corerRadius = transition.animateDp(label = "chip_corner") { state ->
        when (state) {
            SelectionState.Unselected -> 8.dp
            SelectionState.Selected   -> 16.dp
        }
    }
    return remember(transition) {
        SelectableChipTransition(corerRadius)
    }
}

@Composable
fun SelectionChip(
    item: ChipItem,
    isSelected: Boolean,
    colors: SelectableChipColors = FilterChipDefaults.filterChipColors(
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
        labelColor = MaterialTheme.colorScheme.onSurface,
        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
        iconColor = MaterialTheme.colorScheme.onSurface,
        selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
    ),
    onClick: () -> Unit,
) {
    val selectableChipTransitionState = selectableChipTransition(selected = isSelected)

    FilterChip(
        colors = colors,
        shape = RoundedCornerShape(selectableChipTransitionState.cornerRadius),
        border = null,
        selected = isSelected,
        leadingIcon = {
            ButtonIcon(item.icon, item.textId)
        },
        onClick = onClick,
        label = {
            Text(text = stringResource(id = item.textId))
        }
    )
}

@Composable
fun SelectionChip(
    label: String,
    isSelected: Boolean,
    colors: SelectableChipColors = FilterChipDefaults.filterChipColors(
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
        labelColor = MaterialTheme.colorScheme.onSurface,
        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
    ),
    onClick: () -> Unit,
) {
    val selectableChipTransitionState = selectableChipTransition(selected = isSelected)

    FilterChip(
        colors = colors,
        shape = RoundedCornerShape(selectableChipTransitionState.cornerRadius),
        border = null,
        selected = isSelected,
        onClick = onClick,
        label = {
            Text(text = label)
        }
    )
}

@Composable
fun InfoChip(
    item: InfoChipItem,
) {
    SuggestionChip(
        icon = {
            if (item.icon != null) Icon(
                imageVector = item.icon,
                contentDescription = item.text,
            )
        },
        border = null,
        label = {
            Text(text = item.text)
        },
        colors = SuggestionChipDefaults.suggestionChipColors(
            containerColor = item.color ?: MaterialTheme.colorScheme.surfaceContainer,
            labelColor = if (item.color != null) MaterialTheme.colorScheme.surfaceContainerLowest
            else MaterialTheme.colorScheme.onSurface,
            iconContentColor = if (item.color != null) MaterialTheme.colorScheme.surfaceContainerLowest
            else MaterialTheme.colorScheme.onSurface,
        ),
        onClick = {}
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun StateChip(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    text: String,
    color: Color,
    index: Int,
    count: Int,
    checked: Boolean,
    onClick: () -> Unit,
) {
    val openPopup = remember { mutableStateOf(false) }
    val shape = RoundedCornerShape(
        topStart = if (index == 0) MaterialTheme.shapes.extraLarge.topStart
        else MaterialTheme.shapes.extraSmall.topStart,
        bottomStart = if (index == 0) MaterialTheme.shapes.extraLarge.topStart
        else MaterialTheme.shapes.extraSmall.topStart,
        topEnd = if (index == count - 1) MaterialTheme.shapes.extraLarge.topStart
        else MaterialTheme.shapes.extraSmall.topStart,
        bottomEnd = if (index == count - 1) MaterialTheme.shapes.extraLarge.topStart
        else MaterialTheme.shapes.extraSmall.topStart,
    )

    Surface(
        modifier = modifier
            .defaultMinSize(minWidth = 56.dp, minHeight = 1.dp)
            .clip(shape)
            .combinedClickable(
                onClick = onClick,
                onLongClick = { openPopup.value = true }
            ),
        contentColor = if (checked) MaterialTheme.colorScheme.surfaceContainerLowest else color,
        color = if (checked) color else Color.Transparent,
        shape = shape,
        border = BorderStroke(1.dp, color),
    ) {
        Icon(
            modifier = Modifier.padding(8.dp),
            imageVector = icon,
            contentDescription = text,
        )

        if (openPopup.value) {
            Tooltip(text, openPopup)
        }
    }
}

@Composable
fun CheckChip(
    modifier: Modifier = Modifier,
    checked: Boolean,
    textId: Int,
    checkedTextId: Int,
    onCheckedChange: (Boolean) -> Unit,
) {
    val (checked, check) = remember(checked) { mutableStateOf(checked) }   //TODO hg42 should probably be removed like for MultiChips

    FilterChip(
        modifier = modifier,
        selected = checked,
        colors = FilterChipDefaults.filterChipColors(
            labelColor = MaterialTheme.colorScheme.onSurface,
            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
            iconColor = MaterialTheme.colorScheme.onSurface,
            selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
            containerColor = Color.Transparent,
            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        shape = MaterialTheme.shapes.medium,
        leadingIcon = {
            if (checked) ButtonIcon(Phosphor.Checks, R.string.enabled)
        },
        onClick = {
            onCheckedChange(!checked)
            check(!checked)
        },
        label = {
            Row(modifier = Modifier.padding(vertical = 10.dp)) {
                Text(text = stringResource(id = if (checked) checkedTextId else textId))
            }
        }
    )
}

@Composable
fun ActionChip(
    modifier: Modifier = Modifier,
    text: String = "",
    icon: ImageVector? = null,
    positive: Boolean,
    fullWidth: Boolean = false,
    onClick: () -> Unit = {},
) {
    AssistChip(
        modifier = modifier,
        label = {
            Text(
                modifier = Modifier
                    .padding(vertical = 8.dp)
                    .ifThen(fullWidth) {
                        fillMaxWidth()
                    },
                text = text,
                textAlign = TextAlign.Center,
            )
        },
        leadingIcon = {
            icon?.let {
                Icon(
                    imageVector = it,
                    contentDescription = text
                )
            }
        },
        shape = MaterialTheme.shapes.large,
        colors = AssistChipDefaults.assistChipColors(
            containerColor = if (positive) MaterialTheme.colorScheme.primaryContainer
            else MaterialTheme.colorScheme.tertiaryContainer,
            labelColor = if (positive) MaterialTheme.colorScheme.onPrimaryContainer
            else MaterialTheme.colorScheme.onTertiaryContainer,
            leadingIconContentColor = if (positive) MaterialTheme.colorScheme.onPrimaryContainer
            else MaterialTheme.colorScheme.onTertiaryContainer,
        ),
        border = null,
        onClick = onClick
    )
}

@Composable
fun SwitchChip(
    firstTextId: Int,
    firstIcon: ImageVector,
    secondTextId: Int,
    secondIcon: ImageVector,
    firstSelected: Boolean = true,
    onCheckedChange: (Boolean) -> Unit,
) {
    val colors = SegmentedButtonDefaults.colors(
        inactiveContainerColor = Color.Transparent,
        activeContainerColor = MaterialTheme.colorScheme.primaryContainer,
        inactiveContentColor = MaterialTheme.colorScheme.onSurface,
        activeContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        activeBorderColor = Color.Transparent,
        inactiveBorderColor = Color.Transparent,
    )
    var firstSelected by remember { mutableStateOf(firstSelected) }

    SingleChoiceSegmentedButtonRow(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.surfaceContainer, MaterialTheme.shapes.medium)
            .padding(horizontal = 6.dp)
            .fillMaxWidth(),
    ) {
        SegmentedButton(
            modifier = Modifier.weight(1f),
            selected = firstSelected,
            shape = MaterialTheme.shapes.small,
            colors = colors,
            icon = {
                ButtonIcon(firstIcon, firstTextId)
            },
            onClick = {
                onCheckedChange(true)
                firstSelected = true
            }
        ) {
            Text(text = stringResource(id = firstTextId))
        }
        SegmentedButton(
            modifier = Modifier.weight(1f),
            selected = !firstSelected,
            shape = MaterialTheme.shapes.small,
            colors = colors,
            icon = {
                ButtonIcon(secondIcon, secondTextId)
            },
            onClick = {
                onCheckedChange(false)
                firstSelected = false
            }
        ) {
            Text(text = stringResource(id = secondTextId))
        }
    }
}