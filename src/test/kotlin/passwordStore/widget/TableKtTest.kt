package passwordStore.widget

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import passwordStore.DiInjection
import kotlin.test.Test


class TableKtTest {
    private val di = DiInjection.testDi

    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `should display a table with 2 columns`() = runTest {
        val headers = listOf("First header", "Second header")
        rule.setContent {
            Table(
                columnCount = 2,
                rowCount = 2,
                headers = headers,
                cellContent = { columnIndex, rowIndex ->
                    content(rowIndex, columnIndex)
                }
            )
        }
        rule.awaitIdle()
        headers.forEach {
            rule.onNodeWithText(it).assertExists()
        }
        (0 until 2).forEach { row ->
            (0 until 2).forEach { col ->
                rule.onNodeWithText("Row: $row, Column: $col").assertExists()
            }
        }
    }

    @Composable
    private fun content(rowIndex: Int, columnIndex: Int) {
        Text("Row: $rowIndex, Column: $columnIndex")
    }
}