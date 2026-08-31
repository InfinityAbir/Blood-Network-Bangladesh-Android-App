package com.bloodnetwork.bangladesh.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Save
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.bloodnetwork.bangladesh.ui.util.formatDateTime
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bloodnetwork.bangladesh.data.model.SystemSettingsDto
import com.bloodnetwork.bangladesh.ui.LocalVmFactory
import com.bloodnetwork.bangladesh.ui.components.ErrorText
import com.bloodnetwork.bangladesh.ui.i18n.tr
import com.bloodnetwork.bangladesh.ui.theme.BloodRed
import com.bloodnetwork.bangladesh.ui.viewmodel.AdminSystemSettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminSystemSettingsScreen(onBack: () -> Unit) {
    val factory = LocalVmFactory.current
    val vm: AdminSystemSettingsViewModel = viewModel(factory = factory)
    val state by vm.uiState.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }

    var form by remember { mutableStateOf<SystemSettingsDto?>(null) }

    LaunchedEffect(state.settings) {
        if (form == null && state.settings != null) form = state.settings
    }
    LaunchedEffect(state.successMessage, state.error) {
        state.successMessage?.let { snackbar.showSnackbar(it); vm.clearMessages() }
        state.error?.let { snackbar.showSnackbar(it); vm.clearMessages() }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(tr("System Settings", "সিস্টেম সেটিংস"), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, contentDescription = tr("Back", "পেছনে")) } },
                actions = {
                    if (form != null) {
                        IconButton(onClick = { form?.let { vm.save(it) } }, enabled = !state.isSaving) {
                            Icon(Icons.Filled.Save, contentDescription = tr("Save", "সেভ করুন"))
                        }
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbar) }
    ) { padding ->
        when {
            state.isLoading -> {
                Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp), verticalArrangement = Arrangement.Center) {
                    CircularProgressIndicator()
                }
            }
            state.error != null && form == null -> {
                Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    ErrorText(state.error ?: "")
                    Button(onClick = { vm.load() }) { Text(tr("Retry", "আবার চেষ্টা করুন")) }
                }
            }
            form != null -> {
                val f = form!!
                Column(
                    modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).imePadding().padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(tr("Business Rules", "ব্যবসায়িক নিয়ম"), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    SectionCard {
                        NumberField(tr("Min Donation Interval (days)", "সর্বনিম্ন রক্তদানের ব্যবধান (দিন)"), f.minimumDonationIntervalDays) { form = f.copy(minimumDonationIntervalDays = it) }
                        NumberField(tr("Profile Confirmation Days", "প্রোফাইল নিশ্চিতকরণ দিন"), f.donorProfileConfirmationDays) { form = f.copy(donorProfileConfirmationDays = it) }
                        NumberField(tr("Max Active Requests Per User", "প্রতি ব্যবহারকারীর সর্বোচ্চ সক্রিয় অনুরোধ"), f.maxActiveRequestsPerUser) { form = f.copy(maxActiveRequestsPerUser = it) }
                        NumberField(tr("Contact Cooldown (hours)", "যোগাযোগ বিরতি (ঘণ্টা)"), f.contactCooldownHours) { form = f.copy(contactCooldownHours = it) }
                    }
                    Text(tr("Match Score Weights (0-100)", "ম্যাচ স্কোর ওজন (০-১০০)"), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    Text(tr("Higher weight = stronger influence. Sum of applicable bonuses = final score.", "উচ্চ ওজন = বেশি প্রভাব। প্রযোজ্য বোনাসের যোগফল = চূড়ান্ত স্কোর।"), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    SectionCard {
                        NumberField("Exact Blood Group", f.exactBloodGroupWeight) { form = f.copy(exactBloodGroupWeight = it) }
                        NumberField("Compatible Blood Group", f.compatibleBloodGroupWeight) { form = f.copy(compatibleBloodGroupWeight = it) }
                        NumberField("Available", f.availableWeight) { form = f.copy(availableWeight = it) }
                        NumberField("Unknown", f.unknownWeight) { form = f.copy(unknownWeight = it) }
                        NumberField("Verified", f.verifiedWeight) { form = f.copy(verifiedWeight = it) }
                        NumberField("Unverified", f.unverifiedWeight) { form = f.copy(unverifiedWeight = it) }
                        NumberField("Profile Freshness", f.profileFreshnessWeight) { form = f.copy(profileFreshnessWeight = it) }
                        NumberField("Distance 0-3km", f.distance0to3kmWeight) { form = f.copy(distance0to3kmWeight = it) }
                        NumberField("Distance 3-10km", f.distance3to10kmWeight) { form = f.copy(distance3to10kmWeight = it) }
                        NumberField("Distance 10-25km", f.distance10to25kmWeight) { form = f.copy(distance10to25kmWeight = it) }
                        NumberField("Distance >25km", f.distanceOver25kmWeight) { form = f.copy(distanceOver25kmWeight = it) }
                    }
                    Button(
                        onClick = { vm.save(f) },
                        enabled = !state.isSaving,
                        colors = ButtonDefaults.buttonColors(containerColor = BloodRed),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(vertical = 14.dp)
                    ) {
                        if (state.isSaving) CircularProgressIndicator(modifier = Modifier.padding(end = 8.dp), color = androidx.compose.ui.graphics.Color.White) else Text(tr("Save Settings", "সেটিংস সংরক্ষণ করুন"))
                    }
                    state.settings?.updatedAt?.let { Text(tr("Last updated: ${formatDateTime(it)}", "শেষ আপডেট: ${formatDateTime(it)}"), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                }
            }
        }
    }
}

@Composable
private fun SectionCard(content: @Composable () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) { content() }
    }
}

@Composable
private fun NumberField(label: String, value: Int, onChange: (Int) -> Unit) {
    var text by remember(value) { mutableStateOf(value.toString()) }
    OutlinedTextField(
        value = text,
        onValueChange = {
            text = it
            it.toIntOrNull()?.let(onChange)
        },
        label = { Text(label, style = MaterialTheme.typography.labelSmall) },
        singleLine = true,
        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp)
    )
}
