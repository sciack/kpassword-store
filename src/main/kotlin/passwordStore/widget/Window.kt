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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.WindowScope
import androidx.compose.ui.window.WindowState

import passwordStore.ui.theme.XL
import passwordStore.ui.theme.XXL
import passwordStore.utils.Platform


val MENU_WIDTH = 128.dp
val APP_BAR_HEIGHT: Dp by lazy {
    when (Platform.os()) {
        Platform.OsFamily.LINUX -> 36.dp
        Platform.OsFamily.WINDOWS -> 36.dp
        else -> 48.dp
    }
}

val ICON_SIZE: Dp by lazy {
    when (Platform.os()) {
        Platform.OsFamily.LINUX -> 12.dp
        else -> 24.dp
    }
}

@Composable
fun WindowScope.AppWindowTitleBar(
    title: @Composable RowScope.() -> Unit = {},
    state: WindowState,
    onMinimize: () -> Unit,
    onMaximize: () -> Unit,
    onClose: () -> Unit,
    navigationIcon: @Composable () -> Unit
) {
    Box(Modifier.background(MaterialTheme.colorScheme.primary).width(MENU_WIDTH).height(APP_BAR_HEIGHT)) {
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
                    modifier = Modifier.size(ICON_SIZE)
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
                        modifier = Modifier.size(ICON_SIZE)
                    )
                } else {
                    Icon(
                        Icons.Default.Maximize, "Windows", tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(ICON_SIZE)
                    )
                }
            }
            IconButton(
                onClick = onClose,
                modifier = Modifier.padding().align(Alignment.CenterVertically)
            ) {
                Icon(
                    Icons.Default.Close, "Close", tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(ICON_SIZE)
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
            Row(modifier = Modifier.align(Alignment.TopCenter).height(APP_BAR_HEIGHT).padding(end = MENU_WIDTH)) {
                title()
            }
        }
    }