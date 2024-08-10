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

val Phosphor.SpeakerSimpleSlash: ImageVector
    get() {
        if (_speaker_simple_slash != null) {
            return _speaker_simple_slash!!
        }
        _speaker_simple_slash = Builder(
            name = "Speaker-simple-slash",
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
                moveTo(184.0f, 152.0f)
                lineTo(184.0f, 104.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, true, 16.0f, 0.0f)
                verticalLineToRelative(48.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, true, -16.0f, 0.0f)
                close()
                moveTo(224.0f, 80.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, false, -8.0f, 8.0f)
                verticalLineToRelative(80.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, false, 16.0f, 0.0f)
                lineTo(232.0f, 88.0f)
                arcTo(8.0f, 8.0f, 0.0f, false, false, 224.0f, 80.0f)
                close()
                moveTo(213.9f, 210.6f)
                arcToRelative(7.9f, 7.9f, 0.0f, false, true, -0.5f, 11.3f)
                arcTo(8.2f, 8.2f, 0.0f, false, true, 208.0f, 224.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, true, -5.9f, -2.6f)
                lineTo(160.0f, 175.1f)
                lineTo(160.0f, 224.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, true, -4.5f, 7.2f)
                arcToRelative(8.5f, 8.5f, 0.0f, false, true, -3.5f, 0.8f)
                arcToRelative(8.1f, 8.1f, 0.0f, false, true, -4.9f, -1.7f)
                lineTo(77.3f, 176.0f)
                lineTo(32.0f, 176.0f)
                arcToRelative(16.0f, 16.0f, 0.0f, false, true, -16.0f, -16.0f)
                lineTo(16.0f, 96.0f)
                arcTo(16.0f, 16.0f, 0.0f, false, true, 32.0f, 80.0f)
                lineTo(73.6f, 80.0f)
                lineTo(42.1f, 45.4f)
                arcTo(8.0f, 8.0f, 0.0f, false, true, 53.9f, 34.6f)
                close()
                moveTo(144.0f, 157.5f)
                lineTo(88.1f, 96.0f)
                lineTo(32.0f, 96.0f)
                verticalLineToRelative(64.0f)
                lineTo(80.0f, 160.0f)
                arcToRelative(7.9f, 7.9f, 0.0f, false, true, 4.9f, 1.7f)
                lineTo(144.0f, 207.6f)
                close()
                moveTo(117.1f, 69.3f)
                lineTo(144.0f, 48.4f)
                verticalLineToRelative(58.4f)
                arcToRelative(8.0f, 8.0f, 0.0f, true, false, 16.0f, 0.0f)
                lineTo(160.0f, 32.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, false, -12.9f, -6.3f)
                lineToRelative(-39.9f, 31.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, true, false, 9.9f, 12.6f)
                close()
            }
        }
            .build()
        return _speaker_simple_slash!!
    }

private var _speaker_simple_slash: ImageVector? = null



@Preview
@Composable
fun SpeakerSimpleSlashPreview() {
    Image(
        Phosphor.SpeakerSimpleSlash,
        null
    )
}
