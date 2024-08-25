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

val Phosphor.`Arrow-bend-double-up-right`: ImageVector
    get() {
        if (`_arrow-bend-double-up-right` != null) {
            return `_arrow-bend-double-up-right`!!
        }
        `_arrow-bend-double-up-right` = Builder(
            name = "Arrow-bend-double-up-right", defaultWidth =
            256.0.dp, defaultHeight = 256.0.dp, viewportWidth = 256.0f, viewportHeight =
            256.0f
        ).apply {
            path(
                fill = SolidColor(Color(0xFF000000)), stroke = null, strokeLineWidth = 0.0f,
                strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                pathFillType = NonZero
            ) {
                moveTo(229.7f, 109.7f)
                lineToRelative(-48.0f, 48.0f)
                arcToRelative(8.2f, 8.2f, 0.0f, false, true, -11.4f, 0.0f)
                arcToRelative(8.1f, 8.1f, 0.0f, false, true, 0.0f, -11.4f)
                lineTo(212.7f, 104.0f)
                lineTo(170.3f, 61.7f)
                arcToRelative(8.1f, 8.1f, 0.0f, false, true, 11.4f, -11.4f)
                lineToRelative(48.0f, 48.0f)
                arcTo(8.1f, 8.1f, 0.0f, false, true, 229.7f, 109.7f)
                close()
                moveTo(181.7f, 98.3f)
                lineTo(133.7f, 50.3f)
                arcToRelative(8.1f, 8.1f, 0.0f, false, false, -11.4f, 11.4f)
                lineTo(156.7f, 96.0f)
                lineTo(128.0f, 96.0f)
                arcTo(104.2f, 104.2f, 0.0f, false, false, 24.0f, 200.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, false, 16.0f, 0.0f)
                arcToRelative(88.1f, 88.1f, 0.0f, false, true, 88.0f, -88.0f)
                horizontalLineToRelative(28.7f)
                lineToRelative(-34.4f, 34.3f)
                arcToRelative(8.1f, 8.1f, 0.0f, false, false, 0.0f, 11.4f)
                arcToRelative(8.2f, 8.2f, 0.0f, false, false, 11.4f, 0.0f)
                lineToRelative(48.0f, -48.0f)
                arcTo(8.1f, 8.1f, 0.0f, false, false, 181.7f, 98.3f)
                close()
            }
        }
            .build()
        return `_arrow-bend-double-up-right`!!
    }

private var `_arrow-bend-double-up-right`: ImageVector? = null
