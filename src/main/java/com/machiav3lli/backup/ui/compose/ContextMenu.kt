package com.machiav3lli.backup.ui.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.machiav3lli.backup.preferences.pref_fixNavBarOverlap
import com.machiav3lli.backup.ui.compose.icons.Phosphor
import com.machiav3lli.backup.ui.compose.icons.phosphor.Check
import com.machiav3lli.backup.ui.compose.icons.phosphor.X
import com.machiav3lli.backup.utils.SystemUtils
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.delay
import java.util.concurrent.Executors

val yesNo = listOf(
    "yes" to "no",
    "really!" to "oh no!",
    "yeah" to "forget it"
)

@Composable
fun Confirmation(
    expanded: MutableState<Boolean>,
    text: String = "Are you sure?",
    onAction: () -> Unit = {},
) {
    val (yes, no) = yesNo.random()
    DropdownMenuItem(
        leadingIcon = { Icon(Phosphor.Check, null, tint = Color.Green) },
        text = { Text(yes) },
        onClick = {
            expanded.value = false
            onAction()
        }
    )
    DropdownMenuItem(
        leadingIcon = { Icon(Phosphor.X, null, tint = Color.Red) },
        text = { Text(no) },
        onClick = {
            expanded.value = false
        }
    )
}

@Composable
fun TextInputMenuItem(
    text: String = "",
    placeholder: String = "",
    trailingIcon: ImageVector? = null,
    onAction: (String) -> Unit = {},
) {
    val input = remember { mutableStateOf(text) }
    val focusManager = LocalFocusManager.current
    val textFieldFocusRequester = remember { FocusRequester() }

    LaunchedEffect(textFieldFocusRequester) {
        delay(100)
        textFieldFocusRequester.requestFocus()
    }

    fun submit() {
        focusManager.clearFocus()
        onAction(input.value)
    }

    DropdownMenuItem(
        text = {
            OutlinedTextField(
                modifier = Modifier
                    .testTag("input")
                    .focusRequester(textFieldFocusRequester),
                value = input.value,
                placeholder = { Text(text = placeholder, color = Color.Gray) },
                singleLine = true,
                trailingIcon = {
                    trailingIcon?.let { icon ->
                        IconButton(onClick = { submit() }) {
                            Icon(icon, null)
                        }
                    }
                },
                keyboardActions = KeyboardActions(
                    onDone = {
                        submit()
                    }
                ),
                keyboardOptions = KeyboardOptions(
                    autoCorrect = false
                ),
                onValueChange = {
                    if (it.contains("\n")) {
                        input.value = it.replace("\n", "")
                        submit()
                    } else
                        input.value = it
                }
            )
        },
        onClick = {}
    )
}

fun openSubMenu(
    subMenu: MutableState<(@Composable () -> Unit)?>,
    content: @Composable () -> Unit,
) {
    subMenu.value = {
        DropdownMenu(
            expanded = true,
            offset = DpOffset(100.dp, (-1000).dp),
            modifier = Modifier.background(MaterialTheme.colorScheme.surfaceContainerHighest),
            onDismissRequest = { subMenu.value = null }
        ) {
            if (pref_fixNavBarOverlap.value > 0) {
                Column(
                    modifier = Modifier
                        .padding(bottom = pref_fixNavBarOverlap.value.dp)
                ) {
                    content()
                }
            } else {
                content()
            }
        }
    }
}

fun closeSubMenu(
    subMenu: MutableState<(@Composable () -> Unit)?>,
) {
    subMenu.value = null
}

// menu actions should continue even if the ui is left
val menuScope = MainScope()
val menuPool = Executors.newFixedThreadPool(SystemUtils.numCores).asCoroutineDispatcher()
// Dispatchers.Default  unclear and can do anything in the future
// Dispatchers.IO       creates many threads (~65)

