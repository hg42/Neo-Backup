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

val Phosphor.NumberSquareOne: ImageVector
    get() {
        if (_number_square_one != null) {
            return _number_square_one!!
        }
        _number_square_one = Builder(
            name = "Number-square-one",
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
                moveTo(140.0f, 84.0f)
                verticalLineToRelative(92.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, true, -16.0f, 0.0f)
                lineTo(124.0f, 98.9f)
                lineToRelative(-11.6f, 7.8f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, true, -8.8f, -13.4f)
                lineToRelative(24.0f, -16.0f)
                arcToRelative(8.3f, 8.3f, 0.0f, false, true, 8.2f, -0.4f)
                arcTo(8.0f, 8.0f, 0.0f, false, true, 140.0f, 84.0f)
                close()
                moveTo(224.0f, 48.0f)
                lineTo(224.0f, 208.0f)
                arcToRelative(16.0f, 16.0f, 0.0f, false, true, -16.0f, 16.0f)
                lineTo(48.0f, 224.0f)
                arcToRelative(16.0f, 16.0f, 0.0f, false, true, -16.0f, -16.0f)
                lineTo(32.0f, 48.0f)
                arcTo(16.0f, 16.0f, 0.0f, false, true, 48.0f, 32.0f)
                lineTo(208.0f, 32.0f)
                arcTo(16.0f, 16.0f, 0.0f, false, true, 224.0f, 48.0f)
                close()
                moveTo(208.0f, 208.0f)
                lineTo(208.0f, 48.0f)
                lineTo(48.0f, 48.0f)
                lineTo(48.0f, 208.0f)
                close()
            }
        }
            .build()
        return _number_square_one!!
    }

private var _number_square_one: ImageVector? = null



@Preview
@Composable
fun NumberSquareOnePreview() {
    Image(
        Phosphor.NumberSquareOne,
        null
    )
}
