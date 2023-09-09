package passwordStore.widget

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.window.WindowDraggableArea
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Maximize
import androidx.compose.material.icons.filled.Minimize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.WindowScope

@Composable
fun WindowScope.AppWindowTitleBar(
    title: MutableState<String>,
    onMinimize: () -> Unit,
    onMaximize: () -> Unit,
    onClose: () -> Unit,
    navigationIcon: @Composable () -> Unit
) {
    Box {
        Row {
            navigationIcon()
        }
    }
    AppDraggableArea(title)
    Box(Modifier.fillMaxWidth()) {
        Row(Modifier.align(Alignment.TopEnd).padding(horizontal = 8.dp)) {
            IconButton(onClick = onMinimize) {
                Icon(Icons.Default.Minimize, "Minimize", tint = MaterialTheme.colors.onPrimary)
            }
            IconButton(onClick = onMaximize) {
                Icon(Icons.Default.Maximize, "Maximize", tint = MaterialTheme.colors.onPrimary)
            }
            IconButton(onClick = onClose) {
                Icon(Icons.Default.Close, "Close", tint = MaterialTheme.colors.onPrimary)
            }
        }
    }
}

@Composable
fun WindowScope.AppDraggableArea(title: MutableState<String>) =
    WindowDraggableArea(Modifier.fillMaxWidth().padding(start = 96.dp)) {

        Box(
            Modifier.fillMaxWidth().height(24.dp)
                //.shadow(4.dp, RoundedCornerShape(4.dp, 4.dp, 12.dp, 12.dp), ambientColor = MaterialTheme.colors.background)
                .background(MaterialTheme.colors.primary)
        ) {
            Row(modifier = Modifier.align(Alignment.TopStart)) {
                Text(title.value, color = MaterialTheme.colors.onPrimary)
            }
        }
    }