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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bloodnetwork.bangladesh.ui.LocalVmFactory
import com.bloodnetwork.bangladesh.ui.components.PaginationFooter
import com.bloodnetwork.bangladesh.ui.components.SkeletonCard
import com.bloodnetwork.bangladesh.ui.i18n.tr
import com.bloodnetwork.bangladesh.ui.theme.BloodRed
import com.bloodnetwork.bangladesh.ui.viewmodel.AdminViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminAuditLogsScreen(onNavigate: (String) -> Unit, onBack: () -> Unit) {
    val factory = LocalVmFactory.current!!
    val vm: AdminViewModel = viewModel(factory = factory)
    val state by vm.uiState.collectAsStateWithLifecycle()

    var entityTypeFilter by remember { mutableStateOf("") }

    LaunchedEffect(Unit) { vm.loadAuditLogs(entityTypeFilter.ifBlank { null }) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(tr("Audit Logs", "অডিট লগ"), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold) },
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
            onRefresh = { vm.refreshAuditLogs() },
            modifier = Modifier.fillMaxSize().padding(padding).imePadding(),
        ) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 130.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = entityTypeFilter,
                        onValueChange = { entityTypeFilter = it },
                        placeholder = { Text(tr("Entity Type (User, BloodRequest)", "এন্টিটি টাইপ (User, BloodRequest)"), style = MaterialTheme.typography.bodySmall) },
                        label = { Text(tr("Entity Type", "এন্টিটি টাইপ"), style = MaterialTheme.typography.bodySmall) },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                    )
                    Button(
                        onClick = { vm.loadAuditLogs(entityTypeFilter.ifBlank { null }) },
                        colors = ButtonDefaults.buttonColors(containerColor = BloodRed),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 12.dp),
                    ) { Text(tr("Filter", "ফিল্টার"), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold) }
                }
            }
            val errorMsg = state.error
            if (state.isLoading && state.auditLogs.isEmpty()) {
                items(6) { Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)) { SkeletonCard() } }
            } else if (errorMsg != null && state.auditLogs.isEmpty()) {
                item {
                    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(tr("Failed to load logs", "লগ লোড করা যায়নি"), style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.SemiBold)
                            Text(errorMsg, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Button(onClick = { vm.loadAuditLogs(entityTypeFilter.ifBlank { null }) }, colors = ButtonDefaults.buttonColors(containerColor = BloodRed), shape = RoundedCornerShape(50)) { Text(tr("Retry", "আবার চেষ্টা করুন"), style = MaterialTheme.typography.labelSmall) }
                        }
                    }
                }
            } else if (state.auditLogs.isEmpty()) {
                item {
                    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)) {
                        Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Box(modifier = Modifier.size(48.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceVariant), contentAlignment = Alignment.Center) {
                                Icon(Icons.Filled.History, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Text(tr("No audit logs", "কোনো অডিট লগ নেই"), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                            Text(tr("Logs appear as admins take actions", "অ্যাডমিনরা কোনো কার্যক্রম নিলে এখানে লগ দেখা যাবে"), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            } else {
                items(state.auditLogs, key = { it.id }) { log ->
                    AuditLogCard(
                        modifier = Modifier.animateItem(),
                        userName = log.userName ?: tr("System", "সিস্টেম"),
                        action = log.action,
                        entityType = log.entityType,
                        entityId = log.entityId,
                        ipAddress = log.ipAddress,
                        metadata = log.metadata,
                        createdAt = log.createdAt,
                    )
                }
                item {
                    PaginationFooter(
                        page = state.auditLogsPage,
                        pageSize = state.auditLogsPageSize,
                        totalCount = state.auditLogsTotalCount,
                        label = tr("logs", "লগ"),
                        onPageChange = { newPage -> vm.gotoAuditLogsPage(newPage) },
                        onPageSizeChange = { newSize -> vm.loadAuditLogs(entityTypeFilter.ifBlank { null }, newSize) },
                    )
                }
            }
        }
        }
    }
}

@Composable
private fun AuditLogCard(
    userName: String,
    action: String,
    entityType: String,
    entityId: String?,
    ipAddress: String?,
    metadata: String?,
    createdAt: String,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.weight(1f)) {
                    Box(modifier = Modifier.size(36.dp).clip(CircleShape).background(BloodRed.copy(alpha = 0.12f)), contentAlignment = Alignment.Center) {
                        Icon(Icons.Filled.Person, contentDescription = null, tint = BloodRed, modifier = Modifier.size(18.dp))
                    }
                    Column {
                        Text(userName, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, maxLines = 1)
                        Text(entityType, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Spacer(Modifier.width(8.dp))
                ActionPill(action)
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                DetailRow(icon = Icons.Filled.Badge, label = tr("Entity", "এন্টিটি"), value = entityType)
                entityId?.let { DetailRow(icon = Icons.Filled.Fingerprint, label = tr("ID", "আইডি"), value = it.take(8) + "…") }
                ipAddress?.let { DetailRow(icon = Icons.Filled.Language, label = tr("IP", "আইপি"), value = it) }
            }
            metadata?.let {
                Box(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)).padding(10.dp)) {
                    Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface)
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Icon(Icons.Filled.History, contentDescription = null, modifier = Modifier.size(12.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(createdAt, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun DetailRow(icon: ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(12.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Text("$label:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Medium)
        Text(value, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
private fun ActionPill(action: String) {
    val (bg, fg) = when (action) {
        "VerifyDonor" -> Color(0xFF2E7D32).copy(alpha = 0.12f) to Color(0xFF2E7D32)
        "ToggleUserActive" -> Color(0xFF1565C0).copy(alpha = 0.12f) to Color(0xFF1565C0)
        "ResolveReport" -> Color(0xFFEF6C00).copy(alpha = 0.12f) to Color(0xFFEF6C00)
        else -> BloodRed.copy(alpha = 0.1f) to BloodRed
    }
    Box(modifier = Modifier.clip(RoundedCornerShape(50)).background(bg).padding(horizontal = 10.dp, vertical = 5.dp)) {
        Text(action, style = MaterialTheme.typography.labelSmall, color = fg, fontWeight = FontWeight.SemiBold, maxLines = 1)
    }
}
