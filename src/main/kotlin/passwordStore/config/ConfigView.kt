package passwordStore.config

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import passwordStore.ui.theme.*
import passwordStore.users.LocalUser
import passwordStore.users.admin

@Composable
fun configView(configVM: ConfigVM) {
    val coroutineScope = rememberCoroutineScope()
    val user = LocalUser.current
    val jdbc = remember {
        mutableStateOf(TextFieldValue(configVM.jdbcUrl))
    }
    val secret = remember {
        mutableStateOf(TextFieldValue(configVM.secret))
    }
    val dbPassword = remember {
        mutableStateOf(TextFieldValue(configVM.dbPassword))
    }
    val ivSpec = remember {
        mutableStateOf(TextFieldValue(configVM.ivSpec))
    }
    val darkMode = remember {
        configVM.darkMode
    }
    val navController = LocalNavigator.currentOrThrow
    Column(modifier = Modifier.fillMaxWidth(0.9f).padding(LARGE)) {
        Row {
            Text(
                "Dark mode",
                Modifier.padding(end = SMALL)
            )
            RadioButton(darkMode.value == DarkModes.LIGHT,
                onClick = {
                    configVM.darkMode.value = DarkModes.LIGHT
                    darkMode.value = DarkModes.LIGHT
                })
            Text(
                "Light",
                modifier = Modifier.padding(start = SMALL, end = LARGE)
            )

            RadioButton(darkMode.value == DarkModes.DARK,
                onClick = {
                    configVM.darkMode.value = DarkModes.DARK
                    darkMode.value = DarkModes.DARK
                })
            Text(
                "Dark",
                modifier = Modifier.padding(start = SMALL, end = LARGE)
            )
            RadioButton(darkMode.value == DarkModes.SYSTEM_DEFAULT,
                onClick = {
                    configVM.darkMode.value = DarkModes.SYSTEM_DEFAULT
                    darkMode.value = DarkModes.SYSTEM_DEFAULT
                })
            Text(
                "System Default",
                modifier = Modifier.padding(start = SMALL, end = LARGE)
            )
        }
        Divider()
        Row(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = jdbc.value,
                onValueChange = { value: TextFieldValue ->
                    configVM.jdbcUrl = value.text
                    jdbc.value = value
                },
                label = { Text("Jdbc Url") },
                readOnly = !user.admin()
            )
        }
        Row(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = dbPassword.value,
                onValueChange = { value: TextFieldValue ->
                    configVM.dbPassword = value.text
                    dbPassword.value = value
                },
                label = { Text("Database Password") },
                readOnly = !user.admin()
            )
        }
        Row(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = secret.value,
                onValueChange = { value: TextFieldValue ->
                    configVM.secret = value.text
                    secret.value = value
                },
                label = { Text("Secret") },
                readOnly = !user.admin()
            )
        }
        Row(modifier = Modifier.fillMaxWidth()) {
            Text(
                "Changing the secret could make the database not usable anymore and require a reload of all the data, should be done with an empty database",
                fontWeight = FontWeight.Light,
                fontStyle = FontStyle.Italic,
                style = MaterialTheme.typography.bodySmall
            )
        }
        Spacer(Modifier.height(XL))
        Row(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = ivSpec.value,
                onValueChange = { value: TextFieldValue ->
                    val text = if (value.text.length > 16) {
                        value.text.substring(0 until 16)
                    } else {
                        value.text
                    }
                    configVM.ivSpec = text
                    ivSpec.value = value.copy(text)
                },
                isError = ivSpec.value.text.toByteArray().size != 16,
                label = { Text("IV spec string") },
                readOnly = !user.admin()
            )
        }
        Row(modifier = Modifier.fillMaxWidth()) {
            Text(
                "Must be 16 byte string",
                fontWeight = FontWeight.Light,
                fontStyle = FontStyle.Italic,
                style = MaterialTheme.typography.bodySmall
            )
        }
        Spacer(Modifier.height(XL))
        Row(modifier = Modifier.fillMaxWidth()) {
            val setDarkMode = LocalSetDarkMode.current
            Button(
                onClick = {
                    coroutineScope.launch {
                        configVM.save()
                        withContext(Dispatchers.Main) {
                            setDarkMode(darkMode.value)
                            navController.pop()
                        }
                    }
                }
            ) {
                Text("Save")
            }
            Spacer(Modifier.width(MEDIUM))
            Button(
                onClick = {
                    configVM.reset()
                    navController.pop()
                }
            ) {
                Text("Cancel")
            }
        }


    }
}