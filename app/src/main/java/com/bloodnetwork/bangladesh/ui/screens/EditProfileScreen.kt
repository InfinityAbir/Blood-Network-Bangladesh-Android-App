package com.bloodnetwork.bangladesh.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bloodnetwork.bangladesh.ui.LocalVmFactory
import com.bloodnetwork.bangladesh.ui.components.Avatar
import com.bloodnetwork.bangladesh.ui.components.ErrorText
import com.bloodnetwork.bangladesh.ui.components.LabeledTextField
import com.bloodnetwork.bangladesh.ui.components.PrimaryButton
import com.bloodnetwork.bangladesh.ui.i18n.tr
import com.bloodnetwork.bangladesh.ui.theme.BloodRed
import com.bloodnetwork.bangladesh.ui.viewmodel.AuthViewModel
import com.bloodnetwork.bangladesh.ui.viewmodel.EditProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(onNavigate: (String) -> Unit, onBack: () -> Unit, authVm: AuthViewModel) {
    val factory = LocalVmFactory.current
    val vm: EditProfileViewModel = viewModel(factory = factory)
    val state by vm.uiState.collectAsStateWithLifecycle()
    val authState by authVm.uiState.collectAsStateWithLifecycle()

    var photoUrlInput by remember(authState.user?.photoUrl) { mutableStateOf(authState.user?.photoUrl ?: "") }
    var isSavingPhoto by remember { mutableStateOf(false) }
    var photoError by remember { mutableStateOf<String?>(null) }

    var newPhone by remember { mutableStateOf("") }
    var newEmail by remember { mutableStateOf("") }
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmNewPassword by remember { mutableStateOf("") }
    var localError by remember { mutableStateOf<String?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }

    val hasChanges = newPhone.isNotBlank() || newEmail.isNotBlank() || newPassword.isNotBlank()

    // Precomputed translated strings for use inside non-composable lambdas (onClick, etc.)
    val enterCurrentPasswordErr = tr("Enter current password", "বর্তমান পাসওয়ার্ড লিখুন")
    val passwordsMismatchErr = tr("Passwords do not match", "পাসওয়ার্ড মিলছে না")
    val passwordTooShortErr = tr("Password must be at least 8 characters", "পাসওয়ার্ড কমপক্ষে ৮ অক্ষর হতে হবে")

    LaunchedEffect(state.error) {
        state.error?.let { snackbarHostState.showSnackbar(it) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(tr("Edit Profile", "প্রোফাইল সম্পাদনা")) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = tr("Back", "পেছনে"))
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .imePadding()
                .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(tr("Profile Photo", "প্রোফাইল ছবি"), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Avatar(photoUrl = photoUrlInput.ifBlank { authState.user?.photoUrl }, donorName = "", size = 64.dp)
                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            LabeledTextField(photoUrlInput, { photoUrlInput = it; photoError = null }, tr("Photo URL", "ছবির ইউআরএল"))
                            photoError?.let { ErrorText(it) }
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        PrimaryButton(
                            text = tr("Save Photo", "ছবি সংরক্ষণ করুন"),
                            loading = isSavingPhoto,
                            enabled = !isSavingPhoto && photoUrlInput != (authState.user?.photoUrl ?: ""),
                            fillMax = false,
                            onClick = {
                                isSavingPhoto = true
                                authVm.updatePhoto(
                                    photoUrl = photoUrlInput.trim(),
                                    onSuccess = { isSavingPhoto = false },
                                    onError = { msg -> isSavingPhoto = false; photoError = msg },
                                )
                            },
                        )
                        if (!authState.user?.photoUrl.isNullOrBlank()) {
                            OutlinedButton(
                                enabled = !isSavingPhoto,
                                onClick = {
                                    isSavingPhoto = true
                                    photoUrlInput = ""
                                    authVm.updatePhoto(
                                        photoUrl = "",
                                        onSuccess = { isSavingPhoto = false },
                                        onError = { msg -> isSavingPhoto = false; photoError = msg },
                                    )
                                },
                            ) { Text(tr("Remove", "অপসারণ করুন")) }
                        }
                    }
                }
            }

            Text(tr("Change Phone Number", "ফোন নম্বর পরিবর্তন করুন"), style = MaterialTheme.typography.titleMedium)
            Text(
                tr("Current: ", "বর্তমান: ") + (authState.user?.phoneNumber ?: "—"),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            LabeledTextField(newPhone, { newPhone = it }, tr("New Phone Number (optional)", "নতুন ফোন নম্বর (ঐচ্ছিক)"),
                keyboardType = KeyboardType.Phone)

            Text(tr("Change Email", "ইমেইল পরিবর্তন করুন"), style = MaterialTheme.typography.titleMedium)
            Text(
                tr("Current: ", "বর্তমান: ") + (authState.user?.email?.takeIf { it.isNotBlank() } ?: tr("Not set", "নির্ধারিত নয়")),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            LabeledTextField(newEmail, { newEmail = it }, tr("New Email (optional)", "নতুন ইমেইল (ঐচ্ছিক)"),
                keyboardType = KeyboardType.Email)

            Text(tr("Change Password", "পাসওয়ার্ড পরিবর্তন করুন"), style = MaterialTheme.typography.titleMedium)
            LabeledTextField(currentPassword, { currentPassword = it }, tr("Current Password", "বর্তমান পাসওয়ার্ড"),
                isPassword = true)
            LabeledTextField(newPassword, { newPassword = it }, tr("New Password (optional)", "নতুন পাসওয়ার্ড (ঐচ্ছিক)"),
                isPassword = true)
            LabeledTextField(confirmNewPassword, { confirmNewPassword = it }, tr("Confirm New Password", "নতুন পাসওয়ার্ড নিশ্চিত করুন"),
                isPassword = true)

            localError?.let { ErrorText(it) }

            if (state.success) {
                Text(tr("Profile updated successfully", "প্রোফাইল সফলভাবে হালনাগাদ হয়েছে"), color = BloodRed, style = MaterialTheme.typography.labelLarge)
            }

            PrimaryButton(
                text = tr("Update Profile", "প্রোফাইল হালনাগাদ করুন"),
                loading = state.isLoading,
                enabled = hasChanges && currentPassword.isNotBlank(),
                onClick = {
                    localError = when {
                        currentPassword.isBlank() -> enterCurrentPasswordErr
                        newPassword.isNotBlank() && newPassword != confirmNewPassword -> passwordsMismatchErr
                        newPassword.isNotBlank() && newPassword.length < 8 -> passwordTooShortErr
                        else -> null
                    }
                    if (localError == null) {
                        vm.updateProfile(
                            currentPassword = currentPassword,
                            newPhone = newPhone.ifBlank { null },
                            newEmail = newEmail.ifBlank { null },
                            newPassword = newPassword.ifBlank { null },
                        )
                    }
                },
            )
        }
    }
}
