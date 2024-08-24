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

val Phosphor.BookmarkSimple: ImageVector
    get() {
        if (_bookmark_simple != null) {
            return _bookmark_simple!!
        }
        _bookmark_simple = Builder(
            name = "Bookmark-simple",
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
                moveTo(184.0f, 32.0f)
                lineTo(72.0f, 32.0f)
                arcTo(16.0f, 16.0f, 0.0f, false, false, 56.0f, 48.0f)
                lineTo(56.0f, 224.0f)
                arcToRelative(8.1f, 8.1f, 0.0f, false, false, 4.1f, 7.0f)
                arcToRelative(7.8f, 7.8f, 0.0f, false, false, 3.9f, 1.0f)
                arcToRelative(7.6f, 7.6f, 0.0f, false, false, 4.2f, -1.2f)
                lineTo(128.0f, 193.4f)
                lineToRelative(59.8f, 37.4f)
                arcTo(8.0f, 8.0f, 0.0f, false, false, 200.0f, 224.0f)
                lineTo(200.0f, 48.0f)
                arcTo(16.0f, 16.0f, 0.0f, false, false, 184.0f, 32.0f)
                close()
                moveTo(184.0f, 209.6f)
                lineTo(132.2f, 177.2f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, false, -8.4f, 0.0f)
                lineTo(72.0f, 209.6f)
                lineTo(72.0f, 48.0f)
                lineTo(184.0f, 48.0f)
                close()
            }
        }
            .build()
        return _bookmark_simple!!
    }

private var _bookmark_simple: ImageVector? = null



@Preview
@Composable
fun BookmarkSimplePreview() {
    Image(
        Phosphor.BookmarkSimple,
        null
    )
}
