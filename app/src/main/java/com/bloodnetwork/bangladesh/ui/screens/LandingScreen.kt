package com.bloodnetwork.bangladesh.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.background
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bloodtype
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bloodnetwork.bangladesh.ui.components.PrimaryButton
import com.bloodnetwork.bangladesh.ui.navigation.Routes
import com.bloodnetwork.bangladesh.ui.theme.BloodRed

@Composable
fun LandingScreen(onNavigate: (String) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = Icons.Filled.Bloodtype,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier
                .size(96.dp)
                .background(BloodRed, CircleShape)
                .padding(20.dp),
        )
        Spacer(Modifier.height(24.dp))
        Text(
            text = "Blood Network BD",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = "Blood Network Bangladesh",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.secondary,
        )
        Spacer(Modifier.height(40.dp))

        PrimaryButton("Find Blood", onClick = { onNavigate(Routes.FIND_BLOOD) })
        Spacer(Modifier.height(12.dp))
        PrimaryButton("Donate Blood", onClick = { onNavigate(Routes.ELIGIBILITY) })
        Spacer(Modifier.height(12.dp))
        PrimaryButton("Login", onClick = { onNavigate(Routes.LOGIN) })
        Spacer(Modifier.height(12.dp))
        PrimaryButton("Create Account", onClick = { onNavigate(Routes.REGISTER) })
    }
}
