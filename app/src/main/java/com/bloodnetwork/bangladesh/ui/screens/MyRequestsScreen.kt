package com.bloodnetwork.bangladesh.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bloodnetwork.bangladesh.data.model.RequestStatus
import com.bloodnetwork.bangladesh.ui.LocalVmFactory
import com.bloodnetwork.bangladesh.ui.components.SkeletonCard
import com.bloodnetwork.bangladesh.ui.i18n.tr
import com.bloodnetwork.bangladesh.ui.navigation.Routes
import com.bloodnetwork.bangladesh.ui.theme.BloodRed
import com.bloodnetwork.bangladesh.ui.viewmodel.RequestBloodViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyRequestsScreen(onNavigate: (String) -> Unit, onBack: () -> Unit) {
    val factory = LocalVmFactory.current!!
    val vm: RequestBloodViewModel = viewModel(factory = factory)
    val state by vm.uiState.collectAsStateWithLifecycle()
    var isRefreshing by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { vm.loadMyRequests() }
    LaunchedEffect(state.loadingRequests) {
        if (!state.loadingRequests) isRefreshing = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(tr("My Requests", "আমার অনুরোধসমূহ"), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = tr("Back", "পেছনে"))
                    }
                },
            )
        },
    ) { padding ->
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = { isRefreshing = true; vm.loadMyRequests() },
            modifier = Modifier.fillMaxSize().padding(padding),
        ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            val errorMsg = state.error
            if (state.loadingRequests && state.myRequests.isEmpty()) {
                items(3) { Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)) { SkeletonCard() } }
            } else if (errorMsg != null && state.myRequests.isEmpty()) {
                item {
                    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(tr("Failed to load requests", "অনুরোধ লোড করা যায়নি"), style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.SemiBold)
                            Text(errorMsg, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Button(onClick = { vm.loadMyRequests() }, colors = ButtonDefaults.buttonColors(containerColor = BloodRed), shape = RoundedCornerShape(50)) { Text(tr("Retry", "আবার চেষ্টা করুন"), style = MaterialTheme.typography.labelSmall) }
                        }
                    }
                }
            } else if (state.myRequests.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(top = 48.dp), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Box(modifier = Modifier.size(64.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceVariant), contentAlignment = Alignment.Center) {
                                Icon(Icons.Filled.Assignment, contentDescription = null, modifier = Modifier.size(28.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Spacer(Modifier.height(4.dp))
                            Text(tr("No requests yet", "এখনো কোনো অনুরোধ নেই"), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                            Text(
                                tr("Requests you create will appear here", "আপনার তৈরি করা অনুরোধগুলো এখানে দেখা যাবে"),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            } else {
                items(state.myRequests, key = { it.id }) { req ->
                    Card(
                        modifier = Modifier.fillMaxWidth().animateItem().clickable { onNavigate("${Routes.REQUEST_DETAILS}/${req.id}") },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Box(modifier = Modifier.size(36.dp).clip(CircleShape).background(BloodRed.copy(alpha = 0.12f)), contentAlignment = Alignment.Center) {
                                        Text(req.bloodGroup.label, style = MaterialTheme.typography.labelLarge, color = BloodRed, fontWeight = FontWeight.Bold)
                                    }
                                    Column {
                                        Text(req.hospitalName, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, maxLines = 1)
                                        Text(
                                            tr("${req.unitsFulfilled} of ${req.unitsRequired} unit(s) fulfilled", "${req.unitsRequired} ইউনিটের মধ্যে ${req.unitsFulfilled} ইউনিট পূরণ"),
                                            style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                    }
                                }
                                MyRequestStatusPill(req.status)
                            }
                            Text(tr("Required by ${req.requiredBy.take(10)}", "প্রয়োজনের তারিখ ${req.requiredBy.take(10)}"), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
        }
    }
}

@Composable
private fun MyRequestStatusPill(status: RequestStatus) {
    val (accent, label) = when (status) {
        RequestStatus.Open -> Color(0xFFC62828) to tr("Open", "খোলা")
        RequestStatus.PartiallyFulfilled -> Color(0xFFEF6C00) to tr("Partial", "আংশিক")
        RequestStatus.Fulfilled -> Color(0xFF2E7D32) to tr("Fulfilled", "পূর্ণ")
        RequestStatus.Cancelled -> Color(0xFF616161) to tr("Cancelled", "বাতিল")
        RequestStatus.Expired -> Color(0xFF616161) to tr("Expired", "মেয়াদোত্তীর্ণ")
    }
    Box(modifier = Modifier.clip(RoundedCornerShape(50)).background(accent.copy(alpha = 0.12f)).padding(horizontal = 10.dp, vertical = 5.dp)) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = accent, fontWeight = FontWeight.SemiBold, maxLines = 1)
    }
}
