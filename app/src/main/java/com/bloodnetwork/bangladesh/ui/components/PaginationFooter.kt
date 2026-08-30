package com.bloodnetwork.bangladesh.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bloodnetwork.bangladesh.ui.i18n.tr
import com.bloodnetwork.bangladesh.ui.theme.BloodRed

/**
 * Mobile-first pagination control, two rows instead of one crowded line:
 * range + page-size on top, large circular Prev/Next either side of "Page X of Y" below.
 * No jump-to-first/last — these admin lists are small enough that stepping is enough,
 * and the extra buttons only added clutter on narrow screens.
 */
@Composable
fun PaginationFooter(
    page: Int,
    pageSize: Int,
    totalCount: Int,
    label: String = tr("items", "আইটেম"),
    onPageChange: (Int) -> Unit,
    onPageSizeChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val totalPages = if (totalCount <= 0) 1 else (totalCount + pageSize - 1) / pageSize
    val start = if (totalCount == 0) 0 else (page - 1) * pageSize + 1
    val end = minOf(page * pageSize, totalCount)

    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = tr(
                    "$start–$end of $totalCount $label",
                    "$totalCount-টি $label এর মধ্যে $start–$end",
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            PageSizeDropdown(pageSize = pageSize, onPageSizeChange = onPageSizeChange)
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            FilledTonalIconButton(
                onClick = { onPageChange(page - 1) },
                enabled = page > 1,
                modifier = Modifier.size(40.dp),
                colors = IconButtonDefaults.filledTonalIconButtonColors(containerColor = BloodRed.copy(alpha = 0.12f), contentColor = BloodRed),
            ) {
                Icon(Icons.Filled.ChevronLeft, contentDescription = tr("Previous page", "আগের পাতা"))
            }
            Text(
                text = tr("Page $page of $totalPages", "$totalPages এর $page পাতা"),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(horizontal = 16.dp),
            )
            FilledTonalIconButton(
                onClick = { onPageChange(page + 1) },
                enabled = page < totalPages,
                modifier = Modifier.size(40.dp),
                colors = IconButtonDefaults.filledTonalIconButtonColors(containerColor = BloodRed.copy(alpha = 0.12f), contentColor = BloodRed),
            ) {
                Icon(Icons.Filled.ChevronRight, contentDescription = tr("Next page", "পরের পাতা"))
            }
        }
    }
}

@Composable
private fun PageSizeDropdown(pageSize: Int, onPageSizeChange: (Int) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(50))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .clickable { expanded = true }
                .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("$pageSize " + tr("per page", "প্রতি পাতায়"), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold)
            Icon(Icons.Filled.ExpandMore, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            listOf(10, 20, 50, 100).forEach { size ->
                DropdownMenuItem(
                    text = { Text("$size " + tr("per page", "প্রতি পাতায়")) },
                    onClick = {
                        expanded = false
                        if (size != pageSize) onPageSizeChange(size)
                    },
                )
            }
        }
    }
}
