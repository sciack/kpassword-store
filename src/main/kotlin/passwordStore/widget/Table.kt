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
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp

@Composable
fun Table(
    modifier: Modifier = Modifier,
    rowModifier: Modifier = Modifier,
    verticalLazyListState: LazyListState = rememberLazyListState(),
    horizontalScrollState: ScrollState = rememberScrollState(),
    headers: List<String>,
    cellContent: @Composable (col: Int, row: Int) -> Unit,
    beforeRow: @Composable (row: Int) -> Unit = {},
    rowCount: Int,
    columnCount: Int = headers.size
) {
    val columnWidths = remember { mutableStateMapOf<Int, Int>() }

    Box(modifier = modifier.then(Modifier.horizontalScroll(horizontalScrollState))) {
        Column(modifier = modifier.fillMaxWidth()) {

            Row(modifier = rowModifier) {
                (0..columnCount).forEach { columnIndex ->
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
                        if (columnIndex == 0) {
                            Text("")
                        } else {
                            Text(
                                fontWeight = FontWeight.Bold,
                                text = headers[columnIndex - 1]
                            )
                        }
                    }
                }
            }

            Box(modifier = modifier) {
                LazyColumn(state = verticalLazyListState,
                    modifier = Modifier.fillMaxWidth()) {
                    items(rowCount) { row ->

                        Column(modifier=Modifier.fillMaxWidth()) {
                            Row(modifier = rowModifier) {

                                (0..columnCount).forEach { col ->
                                    Box(modifier = Modifier.layout { measurable, constraints ->
                                        val placeable = measurable.measure(constraints)

                                        val existingWidth = columnWidths[col] ?: 0
                                        val maxWidth = maxOf(existingWidth, placeable.width)

                                        if (maxWidth > existingWidth) {
                                            columnWidths[col] = maxWidth
                                        }

                                        layout(width = maxWidth, height = placeable.height) {
                                            placeable.placeRelative(0, 0)
                                        }
                                    }) {
                                        if (col == 0) {
                                            beforeRow.invoke(row)
                                        } else {
                                            cellContent(col - 1, row)
                                        }
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

@Composable
fun Modifier.bottomBorder(strokeWidth: Dp, color: Color) = composed(
    factory = {
        val density = LocalDensity.current
        val strokeWidthPx = density.run { strokeWidth.toPx() }

        Modifier.drawBehind {
            val width = size.width
            val height = size.height - strokeWidthPx / 2

            drawLine(
                color = color,
                start = Offset(x = 0f, y = height),
                end = Offset(x = width, y = height),
                strokeWidth = strokeWidthPx
            )
        }
    }
)
