package com.machiav3lli.backup.preferences.ui

import androidx.compose.runtime.Composable
import com.machiav3lli.backup.ui.item.Pref

@Composable
fun PrefsBuilder(
    pref: Pref,
    onDialogPref: (Pref) -> Unit
) {
    pref.UI?.let { ui ->
        ui(pref, onDialogPref)
    }
}
