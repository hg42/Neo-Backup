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

val Phosphor.HandSoap: ImageVector
    get() {
        if (_hand_soap != null) {
            return _hand_soap!!
        }
        _hand_soap = Builder(
            name = "Hand-soap",
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
                moveTo(184.0f, 96.8f)
                lineTo(184.0f, 88.0f)
                arcToRelative(32.1f, 32.1f, 0.0f, false, false, -32.0f, -32.0f)
                lineTo(136.0f, 56.0f)
                lineTo(136.0f, 32.0f)
                horizontalLineToRelative(32.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, true, 8.0f, 8.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, false, 16.0f, 0.0f)
                arcToRelative(24.1f, 24.1f, 0.0f, false, false, -24.0f, -24.0f)
                lineTo(104.0f, 16.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, false, 0.0f, 16.0f)
                horizontalLineToRelative(16.0f)
                lineTo(120.0f, 56.0f)
                lineTo(104.0f, 56.0f)
                arcTo(32.1f, 32.1f, 0.0f, false, false, 72.0f, 88.0f)
                verticalLineToRelative(8.8f)
                arcTo(40.1f, 40.1f, 0.0f, false, false, 40.0f, 136.0f)
                verticalLineToRelative(80.0f)
                arcToRelative(16.0f, 16.0f, 0.0f, false, false, 16.0f, 16.0f)
                lineTo(200.0f, 232.0f)
                arcToRelative(16.0f, 16.0f, 0.0f, false, false, 16.0f, -16.0f)
                lineTo(216.0f, 136.0f)
                arcTo(40.1f, 40.1f, 0.0f, false, false, 184.0f, 96.8f)
                close()
                moveTo(104.0f, 72.0f)
                horizontalLineToRelative(48.0f)
                arcToRelative(16.0f, 16.0f, 0.0f, false, true, 16.0f, 16.0f)
                verticalLineToRelative(8.0f)
                lineTo(88.0f, 96.0f)
                lineTo(88.0f, 88.0f)
                arcTo(16.0f, 16.0f, 0.0f, false, true, 104.0f, 72.0f)
                close()
                moveTo(200.0f, 216.0f)
                lineTo(56.0f, 216.0f)
                lineTo(56.0f, 136.0f)
                arcToRelative(24.1f, 24.1f, 0.0f, false, true, 24.0f, -24.0f)
                horizontalLineToRelative(96.0f)
                arcToRelative(24.1f, 24.1f, 0.0f, false, true, 24.0f, 24.0f)
                verticalLineToRelative(80.0f)
                close()
            }
        }
            .build()
        return _hand_soap!!
    }

private var _hand_soap: ImageVector? = null



@Preview
@Composable
fun HandSoapPreview() {
    Image(
        Phosphor.HandSoap,
        null
    )
}
