package com.bloodnetwork.bangladesh.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.bloodnetwork.bangladesh.ui.i18n.tr
import com.bloodnetwork.bangladesh.ui.theme.BloodRed

/** Circular avatar showing [photoUrl] when set. Falls back to donor initials
 * when there's no URL, while it's loading, and if the load fails — never a broken-image glyph. */
@Composable
fun Avatar(photoUrl: String?, donorName: String? = null, modifier: Modifier = Modifier, size: Dp = 64.dp) {
    val fallback = rememberVectorPainter(Icons.Filled.Person)
    val initials = donorName?.take(1)?.uppercase() ?: ""
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(BloodRed.copy(alpha = 0.12f)),
        contentAlignment = Alignment.Center,
    ) {
        if (!photoUrl.isNullOrBlank()) {
            AsyncImage(
                model = photoUrl,
                contentDescription = tr("Profile photo", "প্রোফাইল ছবি"),
                modifier = Modifier.size(size).clip(CircleShape),
                contentScale = ContentScale.Crop,
                placeholder = fallback,
                error = fallback,
                colorFilter = null,
            )
        } else if (donorName?.isNotEmpty() == true) {
            Text(
                text = initials,
                style = MaterialTheme.typography.titleLarge,
                color = BloodRed,
            )
        } else {
            Icon(
                Icons.Filled.Person,
                contentDescription = null,
                tint = BloodRed,
                modifier = Modifier.size(size * 0.5f),
            )
        }
    }
}
