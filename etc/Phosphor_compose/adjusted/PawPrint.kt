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

val Phosphor.PawPrint: ImageVector
    get() {
        if (_paw_print != null) {
            return _paw_print!!
        }
        _paw_print = Builder(
            name = "Paw-print",
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
                moveTo(212.0f, 80.0f)
                arcToRelative(28.0f, 28.0f, 0.0f, true, false, 28.0f, 28.0f)
                arcTo(28.1f, 28.1f, 0.0f, false, false, 212.0f, 80.0f)
                close()
                moveTo(212.0f, 120.0f)
                arcToRelative(12.0f, 12.0f, 0.0f, true, true, 12.0f, -12.0f)
                arcTo(12.0f, 12.0f, 0.0f, false, true, 212.0f, 120.0f)
                close()
                moveTo(72.0f, 108.0f)
                arcToRelative(28.0f, 28.0f, 0.0f, true, false, -28.0f, 28.0f)
                arcTo(28.1f, 28.1f, 0.0f, false, false, 72.0f, 108.0f)
                close()
                moveTo(44.0f, 120.0f)
                arcToRelative(12.0f, 12.0f, 0.0f, true, true, 12.0f, -12.0f)
                arcTo(12.0f, 12.0f, 0.0f, false, true, 44.0f, 120.0f)
                close()
                moveTo(92.0f, 88.0f)
                arcTo(28.0f, 28.0f, 0.0f, true, false, 64.0f, 60.0f)
                arcTo(28.1f, 28.1f, 0.0f, false, false, 92.0f, 88.0f)
                close()
                moveTo(92.0f, 48.0f)
                arcTo(12.0f, 12.0f, 0.0f, true, true, 80.0f, 60.0f)
                arcTo(12.0f, 12.0f, 0.0f, false, true, 92.0f, 48.0f)
                close()
                moveTo(164.0f, 88.0f)
                arcToRelative(28.0f, 28.0f, 0.0f, true, false, -28.0f, -28.0f)
                arcTo(28.1f, 28.1f, 0.0f, false, false, 164.0f, 88.0f)
                close()
                moveTo(164.0f, 48.0f)
                arcToRelative(12.0f, 12.0f, 0.0f, true, true, -12.0f, 12.0f)
                arcTo(12.0f, 12.0f, 0.0f, false, true, 164.0f, 48.0f)
                close()
                moveTo(187.1f, 148.8f)
                arcToRelative(35.3f, 35.3f, 0.0f, false, true, -16.9f, -21.1f)
                arcToRelative(43.9f, 43.9f, 0.0f, false, false, -84.4f, 0.0f)
                arcTo(35.5f, 35.5f, 0.0f, false, true, 69.0f, 148.8f)
                arcTo(40.0f, 40.0f, 0.0f, false, false, 88.0f, 224.0f)
                arcToRelative(40.5f, 40.5f, 0.0f, false, false, 15.5f, -3.1f)
                arcToRelative(64.2f, 64.2f, 0.0f, false, true, 48.9f, -0.1f)
                arcTo(39.6f, 39.6f, 0.0f, false, false, 168.0f, 224.0f)
                arcToRelative(40.0f, 40.0f, 0.0f, false, false, 19.1f, -75.2f)
                close()
                moveTo(168.0f, 208.0f)
                arcToRelative(24.1f, 24.1f, 0.0f, false, true, -9.5f, -1.9f)
                arcTo(78.7f, 78.7f, 0.0f, false, false, 128.0f, 200.0f)
                arcToRelative(79.9f, 79.9f, 0.0f, false, false, -30.6f, 6.1f)
                arcTo(23.2f, 23.2f, 0.0f, false, true, 88.0f, 208.0f)
                arcToRelative(24.0f, 24.0f, 0.0f, false, true, -11.3f, -45.2f)
                arcToRelative(51.1f, 51.1f, 0.0f, false, false, 24.4f, -30.6f)
                arcToRelative(28.0f, 28.0f, 0.0f, false, true, 53.8f, 0.0f)
                arcToRelative(51.1f, 51.1f, 0.0f, false, false, 24.4f, 30.6f)
                horizontalLineToRelative(0.1f)
                arcTo(24.0f, 24.0f, 0.0f, false, true, 168.0f, 208.0f)
                close()
            }
        }
            .build()
        return _paw_print!!
    }

private var _paw_print: ImageVector? = null



@Preview
@Composable
fun PawPrintPreview() {
    Image(
        Phosphor.PawPrint,
        null
    )
}
