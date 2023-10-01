package passwordStore.services

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import passwordStore.navigation.KPasswordScreen
import passwordStore.services.audit.Action
import passwordStore.services.audit.Event
import passwordStore.ui.theme.INPUT_MEDIUM
import passwordStore.ui.theme.LARGE
import passwordStore.users.LocalUser
import passwordStore.utils.asTitle
import passwordStore.utils.short
import passwordStore.widget.Table
import passwordStore.widget.passwordToolTip
import passwordStore.widget.showOk


@Composable
fun history(historySM: HistorySM, service: Service?) {
    val state by historySM.state.collectAsState()
    val user = LocalUser.currentOrThrow
    when (state) {
        HistorySM.State.First -> {
            Column(Modifier.fillMaxSize()) {
                CircularProgressIndicator(
                    modifier = Modifier.width(32.dp).align(Alignment.CenterHorizontally),
                    color = MaterialTheme.colorScheme.surface
                )
            }
            LaunchedEffect("after circular") {
                historySM.loadHistory(service?.service, user)
            }
        }

        is HistorySM.State.Loaded -> historyTable(historySM, (state as HistorySM.State.Loaded).history)
    }
}

@Composable
fun historyTable(historySM: HistorySM, history: List<Event>) {
    val coroutineScope = rememberCoroutineScope()
    val navController = LocalNavigator.currentOrThrow
    val headers = listOf("Action", "Action Date", "Service", "Username", "Password", "Url", "Tags", "Note")

    Column(Modifier.fillMaxSize().padding(LARGE)) {
        Row {
            searchField(historySM)
        }
        Row(modifier = Modifier.fillMaxWidth()) {
            Table(modifier = Modifier.fillMaxSize(),
                headers = headers,
                values = history,
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
    Action.delete -> MaterialTheme.colorScheme.error
    Action.insert -> MaterialTheme.colorScheme.primary
    Action.update -> MaterialTheme.colorScheme.tertiary
}

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
        5 -> Text(service.url, color = colors)
        6 -> Text(service.tags.joinToString(", "), color = colors)
        7 -> Text(
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