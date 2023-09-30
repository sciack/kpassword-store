package passwordStore.services

import androidx.compose.foundation.ContextMenuDataProvider
import androidx.compose.foundation.ContextMenuItem
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.kodein.rememberScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import org.kodein.di.compose.localDI
import org.kodein.di.instance
import passwordStore.LOGGER
import passwordStore.navigation.KPasswordScreen
import passwordStore.services.ExportSM.State.*
import passwordStore.services.ImportSM.State.Import
import passwordStore.services.ImportSM.State.Loaded
import passwordStore.services.ServicesSM.State.Loading
import passwordStore.services.ServicesSM.State.Services
import passwordStore.services.ServiceSM.State.*
import passwordStore.ui.theme.*
import passwordStore.users.LocalUser
import passwordStore.utils.LocalStatusHolder
import passwordStore.utils.currentDateTime
import passwordStore.utils.obfuscate
import passwordStore.widget.*
import javax.swing.JFileChooser
import javax.swing.filechooser.FileNameExtensionFilter

@Composable
fun servicesTable(serviceSM: ServicesSM) {

    val navController = LocalNavigator.currentOrThrow
    val coroutineScope = rememberCoroutineScope()
    val state by serviceSM.state.collectAsState()
    val action = remember {
        MutableStateFlow<ServiceAction>(ServiceAction.Hide)
    }
    val user = LocalUser.currentOrThrow

    val (services, tags) = when (state) {
        is Services -> (state as Services).services to (state as Services).tags
        is Loading -> listOf<Service>() to (state as Loading).tags

    }
    Column(modifier = Modifier.padding(LARGE).fillMaxSize()) {
        Row(modifier = Modifier.fillMaxWidth()) {
            searchField(serviceSM)
            Spacer(Modifier.width(XL))
            tagView(tags, serviceSM.tag) { tag ->
                coroutineScope.launch(Dispatchers.IO) {
                    serviceSM.searchWithTags(tag?.name.orEmpty(), user = user)
                }
            }
        }

        Spacer(Modifier.height(LARGE))
        Row(modifier = Modifier.fillMaxWidth().fillMaxHeight(0.9f)) {
            if (state is Loading) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    CircularProgressIndicator(
                        modifier = Modifier.width(32.dp).align(Alignment.CenterHorizontally),
                        color = MaterialTheme.colors.surface,

                        backgroundColor = MaterialTheme.colors.secondary,
                    )
                }
            } else {
                Table(modifier = Modifier.fillMaxSize(),
                    headers = listOf("Service", "Username", "Password", "Url", "Tags", "Note"),
                    values = services,
                    beforeRow = { service ->
                        serviceButton(service, action) {
                            coroutineScope.launch(Dispatchers.IO) {
                                serviceSM.delete(service, user)
                            }
                        }
                    },
                    onClickRow = { row ->
                        coroutineScope.launch {
                            action.emit(ServiceAction.Show(row))
                        }
                    }) { columnIndex, service ->
                    cell(service, columnIndex)
                }
            }
        }

    }
    Box(Modifier.fillMaxSize()) {
        IconButton(
            onClick = { navController.push(KPasswordScreen.NewService) },
            modifier = Modifier.align(Alignment.BottomEnd).padding(LARGE)
        ) {
            Icon(
                Icons.Default.Add,
                KPasswordScreen.NewService.name,
                tint = MaterialTheme.colors.onPrimary,
                modifier = Modifier.size(XL * 2).clip(CircleShape).background(color = MaterialTheme.colors.primary)
                    .border(2.dp, color = MaterialTheme.colors.onPrimary, shape = CircleShape)
            )
        }
    }
    ShowServiceScreen(action) { serviceSM.fetchAll(user) }.Content()
}


