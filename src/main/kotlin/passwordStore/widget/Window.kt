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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.WindowScope
import androidx.compose.ui.window.WindowState


val MENU_WIDTH = 96.dp
val APP_BAR_HEIGHT = 24.dp

@Composable
fun WindowScope.AppWindowTitleBar(
    title: MutableState<String>,
    state: WindowState,
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
            IconButton(
                onClick = onMinimize,
                modifier = Modifier.padding(end = 8.dp)
            ) {
                Icon(Icons.Default.Minimize, "Minimize", tint = MaterialTheme.colors.onPrimary)
            }
            IconButton(
                onClick = onMaximize,
                modifier = Modifier.padding(end = 8.dp)
            ) {
                if (state.placement == WindowPlacement.Maximized) {
                    Icon(
                        painterResource("/icons/window-restore.svg"),
                        "Restore",
                        tint = MaterialTheme.colors.onPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Icon(Icons.Default.Maximize, "Windows", tint = MaterialTheme.colors.onPrimary)
                }
            }
            IconButton(
                onClick = onClose,
                modifier = Modifier.padding(end = 8.dp)
            ) {
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