package com.machiav3lli.backup.ui.compose.icons.phosphor

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType.Companion.NonZero
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap.Companion.Butt
import androidx.compose.ui.graphics.StrokeJoin.Companion.Miter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.ImageVector.Builder
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import com.machiav3lli.backup.ui.compose.icons.Phosphor

val Phosphor.`Battery-low`: ImageVector
    get() {
        if (`_battery-low` != null) {
            return `_battery-low`!!
        }
        `_battery-low` = Builder(
            name = "Battery-low", defaultWidth = 256.0.dp, defaultHeight =
            256.0.dp, viewportWidth = 256.0f, viewportHeight = 256.0f
        ).apply {
            path(
                fill = SolidColor(Color(0xFF000000)), stroke = null, strokeLineWidth = 0.0f,
                strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                pathFillType = NonZero
            ) {
                moveTo(256.0f, 96.0f)
                verticalLineToRelative(64.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, true, -16.0f, 0.0f)
                lineTo(240.0f, 96.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, true, 16.0f, 0.0f)
                close()
                moveTo(224.0f, 72.0f)
                lineTo(224.0f, 184.0f)
                arcToRelative(24.1f, 24.1f, 0.0f, false, true, -24.0f, 24.0f)
                lineTo(48.0f, 208.0f)
                arcToRelative(24.1f, 24.1f, 0.0f, false, true, -24.0f, -24.0f)
                lineTo(24.0f, 72.0f)
                arcTo(24.1f, 24.1f, 0.0f, false, true, 48.0f, 48.0f)
                lineTo(200.0f, 48.0f)
                arcTo(24.1f, 24.1f, 0.0f, false, true, 224.0f, 72.0f)
                close()
                moveTo(208.0f, 72.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, false, -8.0f, -8.0f)
                lineTo(48.0f, 64.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, false, -8.0f, 8.0f)
                lineTo(40.0f, 184.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, false, 8.0f, 8.0f)
                lineTo(200.0f, 192.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, false, 8.0f, -8.0f)
                close()
                moveTo(68.8f, 80.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, false, -8.0f, 8.0f)
                verticalLineToRelative(80.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, false, 16.0f, 0.0f)
                lineTo(76.8f, 88.0f)
                arcTo(8.0f, 8.0f, 0.0f, false, false, 68.8f, 80.0f)
                close()
            }
        }
            .build()
        return `_battery-low`!!
    }

private var `_battery-low`: ImageVector? = null
