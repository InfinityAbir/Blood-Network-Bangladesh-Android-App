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
import com.bloodnetwork.bangladesh.ui.components.SkeletonLine
import com.bloodnetwork.bangladesh.ui.i18n.tr
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
                title = { Text(tr("Admin Dashboard", "অ্যাডমিন ড্যাশবোর্ড")) },
                actions = {
                    com.bloodnetwork.bangladesh.ui.components.LanguageToggleButton()
                    com.bloodnetwork.bangladesh.ui.components.ThemeToggleButton()
                    BadgedBox(
                        badge = { com.bloodnetwork.bangladesh.ui.components.AnimatedCountBadge(notifState.unreadCount) },
                    ) {
                        IconButton(onClick = { showNotifSheet = true }) {
                            Icon(Icons.Filled.Notifications, contentDescription = tr("Notifications", "নোটিফিকেশন"))
                        }
                    }
                    Spacer(Modifier.width(4.dp))
                    IconButton(onClick = onLogout) {
                        Icon(Icons.Filled.Logout, contentDescription = tr("Logout", "লগ আউট"))
                    }
                    Spacer(Modifier.width(4.dp))
                },
            )
        },
    ) { padding ->
        if (state.isLoading) {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                item { SkeletonLine("30%", 20) }
                items(3) {
                    Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        StatCardSkeleton(Modifier.weight(1f).fillMaxHeight())
                        StatCardSkeleton(Modifier.weight(1f).fillMaxHeight())
                    }
                }
                item { SkeletonLine("35%", 20) }
                items(4) {
                    Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        ManagementCardSkeleton(Modifier.weight(1f).fillMaxHeight())
                        ManagementCardSkeleton(Modifier.weight(1f).fillMaxHeight())
                    }
                }
            }
        } else {
            PullToRefreshBox(
                isRefreshing = state.isRefreshing,
                onRefresh = { vm.refreshDashboard() },
                modifier = Modifier.fillMaxSize().padding(padding),
            ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                state.dashboard?.let { stats ->
                    item { Text(tr("Overview", "সংক্ষিপ্ত বিবরণ"), style = MaterialTheme.typography.titleMedium, fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold) }
                    item {
                        Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            StatCard(tr("Total Users", "মোট ব্যবহারকারী"), stats.totalUsers, Modifier.weight(1f).fillMaxHeight())
                            StatCard(tr("Donors", "দাতা"), stats.totalDonors, Modifier.weight(1f).fillMaxHeight())
                        }
                    }
                    item {
                        Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            StatCard(tr("Blood Requests", "রক্তের অনুরোধ"), stats.openBloodRequests, Modifier.weight(1f).fillMaxHeight(), tr("open", "খোলা"))
                            StatCard(tr("Matches", "মিল"), stats.acceptedMatches, Modifier.weight(1f).fillMaxHeight(), tr("accepted", "গৃহীত"))
                        }
                    }
                    item {
                        Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            StatCard(tr("Reports", "রিপোর্ট"), stats.openReports, Modifier.weight(1f).fillMaxHeight(), tr("open", "খোলা"))
                            StatCard(tr("Pending Verifications", "মুলতুবি যাচাইকরণ"), stats.pendingVerifications, Modifier.weight(1f).fillMaxHeight())
                        }
                    }
                }

                item { Text(tr("Management", "ব্যবস্থাপনা"), style = MaterialTheme.typography.titleMedium, fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold) }
                item {
                    Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        ManagementCard(tr("Analytics", "বিশ্লেষণ"), tr("Trends, blood-type & district breakdowns", "প্রবণতা, রক্তের গ্রুপ ও জেলাভিত্তিক বিভাজন"), Icons.Filled.BarChart, Modifier.weight(1f).fillMaxHeight()) { onNavigate("admin_analytics") }
                        ManagementCard(tr("User Management", "ব্যবহারকারী ব্যবস্থাপনা"), tr("Verify, activate & manage accounts", "অ্যাকাউন্ট যাচাই, সক্রিয় ও পরিচালনা করুন"), Icons.Filled.People, Modifier.weight(1f).fillMaxHeight()) { onNavigate("admin_users") }
                    }
                }
                item {
                    Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        ManagementCard(tr("Reports", "রিপোর্ট"), tr("Review & resolve flagged reports", "চিহ্নিত রিপোর্ট পর্যালোচনা ও সমাধান করুন"), Icons.Filled.Assignment, Modifier.weight(1f).fillMaxHeight()) { onNavigate("admin_reports") }
                        ManagementCard(tr("Audit Logs", "অডিট লগ"), tr("Track system activity history", "সিস্টেম কার্যকলাপের ইতিহাস ট্র্যাক করুন"), Icons.Filled.History, Modifier.weight(1f).fillMaxHeight()) { onNavigate("admin_audit_logs") }
                    }
                }
                item {
                    Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        ManagementCard(tr("Account Settings", "অ্যাকাউন্ট সেটিংস"), tr("Update email, phone & password", "ইমেইল, ফোন ও পাসওয়ার্ড আপডেট করুন"), Icons.Filled.Settings, Modifier.weight(1f).fillMaxHeight()) { onNavigate("admin_settings") }
                        ManagementCard(tr("About", "সম্পর্কে"), tr("Edit the developer info shown to users", "ব্যবহারকারীদের দেখানো ডেভেলপার তথ্য সম্পাদনা করুন"), Icons.Filled.Info, Modifier.weight(1f).fillMaxHeight()) { onNavigate("about") }
                    }
                }
                item {
                    Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        ManagementCard(tr("Eligibility Questions", "যোগ্যতার প্রশ্নাবলী"), tr("Edit the donor eligibility questionnaire", "দাতার যোগ্যতা প্রশ্নাবলী সম্পাদনা করুন"), Icons.Filled.FactCheck, Modifier.weight(1f).fillMaxHeight()) { onNavigate(com.bloodnetwork.bangladesh.ui.navigation.Routes.ADMIN_ELIGIBILITY_QUESTIONS) }
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
fun StatCardSkeleton(modifier: Modifier = Modifier) {
    Card(modifier = modifier.heightIn(min = 92.dp)) {
        Column(modifier = Modifier.padding(16.dp).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            SkeletonLine("40%", 28)
            SkeletonLine("70%", 14)
        }
    }
}

@Composable
fun ManagementCardSkeleton(modifier: Modifier = Modifier) {
    Card(modifier = modifier.heightIn(min = 118.dp)) {
        Column(modifier = Modifier.padding(16.dp).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            SkeletonLine("20%", 24)
            SkeletonLine("60%", 16)
            SkeletonLine("90%", 12)
        }
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
