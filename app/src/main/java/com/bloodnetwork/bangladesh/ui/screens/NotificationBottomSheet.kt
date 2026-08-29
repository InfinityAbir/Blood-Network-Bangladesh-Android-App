package com.bloodnetwork.bangladesh.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bloodtype
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.NotificationImportant
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SyncAlt
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberSwipeToDismissBoxState
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bloodnetwork.bangladesh.data.model.NotificationDto
import com.bloodnetwork.bangladesh.data.model.NotificationType
import com.bloodnetwork.bangladesh.data.model.metadataAvailabilityStatus
import com.bloodnetwork.bangladesh.ui.components.SkeletonCard
import com.bloodnetwork.bangladesh.ui.i18n.tr
import com.bloodnetwork.bangladesh.ui.navigation.Routes
import com.bloodnetwork.bangladesh.ui.theme.BloodPink
import com.bloodnetwork.bangladesh.ui.theme.BloodRed
import com.bloodnetwork.bangladesh.ui.viewmodel.NotificationsViewModel
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import kotlinx.coroutines.delay

private const val SWIPE_EXIT_DURATION_MS = 220

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationBottomSheet(
    vm: NotificationsViewModel,
    onDismiss: () -> Unit,
    onNavigate: (String) -> Unit = {},
) {
    val state by vm.uiState.collectAsStateWithLifecycle()
    var showClearAllConfirm by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { vm.load() }

    if (showClearAllConfirm) {
        AlertDialog(
            onDismissRequest = { showClearAllConfirm = false },
            title = { Text(tr("Clear all notifications?", "সব নোটিফিকেশন মুছে ফেলবেন?")) },
            text = { Text(tr("This removes every notification from your list. This can't be undone.", "এতে আপনার তালিকা থেকে সব নোটিফিকেশন মুছে যাবে। এটি ফিরিয়ে আনা যাবে না।")) },
            confirmButton = {
                TextButton(onClick = { vm.clearAll(); showClearAllConfirm = false }) {
                    Text(tr("Clear all", "সব মুছুন"), color = BloodRed)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearAllConfirm = false }) { Text(tr("Cancel", "বাতিল")) }
            },
        )
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.32f))
                .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { onDismiss() },
            contentAlignment = Alignment.BottomCenter,
        ) {
            AnimatedVisibility(
                visible = true,
                enter = slideInVertically(animationSpec = tween(250), initialOffsetY = { it }),
                exit = slideOutVertically(animationSpec = tween(200), targetOffsetY = { it }),
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.75f)
                        .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { },
                    shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp)
                            .padding(top = 12.dp, bottom = 24.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.CenterHorizontally)
                                .size(width = 36.dp, height = 4.dp)
                                .clip(RoundedCornerShape(50))
                                .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)),
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(tr("Notifications", "নোটিফিকেশন"), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                            IconButton(onClick = onDismiss) {
                                Icon(Icons.Filled.Close, contentDescription = tr("Close", "বন্ধ করুন"), modifier = Modifier.size(20.dp))
                            }
                        }
                        if (state.notifications.isNotEmpty()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                if (state.unreadCount > 0) {
                                    TextButton(onClick = { vm.markAllRead() }) {
                                        Text(tr("Mark all read", "সব পঠিত হিসেবে চিহ্নিত করুন"), style = MaterialTheme.typography.labelSmall)
                                    }
                                }
                                TextButton(onClick = { showClearAllConfirm = true }) {
                                    Text(tr("Clear all", "সব মুছুন"), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.error)
                                }
                            }
                        }

                        if (state.isLoading && state.notifications.isEmpty()) {
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                repeat(3) { SkeletonCard() }
                            }
                        } else if (state.notifications.isEmpty()) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f)
                                    .padding(vertical = 32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                            ) {
                                Box(modifier = Modifier.size(56.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceVariant), contentAlignment = Alignment.Center) {
                                    Icon(Icons.Filled.NotificationsNone, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(28.dp))
                                }
                                Text(tr("No notifications yet", "এখনো কোনো নোটিফিকেশন নেই"), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                                Text(tr("You're all caught up", "আপনি সব দেখে ফেলেছেন"), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        } else {
                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth().weight(1f),
                            ) {
                                items(state.notifications, key = { it.id }) { n ->
                                    NotificationListItem(
                                        n = n,
                                        onClick = {
                                            vm.markRead(n.id)
                                            if (n.type == NotificationType.Availability) {
                                                onDismiss()
                                                onNavigate(Routes.FIND_BLOOD)
                                            }
                                        },
                                        onDelete = { vm.deleteNotification(n.id) },
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun notificationIconFor(type: NotificationType): ImageVector = when (type) {
    NotificationType.BloodRequestMatch -> Icons.Filled.Bloodtype
    NotificationType.RequestUpdate -> Icons.Filled.SyncAlt
    NotificationType.DonorAccepted -> Icons.Filled.CheckCircle
    NotificationType.DonorDeclined -> Icons.Filled.HourglassEmpty
    NotificationType.ProfileReminder -> Icons.Filled.Person
    NotificationType.Availability -> Icons.Filled.WaterDrop
    NotificationType.System -> Icons.Filled.Campaign
    NotificationType.NewRequestPendingReview -> Icons.Filled.NotificationImportant
}

private fun cleanNotificationMessage(title: String, message: String): String {
    var text = message.trim()
    text = Regex("^Hi\\s+[^,]+,\\s*", RegexOption.IGNORE_CASE).replaceFirst(text, "")
    if (text.startsWith(title.trim(), ignoreCase = true)) {
        text = text.substring(title.trim().length).trimStart()
    }
    text = text.replaceFirstChar { if (it.isLowerCase()) it.uppercase() else it.toString() }
    return text.ifBlank { message.trim() }
}

@Composable
private fun relativeTime(iso: String): String {
    if (iso.isBlank()) return ""
    val then = try {
        Instant.parse(iso)
    } catch (e: DateTimeParseException) {
        return iso
    }
    val seconds = Duration.between(then, Instant.now()).seconds.coerceAtLeast(0)
    return when {
        seconds < 60 -> tr("Just now", "এইমাত্র")
        seconds < 3600 -> tr("${seconds / 60}m ago", "${seconds / 60} মিনিট আগে")
        seconds < 86400 -> tr("${seconds / 3600}h ago", "${seconds / 3600} ঘণ্টা আগে")
        seconds < 604800 -> tr("${seconds / 86400}d ago", "${seconds / 86400} দিন আগে")
        else -> then.atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("MMM d, yyyy · h:mm a"))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LazyItemScope.NotificationListItem(n: NotificationDto, onClick: () -> Unit, onDelete: () -> Unit) {
    var visible by remember { mutableStateOf(true) }
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value != SwipeToDismissBoxValue.Settled) visible = false
            true
        },
    )
    LaunchedEffect(visible) {
        if (!visible) {
            delay(SWIPE_EXIT_DURATION_MS.toLong())
            onDelete()
        }
    }
    AnimatedVisibility(
        visible = visible,
        exit = shrinkVertically(animationSpec = tween(SWIPE_EXIT_DURATION_MS)) + fadeOut(animationSpec = tween(SWIPE_EXIT_DURATION_MS / 2)),
        modifier = Modifier.animateItem(),
    ) {
        SwipeToDismissBox(
            state = dismissState,
            backgroundContent = {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(MaterialTheme.shapes.medium)
                        .background(MaterialTheme.colorScheme.errorContainer)
                        .padding(horizontal = 20.dp),
                    contentAlignment = if (dismissState.dismissDirection == SwipeToDismissBoxValue.StartToEnd) Alignment.CenterStart else Alignment.CenterEnd,
                ) {
                    Icon(Icons.Filled.Delete, contentDescription = tr("Delete", "মুছুন"), tint = MaterialTheme.colorScheme.onErrorContainer)
                }
            },
        ) {
            NotificationSheetCard(n, onClick = onClick)
        }
    }
}

@Composable
fun NotificationSheetCard(n: NotificationDto, onClick: () -> Unit = {}) {
    // Opaque, not BloodPink.copy(alpha = ...): this card sits in front of the swipe-to-dismiss
    // background (the red Delete icon), and a translucent container lets that bleed through
    // even at rest, not just mid-swipe.
    val unreadContainer = androidx.compose.ui.graphics.lerp(MaterialTheme.colorScheme.surface, BloodPink, 0.35f)
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (n.isRead) MaterialTheme.colorScheme.surface else unreadContainer,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (n.isRead) 1.dp else 0.dp),
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(if (n.isRead) MaterialTheme.colorScheme.surfaceVariant else BloodRed.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    notificationIconFor(n.type),
                    contentDescription = null,
                    tint = if (n.isRead) MaterialTheme.colorScheme.onSurfaceVariant else BloodRed,
                    modifier = Modifier.size(20.dp),
                )
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = n.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false),
                    )
                    if (!n.isRead) {
                        Box(modifier = Modifier.size(7.dp).clip(CircleShape).background(BloodRed))
                    }
                    if (n.type == NotificationType.Availability) {
                        AvailabilityPill(n.metadataAvailabilityStatus())
                    }
                }
                Text(
                    text = cleanNotificationMessage(n.title, n.message),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 4,
                    overflow = TextOverflow.Ellipsis,
                )
                if (n.createdAt.isNotBlank()) {
                    Text(
                        text = relativeTime(n.createdAt),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    )
                }
            }
        }
    }
}
