package passwordStore.services

import androidx.compose.foundation.layout.*
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import passwordStore.navigation.KPasswordScreen
import passwordStore.ui.theme.XL
import passwordStore.users.LocalUser
import passwordStore.utils.LocalStatusHolder
import passwordStore.widget.confirmUpload
import passwordStore.widget.displayError
import javax.swing.JFileChooser
import javax.swing.filechooser.FileNameExtensionFilter


@Composable
fun upload(screenModel: ImportSM) {
    val state by screenModel.state.collectAsState()
    when (state) {
        is ImportSM.State.Import -> importCsv(screenModel)
        is ImportSM.State.Loading -> loadCsvView(state as ImportSM.State.Loading)
        is ImportSM.State.Loaded -> {
            val csvFile = (state as ImportSM.State.Loaded).csvFile
            val statusHolder = LocalStatusHolder.currentOrThrow
            val navigator = LocalNavigator.currentOrThrow
            val successMessage = "Successfully uploaded file: $csvFile"
            confirmUpload(successMessage) {
                statusHolder.sendMessage(successMessage)
                navigator.popUntil { it == KPasswordScreen.Home }
            }
        }

        is ImportSM.State.Error -> {
            val errorMsg = (state as ImportSM.State.Error).errorMsg
            displayError(errorMsg)
        }
    }
}

@Composable
fun importCsv(screenModel: ImportSM) {
    val home = System.getProperty("user.home")
    val user = LocalUser.currentOrThrow
    val (result, path) = remember {
        val fileChooser = JFileChooser(home)
        fileChooser.fileFilter = FileNameExtensionFilter("Comma Separated File", "csv")
        fileChooser.showOpenDialog(null) to fileChooser.selectedPath()
    }

    if (result == JFileChooser.APPROVE_OPTION && path != null) {
        screenModel.startLoading(path, user)
    } else {
        LocalNavigator.currentOrThrow.popUntil { it == KPasswordScreen.Home }
    }

}


@Composable
fun loadCsvView(state: ImportSM.State.Loading) {
    val (correct, fail, current, total) = state
    Column(Modifier.fillMaxWidth()) {
        Spacer(Modifier.height(XL))
        Row(modifier = Modifier.align(Alignment.CenterHorizontally)) {
            LinearProgressIndicator(
                progress = current.toFloat() / total.toFloat(), modifier = Modifier.width(300.dp).height(XL)
            )
        }
        Spacer(Modifier.height(XL))
        Row(modifier = Modifier.align(Alignment.CenterHorizontally)) {
            val annotatedString = buildAnnotatedString {
                withStyle(
                    SpanStyle(
                        fontWeight = FontWeight.Bold,
                    )
                ) {
                    append("Progress: ")
                }
                append("$current/$total")
                append("    ")
                append("$correct")
                withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                    append(" Correct")
                }
                append("    ")
                append("$fail")
                withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                    append("Failed")
                }
                toAnnotatedString()
            }
            Text(annotatedString)
        }
    }
}


@Composable
fun exportStatus(state: ExportSM.State.Exporting) {
    val (current, total) = state
    Column(Modifier.fillMaxWidth()) {
        Spacer(Modifier.height(XL))
        Row(modifier = Modifier.align(Alignment.CenterHorizontally)) {
            LinearProgressIndicator(
                progress = current.toFloat() / total.toFloat(), modifier = Modifier.width(300.dp).height(XL)
            )
        }
        Spacer(Modifier.height(XL))
        Row(modifier = Modifier.align(Alignment.CenterHorizontally)) {
            val annotatedString = buildAnnotatedString {
                withStyle(
                    SpanStyle(
                        fontWeight = FontWeight.Bold,
                    )
                ) {
                    append("Progress: ")
                }
                append("$current/$total")

                toAnnotatedString()
            }
            Text(annotatedString)
        }
    }
}

@Composable
fun download(screenModel: ExportSM) {
    val state by screenModel.state.collectAsState()
    when (state) {
        is ExportSM.State.Export -> exportCsv(screenModel)
        is ExportSM.State.Exporting -> exportStatus(state as ExportSM.State.Exporting)
        is ExportSM.State.Exported -> {
            val csvFile = (state as ExportSM.State.Exported).csvFile

            val statusHolder = LocalStatusHolder.currentOrThrow
            val navigator = LocalNavigator.currentOrThrow
            val successMessage = "Successfully created file: $csvFile"
            confirmUpload(successMessage) {
                statusHolder.sendMessage(successMessage)
                navigator.popUntil { it == KPasswordScreen.Home }
            }
        }

        is ExportSM.State.Error -> {
            val errorMsg = (state as ExportSM.State.Error).errorMsg
            displayError(errorMsg)
        }
    }
}


@Composable
fun exportCsv(screenModel: ExportSM) {
    val exportPath = exportPath()
    val user = LocalUser.currentOrThrow
    val (result, path) = remember {
        val fileChooser = JFileChooser(exportPath.toFile())
        fileChooser.fileFilter = FileNameExtensionFilter("Comma Separated File", "csv")
        fileChooser.showSaveDialog(null) to fileChooser.selectedPath()
    }

    if (result == JFileChooser.APPROVE_OPTION && path != null) {
        screenModel.startExport(path, user)
    } else {
        LocalNavigator.currentOrThrow.popUntil { it == KPasswordScreen.Home }
    }

}

private fun JFileChooser.selectedPath() = selectedFile?.toPath()