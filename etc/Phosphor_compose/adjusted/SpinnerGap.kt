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

val Phosphor.SpinnerGap: ImageVector
    get() {
        if (_spinner_gap != null) {
            return _spinner_gap!!
        }
        _spinner_gap = Builder(
            name = "Spinner-gap",
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
                moveTo(136.0f, 32.0f)
                lineTo(136.0f, 64.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, true, -16.0f, 0.0f)
                lineTo(120.0f, 32.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, true, 16.0f, 0.0f)
                close()
                moveTo(224.0f, 120.0f)
                lineTo(192.0f, 120.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, false, 0.0f, 16.0f)
                horizontalLineToRelative(32.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, false, 0.0f, -16.0f)
                close()
                moveTo(178.9f, 167.6f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, false, -11.3f, 11.3f)
                lineToRelative(22.6f, 22.6f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, false, 5.7f, 2.4f)
                arcToRelative(7.7f, 7.7f, 0.0f, false, false, 5.6f, -2.4f)
                arcToRelative(7.9f, 7.9f, 0.0f, false, false, 0.0f, -11.3f)
                close()
                moveTo(128.0f, 184.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, false, -8.0f, 8.0f)
                verticalLineToRelative(32.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, false, 16.0f, 0.0f)
                lineTo(136.0f, 192.0f)
                arcTo(8.0f, 8.0f, 0.0f, false, false, 128.0f, 184.0f)
                close()
                moveTo(77.1f, 167.6f)
                lineTo(54.5f, 190.2f)
                arcToRelative(7.9f, 7.9f, 0.0f, false, false, 0.0f, 11.3f)
                arcToRelative(7.7f, 7.7f, 0.0f, false, false, 5.6f, 2.4f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, false, 5.7f, -2.4f)
                lineToRelative(22.6f, -22.6f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, false, -11.3f, -11.3f)
                close()
                moveTo(72.0f, 128.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, false, -8.0f, -8.0f)
                lineTo(32.0f, 120.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, false, 0.0f, 16.0f)
                lineTo(64.0f, 136.0f)
                arcTo(8.0f, 8.0f, 0.0f, false, false, 72.0f, 128.0f)
                close()
                moveTo(65.8f, 54.5f)
                arcTo(8.0f, 8.0f, 0.0f, false, false, 54.5f, 65.8f)
                lineTo(77.1f, 88.4f)
                arcToRelative(8.1f, 8.1f, 0.0f, false, false, 11.3f, 0.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, false, 0.0f, -11.3f)
                close()
            }
        }
            .build()
        return _spinner_gap!!
    }

private var _spinner_gap: ImageVector? = null



@Preview
@Composable
fun SpinnerGapPreview() {
    Image(
        Phosphor.SpinnerGap,
        null
    )
}
