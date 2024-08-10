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

val Phosphor.HouseSimple: ImageVector
    get() {
        if (_house_simple != null) {
            return _house_simple!!
        }
        _house_simple = Builder(
            name = "House-simple",
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
                moveTo(208.0f, 224.0f)
                horizontalLineTo(48.0f)
                arcToRelative(16.0f, 16.0f, 0.0f, false, true, -16.0f, -16.0f)
                verticalLineTo(115.5f)
                arcToRelative(16.0f, 16.0f, 0.0f, false, true, 5.2f, -11.8f)
                lineToRelative(80.0f, -72.7f)
                arcToRelative(16.0f, 16.0f, 0.0f, false, true, 21.6f, 0.0f)
                lineToRelative(80.0f, 72.7f)
                horizontalLineToRelative(0.0f)
                arcToRelative(16.0f, 16.0f, 0.0f, false, true, 5.2f, 11.8f)
                verticalLineTo(208.0f)
                arcTo(16.0f, 16.0f, 0.0f, false, true, 208.0f, 224.0f)
                close()
                moveTo(128.0f, 42.8f)
                lineTo(48.0f, 115.5f)
                verticalLineTo(208.0f)
                horizontalLineTo(208.0f)
                verticalLineTo(115.5f)
                close()
            }
        }
            .build()
        return _house_simple!!
    }

private var _house_simple: ImageVector? = null



@Preview
@Composable
fun HouseSimplePreview() {
    Image(
        Phosphor.HouseSimple,
        null
    )
}
