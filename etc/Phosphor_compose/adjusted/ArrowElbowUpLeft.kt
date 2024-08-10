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

val Phosphor.ArrowElbowUpLeft: ImageVector
    get() {
        if (_arrow_elbow_up_left != null) {
            return _arrow_elbow_up_left!!
        }
        _arrow_elbow_up_left = Builder(
            name = "Arrow-elbow-up-left",
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
                moveTo(200.0f, 80.0f)
                verticalLineTo(224.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, true, -16.0f, 0.0f)
                verticalLineTo(88.0f)
                horizontalLineTo(67.3f)
                lineToRelative(34.4f, 34.3f)
                arcToRelative(8.1f, 8.1f, 0.0f, false, true, 0.0f, 11.4f)
                arcToRelative(8.2f, 8.2f, 0.0f, false, true, -11.4f, 0.0f)
                lineToRelative(-48.0f, -48.0f)
                horizontalLineToRelative(0.0f)
                lineToRelative(-0.5f, -0.5f)
                lineToRelative(-0.2f, -0.3f)
                lineToRelative(-0.3f, -0.4f)
                curveToRelative(0.0f, -0.1f, -0.1f, -0.2f, -0.2f, -0.3f)
                lineToRelative(-0.2f, -0.3f)
                curveToRelative(0.0f, -0.2f, -0.1f, -0.3f, -0.1f, -0.4f)
                lineToRelative(-0.2f, -0.3f)
                curveToRelative(0.0f, -0.2f, -0.1f, -0.3f, -0.1f, -0.4f)
                lineToRelative(-0.2f, -0.4f)
                arcToRelative(0.8f, 0.8f, 0.0f, false, false, -0.1f, -0.4f)
                verticalLineToRelative(-0.3f)
                arcToRelative(0.9f, 0.9f, 0.0f, false, true, -0.1f, -0.5f)
                arcToRelative(0.4f, 0.4f, 0.0f, false, false, -0.1f, -0.3f)
                verticalLineTo(79.2f)
                arcToRelative(0.4f, 0.4f, 0.0f, false, false, 0.1f, -0.3f)
                arcToRelative(0.9f, 0.9f, 0.0f, false, true, 0.1f, -0.5f)
                verticalLineToRelative(-0.3f)
                arcToRelative(0.8f, 0.8f, 0.0f, false, false, 0.1f, -0.4f)
                lineToRelative(0.2f, -0.4f)
                curveToRelative(0.0f, -0.1f, 0.1f, -0.2f, 0.1f, -0.4f)
                lineToRelative(0.2f, -0.3f)
                curveToRelative(0.0f, -0.1f, 0.1f, -0.2f, 0.1f, -0.4f)
                lineToRelative(0.2f, -0.3f)
                curveToRelative(0.1f, -0.1f, 0.2f, -0.2f, 0.2f, -0.3f)
                lineToRelative(0.3f, -0.4f)
                lineToRelative(0.2f, -0.3f)
                lineToRelative(0.5f, -0.5f)
                horizontalLineToRelative(0.0f)
                lineToRelative(48.0f, -48.0f)
                arcToRelative(8.1f, 8.1f, 0.0f, true, true, 11.4f, 11.4f)
                lineTo(67.3f, 72.0f)
                horizontalLineTo(192.0f)
                arcTo(8.0f, 8.0f, 0.0f, false, true, 200.0f, 80.0f)
                close()
            }
        }
            .build()
        return _arrow_elbow_up_left!!
    }

private var _arrow_elbow_up_left: ImageVector? = null



@Preview
@Composable
fun ArrowElbowUpLeftPreview() {
    Image(
        Phosphor.ArrowElbowUpLeft,
        null
    )
}
