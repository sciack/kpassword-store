package passwordStore.widget

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedCard
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.layout
import passwordStore.ui.theme.MEDIUM


@Composable
fun EditorCard(onCloseRequest: () -> Unit, content: @Composable EditorCardScope.() -> Unit) {

    Box(Modifier.fillMaxSize().clickable {
        onCloseRequest()
    }) {
        OutlinedCard(modifier = Modifier.fillMaxHeight(0.9f).layout { measurable, constraints ->
            val placeable = measurable.measure(constraints)
            val maxWidth = constraints.maxWidth
            val x = (maxWidth - placeable.width).coerceAtLeast(0)
            layout(width = placeable.width, height = placeable.height) {
                placeable.place(x, 10)
            }
        }.clickable  {
            // just do nothing but avoid propagate the click to the box
        },
            elevation = CardDefaults.cardElevation(MEDIUM),
            colors = CardDefaults.cardColors(MaterialTheme.colors.background)
        ) {
            with(object: EditorCardScope {
                override fun close() {
                    onCloseRequest()
                }
            }) {
                content()
            }
        }
    }
}

interface EditorCardScope {
    fun close()
}