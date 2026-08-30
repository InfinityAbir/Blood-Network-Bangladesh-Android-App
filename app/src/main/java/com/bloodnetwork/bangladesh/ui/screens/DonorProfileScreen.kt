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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bloodnetwork.bangladesh.data.model.AvailabilityStatus
import com.bloodnetwork.bangladesh.data.model.BloodGroup
import com.bloodnetwork.bangladesh.data.prefs.DonorProfileStore
import com.bloodnetwork.bangladesh.ui.LocalVmFactory
import com.bloodnetwork.bangladesh.ui.components.BloodGroupChips
import com.bloodnetwork.bangladesh.ui.components.DatePickerField
import com.bloodnetwork.bangladesh.ui.components.ErrorText
import com.bloodnetwork.bangladesh.ui.components.LabeledTextField
import com.bloodnetwork.bangladesh.ui.components.PickerField
import com.bloodnetwork.bangladesh.ui.components.PrimaryButton
import com.bloodnetwork.bangladesh.ui.components.RowChips
import com.bloodnetwork.bangladesh.ui.components.DonorProfileSkeleton
import com.bloodnetwork.bangladesh.ui.i18n.tr
import com.bloodnetwork.bangladesh.ui.theme.BloodRed
import com.bloodnetwork.bangladesh.ui.viewmodel.DonorViewModel
import com.bloodnetwork.bangladesh.ui.viewmodel.LocationViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DonorProfileScreen(onNavigate: (String) -> Unit, onBack: () -> Unit) {
    val factory = LocalVmFactory.current
    val vm: DonorViewModel = viewModel(factory = factory)
    val locVm: LocationViewModel = viewModel(factory = factory)

    val state by vm.uiState.collectAsStateWithLifecycle()
    val locState by locVm.uiState.collectAsStateWithLifecycle()
    val draftData by vm.draftData.collectAsStateWithLifecycle()

    var bloodGroup by remember { mutableStateOf<String?>(null) }
    var gender by remember { mutableStateOf<String?>(null) }
    var dateOfBirth by remember { mutableStateOf("") }
    var divisionId by remember { mutableStateOf<String?>(null) }
    var divisionName by remember { mutableStateOf<String?>(null) }
    var districtId by remember { mutableStateOf<String?>(null) }
    var districtName by remember { mutableStateOf<String?>(null) }
    var upazilaId by remember { mutableStateOf<String?>(null) }
    var upazilaName by remember { mutableStateOf<String?>(null) }
    var area by remember { mutableStateOf("") }
    var customAddress by remember { mutableStateOf("") }
    var lastDonationDate by remember { mutableStateOf("") }
    var formError by remember { mutableStateOf<String?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }

    val selectBloodGroupErr = tr("Select a blood group", "রক্তের গ্রুপ নির্বাচন করুন")
    val selectDistrictErr = tr("Select a district", "জেলা নির্বাচন করুন")
    val selectUpazilaErr = tr("Select an upazila", "উপজেলা নির্বাচন করুন")

    var initialized by remember { mutableStateOf(false) }
    var origBloodGroup by remember { mutableStateOf<String?>(null) }
    var origGender by remember { mutableStateOf<String?>(null) }
    var origDateOfBirth by remember { mutableStateOf("") }
    var origDistrictId by remember { mutableStateOf<String?>(null) }
    var origUpazilaId by remember { mutableStateOf<String?>(null) }
    var origArea by remember { mutableStateOf("") }
    var origLastDonationDate by remember { mutableStateOf("") }

    fun saveDraft() {
        vm.saveDraftData(DonorProfileStore.DonorProfileData(
            bloodGroup = bloodGroup ?: "",
            gender = gender ?: "",
            dateOfBirth = dateOfBirth,
            divisionId = divisionId ?: "",
            divisionName = divisionName ?: "",
            districtId = districtId ?: "",
            districtName = districtName ?: "",
            upazilaId = upazilaId ?: "",
            upazilaName = upazilaName ?: "",
            area = area,
            customAddress = customAddress,
            lastDonationDate = lastDonationDate,
        ))
    }

    LaunchedEffect(Unit) {
        locVm.loadDivisions()
        locVm.loadAllDistricts()
        vm.loadProfile()
    }

    LaunchedEffect(state.profile) {
        val p = state.profile
        if (p != null) {
            if (!initialized) {
                initialized = true
                bloodGroup = p.bloodGroup.label
                gender = p.gender
                dateOfBirth = p.dateOfBirth?.take(10) ?: ""
                districtId = p.districtId.takeIf { it.isNotBlank() }
                districtName = p.districtName
                upazilaId = p.upazilaId.takeIf { it.isNotBlank() }
                upazilaName = p.upazilaName
                area = p.area ?: ""
                customAddress = p.customAddress ?: ""
                lastDonationDate = p.lastDonationDate?.take(10) ?: ""
                districtId?.let { locVm.loadUpazilas(it) }
                origBloodGroup = bloodGroup
                origGender = gender
                origDateOfBirth = dateOfBirth
                origDistrictId = districtId
                origUpazilaId = upazilaId
                origArea = area
                origLastDonationDate = lastDonationDate
            }
        } else if (!initialized && draftData.bloodGroup.isNotEmpty()) {
            initialized = true
            bloodGroup = draftData.bloodGroup.ifBlank { null }
            gender = draftData.gender.ifBlank { null }
            dateOfBirth = draftData.dateOfBirth
            divisionId = draftData.divisionId.ifBlank { null }
            divisionName = draftData.divisionName.ifBlank { null }
            districtId = draftData.districtId.ifBlank { null }
            districtName = draftData.districtName.ifBlank { null }
            upazilaId = draftData.upazilaId.ifBlank { null }
            upazilaName = draftData.upazilaName.ifBlank { null }
            area = draftData.area
            customAddress = draftData.customAddress
            lastDonationDate = draftData.lastDonationDate
            districtId?.let { locVm.loadUpazilas(it) }
            origBloodGroup = bloodGroup
            origGender = gender
            origDateOfBirth = dateOfBirth
            origDistrictId = districtId
            origUpazilaId = upazilaId
            origArea = area
            origLastDonationDate = lastDonationDate
        }
    }

    LaunchedEffect(locState.districts, locState.divisions, state.profile) {
        if (initialized && districtId != null && divisionId == null && locState.districts.isNotEmpty() && locState.divisions.isNotEmpty()) {
            val dist = locState.districts.firstOrNull { it.id == districtId }
            if (dist != null) {
                divisionId = dist.divisionId
                divisionName = locState.divisions.firstOrNull { it.id == dist.divisionId }?.name
            }
        }
    }

    val availabilityAvailable = state.profile?.availabilityStatus == AvailabilityStatus.Available

    val hasChanges = state.profile != null && (
        bloodGroup != origBloodGroup ||
        gender != origGender ||
        dateOfBirth != origDateOfBirth ||
        districtId != origDistrictId ||
        upazilaId != origUpazilaId ||
        area != origArea ||
        lastDonationDate != origLastDonationDate
    )

    LaunchedEffect(hasChanges) {
        if (hasChanges && state.saved) {
            vm.clearSaved()
        }
    }

    val scrollState = rememberScrollState()

    LaunchedEffect(state.saved) {
        if (state.saved) {
            origBloodGroup = bloodGroup
            origGender = gender
            origDateOfBirth = dateOfBirth
            origDistrictId = districtId
            origUpazilaId = upazilaId
            origArea = area
            origLastDonationDate = lastDonationDate
            // Saving can reveal the "Available to donate" card for the first time (it only
            // shows once a profile exists), pushing the button/confirmation further down while
            // scroll offset stays put — follow it so the user isn't left looking at content
            // above where they were.
            scrollState.animateScrollTo(Int.MAX_VALUE)
        }
    }

    // "No profile yet" is an expected state, not a failure — only surface real
    // network/API errors as a transient snackbar.
    val networkError = state.error?.takeUnless {
        it.contains("not found", ignoreCase = true) || it.contains("No donor profile", ignoreCase = true)
    }

    LaunchedEffect(networkError) {
        networkError?.let { snackbarHostState.showSnackbar(it) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(tr("Donor Profile", "দাতার প্রোফাইল")) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = tr("Back", "পেছনে"))
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        if (state.isLoading && !initialized) {
            DonorProfileSkeleton()
        } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scrollState)
                .imePadding()
                .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(tr("Blood Group", "রক্তের গ্রুপ"), style = MaterialTheme.typography.titleMedium)
            BloodGroupChips(
                options = BloodGroup.entries.map { it.label },
                selected = bloodGroup,
                onSelect = { bloodGroup = it; saveDraft() },
            )

            Text(tr("Gender", "লিঙ্গ"), style = MaterialTheme.typography.titleMedium)
            RowChips(
                options = listOf("Male", "Female", "Other"),
                selected = gender ?: "",
                labelOf = { it },
                onSelect = { gender = it; saveDraft() },
            )

            DatePickerField(dateOfBirth, { dateOfBirth = it; saveDraft() }, tr("Date of Birth", "জন্ম তারিখ"))

            Text(tr("Division", "বিভাগ"), style = MaterialTheme.typography.titleMedium)
            PickerField(
                label = divisionName ?: tr("Select Division", "বিভাগ নির্বাচন করুন"),
                options = locState.divisions.map { it.name },
                onSelect = { name ->
                    val d = locState.divisions.firstOrNull { it.name == name }
                    divisionName = name
                    divisionId = d?.id
                    districtId = null
                    districtName = null
                    upazilaId = null
                    upazilaName = null
                    if (d != null) locVm.loadDistricts(d.id)
                    saveDraft()
                },
            )
            Text(tr("District", "জেলা"), style = MaterialTheme.typography.titleMedium)
            PickerField(
                label = districtName ?: tr("Select District", "জেলা নির্বাচন করুন"),
                options = locState.districts.map { it.name },
                onSelect = { name ->
                    val d = locState.districts.firstOrNull { it.name == name }
                    districtName = name
                    districtId = d?.id
                    upazilaId = null
                    upazilaName = null
                    if (d != null) locVm.loadUpazilas(d.id)
                    saveDraft()
                },
            )
            Text(tr("Upazila / Area", "উপজেলা / এলাকা"), style = MaterialTheme.typography.titleMedium)
            PickerField(
                label = upazilaName ?: tr("Select Upazila", "উপজেলা নির্বাচন করুন"),
                options = locState.upazilas.map { it.name },
                onSelect = { name ->
                    val u = locState.upazilas.firstOrNull { it.name == name }
                    upazilaName = name
                    upazilaId = u?.id
                    saveDraft()
                },
            )
            LabeledTextField(area, { area = it; saveDraft() }, tr("Area (optional)", "এলাকা (ঐচ্ছিক)"))
            LabeledTextField(
                customAddress,
                { customAddress = it; saveDraft() },
                tr("Custom Address (if not in dropdown)", "নিজের ঠিকানা লিখুন (তালিকায় না থাকলে)"),
                singleLine = false,
            )
            DatePickerField(lastDonationDate, { lastDonationDate = it; saveDraft() }, tr("Last Donation Date", "সর্বশেষ রক্তদানের তারিখ"))

            if (state.profile != null) {
                val isRecentlyDonated = state.profile?.availabilityStatus == AvailabilityStatus.RecentlyDonated
                val eligibleAgainOn = remember(state.profile?.lastDonationDate) {
                    state.profile?.lastDonationDate?.take(10)?.let { dateStr ->
                        runCatching { java.time.LocalDate.parse(dateStr).plusDays(90) }.getOrNull()
                    }
                }
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column {
                            Text(tr("Available to donate", "রক্তদানের জন্য উপলব্ধ"), style = MaterialTheme.typography.titleMedium)
                            Text(
                                tr(
                                    "Current: ${state.profile?.availabilityStatus?.name ?: "Unknown"}",
                                    "বর্তমান অবস্থা: ${state.profile?.availabilityStatus?.name ?: "অজানা"}",
                                ),
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                        Switch(
                            checked = availabilityAvailable,
                            enabled = !isRecentlyDonated,
                            onCheckedChange = { checked ->
                                vm.updateAvailability(if (checked) AvailabilityStatus.Available else AvailabilityStatus.Unavailable)
                            },
                        )
                    }
                    if (isRecentlyDonated) {
                        Text(
                            text = tr(
                                "Donors need a 90-day gap between donations." + (eligibleAgainOn?.let { " Available again on $it." } ?: ""),
                                "রক্তদানের মধ্যে অন্তত ৯০ দিনের ব্যবধান থাকা প্রয়োজন।" + (eligibleAgainOn?.let { " আবার উপলব্ধ হবেন $it তারিখে।" } ?: ""),
                            ),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 4.dp),
                        )
                    }
                }
            }

            formError?.let { ErrorText(it) }
            if (state.saved) {
                Text(tr("Profile saved", "প্রোফাইল সংরক্ষিত হয়েছে"), color = BloodRed, style = MaterialTheme.typography.labelLarge)
            }

            PrimaryButton(
                text = tr("Save Donor Profile", "দাতার প্রোফাইল সংরক্ষণ করুন"),
                loading = state.isLoading,
                enabled = state.profile == null || hasChanges,
                onClick = {
                    val group = bloodGroup?.let { label -> BloodGroup.entries.firstOrNull { it.label == label } }
                    formError = when {
                        group == null -> selectBloodGroupErr
                        districtId == null -> selectDistrictErr
                        upazilaId == null -> selectUpazilaErr
                        else -> null
                    }
                    if (formError == null) {
                        vm.saveOrUpdate(
                            bloodGroup = group!!,
                            gender = gender,
                            dateOfBirth = dateOfBirth.ifBlank { null },
                            districtId = districtId!!,
                            upazilaId = upazilaId!!,
                            area = area,
                            customAddress = customAddress.ifBlank { null },
                            lastDonationDate = lastDonationDate.ifBlank { null },
                        )
                    }
                },
            )
        }
        }
    }
}
