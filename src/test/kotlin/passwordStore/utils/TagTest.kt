package passwordStore.utils

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.isEmpty
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.jetbrains.skia.Surface
import org.junit.Rule
import passwordStore.DiInjection
import kotlin.test.Test
import kotlin.test.assertContains

class TagTests() {

    private val di = DiInjection.testDi

    @get:Rule
    val rule = createComposeRule()

    // don't inline, surface controls canvas life time
    private val surface = Surface.makeRasterN32Premul(100, 100)

    private val canvas = surface.canvas

    @Test
    fun testEmptyPlainTextNode() {
        runBlocking(Dispatchers.Main) {
            rule.setContent {
                val tags = remember {
                    mutableStateOf(setOf("tag"))
                }
                tagEditor(tags, {})
            }
            rule.awaitIdle()

            rule.onNodeWithTag("tags").performTextInput("testLabel,")
            rule.awaitIdle()
            rule.onNodeWithText("testLabel").assertExists()
        }
    }


    @Test
    fun `on value change should add a callback`() {
        runBlocking(Dispatchers.Main) {
            val collectedTags = mutableListOf<String>()
            rule.setContent {
                val tags = remember {
                    mutableStateOf(setOf("tag"))
                }
                tagEditor(tags) {
                    collectedTags.clear()
                    collectedTags.addAll(it)
                }
            }
            rule.awaitIdle()

            rule.onNodeWithTag("tags").performTextInput("testLabel,")
            rule.awaitIdle()
            rule.onNodeWithText("testLabel").assertExists()
            assertContains(collectedTags, "testLabel")
        }
    }

    @Test
    fun `on click on tag should remove the entry`() {
        runBlocking(Dispatchers.Main) {
            val collectedTags = mutableListOf<String>("tags")
            rule.setContent {
                val tags = remember {
                    mutableStateOf(collectedTags.toSet())
                }
                tagEditor(tags) {
                    collectedTags.clear()
                    collectedTags.addAll(it)
                }
            }
            rule.awaitIdle()

            rule.onNodeWithText("tags").performClick()
            rule.awaitIdle()

            assertThat(collectedTags, isEmpty)
        }
    }
}