package com.machiav3lli.backup.ui.compose.item

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.machiav3lli.backup.ICON_SIZE_SMALL
import com.machiav3lli.backup.OABX
import com.machiav3lli.backup.R
import com.machiav3lli.backup.preferences.pref_busyIconScale
import com.machiav3lli.backup.preferences.pref_busyIconTurnTime
import com.machiav3lli.backup.ui.compose.icons.Phosphor
import com.machiav3lli.backup.ui.compose.icons.phosphor.ArrowsClockwise
import kotlin.math.max

@Composable
fun DialogPositiveButton(
    modifier: Modifier = Modifier,
    text: String,
    onClick: () -> Unit = {}
) {
    TextButton(
        shape = MaterialTheme.shapes.large,
        onClick = onClick,
        modifier = modifier,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        )
    ) {
        Text(
            text = text,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.padding(top = 5.dp, bottom = 5.dp)
        )
    }
}

@Composable
fun DialogPositiveButton(
    modifier: Modifier = Modifier,
    text: String,
    icon: ImageVector?,
    onClick: () -> Unit = {}
) {
    ElevatedButton(
        shape = MaterialTheme.shapes.large,
        onClick = onClick,
        modifier = modifier,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        )
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = text
            )
        }
        Text(
            text = text,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.padding(top = 5.dp, bottom = 5.dp)
        )
    }
}

@Composable
fun DialogNegativeButton(
    modifier: Modifier = Modifier,
    text: String,
    onClick: () -> Unit = {}
) {
    TextButton(
        shape = MaterialTheme.shapes.large,
        onClick = onClick,
        modifier = modifier
    ) {
        Text(
            text = text,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.padding(vertical = 5.dp, horizontal = 8.dp)
        )
    }
}


@Composable
fun ActionButton(
    text: String,
    modifier: Modifier = Modifier,
    positive: Boolean = true,
    iconOnSide: Boolean = false,
    icon: ImageVector? = null,
    onClick: () -> Unit,
) {
    TextButton(
        modifier = modifier,
        colors = ButtonDefaults.textButtonColors(
            contentColor = if (positive) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.tertiary
        ),
        onClick = onClick
    ) {
        Text(
            modifier = Modifier.padding(horizontal = 4.dp),
            text = text,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.titleSmall
        )
        if (icon != null) {
            if (iconOnSide) Spacer(modifier = Modifier.weight(1f))
            Icon(
                imageVector = icon,
                contentDescription = text
            )
        }
    }
}

@Composable
fun ElevatedActionButton(
    modifier: Modifier = Modifier,
    text: String,
    positive: Boolean = true,
    icon: ImageVector? = null,
    fullWidth: Boolean = false,
    enabled: Boolean = true,
    colored: Boolean = true,
    withText: Boolean = text.isNotEmpty(),
    onClick: () -> Unit,
) {
    ElevatedButton(
        modifier = modifier,
        colors = ButtonDefaults.elevatedButtonColors(
            contentColor = when {
                !colored -> MaterialTheme.colorScheme.onSurface
                positive -> MaterialTheme.colorScheme.onPrimaryContainer
                else     -> MaterialTheme.colorScheme.onTertiaryContainer
            },
            containerColor = when {
                !colored -> MaterialTheme.colorScheme.surfaceContainer
                positive -> MaterialTheme.colorScheme.primaryContainer
                else     -> MaterialTheme.colorScheme.tertiaryContainer
            }
        ),
        enabled = enabled,
        onClick = onClick
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = text
            )
        }
        if (withText)
            Text(
                modifier = when {
                    fullWidth -> Modifier.weight(1f)
                    else      -> Modifier.padding(start = 8.dp)
                },
                text = text,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleSmall
            )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CardButton(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainerHigh,
    description: String,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    val showTooltip = remember { mutableStateOf(false) }

    ListItem(
        modifier = modifier
            .clip(MaterialTheme.shapes.extraLarge)
            .combinedClickable(
                onClick = onClick,
                onLongClick = { showTooltip.value = true },
                enabled = enabled,
            ),
        colors = ListItemDefaults.colors(
            leadingIconColor = contentColor,
            headlineColor = contentColor,
            containerColor = containerColor,
        ),
        leadingContent = {
            Icon(imageVector = icon, contentDescription = description)
        },
        headlineContent = {
            Text(
                text = description,
                overflow = TextOverflow.Ellipsis,
                maxLines = 2,
                style = MaterialTheme.typography.titleSmall
            )

            if (showTooltip.value) {
                Tooltip(description, showTooltip)
            }
        }
    )
}


@Composable
fun RoundButton(
    modifier: Modifier = Modifier,
    size: Dp = ICON_SIZE_SMALL,
    icon: ImageVector,
    tint: Color = MaterialTheme.colorScheme.onSurface,
    description: String = "",
    onClick: () -> Unit,
) {
    IconButton(
        modifier = modifier,
        onClick = onClick
    ) {
        Icon(
            modifier = Modifier.size(size),
            imageVector = icon,
            tint = tint,
            contentDescription = description
        )
    }
}

@Composable
fun RefreshButton(
    modifier: Modifier = Modifier,
    size: Dp = ICON_SIZE_SMALL,
    tint: Color = MaterialTheme.colorScheme.onSurface,
    hideIfNotBusy: Boolean = false,
    onClick: () -> Unit = {},
) {
    val isBusy by remember { OABX.busy }

    if (hideIfNotBusy && isBusy.not())
        return

    val (angle, scale) = if (isBusy) {
        val infiniteTransition = rememberInfiniteTransition(label = "infiniteTransition")

        // Animate from 0f to 1f
        val animationProgress by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = pref_busyIconTurnTime.value,
                    easing = LinearEasing
                )
            ), label = "animationProgress"
        )
        val angle = 360f * animationProgress
        val scale = 0.01f * pref_busyIconScale.value
        angle to scale
    } else {
        0f to 1f
    }

    RoundButton(
        description = stringResource(id = R.string.refresh),
        icon = Phosphor.ArrowsClockwise,
        size = size,
        tint = if (isBusy) Color.Red else tint,
        modifier = modifier
            .scale(scale)
            .rotate(angle),
        onClick = onClick
    )
}


@Preview
@Composable
fun RefreshButtonPreview() {
    OABX.fakeContext = LocalContext.current.applicationContext

    val level by remember { OABX.busyLevel }
    val factor = 1.0 / max(1, level)

    Column {
        Text("factor: $factor")
        Text("level: $level")
        Text("time: ${(pref_busyIconTurnTime.value * factor).toInt()}")
        Row {
            RefreshButton()
            ActionButton(text = "hit") {
                OABX.hitBusy()
            }
            ActionButton(text = "begin") {
                OABX.beginBusy()
            }
            ActionButton(text = "end") {
                OABX.endBusy()
            }
        }
    }
}