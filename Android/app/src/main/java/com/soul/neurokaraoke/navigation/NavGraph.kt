package com.soul.neurokaraoke.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.soul.neurokaraoke.data.model.Playlist
import com.soul.neurokaraoke.data.model.Song
import com.soul.neurokaraoke.ui.screens.about.AboutScreen
import com.soul.neurokaraoke.ui.screens.artists.ArtistDetailScreen
import com.soul.neurokaraoke.ui.screens.artists.ArtistsScreen
import com.soul.neurokaraoke.ui.screens.explore.ExploreScreen
import com.soul.neurokaraoke.ui.screens.explore.PlaylistDetailScreen
import com.soul.neurokaraoke.ui.screens.home.HomeScreen
import com.soul.neurokaraoke.ui.screens.library.FavoritesScreen
import com.soul.neurokaraoke.ui.screens.library.PlaylistsScreen
import com.soul.neurokaraoke.ui.screens.search.SearchScreen
import com.soul.neurokaraoke.ui.screens.setlist.SetlistDetailScreen
import com.soul.neurokaraoke.ui.screens.setlist.SetlistScreen

@Composable
fun NavGraph(
    navController: NavHostController,
    songs: List<Song> = emptyList(),
    allSongs: List<Song> = emptyList(),
    isLoadingAllSongs: Boolean = false,
    playlists: List<Playlist> = emptyList(),
    currentPlaylistId: String? = null,
    currentPlaylistName: String? = null,
    isLoading: Boolean = false,
    isLoggedIn: Boolean = false,
    favoriteSongs: List<Song> = emptyList(),
    currentSong: Song? = null,
    onSongClick: (String) -> Unit,
    onSearchSongClick: (String) -> Unit = onSongClick,
    onPlaySongWithQueue: (Song, List<Song>) -> Unit = { _, _ -> },
    onPlaylistSelect: (Playlist) -> Unit = {},
    @Suppress("UNUSED_PARAMETER") onExpandPlayer: () -> Unit,
    onLoadAllSongs: () -> Unit = {},
    onSignInClick: () -> Unit = {},
    onPlayClick: () -> Unit = {},
    onShuffleClick: () -> Unit = {}
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                songs = songs,
                playlistName = currentPlaylistName,
                isLoading = isLoading,
                onSongClick = onSongClick,
                onSeeAllClick = { navController.navigate(Screen.Search.route) }
            )
        }

        composable(Screen.Search.route) {
            // Trigger loading all songs when search screen is opened
            androidx.compose.runtime.LaunchedEffect(Unit) {
                onLoadAllSongs()
            }
            SearchScreen(
                songs = allSongs.ifEmpty { songs },
                isLoading = isLoadingAllSongs,
                onSongClick = onSearchSongClick
            )
        }

        composable(Screen.Explore.route) {
            ExploreScreen(
                onPlaylistClick = { playlistId ->
                    navController.navigate(Screen.PlaylistDetail.createRoute(playlistId))
                }
            )
        }

        composable(Screen.PlaylistDetail.route) { backStackEntry ->
            val playlistId = backStackEntry.arguments?.getString("playlistId") ?: return@composable
            PlaylistDetailScreen(
                playlistId = playlistId,
                onBackClick = { navController.popBackStack() },
                onPlaySong = onPlaySongWithQueue
            )
        }

        composable(Screen.Artists.route) {
            // Trigger loading all songs when artists screen is opened
            androidx.compose.runtime.LaunchedEffect(Unit) {
                onLoadAllSongs()
            }
            ArtistsScreen(
                songs = allSongs.ifEmpty { songs },
                isLoading = isLoadingAllSongs,
                onArtistClick = { artistName ->
                    navController.navigate(Screen.ArtistDetail.createRoute(java.net.URLEncoder.encode(artistName, "UTF-8")))
                }
            )
        }

        composable(Screen.ArtistDetail.route) { backStackEntry ->
            val artistName = backStackEntry.arguments?.getString("artistId")?.let {
                java.net.URLDecoder.decode(it, "UTF-8")
            } ?: return@composable

            ArtistDetailScreen(
                artistName = artistName,
                songs = allSongs.ifEmpty { songs },
                onBackClick = { navController.popBackStack() },
                onSongClick = onSearchSongClick
            )
        }

        composable(Screen.Setlists.route) {
            SetlistScreen(
                playlists = playlists,
                currentPlaylistId = currentPlaylistId,
                onPlaylistSelect = { playlist ->
                    onPlaylistSelect(playlist)
                    navController.navigate(Screen.SetlistDetail.createRoute(playlist.id))
                }
            )
        }

        composable(Screen.SetlistDetail.route) { backStackEntry ->
            val setlistId = backStackEntry.arguments?.getString("setlistId") ?: return@composable
            val playlist = playlists.find { it.id == setlistId }

            if (playlist != null) {
                SetlistDetailScreen(
                    playlist = playlist,
                    songs = if (currentPlaylistId == setlistId) songs else emptyList(),
                    isLoading = isLoading && currentPlaylistId == setlistId,
                    currentSong = currentSong,
                    onBackClick = { navController.popBackStack() },
                    onPlayClick = onPlayClick,
                    onShuffleClick = onShuffleClick,
                    onSongClick = onSongClick
                )
            }
        }

        composable(Screen.About.route) {
            AboutScreen()
        }

        composable(Screen.Favorites.route) {
            FavoritesScreen(
                songs = songs,
                favoriteSongs = favoriteSongs,
                isLoggedIn = isLoggedIn,
                onSongClick = onSongClick,
                onSignInClick = onSignInClick
            )
        }

        composable(Screen.Playlists.route) {
            PlaylistsScreen(
                onPlaylistClick = { playlistId ->
                    navController.navigate(Screen.PlaylistDetail.createRoute(playlistId))
                }
            )
        }
    }
}
