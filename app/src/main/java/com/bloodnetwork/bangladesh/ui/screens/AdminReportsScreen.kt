package com.bloodnetwork.bangladesh.ui.screens

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Report
import androidx.compose.material3.AlertDialog
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bloodnetwork.bangladesh.ui.LocalVmFactory
import com.bloodnetwork.bangladesh.ui.components.SkeletonCard
import com.bloodnetwork.bangladesh.ui.theme.BloodRed
import com.bloodnetwork.bangladesh.ui.viewmodel.AdminViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminReportsScreen(onNavigate: (String) -> Unit, onBack: () -> Unit) {
    val factory = LocalVmFactory.current!!
    val vm: AdminViewModel = viewModel(factory = factory)
    val state by vm.uiState.collectAsStateWithLifecycle()

    var selectedStatus by remember { mutableStateOf("") }
    var showResolveDialog by remember { mutableStateOf(false) }
    var resolveTargetId by remember { mutableStateOf("") }
    var resolveStatus by remember { mutableStateOf("") }
    var resolutionNote by remember { mutableStateOf("") }

    LaunchedEffect(Unit) { vm.loadReports(selectedStatus.ifBlank { null }) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Report Management", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { padding ->
        val listState = rememberLazyListState()
        val shouldLoadMore by remember {
            derivedStateOf {
                val last = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
                val total = listState.layoutInfo.totalItemsCount
                total > 0 && last >= total - 3
            }
        }
        LaunchedEffect(shouldLoadMore, state.reportsHasMore) {
            if (shouldLoadMore && state.reportsHasMore) vm.loadMoreReports()
        }

        PullToRefreshBox(
            isRefreshing = state.isRefreshing,
            onRefresh = { vm.refreshReports() },
            modifier = Modifier.fillMaxSize().padding(padding),
        ) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), contentPadding = PaddingValues(end = 8.dp)) {
                    item {
                        FilterChip(selected = selectedStatus == "", onClick = { selectedStatus = ""; vm.loadReports(null) }, label = { Text("All", style = MaterialTheme.typography.labelSmall) }, shape = RoundedCornerShape(50), colors = FilterChipDefaults.filterChipColors(selectedContainerColor = BloodRed, selectedLabelColor = Color.White))
                    }
                    item {
                        FilterChip(selected = selectedStatus == "Open", onClick = { selectedStatus = "Open"; vm.loadReports("Open") }, label = { Text("Open", style = MaterialTheme.typography.labelSmall) }, shape = RoundedCornerShape(50), colors = FilterChipDefaults.filterChipColors(selectedContainerColor = BloodRed, selectedLabelColor = Color.White))
                    }
                    item {
                        FilterChip(selected = selectedStatus == "UnderReview", onClick = { selectedStatus = "UnderReview"; vm.loadReports("UnderReview") }, label = { Text("Review", style = MaterialTheme.typography.labelSmall) }, shape = RoundedCornerShape(50), colors = FilterChipDefaults.filterChipColors(selectedContainerColor = BloodRed, selectedLabelColor = Color.White))
                    }
                    item {
                        FilterChip(selected = selectedStatus == "Resolved", onClick = { selectedStatus = "Resolved"; vm.loadReports("Resolved") }, label = { Text("Resolved", style = MaterialTheme.typography.labelSmall) }, shape = RoundedCornerShape(50), colors = FilterChipDefaults.filterChipColors(selectedContainerColor = BloodRed, selectedLabelColor = Color.White))
                    }
                    item {
                        FilterChip(selected = selectedStatus == "Dismissed", onClick = { selectedStatus = "Dismissed"; vm.loadReports("Dismissed") }, label = { Text("Dismissed", style = MaterialTheme.typography.labelSmall) }, shape = RoundedCornerShape(50), colors = FilterChipDefaults.filterChipColors(selectedContainerColor = BloodRed, selectedLabelColor = Color.White))
                    }
                }
            }
            val errorMsg = state.error
            if (state.isLoading && state.reports.isEmpty()) {
                items(4) { Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)) { SkeletonCard() } }
            } else if (errorMsg != null && state.reports.isEmpty()) {
                item {
                    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Failed to load reports", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.SemiBold)
                            Text(errorMsg, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Button(onClick = { vm.loadReports(selectedStatus.ifBlank { null }) }, colors = ButtonDefaults.buttonColors(containerColor = BloodRed), shape = RoundedCornerShape(50)) { Text("Retry", style = MaterialTheme.typography.labelSmall) }
                        }
                    }
                }
            } else if (state.reports.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillParentMaxHeight(0.6f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)) {
                            Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Box(modifier = Modifier.size(48.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceVariant), contentAlignment = Alignment.Center) {
                                    Icon(Icons.Filled.Report, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Text("No reports", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                                Text("Reports filed by users will appear here", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            } else {
                items(state.reports) { report ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.weight(1f)) {
                                    Box(modifier = Modifier.size(36.dp).clip(CircleShape).background(BloodRed.copy(alpha = 0.12f)), contentAlignment = Alignment.Center) {
                                        Icon(Icons.Filled.Report, contentDescription = null, tint = BloodRed, modifier = Modifier.size(18.dp))
                                    }
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(report.reason, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, maxLines = 2)
                                        Text("${report.reporterName} → ${report.reportedUserName}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
                                    }
                                }
                                Spacer(Modifier.width(8.dp))
                                ReportStatusPill(report.status)
                            }
                            report.description?.let {
                                Box(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)).padding(10.dp)) {
                                    Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface)
                                }
                            }
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Icon(Icons.Filled.History, contentDescription = null, modifier = Modifier.size(12.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(report.createdAt, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                report.reviewedByName?.let {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Icon(Icons.Filled.Gavel, contentDescription = null, modifier = Modifier.size(12.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text("Reviewed by $it", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                                report.resolution?.let {
                                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Icon(Icons.Filled.Gavel, contentDescription = null, modifier = Modifier.size(12.dp), tint = Color(0xFF2E7D32))
                                        Text("Resolution: $it", style = MaterialTheme.typography.bodySmall, color = Color(0xFF2E7D32), fontWeight = FontWeight.Medium)
                                    }
                                }
                            }
                            if (report.status == "Open" || report.status == "UnderReview") {
                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End), verticalAlignment = Alignment.CenterVertically) {
                                    CompactOutlinedReportButton("Dismiss", onClick = {
                                        resolveTargetId = report.id
                                        resolveStatus = "Dismissed"
                                        resolutionNote = ""
                                        showResolveDialog = true
                                    })
                                    CompactFilledReportButton("Resolve", onClick = {
                                        resolveTargetId = report.id
                                        resolveStatus = "Resolved"
                                        resolutionNote = ""
                                        showResolveDialog = true
                                    })
                                }
                            }
                        }
                    }
                }
                if (state.reportsLoadingMore) {
                    item { Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) { SkeletonCard() } }
                }
            }
        }
        }
    }

    if (showResolveDialog) {
        AlertDialog(
            onDismissRequest = { showResolveDialog = false },
            title = { Text("Resolve Report", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold) },
            text = {
                OutlinedTextField(
                    value = resolutionNote,
                    onValueChange = { resolutionNote = it },
                    label = { Text("Resolution notes (optional)", style = MaterialTheme.typography.bodySmall) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    minLines = 2,
                )
            },
            confirmButton = {
                Button(onClick = {
                    vm.resolveReport(resolveTargetId, resolveStatus, resolutionNote.ifBlank { null })
                    showResolveDialog = false
                }, colors = ButtonDefaults.buttonColors(containerColor = BloodRed), shape = RoundedCornerShape(50)) { Text("Submit", style = MaterialTheme.typography.labelSmall) }
            },
            dismissButton = {
                TextButton(onClick = { showResolveDialog = false }) { Text("Cancel", style = MaterialTheme.typography.labelSmall) }
            },
        )
    }
}

