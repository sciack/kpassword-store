package passwordStore.widget

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.seanproctor.datatable.DataColumn
import com.seanproctor.datatable.TableCellScope
import com.seanproctor.datatable.material3.PaginatedDataTable
import com.seanproctor.datatable.paging.rememberPaginatedDataTableState

@Composable
fun <T> Table(
    modifier: Modifier = Modifier,
    headers: List<String>,
    values: List<T>,
    beforeRow: @Composable TableCellScope.(row: T) -> Unit = {},
    onClickRow: (row: T) -> Unit = {},
    cellContent: @Composable TableCellScope.(col: Int, row: T) -> Unit
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
                onClick = {
                    onClickRow(currentRow)
                }
                (0..headers.size).forEach { col ->
                    cell {
                        if (col == 0) {
                            beforeRow(currentRow)
                        } else {
                            cellContent(col - 1, currentRow)
                        }
                    }
                }
            }
        }
    }
}
