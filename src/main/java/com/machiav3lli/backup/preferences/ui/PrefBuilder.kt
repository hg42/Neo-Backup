package com.machiav3lli.backup.preferences.ui

import androidx.compose.runtime.Composable
import com.machiav3lli.backup.ui.item.Pref

@Composable
fun PrefsBuilder(
    pref: Pref,
    onDialogPref: (Pref) -> Unit,
    index: Int,
    size: Int,
) {
    pref.UI?.let { ui ->
        ui(pref, onDialogPref, index, size)
    }
}
