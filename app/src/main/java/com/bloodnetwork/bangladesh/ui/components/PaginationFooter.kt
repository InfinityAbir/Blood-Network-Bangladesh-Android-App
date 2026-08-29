package com.bloodnetwork.bangladesh.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.bloodnetwork.bangladesh.ui.i18n.tr

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

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = tr(
                "Showing $start to $end of $totalCount $label",
                "$totalCount-টি $label এর মধ্যে $start–$end দেখানো হচ্ছে",
            ),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(
                onClick = { onPageChange(1) },
                enabled = page > 1,
                modifier = Modifier.padding(0.dp)
            ) {
                Text("<<", style = MaterialTheme.typography.labelMedium)
            }
            IconButton(
                onClick = { onPageChange(page - 1) },
                enabled = page > 1
            ) {
                Icon(Icons.Filled.ChevronLeft, contentDescription = tr("Previous page", "আগের পাতা"))
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "$page",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            IconButton(
                onClick = { onPageChange(page + 1) },
                enabled = page < totalPages
            ) {
                Icon(Icons.Filled.ChevronRight, contentDescription = tr("Next page", "পরের পাতা"))
            }
            TextButton(
                onClick = { onPageChange(totalPages) },
                enabled = page < totalPages
            ) {
                Text(">>", style = MaterialTheme.typography.labelMedium)
            }
        }
        PageSizeDropdown(
            pageSize = pageSize,
            onPageSizeChange = onPageSizeChange
        )
    }
}

@Composable
private fun PageSizeDropdown(
    pageSize: Int,
    onPageSizeChange: (Int) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        OutlinedButton(
            onClick = { expanded = true },
            shape = RoundedCornerShape(8.dp)
        ) {
            Text("$pageSize")
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            listOf(10, 20, 50, 100).forEach { size ->
                DropdownMenuItem(
                    text = { Text("$size") },
                    onClick = {
                        expanded = false
                        if (size != pageSize) onPageSizeChange(size)
                    }
                )
            }
        }
    }
}
