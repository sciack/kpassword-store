package passwordStore.services

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import kotlinx.coroutines.launch
import passwordStore.services.audit.Action
import passwordStore.services.audit.Event
import passwordStore.navigation.KPasswordScreen
import passwordStore.ui.theme.INPUT_MEDIUM
import passwordStore.ui.theme.LARGE
import passwordStore.users.LocalUser
import passwordStore.utils.asTitle
import passwordStore.utils.short
import passwordStore.widget.Table
import passwordStore.widget.passwordToolTip
import passwordStore.widget.showOk


@Composable
fun historyTable(historySM: HistorySM) {
    val coroutineScope = rememberCoroutineScope()
    val navController = LocalNavigator.currentOrThrow
    val headers = listOf("Action", "Action Date", "Service", "Username", "Password", "Tags", "Note")

    Column(Modifier.fillMaxSize().padding(LARGE)) {
        Row {
            searchField(historySM)
        }
        Row(modifier = Modifier.fillMaxWidth()) {
            Table(modifier = Modifier.fillMaxSize(),
                headers = headers,
                values = historySM.historyEvents.value,
                cellContent = { columnIndex, event ->
                    displayHistService(event, columnIndex)
                },
                beforeRow = { event ->
                    Row {
                        val color = contentRowModifier(event)
                        if (event.action == Action.delete) {
                            val showAlert = remember {
                                mutableStateOf(false)
                            }
                            IconButton(onClick = {
                                coroutineScope.launch {
                                    historySM.store(event.service)
                                        .onSuccess { navController.push(KPasswordScreen.Home) }
                                        .onFailure { showAlert.value = true }
                                }
                            }, modifier = Modifier.align(Alignment.CenterVertically)) {
                                Icon(painterResource("/icons/undo.svg"), "Restore", tint = color)
                            }
                            showOk(
                                "Error on restore",
                                "Error restoring service ${event.service.service}: ${historySM.saveError.value}",
                                showAlert
                            )
                        }
                    }
                })
        }
    }
}

@Composable
private fun contentRowModifier(event: Event) = when (event.action) {
    Action.delete -> MaterialTheme.colors.error
    Action.insert -> MaterialTheme.colors.primary
    Action.update -> MaterialTheme.colors.primary
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun displayHistService(event: Event, columnIndex: Int) {
    val service = event.service
    val colors = contentRowModifier(event)
    when (columnIndex) {
        0 -> Text(event.action.toString().asTitle(), color = colors)
        1 -> Text(event.actionDate.short(), color = colors)
        2 -> Text(service.service, color = colors)
        3 -> Text(service.username, color = colors)
        4 -> passwordToolTip(service.password, colors)
        5 -> Text(service.tags.joinToString(", "), color = colors)
        6 -> Text(
            text = service.note,
            softWrap = true,
            overflow = TextOverflow.Clip,
            minLines = 1,
            maxLines = 5,
            modifier = Modifier.widthIn(max = 350.dp),
            color = colors
        )
    }
}

@Composable
private fun RowScope.searchField(historySM: HistorySM) {
    val user = LocalUser.currentOrThrow
    val search = remember {
        mutableStateOf(TextFieldValue())
    }
    val coroutineScope = rememberCoroutineScope()

    OutlinedTextField(
        label = { Text("Search") },
        value = search.value,
        leadingIcon = {
            Icon(
                Icons.Default.Search,
                "Search"
            )
        },
        onValueChange = {
            search.value = it
            coroutineScope.launch {
                historySM.history(it.text, false, user)
            }
        },
        modifier = Modifier.testTag("Search field").align(Alignment.Bottom).width(INPUT_MEDIUM)
    )
}