package com.soul.neurokaraoke.ui.screens.explore

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.soul.neurokaraoke.data.api.ApiPublicPlaylist
import com.soul.neurokaraoke.data.api.NeuroKaraokeApi
import com.soul.neurokaraoke.data.model.Playlist
import com.soul.neurokaraoke.ui.components.Pagination
import com.soul.neurokaraoke.ui.components.PlaylistCard
import com.soul.neurokaraoke.ui.components.SearchBar

private const val PAGE_SIZE = 20

@Composable
fun ExploreScreen(
    onPlaylistClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var playlists by remember { mutableStateOf<List<Playlist>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var currentPage by remember { mutableIntStateOf(1) }

    val api = remember { NeuroKaraokeApi() }

    // Reset page when search changes
    LaunchedEffect(searchQuery) {
        currentPage = 1
    }

    // Fetch public playlists on first load
    LaunchedEffect(Unit) {
        isLoading = true
        error = null
        api.fetchPublicPlaylists().fold(
            onSuccess = { apiPlaylists ->
                playlists = apiPlaylists.map { it.toPlaylist() }
                isLoading = false
            },
            onFailure = { e ->
                error = e.message ?: "Failed to load playlists"
                isLoading = false
            }
        )
    }

    val filteredPlaylists by remember(searchQuery, playlists) {
        derivedStateOf {
            if (searchQuery.isBlank()) {
                playlists
            } else {
                playlists.filter { playlist ->
                    playlist.title.contains(searchQuery, ignoreCase = true) ||
                    playlist.description.contains(searchQuery, ignoreCase = true)
                }
            }
        }
    }

    val paginatedPlaylists by remember(filteredPlaylists, currentPage) {
        derivedStateOf {
            val startIndex = (currentPage - 1) * PAGE_SIZE
            val endIndex = minOf(startIndex + PAGE_SIZE, filteredPlaylists.size)
            if (startIndex < filteredPlaylists.size) {
                filteredPlaylists.subList(startIndex, endIndex)
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

        Text(
            text = "Explore Playlists",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Public playlists created by our community",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        SearchBar(
            query = searchQuery,
            onQueryChange = { searchQuery = it },
            placeholder = "Search playlists"
        )

        // Pagination
        if (!isLoading && filteredPlaylists.isNotEmpty()) {
            Pagination(
                currentPage = currentPage,
                totalItems = filteredPlaylists.size,
                pageSize = PAGE_SIZE,
                onPageChange = { currentPage = it }
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        when {
            isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Loading playlists...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            error != null -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = error ?: "Unknown error",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
            filteredPlaylists.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (searchQuery.isNotBlank()) "No playlists match your search" else "No playlists available",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            else -> {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 100.dp)
                ) {
                    items(paginatedPlaylists) { playlist ->
                        PlaylistCard(
                            playlist = playlist,
                            onClick = { onPlaylistClick(playlist.id) }
                        )
                    }
                }
            }
        }
    }
}

private fun ApiPublicPlaylist.toPlaylist(): Playlist {
    // Use mosaic covers for preview, or cover URL
    val previewCovers = mosaicCovers.ifEmpty {
        coverUrl?.let { listOf(it) } ?: emptyList()
    }

    return Playlist(
        id = id,
        title = name,
        description = createdBy?.let { "by $it" } ?: "",
        coverUrl = coverUrl ?: previewCovers.firstOrNull() ?: "",
        previewCovers = previewCovers,
        songCount = songCount
    )
}
