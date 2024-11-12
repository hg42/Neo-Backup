package com.machiav3lli.backup.ui.compose.item

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.machiav3lli.backup.OABX
import com.machiav3lli.backup.R
import com.machiav3lli.backup.dialogs.BaseDialog
import com.machiav3lli.backup.preferences.pref_showInfoLogBar
import com.machiav3lli.backup.ui.compose.blockBorderTop
import com.machiav3lli.backup.ui.compose.icons.Phosphor
import com.machiav3lli.backup.ui.compose.icons.phosphor.MagnifyingGlass
import com.machiav3lli.backup.ui.compose.icons.phosphor.X
import com.machiav3lli.backup.ui.compose.ifThenElse
import com.machiav3lli.backup.ui.compose.vertical
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.lang.Float.max

@Composable
fun ProgressIndicator() {
    val busy by remember(OABX.busy.value) { OABX.busy }
    val progress by remember(
        OABX.progress.value.first,
        OABX.progress.value.second
    ) { OABX.progress }

    if (progress.first) {
        LinearProgressIndicator(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp),
            trackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
            color = MaterialTheme.colorScheme.primary,
            progress = { max(0.02f, progress.second) }
        )
    } else if (busy) {
        LinearProgressIndicator(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp),
            trackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
            color = MaterialTheme.colorScheme.primary,
        )
    }
}

@Composable
fun GlobalIndicators() {
    Column(
        verticalArrangement = Arrangement.SpaceEvenly,
        modifier = Modifier
            .fillMaxWidth()
    ) {
        ProgressIndicator()
    }
}

@Composable
fun TitleOrInfoLog(
    title: String,
    showInfo: Boolean,
    tempShowInfo: MutableState<Boolean>,
    modifier: Modifier = Modifier,
) {
    val infoLogText = OABX.getInfoLogText(n = 5, fill = "")
    val scroll = rememberScrollState(0)
    val scope = rememberCoroutineScope()
    val rotation by animateFloatAsState(
        targetValue = if (showInfo) -90f else 0f,
        label = "rotation"
    )

    LaunchedEffect(infoLogText) {
        tempShowInfo.value = true
        scope.launch {
            scroll.scrollTo(scroll.maxValue)
            delay(5000)
            tempShowInfo.value = false
        }
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .wrapContentHeight()
            .fillMaxWidth()
    ) {
        Text(
            text = title,
            style = if (showInfo) MaterialTheme.typography.labelMedium
            else MaterialTheme.typography.headlineSmall,
            modifier = Modifier
                .ifThenElse(
                    boolean = showInfo,
                    modifier = { vertical() },
                    elseModifier = { fillMaxWidth() }
                )
                .rotate(rotation)
                .wrapContentHeight()
        )

        if (showInfo) Text(
            text = infoLogText,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = MaterialTheme.colorScheme.surface,
                    shape = MaterialTheme.shapes.extraSmall
                )
                .padding(horizontal = 4.dp)
        )
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

    Box { // overlay TopBar and indicators
        ListItem(
            modifier = modifier
                .windowInsetsPadding(TopAppBarDefaults.windowInsets)
                .heightIn(min = 72.dp)
                .blockBorderTop()
                .fillMaxWidth(),
            colors = ListItemDefaults.colors(
                containerColor = Color.Transparent,
            ),
            headlineContent = {
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
                    BaseDialog(onDismiss = { showDevTools.value = false }) {
                        DevTools(expanded = showDevTools)
                    }
                }
            },
            trailingContent = {
                Row { actions() }
            }
        )

        // must be second item to overlay first
        GlobalIndicators()

    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun MainTopBar(
    title: String,
    expanded: MutableState<Boolean>,
    query: String,
    onQueryChanged: (String) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
    actions: @Composable() (RowScope.() -> Unit) = {},
) {
    val showDevTools = remember { mutableStateOf(false) }
    val tempShowInfo = remember { mutableStateOf(false) }
    val showInfo =
        !showDevTools.value && (OABX.showInfoLog || tempShowInfo.value) && pref_showInfoLogBar.value
    val (isExpanded, onExpanded) = remember { expanded }
    val enterPositive = expandHorizontally(expandFrom = Alignment.End)
    val exitPositive = shrinkHorizontally(shrinkTowards = Alignment.End)
    val enterNegative = expandHorizontally(expandFrom = Alignment.Start)
    val exitNegative = shrinkHorizontally(shrinkTowards = Alignment.Start)

    Box { // overlay TopBar and indicators
        ListItem(
            modifier = modifier
                .windowInsetsPadding(TopAppBarDefaults.windowInsets)
                .height(72.dp)
                .blockBorderTop()
                .fillMaxWidth(),
            colors = ListItemDefaults.colors(
                containerColor = Color.Transparent,
            ),
            headlineContent = {
                AnimatedVisibility(
                    visible = !isExpanded,
                    enter = enterNegative,
                    exit = exitNegative,
                ) {
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
                }
                AnimatedVisibility(
                    visible = isExpanded,
                    enter = enterPositive,
                    exit = exitPositive,
                ) {
                    ExpandedSearchView(
                        query = query,
                        modifier = modifier,
                        onClose = onClose,
                        onExpanded = onExpanded,
                        onQueryChanged = onQueryChanged
                    )
                }
                if (showDevTools.value) {
                    BaseDialog(onDismiss = { showDevTools.value = false }) {
                        DevTools(expanded = showDevTools)
                    }
                }
            },
            trailingContent = {
                AnimatedVisibility(
                    visible = !isExpanded,
                    enter = enterPositive,
                    exit = exitPositive,
                ) {
                    Row { actions() }
                }
            }
        )

        // must be second item to overlay first
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

    OABX.clearInfoLogText()
    repeat(10) { OABX.addInfoLogText("line $it") }
    OABX.setProgress(count, maxCount)

    LaunchedEffect(true) {
        MainScope().launch {
            while (count < maxCount) {
                OABX.beginBusy()
                OABX.addInfoLogText("count is $count")
                delay(1000)
                count = (count + 1) % (maxCount + 2)
                OABX.endBusy()
                if (count > maxCount)
                    OABX.setProgress()
                OABX.addInfoLogText("count is $count")
                delay(1000)
            }
        }
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
            }
        ) {
            Text("$count")
        }
    }
}

@Preview
@Composable
fun VerticalPreview() {
    Row(
        modifier = Modifier.wrapContentSize()
    ) {
        Text(
            modifier = Modifier
                .vertical()
                .rotate(-90f),
            fontWeight = FontWeight.Bold,
            text = "vertical text"
        )
        Text(text = "horizontal")
    }
}
