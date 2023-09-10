package passwordStore.services

import androidx.compose.foundation.ContextMenuDataProvider
import androidx.compose.foundation.ContextMenuItem
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.seanproctor.datatable.TableCellScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import org.kodein.di.DI
import org.kodein.di.compose.localDI
import org.kodein.di.compose.rememberInstance
import org.kodein.di.instance
import passwordStore.LOGGER
import passwordStore.navigation.KPasswordScreen
import passwordStore.ui.theme.*
import passwordStore.users.UserVM
import passwordStore.utils.StatusHolder
import passwordStore.utils.currentDateTime
import passwordStore.utils.obfuscate
import passwordStore.widget.*
import javax.swing.JFileChooser
import javax.swing.filechooser.FileNameExtensionFilter
import kotlin.io.path.writer

@Composable
fun servicesTable() {
    val serviceModel by localDI().instance<ServiceVM>()
    val navController = LocalNavigator.currentOrThrow
    val coroutineScope = rememberCoroutineScope()

    val services = remember {
        serviceModel.services
    }

    val editService = remember {
        mutableStateOf(false)
    }

    val selectedService = remember {
        serviceModel.selectedService
    }


    Column(modifier = Modifier.padding(LARGE).fillMaxSize()) {
        Row(modifier = Modifier.fillMaxWidth()) {
            searchField()
            Spacer(Modifier.width(XL))
            tagView()
        }

        Spacer(Modifier.height(LARGE))
        Row(modifier = Modifier.fillMaxWidth().fillMaxHeight(0.9f)) {
            Table(
                modifier = Modifier.fillMaxSize(),
                headers = listOf("Tags", "Service", "Username", "Password", "Note"),
                values = services.toList(),
                beforeRow = { service ->
                    serviceButton(service, editService)
                }
            ) { columnIndex, service ->
                cell(service, columnIndex)
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
                tint = MaterialTheme.colors.onSurface,
                modifier = Modifier.size(XL * 2 ).clip(CircleShape)
                    .background(color=MaterialTheme.colors.primary)
                    .border(2.dp, color = MaterialTheme.colors.onPrimary, shape = CircleShape)
            )
        }
    }

    if (selectedService.value.service.isNotEmpty()) {
        Card(modifier = Modifier.layout { measurable, constraints ->
            val placeable = measurable.measure(constraints)
            val maxWidth = constraints.maxWidth
            val x = (maxWidth - placeable.width).coerceAtLeast(0)
            layout(width = placeable.width, height = placeable.height) {
                placeable.place(x, 10)
            }
        }) {
            if (editService.value) {
                newService(onCancel = {
                    coroutineScope.launch {
                        serviceModel.resetService()
                    }
                }) { service ->
                    coroutineScope.launch {
                        if (service.dirty) {
                            serviceModel.update(service)
                        }
                    }
                }
            } else {
                showService {
                    coroutineScope.launch {
                        serviceModel.resetService()
                    }
                }
            }
        }
    }

}


@Composable
private fun TableCellScope.serviceButton(service: Service, editService: MutableState<Boolean>) {
    val coroutineScope = rememberCoroutineScope()
    val serviceModel by rememberInstance<ServiceVM>()
    val navController = LocalNavigator.currentOrThrow

    Row() {
        IconButton(
            onClick = {
                editService.value = false
                serviceModel.selectService(service)
            },
            modifier = Modifier.testTag("Show ${service.service}").align(Alignment.CenterVertically)
        ) {
            Icon(
                Icons.Default.KeyboardArrowRight,
                "Show",
                tint = MaterialTheme.colors.secondary

            )
        }
        Spacer(Modifier.width(XS))
        IconButton(
            onClick = {
                coroutineScope.launch(Dispatchers.IO) {
                    serviceModel.history(service.service, true)
                    withContext(Dispatchers.Main) {
                        navController.push(KPasswordScreen.History)
                    }
                }
            },
            modifier = Modifier.testTag("History ${service.service}").align(Alignment.CenterVertically)
        ) {
            Icon(
                painterResource("/icons/history.svg"),
                "History",
                tint = MaterialTheme.colors.secondary
            )
        }
        Spacer(Modifier.width(XS))
        IconButton(
            onClick = {
                editService.value = true
                serviceModel.selectService(service)
            },
            modifier = Modifier.testTag("Edit ${service.service}").align(Alignment.CenterVertically)
        ) {
            Icon(
                Icons.Default.Edit,
                "Edit",
                tint = MaterialTheme.colors.secondary
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
                Icons.Default.Delete,
                "Delete",
                tint = MaterialTheme.colors.error

            )
        }

        showOkCancel(
            title = "Delete confirmation",
            message = "Do you want to delete service ${service.service}?",
            showAlert,
            onConfirm = {
                coroutineScope.launch(Dispatchers.IO) {
                    serviceModel.delete(service)
                }
            }
        )
    }
}

