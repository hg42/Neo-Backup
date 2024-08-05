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

val Phosphor.GitCommit: ImageVector
    get() {
        if (_git_commit != null) {
            return _git_commit!!
        }
        _git_commit = Builder(
            name = "Git-commit",
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
                moveTo(248.0f, 120.0f)
                horizontalLineTo(187.5f)
                arcToRelative(60.0f, 60.0f, 0.0f, false, false, -119.0f, 0.0f)
                horizontalLineTo(8.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, false, 0.0f, 16.0f)
                horizontalLineTo(68.5f)
                arcToRelative(60.0f, 60.0f, 0.0f, false, false, 119.0f, 0.0f)
                horizontalLineTo(248.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, false, 0.0f, -16.0f)
                close()
                moveTo(128.0f, 172.0f)
                arcToRelative(44.0f, 44.0f, 0.0f, true, true, 44.0f, -44.0f)
                arcTo(44.0f, 44.0f, 0.0f, false, true, 128.0f, 172.0f)
                close()
            }
        }
            .build()
        return _git_commit!!
    }

private var _git_commit: ImageVector? = null



@Preview
@Composable
fun GitCommitPreview() {
    Image(
        Phosphor.GitCommit,
        null
    )
}
