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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
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
import com.bloodnetwork.bangladesh.data.model.UserRole
import com.bloodnetwork.bangladesh.ui.LocalVmFactory
import com.bloodnetwork.bangladesh.ui.components.SkeletonCard
import com.bloodnetwork.bangladesh.ui.i18n.tr
import com.bloodnetwork.bangladesh.ui.theme.BloodRed
import com.bloodnetwork.bangladesh.ui.viewmodel.AdminViewModel
import com.bloodnetwork.bangladesh.ui.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminUserManagementScreen(onNavigate: (String) -> Unit, onBack: () -> Unit) {
    val factory = LocalVmFactory.current!!
    val vm: AdminViewModel = viewModel(factory = factory)
    val authVm: AuthViewModel = viewModel(factory = factory)
    val state by vm.uiState.collectAsStateWithLifecycle()
    val authState by authVm.uiState.collectAsStateWithLifecycle()
    val currentUserId = authState.user?.id

    var searchQuery by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf("") }
    var selectedStatus by remember { mutableStateOf("") } // All | Active | Deactive

    fun isActiveFilter(): Boolean? = when (selectedStatus) {
        "Active" -> true
        "Deactive" -> false
        else -> null
    }

    LaunchedEffect(Unit) { vm.loadUsers(searchQuery.ifBlank { null }, selectedRole.ifBlank { null }, isActiveFilter()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(tr("User Management", "ব্যবহারকারী ব্যবস্থাপনা"), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold) },
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
            onRefresh = { vm.refreshUsers() },
            modifier = Modifier.fillMaxSize().padding(padding).imePadding(),
        ) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text(tr("Search name, phone, email", "নাম, ফোন, ইমেইল খুঁজুন"), style = MaterialTheme.typography.bodySmall) },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = {
                                    searchQuery = ""
                                    vm.loadUsers(null, selectedRole.ifBlank { null }, isActiveFilter())
                                }) {
                                    Icon(Icons.Filled.Close, contentDescription = tr("Clear search", "খোঁজ মুছুন"))
                                }
                            }
                        },
                    )
                    Button(
                        onClick = { vm.loadUsers(searchQuery.ifBlank { null }, selectedRole.ifBlank { null }, isActiveFilter()) },
                        colors = ButtonDefaults.buttonColors(containerColor = BloodRed),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 12.dp),
                    ) { Text(tr("Search", "খুঁজুন"), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold) }
                }
            }
            // --- Professional two-row filter groups: Role + Status ---
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    // Role row
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(tr("Role", "ভূমিকা"), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.width(48.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.weight(1f)) {
                            FilterChip(
                                selected = selectedRole == "", onClick = { selectedRole = ""; vm.loadUsers(searchQuery.ifBlank { null }, null, isActiveFilter()) },
                                label = { Text(tr("All", "সব"), style = MaterialTheme.typography.labelSmall) },
                                shape = RoundedCornerShape(50),
                                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = BloodRed, selectedLabelColor = Color.White),
                            )
                            FilterChip(selected = selectedRole == "Donor", onClick = { selectedRole = "Donor"; vm.loadUsers(searchQuery.ifBlank { null }, "Donor", isActiveFilter()) }, label = { Text(tr("Donor", "রক্তদাতা"), style = MaterialTheme.typography.labelSmall) }, shape = RoundedCornerShape(50), colors = FilterChipDefaults.filterChipColors(selectedContainerColor = BloodRed, selectedLabelColor = Color.White))
                            FilterChip(selected = selectedRole == "Requester", onClick = { selectedRole = "Requester"; vm.loadUsers(searchQuery.ifBlank { null }, "Requester", isActiveFilter()) }, label = { Text(tr("Requester", "অনুরোধকারী"), style = MaterialTheme.typography.labelSmall) }, shape = RoundedCornerShape(50), colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Color(0xFF1565C0), selectedLabelColor = Color.White))
                            FilterChip(selected = selectedRole == "Admin", onClick = { selectedRole = "Admin"; vm.loadUsers(searchQuery.ifBlank { null }, "Admin", isActiveFilter()) }, label = { Text(tr("Admin", "অ্যাডমিন"), style = MaterialTheme.typography.labelSmall) }, shape = RoundedCornerShape(50), colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Color(0xFF7B1FA2), selectedLabelColor = Color.White))
                        }
                    }
                    // Status row - modern, clean with icons
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(tr("Status", "অবস্থা"), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.width(48.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.weight(1f)) {
                            FilterChip(
                                selected = selectedStatus == "", onClick = { selectedStatus = ""; vm.loadUsers(searchQuery.ifBlank { null }, selectedRole.ifBlank { null }, null) },
                                label = { Text(tr("All", "সব"), style = MaterialTheme.typography.labelSmall) },
                                shape = RoundedCornerShape(50),
                                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = BloodRed, selectedLabelColor = Color.White),
                            )
                            FilterChip(
                                selected = selectedStatus == "Active",
                                onClick = { selectedStatus = "Active"; vm.loadUsers(searchQuery.ifBlank { null }, selectedRole.ifBlank { null }, true) },
                                label = { Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) { Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(14.dp)); Text(tr("Active", "সক্রিয়"), style = MaterialTheme.typography.labelSmall) } },
                                shape = RoundedCornerShape(50),
                                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Color(0xFF2E7D32), selectedLabelColor = Color.White),
                            )
                            FilterChip(
                                selected = selectedStatus == "Deactive",
                                onClick = { selectedStatus = "Deactive"; vm.loadUsers(searchQuery.ifBlank { null }, selectedRole.ifBlank { null }, false) },
                                label = { Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) { Icon(Icons.Filled.Close, contentDescription = null, modifier = Modifier.size(14.dp)); Text(tr("Deactive", "নিষ্ক্রিয়"), style = MaterialTheme.typography.labelSmall) } },
                                shape = RoundedCornerShape(50),
                                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Color(0xFFC62828), selectedLabelColor = Color.White),
                            )
                        }
                    }
                }
            }
            val errorMsg = state.error
            if (state.isLoading) {
                items(5) { Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)) { SkeletonCard() } }
            } else if (errorMsg != null) {
                item {
                    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(tr("Failed to load users", "ব্যবহারকারীদের তথ্য লোড করা যায়নি"), style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.SemiBold)
                            Text(errorMsg, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Button(onClick = { vm.loadUsers(searchQuery.ifBlank { null }, selectedRole.ifBlank { null }, isActiveFilter()) }, colors = ButtonDefaults.buttonColors(containerColor = BloodRed), shape = RoundedCornerShape(50), contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)) { Text(tr("Retry", "আবার চেষ্টা করুন"), style = MaterialTheme.typography.labelSmall) }
                        }
                    }
                }
            } else if (state.users.isEmpty()) {
                item {
                    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)) {
                        Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(tr("No users found", "কোনো ব্যবহারকারী পাওয়া যায়নি"), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                            Text(tr("Try a different search or role filter", "ভিন্ন খোঁজ বা ভূমিকা ফিল্টার চেষ্টা করুন"), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            } else {
                items(state.users, key = { it.id }) { user ->
                    Card(
                        modifier = Modifier.fillMaxWidth().animateItem(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    com.bloodnetwork.bangladesh.ui.components.Avatar(photoUrl = user.photoUrl, size = 36.dp)
                                    Column {
                                        Text("${user.firstName} ${user.lastName}", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, maxLines = 1)
                                        Spacer(Modifier.height(2.dp))
                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            Icon(Icons.Filled.Phone, contentDescription = null, modifier = Modifier.width(12.dp).height(12.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                            Text(user.phoneNumber, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    }
                                }
                                RoleBadge(user.role.name)
                            }
                            user.email?.let {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Icon(Icons.Filled.Email, contentDescription = null, modifier = Modifier.width(12.dp).height(12.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
                                }
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                                StatusPill(text = if (user.isActive) tr("Active", "সক্রিয়") else tr("Inactive", "নিষ্ক্রিয়"), active = user.isActive)
                                user.donorVerificationStatus?.let {
                                    VerifyPill(status = it)
                                }
                            }
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    if (user.role == UserRole.Donor) {
                                        when (user.donorVerificationStatus) {
                                            "Unverified", "Rejected" -> {
                                                CompactFilledButton(tr("Verify", "যাচাই করুন"), onClick = { vm.verifyDonor(user.id, "Verified") })
                                            }
                                            "Verified" -> {
                                                CompactOutlinedButton(tr("Reject", "প্রত্যাখ্যান করুন"), onClick = { vm.verifyDonor(user.id, "Rejected") })
                                            }
                                            "Pending" -> {
                                                CompactFilledButton(tr("Verify", "যাচাই করুন"), onClick = { vm.verifyDonor(user.id, "Verified") })
                                                CompactOutlinedButton(tr("Reject", "প্রত্যাখ্যান করুন"), onClick = { vm.verifyDonor(user.id, "Rejected") })
                                            }
                                        }
                                    }
                                    val isSelf = currentUserId != null && user.id == currentUserId
                                    if (user.isActive) {
                                        CompactOutlinedButton(tr("Deactivate", "নিষ্ক্রিয় করুন"), onClick = { vm.toggleActive(user.id, false) }, enabled = !isSelf)
                                    } else {
                                        CompactFilledButton(tr("Activate", "সক্রিয় করুন"), onClick = { vm.toggleActive(user.id, true) }, enabled = !isSelf)
                                    }
                                }
                            }
                        }
                    }
                }
                // Pagination footer - keep cards, only swap bar (Showing X to Y + << < 1 > >> + 10|20|50|100)
                if (state.users.isNotEmpty() && !state.isLoading) {
                    item {
                        com.bloodnetwork.bangladesh.ui.components.PaginationFooter(
                            page = state.usersPage,
                            pageSize = state.usersPageSize,
                            totalCount = state.usersTotalCount,
                            label = tr("users", "ব্যবহারকারী"),
                            onPageChange = { newPage -> vm.gotoUsersPage(newPage) },
                            onPageSizeChange = { newSize -> vm.loadUsers(searchQuery.ifBlank { null }, selectedRole.ifBlank { null }, isActiveFilter(), newSize) }
                        )
                    }
                }
                if (state.usersLoadingMore) {
                    item {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp))
                        }
                    }
                }
            }
        }
        }
    }
}

