package passwordStore

import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import passwordStore.audit.Event
import passwordStore.utils.titlecase
import passwordStore.widget.Table
import passwordStore.widget.bottomBorder
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle


@Composable
fun historyTable(historyEvents: List<Event>) {
    val headers = listOf("Action", "Action Date", "Service", "Username", "Password", "Note")
    Column(Modifier.fillMaxSize(.9f)) {
        Row(modifier = Modifier.fillMaxWidth()) {
            Table(
                modifier = Modifier.fillMaxSize(),
                rowModifier = Modifier.fillMaxWidth().bottomBorder(2.dp, color = Color.LightGray),
                rowCount = historyEvents.size,
                headers = headers,
                cellContent = { columnIndex, rowIndex ->
                    val service = historyEvents[rowIndex]
                    displayHistService(service, columnIndex)
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
            modifier = Modifier.widthIn(max=350.dp)
        )


    }

}
