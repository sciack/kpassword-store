package passwordStore.widget

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.window.WindowDraggableArea
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Maximize
import androidx.compose.material.icons.filled.Minimize
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.WindowScope
import androidx.compose.ui.window.WindowState

import passwordStore.ui.theme.XL
import passwordStore.ui.theme.XXL


val MENU_WIDTH = 128.dp
val APP_BAR_HEIGHT = XXL

@Composable
fun WindowScope.AppWindowTitleBar(
    title: @Composable RowScope.() -> Unit = {},
    state: WindowState,
    onMinimize: () -> Unit,
    onMaximize: () -> Unit,
    onClose: () -> Unit,
    navigationIcon: @Composable () -> Unit
) {
    Box(Modifier.background(MaterialTheme.colorScheme.primary).width(MENU_WIDTH).height(XXL)) {
        Row {
            navigationIcon()
        }
    }
    AppDraggableArea(title)
    Box(Modifier.fillMaxWidth()) {
        Row(Modifier.align(Alignment.TopEnd).padding()) {
            IconButton(
                onClick = onMinimize,
                modifier = Modifier.padding().align(Alignment.CenterVertically)
            ) {
                Icon(
                    Icons.Default.Minimize, "Minimize", tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(XL)
                )
            }
            IconButton(
                onClick = onMaximize,
                modifier = Modifier.padding().align(Alignment.CenterVertically)
            ) {
                if (state.placement == WindowPlacement.Maximized) {
                    Icon(
                        painterResource("/icons/window-restore.svg"),
                        "Restore",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(XL)
                    )
                } else {
                    Icon(
                        Icons.Default.Maximize, "Windows", tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(XL)
                    )
                }
            }
            IconButton(
                onClick = onClose,
                modifier = Modifier.padding().align(Alignment.CenterVertically)
            ) {
                Icon(
                    Icons.Default.Close, "Close", tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(XL)
                )
            }
        }
    }
}

@Composable
fun WindowScope.AppDraggableArea(title: @Composable RowScope.() -> Unit) =
    WindowDraggableArea(Modifier.fillMaxWidth().padding(start = MENU_WIDTH)) {

        Box(
            Modifier.fillMaxWidth().height(APP_BAR_HEIGHT)
                //.shadow(4.dp, RoundedCornerShape(4.dp, 4.dp, 12.dp, 12.dp), ambientColor = MaterialTheme.colors.background)
                .background(MaterialTheme.colorScheme.primary)
        ) {
            Row(modifier = Modifier.align(Alignment.TopCenter).height(XXL).padding(end = MENU_WIDTH)) {
                title()
            }
        }
    }