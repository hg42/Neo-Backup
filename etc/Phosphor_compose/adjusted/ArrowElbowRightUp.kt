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

val Phosphor.ArrowElbowRightUp: ImageVector
    get() {
        if (_arrow_elbow_right_up != null) {
            return _arrow_elbow_right_up!!
        }
        _arrow_elbow_right_up = Builder(
            name = "Arrow-elbow-right-up",
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
                moveTo(229.7f, 101.7f)
                arcToRelative(8.2f, 8.2f, 0.0f, false, true, -11.4f, 0.0f)
                lineTo(184.0f, 67.3f)
                verticalLineTo(192.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, true, -8.0f, 8.0f)
                horizontalLineTo(32.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, true, 0.0f, -16.0f)
                horizontalLineTo(168.0f)
                verticalLineTo(67.3f)
                lineToRelative(-34.3f, 34.4f)
                arcToRelative(8.1f, 8.1f, 0.0f, false, true, -11.4f, -11.4f)
                lineToRelative(48.0f, -48.0f)
                arcToRelative(8.1f, 8.1f, 0.0f, false, true, 11.4f, 0.0f)
                lineToRelative(48.0f, 48.0f)
                arcTo(8.1f, 8.1f, 0.0f, false, true, 229.7f, 101.7f)
                close()
            }
        }
            .build()
        return _arrow_elbow_right_up!!
    }

private var _arrow_elbow_right_up: ImageVector? = null



@Preview
@Composable
fun ArrowElbowRightUpPreview() {
    Image(
        Phosphor.ArrowElbowRightUp,
        null
    )
}
