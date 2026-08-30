package com.bloodnetwork.bangladesh.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Bloodtype
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bloodnetwork.bangladesh.ui.LocalVmFactory
import com.bloodnetwork.bangladesh.ui.i18n.tr
import com.bloodnetwork.bangladesh.ui.navigation.Routes
import com.bloodnetwork.bangladesh.ui.viewmodel.AuthViewModel
import com.bloodnetwork.bangladesh.ui.viewmodel.NotificationsViewModel

data class DashboardAction(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val route: String,
)

@Composable
private fun dashboardActions(): List<DashboardAction> = listOf(
    DashboardAction(tr("Find Blood", "রক্ত খুঁজুন"), tr("Search for donors by blood group and location", "রক্তের গ্রুপ ও এলাকা অনুযায়ী দাতা খুঁজুন"), Icons.Filled.Search, Routes.FIND_BLOOD),
    DashboardAction(tr("Donate Blood", "রক্ত দিন"), tr("Check eligibility and become a donor", "যোগ্যতা যাচাই করুন এবং দাতা হয়ে উঠুন"), Icons.Filled.WaterDrop, Routes.ELIGIBILITY),
    DashboardAction(tr("Request Blood", "রক্তের অনুরোধ করুন"), tr("Create a new blood request", "নতুন রক্তের অনুরোধ তৈরি করুন"), Icons.Filled.HealthAndSafety, Routes.REQUEST_BLOOD),
    DashboardAction(tr("My Requests", "আমার অনুরোধসমূহ"), tr("View, edit or cancel your blood requests", "আপনার রক্তের অনুরোধ দেখুন, সম্পাদনা বা বাতিল করুন"), Icons.Filled.Assignment, Routes.MY_REQUESTS),
    DashboardAction(tr("My Donor Profile", "আমার দাতা প্রোফাইল"), tr("Manage your donor profile and availability", "আপনার দাতা প্রোফাইল ও সহজলভ্যতা পরিচালনা করুন"), Icons.Filled.Person, Routes.DONOR_PROFILE),
    DashboardAction(tr("My Matches", "আমার মিল"), tr("Respond to donor match requests", "দাতা মিলের অনুরোধে সাড়া দিন"), Icons.Filled.Bloodtype, Routes.MY_MATCHES),
    DashboardAction(tr("Edit Profile", "প্রোফাইল সম্পাদনা করুন"), tr("Change your phone, email or password", "আপনার ফোন, ইমেইল বা পাসওয়ার্ড পরিবর্তন করুন"), Icons.Filled.Settings, Routes.EDIT_PROFILE),
    DashboardAction(tr("About", "সম্পর্কে"), tr("Meet the developer behind this app", "এই অ্যাপের পেছনের ডেভেলপারের সাথে পরিচিত হন"), Icons.Filled.Info, Routes.ABOUT),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DonorDashboardScreen(
    onNavigate: (String) -> Unit,
    vm: AuthViewModel,
    onLogout: () -> Unit,
) {
    val state by vm.uiState.collectAsStateWithLifecycle()
    val factory = LocalVmFactory.current!!
    val notifVm: NotificationsViewModel = viewModel(factory = factory)
    val notifState by notifVm.uiState.collectAsStateWithLifecycle()
    var showNotifSheet by remember { mutableStateOf(false) }
    val actions = dashboardActions()

    LaunchedEffect(Unit) { notifVm.loadUnreadCount() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(tr("Blood Network BD", "ব্লাড নেটওয়ার্ক বিডি")) },
                actions = {
                    com.bloodnetwork.bangladesh.ui.components.LanguageToggleButton()
                    com.bloodnetwork.bangladesh.ui.components.ThemeToggleButton()
                    BadgedBox(
                        badge = { com.bloodnetwork.bangladesh.ui.components.AnimatedCountBadge(notifState.unreadCount) },
                    ) {
                        IconButton(onClick = { showNotifSheet = true }) {
                            Icon(
                                Icons.Filled.Notifications,
                                contentDescription = tr("Notifications", "নোটিফিকেশন"),
                            )
                        }
                    }
                    Spacer(Modifier.width(4.dp))
                    IconButton(onClick = onLogout) {
                        Icon(
                            Icons.Filled.Logout,
                            contentDescription = tr("Logout", "লগ আউট"),
                        )
                    }
                    Spacer(Modifier.width(4.dp))
                },
            )
        },
    ) { padding ->
        PullToRefreshBox(
            isRefreshing = state.isProfileRefreshing,
            onRefresh = {
                vm.refreshProfile()
                notifVm.loadUnreadCount()
            },
            modifier = Modifier.fillMaxSize().padding(padding),
        ) {
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 160.dp),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                Text(
                    text = "${tr("Welcome", "স্বাগতম")}${state.user?.let { ", ${it.firstName}" } ?: ""}",
                    style = MaterialTheme.typography.headlineSmall,
                )
            }
            items(actions) { action ->
                ActionCard(action, onNavigate)
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
fun ActionCard(action: DashboardAction, onNavigate: (String) -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val containerColor by animateColorAsState(
        targetValue = if (isPressed) MaterialTheme.colorScheme.primary.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface,
        label = "cardColor",
    )

    Card(
        onClick = { onNavigate(action.route) },
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 132.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp, pressedElevation = 4.dp),
        interactionSource = interactionSource,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = action.icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
            Text(action.title, style = MaterialTheme.typography.titleMedium)
            Text(
                action.subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                minLines = 2,
            )
        }
    }
}
