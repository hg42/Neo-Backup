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

val Phosphor.FloppyDiskBack: ImageVector
    get() {
        if (_floppy_disk_back != null) {
            return _floppy_disk_back!!
        }
        _floppy_disk_back = Builder(
            name = "Floppy-disk-back",
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
                lineTo(91.3f, 32.0f)
                arcTo(15.9f, 15.9f, 0.0f, false, false, 80.0f, 36.7f)
                lineTo(36.7f, 80.0f)
                arcTo(15.9f, 15.9f, 0.0f, false, false, 32.0f, 91.3f)
                lineTo(32.0f, 208.0f)
                arcToRelative(16.0f, 16.0f, 0.0f, false, false, 16.0f, 16.0f)
                lineTo(208.0f, 224.0f)
                arcToRelative(16.0f, 16.0f, 0.0f, false, false, 16.0f, -16.0f)
                lineTo(224.0f, 48.0f)
                arcTo(16.0f, 16.0f, 0.0f, false, false, 208.0f, 32.0f)
                close()
                moveTo(208.0f, 208.0f)
                lineTo(48.0f, 208.0f)
                lineTo(48.0f, 91.3f)
                lineTo(91.3f, 48.0f)
                lineTo(152.0f, 48.0f)
                lineTo(152.0f, 72.0f)
                lineTo(96.0f, 72.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, false, 0.0f, 16.0f)
                horizontalLineToRelative(64.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, false, 8.0f, -8.0f)
                lineTo(168.0f, 48.0f)
                horizontalLineToRelative(40.0f)
                close()
                moveTo(128.0f, 112.0f)
                arcToRelative(36.0f, 36.0f, 0.0f, true, false, 36.0f, 36.0f)
                arcTo(36.1f, 36.1f, 0.0f, false, false, 128.0f, 112.0f)
                close()
                moveTo(128.0f, 168.0f)
                arcToRelative(20.0f, 20.0f, 0.0f, true, true, 20.0f, -20.0f)
                arcTo(20.1f, 20.1f, 0.0f, false, true, 128.0f, 168.0f)
                close()
            }
        }
            .build()
        return _floppy_disk_back!!
    }

private var _floppy_disk_back: ImageVector? = null



@Preview
@Composable
fun FloppyDiskBackPreview() {
    Image(
        Phosphor.FloppyDiskBack,
        null
    )
}