@Composable
private fun serviceButton(
    service: Service, action: MutableStateFlow<ServiceAction>, onDelete: () -> Unit
) {
    val navController = LocalNavigator.currentOrThrow
    val coroutineScope = rememberCoroutineScope()
    Row {
        IconButton(
            onClick = {
                coroutineScope.launch {
                    action.emit(ServiceAction.Show(service))
                }
            }, modifier = Modifier.testTag("Show ${service.service}").align(Alignment.CenterVertically)
        ) {
            Icon(
                Icons.Default.KeyboardArrowRight, "Show", tint = MaterialTheme.colors.secondary

            )
        }
        Spacer(Modifier.width(XS))
        IconButton(
            onClick = {
                navController.push(KPasswordScreen.ServiceHistory(service))
            }, modifier = Modifier.testTag("History ${service.service}").align(Alignment.CenterVertically)
        ) {
            Icon(
                painterResource("/icons/history.svg"), "History", tint = MaterialTheme.colors.secondary
            )
        }
        Spacer(Modifier.width(XS))
        IconButton(
            onClick = {
                coroutineScope.launch {
                    action.emit(ServiceAction.Edit(service))
                }
            }, modifier = Modifier.testTag("Edit ${service.service}").align(Alignment.CenterVertically)
        ) {
            Icon(
                Icons.Default.Edit, "Edit", tint = MaterialTheme.colors.secondary
            )
        }
        val showAlert = remember {
            mutableStateOf(false)
        }
        Spacer(Modifier.width(XS))
        IconButton(
            onClick = { showAlert.value = true },
            modifier = Modifier.testTag("Delete ${service.service}").align(Alignment.CenterVertically)
        ) {
            Icon(
                Icons.Default.Delete, "Delete", tint = MaterialTheme.colors.error

            )
        }

        showOkCancel(title = "Delete confirmation",
            message = "Do you want to delete service ${service.service}?",
            showAlert,
            onConfirm = {
                onDelete()
            })
    }
}

@Composable
fun withMenu(service: Service, content: @Composable () -> Unit) {
    val clipboardManager = LocalClipboardManager.current

    ContextMenuDataProvider(items = {
        listOf(ContextMenuItem("Copy username") {
            clipboardManager.setText(
                AnnotatedString(service.username)
            )
        }, ContextMenuItem("Copy password") {
            clipboardManager.setText(
                AnnotatedString(service.password)
            )
        })
    }) {
        SelectionContainer {
            content()
        }

    }
}

@Composable
private fun cell(service: Service, columnIndex: Int) {

    val localUrl = LocalUriHandler.current


    when (columnIndex) {
        0 -> withMenu(service) {
            Text(service.service)
        }

        1 -> withMenu(service) {
            Text(service.username)
        }

        2 -> withMenu(service) {
            Text(service.password.obfuscate())
        }

        3 -> ClickableText(buildAnnotatedString {
            withStyle(
                style = SpanStyle(
                    color = MaterialTheme.colors.secondary,
                    textDecoration = TextDecoration.Underline
                )
            ) {
                append(service.url)
            }
            toAnnotatedString()
        },
            onClick = {
                LOGGER.warn("Try to open url ${service.url}")
                localUrl.openUri(service.url)
            })

        4 -> Text(service.tags.joinToString(", "))
        5 -> Text(
            text = service.note,
            softWrap = true,
            overflow = TextOverflow.Clip,
            minLines = 1,
            maxLines = 5,
            modifier = Modifier.widthIn(max = 350.dp)
        )

        else -> Text("")

    }

}


