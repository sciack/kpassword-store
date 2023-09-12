package passwordStore.widget

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import passwordStore.config.LocalVersion
import passwordStore.ui.theme.LARGE
import passwordStore.ui.theme.XL


@Composable
fun showAbout(show: MutableState<Boolean>) {
    val version = LocalVersion.current
    if (show.value) {
        AlertDialog(
            text = {
                Row(Modifier.padding(XL)) {
                    Image(
                        painterResource("/icons/lockoverlay.png"),
                        "Lock Overlay",
                        modifier = Modifier.align(Alignment.CenterVertically)
                    )
                    val color = MaterialTheme.colors.onSurface
                    Column(
                        Modifier.padding(LARGE)
                    ) {
                        val value = buildAnnotatedString {
                            withStyle(
                                SpanStyle(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = TextUnit(1.5f, TextUnitType.Em),
                                    fontFamily = FontFamily.SansSerif
                                )
                            ) {
                                append("About KPassword Store")
                            }
                            appendLine()
                            append("Version: $version")
                            appendLine()
                            append("Built with Kotlin ${KotlinVersion.CURRENT}")
                            appendLine()
                            append("Using JVM: ${System.getProperty("java.version")} - ${System.getProperty("java.vendor")}")
                            appendLine()
                            appendLine()
                            appendLine()
                            withStyle(
                                SpanStyle(
                                    fontFamily = FontFamily.Cursive,
                                    fontStyle = FontStyle.Italic,
                                )
                            ) {
                                append("Author: Mirko Sciachero<m.sciachero@gmail.com>")
                            }
                            toAnnotatedString()
                        }
                        Text(value)
                    }
                }
            },
            title = {
                Text("About")
            },
            onDismissRequest = {
                show.value = false
            },
            confirmButton = {
                Column(Modifier.fillMaxWidth()) {
                    Button(onClick = {
                        show.value = false
                    }, modifier = Modifier.align(Alignment.CenterHorizontally)) {
                        Text("Close")
                    }
                }
            }
        )
    }
}
