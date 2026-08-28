package com.bloodnetwork.bangladesh.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bloodnetwork.bangladesh.data.model.NotificationDto
import com.bloodnetwork.bangladesh.ui.LocalVmFactory
import com.bloodnetwork.bangladesh.ui.components.SkeletonCard
import com.bloodnetwork.bangladesh.ui.theme.BloodRed
import com.bloodnetwork.bangladesh.ui.theme.BloodPink
import com.bloodnetwork.bangladesh.ui.viewmodel.NotificationsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(onNavigate: (String) -> Unit, onBack: () -> Unit) {
    val factory = LocalVmFactory.current!!
    val vm: NotificationsViewModel = viewModel(factory = factory)
    val state by vm.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) { vm.load() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notifications") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (state.unreadCount > 0) {
                        TextButton(onClick = { vm.markAllRead() }) { Text("Mark all read") }
                    }
                },
            )
        },
    ) { padding ->
        if (state.isLoading && state.notifications.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                repeat(5) { SkeletonCard() }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                item {
                    Text(
                        if (state.unreadCount > 0) "${state.unreadCount} unread" else "You're all caught up",
                        style = MaterialTheme.typography.titleSmall,
                    )
                }
                if (state.notifications.isEmpty()) {
                    item { Text("No notifications yet", style = MaterialTheme.typography.bodyMedium) }
                }
                items(state.notifications, key = { it.id }) { n ->
                    NotificationCard(n)
                }
            }
        }
    }
}

@Composable
fun NotificationCard(n: NotificationDto) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (n.isRead) MaterialTheme.colorScheme.surface else BloodPink,
        ),
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = n.title,
                style = MaterialTheme.typography.titleSmall,
                color = if (n.isRead) MaterialTheme.colorScheme.onSurface else BloodRed,
            )
            Text(n.message, style = MaterialTheme.typography.bodyMedium)
            if (n.createdAt.isNotBlank()) {
                Text(n.createdAt, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
