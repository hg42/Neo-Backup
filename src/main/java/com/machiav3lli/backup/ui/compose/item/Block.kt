package com.machiav3lli.backup.ui.compose.item

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.machiav3lli.backup.ui.compose.ShowIf
import com.machiav3lli.backup.ui.compose.icons.Phosphor
import com.machiav3lli.backup.ui.compose.icons.phosphor.CaretDown
import com.machiav3lli.backup.ui.compose.icons.phosphor.CaretUp

@Composable
fun ExpandableBlock(
    modifier: Modifier = Modifier,
    heading: String? = null,
    icon: ImageVector? = null,
    preExpanded: Boolean = true,
    content: @Composable ColumnScope.() -> Unit,
) {
    var expanded by rememberSaveable { mutableStateOf(preExpanded) }

    val surfaceColor =
        if (expanded) MaterialTheme.colorScheme.surfaceContainerHighest
        else MaterialTheme.colorScheme.surfaceContainerHigh
    //val surfaceColor by animateColorAsState(
    //    targetValue = if (expanded) MaterialTheme.colorScheme.surfaceContainerHighest
    //    else Color.Transparent,
    //    label = "surfaceColor"
    //)

    val expandedPadding = if (expanded) 2.dp else 0.dp
    //val expandedPadding by animateDpAsState(
    //    targetValue = if (expanded) 2.dp else 0.dp,
    //    label = "expandedPadding"
    //)

    Surface(
        modifier = Modifier
            //.animateContentSize()
            .padding(vertical = expandedPadding),
        shape = MaterialTheme.shapes.large,
        onClick = { expanded = !expanded },
        color = surfaceColor
    ) {
        Column(modifier = modifier) {
            ExpandableBlockHeader(heading, icon, expanded = expanded)
            ShowIf(expanded) {
                Column(
                    Modifier.padding(
                        top = 0.dp,
                        bottom = 8.dp,
                        start = 8.dp,
                        end = 8.dp,
                    )
                ) {
                    content()
                }
            }
        }
    }
}

@Composable
fun ExpandableBlockHeader(
    heading: String? = null,
    icon: ImageVector? = null,
    withToggle: Boolean = true,
    expanded: Boolean = true,
) {
    var spacerHeight = 0
    if (heading == null) spacerHeight += 8
    Spacer(modifier = Modifier.requiredHeight(spacerHeight.dp))
    if (heading != null) {
        Row(
            modifier = Modifier
                .padding(
                    top = 4.dp,
                    bottom = 4.dp,
                    start = 8.dp,
                    end = 8.dp,
                )
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            icon?.let {
                Icon(
                    imageVector = icon,
                    contentDescription = heading
                )
            }
            Text(
                modifier = Modifier.weight(1f),
                text = heading,
                style = MaterialTheme.typography.titleMedium,
            )
            if (withToggle)
                Icon(
                    modifier = Modifier.size(24.dp),
                    imageVector = if (expanded) Phosphor.CaretUp else Phosphor.CaretDown,
                    contentDescription = heading
                )
        }
    }
}