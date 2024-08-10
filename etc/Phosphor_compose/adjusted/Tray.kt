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

val Phosphor.Tray: ImageVector
    get() {
        if (_tray != null) {
            return _tray!!
        }
        _tray = Builder(
            name = "Tray",
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
                moveTo(208.0f, 32.0f)
                lineTo(48.0f, 32.0f)
                arcTo(16.0f, 16.0f, 0.0f, false, false, 32.0f, 48.0f)
                lineTo(32.0f, 159.9f)
                horizontalLineToRelative(0.0f)
                lineTo(32.0f, 208.0f)
                arcToRelative(16.0f, 16.0f, 0.0f, false, false, 16.0f, 16.0f)
                lineTo(208.0f, 224.0f)
                arcToRelative(16.0f, 16.0f, 0.0f, false, false, 16.0f, -16.0f)
                lineTo(224.0f, 48.0f)
                arcTo(16.0f, 16.0f, 0.0f, false, false, 208.0f, 32.0f)
                close()
                moveTo(208.0f, 48.0f)
                lineTo(208.0f, 152.0f)
                lineTo(179.3f, 152.0f)
                arcToRelative(15.9f, 15.9f, 0.0f, false, false, -11.3f, 4.7f)
                lineTo(148.7f, 176.0f)
                lineTo(107.3f, 176.0f)
                lineTo(88.0f, 156.7f)
                arcTo(15.9f, 15.9f, 0.0f, false, false, 76.7f, 152.0f)
                lineTo(48.0f, 152.0f)
                lineTo(48.0f, 48.0f)
                close()
                moveTo(208.0f, 208.0f)
                lineTo(48.0f, 208.0f)
                lineTo(48.0f, 168.0f)
                lineTo(76.7f, 168.0f)
                lineTo(96.0f, 187.3f)
                arcToRelative(15.9f, 15.9f, 0.0f, false, false, 11.3f, 4.7f)
                horizontalLineToRelative(41.4f)
                arcToRelative(15.9f, 15.9f, 0.0f, false, false, 11.3f, -4.7f)
                lineTo(179.3f, 168.0f)
                lineTo(208.0f, 168.0f)
                verticalLineToRelative(40.0f)
                close()
            }
        }
            .build()
        return _tray!!
    }

private var _tray: ImageVector? = null



@Preview
@Composable
fun TrayPreview() {
    Image(
        Phosphor.Tray,
        null
    )
}
