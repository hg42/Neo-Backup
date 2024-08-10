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

val Phosphor.CaretCircleDoubleDown: ImageVector
    get() {
        if (_caret_circle_double_down != null) {
            return _caret_circle_double_down!!
        }
        _caret_circle_double_down = Builder(
            name = "Caret-circle-double-down",
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
                moveTo(201.5f, 54.5f)
                arcToRelative(103.8f, 103.8f, 0.0f, false, false, -147.0f, 0.0f)
                arcToRelative(103.8f, 103.8f, 0.0f, false, false, 0.0f, 147.0f)
                arcToRelative(103.8f, 103.8f, 0.0f, false, false, 147.0f, 0.0f)
                arcToRelative(103.8f, 103.8f, 0.0f, false, false, 0.0f, -147.0f)
                close()
                moveTo(190.2f, 190.2f)
                arcToRelative(88.0f, 88.0f, 0.0f, true, true, 0.0f, -124.4f)
                arcTo(88.1f, 88.1f, 0.0f, false, true, 190.2f, 190.2f)
                close()
                moveTo(165.7f, 82.3f)
                arcToRelative(8.1f, 8.1f, 0.0f, false, true, 0.0f, 11.4f)
                lineToRelative(-32.0f, 32.0f)
                arcToRelative(8.2f, 8.2f, 0.0f, false, true, -11.4f, 0.0f)
                lineToRelative(-32.0f, -32.0f)
                arcToRelative(8.1f, 8.1f, 0.0f, false, true, 11.4f, -11.4f)
                lineTo(128.0f, 108.7f)
                lineToRelative(26.3f, -26.4f)
                arcTo(8.1f, 8.1f, 0.0f, false, true, 165.7f, 82.3f)
                close()
                moveTo(165.7f, 138.3f)
                arcToRelative(8.1f, 8.1f, 0.0f, false, true, 0.0f, 11.4f)
                lineToRelative(-32.0f, 32.0f)
                arcToRelative(8.2f, 8.2f, 0.0f, false, true, -11.4f, 0.0f)
                lineToRelative(-32.0f, -32.0f)
                arcToRelative(8.1f, 8.1f, 0.0f, false, true, 11.4f, -11.4f)
                lineTo(128.0f, 164.7f)
                lineToRelative(26.3f, -26.4f)
                arcTo(8.1f, 8.1f, 0.0f, false, true, 165.7f, 138.3f)
                close()
            }
        }
            .build()
        return _caret_circle_double_down!!
    }

private var _caret_circle_double_down: ImageVector? = null



@Preview
@Composable
fun CaretCircleDoubleDownPreview() {
    Image(
        Phosphor.CaretCircleDoubleDown,
        null
    )
}
