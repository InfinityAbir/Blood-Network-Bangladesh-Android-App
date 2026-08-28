package com.bloodnetwork.bangladesh.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.bloodnetwork.bangladesh.ui.theme.BloodRed

@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
) {
    Button(
        onClick = onClick,
        modifier = modifier.fillMaxWidth().heightIn(min = 50.dp),
        enabled = enabled && !loading,
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(containerColor = BloodRed),
    ) {
        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.heightIn(max = 22.dp),
                strokeWidth = 2.dp,
                color = MaterialTheme.colorScheme.onPrimary,
            )
        } else {
            Text(text)
        }
    }
}

@Composable
fun ErrorText(message: String?, modifier: Modifier = Modifier) {
    if (!message.isNullOrBlank()) {
        Text(
            text = message,
            color = MaterialTheme.colorScheme.error,
            modifier = modifier.padding(vertical = 4.dp),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LabeledTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Next,
    singleLine: Boolean = true,
    maxLines: Int = if (singleLine) 1 else 3,
    isError: Boolean = false,
    isPassword: Boolean = false,
    leadingIcon: (@Composable () -> Unit)? = null,
    visualTransformation: VisualTransformation = VisualTransformation.None,
) {
    var passwordVisible by remember { mutableStateOf(false) }

    val transformation = if (isPassword && !passwordVisible) {
        androidx.compose.ui.text.input.PasswordVisualTransformation()
    } else {
        visualTransformation
    }

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        placeholder = if (placeholder.isNotEmpty()) { { Text(placeholder) } } else null,
        modifier = modifier.fillMaxWidth(),
        keyboardOptions = KeyboardOptions(
            keyboardType = if (isPassword) KeyboardType.Password else keyboardType,
            imeAction = imeAction,
            capitalization = KeyboardCapitalization.None,
        ),
        singleLine = singleLine,
        maxLines = maxLines,
        isError = isError,
        leadingIcon = leadingIcon,
        trailingIcon = if (isPassword) {
            {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                        contentDescription = if (passwordVisible) "Hide password" else "Show password",
                    )
                }
            }
        } else null,
        visualTransformation = transformation,
    )
}

@Composable
fun BloodGroupChips(
    options: List<String>,
    selected: String?,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            val visible = options.take(4)
            visible.forEach { label ->
                FilterChip(
                    selected = selected == label,
                    onClick = { onSelect(label) },
                    label = { Text(label) },
                )
            }
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
        ) {
            options.drop(4).forEach { label ->
                FilterChip(
                    selected = selected == label,
                    onClick = { onSelect(label) },
                    label = { Text(label) },
                )
            }
        }
    }
}

@Composable
fun LoadingBox(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        CircularProgressIndicator(color = BloodRed)
    }
}

@Composable
fun <T> RowChips(
    options: List<T>,
    selected: T,
    labelOf: (T) -> String,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        options.forEach { option ->
            FilterChip(
                selected = selected == option,
                onClick = { onSelect(option) },
                label = { Text(labelOf(option)) },
            )
        }
    }
}

@Composable
fun RoleBadge(status: String, modifier: Modifier = Modifier) {
    val (text, color) = when (status.lowercase()) {
        "available" -> "Available" to com.bloodnetwork.bangladesh.ui.theme.AvailableGreen
        "recentlydonated" -> "Recently Donated" to com.bloodnetwork.bangladesh.ui.theme.RecentlyDonatedAmber
        "unavailable" -> "Unavailable" to com.bloodnetwork.bangladesh.ui.theme.UnavailableGray
        else -> status to MaterialTheme.colorScheme.secondary
    }
    Text(
        text = text,
        color = androidx.compose.ui.graphics.Color.White,
        style = MaterialTheme.typography.labelMedium,
        modifier = modifier
            .background(color, RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp),
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PickerField(
    label: String,
    options: List<String>,
    onSelect: (String) -> Unit,
) {
    var query by remember { mutableStateOf("") }
    var selected by remember { mutableStateOf(label) }
    var focused by remember { mutableStateOf(false) }

    LaunchedEffect(label) { selected = label }

    val filtered = if (query.isBlank()) emptyList()
    else options.filter { it.contains(query, ignoreCase = true) }.take(5)

    Column {
        OutlinedTextField(
            value = if (focused) query else selected,
            onValueChange = { query = it },
            label = { Text(if (focused && query.isEmpty()) label else "") },
            placeholder = { if (focused && query.isEmpty()) Text(label) },
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged { state ->
                    focused = state.isFocused
                    if (state.isFocused) query = ""
                },
            singleLine = true,
            trailingIcon = {
                IconButton(onClick = {
                    if (focused) {
                        focused = false
                        query = ""
                    } else {
                        focused = true
                        query = ""
                    }
                }) {
                    Icon(
                        imageVector = if (focused) Icons.Filled.Close else Icons.Filled.ArrowDropDown,
                        contentDescription = null,
                    )
                }
            },
        )
        if (focused && filtered.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            ) {
                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                    filtered.forEach { option ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selected = option
                                    query = ""
                                    focused = false
                                    onSelect(option)
                                }
                                .padding(horizontal = 16.dp, vertical = 10.dp),
                        ) {
                            Text(option, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String = "YYYY-MM-DD",
) {
    var showDialog by remember { mutableStateOf(false) }
    val today = remember {
        java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.HOUR_OF_DAY, 12)
            set(java.util.Calendar.MINUTE, 0)
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
        }
    }
    val parts = value.split("-")
    val year = parts.getOrNull(0)?.toIntOrNull() ?: today.get(java.util.Calendar.YEAR)
    val month = (parts.getOrNull(1)?.toIntOrNull() ?: (today.get(java.util.Calendar.MONTH) + 1)) - 1
    val day = parts.getOrNull(2)?.toIntOrNull() ?: today.get(java.util.Calendar.DAY_OF_MONTH)

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        placeholder = { Text(placeholder) },
        modifier = modifier.fillMaxWidth(),
        singleLine = true,
        readOnly = true,
        trailingIcon = {
            IconButton(onClick = { showDialog = true }) {
                Icon(Icons.Filled.ArrowDropDown, contentDescription = "Pick date")
            }
        },
    )

    if (showDialog) {
        val state = rememberDatePickerState(
            initialSelectedDateMillis = remember(year, month, day) {
                java.util.Calendar.getInstance().apply {
                    set(year, month, day, 0, 0, 0)
                    set(java.util.Calendar.MILLISECOND, 0)
                }.timeInMillis
            },
        )
        DatePickerDialog(
            onDismissRequest = { showDialog = false },
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let { millis ->
                        val cal = java.util.Calendar.getInstance().apply { timeInMillis = millis }
                        val y = cal.get(java.util.Calendar.YEAR)
                        val m = cal.get(java.util.Calendar.MONTH) + 1
                        val d = cal.get(java.util.Calendar.DAY_OF_MONTH)
                        onValueChange("%04d-%02d-%02d".format(y, m, d))
                    }
                    showDialog = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) { Text("Cancel") }
            },
        ) {
            DatePicker(state = state)
        }
    }
}