@Composable
fun newService(
    saveError: MutableState<String>,
    selectedService: Service = Service(),
    onCancel: () -> Unit,
    onSubmit: (Service) -> Unit
) {

    val user = LocalUser.currentOrThrow
    val service = remember {
        mutableStateOf(selectedService)
    }

    val tags = remember {
        mutableStateOf(selectedService.tags.toSet())
    }

    val dirty = remember {
        mutableStateOf(false)
    }

    val readonly = remember {
        mutableStateOf(selectedService.service.isNotEmpty())
    }
    val errorMsg = remember {
        saveError
    }
    val clock: Clock by localDI().instance()

    Row(Modifier.padding(LARGE)) {
        Column(modifier = Modifier.width(INPUT_LARGE)) {
            OutlinedTextField(
                label = { Text("Service") },
                onValueChange = { value ->
                    dirty.value = dirty.value || service.value.service != value
                    service.value = service.value.copy(service = value)
                },
                readOnly = readonly.value,
                value = service.value.service,
                modifier = Modifier.fillMaxWidth().align(Alignment.CenterHorizontally).testTag("service"),
                singleLine = true,
                isError = errorMsg.value.isNotEmpty()
            )

            tagEditor(tags, onValueChange = { values ->
                val modelTags = selectedService.tags
                dirty.value = dirty.value || values != modelTags
                service.value = service.value.copy(tags = values)
            })

            OutlinedTextField(
                label = { Text("Username") },
                value = service.value.username,
                onValueChange = {
                    dirty.value = dirty.value || service.value.username != it
                    service.value = service.value.copy(username = it)
                },
                modifier = Modifier.fillMaxWidth().align(Alignment.CenterHorizontally).testTag("username"),
                singleLine = true,
                isError = errorMsg.value.isNotEmpty()
            )

            val showPasswordDialog = remember {
                mutableStateOf(false)
            }

            OutlinedTextField(
                label = { Text("Password") },
                value = service.value.password,
                onValueChange = {
                    dirty.value = dirty.value || service.value.password != it
                    service.value = service.value.copy(password = it)
                },
                trailingIcon = {
                    IconButton(onClick = { showPasswordDialog.value = showPasswordDialog.value.not() }) {
                        Icon(Icons.Default.Edit, "Generate password")
                    }
                },
                modifier = Modifier.fillMaxWidth().align(Alignment.CenterHorizontally).testTag("password"),
                singleLine = true,
                isError = errorMsg.value.isNotEmpty()
            )
            passwordDialog(showPasswordDialog) {
                service.value = service.value.copy(password = it)
                dirty.value = true
            }

            OutlinedTextField(
                label = { Text("Url") },
                value = service.value.url,
                singleLine = true,
                onValueChange = {
                    dirty.value = dirty.value || service.value.url != it
                    service.value = service.value.copy(url = it)
                },
                modifier = Modifier.fillMaxWidth().align(Alignment.CenterHorizontally).testTag("url")
            )

            OutlinedTextField(
                label = { Text("Note") },
                value = service.value.note,
                minLines = 5,
                maxLines = 10,
                onValueChange = {
                    dirty.value = dirty.value || service.value.note != it
                    service.value = service.value.copy(note = it)
                },
                modifier = Modifier.fillMaxWidth().align(Alignment.CenterHorizontally).testTag("note")
            )
            if (errorMsg.value.isNotEmpty()) {
                Row(modifier = Modifier.align(Alignment.CenterHorizontally)) {
                    Text(
                        errorMsg.value,
                        color = MaterialTheme.colors.error,
                        fontWeight = FontWeight.Bold,
                        fontSize = TextUnit(0.8f, TextUnitType.Em),
                        modifier = Modifier.testTag("ErrorMsg")
                    )
                }
            }
            Row(Modifier.align(Alignment.CenterHorizontally).padding(top = SMALL)) {
                Button(modifier = Modifier.testTag("submit"), enabled = dirty.value, onClick = {
                    if (dirty.value) {

                        val newService = service.value.copy(
                            userid = user.userid, dirty = dirty.value, updateTime = clock.currentDateTime()
                        ).trim()
                        newService.validate().onSuccess {
                            onSubmit(newService)
                        }.onFailure {
                            errorMsg.value = it.localizedMessage
                        }
                    }
                }) {
                    Text("Submit")
                }
                Spacer(Modifier.width(SMALL))
                Button(onClick = {
                    onCancel()
                }) {
                    Text("Cancel")
                }
            }
        }
    }
}


@Composable
fun showService(selectedService: Service, onClose: () -> Unit) {
    val service = remember {
        selectedService
    }
    val tags = remember {
        mutableStateOf(selectedService.tags.toSet())
    }

    val clock: Clock by localDI().instance()
    val localUrl = LocalUriHandler.current

    Row(Modifier.padding(LARGE)) {
        Column(modifier = Modifier.width(INPUT_LARGE)) {
            OutlinedTextField(
                label = { Text("Service") },
                onValueChange = { },
                readOnly = true,
                value = service.service,
                modifier = Modifier.fillMaxWidth().align(Alignment.CenterHorizontally).testTag("service"),
                singleLine = true,
            )

            tagViewer(tags)

            OutlinedTextField(
                label = { Text("Username") },
                value = service.username,
                onValueChange = {},
                modifier = Modifier.fillMaxWidth().align(Alignment.CenterHorizontally).testTag("username"),
                singleLine = true,
                readOnly = true
            )

            OutlinedTextField(
                label = { Text("Password") },
                value = service.password,
                onValueChange = {},
                trailingIcon = {},
                modifier = Modifier.fillMaxWidth().align(Alignment.CenterHorizontally).testTag("password"),
                singleLine = true,
                readOnly = true
            )

            ClickableText(
                buildAnnotatedString {
                    withStyle(
                        style = SpanStyle(
                            color = MaterialTheme.colors.secondary,
                            textDecoration = TextDecoration.Underline
                        )
                    ) {
                        append(service.url)
                    }
                    toAnnotatedString()
                },
                onClick = {
                    LOGGER.warn("Try to open url ${service.url}")
                    localUrl.openUri(service.url)
                },
                modifier = Modifier.padding(LARGE)
            )

            OutlinedTextField(
                label = { Text("Note") },
                value = service.note,
                minLines = 5,
                maxLines = 10,
                onValueChange = {},
                modifier = Modifier.fillMaxWidth().align(Alignment.CenterHorizontally).testTag("note"),
                readOnly = true
            )

            Row(Modifier.align(Alignment.CenterHorizontally).padding(top = SMALL)) {
                Button(onClick = {
                    onClose()
                }) {
                    Text("Close")
                }
            }
        }
    }
}

