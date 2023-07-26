package passwordStore

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toJavaLocalDateTime
import kotlinx.datetime.toLocalDateTime
import org.kodein.di.compose.localDI
import org.kodein.di.instance
import passwordStore.audit.Event
import passwordStore.navigation.NavController
import passwordStore.tags.tagEditor
import passwordStore.widget.tagView
import passwordStore.users.User
import passwordStore.widget.Table

@Composable
fun servicesTable() {
    val serviceModel by localDI().instance<Services>()
    val navController by localDI().instance<NavController>()
    val services = remember {
        serviceModel.services
    }

    val state = rememberLazyListState()
    Column(Modifier.fillMaxSize()) {
        Row(modifier = Modifier.fillMaxWidth()) {
            tagView()
        }
        Spacer(Modifier.height(15.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            Table(
                modifier = Modifier.fillMaxSize(),
                rowModifier = Modifier.fillMaxWidth(),
                columnCount = 4,
                rowCount = services.value.size,
                headers = listOf("Service", "Username", "Password", "Note"),
                cellContent = {columnIndex, rowIndex ->
                    val service = services.value[rowIndex]
                    displayService(service, columnIndex)
                }
            )
        }
    }
}

@Composable
fun displayService(service: Service, columnIndex: Int) {
    when(columnIndex) {
        0 -> Text(service.service)
        1 -> Text(service.username)
        2 -> Text(service.password)
        3 -> Text(
            text = service.note,
            softWrap = true,
            overflow = TextOverflow.Clip,
            minLines = 1,
            maxLines = 5
        )
    }
}

@Composable
fun historyTable(historyEvents: List<Event>) {
    Box(
        modifier = Modifier.fillMaxWidth()
            .padding(10.dp)
    ) {
        val state = rememberLazyListState()

        LazyColumn(Modifier.fillMaxWidth().padding(end = 12.dp), state) {
            items(historyEvents) { event ->
                renderService(event.service) {}
                Spacer(modifier = Modifier.height(5.dp))
            }
        }
        VerticalScrollbar(
            modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight(),
            adapter = rememberScrollbarAdapter(
                scrollState = state
            )
        )
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalFoundationApi::class)
@Composable
fun renderService(service: Service, clickEvent: () -> Unit) {
    val clipboardManager = LocalClipboardManager.current
    Column(
        Modifier.border(width = 1.dp, color = Color.DarkGray, shape = RoundedCornerShape(3.dp))
            .padding(5.dp).fillMaxWidth()
    ) {
        FlowRow(
            Modifier.fillMaxWidth()
        ) {
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
                    Row {
                        Column {
                            Row {
                                Icon(Icons.Default.Edit,
                                    contentDescription = "Edit",
                                    modifier = Modifier.onClick {
                                        clickEvent()
                                    })
                            }
                        }
                        Column {
                            Row {
                                Text("Service:", fontWeight = FontWeight.Bold)
                                Spacer(Modifier.width(5.dp))
                                Text(service.service)
                                Spacer(Modifier.width(15.dp))
                                Text("Username:", fontWeight = FontWeight.Bold)
                                Spacer(Modifier.width(5.dp))
                                Text(service.username)
                                Spacer(Modifier.width(15.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun newService( originalService: Service = Service(), onSubmit: (Service) -> Unit) {
    val serviceModel by localDI().instance<Services>()
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
    Row() {
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

            Button(
                modifier = Modifier.align(Alignment.CenterHorizontally).testTag("submit"),
                onClick = {
                    val user = serviceModel.user
                    val service = if (originalService.service.isEmpty()) {
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
                    onSubmit(service)
                }
            ) {
                Text("Submit")
            }
        }
    }
}


