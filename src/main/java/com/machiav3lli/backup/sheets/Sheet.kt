package com.machiav3lli.backup.sheets

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Sheet(
    onDismissRequest: () -> Unit,
    //modifier: Modifier,
    sheetState: SheetState,
    //shape: Shape,
    //containerColor: Color,
    //contentColor: Color,
    //tonalElevation: Dp,
    //scrimColor: Color,
    //dragHandle: @Composable() (() -> Unit)?,
    //windowInsets: WindowInsets,
    content: @Composable() (ColumnScope.() -> Unit),
) {
    ModalBottomSheet(
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
        scrimColor = Color.Transparent,
        dragHandle = null,
        onDismissRequest = onDismissRequest
    ) {
        // bottom sheets even recomposit when hidden
        // which is bad when they contain live content
        if (sheetState.currentValue != SheetValue.Hidden) {
            content()
        } else {
            // inexpensive placeholder while hidden
            // necessary becasue empty sheets are kept hidden
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}
