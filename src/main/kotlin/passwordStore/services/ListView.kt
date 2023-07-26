package passwordStore.services

import androidx.compose.foundation.ContextMenuDataProvider
import androidx.compose.foundation.ContextMenuItem
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.onClick
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.Button
import androidx.compose.material.Icon
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toJavaLocalDateTime
import kotlinx.datetime.toLocalDateTime
import org.kodein.di.compose.localDI
import org.kodein.di.instance
import passwordStore.Screen
import passwordStore.navigation.NavController
import passwordStore.tags.tagEditor
import passwordStore.widget.Table
import passwordStore.widget.bottomBorder
import passwordStore.widget.tagView

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun servicesTable() {
    val serviceModel by localDI().instance<Services>()
    val navController by localDI().instance<NavController>()
    val services = remember {
        serviceModel.services
    }

    Column(Modifier.fillMaxSize(.9f)) {
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
                    val service = services.value[rowIndex]
                    displayService(service, columnIndex)
                },
                beforeRow = { row ->
                    val service = services.value[row]
                    Row {
                        Icon(Icons.Default.Edit,
                            "Edit",
                            modifier = Modifier.onClick {
                                navController.navigate(Screen.Details(service))
                            })
                    }
                }
            )
        }
    }
}

@Composable
fun displayService(service: Service, columnIndex: Int) {
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
                2 -> Text(text = "****")
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
fun newService(originalService: Service = Service(), onSubmit: (Service) -> Unit) {
    val serviceModel by localDI().instance<Services>()
    val navController by localDI().instance<NavController>()

    val service = remember {
        mutableStateOf(TextFieldValue(originalService.service))
    }
    val username = remember {
        mutableStateOf(TextFieldValue(originalService.username))
    }
    val password = remember {
        mutableStateOf(TextFieldValue(originalService.password))
    }
    val note = remember {
        mutableStateOf(TextFieldValue(originalService.note))
    }
    val dirty = remember {
        mutableStateOf(false)
    }
    val tags = remember {
        mutableStateOf(originalService.tags.toSet())
    }

    val clock: Clock by localDI().instance()
    Row(Modifier.padding(16.dp)) {
        Column(modifier = Modifier.width(600.dp)) {
            OutlinedTextField(
                label = { Text("Service") },
                onValueChange = {
                    dirty.value = dirty.value || service.value != it
                    service.value = it
                },
                readOnly = originalService.service.isNotEmpty(),
                value = service.value,
                modifier = Modifier.fillMaxWidth()
                    .align(Alignment.CenterHorizontally)
                    .testTag("service"),
                singleLine = true
            )

            tagEditor(tags, onValueChange = { values ->
                dirty.value = dirty.value || values != originalService.tags
            })

            OutlinedTextField(
                label = { Text("Username") },
                value = username.value,
                onValueChange = {
                    dirty.value = dirty.value || username.value != it
                    username.value = it
                },
                modifier = Modifier.fillMaxWidth()
                    .align(Alignment.CenterHorizontally)
                    .testTag("username"),
                singleLine = true
            )


            OutlinedTextField(
                label = { Text("Password") },
                value = password.value,
                onValueChange = {
                    dirty.value = dirty.value || password.value != it
                    password.value = it
                },
                modifier = Modifier.fillMaxWidth()
                    .align(Alignment.CenterHorizontally)
                    .testTag("password"),
                singleLine = true
            )


            OutlinedTextField(
                label = { Text("Note") },
                value = note.value,
                minLines = 5,
                maxLines = 10,
                onValueChange = {
                    dirty.value = dirty.value || note.value != it
                    note.value = it
                },
                modifier = Modifier.fillMaxWidth().align(Alignment.CenterHorizontally)
                    .testTag("note")
            )
            Row(Modifier.align(Alignment.CenterHorizontally)) {
                Button(
                    modifier = Modifier.testTag("submit"),
                    onClick = {
                        val user = serviceModel.user
                        val newService = if (originalService.service.isEmpty()) {
                            Service(
                                service = service.value.text,
                                username = username.value.text,
                                password = password.value.text,
                                note = note.value.text,
                                userid = user.userid,
                                tags = tags.value.toList(),
                                updateTime = clock.now().toLocalDateTime(TimeZone.currentSystemDefault())
                                    .toJavaLocalDateTime()
                            )
                        } else {
                            originalService.copy(
                                username = username.value.text,
                                password = password.value.text,
                                note = note.value.text,
                                userid = user.userid,
                                tags = tags.value.toList(),
                                dirty = dirty.value,
                                updateTime = clock.now().toLocalDateTime(TimeZone.currentSystemDefault())
                                    .toJavaLocalDateTime()
                            )
                        }
                        onSubmit(newService)
                    }
                ) {
                    Text("Submit")
                }
                Spacer(Modifier.width(16.dp))
                Button(onClick = {
                    navController.navigateBack()
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
    val services by localDI().instance<Services>()

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
            services.searchPattern(it.text)
        },
        modifier = Modifier.testTag("Search field")
    )
}