package com.bloodnetwork.bangladesh.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.FactCheck
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bloodnetwork.bangladesh.ui.LocalVmFactory
import com.bloodnetwork.bangladesh.ui.components.SkeletonCard
import com.bloodnetwork.bangladesh.ui.theme.BloodRed
import com.bloodnetwork.bangladesh.ui.viewmodel.AdminViewModel
import com.bloodnetwork.bangladesh.ui.viewmodel.NotificationsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(onNavigate: (String) -> Unit, onBack: () -> Unit, onLogout: () -> Unit = {}) {
    val factory = LocalVmFactory.current!!
    val vm: AdminViewModel = viewModel(factory = factory)
    val state by vm.uiState.collectAsStateWithLifecycle()
    val notifVm: NotificationsViewModel = viewModel(factory = factory)
    val notifState by notifVm.uiState.collectAsStateWithLifecycle()
    var showNotifSheet by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { notifVm.loadUnreadCount() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Admin Dashboard") },
                actions = {
                    com.bloodnetwork.bangladesh.ui.components.ThemeToggleButton()
                    BadgedBox(
                        badge = { com.bloodnetwork.bangladesh.ui.components.AnimatedCountBadge(notifState.unreadCount) },
                    ) {
                        IconButton(onClick = { showNotifSheet = true }) {
                            Icon(Icons.Filled.Notifications, contentDescription = "Notifications")
                        }
                    }
                    Spacer(Modifier.width(4.dp))
                    IconButton(onClick = onLogout) {
                        Icon(Icons.Filled.Logout, contentDescription = "Logout")
                    }
                    Spacer(Modifier.width(4.dp))
                },
            )
        },
    ) { padding ->
        if (state.isLoading) {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(4) { Card(modifier = Modifier.fillMaxWidth()) { SkeletonCard() } }
            }
        } else {
            PullToRefreshBox(
                isRefreshing = state.isRefreshing,
                onRefresh = { vm.refreshDashboard() },
                modifier = Modifier.fillMaxSize().padding(padding),
            ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                state.dashboard?.let { stats ->
                    item { Text("Overview", style = MaterialTheme.typography.titleMedium, fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold) }
                    item {
                        Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            StatCard("Total Users", stats.totalUsers, Modifier.weight(1f).fillMaxHeight())
                            StatCard("Donors", stats.totalDonors, Modifier.weight(1f).fillMaxHeight())
                        }
                    }
                    item {
                        Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            StatCard("Blood Requests", stats.openBloodRequests, Modifier.weight(1f).fillMaxHeight(), "open")
                            StatCard("Matches", stats.acceptedMatches, Modifier.weight(1f).fillMaxHeight(), "accepted")
                        }
                    }
                    item {
                        Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            StatCard("Reports", stats.openReports, Modifier.weight(1f).fillMaxHeight(), "open")
                            StatCard("Pending Verifications", stats.pendingVerifications, Modifier.weight(1f).fillMaxHeight())
                        }
                    }
                }

                item { Text("Management", style = MaterialTheme.typography.titleMedium, fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold) }
                item {
                    Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        ManagementCard("Analytics", "Trends, blood-type & district breakdowns", Icons.Filled.BarChart, Modifier.weight(1f).fillMaxHeight()) { onNavigate("admin_analytics") }
                        ManagementCard("User Management", "Verify, activate & manage accounts", Icons.Filled.People, Modifier.weight(1f).fillMaxHeight()) { onNavigate("admin_users") }
                    }
                }
                item {
                    Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        ManagementCard("Reports", "Review & resolve flagged reports", Icons.Filled.Assignment, Modifier.weight(1f).fillMaxHeight()) { onNavigate("admin_reports") }
                        ManagementCard("Audit Logs", "Track system activity history", Icons.Filled.History, Modifier.weight(1f).fillMaxHeight()) { onNavigate("admin_audit_logs") }
                    }
                }
                item {
                    Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        ManagementCard("Account Settings", "Update email, phone & password", Icons.Filled.Settings, Modifier.weight(1f).fillMaxHeight()) { onNavigate("admin_settings") }
                        ManagementCard("About", "Edit the developer info shown to users", Icons.Filled.Info, Modifier.weight(1f).fillMaxHeight()) { onNavigate("about") }
                    }
                }
                item {
                    Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        ManagementCard("Eligibility Questions", "Edit the donor eligibility questionnaire", Icons.Filled.FactCheck, Modifier.weight(1f).fillMaxHeight()) { onNavigate(com.bloodnetwork.bangladesh.ui.navigation.Routes.ADMIN_ELIGIBILITY_QUESTIONS) }
                    }
                }
            }
            }
        }
    }

    if (showNotifSheet) {
        NotificationBottomSheet(
            vm = notifVm,
            onDismiss = {
                showNotifSheet = false
                notifVm.loadUnreadCount()
            },
            onNavigate = onNavigate,
        )
    }
}

@Composable
fun StatCard(label: String, value: Int, modifier: Modifier = Modifier, subtitle: String = "") {
    Card(modifier = modifier.heightIn(min = 92.dp)) {
        Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
            Text("$value", style = MaterialTheme.typography.headlineMedium, color = BloodRed)
            Text(label, style = MaterialTheme.typography.bodyMedium, maxLines = 2)
            // Reserve one line for subtitle so cards without subtitle keep same height
            Text(
                text = subtitle.ifEmpty { " " },
                style = MaterialTheme.typography.bodySmall,
                color = if (subtitle.isEmpty()) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
            )
        }
    }
}

@Composable
fun ManagementCard(title: String, subtitle: String, icon: ImageVector, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Card(onClick = onClick, modifier = modifier.heightIn(min = 118.dp)) {
        Column(modifier = Modifier.padding(16.dp).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Text(title, style = MaterialTheme.typography.titleSmall, maxLines = 1)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2, minLines = 2)
        }
    }
}
