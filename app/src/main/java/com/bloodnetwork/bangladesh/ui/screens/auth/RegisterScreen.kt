package com.bloodnetwork.bangladesh.ui.screens.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bloodnetwork.bangladesh.data.prefs.RegistrationStore
import com.bloodnetwork.bangladesh.ui.components.ErrorText
import com.bloodnetwork.bangladesh.ui.components.LabeledTextField
import com.bloodnetwork.bangladesh.ui.components.PrimaryButton
import com.bloodnetwork.bangladesh.ui.navigation.Routes
import com.bloodnetwork.bangladesh.ui.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    onNavigate: (String) -> Unit,
    vm: AuthViewModel,
    onRegistered: () -> Unit,
    onBack: () -> Unit,
) {
    val state by vm.uiState.collectAsStateWithLifecycle()
    val savedData by vm.registrationData.collectAsStateWithLifecycle()

    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }
    var localError by remember { mutableStateOf<String?>(null) }
    var phoneError by remember { mutableStateOf(false) }
    var emailError by remember { mutableStateOf(false) }
    var initialized by remember { mutableStateOf(false) }

    LaunchedEffect(savedData) {
        if (!initialized) {
            initialized = true
            firstName = savedData.fullName.substringBefore(" ").ifBlank { "" }
            lastName = savedData.fullName.substringAfter(" ").ifBlank { "" }
            phone = savedData.phoneNumber
            password = savedData.password
        }
    }

    LaunchedEffect(state.isLoggedIn) {
        if (state.isLoggedIn) onRegistered()
    }

    LaunchedEffect(state.error) {
        val err = state.error
        phoneError = err?.contains("phone", ignoreCase = true) == true
        emailError = err?.contains("email", ignoreCase = true) == true
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create Account") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = "Register to donate or request blood",
                style = MaterialTheme.typography.titleLarge,
            )
            LabeledTextField(firstName, { firstName = it; vm.saveRegistrationData(RegistrationStore.RegistrationData(firstName = it, lastName = lastName, phoneNumber = phone, password = password)) }, "First Name",
                leadingIcon = { Icon(Icons.Filled.Person, contentDescription = null) })
            LabeledTextField(lastName, { lastName = it; vm.saveRegistrationData(RegistrationStore.RegistrationData(firstName = firstName, lastName = it, phoneNumber = phone, password = password)) }, "Last Name")
            LabeledTextField(phone, { phone = it; phoneError = false; localError = null; vm.saveRegistrationData(RegistrationStore.RegistrationData(firstName = firstName, lastName = lastName, phoneNumber = it, password = password)) }, "Phone Number",
                keyboardType = KeyboardType.Phone, isError = phoneError,
                leadingIcon = { Icon(Icons.Filled.Phone, contentDescription = null) })
            LabeledTextField(email, { email = it; emailError = false; localError = null }, "Email (optional)",
                keyboardType = KeyboardType.Email, isError = emailError)
            LabeledTextField(password, { password = it; vm.saveRegistrationData(RegistrationStore.RegistrationData(firstName = firstName, lastName = lastName, phoneNumber = phone, password = it)) }, "Password",
                isPassword = true,
                leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = null) })
            LabeledTextField(confirm, { confirm = it }, "Confirm Password",
                isPassword = true)

            localError?.let { ErrorText(it) }
            state.error?.let { ErrorText(it) }

            PrimaryButton(
                text = "Register",
                loading = state.isLoading,
                onClick = {
                    localError = when {
                        firstName.isBlank() || lastName.isBlank() -> "Please fill in your name"
                        phone.length < 11 -> "Enter a valid Bangladeshi phone number"
                        password.length < 8 -> "Password must be at least 8 characters"
                        !password.any { it.isUpperCase() } -> "Password must contain an uppercase letter"
                        !password.any { it.isLowerCase() } -> "Password must contain a lowercase letter"
                        !password.any { it.isDigit() } -> "Password must contain a digit"
                        password != confirm -> "Passwords do not match"
                        else -> null
                    }
                    if (localError == null) vm.register(firstName, lastName, phone, password, email)
                },
            )
            TextButton(onClick = { onNavigate(Routes.LOGIN) }) {
                Text("Already have an account? Login")
            }
        }
    }
}
