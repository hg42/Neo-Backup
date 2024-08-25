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

val Phosphor.`Map-pin-line`: ImageVector
    get() {
        if (`_map-pin-line` != null) {
            return `_map-pin-line`!!
        }
        `_map-pin-line` = Builder(
            name = "Map-pin-line", defaultWidth = 256.0.dp, defaultHeight =
            256.0.dp, viewportWidth = 256.0f, viewportHeight = 256.0f
        ).apply {
            path(
                fill = SolidColor(Color(0xFF000000)), stroke = null, strokeLineWidth = 0.0f,
                strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                pathFillType = NonZero
            ) {
                moveTo(200.1f, 224.0f)
                lineTo(150.7f, 224.0f)
                arcToRelative(266.5f, 266.5f, 0.0f, false, false, 23.5f, -23.8f)
                curveToRelative(27.4f, -31.5f, 41.9f, -64.8f, 41.9f, -96.2f)
                arcToRelative(88.0f, 88.0f, 0.0f, false, false, -176.0f, 0.0f)
                curveToRelative(0.0f, 31.4f, 14.5f, 64.7f, 42.0f, 96.2f)
                arcTo(253.6f, 253.6f, 0.0f, false, false, 105.6f, 224.0f)
                lineTo(56.1f, 224.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, false, 0.0f, 16.0f)
                horizontalLineToRelative(144.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, true, false, 0.0f, -16.0f)
                close()
                moveTo(56.1f, 104.0f)
                arcToRelative(72.0f, 72.0f, 0.0f, false, true, 144.0f, 0.0f)
                curveToRelative(0.0f, 57.2f, -55.5f, 105.0f, -72.0f, 118.0f)
                curveTo(111.6f, 209.0f, 56.1f, 161.2f, 56.1f, 104.0f)
                close()
                moveTo(168.1f, 104.0f)
                arcToRelative(40.0f, 40.0f, 0.0f, true, false, -40.0f, 40.0f)
                arcTo(40.0f, 40.0f, 0.0f, false, false, 168.1f, 104.0f)
                close()
                moveTo(104.1f, 104.0f)
                arcToRelative(24.0f, 24.0f, 0.0f, true, true, 24.0f, 24.0f)
                arcTo(24.1f, 24.1f, 0.0f, false, true, 104.1f, 104.0f)
                close()
            }
        }
            .build()
        return `_map-pin-line`!!
    }

private var `_map-pin-line`: ImageVector? = null
