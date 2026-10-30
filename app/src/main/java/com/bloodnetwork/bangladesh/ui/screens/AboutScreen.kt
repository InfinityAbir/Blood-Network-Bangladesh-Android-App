package com.bloodnetwork.bangladesh.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bloodnetwork.bangladesh.data.model.DeveloperInfoDto
import com.bloodnetwork.bangladesh.data.model.UpdateDeveloperInfoRequest
import com.bloodnetwork.bangladesh.ui.LocalVmFactory
import com.bloodnetwork.bangladesh.ui.components.SkeletonCard
import com.bloodnetwork.bangladesh.ui.i18n.tr
import com.bloodnetwork.bangladesh.ui.theme.BloodRed
import com.bloodnetwork.bangladesh.ui.util.AppLinks
import com.bloodnetwork.bangladesh.ui.util.SafeLinks
import com.bloodnetwork.bangladesh.ui.util.openDialer
import com.bloodnetwork.bangladesh.ui.util.openEmail
import com.bloodnetwork.bangladesh.ui.util.openHttpsUrl
import com.bloodnetwork.bangladesh.ui.viewmodel.AboutViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(onBack: () -> Unit, isAdmin: Boolean) {
    val factory = LocalVmFactory.current
    val vm: AboutViewModel = viewModel(factory = factory)
    val state by vm.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var showEditDialog by remember { mutableStateOf(false) }
    val cantOpenMsg = tr("No app found to open this link", "এই লিংকটি খোলার মতো অ্যাপ পাওয়া যায়নি")

    LaunchedEffect(Unit) { vm.load() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(tr("About the Developer", "ডেভেলপার সম্পর্কে"), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = tr("Back", "পেছনে"))
                    }
                },
                actions = {
                    if (isAdmin && state.info != null) {
                        IconButton(onClick = { showEditDialog = true }) {
                            Icon(Icons.Filled.Edit, contentDescription = tr("Edit developer info", "ডেভেলপার তথ্য সম্পাদনা করুন"))
                        }
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            when {
                state.isLoading && state.info == null -> Card(modifier = Modifier.fillMaxWidth()) { SkeletonCard() }
                state.error != null && state.info == null -> {
                    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(tr("Couldn't load this page", "এই পৃষ্ঠাটি লোড করা যায়নি"), style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.SemiBold)
                            Text(state.error ?: "", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Button(onClick = { vm.load() }, colors = ButtonDefaults.buttonColors(containerColor = BloodRed), shape = RoundedCornerShape(50)) { Text(tr("Retry", "আবার চেষ্টা করুন")) }
                        }
                    }
                }
                state.info != null -> AboutContent(state.info!!, onEmail = { email ->
                    if (!context.openEmail(email)) Toast.makeText(context, cantOpenMsg, Toast.LENGTH_SHORT).show()
                }, onCall = { phone ->
                    if (!context.openDialer(phone)) Toast.makeText(context, cantOpenMsg, Toast.LENGTH_SHORT).show()
                }, onOpenUrl = { url ->
                    if (!context.openHttpsUrl(url)) Toast.makeText(context, cantOpenMsg, Toast.LENGTH_SHORT).show()
                }, onOpenPrivacy = {
                    if (!context.openHttpsUrl(AppLinks.privacyPolicyUrl(context))) Toast.makeText(context, cantOpenMsg, Toast.LENGTH_SHORT).show()
                })
            }
        }
    }

    if (showEditDialog && state.info != null) {
        EditDeveloperInfoDialog(
            info = state.info!!,
            isSaving = state.isSaving,
            saveError = state.saveError,
            onDismiss = { showEditDialog = false },
            onSave = { request -> vm.save(request) { showEditDialog = false } },
        )
    }
}

