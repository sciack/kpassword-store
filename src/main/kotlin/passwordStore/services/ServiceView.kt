package passwordStore.services

import androidx.compose.foundation.ContextMenuDataProvider
import androidx.compose.foundation.ContextMenuItem
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
import passwordStore.Screen
import passwordStore.navigation.NavController
import passwordStore.tags.tagEditor
import passwordStore.users.UserVM
import passwordStore.utils.StatusHolder
import passwordStore.utils.currentDateTime
import passwordStore.utils.obfuscate
import passwordStore.widget.*
import javax.swing.JFileChooser
import javax.swing.filechooser.FileNameExtensionFilter
import kotlin.io.path.writer
import kotlin.random.Random

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterialApi::class)
@Composable
fun servicesTable() {
    val serviceModel by localDI().instance<ServiceVM>()
    val navController by localDI().instance<NavController>()
    val coroutineScope = rememberCoroutineScope()

    val services = remember {
        serviceModel.services
    }

    val selectedService = remember {
        serviceModel.selectedService
    }


    Column(modifier = Modifier.fillMaxWidth(0.9f).padding(16.dp)) {
        Row(modifier = Modifier.fillMaxWidth()) {
            searchField()
            Spacer(Modifier.width(20.dp))
            tagView()
        }
        Spacer(Modifier.height(16.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            Table(
                modifier = Modifier.fillMaxSize(),
                rowModifier = Modifier.fillMaxWidth().bottomBorder(1.dp, color = Color.LightGray),
                headers = listOf("Service", "Username", "Password", "Note"),
                values = services.toList(),
                cellContent = { columnIndex, service ->
                    cell(service, columnIndex)
                },
                beforeRow = { service ->
                    Row(modifier = Modifier.align(Alignment.CenterVertically)) {
                        IconButton(
                            onClick = {
                                coroutineScope.launch(Dispatchers.IO) {
                                    serviceModel.history(service.service, true)
                                    withContext(Dispatchers.Main) {
                                        navController.navigate(Screen.History)
                                    }
                                }
                            },
                            modifier = Modifier.testTag("History ${service.service}")
                        ) {
                            Icon(
                                painterResource("/icons/history.svg"),
                                "History",
                            )
                        }
                        IconButton(
                            onClick = {
                                serviceModel.selectService(service)
                            },
                            modifier = Modifier.testTag("Edit ${service.service}")
                        ) {
                            Icon(
                                Icons.Default.Edit,
                                "Edit",

                                )
                        }
                        val showAlert = remember {
                            mutableStateOf(false)
                        }
                        IconButton(
                            onClick = { showAlert.value = true },
                            modifier = Modifier.testTag("Delete ${service.service}")
                        ) {
                            Icon(
                                Icons.Default.Delete,
                                "Delete",

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
        }
    }

}

@Composable
fun cell(service: Service, columnIndex: Int) {
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
                0 -> Text(service.service)
                1 -> Text(service.username)
                2 -> Text(text = service.password.obfuscate())
                3 -> Text(
                    text = service.note,
                    softWrap = true,
                    overflow = TextOverflow.Clip,
                    minLines = 1,
                    maxLines = 5,
                    modifier = Modifier.widthIn(max = 350.dp)
                )
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
    Row(Modifier.padding(16.dp)) {
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
            Row(Modifier.align(Alignment.CenterHorizontally)) {
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
                Spacer(Modifier.width(16.dp))
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
fun searchField() {
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
        modifier = Modifier.testTag("Search field")
    )
}

suspend fun upload(di:DI) {
    withContext(Dispatchers.Main) {
        val serviceVM by di.instance<ServiceVM>()
        val home = System.getProperty("user.home")
        val fileChooser = JFileChooser(home)
        fileChooser.fileFilter = FileNameExtensionFilter("Comma Separated File", "csv")
        fileChooser.showOpenDialog(null).let { result ->
            if (result == JFileChooser.APPROVE_OPTION) {
                val path = fileChooser.selectedFile.toPath()
                withContext(Dispatchers.IO) {
                    withContext(Dispatchers.Main) {
                        StatusHolder.closeDrawer()
                    }
                    serviceVM.readFile(path).onSuccess {
                        StatusHolder.sendMessage("CSV imported")
                    }.onFailure {
                        StatusHolder.sendMessage("Error importing CSV: ${it.localizedMessage}")

                    }
                }
            }
        }
    }

}

fun CoroutineScope.download(di: DI) {
    val exportPath = exportPath()
    LOGGER.warn { "Writing in directory: $exportPath" }

    launch(Dispatchers.IO) {
        StatusHolder.closeDrawer()
        exportPath.writer().use {
            it.performDownload(di)
        }
        StatusHolder.sendMessage("Download completed")
    }
}
