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

val Phosphor.Umbrella: ImageVector
    get() {
        if (_umbrella != null) {
            return _umbrella!!
        }
        _umbrella = Builder(
            name = "Umbrella", defaultWidth = 256.0.dp, defaultHeight = 256.0.dp,
            viewportWidth = 256.0f, viewportHeight = 256.0f
        ).apply {
            path(
                fill = SolidColor(Color(0xFF000000)), stroke = null, strokeLineWidth = 0.0f,
                strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                pathFillType = NonZero
            ) {
                moveTo(239.6f, 126.6f)
                arcTo(112.1f, 112.1f, 0.0f, false, false, 128.0f, 24.0f)
                horizontalLineToRelative(0.0f)
                arcTo(112.1f, 112.1f, 0.0f, false, false, 16.4f, 126.6f)
                arcToRelative(15.7f, 15.7f, 0.0f, false, false, 4.2f, 12.2f)
                arcTo(15.9f, 15.9f, 0.0f, false, false, 32.3f, 144.0f)
                lineTo(120.0f, 144.0f)
                verticalLineToRelative(56.0f)
                arcToRelative(32.0f, 32.0f, 0.0f, false, false, 64.0f, 0.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, false, -16.0f, 0.0f)
                arcToRelative(16.0f, 16.0f, 0.0f, false, true, -32.0f, 0.0f)
                lineTo(136.0f, 144.0f)
                horizontalLineToRelative(87.7f)
                arcToRelative(15.9f, 15.9f, 0.0f, false, false, 11.7f, -5.2f)
                arcTo(15.7f, 15.7f, 0.0f, false, false, 239.6f, 126.6f)
                close()
                moveTo(96.2f, 128.0f)
                curveToRelative(1.4f, -30.8f, 10.5f, -52.8f, 18.3f, -66.2f)
                arcTo(97.7f, 97.7f, 0.0f, false, true, 128.0f, 43.2f)
                arcToRelative(97.7f, 97.7f, 0.0f, false, true, 13.5f, 18.6f)
                curveToRelative(13.3f, 22.9f, 17.5f, 47.5f, 18.3f, 66.2f)
                close()
                moveTo(32.3f, 128.0f)
                arcToRelative(95.9f, 95.9f, 0.0f, false, true, 75.9f, -85.9f)
                curveToRelative(-11.7f, 16.0f, -26.4f, 44.0f, -28.0f, 85.9f)
                close()
                moveTo(175.8f, 128.0f)
                curveToRelative(-1.6f, -41.9f, -16.3f, -69.9f, -28.0f, -85.9f)
                arcTo(95.9f, 95.9f, 0.0f, false, true, 223.7f, 128.0f)
                close()
            }
        }
            .build()
        return _umbrella!!
    }

private var _umbrella: ImageVector? = null
