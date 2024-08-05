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

val Phosphor.LockLaminatedOpen: ImageVector
    get() {
        if (_lock_laminated_open != null) {
            return _lock_laminated_open!!
        }
        _lock_laminated_open = Builder(
            name = "Lock-laminated-open",
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
                moveTo(208.0f, 80.0f)
                lineTo(100.0f, 80.0f)
                lineTo(100.0f, 52.0f)
                arcToRelative(28.0f, 28.0f, 0.0f, false, true, 56.0f, 0.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, false, 16.0f, 0.0f)
                arcToRelative(44.0f, 44.0f, 0.0f, false, false, -88.0f, 0.0f)
                lineTo(84.0f, 80.0f)
                lineTo(48.0f, 80.0f)
                arcTo(16.0f, 16.0f, 0.0f, false, false, 32.0f, 96.0f)
                lineTo(32.0f, 208.0f)
                arcToRelative(16.0f, 16.0f, 0.0f, false, false, 16.0f, 16.0f)
                lineTo(208.0f, 224.0f)
                arcToRelative(16.0f, 16.0f, 0.0f, false, false, 16.0f, -16.0f)
                lineTo(224.0f, 96.0f)
                arcTo(16.0f, 16.0f, 0.0f, false, false, 208.0f, 80.0f)
                close()
                moveTo(48.0f, 128.0f)
                lineTo(208.0f, 128.0f)
                verticalLineToRelative(16.0f)
                lineTo(48.0f, 144.0f)
                close()
                moveTo(48.0f, 160.0f)
                lineTo(208.0f, 160.0f)
                verticalLineToRelative(16.0f)
                lineTo(48.0f, 176.0f)
                close()
                moveTo(208.0f, 96.0f)
                verticalLineToRelative(16.0f)
                lineTo(48.0f, 112.0f)
                lineTo(48.0f, 96.0f)
                close()
                moveTo(208.0f, 208.0f)
                lineTo(48.0f, 208.0f)
                lineTo(48.0f, 192.0f)
                lineTo(208.0f, 192.0f)
                verticalLineToRelative(16.0f)
                close()
            }
        }
            .build()
        return _lock_laminated_open!!
    }

private var _lock_laminated_open: ImageVector? = null



@Preview
@Composable
fun LockLaminatedOpenPreview() {
    Image(
        Phosphor.LockLaminatedOpen,
        null
    )
}
