package com.bloodnetwork.bangladesh.ui.screens.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
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
import com.bloodnetwork.bangladesh.ui.components.ErrorText
import com.bloodnetwork.bangladesh.ui.components.LabeledTextField
import com.bloodnetwork.bangladesh.ui.components.PrimaryButton
import com.bloodnetwork.bangladesh.ui.i18n.tr
import com.bloodnetwork.bangladesh.ui.navigation.Routes
import com.bloodnetwork.bangladesh.ui.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onNavigate: (String) -> Unit,
    vm: AuthViewModel,
    onLoggedIn: () -> Unit,
    onBack: () -> Unit,
) {
    val state by vm.uiState.collectAsStateWithLifecycle()
    var phone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var localError by remember { mutableStateOf<String?>(null) }

    val invalidPhoneMsg = tr("Enter a valid Bangladeshi phone number", "সঠিক বাংলাদেশী ফোন নম্বর লিখুন")

    LaunchedEffect(state.isLoggedIn) {
        if (state.isLoggedIn) onLoggedIn()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(tr("Login", "লগ ইন")) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = tr("Back", "পিছনে"))
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .imePadding()
                .padding(start = 24.dp, end = 24.dp, top = 24.dp, bottom = 88.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = tr("Welcome back", "স্বাগতম"),
                style = MaterialTheme.typography.titleLarge,
            )
            LabeledTextField(
                value = phone,
                onValueChange = { phone = it; localError = null },
                label = tr("Phone Number", "ফোন নম্বর"),
                placeholder = "01XXXXXXXXX",
                keyboardType = KeyboardType.Phone,
                isError = localError != null,
                leadingIcon = { androidx.compose.material3.Icon(Icons.Filled.Phone, contentDescription = null) },
            )
            LabeledTextField(
                value = password,
                onValueChange = { password = it },
                label = tr("Password", "পাসওয়ার্ড"),
                isPassword = true,
                leadingIcon = { androidx.compose.material3.Icon(Icons.Filled.Lock, contentDescription = null) },
            )
            localError?.let { ErrorText(it) }
            ErrorText(state.error)
            PrimaryButton(
                text = tr("Login", "লগ ইন"),
                loading = state.isLoading,
                onClick = {
                    val isBanglaPhone = phone.matches(Regex("^01[3-9]\\d{8}$"))
                    if (!isBanglaPhone) {
                        localError = invalidPhoneMsg
                        return@PrimaryButton
                    }
                    vm.login(phone, password)
                },
            )
            TextButton(onClick = { onNavigate(Routes.REGISTER) }) {
                Text(tr("Don't have an account? Register", "অ্যাকাউন্ট নেই? রেজিস্টার করুন"))
            }
        }
    }
}
