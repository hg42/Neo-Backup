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

val Phosphor.NewspaperClipping: ImageVector
    get() {
        if (_newspaper_clipping != null) {
            return _newspaper_clipping!!
        }
        _newspaper_clipping = Builder(
            name = "Newspaper-clipping",
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
                moveTo(216.0f, 40.0f)
                lineTo(40.0f, 40.0f)
                arcTo(16.0f, 16.0f, 0.0f, false, false, 24.0f, 56.0f)
                lineTo(24.0f, 216.0f)
                arcToRelative(7.9f, 7.9f, 0.0f, false, false, 3.8f, 6.8f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, false, 7.8f, 0.4f)
                lineTo(64.0f, 208.9f)
                lineToRelative(28.4f, 14.3f)
                arcToRelative(8.3f, 8.3f, 0.0f, false, false, 7.2f, 0.0f)
                lineTo(128.0f, 208.9f)
                lineToRelative(28.4f, 14.3f)
                arcToRelative(8.5f, 8.5f, 0.0f, false, false, 7.2f, 0.0f)
                lineTo(192.0f, 208.9f)
                lineToRelative(28.4f, 14.3f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, false, 7.8f, -0.4f)
                arcTo(7.9f, 7.9f, 0.0f, false, false, 232.0f, 216.0f)
                lineTo(232.0f, 56.0f)
                arcTo(16.0f, 16.0f, 0.0f, false, false, 216.0f, 40.0f)
                close()
                moveTo(216.0f, 203.1f)
                lineTo(195.6f, 192.8f)
                arcToRelative(8.3f, 8.3f, 0.0f, false, false, -7.2f, 0.0f)
                lineTo(160.0f, 207.1f)
                lineToRelative(-28.4f, -14.3f)
                arcToRelative(8.5f, 8.5f, 0.0f, false, false, -7.2f, 0.0f)
                lineTo(96.0f, 207.1f)
                lineTo(67.6f, 192.8f)
                arcToRelative(8.3f, 8.3f, 0.0f, false, false, -7.2f, 0.0f)
                lineTo(40.0f, 203.1f)
                lineTo(40.0f, 56.0f)
                lineTo(216.0f, 56.0f)
                close()
                moveTo(136.0f, 112.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, true, 8.0f, -8.0f)
                horizontalLineToRelative(48.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, true, 0.0f, 16.0f)
                lineTo(144.0f, 120.0f)
                arcTo(8.0f, 8.0f, 0.0f, false, true, 136.0f, 112.0f)
                close()
                moveTo(136.0f, 144.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, true, 8.0f, -8.0f)
                horizontalLineToRelative(48.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, true, 0.0f, 16.0f)
                lineTo(144.0f, 152.0f)
                arcTo(8.0f, 8.0f, 0.0f, false, true, 136.0f, 144.0f)
                close()
                moveTo(64.0f, 168.0f)
                horizontalLineToRelative(48.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, false, 8.0f, -8.0f)
                lineTo(120.0f, 96.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, false, -8.0f, -8.0f)
                lineTo(64.0f, 88.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, false, -8.0f, 8.0f)
                verticalLineToRelative(64.0f)
                arcTo(8.0f, 8.0f, 0.0f, false, false, 64.0f, 168.0f)
                close()
                moveTo(72.0f, 104.0f)
                horizontalLineToRelative(32.0f)
                verticalLineToRelative(48.0f)
                lineTo(72.0f, 152.0f)
                close()
            }
        }
            .build()
        return _newspaper_clipping!!
    }

private var _newspaper_clipping: ImageVector? = null



@Preview
@Composable
fun NewspaperClippingPreview() {
    Image(
        Phosphor.NewspaperClipping,
        null
    )
}
