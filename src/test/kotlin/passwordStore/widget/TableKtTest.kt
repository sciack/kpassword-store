package passwordStore.widget

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import passwordStore.DiInjection
import passwordStore.LOGGER
import kotlin.test.Test


class TableKtTest {
    private val di = DiInjection.testDi

    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `should display a table with 2 columns`() = runTest {
        val headers = listOf("First header", "Second header")
        val elements = listOf(listOf("1", "2"), listOf("3", "4"))
        rule.setContent {
            Table(
                headers = headers,
                values = elements,
                cellContent = { columnIndex, element ->
                    content(element, columnIndex)
                }
            )
        }
        rule.awaitIdle()
        headers.forEach {
            rule.onNodeWithText(it).assertExists()
        }
        var counter = 1
        (0 until 2).forEach { _ ->
            (0 until 2).forEach { col ->
                rule.onNodeWithText("Row: ${counter}, Column: $col").assertExists()
                counter = counter.inc()
            }
        }
    }

    @Composable
    private fun content(ele: List<String>, columnIndex: Int) {
        val text = "Row: ${ele[columnIndex]}, Column: $columnIndex"
        LOGGER.warn { text }
        Text(text)
    }
}