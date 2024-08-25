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

val Phosphor.`Test-tube`: ImageVector
    get() {
        if (`_test-tube` != null) {
            return `_test-tube`!!
        }
        `_test-tube` = Builder(
            name = "Test-tube", defaultWidth = 256.0.dp, defaultHeight =
            256.0.dp, viewportWidth = 256.0f, viewportHeight = 256.0f
        ).apply {
            path(
                fill = SolidColor(Color(0xFF000000)), stroke = null, strokeLineWidth = 0.0f,
                strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                pathFillType = NonZero
            ) {
                moveTo(237.7f, 86.3f)
                lineToRelative(-60.0f, -60.0f)
                arcToRelative(8.1f, 8.1f, 0.0f, false, false, -11.4f, 0.0f)
                lineTo(62.4f, 130.3f)
                horizontalLineToRelative(0.0f)
                lineToRelative(-26.1f, 26.0f)
                arcToRelative(44.8f, 44.8f, 0.0f, false, false, 63.4f, 63.4f)
                lineToRelative(77.9f, -78.0f)
                horizontalLineToRelative(0.0f)
                lineTo(212.3f, 107.0f)
                lineToRelative(22.2f, -7.4f)
                arcToRelative(8.2f, 8.2f, 0.0f, false, false, 5.3f, -5.8f)
                arcTo(8.3f, 8.3f, 0.0f, false, false, 237.7f, 86.3f)
                close()
                moveTo(88.3f, 208.3f)
                arcToRelative(28.5f, 28.5f, 0.0f, false, true, -40.6f, 0.0f)
                arcToRelative(28.5f, 28.5f, 0.0f, false, true, 0.0f, -40.6f)
                lineToRelative(25.6f, -25.6f)
                curveToRelative(2.1f, -1.6f, 17.9f, -11.6f, 43.1f, 1.1f)
                curveToRelative(11.0f, 5.5f, 20.8f, 7.6f, 29.2f, 7.9f)
                close()
                moveTo(205.5f, 92.4f)
                arcToRelative(9.4f, 9.4f, 0.0f, false, false, -3.2f, 1.9f)
                lineToRelative(-35.6f, 35.6f)
                curveToRelative(-2.1f, 1.6f, -17.8f, 11.6f, -43.1f, -1.1f)
                curveToRelative(-11.0f, -5.5f, -20.8f, -7.6f, -29.2f, -7.9f)
                lineTo(172.0f, 43.3f)
                lineToRelative(45.2f, 45.2f)
                close()
            }
        }
            .build()
        return `_test-tube`!!
    }

private var `_test-tube`: ImageVector? = null
