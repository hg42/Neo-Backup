package com.machiav3lli.backup.preferences.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.machiav3lli.backup.BUTTON_SIZE_MEDIUM
import com.machiav3lli.backup.traceDebug
import com.machiav3lli.backup.ui.compose.item.ExpandableBlock
import com.machiav3lli.backup.ui.item.Pref
import kotlinx.collections.immutable.ImmutableList

@Composable
fun PrefsGroupCollapsed(prefs: ImmutableList<Pref>, heading: String) {
    if (prefs.isNotEmpty())
        ExpandableBlock(
            heading = heading,
            preExpanded = false,
        ) {
            PrefsGroup(prefs = prefs, heading = null)
        }
}

@Composable
fun PrefsGroup(
    modifier: Modifier = Modifier,
    heading: String? = null,
    content: @Composable () -> Unit,
) {
    PrefsGroupHeading(heading)
    CompositionLocalProvider(
        LocalContentColor provides MaterialTheme.colorScheme.primary
    ) {
        Surface(color = Color.Transparent) {
            Column(
                modifier = modifier.padding(0.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                content()
            }
        }
    }
}

@Composable
fun PrefsGroup(
    modifier: Modifier = Modifier,
    heading: String? = null,
    prefs: ImmutableList<Pref>,
    onPrefDialog: (Pref) -> Unit = {},
) {
    val size = prefs.size

    PrefsGroup(
        modifier = modifier,
        heading = heading
    ) {
        if (prefs.isNotEmpty()) {
            prefs.forEachIndexed { index, pref ->
                traceDebug { "${pref.key} = $pref" }
                PrefsBuilder(
                    pref,
                    onPrefDialog,
                )
            }
        }
    }
}

@Composable
fun PrefsGroupHeading(
    heading: String? = null,
    modifier: Modifier = Modifier,
) {
    if (heading != null) {
        Column(
            verticalArrangement = Arrangement.Center,
            modifier = modifier
                .height(BUTTON_SIZE_MEDIUM)
                .padding(horizontal = 32.dp)
                .fillMaxWidth(),
        ) {
            Text(
                text = heading,
                style = MaterialTheme.typography.headlineMedium,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
            )
        }
    } else {
        Spacer(modifier = Modifier.requiredHeight(8.dp))
    }
}
