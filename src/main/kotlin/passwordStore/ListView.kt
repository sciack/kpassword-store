package passwordStore

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun servicesTable(services: List<Service>) {
    val rows = services.map { row ->
        Row(Modifier.padding(16.dp)) {
            Text(row.service)
            Text(row.userid)
            Text(row.username)
            Text(row.password)
        }
    }
    Column(Modifier.padding(16.dp)) {
        Row(Modifier.padding(16.dp), horizontalArrangement = Arrangement.Center) {
            header.forEach { text ->
                Text(text)
            }
        }
        rows
    }
}

private val header = arrayOf<String>("Col 1", "Col2")