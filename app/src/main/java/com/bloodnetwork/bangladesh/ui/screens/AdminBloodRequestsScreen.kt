package com.bloodnetwork.bangladesh.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Bloodtype
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bloodnetwork.bangladesh.data.model.BloodGroup
import com.bloodnetwork.bangladesh.data.model.RequestStatus
import com.bloodnetwork.bangladesh.data.model.Urgency
import com.bloodnetwork.bangladesh.ui.LocalVmFactory
import com.bloodnetwork.bangladesh.ui.components.PaginationFooter
import com.bloodnetwork.bangladesh.ui.components.SkeletonCard
import com.bloodnetwork.bangladesh.ui.i18n.tr
import com.bloodnetwork.bangladesh.ui.theme.BloodRed
import com.bloodnetwork.bangladesh.ui.viewmodel.AdminViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminBloodRequestsScreen(onBack: () -> Unit, initialStatus: String? = null) {
    val factory = LocalVmFactory.current!!
    val vm: AdminViewModel = viewModel(factory = factory)
    val state by vm.uiState.collectAsStateWithLifecycle()

    var selectedStatus by remember { mutableStateOf(initialStatus?.let { s -> RequestStatus.entries.firstOrNull { it.name == s } }) }
    var selectedGroup by remember { mutableStateOf<BloodGroup?>(null) }

    LaunchedEffect(Unit) { vm.loadBloodRequests(selectedStatus?.name, selectedGroup) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(tr("Blood Requests", "রক্তের অনুরোধ"), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        Text(
                            tr("${state.bloodRequestsTotalCount} request${if (state.bloodRequestsTotalCount == 1) "" else "s"}", "${state.bloodRequestsTotalCount} টি অনুরোধ"),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = tr("Back", "পেছনে"))
                    }
                },
            )
        },
    ) { padding ->
        val listState = rememberLazyListState()

        PullToRefreshBox(
            isRefreshing = state.isRefreshing,
            onRefresh = { vm.refreshBloodRequests() },
            modifier = Modifier.fillMaxSize().padding(padding),
        ) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 130.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(tr("Status", "অবস্থা"), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.width(48.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), contentPadding = PaddingValues(end = 8.dp)) {
                            item {
                                FilterChip(
                                    selected = selectedStatus == null, onClick = { selectedStatus = null; vm.loadBloodRequests(null, selectedGroup) },
                                    label = { Text(tr("All", "সব"), style = MaterialTheme.typography.labelSmall) },
                                    shape = RoundedCornerShape(50),
                                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = BloodRed, selectedLabelColor = Color.White),
                                )
                            }
                            items(RequestStatus.entries.toList()) { status ->
                                FilterChip(
                                    selected = selectedStatus == status, onClick = { selectedStatus = status; vm.loadBloodRequests(status.name, selectedGroup) },
                                    label = { Text(statusLabel(status), style = MaterialTheme.typography.labelSmall) },
                                    shape = RoundedCornerShape(50),
                                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = statusAccentColor(status), selectedLabelColor = Color.White),
                                )
                            }
                        }
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(tr("Group", "গ্রুপ"), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.width(48.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), contentPadding = PaddingValues(end = 8.dp)) {
                            item {
                                FilterChip(
                                    selected = selectedGroup == null, onClick = { selectedGroup = null; vm.loadBloodRequests(selectedStatus?.name, null) },
                                    label = { Text(tr("All", "সব"), style = MaterialTheme.typography.labelSmall) },
                                    shape = RoundedCornerShape(50),
                                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = BloodRed, selectedLabelColor = Color.White),
                                )
                            }
                            items(BloodGroup.entries.toList()) { group ->
                                FilterChip(
                                    selected = selectedGroup == group, onClick = { selectedGroup = group; vm.loadBloodRequests(selectedStatus?.name, group) },
                                    label = { Text(group.label, style = MaterialTheme.typography.labelSmall) },
                                    shape = RoundedCornerShape(50),
                                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = BloodRed, selectedLabelColor = Color.White),
                                )
                            }
                        }
                    }
                }
            }
            val errorMsg = state.error
            if (state.isLoading && state.bloodRequests.isEmpty()) {
                items(4) { Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)) { SkeletonCard() } }
            } else if (errorMsg != null && state.bloodRequests.isEmpty()) {
                item {
                    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(tr("Failed to load requests", "অনুরোধ লোড করা যায়নি"), style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.SemiBold)
                            Text(errorMsg, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Button(onClick = { vm.loadBloodRequests(selectedStatus?.name, selectedGroup) }, colors = ButtonDefaults.buttonColors(containerColor = BloodRed), shape = RoundedCornerShape(50)) { Text(tr("Retry", "আবার চেষ্টা করুন"), style = MaterialTheme.typography.labelSmall) }
                        }
                    }
                }
            } else if (state.bloodRequests.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(top = 48.dp), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Box(modifier = Modifier.size(64.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceVariant), contentAlignment = Alignment.Center) {
                                Icon(Icons.Filled.Bloodtype, contentDescription = null, modifier = Modifier.size(28.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Spacer(Modifier.height(4.dp))
                            Text(tr("No requests found", "কোনো অনুরোধ পাওয়া যায়নি"), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                            Text(
                                tr("Try a different status or blood group filter", "ভিন্ন অবস্থা বা রক্তের গ্রুপ ফিল্টার চেষ্টা করুন"),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            } else {
                items(state.bloodRequests, key = { it.id }) { req ->
                    val urgencyColor = urgencyAccentColor(req.urgency)
                    Card(
                        modifier = Modifier.fillMaxWidth().animateItem(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    ) {
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Box(modifier = Modifier.width(4.dp).fillMaxHeight().background(urgencyColor))
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.weight(1f)) {
                                        Box(modifier = Modifier.size(36.dp).clip(CircleShape).background(BloodRed.copy(alpha = 0.12f)), contentAlignment = Alignment.Center) {
                                            Text(req.bloodGroup.label, style = MaterialTheme.typography.labelLarge, color = BloodRed, fontWeight = FontWeight.Bold)
                                        }
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(req.hospitalName, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, maxLines = 1)
                                            Text(
                                                tr("${req.unitsFulfilled} of ${req.unitsRequired} unit(s) fulfilled", "${req.unitsRequired} ইউনিটের মধ্যে ${req.unitsFulfilled} ইউনিট পূরণ"),
                                                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            )
                                        }
                                    }
                                    Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        RequestStatusPill(req.status)
                                        UrgencyPill(req.urgency)
                                    }
                                }
                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Icon(Icons.Filled.Person, contentDescription = null, modifier = Modifier.size(12.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(
                                        tr("Requested by ${req.requesterName}", "${req.requesterName} দ্বারা অনুরোধ করা হয়েছে"),
                                        style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1,
                                    )
                                }
                                req.patientName?.let {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Icon(Icons.Filled.Bloodtype, contentDescription = null, modifier = Modifier.size(12.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                        val relation = req.patientRelation?.let { r -> " ($r)" }.orEmpty()
                                        Text(tr("Patient: $it$relation", "রোগী: $it$relation"), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
                                    }
                                }
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Icon(Icons.Filled.Phone, contentDescription = null, modifier = Modifier.size(12.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(req.contactPhone, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Icon(Icons.Filled.LocationOn, contentDescription = null, modifier = Modifier.size(12.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                    val place = listOfNotNull(req.upazilaName, req.districtName).joinToString(", ")
                                    Text(place.ifBlank { tr("Unknown location", "অজানা অবস্থান") }, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
                                }
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Icon(Icons.Filled.CalendarToday, contentDescription = null, modifier = Modifier.size(12.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(tr("Required by ${req.requiredBy.take(10)}", "প্রয়োজনের তারিখ ${req.requiredBy.take(10)}"), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                req.additionalInformation?.let {
                                    Box(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)).padding(10.dp)) {
                                        Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface)
                                    }
                                }
                            }
                        }
                    }
                }
                item {
                    PaginationFooter(
                        page = state.bloodRequestsPage,
                        pageSize = state.bloodRequestsPageSize,
                        totalCount = state.bloodRequestsTotalCount,
                        label = tr("requests", "অনুরোধ"),
                        onPageChange = { newPage -> vm.gotoBloodRequestsPage(newPage) },
                        onPageSizeChange = { newSize -> vm.loadBloodRequests(selectedStatus?.name, selectedGroup, newSize) },
                    )
                }
            }
        }
        }
    }
}

private fun urgencyAccentColor(urgency: Urgency): Color = when (urgency) {
    Urgency.Critical -> Color(0xFFC62828)
    Urgency.Urgent -> Color(0xFFEF6C00)
    Urgency.Normal -> Color(0xFF616161)
}

private fun statusAccentColor(status: RequestStatus): Color = when (status) {
    RequestStatus.Open -> Color(0xFFC62828)
    RequestStatus.PartiallyFulfilled -> Color(0xFFEF6C00)
    RequestStatus.Fulfilled -> Color(0xFF2E7D32)
    RequestStatus.Cancelled -> Color(0xFF616161)
    RequestStatus.Expired -> Color(0xFF616161)
}

@Composable
private fun statusLabel(status: RequestStatus): String = when (status) {
    RequestStatus.Open -> tr("Open", "খোলা")
    RequestStatus.PartiallyFulfilled -> tr("Partial", "আংশিক")
    RequestStatus.Fulfilled -> tr("Fulfilled", "পূর্ণ")
    RequestStatus.Cancelled -> tr("Cancelled", "বাতিল")
    RequestStatus.Expired -> tr("Expired", "মেয়াদোত্তীর্ণ")
}

@Composable
private fun RequestStatusPill(status: RequestStatus) {
    val accent = statusAccentColor(status)
    Box(modifier = Modifier.clip(RoundedCornerShape(50)).background(accent.copy(alpha = 0.12f)).padding(horizontal = 10.dp, vertical = 5.dp)) {
        Text(statusLabel(status), style = MaterialTheme.typography.labelSmall, color = accent, fontWeight = FontWeight.SemiBold, maxLines = 1)
    }
}

@Composable
private fun UrgencyPill(urgency: Urgency) {
    val accent = urgencyAccentColor(urgency)
    val displayText = when (urgency) {
        Urgency.Critical -> tr("Critical", "সংকটাপন্ন")
        Urgency.Urgent -> tr("Urgent", "জরুরি")
        Urgency.Normal -> tr("Normal", "স্বাভাবিক")
    }
    Box(modifier = Modifier.clip(RoundedCornerShape(50)).background(accent.copy(alpha = 0.12f)).padding(horizontal = 10.dp, vertical = 5.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            if (urgency == Urgency.Critical) {
                Icon(Icons.Filled.Warning, contentDescription = null, modifier = Modifier.size(12.dp), tint = accent)
            }
            Text(displayText, style = MaterialTheme.typography.labelSmall, color = accent, fontWeight = FontWeight.SemiBold, maxLines = 1)
        }
    }
}
