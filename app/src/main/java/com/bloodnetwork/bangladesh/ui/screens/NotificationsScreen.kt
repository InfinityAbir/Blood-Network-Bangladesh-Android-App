package com.bloodnetwork.bangladesh.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bloodnetwork.bangladesh.data.model.NotificationDto
import com.bloodnetwork.bangladesh.data.model.NotificationType
import com.bloodnetwork.bangladesh.data.model.metadataAvailabilityStatus
import com.bloodnetwork.bangladesh.ui.LocalVmFactory
import com.bloodnetwork.bangladesh.ui.components.SkeletonCard
import com.bloodnetwork.bangladesh.ui.i18n.tr
import com.bloodnetwork.bangladesh.ui.navigation.Routes
import com.bloodnetwork.bangladesh.ui.theme.AvailableGreen
import com.bloodnetwork.bangladesh.ui.theme.BloodRed
import com.bloodnetwork.bangladesh.ui.theme.BloodPink
import com.bloodnetwork.bangladesh.ui.theme.GrayMid
import com.bloodnetwork.bangladesh.ui.theme.RecentlyDonatedAmber
import com.bloodnetwork.bangladesh.ui.viewmodel.NotificationsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(onNavigate: (String) -> Unit, onBack: () -> Unit) {
    val factory = LocalVmFactory.current!!
    val vm: NotificationsViewModel = viewModel(factory = factory)
    val state by vm.uiState.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) { vm.load() }

    LaunchedEffect(state.error) {
        state.error?.let { snackbarHostState.showSnackbar(it) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(tr("Notifications", "নোটিফিকেশন")) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = tr("Back", "পেছনে"))
                    }
                },
                actions = {
                    if (state.unreadCount > 0) {
                        TextButton(onClick = { vm.markAllRead() }) { Text(tr("Mark all read", "সব পঠিত হিসেবে চিহ্নিত করুন")) }
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        if (state.isLoading && state.notifications.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                repeat(5) { SkeletonCard() }
            }
        } else {
            PullToRefreshBox(
                isRefreshing = state.isRefreshing,
                onRefresh = { vm.refresh() },
                modifier = Modifier.fillMaxSize().padding(padding),
            ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = state.typeFilter == null,
                            onClick = { vm.setTypeFilter(null) },
                            label = { Text(tr("All", "সব")) },
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = BloodRed, selectedLabelColor = Color.White),
                        )
                        FilterChip(
                            selected = state.typeFilter == NotificationType.Availability,
                            onClick = { vm.setTypeFilter(NotificationType.Availability) },
                            label = { Text(tr("Donor available", "দাতা উপলব্ধ")) },
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = BloodRed, selectedLabelColor = Color.White),
                        )
                    }
                }
                item {
                    Text(
                        if (state.unreadCount > 0) {
                            tr("${state.unreadCount} unread", "${state.unreadCount}টি অপঠিত")
                        } else {
                            tr("You're all caught up", "আপনি সব দেখে ফেলেছেন")
                        },
                        style = MaterialTheme.typography.titleSmall,
                    )
                }
                if (state.notifications.isEmpty()) {
                    item { Text(tr("No notifications yet", "এখনো কোনো নোটিফিকেশন নেই"), style = MaterialTheme.typography.bodyMedium) }
                }
                items(state.notifications, key = { it.id }) { n ->
                    NotificationCard(n, onClick = {
                        vm.markRead(n.id)
                        if (n.type == NotificationType.Availability) onNavigate(Routes.FIND_BLOOD)
                    })
                }
            }
            }
        }
    }
}

@Composable
fun NotificationCard(n: NotificationDto, onClick: () -> Unit = {}) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (n.isRead) MaterialTheme.colorScheme.surface else BloodPink,
        ),
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = n.title,
                    style = MaterialTheme.typography.titleSmall,
                    color = if (n.isRead) MaterialTheme.colorScheme.onSurface else BloodRed,
                    modifier = Modifier.weight(1f),
                )
                if (n.type == NotificationType.Availability) {
                    AvailabilityPill(n.metadataAvailabilityStatus())
                }
            }
            Text(n.message, style = MaterialTheme.typography.bodyMedium)
            if (n.createdAt.isNotBlank()) {
                Text(n.createdAt, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
fun AvailabilityPill(status: String?) {
    val (bg, fg, label) = when (status) {
        "Available" -> Triple(AvailableGreen.copy(alpha = 0.12f), AvailableGreen, tr("Available", "উপলব্ধ"))
        "RecentlyDonated" -> Triple(RecentlyDonatedAmber.copy(alpha = 0.14f), RecentlyDonatedAmber, tr("Recently donated", "সম্প্রতি দান করেছেন"))
        "Unavailable" -> Triple(GrayMid.copy(alpha = 0.12f), GrayMid, tr("Unavailable", "অনুপলব্ধ"))
        else -> Triple(GrayMid.copy(alpha = 0.12f), GrayMid, tr("Unknown", "অজানা"))
    }
    Box(modifier = Modifier.clip(RoundedCornerShape(50)).background(bg).padding(horizontal = 8.dp, vertical = 3.dp)) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = fg, fontWeight = FontWeight.SemiBold, maxLines = 1)
    }
}
