package com.bloodnetwork.bangladesh.ui.screens

import android.content.Intent
import android.net.Uri
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Bloodtype
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Route
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bloodnetwork.bangladesh.data.model.BloodRequestMatchDto
import com.bloodnetwork.bangladesh.data.model.DonorResponse
import com.bloodnetwork.bangladesh.ui.LocalVmFactory
import com.bloodnetwork.bangladesh.ui.components.SkeletonCard
import com.bloodnetwork.bangladesh.ui.i18n.tr
import com.bloodnetwork.bangladesh.ui.theme.BloodRed
import com.bloodnetwork.bangladesh.ui.viewmodel.DonorMatchesViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DonorMatchesScreen(onBack: () -> Unit) {
    val factory = LocalVmFactory.current!!
    val vm: DonorMatchesViewModel = viewModel(factory = factory)
    val state by vm.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(Unit) { vm.loadMatches() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(tr("My Matches", "আমার মিল"), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = tr("Back", "পেছনে"))
                    }
                },
            )
        },
    ) { padding ->
        PullToRefreshBox(
            isRefreshing = state.isRefreshing,
            onRefresh = { vm.refresh() },
            modifier = Modifier.fillMaxSize().padding(padding),
        ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            val errorMsg = state.error
            if (state.isLoading && state.matches.isEmpty()) {
                items(3) { Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)) { SkeletonCard() } }
            } else if (errorMsg != null && state.matches.isEmpty()) {
                item {
                    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(tr("Failed to load matches", "মিল লোড করা যায়নি"), style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.SemiBold)
                            Text(errorMsg, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Button(onClick = { vm.loadMatches() }, colors = ButtonDefaults.buttonColors(containerColor = BloodRed), shape = RoundedCornerShape(50)) { Text(tr("Retry", "আবার চেষ্টা করুন"), style = MaterialTheme.typography.labelSmall) }
                        }
                    }
                }
            } else if (state.matches.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(top = 48.dp), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Box(modifier = Modifier.size(64.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceVariant), contentAlignment = Alignment.Center) {
                                Icon(Icons.Filled.Bloodtype, contentDescription = null, modifier = Modifier.size(28.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Spacer(Modifier.height(4.dp))
                            Text(tr("No matches yet", "এখনো কোনো মিল নেই"), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                            Text(
                                tr("When your profile matches a blood request, it will show up here", "আপনার প্রোফাইল কোনো রক্তের অনুরোধের সাথে মিললে তা এখানে দেখা যাবে"),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            } else {
                items(state.matches, key = { it.id }) { match ->
                    MatchCard(
                        match = match,
                        isResponding = state.respondingMatchId == match.id,
                        onAccept = { vm.respond(match.id, DonorResponse.Accepted) },
                        onDecline = { vm.respond(match.id, DonorResponse.Declined) },
                        onCall = { context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:${match.requesterPhone}"))) },
                        modifier = Modifier.animateItem(),
                    )
                }
            }
        }
        }
    }
}

@Composable
private fun MatchCard(
    match: BloodRequestMatchDto,
    isResponding: Boolean,
    onAccept: () -> Unit,
    onDecline: () -> Unit,
    onCall: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val accent = responseAccentColor(match.donorResponse)
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            Box(modifier = Modifier.width(4.dp).fillMaxHeight().background(accent))
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.weight(1f)) {
                        Box(modifier = Modifier.size(36.dp).clip(CircleShape).background(BloodRed.copy(alpha = 0.12f)), contentAlignment = Alignment.Center) {
                            Icon(Icons.Filled.Bloodtype, contentDescription = null, tint = BloodRed, modifier = Modifier.size(18.dp))
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(match.requesterName.ifBlank { tr("Unknown requester", "অজানা অনুরোধকারী") }, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, maxLines = 1)
                            Text(tr("Score ${match.matchScore}/100", "স্কোর ${match.matchScore}/১০০"), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    ResponsePill(match.donorResponse)
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(Icons.Filled.LocalHospital, contentDescription = null, modifier = Modifier.size(12.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(match.hospitalName.ifBlank { tr("Unknown hospital", "অজানা হাসপাতাল") }, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
                }
                match.distanceKm?.let {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(Icons.Filled.Route, contentDescription = null, modifier = Modifier.size(12.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(tr("%.1f km away".format(it), "%.1f কিমি দূরে".format(it)), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                when (match.donorResponse) {
                    DonorResponse.Pending -> {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End), verticalAlignment = Alignment.CenterVertically) {
                            if (isResponding) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = BloodRed)
                            } else {
                                OutlinedButton(onClick = onDecline, shape = RoundedCornerShape(50), contentPadding = PaddingValues(horizontal = 14.dp, vertical = 0.dp), modifier = Modifier.height(32.dp)) {
                                    Text(tr("Decline", "প্রত্যাখ্যান করুন"), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold)
                                }
                                Button(onClick = onAccept, colors = ButtonDefaults.buttonColors(containerColor = BloodRed), shape = RoundedCornerShape(50), contentPadding = PaddingValues(horizontal = 14.dp, vertical = 0.dp), modifier = Modifier.height(32.dp)) {
                                    Text(tr("Accept", "গ্রহণ করুন"), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                    }
                    DonorResponse.Accepted -> {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                            Button(onClick = onCall, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)), shape = RoundedCornerShape(50), contentPadding = PaddingValues(horizontal = 14.dp, vertical = 0.dp), modifier = Modifier.height(32.dp)) {
                                Icon(Icons.Filled.Call, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(Modifier.width(6.dp))
                                Text(tr("Call ${match.requesterName}", "${match.requesterName} কে কল করুন"), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                    else -> {}
                }
            }
        }
    }
}

private fun responseAccentColor(response: DonorResponse): Color = when (response) {
    DonorResponse.Accepted -> Color(0xFF2E7D32)
    DonorResponse.Declined -> Color(0xFFC62828)
    DonorResponse.NoResponse -> Color(0xFF616161)
    DonorResponse.Pending -> Color(0xFFEF6C00)
}

@Composable
private fun ResponsePill(response: DonorResponse) {
    val accent = responseAccentColor(response)
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
