package passwordStore.widget

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import passwordStore.ui.theme.LARGE
import kotlin.random.Random

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun showOkCancel(
    title: String = "Alert", message: String, show: MutableState<Boolean>, onConfirm: () -> Unit = {}
) {

    if (show.value) {
        AlertDialog(text = {
            Text(message)
        }, onDismissRequest = {}, title = {
            Text(
                title, fontWeight = FontWeight.Bold
            )
        }, dismissButton = {
            Button(onClick = {
                show.value = false
            }) {
                Text("Cancel")
            }
        }, confirmButton = {
            Button(onClick = {
                show.value = false
                onConfirm()
            }) {
                Text("OK")
            }
        })
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun showOk(
    title: String = "Error", message: String, show: MutableState<Boolean>
) {

    if (show.value) {
        AlertDialog(text = {
            Text(message)
        }, onDismissRequest = {}, title = {
            Text(
                title, fontWeight = FontWeight.Bold
            )
        }, confirmButton = {
            Button(onClick = {
                show.value = false
            }) {
                Text("OK")
            }
        })
    }

}

@Composable
fun passwordDialog(showDialog: MutableState<Boolean>, onSelect: (String) -> Unit) {
    val length = remember {
        mutableStateOf("8")
    }
    val useNumber = remember {
        mutableStateOf(true)
    }
    val useSymbols = remember {
        mutableStateOf(true)
    }
    val useUpperLowerCase = remember {
        mutableStateOf(true)
    }
    val password = remember {
        mutableStateOf(
            generatePassword(
                length.value.toInt(),
                useUpperLowerCase.value,
                useNumber.value,
                useSymbols.value
            )
        )
    }
    if (showDialog.value) {
        Column(Modifier.zIndex(99f)) {
            Row {
                Text("Password: ")
                Text(
                    password.value,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(Modifier.height(LARGE))
            OutlinedTextField(
                value = length.value,
                onValueChange = { value ->
                    runCatching {
                        if (value.isNullOrEmpty()) {
                            null
                        } else {
                            value.toInt()
                        }
                    }.onSuccess {
                        if (it in 1..30) {
                            length.value = value
                        } else {
                            length.value = ""
                        }
                        password.value =
                            generatePassword(
                                length.value.toIntOrNull() ?: 0,
                                useUpperLowerCase.value,
                                useNumber.value,
                                useSymbols.value
                            )
                    }
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.align(Alignment.Start)
            )
            Row(Modifier.align(Alignment.Start)) {
                Checkbox(
                    useNumber.value,
                    onCheckedChange = {
                        useNumber.value = it
                        password.value =
                            generatePassword(
                                length.value.toIntOrNull() ?: 0,
                                useUpperLowerCase.value,
                                useNumber.value,
                                useSymbols.value
                            )
                    },
                    modifier = Modifier.testTag("number")
                )
                Spacer(Modifier.width(LARGE))
                Text(
                    "Use number",
                    modifier = Modifier.align(Alignment.CenterVertically)
                )
            }
            Row(Modifier.align(Alignment.Start)) {
                Checkbox(
                    useSymbols.value,
                    onCheckedChange = {
                        useSymbols.value = it
                        password.value =
                            generatePassword(
                                length.value.toIntOrNull() ?: 0,
                                useUpperLowerCase.value,
                                useNumber.value,
                                useSymbols.value
                            )
                    },
                    modifier = Modifier.testTag("symbols")
                )
                Spacer(Modifier.width(LARGE))
                Text(
                    "Use symbols",
                    modifier = Modifier.align(Alignment.CenterVertically)
                )
            }
            Row(Modifier.align(Alignment.Start)) {
                Checkbox(
                    useUpperLowerCase.value,
                    onCheckedChange = {
                        useUpperLowerCase.value = it
                        password.value =
                            generatePassword(
                                length.value.toIntOrNull() ?: 0,
                                useUpperLowerCase.value,
                                useNumber.value,
                                useSymbols.value
                            )
                    },
                    modifier = Modifier.testTag("case")
                )
                Spacer(Modifier.width(LARGE))
                Text(
                    "Use upper/lower case",
                    modifier = Modifier.align(Alignment.CenterVertically)
                )
            }

            Row {
                Button(onClick = {
                    showDialog.value = false
                    onSelect(password.value)
                }) {
                    Text("Confirm")
                }
                Spacer(Modifier.width(LARGE))
                Button(onClick = {
                    showDialog.value = false
                }) {
                    Text("Cancel")
                }
            }

        }
    }

}

fun generatePassword(length: Int, upperCase: Boolean, number: Boolean, symbols: Boolean): String {
    if (length == 0) {
        return ""
    }
    val charString = buildString {
        append(('a'..'z').joinToString(separator = ""))
        if (upperCase) {
            append(('A'..'Z').joinToString(separator = ""))
        }
        if (number) {
            append(('0'..'9').joinToString(separator = ""))
        }
        if (symbols) {
            append("@#$%&_-+,.:;!")
        }
    }
    return (1..length).map {
        val pos = Random.nextInt(charString.length)
        charString[pos]
    }.joinToString("")
}