@Composable
private fun RoleBadge(role: String) {
    val bg = when (role) {
        "Admin" -> Color(0xFF7B1FA2).copy(alpha = 0.1f)
        "Donor" -> BloodRed.copy(alpha = 0.1f)
        else -> Color(0xFF1565C0).copy(alpha = 0.1f)
    }
    val fg = when (role) {
        "Admin" -> Color(0xFF7B1FA2)
        "Donor" -> BloodRed
        else -> Color(0xFF1565C0)
    }
    val displayText = when (role) {
        "Admin" -> tr("Admin", "অ্যাডমিন")
        "Donor" -> tr("Donor", "রক্তদাতা")
        "Requester" -> tr("Requester", "অনুরোধকারী")
        else -> role
    }
    Box(modifier = Modifier.clip(RoundedCornerShape(50)).background(bg).padding(horizontal = 10.dp, vertical = 5.dp)) {
        Text(displayText, style = MaterialTheme.typography.labelSmall, color = fg, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun StatusPill(text: String, active: Boolean) {
    val bg = if (active) Color(0xFF2E7D32).copy(alpha = 0.1f) else Color(0xFFC62828).copy(alpha = 0.1f)
    val fg = if (active) Color(0xFF2E7D32) else Color(0xFFC62828)
    Box(modifier = Modifier.clip(RoundedCornerShape(50)).background(bg).padding(horizontal = 8.dp, vertical = 4.dp)) {
        Text(text, style = MaterialTheme.typography.labelSmall, color = fg, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun VerifyPill(status: String) {
    val (bg, fg) = when (status) {
        "Verified" -> Color(0xFF2E7D32).copy(alpha = 0.1f) to Color(0xFF2E7D32)
        "Pending" -> Color(0xFFEF6C00).copy(alpha = 0.12f) to Color(0xFFEF6C00)
        "Rejected" -> Color(0xFFC62828).copy(alpha = 0.1f) to Color(0xFFC62828)
        else -> Color(0xFF616161).copy(alpha = 0.1f) to Color(0xFF616161)
    }
    val displayText = when (status) {
        "Verified" -> tr("Verified", "যাচাইকৃত")
        "Pending" -> tr("Pending", "মুলতুবি")
        "Rejected" -> tr("Rejected", "প্রত্যাখ্যাত")
        "Unverified" -> tr("Unverified", "অযাচাইকৃত")
        else -> status
    }
    Box(modifier = Modifier.clip(RoundedCornerShape(50)).background(bg).padding(horizontal = 8.dp, vertical = 4.dp)) {
        Text(displayText, style = MaterialTheme.typography.labelSmall, color = fg, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun CompactFilledButton(text: String, onClick: () -> Unit, enabled: Boolean = true) {
    Button(
        onClick = onClick,
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(containerColor = BloodRed),
        shape = RoundedCornerShape(50),
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 0.dp),
        modifier = Modifier.height(32.dp),
    ) { Text(text, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold) }
}

@Composable
private fun CompactOutlinedButton(text: String, onClick: () -> Unit, enabled: Boolean = true) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(50),
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 0.dp),
        modifier = Modifier.height(32.dp),
    ) { Text(text, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold) }
}
