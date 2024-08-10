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

val Phosphor.`Gender-neuter`: ImageVector
    get() {
        if (`_gender-neuter` != null) {
            return `_gender-neuter`!!
        }
        `_gender-neuter` = Builder(
            name = "Gender-neuter", defaultWidth = 256.0.dp, defaultHeight =
            256.0.dp, viewportWidth = 256.0f, viewportHeight = 256.0f
        ).apply {
            path(
                fill = SolidColor(Color(0xFF000000)), stroke = null, strokeLineWidth = 0.0f,
                strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                pathFillType = NonZero
            ) {
                moveTo(208.0f, 104.0f)
                arcToRelative(80.0f, 80.0f, 0.0f, true, false, -88.0f, 79.6f)
                lineTo(120.0f, 232.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, false, 16.0f, 0.0f)
                lineTo(136.0f, 183.6f)
                arcTo(80.1f, 80.1f, 0.0f, false, false, 208.0f, 104.0f)
                close()
                moveTo(128.0f, 168.0f)
                arcToRelative(64.0f, 64.0f, 0.0f, true, true, 64.0f, -64.0f)
                arcTo(64.1f, 64.1f, 0.0f, false, true, 128.0f, 168.0f)
                close()
            }
        }
            .build()
        return `_gender-neuter`!!
    }

private var `_gender-neuter`: ImageVector? = null
