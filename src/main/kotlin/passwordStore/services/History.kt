package passwordStore.services

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.kodein.di.compose.localDI
import org.kodein.di.instance
import passwordStore.Screen
import passwordStore.audit.Action
import passwordStore.audit.Event
import passwordStore.navigation.NavController
import passwordStore.utils.titlecase
import passwordStore.widget.Table
import passwordStore.widget.bottomBorder
import passwordStore.widget.showOk
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle


@Composable
fun historyTable(historyEvents: List<Event>) {
    val serviceModel by localDI().instance<ServiceViewModel>()
    val coroutineScope = rememberCoroutineScope()
    val navController by localDI().instance<NavController>()
    val headers = listOf("Action", "Action Date", "Service", "Username", "Password", "Note")
    Column(Modifier.fillMaxSize(.9f)) {
        Row(modifier = Modifier.fillMaxWidth()) {
            Table(
                modifier = Modifier.fillMaxSize(),
                rowModifier = Modifier.fillMaxWidth().bottomBorder(2.dp, color = Color.LightGray),
                rowCount = historyEvents.size,
                headers = headers,
                values = historyEvents,
                cellContent = { columnIndex, event ->
                    displayHistService(event, columnIndex)
                },
                contentRowModifier = { event ->
                    when (event.action) {
                        Action.delete -> Modifier.background(Color(0xffcc7f7f))
                        Action.insert -> Modifier
                        Action.update -> Modifier.background(Color.LightGray)
                    }
                },
                beforeRow = { event ->
                    Row {
                        if (event.action == Action.delete) {
                            val showAlert = remember {
                                mutableStateOf(false)
                            }
                            IconButton(onClick = {
                                coroutineScope.launch {
                                    serviceModel.store(event.service)
                                        .onSuccess { navController.navigate(Screen.List) }
                                        .onFailure { showAlert.value = true }
                                }
                            }) {
                                Icon(Icons.Default.KeyboardArrowLeft, "Restore")
                            }
                            showOk(
                                "Error on restore",
                                "Error restoring service ${event.service.service}: ${serviceModel.saveError.value}",
                                showAlert
                            )
                        }
                    }
                }
            )
        }
    }
}

@Composable
fun displayHistService(event: Event, columnIndex: Int) {
    val service = event.service
    when (columnIndex) {
        0 -> Text(event.action.toString().titlecase())
        1 -> Text(event.actionDate.format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT)))
        2 -> Text(service.service)
        3 -> Text(service.username)
        4 -> Text(service.password)
        5 -> Text(
            text = service.note,
            softWrap = true,
            overflow = TextOverflow.Clip,
            minLines = 1,
            maxLines = 5,
            modifier = Modifier.widthIn(max = 350.dp)
        )


    }

}
