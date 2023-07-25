package passwordStore

import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toJavaLocalDateTime
import kotlinx.datetime.toLocalDateTime
import org.kodein.di.compose.localDI
import org.kodein.di.instance
import passwordStore.audit.Event
import passwordStore.navigation.NavController
import passwordStore.users.User
import passwordStore.utils.tagEditor

@Composable
fun servicesTable( navController: NavController) {
    val serviceModel by localDI().instance<Services>()

    val services = remember {
        serviceModel.services
    }

    Box(
        modifier = Modifier.fillMaxWidth()
            .padding(10.dp)
    ) {
        val state = rememberLazyListState()

        LazyColumn(Modifier.fillMaxWidth().padding(end = 12.dp), state) {
            items(services.value) { service ->
                renderService(service) {
                    navController.navigate(Screen.Details(service))
                }
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

@Composable
fun historyTable(historyEvents: List<Event>, navController: NavController) {
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun renderService(service: Service, clickEvent: () -> Unit) {
    Column(
        Modifier.border(width = 1.dp, color = Color.DarkGray, shape = RoundedCornerShape(3.dp))
            .padding(5.dp).fillMaxWidth()
    ) {
        FlowRow(
            Modifier.clickable(
                onClick = clickEvent
            ).fillMaxWidth()
        ) {
            Text("Service:", fontWeight = FontWeight.Bold)
            Spacer(Modifier.width(5.dp))
            Text(service.service)
            Spacer(Modifier.width(15.dp))
            Text("Username", fontWeight = FontWeight.Bold)
            Spacer(Modifier.width(5.dp))
            Text(service.username)
            Spacer(Modifier.width(15.dp))
        }
    }
}


@Composable
fun newService(user: User, originalService: Service = Service(), onSubmit: (Service) -> Unit) {
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


