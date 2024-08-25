package com.machiav3lli.backup.ui.compose.icons.phosphor


import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview

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

val Phosphor.CellSignalSlash: ImageVector
    get() {
        if (_cell_signal_slash != null) {
            return _cell_signal_slash!!
        }
        _cell_signal_slash = Builder(
            name = "Cell-signal-slash",
            defaultWidth = 24.0.dp,
            defaultHeight = 24.0.dp,
            viewportWidth = 256.0f,
            viewportHeight = 256.0f,
        ).apply {
            path(
                fill = SolidColor(Color(0xFF000000)), stroke = null, strokeLineWidth = 0.0f,
                strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                pathFillType = NonZero
            ) {
                moveTo(129.8f, 94.2f)
                arcToRelative(7.9f, 7.9f, 0.0f, false, true, 0.0f, -11.3f)
                lineTo(180.7f, 32.0f)
                arcTo(16.0f, 16.0f, 0.0f, false, true, 208.0f, 43.3f)
                verticalLineTo(159.6f)
                arcToRelative(8.0f, 8.0f, 0.0f, true, true, -16.0f, 0.0f)
                verticalLineTo(43.3f)
                lineTo(141.1f, 94.2f)
                arcTo(7.9f, 7.9f, 0.0f, false, true, 129.8f, 94.2f)
                close()
                moveTo(216.0f, 216.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, true, -8.0f, 8.0f)
                horizontalLineTo(27.3f)
                arcTo(16.0f, 16.0f, 0.0f, false, true, 16.0f, 196.7f)
                lineTo(101.7f, 111.0f)
                lineTo(42.1f, 45.4f)
                arcToRelative(7.9f, 7.9f, 0.0f, false, true, 0.5f, -11.3f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, true, 11.3f, 0.5f)
                lineToRelative(160.0f, 176.0f)
                horizontalLineToRelative(0.0f)
                arcTo(8.1f, 8.1f, 0.0f, false, true, 216.0f, 216.0f)
                close()
                moveTo(27.3f, 208.0f)
                horizontalLineTo(189.9f)
                lineToRelative(-77.4f, -85.2f)
                close()
            }
        }
            .build()
        return _cell_signal_slash!!
    }

private var _cell_signal_slash: ImageVector? = null



@Preview
@Composable
fun CellSignalSlashPreview() {
    Image(
        Phosphor.CellSignalSlash,
        null
    )
}
