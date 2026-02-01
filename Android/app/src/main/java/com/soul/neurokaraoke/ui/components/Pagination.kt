package com.soul.neurokaraoke.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlin.math.ceil
import kotlin.math.min

@Composable
fun Pagination(
    currentPage: Int,
    totalItems: Int,
    pageSize: Int,
    onPageChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val totalPages = ceil(totalItems.toDouble() / pageSize).toInt().coerceAtLeast(1)
    val startItem = (currentPage - 1) * pageSize + 1
    val endItem = min(currentPage * pageSize, totalItems)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        // Showing X - Y of Z
        Text(
            text = "Showing $startItem - $endItem of $totalItems",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Page controls - scrollable row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // First page (<<)
            IconButton(
                onClick = { onPageChange(1) },
                enabled = currentPage > 1,
                modifier = Modifier.size(32.dp)
            ) {
                Text(
                    text = "«",
                    style = MaterialTheme.typography.titleMedium,
                    color = if (currentPage > 1) MaterialTheme.colorScheme.onSurface
                           else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                )
            }

            // Previous page (<)
            IconButton(
                onClick = { onPageChange(currentPage - 1) },
                enabled = currentPage > 1,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                    contentDescription = "Previous",
                    tint = if (currentPage > 1) MaterialTheme.colorScheme.onSurface
                          else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                )
            }

            // Page numbers
            PageNumbers(
                currentPage = currentPage,
                totalPages = totalPages,
                onPageChange = onPageChange
            )

            // Next page (>)
            IconButton(
                onClick = { onPageChange(currentPage + 1) },
                enabled = currentPage < totalPages,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = "Next",
                    tint = if (currentPage < totalPages) MaterialTheme.colorScheme.onSurface
                          else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                )
            }

            // Last page (>>)
            IconButton(
                onClick = { onPageChange(totalPages) },
                enabled = currentPage < totalPages,
                modifier = Modifier.size(32.dp)
            ) {
                Text(
                    text = "»",
                    style = MaterialTheme.typography.titleMedium,
                    color = if (currentPage < totalPages) MaterialTheme.colorScheme.onSurface
                           else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                )
            }
        }
    }
}

@Composable
private fun PageNumbers(
    currentPage: Int,
    totalPages: Int,
    onPageChange: (Int) -> Unit
) {
    val pages = buildPageList(currentPage, totalPages)

    Row(
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        pages.forEach { page ->
            when (page) {
                -1 -> {
                    // Ellipsis
                    Text(
                        text = "...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                }
                else -> {
                    PageButton(
                        page = page,
                        isSelected = page == currentPage,
                        onClick = { onPageChange(page) }
                    )
                }
            }
        }
    }
}

@Composable
private fun PageButton(
    page: Int,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .height(32.dp)
            .widthIn(min = 32.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(
                if (isSelected) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.surface
            )
            .clickable(enabled = !isSelected, onClick = onClick)
            .padding(horizontal = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = page.toString(),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                   else MaterialTheme.colorScheme.onSurface,
            maxLines = 1
        )
    }
}

private fun buildPageList(currentPage: Int, totalPages: Int): List<Int> {
    if (totalPages <= 7) {
        return (1..totalPages).toList()
    }

    val pages = mutableListOf<Int>()

    // Always show first page
    pages.add(1)

    if (currentPage <= 3) {
        // Near the start: 1 2 3 4 ... lastPage
        pages.addAll(listOf(2, 3, 4))
        pages.add(-1) // ellipsis
        pages.add(totalPages)
    } else if (currentPage >= totalPages - 2) {
        // Near the end: 1 ... last-3 last-2 last-1 lastPage
        pages.add(-1) // ellipsis
        pages.addAll(listOf(totalPages - 3, totalPages - 2, totalPages - 1, totalPages))
    } else {
        // Middle: 1 ... current-1 current current+1 ... lastPage
        pages.add(-1) // ellipsis
        pages.addAll(listOf(currentPage - 1, currentPage, currentPage + 1))
        pages.add(-1) // ellipsis
        pages.add(totalPages)
    }

    return pages
}
