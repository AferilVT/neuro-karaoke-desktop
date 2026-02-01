package com.soul.neurokaraoke.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.soul.neurokaraoke.data.model.Song
import com.soul.neurokaraoke.ui.components.CyanBorderCard
import com.soul.neurokaraoke.ui.components.SimpleSongListItem
import com.soul.neurokaraoke.ui.components.SongCard
import com.soul.neurokaraoke.ui.theme.DuetColor
import com.soul.neurokaraoke.ui.theme.EvilColor
import com.soul.neurokaraoke.ui.theme.NeuroColor
import com.soul.neurokaraoke.ui.theme.OtherColor
import com.soul.neurokaraoke.ui.theme.Surface

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    songs: List<Song> = emptyList(),
    playlistName: String? = null,
    isLoading: Boolean = false,
    onSongClick: (String) -> Unit,
    onSeeAllClick: () -> Unit
) {
    // Derive sections from songs - use remember to avoid reshuffling on every recomposition
    val recentSongs = songs.take(5)
    val trendingSongs = remember(songs) { songs.shuffled().take(6) }
    val madeForYouSongs = remember(songs) { songs.shuffled().take(4) }

    if (isLoading && songs.isEmpty()) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        }
        return
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(bottom = 80.dp),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Recent Songs Section
        item {
            SectionHeader(
                title = if (playlistName != null) "Songs" else "Recent Songs",
                onSeeAllClick = onSeeAllClick
            )
        }

        item {
            if (recentSongs.isNotEmpty()) {
                CyanBorderCard {
                    Column {
                        recentSongs.forEachIndexed { index, song ->
                            SimpleSongListItem(
                                song = song,
                                index = index + 1,
                                onClick = { onSongClick(song.id) }
                            )
                        }
                    }
                }
            } else {
                Text(
                    text = "No songs loaded yet",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Made for You Section
        if (madeForYouSongs.isNotEmpty()) {
            item {
                SectionHeader(
                    title = "Made for You",
                    onSeeAllClick = onSeeAllClick
                )
            }

            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(madeForYouSongs) { song ->
                        SongCard(
                            song = song,
                            onClick = { onSongClick(song.id) },
                            modifier = Modifier.width(160.dp)
                        )
                    }
                }
            }
        }

        // Trending This Week Section
        if (trendingSongs.isNotEmpty()) {
            item {
                SectionHeader(
                    title = "Trending This Week",
                    onSeeAllClick = onSeeAllClick
                )
            }

            item {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.height(380.dp)
                ) {
                    items(trendingSongs.take(4)) { song ->
                        SongCard(
                            song = song,
                            onClick = { onSongClick(song.id) }
                        )
                    }
                }
            }
        }

        // Cover Distribution Section
        item {
            CoverDistributionCard()
        }

        // Top Genres Section
        item {
            TopGenresCard()
        }

        // Bottom spacing for mini player
        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

/**
 * Cover Distribution Card - shows breakdown of songs by singer
 * Hardcoded stats for now until database access is available
 */
@Composable
private fun CoverDistributionCard() {
    // Hardcoded stats from the website - will be replaced with actual data later
    val totalSongs = 1267
    val stats = listOf(
        CoverStat("Neuro V3", 516, NeuroColor),
        CoverStat("Evil", 424, EvilColor),
        CoverStat("Duet", 174, DuetColor, isGradient = true),
        CoverStat("Other", 153, OtherColor)
    )

    CyanBorderCard {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header with title and total
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Cover Distribution",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = totalSongs.toString(),
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Distribution rows
            stats.forEach { stat ->
                CoverDistributionRow(
                    label = stat.label,
                    count = stat.count,
                    total = totalSongs,
                    color = stat.color,
                    isGradient = stat.isGradient
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

private data class CoverStat(
    val label: String,
    val count: Int,
    val color: Color,
    val isGradient: Boolean = false
)

@Composable
private fun CoverDistributionRow(
    label: String,
    count: Int,
    total: Int,
    color: Color,
    isGradient: Boolean = false
) {
    val progress = count.toFloat() / total.coerceAtLeast(1)

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Colored dot
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(color)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyMedium,
                    color = color
                )
            }
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Progress bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            if (isGradient) {
                // Gradient for Duet
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress)
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(EvilColor, NeuroColor)
                            )
                        )
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress)
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(color)
                )
            }
        }
    }
}

/**
 * Top Genres Card - shows genre breakdown
 * Hardcoded stats for now until database access is available
 */
@Composable
private fun TopGenresCard() {
    // Hardcoded stats from the website
    val genres = listOf(
        "Electronic" to 402,
        "J-Pop" to 363,
        "Alternative Rock" to 278,
        "Vocaloid" to 264,
        "Pop" to 262,
        "Rock" to 184,
        "Anime" to 149,
        "Pop Rock" to 139
    )

    CyanBorderCard {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Top Genres",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(12.dp))

            genres.forEach { (genre, count) ->
                GenreRow(genre = genre, count = count)
                Spacer(modifier = Modifier.height(4.dp))
            }
        }
    }
}

@Composable
private fun GenreRow(
    genre: String,
    count: Int
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(4.dp))
            .background(Surface)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = genre,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun SectionHeader(
    title: String,
    onSeeAllClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        if (onSeeAllClick != null) {
            TextButton(onClick = onSeeAllClick) {
                Text(
                    text = "See All",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
