package passwordStore.widget

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.MaterialTheme
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import com.seanproctor.datatable.material3.DataTable
import com.seanproctor.datatable.DataColumn
import com.seanproctor.datatable.material3.PaginatedDataTable
import com.seanproctor.datatable.paging.rememberPaginatedDataTableState

@Composable
fun <T> Table(
    modifier: Modifier = Modifier,
    rowModifier: Modifier = Modifier,
    verticalLazyListState: LazyListState = rememberLazyListState(),
    horizontalScrollState: ScrollState = rememberScrollState(),
    headers: List<String>,
    values: List<T>,
    cellContent: @Composable (col: Int, row: T) -> Unit,
    beforeRow: @Composable (row: T) -> Unit = {},
    rowCount: Int = values.size,
    columnCount: Int = headers.size,
    contentRowModifier: @Composable (T) -> Modifier = { Modifier }
) {

    PaginatedDataTable(
        columns = listOf(DataColumn{
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
