package com.soul.neurokaraoke.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.soul.neurokaraoke.navigation.NavGraph
import com.soul.neurokaraoke.navigation.Screen
import com.soul.neurokaraoke.ui.components.MiniPlayer
import com.soul.neurokaraoke.ui.components.NavigationDrawerContent
import com.soul.neurokaraoke.ui.components.NeuroTopBar
import com.soul.neurokaraoke.ui.screens.player.PlayerScreen
import com.soul.neurokaraoke.viewmodel.AuthViewModel
import com.soul.neurokaraoke.viewmodel.PlayerViewModel
import com.soul.neurokaraoke.viewmodel.RepeatMode
import kotlinx.coroutines.launch

// Fallback playlist ID if catalog is empty
private const val FALLBACK_PLAYLIST_ID = "359bc793-0b63-4b89-b0ea-c3a4d068decc"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    playerViewModel: PlayerViewModel = viewModel(),
    authViewModel: AuthViewModel = viewModel()
) {
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val context = androidx.compose.ui.platform.LocalContext.current

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Collect player state from ViewModel
    val playerState by playerViewModel.uiState.collectAsState()
    val authState by authViewModel.uiState.collectAsState()
    var showFullPlayer by remember { mutableStateOf(false) }

    val playerSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showAlphaBanner by remember { mutableStateOf(true) }

    // Load first available playlist or fallback
    LaunchedEffect(playerState.availablePlaylists) {
        if (playerState.currentPlaylistId == null) {
            val playlistId = playerState.availablePlaylists.firstOrNull()?.id ?: FALLBACK_PLAYLIST_ID
            playerViewModel.loadPlaylist(playlistId)
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                NavigationDrawerContent(
                    currentRoute = currentRoute,
                    onNavigate = { screen ->
                        navController.navigate(screen.route) {
                            popUpTo(Screen.Home.route) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onClose = {
                        scope.launch { drawerState.close() }
                    },
                    isLoggedIn = authState.isLoggedIn,
                    userName = authState.user?.displayName,
                    userAvatarUrl = authState.user?.avatarUrl,
                    onSignInClick = {
                        context.startActivity(authViewModel.getSignInIntent())
                    },
                    onSignOutClick = {
                        authViewModel.logout()
                    }
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                NeuroTopBar(
                    onMenuClick = {
                        scope.launch { drawerState.open() }
                    },
                    onProfileClick = { }
                )
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // Main content
                NavGraph(
                    navController = navController,
                    songs = playerState.songs,
                    allSongs = playerState.allSongs,
                    isLoadingAllSongs = playerState.isLoadingAllSongs,
                    playlists = playerState.availablePlaylists,
                    currentPlaylistId = playerState.currentPlaylistId,
                    currentPlaylistName = playerState.currentPlaylist?.title,
                    isLoading = playerState.isLoading,
                    isLoggedIn = authState.isLoggedIn,
                    favoriteSongs = emptyList(), // TODO: Load from server when logged in
                    currentSong = playerState.currentSong,
                    onSongClick = { songId ->
                        playerViewModel.playSongById(songId)
                    },
                    onSearchSongClick = { songId ->
                        playerViewModel.playSongFromAllSongs(songId)
                    },
                    onPlaySongWithQueue = { song, queue ->
                        playerViewModel.playSongWithQueue(song, queue)
                    },
                    onPlaylistSelect = { playlist ->
                        playerViewModel.selectPlaylist(playlist)
                    },
                    onExpandPlayer = {
                        showFullPlayer = true
                    },
                    onLoadAllSongs = {
                        playerViewModel.loadAllSongs()
                    },
                    onSignInClick = {
                        context.startActivity(authViewModel.getSignInIntent())
                    },
                    onPlayClick = {
                        // Play first song in current playlist
                        playerState.songs.firstOrNull()?.let { song ->
                            playerViewModel.playSongById(song.id)
                        }
                    },
                    onShuffleClick = {
                        // Enable shuffle and play
                        if (!playerState.isShuffleEnabled) {
                            playerViewModel.toggleShuffle()
                        }
                        playerState.songs.randomOrNull()?.let { song ->
                            playerViewModel.playSongById(song.id)
                        }
                    }
                )

                // Alpha banner at top
                if (showAlphaBanner) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                            .align(Alignment.TopCenter),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 12.dp, top = 8.dp, bottom = 8.dp, end = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Alpha Version",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    text = "This app is still in development. Report bugs and feedback to @aferil. on Discord!",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                )
                            }
                            IconButton(onClick = { showAlphaBanner = false }) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Dismiss",
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }
                }

                // Mini player at bottom
                MiniPlayer(
                    currentSong = playerState.currentSong,
                    isPlaying = playerState.isPlaying,
                    progress = playerState.progress,
                    onPlayPauseClick = { playerViewModel.togglePlayPause() },
                    onPreviousClick = { playerViewModel.playPrevious() },
                    onNextClick = { playerViewModel.playNext() },
                    onExpandClick = { showFullPlayer = true },
                    modifier = Modifier.align(Alignment.BottomCenter)
                )
            }
        }
    }

    // Full player bottom sheet
    val currentSong = playerState.currentSong
    if (showFullPlayer && currentSong != null) {
        ModalBottomSheet(
            onDismissRequest = { showFullPlayer = false },
            sheetState = playerSheetState,
            dragHandle = null
        ) {
            PlayerScreen(
                song = currentSong,
                isPlaying = playerState.isPlaying,
                progress = playerState.progress,
                currentPosition = playerState.currentPosition,
                duration = playerState.duration,
                isShuffleEnabled = playerState.isShuffleEnabled,
                repeatMode = playerState.repeatMode,
                queue = playerState.queue,
                onPlayPauseClick = { playerViewModel.togglePlayPause() },
                onPreviousClick = { playerViewModel.playPrevious() },
                onNextClick = { playerViewModel.playNext() },
                onSeekTo = { playerViewModel.seekTo(it) },
                onShuffleClick = { playerViewModel.toggleShuffle() },
                onRepeatClick = { playerViewModel.cycleRepeatMode() },
                onCollapseClick = { showFullPlayer = false },
                onQueueSongClick = { songId -> playerViewModel.playSongById(songId) }
            )
        }
    }
}
