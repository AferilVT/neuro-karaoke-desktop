package com.soul.neurokaraoke.ui.screens.search

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.soul.neurokaraoke.data.model.Singer
import com.soul.neurokaraoke.data.model.Song
import com.soul.neurokaraoke.ui.components.Pagination
import com.soul.neurokaraoke.ui.components.SearchBar
import com.soul.neurokaraoke.ui.components.SongListItem

private const val PAGE_SIZE = 20

enum class SortOption(val label: String) {
    TITLE_ASC("Title A-Z"),
    TITLE_DESC("Title Z-A"),
    ARTIST_ASC("Artist A-Z"),
    ARTIST_DESC("Artist Z-A")
}

@Composable
fun SearchScreen(
    songs: List<Song> = emptyList(),
    isLoading: Boolean = false,
    onSongClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var showFilters by remember { mutableStateOf(false) }
    var selectedSinger by remember { mutableStateOf<Singer?>(null) }
    var sortOption by remember { mutableStateOf(SortOption.TITLE_ASC) }
    var showSortMenu by remember { mutableStateOf(false) }
    var currentPage by remember { mutableIntStateOf(1) }

    // Reset page when filters change
    LaunchedEffect(searchQuery, selectedSinger, sortOption) {
        currentPage = 1
    }

    val activeFilterCount by remember(selectedSinger, sortOption) {
        derivedStateOf {
            var count = 0
            if (selectedSinger != null) count++
            if (sortOption != SortOption.TITLE_ASC) count++
            count
        }
    }

    val filteredSongs by remember(searchQuery, songs, selectedSinger, sortOption) {
        derivedStateOf {
            var result = songs

            // Filter by search query
            if (searchQuery.isNotBlank()) {
                result = result.filter { song ->
                    song.title.contains(searchQuery, ignoreCase = true) ||
                    song.artist.contains(searchQuery, ignoreCase = true)
                }
            }

            // Filter by singer
            if (selectedSinger != null) {
                result = result.filter { it.singer == selectedSinger }
            }

            // Sort
            result = when (sortOption) {
                SortOption.TITLE_ASC -> result.sortedBy { it.title.lowercase() }
                SortOption.TITLE_DESC -> result.sortedByDescending { it.title.lowercase() }
                SortOption.ARTIST_ASC -> result.sortedBy { it.artist.lowercase() }
                SortOption.ARTIST_DESC -> result.sortedByDescending { it.artist.lowercase() }
            }

            result
        }
    }

    val paginatedSongs by remember(filteredSongs, currentPage) {
        derivedStateOf {
            val startIndex = (currentPage - 1) * PAGE_SIZE
            val endIndex = minOf(startIndex + PAGE_SIZE, filteredSongs.size)
            if (startIndex < filteredSongs.size) {
                filteredSongs.subList(startIndex, endIndex)
            } else {
                emptyList()
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        // Search bar
        SearchBar(
            query = searchQuery,
            onQueryChange = { searchQuery = it },
            placeholder = "Search by name or artist"
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Filter button row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = { showFilters = !showFilters }) {
                Icon(
                    imageVector = Icons.Default.FilterList,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = if (activeFilterCount > 0) "Filters ($activeFilterCount)" else "+ Add filters",
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(start = 4.dp)
                )
                Icon(
                    imageVector = if (showFilters) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // Expandable filter section
        AnimatedVisibility(
            visible = showFilters,
            enter = expandVertically(),
            exit = shrinkVertically()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                // Singer filter
                Text(
                    text = "Singer",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SingerFilterChip(
                        label = "All",
                        selected = selectedSinger == null,
                        onClick = { selectedSinger = null }
                    )
                    SingerFilterChip(
                        label = "Neuro",
                        selected = selectedSinger == Singer.NEURO,
                        onClick = { selectedSinger = Singer.NEURO }
                    )
                    SingerFilterChip(
                        label = "Evil",
                        selected = selectedSinger == Singer.EVIL,
                        onClick = { selectedSinger = Singer.EVIL }
                    )
                    SingerFilterChip(
                        label = "Duet",
                        selected = selectedSinger == Singer.DUET,
                        onClick = { selectedSinger = Singer.DUET }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Sort option
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Sort by",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Box {
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .clickable { showSortMenu = true }
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = sortOption.label,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowDown,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        DropdownMenu(
                            expanded = showSortMenu,
                            onDismissRequest = { showSortMenu = false }
                        ) {
                            SortOption.entries.forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(option.label) },
                                    onClick = {
                                        sortOption = option
                                        showSortMenu = false
                                    },
                                    leadingIcon = if (option == sortOption) {
                                        {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    } else null
                                )
                            }
                        }
                    }
                }

                // Clear filters button
                if (activeFilterCount > 0) {
                    Spacer(modifier = Modifier.height(12.dp))
                    TextButton(
                        onClick = {
                            selectedSinger = null
                            sortOption = SortOption.TITLE_ASC
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Clear all filters",
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }

        // Pagination header
        if (!isLoading && filteredSongs.isNotEmpty()) {
            Pagination(
                currentPage = currentPage,
                totalItems = filteredSongs.size,
                pageSize = PAGE_SIZE,
                onPageChange = { currentPage = it }
            )
        }

        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = if (songs.isNotEmpty()) "Loading songs... (${songs.size} found)" else "Loading all setlists...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else if (songs.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No songs available",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else if (filteredSongs.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "No songs match your filters",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    TextButton(
                        onClick = {
                            searchQuery = ""
                            selectedSinger = null
                            sortOption = SortOption.TITLE_ASC
                        }
                    ) {
                        Text("Clear filters")
                    }
                }
            }
        } else {
            // Results list
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                itemsIndexed(paginatedSongs) { index, song ->
                    val globalIndex = (currentPage - 1) * PAGE_SIZE + index
                    SongListItem(
                        song = song,
                        index = globalIndex + 1,
                        onClick = { onSongClick(song.id) }
                    )
                }

                // Bottom spacing for mini player
                item {
                    Spacer(modifier = Modifier.height(100.dp))
                }
            }
        }
    }
}

@Composable
private fun SingerFilterChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label) },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.primary,
            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
        ),
        border = FilterChipDefaults.filterChipBorder(
            borderColor = MaterialTheme.colorScheme.outline,
            selectedBorderColor = MaterialTheme.colorScheme.primary,
            enabled = true,
            selected = selected
        )
    )
}
