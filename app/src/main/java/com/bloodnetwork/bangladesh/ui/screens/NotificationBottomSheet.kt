package com.bloodnetwork.bangladesh.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bloodnetwork.bangladesh.data.model.NotificationDto
import com.bloodnetwork.bangladesh.ui.components.LoadingBox
import com.bloodnetwork.bangladesh.ui.theme.BloodRed
import com.bloodnetwork.bangladesh.ui.theme.BloodPink
import com.bloodnetwork.bangladesh.ui.viewmodel.NotificationsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationBottomSheet(
    vm: NotificationsViewModel,
    onDismiss: () -> Unit,
) {
    val state by vm.uiState.collectAsStateWithLifecycle()
    val sheetState = rememberModalBottomSheetState()

    LaunchedEffect(Unit) { vm.load() }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 24.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text("Notifications", style = MaterialTheme.typography.titleLarge)
                if (state.unreadCount > 0) {
                    TextButton(onClick = { vm.markAllRead() }) {
                        Text("Mark all read")
                    }
                }
            }

            if (state.isLoading && state.notifications.isEmpty()) {
                LoadingBox()
            } else if (state.notifications.isEmpty()) {
                Text(
                    "No notifications yet",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 24.dp),
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(state.notifications, key = { it.id }) { n ->
                        NotificationSheetCard(n)
                    }
                }
            }
        }
    }
}

@Composable
fun NotificationSheetCard(n: NotificationDto) {
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
