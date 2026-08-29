package com.bloodnetwork.bangladesh.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bloodtype
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.bloodnetwork.bangladesh.ui.components.AnimatedSlideIn
import com.bloodnetwork.bangladesh.ui.components.PrimaryButton
import com.bloodnetwork.bangladesh.ui.components.SecondaryButton
import com.bloodnetwork.bangladesh.ui.navigation.Routes

@Composable
fun LandingScreen(onNavigate: (String) -> Unit) {
    var iconVisible by remember { mutableStateOf(false) }
    var contentVisible by remember { mutableStateOf(false) }

    val iconScale by animateFloatAsState(
        targetValue = if (iconVisible) 1f else 0f,
        animationSpec = tween(durationMillis = 500),
        label = "iconScale",
    )
    val iconAlpha by animateFloatAsState(
        targetValue = if (iconVisible) 1f else 0f,
        animationSpec = tween(durationMillis = 400),
        label = "iconAlpha",
    )

    LaunchedEffect(Unit) {
        iconVisible = true
        contentVisible = true
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(start = 24.dp, end = 24.dp, top = 24.dp, bottom = 88.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
        Icon(
            imageVector = Icons.Filled.Bloodtype,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier
                .size(96.dp)
                .scale(iconScale)
                .alpha(iconAlpha)
                .background(MaterialTheme.colorScheme.primary, CircleShape)
                .padding(20.dp),
        )
        Spacer(Modifier.height(24.dp))
        AnimatedSlideIn(visible = contentVisible, delay = 200) {
            Text(
                text = "Blood Network BD",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
            )
        }
        Spacer(Modifier.height(12.dp))
        AnimatedSlideIn(visible = contentVisible, delay = 400) {
            Text(
                text = "Connecting willing donors with people who need blood, fast.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        }
        Spacer(Modifier.height(40.dp))

        AnimatedSlideIn(visible = contentVisible, delay = 600) {
            PrimaryButton("Find Blood", onClick = { onNavigate(Routes.FIND_BLOOD) })
        }
        Spacer(Modifier.height(12.dp))
        AnimatedSlideIn(visible = contentVisible, delay = 700) {
            PrimaryButton("Donate Blood", onClick = { onNavigate(Routes.ELIGIBILITY) })
        }

        Spacer(Modifier.height(24.dp))

        AnimatedSlideIn(visible = contentVisible, delay = 800) {
            SecondaryButton("Login", onClick = { onNavigate(Routes.LOGIN) })
        }
        Spacer(Modifier.height(12.dp))
        AnimatedSlideIn(visible = contentVisible, delay = 900) {
            SecondaryButton("Create Account", onClick = { onNavigate(Routes.REGISTER) })
        }
        }
        com.bloodnetwork.bangladesh.ui.components.ThemeToggleButton(
            modifier = Modifier.align(Alignment.TopEnd).padding(top = 12.dp, end = 12.dp),
        )
    }
}
