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

val Phosphor.Voicemail: ImageVector
    get() {
        if (_voicemail != null) {
            return _voicemail!!
        }
        _voicemail = Builder(
            name = "Voicemail",
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
                moveTo(200.0f, 72.0f)
                arcToRelative(55.9f, 55.9f, 0.0f, false, false, -39.1f, 96.0f)
                lineTo(95.1f, 168.0f)
                arcTo(55.9f, 55.9f, 0.0f, true, false, 56.0f, 184.0f)
                lineTo(200.0f, 184.0f)
                arcToRelative(56.0f, 56.0f, 0.0f, false, false, 0.0f, -112.0f)
                close()
                moveTo(16.0f, 128.0f)
                arcToRelative(40.0f, 40.0f, 0.0f, true, true, 40.0f, 40.0f)
                arcTo(40.0f, 40.0f, 0.0f, false, true, 16.0f, 128.0f)
                close()
                moveTo(200.0f, 168.0f)
                arcToRelative(40.0f, 40.0f, 0.0f, true, true, 40.0f, -40.0f)
                arcTo(40.0f, 40.0f, 0.0f, false, true, 200.0f, 168.0f)
                close()
            }
        }
            .build()
        return _voicemail!!
    }

private var _voicemail: ImageVector? = null



@Preview
@Composable
fun VoicemailPreview() {
    Image(
        Phosphor.Voicemail,
        null
    )
}
