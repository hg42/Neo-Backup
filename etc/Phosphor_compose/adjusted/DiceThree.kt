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

val Phosphor.DiceThree: ImageVector
    get() {
        if (_dice_three != null) {
            return _dice_three!!
        }
        _dice_three = Builder(
            name = "Dice-three",
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
                moveTo(192.0f, 32.0f)
                lineTo(64.0f, 32.0f)
                arcTo(32.1f, 32.1f, 0.0f, false, false, 32.0f, 64.0f)
                lineTo(32.0f, 192.0f)
                arcToRelative(32.1f, 32.1f, 0.0f, false, false, 32.0f, 32.0f)
                lineTo(192.0f, 224.0f)
                arcToRelative(32.1f, 32.1f, 0.0f, false, false, 32.0f, -32.0f)
                lineTo(224.0f, 64.0f)
                arcTo(32.1f, 32.1f, 0.0f, false, false, 192.0f, 32.0f)
                close()
                moveTo(208.0f, 192.0f)
                arcToRelative(16.0f, 16.0f, 0.0f, false, true, -16.0f, 16.0f)
                lineTo(64.0f, 208.0f)
                arcToRelative(16.0f, 16.0f, 0.0f, false, true, -16.0f, -16.0f)
                lineTo(48.0f, 64.0f)
                arcTo(16.0f, 16.0f, 0.0f, false, true, 64.0f, 48.0f)
                lineTo(192.0f, 48.0f)
                arcToRelative(16.0f, 16.0f, 0.0f, false, true, 16.0f, 16.0f)
                close()
                moveTo(104.0f, 92.0f)
                arcTo(12.0f, 12.0f, 0.0f, true, true, 92.0f, 80.0f)
                arcTo(12.0f, 12.0f, 0.0f, false, true, 104.0f, 92.0f)
                close()
                moveTo(140.0f, 128.0f)
                arcToRelative(12.0f, 12.0f, 0.0f, true, true, -12.0f, -12.0f)
                arcTo(12.0f, 12.0f, 0.0f, false, true, 140.0f, 128.0f)
                close()
                moveTo(176.0f, 164.0f)
                arcToRelative(12.0f, 12.0f, 0.0f, true, true, -12.0f, -12.0f)
                arcTo(12.0f, 12.0f, 0.0f, false, true, 176.0f, 164.0f)
                close()
            }
        }
            .build()
        return _dice_three!!
    }

private var _dice_three: ImageVector? = null



@Preview
@Composable
fun DiceThreePreview() {
    Image(
        Phosphor.DiceThree,
        null
    )
}
