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


val MENU_WIDTH = 96.dp
val APP_BAR_HEIGHT = 24.dp

@Composable
fun WindowScope.AppWindowTitleBar(
    title: MutableState<String>,
    onMinimize: () -> Unit,
    onMaximize: () -> Unit,
    onClose: () -> Unit,
    navigationIcon: @Composable () -> Unit
) {
    Box(Modifier.background(MaterialTheme.colors.primary).width(MENU_WIDTH)) {
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
    WindowDraggableArea(Modifier.fillMaxWidth().padding(start = MENU_WIDTH)) {

        Box(
            Modifier.fillMaxWidth().height(APP_BAR_HEIGHT)
                //.shadow(4.dp, RoundedCornerShape(4.dp, 4.dp, 12.dp, 12.dp), ambientColor = MaterialTheme.colors.background)
                .background(MaterialTheme.colors.primary)
        ) {
            Row(modifier = Modifier.align(Alignment.TopStart)) {
                Text(title.value, color = MaterialTheme.colors.onPrimary)
            }
        }
    }