package passwordStore.widget

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import com.seanproctor.datatable.DataColumn
import com.seanproctor.datatable.material3.PaginatedDataTable
import com.seanproctor.datatable.paging.rememberPaginatedDataTableState

@Composable
fun <T> Table(
    modifier: Modifier = Modifier,
    headers: List<String>,
    values: List<T>,
    cellContent: @Composable (col: Int, row: T) -> Unit,
    beforeRow: @Composable (row: T) -> Unit = {},
    columnCount: Int = headers.size,
) {

    PaginatedDataTable(
        columns = listOf(DataColumn {
            Text("")
        }) + headers.map {
            DataColumn {
                Text(
                    it,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        modifier = Modifier.fillMaxSize().then(modifier),
        state = rememberPaginatedDataTableState(10)
    ) {
        values.forEach { currentRow ->
            row {
                (0..columnCount).forEach { col ->
                    cell {
                        if (col == 0) {
                            beforeRow.invoke(currentRow)
                        } else {
                            cellContent(col - 1, currentRow)
                        }
                    }
                }
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
