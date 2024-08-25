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

val Phosphor.`Smiley-sad`: ImageVector
    get() {
        if (`_smiley-sad` != null) {
            return `_smiley-sad`!!
        }
        `_smiley-sad` = Builder(
            name = "Smiley-sad", defaultWidth = 256.0.dp, defaultHeight =
            256.0.dp, viewportWidth = 256.0f, viewportHeight = 256.0f
        ).apply {
            path(
                fill = SolidColor(Color(0xFF000000)), stroke = null, strokeLineWidth = 0.0f,
                strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                pathFillType = NonZero
            ) {
                moveTo(128.0f, 24.0f)
                arcTo(104.0f, 104.0f, 0.0f, true, false, 232.0f, 128.0f)
                arcTo(104.2f, 104.2f, 0.0f, false, false, 128.0f, 24.0f)
                close()
                moveTo(128.0f, 216.0f)
                arcToRelative(88.0f, 88.0f, 0.0f, true, true, 88.0f, -88.0f)
                arcTo(88.1f, 88.1f, 0.0f, false, true, 128.0f, 216.0f)
                close()
                moveTo(80.0f, 108.0f)
                arcToRelative(12.0f, 12.0f, 0.0f, true, true, 12.0f, 12.0f)
                arcTo(12.0f, 12.0f, 0.0f, false, true, 80.0f, 108.0f)
                close()
                moveTo(152.0f, 108.0f)
                arcToRelative(12.0f, 12.0f, 0.0f, true, true, 12.0f, 12.0f)
                arcTo(12.0f, 12.0f, 0.0f, false, true, 152.0f, 108.0f)
                close()
                moveTo(176.5f, 172.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, true, -2.9f, 10.9f)
                arcToRelative(7.6f, 7.6f, 0.0f, false, true, -4.0f, 1.1f)
                arcToRelative(7.7f, 7.7f, 0.0f, false, true, -6.9f, -4.0f)
                arcToRelative(40.1f, 40.1f, 0.0f, false, false, -69.4f, 0.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, true, true, -13.8f, -8.0f)
                arcToRelative(56.0f, 56.0f, 0.0f, false, true, 97.0f, 0.0f)
                close()
            }
        }
            .build()
        return `_smiley-sad`!!
    }

private var `_smiley-sad`: ImageVector? = null
