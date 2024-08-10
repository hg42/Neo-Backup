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

val Phosphor.GenderMale: ImageVector
    get() {
        if (_gender_male != null) {
            return _gender_male!!
        }
        _gender_male = Builder(
            name = "Gender-male",
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
                moveTo(216.0f, 32.0f)
                horizontalLineTo(168.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, false, 0.0f, 16.0f)
                horizontalLineToRelative(28.7f)
                lineTo(154.6f, 90.1f)
                arcToRelative(80.0f, 80.0f, 0.0f, true, false, 11.3f, 11.3f)
                lineTo(208.0f, 59.3f)
                verticalLineTo(88.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, false, 16.0f, 0.0f)
                verticalLineTo(40.0f)
                arcTo(8.0f, 8.0f, 0.0f, false, false, 216.0f, 32.0f)
                close()
                moveTo(149.3f, 197.3f)
                arcToRelative(64.0f, 64.0f, 0.0f, true, true, 0.0f, -90.6f)
                arcTo(64.3f, 64.3f, 0.0f, false, true, 149.3f, 197.3f)
                close()
            }
        }
            .build()
        return _gender_male!!
    }

private var _gender_male: ImageVector? = null



@Preview
@Composable
fun GenderMalePreview() {
    Image(
        Phosphor.GenderMale,
        null
    )
}
