package com.bloodnetwork.bangladesh.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.Text
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.bloodnetwork.bangladesh.data.model.BloodGroup
import com.bloodnetwork.bangladesh.data.model.Urgency
import com.bloodnetwork.bangladesh.ui.LocalVmFactory
import com.bloodnetwork.bangladesh.ui.components.BloodGroupChips
import com.bloodnetwork.bangladesh.ui.components.DatePickerField
import com.bloodnetwork.bangladesh.ui.components.ErrorText
import com.bloodnetwork.bangladesh.ui.components.LabeledTextField
import com.bloodnetwork.bangladesh.ui.components.PickerField
import com.bloodnetwork.bangladesh.ui.components.PrimaryButton
import com.bloodnetwork.bangladesh.ui.components.RowChips
import com.bloodnetwork.bangladesh.ui.i18n.tr
import com.bloodnetwork.bangladesh.ui.viewmodel.AuthViewModel
import com.bloodnetwork.bangladesh.ui.viewmodel.LocationViewModel
import com.bloodnetwork.bangladesh.ui.viewmodel.RequestBloodViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RequestBloodScreen(onBack: () -> Unit, authVm: AuthViewModel) {
    val factory = LocalVmFactory.current!!
    val vm: RequestBloodViewModel = viewModel(factory = factory)
    val locVm: LocationViewModel = viewModel(factory = factory)

    val state by vm.uiState.collectAsStateWithLifecycle()
    val locState by locVm.uiState.collectAsStateWithLifecycle()

    var bloodGroup by remember { mutableStateOf<String?>(null) }
    var units by remember { mutableStateOf("") }
    var hospitalName by remember { mutableStateOf("") }
    var hospitalAddress by remember { mutableStateOf("") }
    var divisionId by remember { mutableStateOf<String?>(null) }
    var divisionName by remember { mutableStateOf<String?>(null) }
    var districtId by remember { mutableStateOf<String?>(null) }
    var districtName by remember { mutableStateOf<String?>(null) }
    var upazilaId by remember { mutableStateOf<String?>(null) }
    var upazilaName by remember { mutableStateOf<String?>(null) }
    var area by remember { mutableStateOf("") }
    var requiredBy by remember { mutableStateOf("") }
    var urgency by remember { mutableStateOf<Urgency>(Urgency.Normal) }
    var patientName by remember { mutableStateOf("") }
    var patientRelation by remember { mutableStateOf("") }
    var contactPhone by remember { mutableStateOf("") }
    var additionalInfo by remember { mutableStateOf("") }
    var formError by remember { mutableStateOf<String?>(null) }
    var justSubmitted by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    val requiredFilled = bloodGroup != null && units.toIntOrNull() != null && hospitalName.isNotBlank() && districtId != null && upazilaId != null && requiredBy.isNotBlank() && contactPhone.length >= 10

    val backLabel = tr("Back", "ফিরে যান")
    val requestBloodTitle = tr("Request Blood", "রক্তের জন্য অনুরোধ")
    val bloodGroupLabel = tr("Blood Group", "রক্তের গ্রুপ")
    val unitsRequiredLabel = tr("Units Required", "প্রয়োজনীয় ইউনিট")
    val hospitalNameLabel = tr("Hospital Name", "হাসপাতালের নাম")
    val hospitalAddressLabel = tr("Hospital Address", "হাসপাতালের ঠিকানা")
    val divisionLabel = tr("Division", "বিভাগ")
    val selectDivisionLabel = tr("Select Division", "বিভাগ নির্বাচন করুন")
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
    val submitRequestLabel = tr("Submit Request", "অনুরোধ জমা দিন")
    val requestSubmittedLabel = tr("Request Submitted", "অনুরোধ জমা হয়েছে")
    val requestSubmittedMsg = tr("Your blood request has been submitted", "আপনার রক্তের অনুরোধ জমা দেওয়া হয়েছে")
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
        locVm.loadDivisions()

        // Came here from "Request Blood" on a specific donor's card (possibly via a Login
        // detour) — pre-fill their blood group and location instead of opening blank.
        authVm.pendingRequestPrefill?.let { prefill ->
            authVm.pendingRequestPrefill = null
            bloodGroup = prefill.bloodGroup.label
            districtId = prefill.districtId
            districtName = prefill.districtName
            upazilaId = prefill.upazilaId
            upazilaName = prefill.upazilaName
            locVm.loadUpazilas(prefill.districtId)
        }
    }

    LaunchedEffect(state.success) {
        if (state.success) {
            bloodGroup = null
            units = ""
            hospitalName = ""
            hospitalAddress = ""
            requiredBy = ""
            formError = null
            vm.clearSuccess()
            justSubmitted = true
            launch { snackbarHostState.showSnackbar(requestSubmittedMsg) }
            delay(2000)
            justSubmitted = false
        }
    }

    LaunchedEffect(state.error) {
        state.error?.let { snackbarHostState.showSnackbar(it) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(requestBloodTitle) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = backLabel)
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
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(bloodGroupLabel, style = MaterialTheme.typography.titleMedium)
            BloodGroupChips(
                options = BloodGroup.entries.map { it.label },
                selected = bloodGroup,
                onSelect = { bloodGroup = it },
            )

            LabeledTextField(units, { units = it }, unitsRequiredLabel, keyboardType = KeyboardType.Number)
            LabeledTextField(hospitalName, { hospitalName = it }, hospitalNameLabel, singleLine = false)
            LabeledTextField(hospitalAddress, { hospitalAddress = it }, hospitalAddressLabel, singleLine = false)

            Text(divisionLabel, style = MaterialTheme.typography.titleMedium)
            PickerField(
                label = divisionName ?: selectDivisionLabel,
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
                },
            )
            Text(districtLabel, style = MaterialTheme.typography.titleMedium)
            PickerField(
                label = districtName ?: selectDistrictLabel,
                options = locState.districts.map { it.name },
                onSelect = { name ->
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
                    val u = locState.upazilas.firstOrNull { it.name == name }
                    upazilaName = name
                    upazilaId = u?.id
                },
            )
            LabeledTextField(area, { area = it }, areaOptionalLabel)

            Text(urgencyLabel, style = MaterialTheme.typography.titleMedium)
            RowChips(
                options = Urgency.entries.toList(),
                selected = urgency,
                labelOf = { urgencyLabels[it] ?: it.name },
                onSelect = { urgency = it },
            )

            DatePickerField(
                requiredBy,
                { requiredBy = it },
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
            LabeledTextField(patientName, { patientName = it }, patientNameLabel)
            LabeledTextField(patientRelation, { patientRelation = it }, patientRelationLabel)
            LabeledTextField(contactPhone, { contactPhone = it }, contactPhoneLabel, keyboardType = KeyboardType.Phone)
            LabeledTextField(additionalInfo, { additionalInfo = it }, additionalInfoLabel, singleLine = false)

            formError?.let { ErrorText(it) }

            PrimaryButton(
                text = if (justSubmitted) requestSubmittedLabel else submitRequestLabel,
                loading = state.isLoading,
                enabled = requiredFilled && !justSubmitted,
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
                        vm.submit(
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
        }
    }
}
