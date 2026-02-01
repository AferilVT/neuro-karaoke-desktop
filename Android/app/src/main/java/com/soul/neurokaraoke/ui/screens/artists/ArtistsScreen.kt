package com.soul.neurokaraoke.ui.screens.artists

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
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.soul.neurokaraoke.data.model.Artist
import com.soul.neurokaraoke.data.model.Song
import com.soul.neurokaraoke.data.repository.ArtistImageRepository
import com.soul.neurokaraoke.ui.components.ArtistCard
import com.soul.neurokaraoke.ui.components.SearchBar

@Composable
fun ArtistsScreen(
    songs: List<Song> = emptyList(),
    isLoading: Boolean = false,
    onArtistClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }

    // Derive artists from songs by grouping by original artist name
    val artists by remember(songs) {
        derivedStateOf {
            songs.groupBy { it.artist }
                .map { (artistName, artistSongs) ->
                    val fallbackImage = artistSongs.firstOrNull()?.coverUrl ?: ""
                    Artist(
                        id = artistName.lowercase().replace(" ", "-"),
                        name = artistName,
                        imageUrl = ArtistImageRepository.getArtistImageOrDefault(artistName, fallbackImage),
                        songCount = artistSongs.size,
                        songs = artistSongs
                    )
                }
                .sortedByDescending { it.songCount }
        }
    }

    val filteredArtists by remember(searchQuery, artists) {
        derivedStateOf {
            if (searchQuery.isBlank()) {
                artists
            } else {
                artists.filter { artist ->
                    artist.name.contains(searchQuery, ignoreCase = true)
                }
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
            text = "Artists",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Browse songs by original artists covered by Neuro and Evil",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        SearchBar(
            query = searchQuery,
            onQueryChange = { searchQuery = it },
            placeholder = "Search artists"
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "${filteredArtists.size} artists",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (isLoading && songs.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Loading artists...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else if (artists.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No artists found",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 100.dp)
            ) {
                items(filteredArtists, key = { it.id }) { artist ->
                    ArtistCard(
                        artist = artist,
                        onClick = { onArtistClick(artist.name) }
                    )
                }
            }
        }
    }
}
