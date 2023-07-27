package passwordStore.services

import androidx.compose.foundation.ContextMenuDataProvider
import androidx.compose.foundation.ContextMenuItem
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.onClick
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import org.kodein.di.compose.localDI
import org.kodein.di.instance
import passwordStore.Screen
import passwordStore.navigation.NavController
import passwordStore.tags.tagEditor
import passwordStore.utils.currentTime
import passwordStore.widget.Table
import passwordStore.widget.bottomBorder
import passwordStore.widget.tagView
import kotlin.random.Random

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterialApi::class)
@Composable
fun servicesTable() {
    val serviceModel by localDI().instance<ServiceViewModel>()
    val navController by localDI().instance<NavController>()
    val coroutineScope by localDI().instance<CoroutineScope>()

    val services = remember {
        serviceModel.services
    }

    val selectedService = remember {
        serviceModel.selectedService
    }


    Column(modifier = Modifier.fillMaxWidth(0.9f).padding(end= 16.dp)) {
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
                rowCount = services.value.size,
                headers = listOf("Service", "Username", "Password", "Note"),
                cellContent = { columnIndex, rowIndex ->
                    if (rowIndex >= services.value.size) {
                        return@Table
                    }
                    val service = services.value[rowIndex]
                    cell(service, columnIndex)
                },
                beforeRow = { row ->
                    if (row >= services.value.size) {
                        return@Table
                    }
                    val service = services.value[row]
                    Row {

                        val showDeleteAlert = remember {
                            mutableStateOf(false)
                        }
                        Icon(Icons.Default.List,
                            "History",
                            modifier = Modifier.onClick {
                                coroutineScope.launch(Dispatchers.IO) {
                                    serviceModel.history(service.service, true)
                                    withContext(Dispatchers.Main) {
                                        navController.navigate(Screen.History)
                                    }
                                }

                            })
                        Icon(Icons.Default.Edit,
                            "Edit",
                            modifier = Modifier.onClick {
                                serviceModel.selectService(service)
                            })

                        Icon(Icons.Default.Delete,
                            "Delete",
                            modifier = Modifier.onClick {
                                showDeleteAlert.value = true
                            }
                        )
                        if (showDeleteAlert.value) {
                            AlertDialog(
                                text = {
                                    Text("Do you want to delete service ${service.service}?")
                                },
                                onDismissRequest = {},
                                title = {
                                    Text(
                                        "Delete confirmation", fontWeight = FontWeight.Bold
                                    )
                                },
                                dismissButton = {
                                    Button(onClick = {
                                        showDeleteAlert.value = false
                                    }) {
                                        Text("Cancel")
                                    }
                                },
                                confirmButton = {
                                    Button(onClick = {
                                        serviceModel.delete(service)
                                        showDeleteAlert.value = false
                                    }) {
                                        Text("OK")
                                    }
                                }
                            )


                        }
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
            newService { service ->
                coroutineScope.launch {
                    if (service.dirty) {
                        serviceModel.update(service)
                        withContext(Dispatchers.Main) {
                            selectedService.value = Service()
                        }
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

private fun String.obfuscate(): String {
    val numStar = Random.nextInt(this.length / 2, this.length * 2)
    return "*".repeat(numStar)
}


@Composable
fun newService(onSubmit: (Service) -> Unit) {
    val serviceModel by localDI().instance<ServiceViewModel>()
    val navController by localDI().instance<NavController>()

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
                singleLine = true
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
                singleLine = true
            )


            OutlinedTextField(
                label = { Text("Password") },
                value = service.value.password,
                onValueChange = {
                    dirty.value = dirty.value || service.value.password != it
                    service.value = service.value.copy(password = it)
                },
                modifier = Modifier.fillMaxWidth()
                    .align(Alignment.CenterHorizontally)
                    .testTag("password"),
                singleLine = true
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
                modifier = Modifier.fillMaxWidth().align(Alignment.CenterHorizontally)
                    .testTag("note")
            )
            Row(Modifier.align(Alignment.CenterHorizontally)) {
                Button(
                    modifier = Modifier.testTag("submit"),
                    onClick = {
                        val user = serviceModel.user
                        val newService = service.value.copy(
                            userid = user.userid, dirty = dirty.value,
                            updateTime = clock.currentTime()
                        )
                        onSubmit(newService)
                    }
                ) {
                    Text("Submit")
                }
                Spacer(Modifier.width(16.dp))
                Button(onClick = {
                    if (readonly.value) {
                        serviceModel.resetService()
                    } else {
                        navController.navigateBack()
                    }
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
    val serviceViewModel by localDI().instance<ServiceViewModel>()

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
            serviceViewModel.searchPattern(it.text)
        },
        modifier = Modifier.testTag("Search field")
    )
}