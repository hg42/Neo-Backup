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

val Phosphor.FileVideo: ImageVector
    get() {
        if (_file_video != null) {
            return _file_video!!
        }
        _file_video = Builder(
            name = "File-video",
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
                moveTo(216.0f, 88.0f)
                arcToRelative(7.8f, 7.8f, 0.0f, false, false, -2.4f, -5.7f)
                lineToRelative(-55.9f, -56.0f)
                arcTo(8.1f, 8.1f, 0.0f, false, false, 152.0f, 24.0f)
                lineTo(56.0f, 24.0f)
                arcTo(16.0f, 16.0f, 0.0f, false, false, 40.0f, 40.0f)
                verticalLineToRelative(88.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, false, 16.0f, 0.0f)
                lineTo(56.0f, 40.0f)
                horizontalLineToRelative(88.0f)
                lineTo(144.0f, 88.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, false, 8.0f, 8.0f)
                horizontalLineToRelative(48.0f)
                lineTo(200.0f, 216.0f)
                lineTo(176.0f, 216.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, false, 0.0f, 16.0f)
                horizontalLineToRelative(24.0f)
                arcToRelative(16.0f, 16.0f, 0.0f, false, false, 16.0f, -16.0f)
                lineTo(216.0f, 88.0f)
                close()
                moveTo(160.0f, 51.3f)
                lineTo(188.7f, 80.0f)
                lineTo(160.0f, 80.0f)
                close()
                moveTo(148.2f, 157.2f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, false, -7.8f, -0.4f)
                lineTo(120.0f, 167.1f)
                arcTo(16.1f, 16.1f, 0.0f, false, false, 104.0f, 152.0f)
                lineTo(48.0f, 152.0f)
                arcToRelative(16.0f, 16.0f, 0.0f, false, false, -16.0f, 16.0f)
                verticalLineToRelative(40.0f)
                arcToRelative(16.0f, 16.0f, 0.0f, false, false, 16.0f, 16.0f)
                horizontalLineToRelative(56.0f)
                arcToRelative(16.0f, 16.0f, 0.0f, false, false, 15.9f, -14.7f)
                lineToRelative(20.3f, 10.9f)
                arcToRelative(7.5f, 7.5f, 0.0f, false, false, 3.8f, 1.0f)
                arcToRelative(7.8f, 7.8f, 0.0f, false, false, 4.1f, -1.2f)
                arcToRelative(7.9f, 7.9f, 0.0f, false, false, 3.9f, -6.8f)
                lineTo(152.0f, 164.0f)
                arcTo(7.9f, 7.9f, 0.0f, false, false, 148.2f, 157.2f)
                close()
                moveTo(104.0f, 208.0f)
                lineTo(48.0f, 208.0f)
                lineTo(48.0f, 168.0f)
                horizontalLineToRelative(56.0f)
                verticalLineToRelative(40.0f)
                close()
                moveTo(136.0f, 199.8f)
                lineTo(120.0f, 191.2f)
                verticalLineToRelative(-6.3f)
                lineToRelative(16.0f, -8.0f)
                close()
            }
        }
            .build()
        return _file_video!!
    }

private var _file_video: ImageVector? = null



@Preview
@Composable
fun FileVideoPreview() {
    Image(
        Phosphor.FileVideo,
        null
    )
}
