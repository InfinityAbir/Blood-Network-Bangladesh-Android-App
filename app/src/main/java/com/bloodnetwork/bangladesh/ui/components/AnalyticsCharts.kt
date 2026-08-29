package com.bloodnetwork.bangladesh.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bloodnetwork.bangladesh.ui.i18n.tr

data class BarItem(val label: String, val value: Int, val color: Color)

/** Horizontal bar list: label left, proportional animated bar, value right. Used for
 * distributions where each category needs its own readable label (blood type, status,
 * district top-N) rather than a dense chart. */
@Composable
fun BarListChart(items: List<BarItem>, modifier: Modifier = Modifier, emptyText: String = tr("No data yet", "এখনো কোনো তথ্য নেই")) {
    if (items.isEmpty()) {
        Text(emptyText, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = modifier)
        return
    }
    val max = (items.maxOfOrNull { it.value } ?: 1).coerceAtLeast(1)
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        items.forEach { item ->
            val fraction by animateFloatAsState(
                targetValue = item.value.toFloat() / max.toFloat(),
                animationSpec = tween(600),
                label = "bar",
            )
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    item.label,
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.width(84.dp),
                    maxLines = 1,
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(14.dp)
                        .clip(RoundedCornerShape(7.dp))
                        .background(item.color.copy(alpha = 0.12f)),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(fraction.coerceIn(0f, 1f))
                            .height(14.dp)
                            .clip(RoundedCornerShape(7.dp))
                            .background(item.color),
                    )
                }
                Text(
                    "${item.value}",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.width(36.dp),
                )
            }
        }
    }
}

/** Area/line trend chart for a daily time series (e.g. last 30 days). Draws a smoothed
 * line with a gradient fill under it, plus start/end date labels and the peak value. */
@Composable
fun TrendLineChart(
    points: List<Int>,
    color: Color,
    modifier: Modifier = Modifier,
    startLabel: String = "",
    endLabel: String = "",
) {
    if (points.size < 2) {
        Text(tr("Not enough data yet", "এখনো পর্যাপ্ত তথ্য নেই"), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = modifier)
        return
    }
    var animatedProgress by remember { mutableStateOf(0f) }
    val progress by animateFloatAsState(targetValue = 1f, animationSpec = tween(700), label = "trend")
    animatedProgress = progress

    val max = (points.maxOrNull() ?: 1).coerceAtLeast(1)
    Column(modifier = modifier) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(110.dp),
        ) {
            val stepX = size.width / (points.size - 1)
            val visibleCount = (points.size * animatedProgress).coerceAtLeast(1f).toInt().coerceAtMost(points.size)
            if (visibleCount < 2) return@Canvas

            val linePath = Path()
            val fillPath = Path()
            for (i in 0 until visibleCount) {
                val x = stepX * i
                val y = size.height - (points[i].toFloat() / max.toFloat()) * size.height
                if (i == 0) {
                    linePath.moveTo(x, y)
                    fillPath.moveTo(x, size.height)
                    fillPath.lineTo(x, y)
                } else {
                    linePath.lineTo(x, y)
                    fillPath.lineTo(x, y)
                }
            }
            val lastX = stepX * (visibleCount - 1)
            fillPath.lineTo(lastX, size.height)
            fillPath.close()

            drawPath(
                path = fillPath,
                brush = Brush.verticalGradient(listOf(color.copy(alpha = 0.28f), color.copy(alpha = 0f))),
            )
            drawPath(
                path = linePath,
                color = color,
                style = Stroke(width = 6f, cap = StrokeCap.Round),
            )

            // Endpoint dot highlights the latest value.
            val endY = size.height - (points[visibleCount - 1].toFloat() / max.toFloat()) * size.height
            drawCircle(color = color, radius = 8f, center = Offset(lastX, endY))
            drawCircle(color = Color.White, radius = 3.5f, center = Offset(lastX, endY))
        }
        Spacer(Modifier.height(6.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(startLabel, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(tr("peak $max", "সর্বোচ্চ $max"), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(endLabel, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun MetricTile(label: String, value: String, accent: Color, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = accent, maxLines = 1)
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2)
    }
}
