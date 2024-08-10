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

val Phosphor.`Smiley-nervous`: ImageVector
    get() {
        if (`_smiley-nervous` != null) {
            return `_smiley-nervous`!!
        }
        `_smiley-nervous` = Builder(
            name = "Smiley-nervous", defaultWidth = 256.0.dp, defaultHeight
            = 256.0.dp, viewportWidth = 256.0f, viewportHeight = 256.0f
        ).apply {
            path(
                fill = SolidColor(Color(0xFF000000)), stroke = null, strokeLineWidth = 0.0f,
                strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                pathFillType = NonZero
            ) {
                moveTo(80.0f, 108.0f)
                arcToRelative(12.0f, 12.0f, 0.0f, true, true, 12.0f, 12.0f)
                arcTo(12.0f, 12.0f, 0.0f, false, true, 80.0f, 108.0f)
                close()
                moveTo(164.0f, 120.0f)
                arcToRelative(12.0f, 12.0f, 0.0f, true, false, -12.0f, -12.0f)
                arcTo(12.0f, 12.0f, 0.0f, false, false, 164.0f, 120.0f)
                close()
                moveTo(232.0f, 128.0f)
                arcTo(104.0f, 104.0f, 0.0f, true, true, 128.0f, 24.0f)
                arcTo(104.2f, 104.2f, 0.0f, false, true, 232.0f, 128.0f)
                close()
                moveTo(216.0f, 128.0f)
                arcToRelative(88.0f, 88.0f, 0.0f, true, false, -88.0f, 88.0f)
                arcTo(88.1f, 88.1f, 0.0f, false, false, 216.0f, 128.0f)
                close()
                moveTo(166.2f, 147.0f)
                arcToRelative(7.9f, 7.9f, 0.0f, false, false, -12.4f, 0.0f)
                lineTo(144.0f, 159.2f)
                lineTo(134.2f, 147.0f)
                arcToRelative(7.9f, 7.9f, 0.0f, false, false, -12.4f, 0.0f)
                lineTo(112.0f, 159.2f)
                lineTo(102.2f, 147.0f)
                arcToRelative(7.9f, 7.9f, 0.0f, false, false, -12.4f, 0.0f)
                lineToRelative(-16.0f, 20.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, false, 12.4f, 10.0f)
                lineTo(96.0f, 164.8f)
                lineToRelative(9.8f, 12.2f)
                arcToRelative(7.9f, 7.9f, 0.0f, false, false, 12.4f, 0.0f)
                lineToRelative(9.8f, -12.2f)
                lineToRelative(9.8f, 12.2f)
                arcToRelative(7.9f, 7.9f, 0.0f, false, false, 12.4f, 0.0f)
                lineToRelative(9.8f, -12.2f)
                lineToRelative(9.8f, 12.2f)
                arcToRelative(7.8f, 7.8f, 0.0f, false, false, 6.2f, 3.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, false, 6.2f, -13.0f)
                close()
            }
        }
            .build()
        return `_smiley-nervous`!!
    }

private var `_smiley-nervous`: ImageVector? = null
