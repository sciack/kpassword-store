package passwordStore.services

import androidx.compose.foundation.layout.*
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import kotlinx.coroutines.launch
import org.kodein.di.compose.localDI
import org.kodein.di.instance
import passwordStore.audit.Action
import passwordStore.audit.Event
import passwordStore.navigation.KPasswordScreen
import passwordStore.utils.asTitle
import passwordStore.utils.short
import passwordStore.widget.Table
import passwordStore.widget.showOk


@Composable
fun historyTable(historyEvents: List<Event>) {
    val serviceModel by localDI().instance<ServiceVM>()
    val coroutineScope = rememberCoroutineScope()
    val navController = LocalNavigator.currentOrThrow
    val headers = listOf("Action", "Action Date", "Service", "Username", "Password", "Note")

    Column(Modifier.fillMaxSize(.9f)) {
        Row(modifier = Modifier.fillMaxWidth()) {
            Table(modifier = Modifier.fillMaxSize(),
                headers = headers,
                values = historyEvents,
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
                                    serviceModel.store(event.service)
                                        .onSuccess { navController.push(KPasswordScreen.List) }
                                        .onFailure { showAlert.value = true }
                                }
                            }, modifier = Modifier.align(Alignment.CenterVertically)) {
                                Icon(painterResource("/icons/undo.svg"), "Restore", tint = color)
                            }
                            showOk(
                                "Error on restore",
                                "Error restoring service ${event.service.service}: ${serviceModel.saveError.value}",
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

@Composable
fun displayHistService(event: Event, columnIndex: Int) {
    val service = event.service
    val colors = contentRowModifier(event)
    when (columnIndex) {
        0 -> Text(event.action.toString().asTitle(), color = colors)
        1 -> Text(event.actionDate.short(), color = colors)
        2 -> Text(service.service, color = colors)
        3 -> Text(service.username, color = colors)
        4 -> Text(service.password, color = colors)
        5 -> Text(
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
