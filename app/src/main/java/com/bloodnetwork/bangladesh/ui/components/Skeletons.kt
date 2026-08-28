package com.bloodnetwork.bangladesh.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun shimmerBrush(): Brush {
    val shimmerColors = listOf(
        Color.LightGray.copy(alpha = 0.6f),
        Color.LightGray.copy(alpha = 0.2f),
        Color.LightGray.copy(alpha = 0.6f),
    )
    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "shimmer_translate",
    )
    return Brush.linearGradient(
        colors = shimmerColors,
        start = Offset(translateAnim - 200f, translateAnim - 200f),
        end = Offset(translateAnim, translateAnim),
    )
}

@Composable
fun SkeletonLine(width: String = "100%", height: Int = 16) {
    val modifier = when (width) {
        "100%" -> Modifier.fillMaxWidth()
        "75%" -> Modifier.fillMaxWidth(0.75f)
        "50%" -> Modifier.fillMaxWidth(0.5f)
        "25%" -> Modifier.fillMaxWidth(0.25f)
        else -> Modifier.width(100.dp)
    }
    Box(
        modifier = modifier
            .height(height.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(shimmerBrush()),
    )
}

@Composable
fun SkeletonCard() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            SkeletonLine("40%", 20)
            SkeletonLine("15%", 20)
        }
        SkeletonLine("60%", 14)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SkeletonLine("25%", 28)
            SkeletonLine("20%", 14)
        }
    }
}

@Composable
fun DonorCardSkeleton() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            SkeletonLine("35%", 20)
            SkeletonLine("12%", 20)
        }
        SkeletonLine("50%", 14)
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth(),
        ) {
            SkeletonLine("20%", 28)
            SkeletonLine("30%", 36)
        }
    }
}

@Composable
fun FormFieldSkeleton() {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SkeletonLine("25%", 14)
        SkeletonLine("100%", 48)
    }
}

@Composable
fun DonorProfileSkeleton() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        FormFieldSkeleton()
        FormFieldSkeleton()
        FormFieldSkeleton()
        FormFieldSkeleton()
        FormFieldSkeleton()
        FormFieldSkeleton()
        SkeletonLine("100%", 50)
    }
}

@Composable
fun RequestBloodSkeleton() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        SkeletonLine("30%", 18)
        SkeletonLine("100%", 40)
        FormFieldSkeleton()
        FormFieldSkeleton()
        FormFieldSkeleton()
        FormFieldSkeleton()
        FormFieldSkeleton()
        SkeletonLine("100%", 50)
    }
}

@Composable
fun SearchResultsSkeleton() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        repeat(3) {
            DonorCardSkeleton()
        }
    }
}