@Composable
private fun AboutContent(
    info: DeveloperInfoDto,
    onEmail: (String) -> Unit,
    onCall: (String) -> Unit,
    onOpenUrl: (String) -> Unit,
    onOpenPrivacy: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
            com.bloodnetwork.bangladesh.ui.components.Avatar(photoUrl = info.photoUrl, donorName = info.name, size = 72.dp)
            Spacer(Modifier.height(4.dp))
            Text(info.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(info.role, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            info.email?.takeIf { it.isNotBlank() }?.let {
                ContactRow(Icons.Filled.Email, tr("Email", "ইমেইল"), it) { onEmail(it) }
            }
            info.phone?.takeIf { it.isNotBlank() }?.let {
                ContactRow(Icons.Filled.Phone, tr("Phone", "ফোন"), it) { onCall(it) }
            }
            info.linkedInUrl?.takeIf { it.isNotBlank() }?.let {
                ContactRow(Icons.Filled.Language, tr("LinkedIn", "লিংকডইন"), it) { onOpenUrl(it) }
            }
            info.githubUrl?.takeIf { it.isNotBlank() }?.let {
                ContactRow(Icons.Filled.Code, tr("GitHub", "গিটহাব"), it) { onOpenUrl(it) }
            }
            info.portfolioUrl?.takeIf { it.isNotBlank() }?.let {
                ContactRow(Icons.Filled.Language, tr("Portfolio", "পোর্টফোলিও"), it) { onOpenUrl(it) }
            }
            ContactRow(Icons.Filled.PrivacyTip, tr("Privacy Policy", "গোপনীয়তা নীতি"), tr("How we use and protect your data", "আমরা আপনার তথ্য কীভাবে ব্যবহার ও সুরক্ষিত রাখি")) { onOpenPrivacy() }
        }
    }

    Text(
        tr(
            "Blood Network Bangladesh helps donors and requesters find each other faster. Built to make donating blood as simple as it should be.",
            "ব্লাড নেটওয়ার্ক বাংলাদেশ দাতা ও গ্রহীতাদের দ্রুত একে অপরকে খুঁজে পেতে সাহায্য করে। রক্তদানকে যতটা সহজ হওয়া উচিত, ঠিক ততটাই সহজ করতে তৈরি।",
        ),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
private fun ContactRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .then(Modifier)
            .padding(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        androidx.compose.material3.Surface(
            onClick = onClick,
            shape = RoundedCornerShape(12.dp),
            color = androidx.compose.ui.graphics.Color.Transparent,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                Box(modifier = Modifier.size(36.dp).clip(CircleShape).background(BloodRed.copy(alpha = 0.1f)), contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = null, tint = BloodRed, modifier = Modifier.size(18.dp))
                }
                Column {
                    Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}

@Composable
private fun EditDeveloperInfoDialog(
    info: DeveloperInfoDto,
    isSaving: Boolean,
    saveError: String?,
    onDismiss: () -> Unit,
    onSave: (UpdateDeveloperInfoRequest) -> Unit,
) {
    var name by remember { mutableStateOf(info.name) }
    var role by remember { mutableStateOf(info.role) }
    var email by remember { mutableStateOf(info.email ?: "") }
    var phone by remember { mutableStateOf(info.phone ?: "") }
    var linkedIn by remember { mutableStateOf(info.linkedInUrl ?: "") }
    var github by remember { mutableStateOf(info.githubUrl ?: "") }
    var portfolio by remember { mutableStateOf(info.portfolioUrl ?: "") }
    var photoUrl by remember { mutableStateOf(info.photoUrl ?: "") }
    var validationError by remember { mutableStateOf<String?>(null) }
    val badLinkMsg = tr("Links must be full https:// URLs", "লিংক অবশ্যই পূর্ণ https:// ইউআরএল হতে হবে")
    val badContactMsg = tr("Enter a valid email address and phone number", "সঠিক ইমেইল ও ফোন নম্বর দিন")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(tr("Edit developer info", "ডেভেলপার তথ্য সম্পাদনা করুন"), fontWeight = FontWeight.SemiBold) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                (validationError ?: saveError)?.let {
                    Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                }
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text(tr("Name", "নাম")) }, singleLine = true, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp))
                OutlinedTextField(value = role, onValueChange = { role = it }, label = { Text(tr("Role", "পদবি")) }, singleLine = true, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp))
                OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text(tr("Email", "ইমেইল")) }, singleLine = true, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp))
                OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text(tr("Phone", "ফোন")) }, singleLine = true, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp))
                OutlinedTextField(value = linkedIn, onValueChange = { linkedIn = it }, label = { Text(tr("LinkedIn URL", "লিংকডইন ইউআরএল")) }, singleLine = true, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp))
                OutlinedTextField(value = github, onValueChange = { github = it }, label = { Text(tr("GitHub URL", "গিটহাব ইউআরএল")) }, singleLine = true, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp))
                OutlinedTextField(value = portfolio, onValueChange = { portfolio = it }, label = { Text(tr("Portfolio URL", "পোর্টফোলিও ইউআরএল")) }, singleLine = true, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp))
                OutlinedTextField(value = photoUrl, onValueChange = { photoUrl = it }, label = { Text(tr("Photo URL", "ছবির ইউআরএল")) }, singleLine = true, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp))
            }
        },
        confirmButton = {
            Button(
                enabled = !isSaving && name.isNotBlank() && role.isNotBlank(),
                onClick = {
                    val urlsOk = listOf(linkedIn, github, portfolio, photoUrl)
                        .all { it.isBlank() || SafeLinks.httpsUrlOrNull(it) != null }
                    val contactOk = (email.isBlank() || SafeLinks.emailOrNull(email) != null) &&
                        (phone.isBlank() || SafeLinks.dialableOrNull(phone) != null)
                    validationError = when {
                        !urlsOk -> badLinkMsg
                        !contactOk -> badContactMsg
                        else -> null
                    }
                    if (validationError != null) return@Button
                    onSave(
                        UpdateDeveloperInfoRequest(
                            name = name.trim(), role = role.trim(),
                            email = email.trim().ifBlank { null }, phone = phone.trim().ifBlank { null },
                            linkedInUrl = linkedIn.trim().ifBlank { null }, githubUrl = github.trim().ifBlank { null },
                            portfolioUrl = portfolio.trim().ifBlank { null },
                            photoUrl = photoUrl.trim().ifBlank { null },
                        ),
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = BloodRed),
            ) {
                if (isSaving) CircularProgressIndicator(modifier = Modifier.size(16.dp), color = MaterialTheme.colorScheme.onPrimary)
                else Text(tr("Save", "সংরক্ষণ করুন"))
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(tr("Cancel", "বাতিল করুন")) } },
    )
}