@Composable
private fun cell(service: Service, columnIndex: Int) {
    val clipboardManager = LocalClipboardManager.current
    ContextMenuDataProvider(
        items = {
            listOf(
                ContextMenuItem("Copy username") {
                    clipboardManager.setText(
                        AnnotatedString(service.username)
                    )
                },
                ContextMenuItem("Copy password") {
                    clipboardManager.setText(
                        AnnotatedString(service.password)
                    )
                }
            )
        }
    ) {
        SelectionContainer {
            when (columnIndex) {
                0 -> Text(service.tags.joinToString(", ") )
                1 -> Text(service.service)
                2 -> Text(service.username)
                3 -> Text(text = service.password.obfuscate())
                4 -> Text(
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
    }
}


@Composable
fun newService(onCancel: () -> Unit, onSubmit: (Service) -> Unit) {
    val serviceModel by rememberInstance<ServiceVM>()
    val userVM by rememberInstance<UserVM>()
    val service = remember {
        serviceModel.selectedService
    }

    val tags = remember {
        mutableStateOf(serviceModel.selectedService.value.tags.toSet())
    }

    val dirty = remember {
        mutableStateOf(false)
    }

    val readonly = remember {
        mutableStateOf(serviceModel.selectedService.value.service.isNotEmpty())
    }
    val errorMsg = remember {
        serviceModel.saveError
    }
    val clock: Clock by localDI().instance()

    tags.value = serviceModel.selectedService.value.tags.toSet()
    Row(Modifier.padding(LARGE)) {
        Column(modifier = Modifier.width(600.dp)) {
            OutlinedTextField(
                label = { Text("Service") },
                onValueChange = { value ->
                    dirty.value = dirty.value || service.value.service != value
                    service.value = service.value.copy(service = value)
                },
                readOnly = readonly.value,
                value = service.value.service,
                modifier = Modifier.fillMaxWidth()
                    .align(Alignment.CenterHorizontally)
                    .testTag("service"),
                singleLine = true,
                isError = errorMsg.value.isNotEmpty()
            )

            tagEditor(tags, onValueChange = { values ->
                val modelTags = serviceModel.selectedService.value.tags
                dirty.value = dirty.value || values != modelTags
                service.value = service.value.copy(tags = values.toList())
            })

            OutlinedTextField(
                label = { Text("Username") },
                value = service.value.username,
                onValueChange = {
                    dirty.value = dirty.value || service.value.username != it
                    service.value = service.value.copy(username = it)
                },
                modifier = Modifier.fillMaxWidth()
                    .align(Alignment.CenterHorizontally)
                    .testTag("username"),
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
                modifier = Modifier.fillMaxWidth()
                    .align(Alignment.CenterHorizontally)
                    .testTag("password"),
                singleLine = true,
                isError = errorMsg.value.isNotEmpty()
            )
            passwordDialog(showPasswordDialog) {
                service.value = service.value.copy(password = it)
                dirty.value = true
            }

            OutlinedTextField(
                label = { Text("Note") },
                value = service.value.note,
                minLines = 5,
                maxLines = 10,
                onValueChange = {
                    dirty.value = dirty.value || service.value.note != it
                    service.value = service.value.copy(note = it)
                },
                modifier = Modifier.fillMaxWidth().align(Alignment.CenterHorizontally)
                    .testTag("note")
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
                Button(
                    modifier = Modifier.testTag("submit"),
                    enabled = dirty.value,
                    onClick = {
                        if (dirty.value) {
                            val user = userVM.loggedUser
                            val newService = service.value.copy(
                                userid = user.value.userid, dirty = dirty.value,
                                updateTime = clock.currentDateTime()
                            ).trim()
                            newService.validate().onSuccess {
                                onSubmit(newService)
                            }.onFailure {
                                errorMsg.value = it.localizedMessage
                            }
                        }
                    }
                ) {
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
fun showService(onClose: () -> Unit) {
    val serviceModel by rememberInstance<ServiceVM>()
    val userVM by rememberInstance<UserVM>()
    val service = remember {
        serviceModel.selectedService
    }
    val tags = remember {
        mutableStateOf(serviceModel.selectedService.value.tags.toSet())
    }

    val clock: Clock by localDI().instance()

    tags.value = serviceModel.selectedService.value.tags.toSet()
    Row(Modifier.padding(LARGE)) {
        Column(modifier = Modifier.width(600.dp)) {
            OutlinedTextField(
                label = { Text("Service") },
                onValueChange = { },
                readOnly = true,
                value = service.value.service,
                modifier = Modifier.fillMaxWidth()
                    .align(Alignment.CenterHorizontally)
                    .testTag("service"),
                singleLine = true,
            )

            tagViewer(tags)

            OutlinedTextField(
                label = { Text("Username") },
                value = service.value.username,
                onValueChange = {},
                modifier = Modifier.fillMaxWidth()
                    .align(Alignment.CenterHorizontally)
                    .testTag("username"),
                singleLine = true,
                readOnly = true
            )

            OutlinedTextField(
                label = { Text("Password") },
                value = service.value.password,
                onValueChange = {
                },
                trailingIcon = {
                },
                modifier = Modifier.fillMaxWidth()
                    .align(Alignment.CenterHorizontally)
                    .testTag("password"),
                singleLine = true,
                readOnly = true
            )

            OutlinedTextField(
                label = { Text("Note") },
                value = service.value.note,
                minLines = 5,
                maxLines = 10,
                onValueChange = {
                },
                modifier = Modifier.fillMaxWidth().align(Alignment.CenterHorizontally)
                    .testTag("note"),
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
private fun RowScope.searchField() {
    val search = remember {
        mutableStateOf(TextFieldValue())
    }
    val coroutineScope = rememberCoroutineScope()
    val serviceVM by localDI().instance<ServiceVM>()

    OutlinedTextField(
        label = { Text("Search") },
        value = search.value,
        leadingIcon = {
            Icon(
                Icons.Default.Search,
                "Search"
            )
        },
        onValueChange = {
            search.value = it
            coroutineScope.launch {
                serviceVM.searchPattern(it.text)
            }
        },
        modifier = Modifier.testTag("Search field").align(Alignment.Bottom)
    )
}

suspend fun upload(di: DI, statusHolder: StatusHolder) {
    withContext(Dispatchers.Main) {
        val serviceVM by di.instance<ServiceVM>()
        val home = System.getProperty("user.home")
        val fileChooser = JFileChooser(home)
        fileChooser.fileFilter = FileNameExtensionFilter("Comma Separated File", "csv")
        fileChooser.showOpenDialog(null).let { result ->
            if (result == JFileChooser.APPROVE_OPTION) {
                val path = fileChooser.selectedPath()
                withContext(Dispatchers.IO) {
                    withContext(Dispatchers.Main) {
                        statusHolder.closeDrawer()
                    }
                    serviceVM.readFile(path).onSuccess {
                        statusHolder.sendMessage("CSV imported")
                    }.onFailure {
                        statusHolder.sendMessage("Error importing CSV: ${it.localizedMessage}")

                    }
                }
            }
        }
    }

}

fun CoroutineScope.download(di: DI, statusHolder: StatusHolder) {
    val exportPath = exportPath()
    val fileChooser = JFileChooser(exportPath.toFile())
    fileChooser.fileFilter = FileNameExtensionFilter("Comma Separated File", "csv")
    fileChooser.showSaveDialog(null).let { result ->
        if (result == JFileChooser.APPROVE_OPTION) {
            val path = fileChooser.selectedPath()
            launch(Dispatchers.IO) {
                LOGGER.info { "Writing in directory: $path" }
                statusHolder.closeDrawer()
                path.writer().use {
                    it.performDownload(di)
                }
                statusHolder.sendMessage("Download completed: $path")
            }
        }

    }
}

private fun JFileChooser.selectedPath() = selectedFile.toPath()