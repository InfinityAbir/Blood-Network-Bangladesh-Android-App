package com.bloodnetwork.bangladesh.ui.screens.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import com.bloodnetwork.bangladesh.data.model.UserRole
import com.bloodnetwork.bangladesh.data.prefs.RegistrationStore
import com.bloodnetwork.bangladesh.ui.components.ErrorText
import com.bloodnetwork.bangladesh.ui.components.LabeledTextField
import com.bloodnetwork.bangladesh.ui.components.PrimaryButton
import com.bloodnetwork.bangladesh.ui.components.RowChips
import com.bloodnetwork.bangladesh.ui.i18n.tr
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
    var selectedRole by remember { mutableStateOf(UserRole.Requester) }
    var localError by remember { mutableStateOf<String?>(null) }
    var phoneError by remember { mutableStateOf(false) }
    var emailError by remember { mutableStateOf(false) }
    var initialized by remember { mutableStateOf(false) }

    val fillNameMsg = tr("Please fill in your name", "আপনার নাম লিখুন")
    val invalidPhoneMsg = tr("Enter a valid Bangladeshi phone number", "সঠিক বাংলাদেশী ফোন নম্বর লিখুন")
    val passwordLengthMsg = tr("Password must be at least 8 characters", "পাসওয়ার্ড অন্তত ৮ অক্ষরের হতে হবে")
    val passwordUppercaseMsg = tr("Password must contain an uppercase letter", "পাসওয়ার্ডে অন্তত একটি বড় হাতের অক্ষর থাকতে হবে")
    val passwordLowercaseMsg = tr("Password must contain a lowercase letter", "পাসওয়ার্ডে অন্তত একটি ছোট হাতের অক্ষর থাকতে হবে")
    val passwordDigitMsg = tr("Password must contain a digit", "পাসওয়ার্ডে অন্তত একটি সংখ্যা থাকতে হবে")
    val passwordsMismatchMsg = tr("Passwords do not match", "পাসওয়ার্ড দুটি মিলছে না")

    LaunchedEffect(savedData) {
        if (!initialized) {
            initialized = true
            firstName = savedData.firstName
            lastName = savedData.lastName
            phone = savedData.phoneNumber
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
                title = { Text(tr("Create Account", "অ্যাকাউন্ট তৈরি করুন")) },
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
                text = tr("Register to donate or request blood", "রক্তদান বা রক্তের জন্য অনুরোধ করতে রেজিস্টার করুন"),
                style = MaterialTheme.typography.titleLarge,
            )
            LabeledTextField(firstName, { firstName = it; vm.saveRegistrationData(RegistrationStore.RegistrationData(firstName = it, lastName = lastName, phoneNumber = phone)) }, tr("First Name", "প্রথম নাম"),
                leadingIcon = { Icon(Icons.Filled.Person, contentDescription = null) })
            LabeledTextField(lastName, { lastName = it; vm.saveRegistrationData(RegistrationStore.RegistrationData(firstName = firstName, lastName = it, phoneNumber = phone)) }, tr("Last Name", "শেষ নাম"))
            LabeledTextField(phone, { phone = it; phoneError = false; localError = null; vm.saveRegistrationData(RegistrationStore.RegistrationData(firstName = firstName, lastName = lastName, phoneNumber = it)) }, tr("Phone Number", "ফোন নম্বর"),
                keyboardType = KeyboardType.Phone, isError = phoneError,
                leadingIcon = { Icon(Icons.Filled.Phone, contentDescription = null) })
            LabeledTextField(email, { email = it; emailError = false; localError = null }, tr("Email (optional)", "ইমেইল (ঐচ্ছিক)"),
                keyboardType = KeyboardType.Email, isError = emailError)
            LabeledTextField(password, { password = it }, tr("Password", "পাসওয়ার্ড"),
                isPassword = true,
                leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = null) })
            LabeledTextField(confirm, { confirm = it }, tr("Confirm Password", "পাসওয়ার্ড নিশ্চিত করুন"),
                isPassword = true)

            // G7: volunteer role selection
            androidx.compose.material3.Text(tr("Register as", "ভূমিকা নির্বাচন করুন"), style = MaterialTheme.typography.titleSmall)
            val roleLabels: Map<UserRole, String> = mapOf(
                UserRole.Requester to tr("Requester", "অনুরোধকারী"),
                UserRole.Donor to tr("Donor", "রক্তদাতা"),
                UserRole.Volunteer to tr("Volunteer", "স্বেচ্ছাসেবক"),
            )
            RowChips(
                options = listOf(UserRole.Requester, UserRole.Donor, UserRole.Volunteer),
                selected = selectedRole,
                labelOf = { roleLabels[it] ?: it.name },
                onSelect = { selectedRole = it },
            )

            localError?.let { ErrorText(it) }
            state.error?.let { ErrorText(it) }

            PrimaryButton(
                text = tr("Register", "রেজিস্টার করুন"),
                loading = state.isLoading,
                onClick = {
                    localError = when {
                        firstName.isBlank() || lastName.isBlank() -> fillNameMsg
                        phone.length < 11 -> invalidPhoneMsg
                        password.length < 8 -> passwordLengthMsg
                        !password.any { it.isUpperCase() } -> passwordUppercaseMsg
                        !password.any { it.isLowerCase() } -> passwordLowercaseMsg
                        !password.any { it.isDigit() } -> passwordDigitMsg
                        password != confirm -> passwordsMismatchMsg
                        else -> null
                    }
                    if (localError == null) vm.register(firstName, lastName, phone, password, email, selectedRole)
                },
            )
            TextButton(onClick = { onNavigate(Routes.LOGIN) }) {
                Text(tr("Already have an account? Login", "ইতিমধ্যে অ্যাকাউন্ট আছে? লগ ইন করুন"))
            }
        }
    }
}
