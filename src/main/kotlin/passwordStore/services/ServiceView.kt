package passwordStore.services

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
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
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import org.kodein.di.compose.localDI
import org.kodein.di.instance
import passwordStore.LOGGER
import passwordStore.navigation.KPasswordScreen
import passwordStore.services.ServiceSM.State.*
import passwordStore.services.ServicesSM.State.Loading
import passwordStore.services.ServicesSM.State.Services
import passwordStore.ui.theme.*
import passwordStore.users.LocalUser
import passwordStore.utils.currentDateTime
import passwordStore.utils.obfuscate
import passwordStore.widget.*

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
            val (selectedTags, setTags) = remember {
                serviceSM.tags
            }
            tagView(tags, selectedTags) { tag ->
                setTags(tag)
                coroutineScope.launch(Dispatchers.IO) {
                    serviceSM.searchWithTags(tag, user = user)
                }
            }
        }

        Spacer(Modifier.height(LARGE))
        Row(modifier = Modifier.fillMaxWidth().fillMaxHeight(0.9f)) {
            if (state is Loading) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    CircularProgressIndicator(
                        modifier = Modifier.width(32.dp).align(Alignment.CenterHorizontally),
                        color = MaterialTheme.colorScheme.surface,
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
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(XL * 2).clip(CircleShape).background(color = MaterialTheme.colorScheme.primary)
                    .border(2.dp, color = MaterialTheme.colorScheme.onPrimary, shape = CircleShape)
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
                Icons.Default.KeyboardArrowRight, "Show", tint = MaterialTheme.colorScheme.secondary

            )
        }
        Spacer(Modifier.width(XS))
        IconButton(
            onClick = {
                navController.push(KPasswordScreen.ServiceHistory(service))
            }, modifier = Modifier.testTag("History ${service.service}").align(Alignment.CenterVertically)
        ) {
            Icon(
                painterResource("/icons/history.svg"), "History", tint = MaterialTheme.colorScheme.secondary
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
                Icons.Default.Edit, "Edit", tint = MaterialTheme.colorScheme.secondary
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
                Icons.Default.Delete, "Delete", tint = MaterialTheme.colorScheme.error

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

@OptIn(ExperimentalFoundationApi::class)
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
                    color = MaterialTheme.colorScheme.secondary, textDecoration = TextDecoration.Underline
                )
            ) {
                append(service.url)
            }
            toAnnotatedString()
        }, onClick = {
            LOGGER.warn("Try to open url ${service.url}")
            localUrl.openUri(service.url)
        })

        4 -> Text(service.tags.joinToString(", "))
        5 -> TooltipArea(tooltip = {
            Row(
                Modifier.background(MaterialTheme.colorScheme.surface)
                    .border(1.dp, color = MaterialTheme.colorScheme.onSurface, shape = RoundedCornerShape(4.dp))
            ) {
                Text(
                    text = service.note,
                    softWrap = true,
                    overflow = TextOverflow.Ellipsis,
                    minLines = 1,
                    maxLines = 5,
                    modifier = Modifier.widthIn(max = 350.dp).padding(LARGE)
                )
            }
        }
        ) {

            Text(
                text = service.note,
                softWrap = true,
                overflow = TextOverflow.Ellipsis,
                minLines = 1,
                maxLines = 2,
                modifier = Modifier.widthIn(max = 350.dp)
            )
        }

        else -> Text("")

    }

}


@Composable
fun newService(createServiceSM: CreateServiceSM) {
    val navController = LocalNavigator.currentOrThrow

    val coroutineScope = rememberCoroutineScope()
    Box(
        Modifier.fillMaxSize()
    ) {
        ElevatedCard(
            Modifier.align(Alignment.Center)
                .fillMaxHeight(0.8f)
        ) {
            editService(createServiceSM.saveError, title = "New service", onCancel = { navController.pop() }) {
                coroutineScope.launch(Dispatchers.IO) {
                    createServiceSM.store(it).onSuccess {
                        withContext(Dispatchers.Main) {
                            navController.push(KPasswordScreen.Home)
                        }
                    }

                }
            }
        }
    }
}

