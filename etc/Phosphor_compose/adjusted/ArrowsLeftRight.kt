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

val Phosphor.ArrowsLeftRight: ImageVector
    get() {
        if (_arrows_left_right != null) {
            return _arrows_left_right!!
        }
        _arrows_left_right = Builder(
            name = "Arrows-left-right",
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
                moveTo(213.7f, 181.7f)
                lineToRelative(-32.0f, 32.0f)
                arcToRelative(8.2f, 8.2f, 0.0f, false, true, -11.4f, 0.0f)
                arcToRelative(8.1f, 8.1f, 0.0f, false, true, 0.0f, -11.4f)
                lineTo(188.7f, 184.0f)
                lineTo(48.0f, 184.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, true, 0.0f, -16.0f)
                lineTo(188.7f, 168.0f)
                lineToRelative(-18.4f, -18.3f)
                arcToRelative(8.1f, 8.1f, 0.0f, false, true, 11.4f, -11.4f)
                lineToRelative(32.0f, 32.0f)
                arcTo(8.1f, 8.1f, 0.0f, false, true, 213.7f, 181.7f)
                close()
                moveTo(74.3f, 117.7f)
                arcToRelative(8.2f, 8.2f, 0.0f, false, false, 11.4f, 0.0f)
                arcToRelative(8.1f, 8.1f, 0.0f, false, false, 0.0f, -11.4f)
                lineTo(67.3f, 88.0f)
                lineTo(208.0f, 88.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, false, 0.0f, -16.0f)
                lineTo(67.3f, 72.0f)
                lineTo(85.7f, 53.7f)
                arcTo(8.1f, 8.1f, 0.0f, false, false, 74.3f, 42.3f)
                lineToRelative(-32.0f, 32.0f)
                arcToRelative(8.1f, 8.1f, 0.0f, false, false, 0.0f, 11.4f)
                close()
            }
        }
            .build()
        return _arrows_left_right!!
    }

private var _arrows_left_right: ImageVector? = null



@Preview
@Composable
fun ArrowsLeftRightPreview() {
    Image(
        Phosphor.ArrowsLeftRight,
        null
    )
}
