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

val Phosphor.PuzzlePiece: ImageVector
    get() {
        if (_puzzle_piece != null) {
            return _puzzle_piece!!
        }
        _puzzle_piece = Builder(
            name = "Puzzle-piece",
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
                moveTo(220.3f, 158.5f)
                arcToRelative(8.1f, 8.1f, 0.0f, false, false, -7.7f, -0.4f)
                arcToRelative(20.2f, 20.2f, 0.0f, false, true, -23.2f, -4.4f)
                arcToRelative(20.0f, 20.0f, 0.0f, false, true, 13.1f, -33.6f)
                arcToRelative(19.6f, 19.6f, 0.0f, false, true, 10.1f, 1.8f)
                arcToRelative(8.1f, 8.1f, 0.0f, false, false, 7.7f, -0.4f)
                arcToRelative(8.2f, 8.2f, 0.0f, false, false, 3.7f, -6.8f)
                verticalLineTo(72.0f)
                arcToRelative(16.0f, 16.0f, 0.0f, false, false, -16.0f, -16.0f)
                horizontalLineTo(171.8f)
                curveToRelative(0.1f, -1.3f, 0.2f, -2.7f, 0.2f, -4.0f)
                arcToRelative(35.6f, 35.6f, 0.0f, false, false, -11.4f, -26.2f)
                arcToRelative(35.3f, 35.3f, 0.0f, false, false, -26.9f, -9.7f)
                arcToRelative(36.0f, 36.0f, 0.0f, false, false, -33.6f, 33.3f)
                arcToRelative(36.4f, 36.4f, 0.0f, false, false, 0.1f, 6.6f)
                horizontalLineTo(64.0f)
                arcTo(16.0f, 16.0f, 0.0f, false, false, 48.0f, 72.0f)
                verticalLineToRelative(32.2f)
                lineToRelative(-4.0f, -0.2f)
                arcToRelative(35.6f, 35.6f, 0.0f, false, false, -26.2f, 11.4f)
                arcToRelative(35.3f, 35.3f, 0.0f, false, false, -9.7f, 26.9f)
                arcToRelative(36.0f, 36.0f, 0.0f, false, false, 33.3f, 33.6f)
                arcToRelative(36.4f, 36.4f, 0.0f, false, false, 6.6f, -0.1f)
                verticalLineTo(208.0f)
                arcToRelative(16.0f, 16.0f, 0.0f, false, false, 16.0f, 16.0f)
                horizontalLineTo(208.0f)
                arcToRelative(16.0f, 16.0f, 0.0f, false, false, 16.0f, -16.0f)
                verticalLineTo(165.3f)
                arcTo(8.2f, 8.2f, 0.0f, false, false, 220.3f, 158.5f)
                close()
                moveTo(208.0f, 208.0f)
                horizontalLineTo(64.0f)
                verticalLineTo(165.3f)
                arcToRelative(8.2f, 8.2f, 0.0f, false, false, -3.7f, -6.8f)
                arcToRelative(8.1f, 8.1f, 0.0f, false, false, -7.7f, -0.4f)
                arcToRelative(19.6f, 19.6f, 0.0f, false, true, -10.1f, 1.8f)
                arcToRelative(20.0f, 20.0f, 0.0f, false, true, -13.1f, -33.6f)
                arcToRelative(20.2f, 20.2f, 0.0f, false, true, 23.2f, -4.4f)
                arcToRelative(8.1f, 8.1f, 0.0f, false, false, 7.7f, -0.4f)
                arcToRelative(8.2f, 8.2f, 0.0f, false, false, 3.7f, -6.8f)
                verticalLineTo(72.0f)
                horizontalLineToRelative(46.7f)
                arcToRelative(8.2f, 8.2f, 0.0f, false, false, 6.8f, -3.7f)
                arcToRelative(8.1f, 8.1f, 0.0f, false, false, 0.4f, -7.7f)
                arcToRelative(19.6f, 19.6f, 0.0f, false, true, -1.8f, -10.1f)
                arcToRelative(20.0f, 20.0f, 0.0f, false, true, 33.6f, -13.1f)
                arcToRelative(20.2f, 20.2f, 0.0f, false, true, 4.4f, 23.2f)
                arcToRelative(8.1f, 8.1f, 0.0f, false, false, 0.4f, 7.7f)
                arcToRelative(8.2f, 8.2f, 0.0f, false, false, 6.8f, 3.7f)
                horizontalLineTo(208.0f)
                verticalLineToRelative(32.2f)
                arcToRelative(36.4f, 36.4f, 0.0f, false, false, -6.6f, -0.1f)
                arcToRelative(36.0f, 36.0f, 0.0f, false, false, -33.3f, 33.6f)
                arcTo(36.1f, 36.1f, 0.0f, false, false, 204.0f, 176.0f)
                lineToRelative(4.0f, -0.2f)
                close()
            }
        }
            .build()
        return _puzzle_piece!!
    }

private var _puzzle_piece: ImageVector? = null



@Preview
@Composable
fun PuzzlePiecePreview() {
    Image(
        Phosphor.PuzzlePiece,
        null
    )
}