@Composable
fun editService(
    saveError: MutableState<String>,
    title: String,
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

    ScrollableView(
        title = title,
        onOk = {
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
        }, okEnabled = dirty.value, onCancel = onCancel
    ) {
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
            PasswordGenerationPane(showPasswordDialog) {
                service.value = service.value.copy(password = it)
                dirty.value = true
            }

            OutlinedTextField(label = { Text("Url") }, value = service.value.url, singleLine = true, onValueChange = {
                dirty.value = dirty.value || service.value.url != it
                service.value = service.value.copy(url = it)
            }, modifier = Modifier.fillMaxWidth().align(Alignment.CenterHorizontally).testTag("url")
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
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold,
                        fontSize = TextUnit(0.8f, TextUnitType.Em),
                        modifier = Modifier.testTag("ErrorMsg")
                    )
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

            ClickableText(buildAnnotatedString {
                withStyle(
                    style = SpanStyle(
                        color = MaterialTheme.colorScheme.secondary, textDecoration = TextDecoration.Underline
                    )
                ) {
                    append(service.url)
                }
                toAnnotatedString()
            }, onClick = {
                LOGGER.warn("Try to open url ${service.url}")
                localUrl.openUri(service.url)
            }, modifier = Modifier.padding(LARGE)
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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
private fun RowScope.searchField(serviceSM: ServicesSM) {
    val user = LocalUser.currentOrThrow

    val search = remember {
        serviceSM.pattern
    }
    val active = remember {
        mutableStateOf(false)
    }
    val cleanSearch = {
        active.value = false
        search.value = ""
    }
    SearchBar(
        query = search.value,
        active = active.value,
        onQueryChange = { query ->
            search.value = query
            active.value = true
            serviceSM.searchFastPattern(query, user)

        },
        onActiveChange = {},
        placeholder = { Text("Search") },
        leadingIcon = {
            if (active.value) {
                IconButton(
                    onClick = {
                        cleanSearch()
                    }
                ) {
                    Icon(
                        Icons.Default.ArrowBack,
                        contentDescription = null
                    )
                }
            } else {
                Icon(Icons.Default.Search, contentDescription = null)
            }
        },
        trailingIcon = {
            if (search.value.isNotEmpty()) {
                IconButton(
                    onClick = {
                        cleanSearch()

                        serviceSM.fetchAll(user)

                    }
                ) {
                    Icon(
                        Icons.Default.Undo,
                        contentDescription = null
                    )
                }
            }
        },
        modifier = Modifier.testTag("Search field").onKeyEvent { event ->
            if (event.key == Key.Escape) {
                cleanSearch()
                false
            } else {
                true
            }
        },
        onSearch = { query ->
            active.value = false
            serviceSM.searchPattern(query, user)

        }) {
        val suggestion = remember { serviceSM.suggestion }
        suggestion.forEach { service ->
            val pattern = service.service
            ListItem(
                headlineContent = { Text(pattern) },
                supportingContent = { Text(service.username) },
                leadingContent = { Icon(Icons.Filled.Star, contentDescription = null) },
                modifier = Modifier
                    .clickable {
                        search.value = pattern
                        active.value = false
                        serviceSM.searchPattern(pattern, user)

                    }
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
            )
        }

    }
}


class ShowServiceScreen(
    private val action: MutableStateFlow<ServiceAction>, private val onChange: suspend () -> Unit
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
                editService(
                    screenModel.saveError,
                    title = "Edit service",
                    selectedService = (state as EditService).service,
                    onCancel = {
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
                Text(
                    "Service",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.secondary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(MEDIUM)
                )
                showService(selectedService = (state as ShowService).service) {
                    close()
                }
            }
        }
    }
}