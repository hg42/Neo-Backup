package com.machiav3lli.backup.ui.compose.item

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.absolutePadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SmallTopAppBar
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.machiav3lli.backup.OABX
import com.machiav3lli.backup.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Preview
@Composable
fun DefaultPreview() {
    var expanded by remember { mutableStateOf(false) }
    var count by remember { mutableStateOf(0) }
    val scope = rememberCoroutineScope()

    OABX.addInfoText("xxxxxxxxxxxxxxxxx")
    OABX.clearInfoText()

    TopBar(title = "Progress", modifier = Modifier.background(color = Color.LightGray)) {
        Button(
            onClick = {
                count++
                OABX.addInfoText("test $count")
            }
        ) {
            Text("$count")
        }
    }
}

fun Modifier.vertical() = layout { measurable, constraints ->
    val placeable = measurable.measure(constraints)
    layout(placeable.height, placeable.width) {
        placeable.place(
            x = -(placeable.width / 2 - placeable.height / 2),
            y = -(placeable.height / 2 - placeable.width / 2)
        )
    }
}

@Preview
@Composable
fun TestPreview() {
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

@Composable
fun TopBar(
    modifier: Modifier = Modifier,
    title: String,
    actions: @Composable (RowScope.() -> Unit)
) {
    var tempShow by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val infoText = OABX.getInfoText(n = 5, fill = "")
    var lastInfoText by remember { mutableStateOf(infoText) }
    val changed = (infoText != lastInfoText)

    if(changed) {
        lastInfoText = infoText
        tempShow = true
        scope.launch {
            delay(5000)
            tempShow = false
        }
    }

    SmallTopAppBar(
        modifier = modifier.wrapContentHeight(),
        title = {
            if (OABX.showInfo || tempShow) {
                Row(
                    verticalAlignment = Alignment.Bottom,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(color = MaterialTheme.colorScheme.background)
                        .clickable {
                            OABX.showInfo = !OABX.showInfo
                            if (!OABX.showInfo)
                                tempShow = false
                        }
                ) {
                    Text(
                        text = buildAnnotatedString {
                            append(title)
                        },
                        style = MaterialTheme.typography.headlineMedium,
                        textAlign = TextAlign.Start,
                        fontSize = 11.0.sp,
                        fontWeight = FontWeight(800),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .absolutePadding(right = 4.dp, bottom = 4.dp)
                            .vertical()
                            .rotate(-90f)
                    )
                    Text(
                        text = infoText,
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 11.0.sp,
                        lineHeight = 11.0.sp,
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                }
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable {
                            OABX.showInfo = !OABX.showInfo
                        }
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.headlineMedium,
                    )
                }
            }
        },
        colors = TopAppBarDefaults.smallTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            actionIconContentColor = MaterialTheme.colorScheme.onSurface,
            navigationIconContentColor = MaterialTheme.colorScheme.onSurface
        ),
        actions = actions
    )
}

@Composable
fun ExpandableSearchAction(
    query: String,
    modifier: Modifier = Modifier,
    expanded: Boolean = false,
    onClose: () -> Unit,
    onQueryChanged: (String) -> Unit
) {
    val (expanded, onExpanded) = remember {
        mutableStateOf(expanded)
    }

    HorizontalExpandingVisibility(
        expanded = expanded,
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
            Row {
                RoundButton(icon = painterResource(id = R.drawable.ic_refresh)) {
                    OABX.main?.needRefresh = true
                }
                CollapsedSearchView(
                    modifier = modifier,
                    onExpanded = onExpanded
                )
            }
        }
    )
}

@Composable
fun CollapsedSearchView(
    modifier: Modifier = Modifier,
    onExpanded: (Boolean) -> Unit
) {
    TopBarButton(
        icon = painterResource(id = R.drawable.ic_search),
        description = stringResource(id = R.string.search),
        onClick = { onExpanded(true) }
    )
}

@Composable
fun ExpandedSearchView(
    query: String,
    modifier: Modifier = Modifier,
    onClose: () -> Unit,
    onExpanded: (Boolean) -> Unit,
    onQueryChanged: (String) -> Unit
) {
    val focusManager = LocalFocusManager.current
    val textFieldFocusRequester = remember { FocusRequester() }
    SideEffect { textFieldFocusRequester.requestFocus() }

    var textFieldValue by remember {
        mutableStateOf(TextFieldValue(query, TextRange(query.length)))
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = Color.Transparent,
                shape = MaterialTheme.shapes.medium
            )
            .border(
                BorderStroke(2.dp, MaterialTheme.colorScheme.primary),
                shape = MaterialTheme.shapes.medium
            ),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        TextField(
            value = textFieldValue,
            onValueChange = {
                textFieldValue = it
                onQueryChanged(it.text)
            },
            modifier = Modifier
                .weight(1f)
                .focusRequester(textFieldFocusRequester),
            colors = TextFieldDefaults.textFieldColors(
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
            ),
            leadingIcon = {
                Icon(
                    painter = painterResource(id = R.drawable.ic_search),
                    contentDescription = stringResource(id = R.string.search),
                )
            },
            singleLine = true,
            label = { Text(text = stringResource(id = R.string.searchHint)) },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
        )
        IconButton(onClick = {
            onExpanded(false)
            onClose()
        }) {
            Icon(
                painter = painterResource(id = R.drawable.ic_close),
                contentDescription = stringResource(id = R.string.dialogCancel)
            )
        }
    }
}