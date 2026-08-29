package com.bloodnetwork.bangladesh.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Card
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bloodnetwork.bangladesh.data.model.BloodGroup
import com.bloodnetwork.bangladesh.data.model.PublicDonorDto
import com.bloodnetwork.bangladesh.ui.LocalVmFactory
import com.bloodnetwork.bangladesh.ui.components.BloodGroupChips
import com.bloodnetwork.bangladesh.ui.components.PickerField
import com.bloodnetwork.bangladesh.ui.components.PrimaryButton
import com.bloodnetwork.bangladesh.ui.components.RoleBadge
import com.bloodnetwork.bangladesh.ui.components.SearchResultsSkeleton
import com.bloodnetwork.bangladesh.ui.theme.BloodRed
import com.bloodnetwork.bangladesh.ui.navigation.Routes
import com.bloodnetwork.bangladesh.ui.viewmodel.AuthViewModel
import com.bloodnetwork.bangladesh.ui.viewmodel.FindBloodViewModel
import com.bloodnetwork.bangladesh.ui.viewmodel.LocationViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FindBloodScreen(onNavigate: (String) -> Unit, onBack: () -> Unit, authVm: AuthViewModel) {
    val factory = LocalVmFactory.current!!
    val vm: FindBloodViewModel = viewModel(factory = factory)
    val locVm: LocationViewModel = viewModel(factory = factory)

    val state by vm.uiState.collectAsStateWithLifecycle()
    val locState by locVm.uiState.collectAsStateWithLifecycle()
    val isLoggedIn by vm.isLoggedIn.collectAsStateWithLifecycle()

    var selectedGroup by remember { mutableStateOf<String?>(null) }
    var selectedDivisionId by remember { mutableStateOf<String?>(null) }
    var selectedDivisionName by remember { mutableStateOf<String?>(null) }
    var selectedDistrictId by remember { mutableStateOf<String?>(null) }
    var selectedUpazilaId by remember { mutableStateOf<String?>(null) }
    var selectedDistrictName by remember { mutableStateOf<String?>(null) }
    var selectedUpazilaName by remember { mutableStateOf<String?>(null) }

    val hasCriteria = selectedGroup != null || selectedDistrictId != null
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) { locVm.loadDivisions() }

    LaunchedEffect(state.error) {
        state.error?.let { snackbarHostState.showSnackbar(it) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Find Blood") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        val listState = rememberLazyListState()
        val shouldLoadMore by remember {
            derivedStateOf {
                val last = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
                val total = listState.layoutInfo.totalItemsCount
                total > 0 && last >= total - 3
            }
        }
        LaunchedEffect(shouldLoadMore, state.hasMore) {
            if (shouldLoadMore && state.hasMore) vm.loadMore()
        }

        PullToRefreshBox(
            isRefreshing = state.isRefreshing,
            onRefresh = { vm.refresh() },
            modifier = Modifier.fillMaxSize().padding(padding),
        ) {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                Text("Blood Group", style = MaterialTheme.typography.titleMedium)
                BloodGroupChips(
                    options = BloodGroup.entries.map { it.label },
                    selected = selectedGroup,
                    onSelect = { selectedGroup = it },
                )
            }
            item {
                Text("Division", style = MaterialTheme.typography.titleMedium)
                PickerField(
                    label = selectedDivisionName ?: "Select Division",
                    options = locState.divisions.map { it.name },
                    onSelect = { name ->
                        val d = locState.divisions.firstOrNull { it.name == name }
                        selectedDivisionName = name
                        selectedDivisionId = d?.id
                        selectedDistrictId = null
                        selectedDistrictName = null
                        selectedUpazilaId = null
                        selectedUpazilaName = null
                        if (d != null) locVm.loadDistricts(d.id)
                    },
                )
                Text("District", style = MaterialTheme.typography.titleMedium)
                PickerField(
                    label = selectedDistrictName ?: "Select District",
                    options = locState.districts.map { it.name },
                    onSelect = { name ->
                        val d = locState.districts.firstOrNull { it.name == name }
                        selectedDistrictName = name
                        selectedDistrictId = d?.id
                        selectedUpazilaId = null
                        selectedUpazilaName = null
                        if (d != null) locVm.loadUpazilas(d.id)
                    },
                )
                Text("Upazila / Area", style = MaterialTheme.typography.titleMedium)
                PickerField(
                    label = selectedUpazilaName ?: "Select Upazila (optional)",
                    options = locState.upazilas.map { it.name },
                    onSelect = { name ->
                        val u = locState.upazilas.firstOrNull { it.name == name }
                        selectedUpazilaName = name
                        selectedUpazilaId = u?.id
                    },
                )
            }
            item {
                PrimaryButton(text = "Search Donors", onClick = {
                    val group = selectedGroup?.let { label ->
                        BloodGroup.entries.firstOrNull { it.label == label }
                    }
                    vm.search(group, selectedDistrictId, selectedUpazilaId)
                }, enabled = hasCriteria)
            }
            if (state.searched && !state.isLoading && state.totalCount > 0) {
                item {
                    val end = state.donors.size
                    val start = if (end == 0) 0 else 1
                    Text(
                        "Showing $start to $end of ${state.totalCount} donors",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                item {
                    if (state.searched && state.totalCount > 0) {
                        Text("${state.totalCount} donor(s) found", style = MaterialTheme.typography.titleSmall)
                    }
                }
            }
            if (state.isLoading) {
                item { SearchResultsSkeleton() }
            } else {
                items(state.donors, key = { it.id }) { donor ->
                    DonorCard(donor, modifier = Modifier.animateItem()) {
                        val districtId = donor.districtId
                        val upazilaId = donor.upazilaId
                        if (districtId != null && upazilaId != null) {
                            authVm.pendingRequestPrefill = com.bloodnetwork.bangladesh.ui.viewmodel.AuthViewModel.PendingRequestPrefill(
                                bloodGroup = donor.bloodGroup,
                                districtId = districtId,
                                districtName = donor.districtName,
                                upazilaId = upazilaId,
                                upazilaName = donor.upazilaName,
                            )
                        }
                        if (isLoggedIn) {
                            onNavigate(Routes.REQUEST_BLOOD)
                        } else {
                            authVm.pendingRedirectRoute = Routes.REQUEST_BLOOD
                            onNavigate(Routes.LOGIN)
                        }
                    }
                }
                if (state.isLoadingMore) {
                    item {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp))
                        }
                    }
                }
            }
        }
        }
    }
}

@Composable
fun DonorCard(donor: PublicDonorDto, modifier: Modifier = Modifier, onRequest: () -> Unit = {}) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                com.bloodnetwork.bangladesh.ui.components.Avatar(photoUrl = donor.photoUrl, size = 44.dp)
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(donor.firstName, style = MaterialTheme.typography.titleMedium)
                        Text(donor.bloodGroup.label, color = BloodRed, style = MaterialTheme.typography.titleMedium)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(
                            Icons.Filled.LocationOn,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(14.dp),
                        )
                        Text(
                            text = listOfNotNull(donor.upazilaName, donor.districtName).joinToString(", "),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    RoleBadge(donor.availabilityStatus.name)
                    donor.distanceKm?.let {
                        Text("${String.format("%.1f", it)} km", style = MaterialTheme.typography.labelLarge)
                    }
                }
                if (donor.availabilityStatus == com.bloodnetwork.bangladesh.data.model.AvailabilityStatus.Available) {
                    PrimaryButton(
                        text = "Request Blood",
                        onClick = onRequest,
                        fillMax = false,
                        modifier = Modifier.padding(start = 8.dp),
                    )
                }
            }
        }
    }
}