@Composable
private fun RowScope.searchField(serviceSM: ServicesSM) {
    val user = LocalUser.currentOrThrow

    val search = remember {
        mutableStateOf(TextFieldValue(serviceSM.pattern))
    }
    val coroutineScope = rememberCoroutineScope()

    OutlinedTextField(label = { Text("Search") }, value = search.value, leadingIcon = {
        Icon(
            Icons.Default.Search, "Search"
        )
    }, onValueChange = {
        search.value = it
        coroutineScope.launch {
            serviceSM.searchPattern(it.text, user)
        }
    }, modifier = Modifier.testTag("Search field").align(Alignment.Bottom).width(INPUT_MEDIUM)
    )
}


@Composable
fun upload(screenModel: ImportSM) {
    val state by screenModel.state.collectAsState()
    when (state) {
        is Import -> importCsv(screenModel)
        is ImportSM.State.Loading -> loadCsvView(state as ImportSM.State.Loading)
        is Loaded -> {
            val csvFile = (state as Loaded).csvFile
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
                progress = current.toFloat() / total.toFloat(),
                modifier = Modifier.width(300.dp).height(XL)
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
fun exportStatus(state: Exporting) {
    val (current, total) = state
    Column(Modifier.fillMaxWidth()) {
        Spacer(Modifier.height(XL))
        Row(modifier = Modifier.align(Alignment.CenterHorizontally)) {
            LinearProgressIndicator(
                progress = current.toFloat() / total.toFloat(),
                modifier = Modifier.width(300.dp).height(XL)
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
        is Export -> exportCsv(screenModel)
        is Exporting -> exportStatus(state as Exporting)
        is Exported -> {
            val csvFile = (state as Exported).csvFile

            val statusHolder = LocalStatusHolder.currentOrThrow
            val navigator = LocalNavigator.currentOrThrow
            val successMessage = "Successfully created file: $csvFile"
            confirmUpload(successMessage) {
                statusHolder.sendMessage(successMessage)
                navigator.popUntil { it == KPasswordScreen.Home }
            }
        }

        is Error -> {
            val errorMsg = (state as Error).errorMsg
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


class ShowServiceScreen(
    private val action: MutableStateFlow<ServiceAction>,
    private val onChange: suspend () -> Unit
) : Screen {

    @Composable
    override fun Content() {
        val screenModel = rememberScreenModel<ServiceSM>()
        val state by screenModel.state.collectAsState()
        val coroutineScope = rememberCoroutineScope()
        val requestedDisplay by action.collectAsState()
        screenModel.display(requestedDisplay)

        when (state) {
            is NoService -> {}
            is EditService -> EditorCard(onCloseRequest = {
                coroutineScope.launch {
                    action.emit(ServiceAction.Hide)
                    onChange()
                }
            }) {
                newService(screenModel.saveError, selectedService = (state as EditService).service, onCancel = {
                    close()
                }) { service ->
                    coroutineScope.launch {
                        if (service.dirty) {
                            screenModel.update(service).onSuccess {
                                close()
                            }
                        }
                    }
                }
            }

            is ShowService -> EditorCard(onCloseRequest = {
                coroutineScope.launch {
                    action.emit(ServiceAction.Hide)
                }

            }) {
                showService(selectedService = (state as ShowService).service) {
                    close()
                }
            }
        }
    }
}