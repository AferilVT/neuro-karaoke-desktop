package com.soul.neurokaraoke.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.Search
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(
    val route: String,
    val title: String,
    val icon: ImageVector? = null
) {
    // Main navigation screens
    data object Home : Screen("home", "Home", Icons.Default.Home)
    data object Search : Screen("search", "Search", Icons.Default.Search)
    data object Explore : Screen("explore", "Explore", Icons.Default.Explore)
    data object Artists : Screen("artists", "Artists", Icons.Default.Person)
    data object Setlists : Screen("setlists", "Karaoke Setlist", Icons.Default.QueueMusic)
    data object About : Screen("about", "About", Icons.Default.Info)

    // Library screens
    data object Favorites : Screen("favorites", "Favorites", Icons.Default.Favorite)
    data object Playlists : Screen("playlists", "Your Playlists", Icons.Default.LibraryMusic)

    // Detail screens
    data object PlaylistDetail : Screen("playlist/{playlistId}", "Playlist") {
        fun createRoute(playlistId: String) = "playlist/$playlistId"
    }
    data object ArtistDetail : Screen("artist/{artistId}", "Artist") {
        fun createRoute(artistId: String) = "artist/$artistId"
    }
    data object SetlistDetail : Screen("setlist/{setlistId}", "Setlist") {
        fun createRoute(setlistId: String) = "setlist/$setlistId"
    }

    // Player screen
    data object Player : Screen("player", "Now Playing")

    companion object {
        val mainNavItems = listOf(Home, Search, Explore, Artists, Setlists, About)
        val libraryItems = listOf(Favorites, Playlists)
    }
}
