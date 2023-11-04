package passwordStore.widget

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.TooltipArea
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import passwordStore.ui.theme.LARGE
import passwordStore.ui.theme.MEDIUM
import passwordStore.ui.theme.SMALL
import passwordStore.utils.obfuscate
import kotlin.random.Random

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun passwordToolTip(password: String, color: Color = MaterialTheme.colorScheme.onBackground) {
    val newPwd = remember { password }
    TooltipArea(tooltip = {
        Row(
            Modifier.background(MaterialTheme.colorScheme.surface)
                .border(2.dp, color = MaterialTheme.colorScheme.onSurface, shape = RoundedCornerShape(4.dp))
        ) {
            Text(
                newPwd,
                modifier = Modifier.padding(MEDIUM)
            )
        }
    }) {
        Text(newPwd.obfuscate(), color = color)
    }
}


@Composable
fun PasswordGenerationPane(showDialog: MutableState<Boolean>, onSelect: (String) -> Unit) {
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
        Column(Modifier.zIndex(99f).padding(MEDIUM)) {
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
                        if (value.isEmpty()) {
                            null
                        } else {
                            value.toInt()
                        }
                    }.onSuccess {
                        if (it in 1..50) {
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
                trailingIcon = {
                    IconButton(
                        onClick = {
                            password.value =
                                generatePassword(
                                    length.value.toIntOrNull() ?: 0,
                                    useUpperLowerCase.value,
                                    useNumber.value,
                                    useSymbols.value
                                )
                        }
                    ) {
                        Icon(Icons.Default.Refresh, null)
                    }

                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.align(Alignment.Start)
            )
            Row(Modifier.align(Alignment.Start).padding(top = SMALL)) {
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
            Row(Modifier.align(Alignment.Start).padding(top = SMALL)) {
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
            Row(Modifier.align(Alignment.Start).padding(top = SMALL)) {
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

            Row(Modifier.padding(top = SMALL)) {
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
    if (length <= 0) {
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
            append(SYMBOLS)
        }
    }
    var pwd = ""
    (1..1000).forEach { _ ->
        pwd = (1..length).map {
            val pos = Random.nextInt(charString.length)
            charString[pos]
        }.joinToString("")
        if (pwd.valid(upperCase, number, symbols)) {
            return@forEach
        }
    }
    return pwd
}

private fun String.valid(upperCase: Boolean, number: Boolean, symbols: Boolean): Boolean {
    return (!upperCase || this.hasUppercase())
            && (!number || this.hasNumber())
            && (!symbols || this.hasSpecialChar())
            && this.any { it.isLowerCase() }
}


fun String.hasNumber() = this.any { it.isDigit() }
fun String.hasUppercase() = this.any { it.isUpperCase() }

fun String.hasSpecialChar() = this.any { it in SYMBOLS }

private const val SYMBOLS = "@#$%&_-+,.:;!"