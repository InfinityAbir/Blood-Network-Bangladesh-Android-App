package com.bloodnetwork.bangladesh.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Route
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bloodnetwork.bangladesh.data.model.BloodGroup
import com.bloodnetwork.bangladesh.data.model.DonorResponse
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
    val factory = LocalVmFactory.current
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
    var showFulfillDialog by remember { mutableStateOf(false) }
    var fulfillUnits by remember { mutableStateOf("") }
    var fulfillNotes by remember { mutableStateOf("") }
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
    val requestFulfilledMsg = tr("Request fulfillment recorded", "অনুরোধ পূরণ রেকর্ড করা হয়েছে")
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

    LaunchedEffect(state.fulfilled) {
        if (state.fulfilled) {
            snackbarHostState.showSnackbar(requestFulfilledMsg)
            vm.clearFulfilled()
        }
    }

    LaunchedEffect(state.error) {
        state.error?.let { snackbarHostState.showSnackbar(it) }
    }

    val editable = state.request?.status == RequestStatus.Open || state.request?.status == RequestStatus.PartiallyFulfilled
    val remaining = state.request?.let { it.unitsRequired - it.unitsFulfilled } ?: 0
    val canFulfill = editable && remaining > 0
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

    if (showFulfillDialog) {
        val maxFulfill = minOf(10, remaining.coerceAtLeast(0))
        val fulfillUnitsInt = fulfillUnits.toIntOrNull()
        val fulfillError = when {
            fulfillUnits.isBlank() -> tr("Enter units delivered", "প্রদান করা ইউনিট লিখুন")
            fulfillUnitsInt == null || fulfillUnitsInt <= 0 -> tr("Units must be at least 1", "ইউনিট অন্তত ১ হতে হবে")
            fulfillUnitsInt > remaining -> tr("Cannot exceed $remaining remaining units", "$remaining ইউনিটের বেশি দেওয়া যাবে না")
            fulfillUnitsInt > 10 -> tr("Cannot fulfill more than 10 units at once", "একবারে ১০ ইউনিটের বেশি পূরণ করা যাবে না")
            else -> null
        }
        AlertDialog(
            onDismissRequest = { showFulfillDialog = false },
            title = { Text(tr("Mark as fulfilled", "পূরণ হিসেবে চিহ্নিত করুন")) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        tr("How many units were delivered? ($remaining of ${state.request?.unitsRequired ?: 0} remaining, max 10)", "কত ইউনিট প্রদান করা হয়েছে? ($remaining টি বাকি, সর্বোচ্চ ১০)"),
                        style = MaterialTheme.typography.bodySmall,
                    )
                    LabeledTextField(
                        value = fulfillUnits,
                        onValueChange = { fulfillUnits = it.filter { c -> c.isDigit() }.take(2) },
                        label = tr("Units delivered (1-$maxFulfill)", "প্রদানকৃত ইউনিট (১-$maxFulfill)"),
                        keyboardType = KeyboardType.Number,
                    )
                    LabeledTextField(
                        value = fulfillNotes,
                        onValueChange = { fulfillNotes = it },
                        label = tr("Notes (optional)", "নোট (ঐচ্ছিক)"),
                        singleLine = false,
                    )
                    if (fulfillError != null && fulfillUnits.isNotBlank()) {
                        Text(fulfillError, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (fulfillError == null && fulfillUnitsInt != null) {
                            showFulfillDialog = false
                            vm.fulfill(requestId, fulfillUnitsInt, fulfillNotes.ifBlank { null })
                            fulfillUnits = ""
                            fulfillNotes = ""
                        }
                    },
                    enabled = fulfillError == null,
                ) { Text(tr("Confirm", "নিশ্চিত করুন"), color = BloodRed) }
            },
            dismissButton = {
                TextButton(onClick = { showFulfillDialog = false }) { Text(tr("Cancel", "বাতিল")) }
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

            // G2: matched donors for this request (requester view)
            val context = LocalContext.current
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text(tr("Matched Donors", "মিলে যাওয়া দাতা"), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                        if (state.isLoadingMatches) CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                        else Text(tr("${state.matches.size} donors", "${state.matches.size} জন দাতা"), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    when {
                        state.isLoadingMatches && state.matches.isEmpty() -> {
                            Text(tr("Loading matches...", "মিল লোড হচ্ছে..."), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        state.matchesError != null -> {
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(state.matchesError ?: "", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                                Button(onClick = { vm.loadMatches(requestId) }, colors = ButtonDefaults.buttonColors(containerColor = BloodRed), shape = RoundedCornerShape(50), contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 14.dp, vertical = 0.dp), modifier = Modifier.height(32.dp)) {
                                    Text(tr("Retry", "আবার চেষ্টা করুন"), style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }
                        state.matches.isEmpty() -> {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                Box(modifier = Modifier.size(36.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceVariant), contentAlignment = Alignment.Center) {
                                    Icon(Icons.Filled.Handshake, contentDescription = null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Text(tr("No donors matched yet", "এখনো কোনো দাতা মেলেনি"), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        else -> {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                state.matches.forEach { match ->
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.weight(1f)) {
                                                    if (!match.donorPhotoUrl.isNullOrBlank()) {
                                                        com.bloodnetwork.bangladesh.ui.components.Avatar(photoUrl = match.donorPhotoUrl, size = 32.dp)
                                                    } else {
                                                        Box(modifier = Modifier.size(32.dp).clip(CircleShape).background(BloodRed.copy(alpha = 0.12f)), contentAlignment = Alignment.Center) {
                                                            Text(
                                                                text = match.donorBloodGroup.take(3),
                                                                style = MaterialTheme.typography.labelSmall,
                                                                color = BloodRed,
                                                                fontWeight = FontWeight.Bold,
                                                                maxLines = 1,
                                                                overflow = TextOverflow.Ellipsis,
                                                                textAlign = TextAlign.Center,
                                                            )
                                                        }
                                                    }
                                                    Column(modifier = Modifier.weight(1f)) {
                                                        Text(match.donorName.ifBlank { tr("Unknown donor", "অজানা দাতা") }, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                            Icon(Icons.Filled.LocalHospital, contentDescription = null, modifier = Modifier.size(12.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                                            Text(match.hospitalName.ifBlank { tr("Unknown hospital", "অজানা হাসপাতাল") }, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
                                                        }
                                                    }
                                                }
                                                Spacer(Modifier.width(8.dp))
                                                RequesterResponsePill(match.donorResponse)
                                            }
                                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                                                match.distanceKm?.let { km ->
                                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                                        Icon(Icons.Filled.Route, contentDescription = null, modifier = Modifier.size(12.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                                        Text("%.1f km".format(km), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                    }
                                                }
                                                Text(tr("Score ${match.matchScore}", "স্কোর ${match.matchScore}"), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            }
                                            if (match.donorPhone.isNotBlank() && match.donorResponse == DonorResponse.Accepted) {
                                                Button(
                                                    onClick = { context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:${match.donorPhone}"))) },
                                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                                                    shape = RoundedCornerShape(50),
                                                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                                                    modifier = Modifier.height(32.dp),
                                                ) {
                                                    Icon(Icons.Filled.Call, contentDescription = null, modifier = Modifier.size(14.dp))
                                                    Spacer(Modifier.width(6.dp))
                                                    Text(tr("Call ${match.donorName}", "${match.donorName} কে কল করুন"), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold, maxLines = 1)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
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
                if (canFulfill) {
                    PrimaryButton(
                        text = tr("Mark as fulfilled", "পূরণ হিসেবে চিহ্নিত করুন"),
                        loading = state.isFulfilling,
                        enabled = !state.isFulfilling,
                        onClick = {
                            fulfillUnits = remaining.coerceAtMost(10).toString()
                            showFulfillDialog = true
                        },
                    )
                }
                OutlinedButton(
                    onClick = { showCancelConfirm = true },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !state.isCancelling,
                ) { Text(if (state.isCancelling) cancellingLabel else cancelRequestLabel, color = BloodRed) }
            }
        }
    }
}

private fun requesterResponseAccentColor(response: DonorResponse): Color = when (response) {
    DonorResponse.Accepted -> Color(0xFF2E7D32)
    DonorResponse.Declined -> Color(0xFFC62828)
    DonorResponse.NoResponse -> Color(0xFF616161)
    else -> Color(0xFFEF6C00)
}

@Composable
private fun RequesterResponsePill(response: DonorResponse) {
    val accent = requesterResponseAccentColor(response)
    val displayText = when (response) {
        DonorResponse.Accepted -> tr("Accepted", "গৃহীত")
        DonorResponse.Declined -> tr("Declined", "প্রত্যাখ্যাত")
        DonorResponse.NoResponse -> tr("No Response", "সাড়া দেয়নি")
        DonorResponse.Pending -> tr("Pending", "মুলতুবি")
    }
    Box(modifier = Modifier.clip(RoundedCornerShape(50)).background(accent.copy(alpha = 0.12f)).padding(horizontal = 10.dp, vertical = 5.dp)) {
        Text(displayText, style = MaterialTheme.typography.labelSmall, color = accent, fontWeight = FontWeight.SemiBold, maxLines = 1)
    }
}
