package com.machiav3lli.backup.ui.compose.item

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.machiav3lli.backup.OABX
import com.machiav3lli.backup.R
import com.machiav3lli.backup.preferences.pref_showInfoLogBar
import com.machiav3lli.backup.preferences.pref_versionOpacity
import com.machiav3lli.backup.ui.compose.icons.Phosphor
import com.machiav3lli.backup.ui.compose.icons.phosphor.MagnifyingGlass
import com.machiav3lli.backup.ui.compose.icons.phosphor.X
import com.machiav3lli.backup.ui.compose.spToDp
import com.machiav3lli.backup.ui.compose.verticalCCW
import kotlinx.coroutines.delay
import java.lang.Float.max

@Composable
fun ProgressIndicator(height: Dp) {
    val busy by remember(OABX.busy.value) { OABX.busy }
    val progress by remember(
        OABX.progress.value.first,
        OABX.progress.value.second
    ) { OABX.progress }

    if (progress.first) {
        LinearProgressIndicator(
            modifier = Modifier
                .height(height)
                .fillMaxWidth(),
            trackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
            strokeCap = StrokeCap.Round,
            color = MaterialTheme.colorScheme.primary,
            progress = { max(0.02f, progress.second) }
        )
    } else if (busy) {
        LinearProgressIndicator(
            modifier = Modifier
                .height(height)
                .fillMaxWidth(),
            trackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
            strokeCap = StrokeCap.Round,
            color = MaterialTheme.colorScheme.primary,
        )
    }
}

@Composable
fun GlobalIndicators() {
    Box(
        modifier = Modifier
            .wrapContentHeight(),
    ) {
        val fontSize = 8.sp
        val height = spToDp(fontSize)+1.dp

        if (pref_versionOpacity.value > 0)
            Text(
                text = "${OABX.versionName} ${OABX.applicationIssuer}",
                fontSize = fontSize,
                lineHeight = fontSize,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = pref_versionOpacity.value / 100f),
                modifier = Modifier
                    .padding(0.dp)
                    .fillMaxWidth()
                    .height(height)
            )

        ProgressIndicator(height)
    }
}