@Composable
private fun ReportStatusPill(status: String) {
    val (bg, fg) = when (status) {
        "Open" -> Color(0xFFC62828).copy(alpha = 0.12f) to Color(0xFFC62828)
        "UnderReview" -> Color(0xFFEF6C00).copy(alpha = 0.12f) to Color(0xFFEF6C00)
        "Resolved" -> Color(0xFF2E7D32).copy(alpha = 0.12f) to Color(0xFF2E7D32)
        "Dismissed" -> Color(0xFF616161).copy(alpha = 0.12f) to Color(0xFF616161)
        else -> BloodRed.copy(alpha = 0.1f) to BloodRed
    }
    Box(modifier = Modifier.clip(RoundedCornerShape(50)).background(bg).padding(horizontal = 10.dp, vertical = 5.dp)) {
        Text(status, style = MaterialTheme.typography.labelSmall, color = fg, fontWeight = FontWeight.SemiBold, maxLines = 1)
    }
}

@Composable
private fun CompactFilledReportButton(text: String, onClick: () -> Unit) {
    Button(onClick = onClick, colors = ButtonDefaults.buttonColors(containerColor = BloodRed), shape = RoundedCornerShape(50), contentPadding = PaddingValues(horizontal = 14.dp, vertical = 0.dp), modifier = Modifier.height(32.dp)) {
        Text(text, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun CompactOutlinedReportButton(text: String, onClick: () -> Unit) {
    androidx.compose.material3.OutlinedButton(onClick = onClick, shape = RoundedCornerShape(50), contentPadding = PaddingValues(horizontal = 14.dp, vertical = 0.dp), modifier = Modifier.height(32.dp)) {
        Text(text, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold)
    }
}
