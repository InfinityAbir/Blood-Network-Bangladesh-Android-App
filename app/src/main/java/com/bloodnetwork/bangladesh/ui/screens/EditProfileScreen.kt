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
import com.bloodnetwork.bangladesh.ui.theme.BloodRed
import com.bloodnetwork.bangladesh.ui.viewmodel.AuthViewModel
import com.bloodnetwork.bangladesh.ui.viewmodel.EditProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(onNavigate: (String) -> Unit, onBack: () -> Unit, authVm: AuthViewModel) {
    val factory = LocalVmFactory.current!!
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

    LaunchedEffect(state.error) {
        state.error?.let { snackbarHostState.showSnackbar(it) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Profile") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
                    Text("Profile Photo", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Avatar(photoUrl = photoUrlInput.ifBlank { authState.user?.photoUrl }, size = 64.dp)
                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            LabeledTextField(photoUrlInput, { photoUrlInput = it; photoError = null }, "Photo URL")
                            photoError?.let { ErrorText(it) }
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        PrimaryButton(
                            text = "Save Photo",
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
                            ) { Text("Remove") }
                        }
                    }
                }
            }

            Text("Change Phone Number", style = MaterialTheme.typography.titleMedium)
            Text(
                "Current: ${authState.user?.phoneNumber ?: "—"}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            LabeledTextField(newPhone, { newPhone = it }, "New Phone Number (optional)",
                keyboardType = KeyboardType.Phone)

            Text("Change Email", style = MaterialTheme.typography.titleMedium)
            Text(
                "Current: ${authState.user?.email?.takeIf { it.isNotBlank() } ?: "Not set"}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            LabeledTextField(newEmail, { newEmail = it }, "New Email (optional)",
                keyboardType = KeyboardType.Email)

            Text("Change Password", style = MaterialTheme.typography.titleMedium)
            LabeledTextField(currentPassword, { currentPassword = it }, "Current Password",
                isPassword = true)
            LabeledTextField(newPassword, { newPassword = it }, "New Password (optional)",
                isPassword = true)
            LabeledTextField(confirmNewPassword, { confirmNewPassword = it }, "Confirm New Password",
                isPassword = true)

            localError?.let { ErrorText(it) }

            if (state.success) {
                Text("Profile updated successfully", color = BloodRed, style = MaterialTheme.typography.labelLarge)
            }

            PrimaryButton(
                text = "Update Profile",
                loading = state.isLoading,
                enabled = hasChanges && currentPassword.isNotBlank(),
                onClick = {
                    localError = when {
                        currentPassword.isBlank() -> "Enter current password"
                        newPassword.isNotBlank() && newPassword != confirmNewPassword -> "Passwords do not match"
                        newPassword.isNotBlank() && newPassword.length < 8 -> "Password must be at least 8 characters"
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