@Composable
fun TitleOrInfoLog(
    title: String,
    showInfo: Boolean,
    tempShowInfo: MutableState<Boolean>,
    modifier: Modifier = Modifier,
) {
    val numLines = 6
    val fontSize = 9.0.sp
    val height = spToDp(fontSize)*numLines
    val infoLogText = OABX.getInfoLogText(n = 10, fill = "")

    LaunchedEffect(infoLogText) {
        tempShowInfo.value = true
        delay(5000)
        tempShowInfo.value = false
    }

    Box(
        modifier = modifier
            .padding(0.dp)
    ) {
        if (showInfo) {
            Row(
                modifier = Modifier
                    .padding(0.dp)
                    .clipToBounds()
                    .height(height)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall,
                    textAlign = TextAlign.Start,
                    fontSize = 11.0.sp,
                    fontWeight = FontWeight(800),
                    lineHeight = 11.0.sp,
                    modifier = Modifier
                        .padding(bottom = 1.dp)
                        .wrapContentSize(Alignment.BottomStart, unbounded = true)
                        .verticalCCW()
                        .height(spToDp(15.sp))
                        .padding(0.dp)
                )

                Text(
                    text = infoLogText,
                    style = MaterialTheme.typography.bodySmall,
                    //textAlign = TextAlign.Start,
                    fontFamily = FontFamily.Monospace,
                    fontSize = fontSize,
                    lineHeight = fontSize,
                    modifier = Modifier
                        .weight(1f)
                        .height(height)
                        .clipToBounds()
                        .fillMaxHeight()
                        .padding(horizontal = 2.dp)
                        .wrapContentHeight(Alignment.Bottom, unbounded = true)
                )
            }
        } else {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .height(height)
                    .fillMaxWidth()
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier
                        .padding(0.dp)
                        .wrapContentHeight()
                        .fillMaxWidth()
                )
            }
        }

    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun TopBar(
    modifier: Modifier = Modifier,
    title: String,
    actions: @Composable (RowScope.() -> Unit) = {},
) {
    val showDevTools = remember { mutableStateOf(false) }
    val tempShowInfo = remember { mutableStateOf(false) }
    val showInfo =
        !showDevTools.value && (OABX.showInfoLog || tempShowInfo.value) && pref_showInfoLogBar.value

    Column(
        modifier = modifier.padding(0.dp)
    ) {

        TopAppBar(
            modifier = modifier.wrapContentHeight(),
            title = {
                TitleOrInfoLog(
                    title = title,
                    showInfo = showInfo,
                    tempShowInfo = tempShowInfo,
                    modifier = Modifier
                        .combinedClickable(
                            onClick = {
                                if (pref_showInfoLogBar.value) {
                                    OABX.showInfoLog = !OABX.showInfoLog
                                }
                                if (!OABX.showInfoLog)
                                    tempShowInfo.value = false
                            },
                            onLongClick = {
                                showDevTools.value = true
                            }
                        )
                )
                if (showDevTools.value) {
                    Dialog(
                        onDismissRequest = { showDevTools.value = false },
                        properties = DialogProperties(
                            usePlatformDefaultWidth = false,
                            dismissOnBackPress = false,
                            dismissOnClickOutside = false
                        )
                    ) {
                        DevTools(expanded = showDevTools)
                    }
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Transparent,
                titleContentColor = MaterialTheme.colorScheme.onSurface,
                actionIconContentColor = MaterialTheme.colorScheme.onSurface,
                navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
            ),
            actions = actions
        )

        GlobalIndicators()
    }
}

@Composable
fun ExpandableSearchAction(
    query: String,
    modifier: Modifier = Modifier,
    expanded: MutableState<Boolean> = mutableStateOf(false),
    onClose: () -> Unit,
    onQueryChanged: (String) -> Unit,
) {
    val (isExpanded, onExpanded) = remember { expanded }

    HorizontalExpandingVisibility(
        expanded = isExpanded,
        expandedView = {
            ExpandedSearchView(
                query = query,
                modifier = modifier,
                onClose = onClose,
                onExpanded = onExpanded,
                onQueryChanged = onQueryChanged
            )
        },
        collapsedView = {
            RoundButton(
                icon = Phosphor.MagnifyingGlass,
                description = stringResource(id = R.string.search),
                onClick = { onExpanded(true) }
            )
        }
    )
}

@Composable
fun ExpandedSearchView(
    query: String,
    modifier: Modifier = Modifier,
    onClose: () -> Unit,
    onExpanded: (Boolean) -> Unit,
    onQueryChanged: (String) -> Unit,
) {
    val focusManager = LocalFocusManager.current
    val textFieldFocusRequester = remember { FocusRequester() }
    SideEffect { textFieldFocusRequester.requestFocus() }

    var textFieldValue by remember {
        mutableStateOf(TextFieldValue(query, TextRange(query.length)))
    }

    TextField(
        value = textFieldValue,
        onValueChange = {
            textFieldValue = it
            onQueryChanged(it.text)
        },
        modifier = modifier
            .padding(horizontal = 8.dp)
            .fillMaxWidth()
            .focusRequester(textFieldFocusRequester),
        singleLine = true,
        colors = TextFieldDefaults.colors(
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
            focusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
            unfocusedIndicatorColor = Color.Transparent,
            focusedIndicatorColor = Color.Transparent,
        ),
        shape = MaterialTheme.shapes.extraLarge,
        leadingIcon = {
            Icon(
                modifier = Modifier.size(24.dp),
                imageVector = Phosphor.MagnifyingGlass,
                contentDescription = stringResource(id = R.string.search),
            )
        },
        trailingIcon = {
            IconButton(onClick = {
                onExpanded(false)
                textFieldValue = TextFieldValue("")
                onQueryChanged("")
                onClose()
            }) {
                Icon(
                    imageVector = Phosphor.X,
                    contentDescription = stringResource(id = R.string.dialogCancel)
                )
            }
        },
        label = { Text(text = stringResource(id = R.string.searchHint)) },
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
    )
}


@Preview
@Composable
fun ProgressPreview() {

    OABX.fakeContext = LocalContext.current.applicationContext

    var count by remember { mutableStateOf(0) }

    val maxCount = 4

    SideEffect {
        if (count >= 0)
            OABX.setProgress(count, maxCount)
        else if (count == -2)
            OABX.setProgress()
        else
            OABX.hitBusy(2000)
    }


    TopBar(
        title = if (count >= 0)
            "count $count"
        else if (count == -2)
            "off"
        else
            "busy",
        modifier = Modifier.background(color = Color.LightGray)
    ) {
        Button(
            onClick = {
                count = (count + 3) % (maxCount + 3) - 2
                OABX.addInfoLogText("count is $count")
            }
        ) {
            Text("$count")
        }
    }
}

@Preview
@Composable
fun TitleOrInfoLogPreview() {

    OABX.clearInfoLogText()
    //LaunchedEffect(Unit) {
        repeat(20) {
            OABX.addInfoLogText("line $it with many words so it's probably wrapping")
            //delay(1000)
        }
    //}

    Box(
        modifier = Modifier
            .height(80.dp)
            .width(200.dp)
            .background(color = MaterialTheme.colorScheme.surfaceContainer)
    ) {
        TitleOrInfoLog(
            "Title may be longer than space",
            showInfo = true,
            tempShowInfo = remember { mutableStateOf(false) }
        )
    }
}

@Preview
@Composable
fun BottomAlignedTextExample() {
    Box(
        modifier = Modifier
            .height(80.dp) // Fixed height for the Box
            .width(200.dp) // Fixed width for the Box
            .background(color = MaterialTheme.colorScheme.surfaceContainer) // Background color
            .clipToBounds() // Clip content outside the Box
    ) {
        Text(
            text = "line 1\nline 2\nline 3\nline 4\nline 5\nline 6\nline 7\nline 8.",
            modifier = Modifier
                .align(Alignment.BottomStart) // Align text to the bottom-left corner
                .wrapContentHeight(Alignment.Bottom), // Ensure text wraps and aligns to the bottom
            style = TextStyle(fontSize = 14.sp)
        )
    }
}

@Preview
@Composable
fun NestedBoxExample() {
    // Parent Box with fixed size and red border
    Box(
        modifier = Modifier
            //.clipToBounds() // Clip content outside the parent Box
            .padding(5.dp)
            .border(2.dp, Color.Red) // Red border (background for visualization)
            .padding(5.dp)
            .border(1.dp, Color.Red) // Red border (background for visualization)
            .padding(5.dp)
            .height(80.dp) // Fixed height
            .width(200.dp) // Fixed width
            .clipToBounds() // Clip content outside the child Box
    ) {
        // Child Box with green border and larger size
        Box(
            modifier = Modifier
                .wrapContentSize(Alignment.BottomStart, unbounded = true)
                //.align(Alignment.BottomStart) // Align to bottom-left
                .clipToBounds() // Clip content outside the child Box
                .border(2.dp, Color.Green) // Green border (background for visualization)
                .padding(5.dp)
                .border(1.dp, Color.Green) // Green border (background for visualization)
                .padding(1.dp)
                //.height(200.dp) // Larger height than parent
                //.width(50.dp)
        ) {
            Text(
                text = "line 1\nline 2\nline 3\nline 4\nline 5\nline 6\nline 7\nline 8."
            )
        }
    }
}