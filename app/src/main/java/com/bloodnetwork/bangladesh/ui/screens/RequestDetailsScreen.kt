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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bloodnetwork.bangladesh.data.model.BloodGroup
import com.bloodnetwork.bangladesh.data.model.RequestStatus
import com.bloodnetwork.bangladesh.data.model.Urgency
import com.bloodnetwork.bangladesh.ui.LocalVmFactory
import com.bloodnetwork.bangladesh.ui.components.BloodGroupChips
import com.bloodnetwork.bangladesh.ui.components.DatePickerField
import com.bloodnetwork.bangladesh.ui.components.ErrorText
import com.bloodnetwork.bangladesh.ui.components.LabeledTextField
import com.bloodnetwork.bangladesh.ui.components.PickerField
import com.bloodnetwork.bangladesh.ui.components.PrimaryButton
import com.bloodnetwork.bangladesh.ui.components.RowChips
import com.bloodnetwork.bangladesh.ui.components.SkeletonCard
import com.bloodnetwork.bangladesh.ui.i18n.tr
import com.bloodnetwork.bangladesh.ui.theme.BloodRed
import com.bloodnetwork.bangladesh.ui.viewmodel.LocationViewModel
import com.bloodnetwork.bangladesh.ui.viewmodel.RequestDetailsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RequestDetailsScreen(requestId: String, onBack: () -> Unit) {
    val factory = LocalVmFactory.current!!
    val vm: RequestDetailsViewModel = viewModel(factory = factory)
    val locVm: LocationViewModel = viewModel(factory = factory)

    val state by vm.uiState.collectAsStateWithLifecycle()
    val locState by locVm.uiState.collectAsStateWithLifecycle()

    var seededForId by remember { mutableStateOf<String?>(null) }

    var bloodGroup by remember { mutableStateOf<String?>(null) }
    var units by remember { mutableStateOf("") }
    var hospitalName by remember { mutableStateOf("") }
    var hospitalAddress by remember { mutableStateOf("") }
    var districtId by remember { mutableStateOf<String?>(null) }
    var districtName by remember { mutableStateOf<String?>(null) }
    var upazilaId by remember { mutableStateOf<String?>(null) }
    var upazilaName by remember { mutableStateOf<String?>(null) }
    var area by remember { mutableStateOf("") }
    var requiredBy by remember { mutableStateOf("") }
    var urgency by remember { mutableStateOf(Urgency.Normal) }
    var patientName by remember { mutableStateOf("") }
    var patientRelation by remember { mutableStateOf("") }
    var contactPhone by remember { mutableStateOf("") }
    var additionalInfo by remember { mutableStateOf("") }
    var formError by remember { mutableStateOf<String?>(null) }
    var showCancelConfirm by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    val backLabel = tr("Back", "ফিরে যান")
    val requestDetailsTitle = tr("Request Details", "অনুরোধের বিস্তারিত")
    val bloodGroupLabel = tr("Blood Group", "রক্তের গ্রুপ")
    val unitsRequiredLabel = tr("Units Required", "প্রয়োজনীয় ইউনিট")
    val hospitalNameLabel = tr("Hospital Name", "হাসপাতালের নাম")
    val hospitalAddressLabel = tr("Hospital Address", "হাসপাতালের ঠিকানা")
    val districtLabel = tr("District", "জেলা")
    val selectDistrictLabel = tr("Select District", "জেলা নির্বাচন করুন")
    val upazilaAreaLabel = tr("Upazila / Area", "উপজেলা / এলাকা")
    val selectUpazilaLabel = tr("Select Upazila", "উপজেলা নির্বাচন করুন")
    val areaOptionalLabel = tr("Area (optional)", "এলাকা (ঐচ্ছিক)")
    val urgencyLabel = tr("Urgency", "জরুরির মাত্রা")
    val requiredByLabel = tr("Required By", "প্রয়োজনের তারিখ")
    val patientNameLabel = tr("Patient Name (optional)", "রোগীর নাম (ঐচ্ছিক)")
    val patientRelationLabel = tr("Patient Relation (optional)", "রোগীর সাথে সম্পর্ক (ঐচ্ছিক)")
    val contactPhoneLabel = tr("Contact Phone", "যোগাযোগের ফোন নম্বর")
    val additionalInfoLabel = tr("Additional Info (optional)", "অতিরিক্ত তথ্য (ঐচ্ছিক)")
    val saveChangesLabel = tr("Save Changes", "পরিবর্তন সংরক্ষণ করুন")
    val cancellingLabel = tr("Cancelling...", "বাতিল হচ্ছে...")
    val cancelRequestLabel = tr("Cancel Request", "অনুরোধ বাতিল করুন")
    val cancelDialogTitle = tr("Cancel this request?", "এই অনুরোধটি বাতিল করবেন?")
    val cancelDialogText = tr(
        "This can't be undone. Matched donors will be notified.",
        "এটি পূর্বাবস্থায় ফেরানো যাবে না। মিলে যাওয়া দাতাদের জানানো হবে।",
    )
    val keepItLabel = tr("Keep It", "রেখে দিন")
    val requestUpdatedMsg = tr("Request updated", "অনুরোধ হালনাগাদ করা হয়েছে")
    val urgencyLabels: Map<Urgency, String> = mapOf(
        Urgency.Critical to tr("Critical", "সংকটাপন্ন"),
        Urgency.Urgent to tr("Urgent", "জরুরি"),
        Urgency.Normal to tr("Normal", "স্বাভাবিক"),
    )

    val selectBloodGroupErr = tr("Select a blood group", "একটি রক্তের গ্রুপ নির্বাচন করুন")
    val enterUnitsErr = tr("Enter units required", "প্রয়োজনীয় ইউনিট লিখুন")
    val enterHospitalNameErr = tr("Enter hospital name", "হাসপাতালের নাম লিখুন")
    val selectDistrictErr = tr("Select a district", "একটি জেলা নির্বাচন করুন")
    val selectUpazilaErr = tr("Select an upazila", "একটি উপজেলা নির্বাচন করুন")
    val enterRequiredByErr = tr("Enter required-by date as YYYY-MM-DD", "প্রয়োজনের তারিখ YYYY-MM-DD আকারে লিখুন")
    val enterValidPhoneErr = tr("Enter a valid contact phone", "একটি সঠিক যোগাযোগের ফোন নম্বর লিখুন")

    LaunchedEffect(Unit) {
        locVm.loadAllDistricts()
        vm.load(requestId)
    }

    // Seed the editable form from the loaded request exactly once (not on every
    // recomposition), so in-progress edits aren't clobbered by state re-emissions.
    LaunchedEffect(state.request) {
        val req = state.request ?: return@LaunchedEffect
        if (seededForId == req.id) return@LaunchedEffect
        seededForId = req.id
        bloodGroup = req.bloodGroup.label
        units = req.unitsRequired.toString()
        hospitalName = req.hospitalName
        hospitalAddress = req.hospitalAddress
        districtId = req.districtId
        districtName = req.districtName
        upazilaId = req.upazilaId
        upazilaName = req.upazilaName
        area = req.area.orEmpty()
        requiredBy = req.requiredBy.take(10)
        urgency = req.urgency
        patientName = req.patientName.orEmpty()
        patientRelation = req.patientRelation.orEmpty()
        contactPhone = req.contactPhone
        additionalInfo = req.additionalInformation.orEmpty()
        locVm.loadUpazilas(req.districtId)
    }

    LaunchedEffect(state.saved) {
        if (state.saved) {
            snackbarHostState.showSnackbar(requestUpdatedMsg)
            vm.clearSaved()
        }
    }

    LaunchedEffect(state.cancelled) {
        if (state.cancelled) onBack()
    }

    LaunchedEffect(state.error) {
        state.error?.let { snackbarHostState.showSnackbar(it) }
    }

    val editable = state.request?.status == RequestStatus.Open || state.request?.status == RequestStatus.PartiallyFulfilled
    val requiredFilled = bloodGroup != null && units.toIntOrNull() != null && hospitalName.isNotBlank() &&
        districtId != null && upazilaId != null && requiredBy.isNotBlank() && contactPhone.length >= 10

    if (showCancelConfirm) {
        AlertDialog(
            onDismissRequest = { showCancelConfirm = false },
            title = { Text(cancelDialogTitle) },
            text = { Text(cancelDialogText) },
            confirmButton = {
                TextButton(onClick = { showCancelConfirm = false; vm.cancel(requestId) }) { Text(cancelRequestLabel, color = BloodRed) }
            },
            dismissButton = {
                TextButton(onClick = { showCancelConfirm = false }) { Text(keepItLabel) }
            },
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(requestDetailsTitle) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = backLabel)
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        if (state.isLoading && state.request == null) {
            Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
                Card(modifier = Modifier.fillMaxWidth()) { SkeletonCard() }
            }
            return@Scaffold
        }

        val req = state.request ?: return@Scaffold

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(req.bloodGroup.label, color = BloodRed, style = MaterialTheme.typography.titleMedium)
                        Text(req.status.name, style = MaterialTheme.typography.labelLarge)
                    }
                    Text(
                        tr(
                            "${req.unitsFulfilled} of ${req.unitsRequired} unit(s) fulfilled",
                            "${req.unitsRequired} ইউনিটের মধ্যে ${req.unitsFulfilled} ইউনিট পূরণ হয়েছে",
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Text(
                        tr("Posted ${req.createdAt.take(10)}", "পোস্ট করা হয়েছে ${req.createdAt.take(10)}"),
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }

            if (!editable) {
                val statusWord = req.status.name.lowercase()
                ErrorText(
                    tr(
                        "This request is $statusWord and can no longer be edited or cancelled.",
                        "এই অনুরোধটি বর্তমানে $statusWord অবস্থায় আছে এবং আর সম্পাদনা বা বাতিল করা যাবে না।",
                    ),
                )
            }

            Text(bloodGroupLabel, style = MaterialTheme.typography.titleMedium)
            BloodGroupChips(
                options = BloodGroup.entries.map { it.label },
                selected = bloodGroup,
                onSelect = { if (editable) bloodGroup = it },
            )

            LabeledTextField(units, { if (editable) units = it }, unitsRequiredLabel, keyboardType = KeyboardType.Number)
            LabeledTextField(hospitalName, { if (editable) hospitalName = it }, hospitalNameLabel, singleLine = false)
            LabeledTextField(hospitalAddress, { if (editable) hospitalAddress = it }, hospitalAddressLabel, singleLine = false)

            Text(districtLabel, style = MaterialTheme.typography.titleMedium)
            PickerField(
                label = districtName ?: selectDistrictLabel,
                options = locState.districts.map { it.name },
                onSelect = { name ->
                    if (!editable) return@PickerField
                    val d = locState.districts.firstOrNull { it.name == name }
                    districtName = name
                    districtId = d?.id
                    upazilaId = null
                    upazilaName = null
                    if (d != null) locVm.loadUpazilas(d.id)
                },
            )
            Text(upazilaAreaLabel, style = MaterialTheme.typography.titleMedium)
            PickerField(
                label = upazilaName ?: selectUpazilaLabel,
                options = locState.upazilas.map { it.name },
                onSelect = { name ->
                    if (!editable) return@PickerField
                    val u = locState.upazilas.firstOrNull { it.name == name }
                    upazilaName = name
                    upazilaId = u?.id
                },
            )
            LabeledTextField(area, { if (editable) area = it }, areaOptionalLabel)

            Text(urgencyLabel, style = MaterialTheme.typography.titleMedium)
            RowChips(
                options = Urgency.entries.toList(),
                selected = urgency,
                labelOf = { urgencyLabels[it] ?: it.name },
                onSelect = { if (editable) urgency = it },
            )

            DatePickerField(
                requiredBy,
                { if (editable) requiredBy = it },
                requiredByLabel,
                minDateMillis = remember {
                    java.util.Calendar.getInstance().apply {
                        set(java.util.Calendar.HOUR_OF_DAY, 0)
                        set(java.util.Calendar.MINUTE, 0)
                        set(java.util.Calendar.SECOND, 0)
                        set(java.util.Calendar.MILLISECOND, 0)
                    }.timeInMillis
                },
            )
            LabeledTextField(patientName, { if (editable) patientName = it }, patientNameLabel)
            LabeledTextField(patientRelation, { if (editable) patientRelation = it }, patientRelationLabel)
            LabeledTextField(contactPhone, { if (editable) contactPhone = it }, contactPhoneLabel, keyboardType = KeyboardType.Phone)
            LabeledTextField(additionalInfo, { if (editable) additionalInfo = it }, additionalInfoLabel, singleLine = false)

            formError?.let { ErrorText(it) }

            if (editable) {
                PrimaryButton(
                    text = saveChangesLabel,
                    loading = state.isSaving,
                    enabled = requiredFilled,
                    onClick = {
                        val group = bloodGroup?.let { label -> BloodGroup.entries.firstOrNull { it.label == label } }
                        formError = when {
                            group == null -> selectBloodGroupErr
                            units.toIntOrNull() == null -> enterUnitsErr
                            hospitalName.isBlank() -> enterHospitalNameErr
                            districtId == null -> selectDistrictErr
                            upazilaId == null -> selectUpazilaErr
                            requiredBy.isBlank() || !Regex("\\d{4}-\\d{2}-\\d{2}").matches(requiredBy.trim()) ->
                                enterRequiredByErr
                            contactPhone.length < 10 -> enterValidPhoneErr
                            else -> null
                        }
                        if (formError == null) {
                            vm.save(
                                id = requestId,
                                bloodGroup = group!!,
                                unitsRequired = units.toInt(),
                                hospitalName = hospitalName,
                                hospitalAddress = hospitalAddress,
                                districtId = districtId!!,
                                upazilaId = upazilaId!!,
                                area = area,
                                requiredBy = requiredBy.trim(),
                                urgency = urgency,
                                patientName = patientName,
                                patientRelation = patientRelation,
                                contactPhone = contactPhone,
                                additionalInformation = additionalInfo,
                            )
                        }
                    },
                )
                OutlinedButton(
                    onClick = { showCancelConfirm = true },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !state.isCancelling,
                ) { Text(if (state.isCancelling) cancellingLabel else cancelRequestLabel, color = BloodRed) }
            }
        }
    }
}
