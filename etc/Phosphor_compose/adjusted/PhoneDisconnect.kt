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

val Phosphor.PhoneDisconnect: ImageVector
    get() {
        if (_phone_disconnect != null) {
            return _phone_disconnect!!
        }
        _phone_disconnect = Builder(
            name = "Phone-disconnect",
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
                moveTo(229.8f, 90.2f)
                curveTo(173.7f, 34.0f, 82.3f, 34.0f, 26.2f, 90.2f)
                arcToRelative(56.1f, 56.1f, 0.0f, false, false, -4.7f, 73.9f)
                arcToRelative(16.2f, 16.2f, 0.0f, false, false, 12.6f, 6.1f)
                arcToRelative(17.1f, 17.1f, 0.0f, false, false, 5.9f, -1.1f)
                lineToRelative(47.4f, -19.0f)
                arcToRelative(16.0f, 16.0f, 0.0f, false, false, 9.7f, -11.7f)
                lineToRelative(5.9f, -29.5f)
                arcToRelative(76.3f, 76.3f, 0.0f, false, true, 49.7f, -0.1f)
                horizontalLineToRelative(0.0f)
                lineToRelative(6.2f, 29.7f)
                arcToRelative(15.9f, 15.9f, 0.0f, false, false, 9.7f, 11.6f)
                lineToRelative(47.4f, 19.0f)
                arcToRelative(16.1f, 16.1f, 0.0f, false, false, 18.5f, -5.0f)
                arcTo(56.1f, 56.1f, 0.0f, false, false, 229.8f, 90.2f)
                close()
                moveTo(221.9f, 154.2f)
                lineTo(174.6f, 135.3f)
                lineTo(168.3f, 105.5f)
                arcTo(15.8f, 15.8f, 0.0f, false, false, 158.0f, 93.7f)
                arcToRelative(92.6f, 92.6f, 0.0f, false, false, -60.4f, 0.1f)
                arcToRelative(16.1f, 16.1f, 0.0f, false, false, -10.3f, 12.0f)
                lineToRelative(-5.9f, 29.5f)
                lineTo(34.1f, 154.2f)
                arcToRelative(40.0f, 40.0f, 0.0f, false, true, 3.4f, -52.7f)
                arcToRelative(128.2f, 128.2f, 0.0f, false, true, 181.0f, 0.0f)
                arcTo(40.0f, 40.0f, 0.0f, false, true, 221.9f, 154.2f)
                close()
                moveTo(224.0f, 200.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, true, -8.0f, 8.0f)
                lineTo(40.0f, 208.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, true, 0.0f, -16.0f)
                lineTo(216.0f, 192.0f)
                arcTo(8.0f, 8.0f, 0.0f, false, true, 224.0f, 200.0f)
                close()
            }
        }
            .build()
        return _phone_disconnect!!
    }

private var _phone_disconnect: ImageVector? = null



@Preview
@Composable
fun PhoneDisconnectPreview() {
    Image(
        Phosphor.PhoneDisconnect,
        null
    )
}
