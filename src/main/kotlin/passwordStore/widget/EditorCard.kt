package passwordStore.widget

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.layout
import androidx.compose.ui.unit.dp



@Composable
fun EditorCard(onCloseRequest: () -> Unit, content: @Composable EditorCardScope.() -> Unit) {

    Box(Modifier.fillMaxSize().clickable {
        onCloseRequest()
    }) {
        ElevatedCard(modifier = Modifier.fillMaxHeight(0.9f).layout { measurable, constraints ->
            val placeable = measurable.measure(constraints)
            val maxWidth = constraints.maxWidth
            val x = (maxWidth - placeable.width).coerceAtLeast(0)
            layout(width = placeable.width, height = placeable.height) {
                placeable.place(x, 10)
            }
        }.clickable {
            // just do nothing but avoid propagate the click to the box
        }) {
            withScope(onCloseRequest) {
                content()
            }
        }
    }
}

interface EditorCardScope {
    fun close()
}


private class CurrentEditorCardScope(private val onClose: () -> Unit) : EditorCardScope {

    override fun close() {
        onClose()
    }
}

@Composable
fun withScope(onClose: () -> Unit, content: @Composable EditorCardScope.() -> Unit) {
    with(CurrentEditorCardScope(onClose)) {
        content()
    }
}