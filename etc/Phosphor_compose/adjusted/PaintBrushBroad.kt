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

val Phosphor.PaintBrushBroad: ImageVector
    get() {
        if (_paint_brush_broad != null) {
            return _paint_brush_broad!!
        }
        _paint_brush_broad = Builder(
            name = "Paint-brush-broad",
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
                moveTo(216.0f, 24.0f)
                horizontalLineTo(72.0f)
                arcTo(40.0f, 40.0f, 0.0f, false, false, 32.0f, 64.0f)
                verticalLineToRelative(72.0f)
                arcToRelative(24.1f, 24.1f, 0.0f, false, false, 24.0f, 24.0f)
                horizontalLineToRelative(46.8f)
                lineToRelative(-6.7f, 46.9f)
                arcTo(3.7f, 3.7f, 0.0f, false, false, 96.0f, 208.0f)
                arcToRelative(32.0f, 32.0f, 0.0f, false, false, 64.0f, 0.0f)
                arcToRelative(3.7f, 3.7f, 0.0f, false, false, -0.1f, -1.1f)
                lineTo(153.2f, 160.0f)
                horizontalLineTo(200.0f)
                arcToRelative(24.1f, 24.1f, 0.0f, false, false, 24.0f, -24.0f)
                verticalLineTo(32.0f)
                arcTo(8.0f, 8.0f, 0.0f, false, false, 216.0f, 24.0f)
                close()
                moveTo(72.0f, 40.0f)
                horizontalLineTo(176.0f)
                verticalLineTo(80.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, false, 16.0f, 0.0f)
                verticalLineTo(40.0f)
                horizontalLineToRelative(16.0f)
                verticalLineToRelative(64.0f)
                horizontalLineTo(48.0f)
                verticalLineTo(64.0f)
                arcTo(24.1f, 24.1f, 0.0f, false, true, 72.0f, 40.0f)
                close()
                moveTo(200.0f, 144.0f)
                horizontalLineTo(153.2f)
                arcToRelative(15.9f, 15.9f, 0.0f, false, false, -15.8f, 18.3f)
                lineToRelative(6.6f, 46.2f)
                arcToRelative(16.0f, 16.0f, 0.0f, false, true, -32.0f, 0.0f)
                lineToRelative(6.6f, -46.2f)
                arcTo(15.9f, 15.9f, 0.0f, false, false, 102.8f, 144.0f)
                horizontalLineTo(56.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, true, -8.0f, -8.0f)
                verticalLineTo(120.0f)
                horizontalLineTo(208.0f)
                verticalLineToRelative(16.0f)
                arcTo(8.0f, 8.0f, 0.0f, false, true, 200.0f, 144.0f)
                close()
            }
        }
            .build()
        return _paint_brush_broad!!
    }

private var _paint_brush_broad: ImageVector? = null



@Preview
@Composable
fun PaintBrushBroadPreview() {
    Image(
        Phosphor.PaintBrushBroad,
        null
    )
}
