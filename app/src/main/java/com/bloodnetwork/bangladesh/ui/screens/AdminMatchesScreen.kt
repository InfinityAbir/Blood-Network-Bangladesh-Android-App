package com.bloodnetwork.bangladesh.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Route
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
import com.bloodnetwork.bangladesh.data.model.BloodGroup
import com.bloodnetwork.bangladesh.data.model.DonorResponse
import com.bloodnetwork.bangladesh.ui.LocalVmFactory
import com.bloodnetwork.bangladesh.ui.components.Avatar
import com.bloodnetwork.bangladesh.ui.components.PaginationFooter
import com.bloodnetwork.bangladesh.ui.components.SkeletonCard
import com.bloodnetwork.bangladesh.ui.i18n.tr
import com.bloodnetwork.bangladesh.ui.theme.BloodRed
import com.bloodnetwork.bangladesh.ui.viewmodel.AdminViewModel
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow

private data class ResponseFilterOption(val value: String?, val labelEn: String, val labelBn: String)

private val RESPONSE_FILTERS = listOf(
    ResponseFilterOption(null, "All", "সব"),
    ResponseFilterOption("Accepted", "Accepted", "গৃহীত"),
    ResponseFilterOption("Pending", "Pending", "মুলতুবি"),
    ResponseFilterOption("Declined", "Declined", "প্রত্যাখ্যাত"),
    ResponseFilterOption("NoResponse", "No Response", "সাড়া দেয়নি"),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminMatchesScreen(onBack: () -> Unit, initialResponse: String? = null) {
    val factory = LocalVmFactory.current
    val vm: AdminViewModel = viewModel(factory = factory)
    val state by vm.uiState.collectAsStateWithLifecycle()

    var selectedResponse by remember { mutableStateOf(initialResponse) }

    LaunchedEffect(Unit) { vm.loadMatches(selectedResponse) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(tr("Donor Matches", "দাতা মিল"), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        Text(
                            tr("${state.matchesTotalCount} match${if (state.matchesTotalCount == 1) "" else "es"}", "${state.matchesTotalCount} টি মিল"),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                },
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
            onRefresh = { vm.refreshMatches() },
            modifier = Modifier.fillMaxSize().padding(padding),
        ) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 130.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            item {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(tr("Status", "অবস্থা"), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.width(48.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), contentPadding = PaddingValues(end = 8.dp)) {
                        items(RESPONSE_FILTERS) { option ->
                            FilterChip(
                                selected = selectedResponse == option.value,
                                onClick = { selectedResponse = option.value; vm.loadMatches(option.value) },
                                label = { Text(tr(option.labelEn, option.labelBn), style = MaterialTheme.typography.labelSmall) },
                                shape = RoundedCornerShape(50),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = responseAccentColor(option.value ?: "Pending"),
                                    selectedLabelColor = Color.White,
                                ),
                            )
                        }
                    }
                }
            }
            val errorMsg = state.error
            if (state.isLoading && state.matches.isEmpty()) {
                items(4) { Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)) { SkeletonCard() } }
            } else if (errorMsg != null && state.matches.isEmpty()) {
                item {
                    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(tr("Failed to load matches", "মিল লোড করা যায়নি"), style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.SemiBold)
                            Text(errorMsg, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Button(onClick = { vm.loadMatches(selectedResponse) }, colors = ButtonDefaults.buttonColors(containerColor = BloodRed), shape = RoundedCornerShape(50)) { Text(tr("Retry", "আবার চেষ্টা করুন"), style = MaterialTheme.typography.labelSmall) }
                        }
                    }
                }
            } else if (state.matches.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(top = 48.dp), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Box(modifier = Modifier.size(64.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceVariant), contentAlignment = Alignment.Center) {
                                Icon(Icons.Filled.Handshake, contentDescription = null, modifier = Modifier.size(28.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Spacer(Modifier.height(4.dp))
                            Text(tr("No matches", "কোনো মিল নেই"), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                            Text(tr("Donor-request matches will appear here", "দাতা-অনুরোধের মিল এখানে দেখা যাবে"), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            } else {
                items(state.matches, key = { it.id }) { match ->
                    val accent = responseAccentColor(match.donorResponse.name)
                    Card(
                        modifier = Modifier.fillMaxWidth().animateItem(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    ) {
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Box(modifier = Modifier.width(4.dp).fillMaxHeight().background(accent))
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.weight(1f)) {
                                        if (!match.donorPhotoUrl.isNullOrBlank()) {
                                            Avatar(photoUrl = match.donorPhotoUrl, size = 36.dp)
                                        } else {
                                            Box(modifier = Modifier.size(36.dp).clip(CircleShape).background(BloodRed.copy(alpha = 0.12f)), contentAlignment = Alignment.Center) {
                                                Text(
                                                    text = formatBloodGroupLabel(match.donorBloodGroup),
                                                    style = MaterialTheme.typography.labelMedium,
                                                    color = BloodRed,
                                                    fontWeight = FontWeight.Bold,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis,
                                                    textAlign = TextAlign.Center,
                                                )
                                            }
                                        }
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(match.donorName, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, maxLines = 1)
                                            Text(
                                                tr("for ${match.requesterName.ifBlank { "Unknown requester" }}", "${match.requesterName.ifBlank { "অজানা অনুরোধকারী" }} এর জন্য"),
                                                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1,
                                            )
                                        }
                                    }
                                    Spacer(Modifier.width(8.dp))
                                    ResponsePill(match.donorResponse)
                                }
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Icon(Icons.Filled.LocalHospital, contentDescription = null, modifier = Modifier.size(12.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(match.hospitalName.ifBlank { tr("Unknown hospital", "অজানা হাসপাতাল") }, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
                                }
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                                    match.distanceKm?.let { km ->
                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            Icon(Icons.Filled.Route, contentDescription = null, modifier = Modifier.size(12.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                            Text(
                                                tr("%.1f km".format(km), "%.1f কিমি".format(km)),
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                            )
                                        }
                                    }
                                    Text(
                                        tr("Score ${match.matchScore}", "স্কোর ${match.matchScore}"),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                    )
                                }
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Icon(Icons.Filled.History, contentDescription = null, modifier = Modifier.size(12.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(match.createdAt.take(10), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }
                item {
                    PaginationFooter(
                        page = state.matchesPage,
                        pageSize = state.matchesPageSize,
                        totalCount = state.matchesTotalCount,
                        label = tr("matches", "মিল"),
                        onPageChange = { newPage -> vm.gotoMatchesPage(newPage) },
                        onPageSizeChange = { newSize -> vm.loadMatches(selectedResponse, newSize) },
                    )
                }
            }
        }
        }
    }
}

private fun formatBloodGroupLabel(raw: String): String {
    if (raw.isBlank()) return "?"
    val trimmed = raw.trim()
    // Direct label match (A+, AB-, etc.) - already compact
    BloodGroup.entries.firstOrNull { it.label.equals(trimmed, ignoreCase = true) }?.let { return it.label }
    // Enum name match (APositive, ANegative, etc.)
    BloodGroup.entries.firstOrNull { it.name.equals(trimmed, ignoreCase = true) }?.let { return it.label }
    // Normalized forms: handle "A Positive", "A+ve", "Apos", "AB Positive", etc.
    // Keep '+' but strip spaces/underscores/hyphens so "A-ve" -> "ave" while "A+ve" -> "a+ve"
    val lower = trimmed.lowercase()
    val normalized = lower.replace(" ", "").replace("_", "").replace("-", "")
    return when (normalized) {
        "apositive", "apos", "aposve", "a+", "aplus", "a+ve" -> "A+"
        "anegative", "aneg", "anegve", "a-", "aminus", "ave" -> "A-"
        "bpositive", "bpos", "bposve", "b+", "bplus", "b+ve" -> "B+"
        "bnegative", "bneg", "bnegve", "b-", "bminus", "bve" -> "B-"
        "abpositive", "abpos", "abposve", "ab+", "abplus", "ab+ve" -> "AB+"
        "abnegative", "abneg", "abnegve", "ab-", "abminus", "abve" -> "AB-"
        "opositive", "opos", "oposve", "o+", "oplus", "o+ve" -> "O+"
        "onegative", "oneg", "onegve", "o-", "ominus", "ove" -> "O-"
        else -> {
            // Fallback: try to infer from first chars (a, b, ab, o) + pos/neg/+/- keywords
            val isPositive = lower.contains("pos") || lower.contains("+") || lower.contains("plus")
            val isNegative = lower.contains("neg") || lower.contains("minus") || (lower.contains("-") && !isPositive)
            val base = when {
                lower.startsWith("ab") -> "AB"
                lower.startsWith("a") -> "A"
                lower.startsWith("b") -> "B"
                lower.startsWith("o") -> "O"
                else -> null
            }
            if (base != null) {
                when {
                    isPositive -> return "$base+"
                    isNegative -> return "$base-"
                }
            }
            BloodGroup.entries.firstOrNull { normalized.contains(it.label.lowercase().replace("+", "pos").replace("-", "neg")) }?.label
                ?: trimmed.take(3).uppercase()
        }
    }
}

private fun responseAccentColor(response: String): Color = when (response) {
    "Accepted" -> Color(0xFF2E7D32)
    "Declined" -> Color(0xFFC62828)
    "NoResponse" -> Color(0xFF616161)
    else -> Color(0xFFEF6C00)
}

@Composable
private fun ResponsePill(response: DonorResponse) {
    val accent = responseAccentColor(response.name)
    val displayText = when (response) {
        DonorResponse.Accepted -> tr("Accepted", "গৃহীত")
        DonorResponse.Declined -> tr("Declined", "প্রত্যাখ্যাত")
        DonorResponse.NoResponse -> tr("No Response", "সাড়া দেয়নি")
        DonorResponse.Pending -> tr("Pending", "মুলতুবি")
    }
    Box(modifier = Modifier.clip(RoundedCornerShape(50)).background(accent.copy(alpha = 0.12f)).padding(horizontal = 10.dp, vertical = 5.dp)) {
        Text(displayText, style = MaterialTheme.typography.labelSmall, color = accent, fontWeight = FontWeight.SemiBold, maxLines = 1)
    }
}
