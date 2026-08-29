package com.bloodnetwork.bangladesh.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bloodnetwork.bangladesh.data.model.AdminAnalyticsDto
import com.bloodnetwork.bangladesh.ui.LocalVmFactory
import com.bloodnetwork.bangladesh.ui.components.BarItem
import com.bloodnetwork.bangladesh.ui.components.BarListChart
import com.bloodnetwork.bangladesh.ui.components.MetricTile
import com.bloodnetwork.bangladesh.ui.components.SkeletonCard
import com.bloodnetwork.bangladesh.ui.components.TrendLineChart
import com.bloodnetwork.bangladesh.ui.theme.AvailableGreen
import com.bloodnetwork.bangladesh.ui.theme.BloodRed
import com.bloodnetwork.bangladesh.ui.theme.GrayMid
import com.bloodnetwork.bangladesh.ui.theme.RecentlyDonatedAmber
import com.bloodnetwork.bangladesh.ui.viewmodel.AdminViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

private val bloodGroupPalette = listOf(
    BloodRed, Color(0xFF1565C0), Color(0xFF2E7D32), Color(0xFFEF6C00),
    Color(0xFF7B1FA2), Color(0xFF00838F), Color(0xFFAD1457), Color(0xFF5D4037),
)

private val dayFormatter = DateTimeFormatter.ofPattern("MMM d")

private fun shortDate(iso: String): String = try {
    LocalDate.parse(iso.take(10)).format(dayFormatter)
} catch (e: DateTimeParseException) {
    iso.take(10)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminAnalyticsScreen(onBack: () -> Unit) {
    val factory = LocalVmFactory.current!!
    val vm: AdminViewModel = viewModel(factory = factory)
    val state by vm.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) { vm.loadAnalytics() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Analytics", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { padding ->
        PullToRefreshBox(
            isRefreshing = state.isAnalyticsLoading && state.analytics != null,
            onRefresh = { vm.loadAnalytics() },
            modifier = Modifier.fillMaxSize().padding(padding),
        ) {
            val analytics = state.analytics
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                when {
                    analytics == null && state.isAnalyticsLoading -> {
                        repeat(4) { SectionCard { SkeletonCard() } }
                    }
                    analytics == null && state.analyticsError != null -> {
                        SectionCard {
                            Text("Failed to load analytics", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.SemiBold)
                            Text(state.analyticsError ?: "", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    analytics != null -> AnalyticsContent(analytics)
                }
            }
        }
    }
}

@Composable
private fun AnalyticsContent(analytics: AdminAnalyticsDto) {
    val totalDonorsTracked = analytics.bloodTypeDistribution.sumOf { it.count }

    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
        MetricTile("Fulfillment rate", "${analytics.fulfillmentRatePercent}%", AvailableGreen, Modifier.weight(1f))
        MetricTile(
            "Avg donor response",
            analytics.averageDonorResponseHours?.let { "${it}h" } ?: "—",
            Color(0xFF1565C0),
            Modifier.weight(1f),
        )
    }
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
        MetricTile("Donors tracked", "$totalDonorsTracked", BloodRed, Modifier.weight(1f))
        MetricTile(
            "Requests (30d)",
            "${analytics.requestsOverTime.sumOf { it.count }}",
            RecentlyDonatedAmber,
            Modifier.weight(1f),
        )
    }

    SectionCard(title = "Blood type distribution") {
        BarListChart(
            items = analytics.bloodTypeDistribution.mapIndexed { i, x ->
                BarItem(x.bloodGroup.label, x.count, bloodGroupPalette[i % bloodGroupPalette.size])
            },
        )
    }

    SectionCard(title = "Requests — last 30 days") {
        val points = analytics.requestsOverTime.map { it.count }
        TrendLineChart(
            points = points,
            color = BloodRed,
            startLabel = analytics.requestsOverTime.firstOrNull()?.date?.let { shortDate(it) } ?: "",
            endLabel = analytics.requestsOverTime.lastOrNull()?.date?.let { shortDate(it) } ?: "",
        )
    }

    SectionCard(title = "New donor sign-ups — last 30 days") {
        val points = analytics.newDonorsOverTime.map { it.count }
        TrendLineChart(
            points = points,
            color = Color(0xFF2E7D32),
            startLabel = analytics.newDonorsOverTime.firstOrNull()?.date?.let { shortDate(it) } ?: "",
            endLabel = analytics.newDonorsOverTime.lastOrNull()?.date?.let { shortDate(it) } ?: "",
        )
    }

    SectionCard(title = "Request status breakdown") {
        BarListChart(items = analytics.requestStatusBreakdown.map { BarItem(it.status, it.count, statusColor(it.status)) })
    }

    SectionCard(title = "Urgency breakdown") {
        BarListChart(items = analytics.urgencyBreakdown.map { BarItem(it.status, it.count, urgencyColor(it.status)) })
    }

    SectionCard(title = "Donor verification breakdown") {
        BarListChart(items = analytics.donorVerificationBreakdown.map { BarItem(it.status, it.count, verificationColor(it.status)) })
    }

    SectionCard(title = "Top districts by open requests") {
        BarListChart(items = analytics.requestsByDistrict.map { BarItem(it.districtName, it.count, BloodRed) })
    }

    SectionCard(title = "Top districts by registered donors") {
        BarListChart(items = analytics.donorsByDistrict.map { BarItem(it.districtName, it.count, Color(0xFF1565C0)) })
    }
}

@Composable
private fun SectionCard(title: String? = null, content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            title?.let { Text(it, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold) }
            content()
        }
    }
}

private fun statusColor(status: String): Color = when (status) {
    "Open" -> Color(0xFFC62828)
    "PartiallyFulfilled" -> RecentlyDonatedAmber
    "Fulfilled" -> AvailableGreen
    "Cancelled" -> GrayMid
    "Expired" -> Color(0xFF616161)
    else -> BloodRed
}

private fun urgencyColor(urgency: String): Color = when (urgency) {
    "Critical" -> Color(0xFFC62828)
    "Urgent" -> RecentlyDonatedAmber
    "Normal" -> AvailableGreen
    else -> BloodRed
}

private fun verificationColor(status: String): Color = when (status) {
    "Verified" -> AvailableGreen
    "Pending" -> RecentlyDonatedAmber
    "Rejected" -> Color(0xFFC62828)
    else -> GrayMid
}
