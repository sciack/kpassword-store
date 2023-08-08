package passwordStore.widget

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import kotlinx.coroutines.test.runTest
import mu.KotlinLogging
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
        val elements = listOf(listOf("1", "2"), listOf("3", "4"))
        rule.setContent {
            Table(
                columnCount = 2,
                rowCount = elements.size,
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
        (0 until 2).forEach { row ->
            (0 until 2).forEach { col ->
                rule.onNodeWithText("Row: ${counter}, Column: $col").assertExists()
                counter = counter.inc()
            }
        }
    }

    @Composable
    private fun content(ele: List<String>, columnIndex: Int) {
        val text = "Row: ${ele[columnIndex]}, Column: $columnIndex"
        KotlinLogging.logger {}.warn { text }
        Text(text)
    }
}