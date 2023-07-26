package passwordStore.widget

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.layout
import androidx.compose.ui.text.font.FontWeight

@Composable
fun Table(
    modifier: Modifier = Modifier,
    rowModifier: Modifier = Modifier,
    verticalLazyListState: LazyListState = rememberLazyListState(),
    horizontalScrollState: ScrollState = rememberScrollState(),
    columnCount: Int,
    rowCount: Int,
    cellContent: @Composable (columnIndex: Int, rowIndex: Int) -> Unit,
    headers: List<String>
) {
    val columnWidths = remember { mutableStateMapOf<Int, Int>() }

    Box(modifier = modifier.then(Modifier.horizontalScroll(horizontalScrollState))) {
        Column {
            Column {
                Row {
                    (0 until columnCount).forEach { columnIndex ->
                        Box(modifier = Modifier.layout { measurable, constraints ->
                            val placeable = measurable.measure(constraints)

                            val existingWidth = columnWidths[columnIndex] ?: 0
                            val maxWidth = maxOf(existingWidth, placeable.width)

                            if (maxWidth > existingWidth) {
                                columnWidths[columnIndex] = maxWidth
                            }

                            layout(width = maxWidth, height = placeable.height) {
                                placeable.placeRelative(0, 0)
                            }
                        }) {
                            Text(
                                fontWeight = FontWeight.Bold,
                                text = headers[columnIndex]
                            )
                        }
                    }
                }
            }
            Box(modifier= modifier) {
                LazyColumn(state = verticalLazyListState) {
                    items(rowCount) { rowIndex ->
                        Column {

                            Row(modifier = rowModifier) {
                                (0 until columnCount).forEach { columnIndex ->
                                    Box(modifier = Modifier.layout { measurable, constraints ->
                                        val placeable = measurable.measure(constraints)

                                        val existingWidth = columnWidths[columnIndex] ?: 0
                                        val maxWidth = maxOf(existingWidth, placeable.width)

                                        if (maxWidth > existingWidth) {
                                            columnWidths[columnIndex] = maxWidth
                                        }

                                        layout(width = maxWidth, height = placeable.height) {
                                            placeable.placeRelative(0, 0)
                                        }
                                    }) {
                                        cellContent(columnIndex, rowIndex)
                                    }
                                }
                            }


                        }
                    }
                }
                VerticalScrollbar(
                    modifier = Modifier.align(Alignment.CenterEnd)
                        .fillMaxHeight(),
                    adapter = rememberScrollbarAdapter(verticalLazyListState)
                )
            }
        }
    }
}