package com.bloodnetwork.bangladesh.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.TextButton
import androidx.compose.ui.platform.LocalContext
import com.bloodnetwork.bangladesh.ui.util.AppLinks
import com.bloodnetwork.bangladesh.ui.util.ImageProcessing
import kotlinx.coroutines.launch
import com.bloodnetwork.bangladesh.ui.util.openEmail
import com.bloodnetwork.bangladesh.ui.util.openHttpsUrl
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
fun EditProfileScreen(
    onNavigate: (String) -> Unit,
    onBack: () -> Unit,
    authVm: AuthViewModel,
    onAccountDeleted: () -> Unit = {},
) {
    val factory = LocalVmFactory.current
    val vm: EditProfileViewModel = viewModel(factory = factory)
    val state by vm.uiState.collectAsStateWithLifecycle()
    val deleteState by vm.deleteState.collectAsStateWithLifecycle()
    val authState by authVm.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var showDeleteDialog by remember { mutableStateOf(false) }
    var deletePassword by remember { mutableStateOf("") }
    val cantOpenMsg = tr("No app found to open this link", "এই লিংকটি খোলার মতো অ্যাপ পাওয়া যায়নি")
    val photoUnreadableMsg = tr(
        "That file couldn't be read as a picture. Please try a different photo.",
        "ফাইলটি ছবি হিসেবে পড়া যায়নি। অনুগ্রহ করে অন্য ছবি বেছে নিন।",
    )
    val photoUpdatedMsg = tr("Photo updated", "ছবি হালনাগাদ হয়েছে")
    val photoRemovedMsg = tr("Photo removed", "ছবি সরানো হয়েছে")

    LaunchedEffect(deleteState.deleted) {
        if (deleteState.deleted) onAccountDeleted()
    }

    var isSavingPhoto by remember { mutableStateOf(false) }
    var photoError by remember { mutableStateOf<String?>(null) }
    var photoNotice by remember { mutableStateOf<String?>(null) }
    val scope = androidx.compose.runtime.rememberCoroutineScope()

    // The system photo picker needs no storage permission. Whatever format is chosen (PNG, JPEG,
    // WebP, ...) is converted to a small square JPEG before it leaves the phone.
    val photoPicker = androidx.activity.compose.rememberLauncherForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia(),
    ) { uri ->
        if (uri != null) {
            isSavingPhoto = true
            photoError = null
            photoNotice = null
            scope.launch {
                val jpeg = ImageProcessing.toAvatarJpeg(context, uri)
                if (jpeg == null) {
                    isSavingPhoto = false
                    photoError = photoUnreadableMsg
                } else {
                    authVm.uploadPhoto(
                        jpeg,
                        onSuccess = { isSavingPhoto = false; photoNotice = photoUpdatedMsg },
                        onError = { msg -> isSavingPhoto = false; photoError = msg },
                    )
                }
            }
        }
    }

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
                    val hasPhoto = !authState.user?.photoUrl.isNullOrBlank()
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Avatar(photoUrl = authState.user?.photoUrl, donorName = authState.user?.firstName ?: "", size = 80.dp)
                        Text(
                            tr(
                                "Pick a picture from your gallery (JPG, PNG, WebP…). It's cropped to a square and shown to people looking for donors.",
                                "গ্যালারি থেকে একটি ছবি বেছে নিন (JPG, PNG, WebP…)। ছবিটি বর্গাকারে কাটা হবে এবং দাতা খোঁজা মানুষ দেখতে পাবে।",
                            ),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.weight(1f),
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        PrimaryButton(
                            text = if (hasPhoto) tr("Change photo", "ছবি বদলান") else tr("Choose photo", "ছবি বাছুন"),
                            loading = isSavingPhoto,
                            fillMax = false,
                            onClick = {
                                photoPicker.launch(
                                    androidx.activity.result.PickVisualMediaRequest(
                                        androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia.ImageOnly,
                                    ),
                                )
                            },
                        )
                        if (hasPhoto) {
                            OutlinedButton(
                                enabled = !isSavingPhoto,
                                onClick = {
                                    isSavingPhoto = true
                                    photoError = null
                                    photoNotice = null
                                    authVm.removePhoto(
                                        onSuccess = { isSavingPhoto = false; photoNotice = photoRemovedMsg },
                                        onError = { msg -> isSavingPhoto = false; photoError = msg },
                                    )
                                },
                            ) { Text(tr("Remove", "অপসারণ করুন")) }
                        }
                    }
                    photoError?.let { ErrorText(it) }
                    photoNotice?.let {
                        Text(it, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
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

            Card(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(tr("Privacy & data", "গোপনীয়তা ও তথ্য"), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    OutlinedButton(
                        onClick = {
                            if (!context.openHttpsUrl(AppLinks.privacyPolicyUrl(context))) {
                                Toast.makeText(context, cantOpenMsg, Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                    ) { Text(tr("Read the privacy policy", "গোপনীয়তা নীতি পড়ুন")) }
                    Text(
                        tr(
                            "Deleting your account permanently removes your profile, donor details and requests. This cannot be undone.",
                            "অ্যাকাউন্ট মুছলে আপনার প্রোফাইল, দাতার তথ্য ও অনুরোধ স্থায়ীভাবে মুছে যাবে। এটি ফেরানো যাবে না।",
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    OutlinedButton(
                        onClick = { vm.resetDeleteState(); deletePassword = ""; showDeleteDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error),
                    ) { Text(tr("Delete my account", "আমার অ্যাকাউন্ট মুছুন")) }
                }
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { if (!deleteState.isDeleting) showDeleteDialog = false },
            title = { Text(tr("Delete your account?", "অ্যাকাউন্ট মুছবেন?")) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (deleteState.selfServiceUnavailable) {
                        Text(
                            tr(
                                "Instant deletion isn't available right now. Send us a deletion request and we'll delete your account and data.",
                                "এখনই সরাসরি মোছা যাচ্ছে না। আমাদের একটি অনুরোধ পাঠান, আমরা আপনার অ্যাকাউন্ট ও তথ্য মুছে দেব।",
                            ),
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    } else {
                        Text(
                            tr(
                                "This permanently deletes your profile, donor details, requests and matches. Enter your password to confirm.",
                                "এটি আপনার প্রোফাইল, দাতার তথ্য, অনুরোধ ও মিল স্থায়ীভাবে মুছে দেবে। নিশ্চিত করতে পাসওয়ার্ড দিন।",
                            ),
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        LabeledTextField(
                            deletePassword, { deletePassword = it }, tr("Password", "পাসওয়ার্ড"),
                            isPassword = true,
                            imeAction = androidx.compose.ui.text.input.ImeAction.Done,
                        )
                        deleteState.error?.let { ErrorText(it) }
                    }
                }
            },
            confirmButton = {
                if (deleteState.selfServiceUnavailable) {
                    val phone = authState.user?.phoneNumber.orEmpty()
                    Button(onClick = {
                        val sent = context.openEmail(
                            AppLinks.supportEmail(context),
                            subject = "Account deletion request",
                            body = "Please delete my Blood Network BD account (phone: $phone) and all associated data.",
                        )
                        if (!sent) Toast.makeText(context, cantOpenMsg, Toast.LENGTH_SHORT).show()
                    }, colors = ButtonDefaults.buttonColors(containerColor = BloodRed)) {
                        Text(tr("Email request", "ইমেইলে অনুরোধ"))
                    }
                } else {
                    Button(
                        onClick = { vm.deleteAccount(deletePassword) },
                        enabled = deletePassword.isNotBlank() && !deleteState.isDeleting,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    ) {
                        if (deleteState.isDeleting) CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onError)
                        else Text(tr("Delete permanently", "স্থায়ীভাবে মুছুন"))
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }, enabled = !deleteState.isDeleting) {
                    Text(tr("Cancel", "বাতিল করুন"))
                }
            },
        )
    }
}
