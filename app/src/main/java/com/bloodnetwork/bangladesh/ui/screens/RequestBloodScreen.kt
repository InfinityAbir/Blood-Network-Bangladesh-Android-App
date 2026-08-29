package com.bloodnetwork.bangladesh.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
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
import com.bloodnetwork.bangladesh.data.model.BloodGroup
import com.bloodnetwork.bangladesh.data.model.Urgency
import com.bloodnetwork.bangladesh.ui.LocalVmFactory
import com.bloodnetwork.bangladesh.ui.components.BloodGroupChips
import com.bloodnetwork.bangladesh.ui.components.ErrorText
import com.bloodnetwork.bangladesh.ui.components.LabeledTextField
import com.bloodnetwork.bangladesh.ui.components.PickerField
import com.bloodnetwork.bangladesh.ui.components.PrimaryButton
import com.bloodnetwork.bangladesh.ui.components.RowChips
import com.bloodnetwork.bangladesh.ui.components.SkeletonCard
import com.bloodnetwork.bangladesh.ui.theme.BloodRed
import com.bloodnetwork.bangladesh.ui.viewmodel.AuthViewModel
import com.bloodnetwork.bangladesh.ui.viewmodel.LocationViewModel
import com.bloodnetwork.bangladesh.ui.viewmodel.RequestBloodViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RequestBloodScreen(onNavigate: (String) -> Unit, onBack: () -> Unit, authVm: AuthViewModel) {
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
    val snackbarHostState = remember { SnackbarHostState() }

    val requiredFilled = bloodGroup != null && units.toIntOrNull() != null && hospitalName.isNotBlank() && districtId != null && upazilaId != null && requiredBy.isNotBlank() && contactPhone.length >= 10

    LaunchedEffect(Unit) {
        locVm.loadDivisions()
        vm.loadMyRequests()

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
        }
    }

    LaunchedEffect(state.error) {
        state.error?.let { snackbarHostState.showSnackbar(it) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Request Blood") },
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
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text("Blood Group", style = MaterialTheme.typography.titleMedium)
            BloodGroupChips(
                options = BloodGroup.entries.map { it.label },
                selected = bloodGroup,
                onSelect = { bloodGroup = it },
            )

            LabeledTextField(units, { units = it }, "Units Required", keyboardType = KeyboardType.Number)
            LabeledTextField(hospitalName, { hospitalName = it }, "Hospital Name", singleLine = false)
            LabeledTextField(hospitalAddress, { hospitalAddress = it }, "Hospital Address", singleLine = false)

            Text("Division", style = MaterialTheme.typography.titleMedium)
            PickerField(
                label = divisionName ?: "Select Division",
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
            Text("District", style = MaterialTheme.typography.titleMedium)
            PickerField(
                label = districtName ?: "Select District",
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
            Text("Upazila / Area", style = MaterialTheme.typography.titleMedium)
            PickerField(
                label = upazilaName ?: "Select Upazila",
                options = locState.upazilas.map { it.name },
                onSelect = { name ->
                    val u = locState.upazilas.firstOrNull { it.name == name }
                    upazilaName = name
                    upazilaId = u?.id
                },
            )
            LabeledTextField(area, { area = it }, "Area (optional)")

            Text("Urgency", style = MaterialTheme.typography.titleMedium)
            RowChips(
                options = Urgency.entries.toList(),
                selected = urgency,
                labelOf = { it.name },
                onSelect = { urgency = it },
            )

            LabeledTextField(requiredBy, { requiredBy = it }, "Required By (YYYY-MM-DD)", placeholder = "2026-12-31")
            LabeledTextField(patientName, { patientName = it }, "Patient Name (optional)")
            LabeledTextField(patientRelation, { patientRelation = it }, "Patient Relation (optional)")
            LabeledTextField(contactPhone, { contactPhone = it }, "Contact Phone", keyboardType = KeyboardType.Phone)
            LabeledTextField(additionalInfo, { additionalInfo = it }, "Additional Info (optional)", singleLine = false)

            formError?.let { ErrorText(it) }

            if (state.success && state.myRequests.isNotEmpty()) {
                Text("Your recent requests", style = MaterialTheme.typography.titleMedium)
            }

            PrimaryButton(
                text = "Submit Request",
                loading = state.isLoading,
                enabled = requiredFilled,
                onClick = {
                    val group = bloodGroup?.let { label -> BloodGroup.entries.firstOrNull { it.label == label } }
                    formError = when {
                        group == null -> "Select a blood group"
                        units.toIntOrNull() == null -> "Enter units required"
                        hospitalName.isBlank() -> "Enter hospital name"
                        districtId == null -> "Select a district"
                        upazilaId == null -> "Select an upazila"
                        requiredBy.isBlank() || !Regex("\\d{4}-\\d{2}-\\d{2}").matches(requiredBy.trim()) ->
                            "Enter required-by date as YYYY-MM-DD"
                        contactPhone.length < 10 -> "Enter a valid contact phone"
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

            if (state.loadingRequests && state.myRequests.isEmpty()) {
                Text("My Requests", style = MaterialTheme.typography.titleLarge)
                Card(modifier = Modifier.fillMaxWidth()) { SkeletonCard() }
            } else if (state.myRequests.isNotEmpty()) {
                Text("My Requests", style = MaterialTheme.typography.titleLarge)
                state.myRequests.forEach { req ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        androidx.compose.foundation.layout.Column(modifier = Modifier.padding(12.dp)) {
                            androidx.compose.foundation.layout.Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Text(req.bloodGroup.label, color = BloodRed, style = MaterialTheme.typography.titleMedium)
                                Text(req.status.name, style = MaterialTheme.typography.labelLarge)
                            }
                            Text("${req.hospitalName} · ${req.unitsRequired} unit(s)", style = MaterialTheme.typography.bodyMedium)
                            Text(req.requiredBy, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
    }
}
