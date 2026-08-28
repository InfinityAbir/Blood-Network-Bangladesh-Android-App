package com.bloodnetwork.bangladesh.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
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
import com.bloodnetwork.bangladesh.ui.components.PrimaryButton
import com.bloodnetwork.bangladesh.ui.navigation.Routes
import com.bloodnetwork.bangladesh.ui.theme.BloodRed
import com.bloodnetwork.bangladesh.ui.viewmodel.AuthViewModel
import com.bloodnetwork.bangladesh.ui.viewmodel.NotificationsViewModel

data class DashboardAction(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val route: String,
)

private val actions = listOf(
    DashboardAction("Find Blood", "Search for donors by blood group and location", Icons.Filled.Search, Routes.FIND_BLOOD),
    DashboardAction("Donate Blood", "Check eligibility and become a donor", Icons.Filled.WaterDrop, Routes.ELIGIBILITY),
    DashboardAction("Request Blood", "Create a new blood request", Icons.Filled.HealthAndSafety, Routes.REQUEST_BLOOD),
    DashboardAction("My Donor Profile", "Manage your donor profile and availability", Icons.Filled.Person, Routes.DONOR_PROFILE),
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

    LaunchedEffect(Unit) { notifVm.loadUnreadCount() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Blood Network BD") },
                actions = {
                    BadgedBox(
                        badge = {
                            if (notifState.unreadCount > 0) {
                                Badge { Text("${notifState.unreadCount}") }
                            }
                        },
                    ) {
                        IconButton(onClick = { showNotifSheet = true }) {
                            Icon(
                                Icons.Filled.Notifications,
                                contentDescription = "Notifications",
                            )
                        }
                    }
                },
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Text(
                    text = "Welcome${state.user?.let { ", ${it.firstName}" } ?: ""}",
                    style = MaterialTheme.typography.headlineSmall,
                )
            }
            items(actions) { action ->
                ActionCard(action, onNavigate)
            }
            item {
                PrimaryButton("Logout", onClick = onLogout)
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
        )
    }
}

@Composable
fun ActionCard(action: DashboardAction, onNavigate: (String) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onNavigate(action.route) },
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Icon(
                imageVector = action.icon,
                contentDescription = null,
                tint = BloodRed,
            )
            Column {
                Text(action.title, style = MaterialTheme.typography.titleMedium)
                Text(action.subtitle, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
