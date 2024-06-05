package passwordStore.widget

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import passwordStore.ui.theme.MEDIUM
import passwordStore.ui.theme.SMALL
import passwordStore.ui.theme.XL


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

            with(CurrentEditorCardScope(onCloseRequest)) {
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
fun ScrollableView(
    modifier: Modifier = Modifier,
    title: String = "",
    onOk: () -> Unit,
    okEnabled: Boolean,
    onCancel: () -> Unit = {},
    content: @Composable ColumnScope.() -> Unit
) {
    Box(Modifier.then(modifier).fillMaxHeight().width(IntrinsicSize.Min)) {
        val scrollState = rememberScrollState()
        Row(Modifier.align(Alignment.TopStart).fillMaxHeight(0.1f).padding(top = SMALL)) {
            Column {
                Row(Modifier.align(Alignment.Start).padding(top = MEDIUM, bottom = XL)) {
                    Text(
                        title,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.secondary,
                        fontWeight = FontWeight.Bold
                    )
                }
                HorizontalDivider(thickness = 2.dp)
            }

        }
        Column(Modifier.align(Alignment.Center).fillMaxHeight(0.8f).verticalScroll(scrollState).padding(end = MEDIUM)) {
            content()
        }

        Row(Modifier.align(Alignment.BottomCenter).fillMaxHeight(0.1f).padding(top = SMALL)) {
            Column {
                HorizontalDivider(thickness = 2.dp)
                Row(Modifier.align(Alignment.CenterHorizontally).padding(top = MEDIUM)) {

                    Button(modifier = Modifier.testTag("submit"), enabled = okEnabled, onClick = onOk) {
                        Text("Submit")
                    }
                    Spacer(Modifier.width(SMALL))
                    Button(onClick = onCancel) {
                        Text("Cancel")
                    }
                }
            }

        }
        VerticalScrollbar(
            rememberScrollbarAdapter(scrollState),
            Modifier.align(Alignment.CenterEnd).fillMaxHeight(0.8f).width(MEDIUM)
        )
    }
}