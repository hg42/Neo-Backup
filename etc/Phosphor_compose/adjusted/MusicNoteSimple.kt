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

val Phosphor.MusicNoteSimple: ImageVector
    get() {
        if (_music_note_simple != null) {
            return _music_note_simple!!
        }
        _music_note_simple = Builder(
            name = "Music-note-simple",
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
                moveTo(210.3f, 56.3f)
                lineToRelative(-80.0f, -24.0f)
                arcToRelative(8.2f, 8.2f, 0.0f, false, false, -7.1f, 1.3f)
                arcTo(8.1f, 8.1f, 0.0f, false, false, 120.0f, 40.0f)
                verticalLineTo(148.3f)
                arcTo(47.4f, 47.4f, 0.0f, false, false, 88.0f, 136.0f)
                arcToRelative(48.0f, 48.0f, 0.0f, true, false, 48.0f, 48.0f)
                verticalLineTo(50.8f)
                lineToRelative(69.7f, 20.9f)
                arcToRelative(8.0f, 8.0f, 0.0f, true, false, 4.6f, -15.4f)
                close()
                moveTo(88.0f, 216.0f)
                arcToRelative(32.0f, 32.0f, 0.0f, true, true, 32.0f, -32.0f)
                arcTo(32.1f, 32.1f, 0.0f, false, true, 88.0f, 216.0f)
                close()
            }
        }
            .build()
        return _music_note_simple!!
    }

private var _music_note_simple: ImageVector? = null



@Preview
@Composable
fun MusicNoteSimplePreview() {
    Image(
        Phosphor.MusicNoteSimple,
        null
    )
}
