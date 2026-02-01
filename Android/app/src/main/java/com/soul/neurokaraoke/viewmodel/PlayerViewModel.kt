package com.soul.neurokaraoke.viewmodel

import android.app.Application
import android.content.ComponentName
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.soul.neurokaraoke.data.PlaylistCatalog
import com.soul.neurokaraoke.data.api.NeuroKaraokeApi
import com.soul.neurokaraoke.data.model.Playlist
import com.soul.neurokaraoke.data.model.Song
import com.soul.neurokaraoke.data.repository.SongRepository
import com.soul.neurokaraoke.service.MediaPlaybackService
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class PlayerUiState(
    val songs: List<Song> = emptyList(),
    val allSongs: List<Song> = emptyList(),
    val isLoadingAllSongs: Boolean = false,
    val allSongsLoaded: Boolean = false,
    val currentSong: Song? = null,
    val isPlaying: Boolean = false,
    val progress: Float = 0f,
    val currentPosition: Long = 0L,
    val duration: Long = 0L,
    val isLoading: Boolean = false,
    val error: String? = null,
    val currentPlaylistId: String? = null,
    val isShuffleEnabled: Boolean = false,
    val repeatMode: RepeatMode = RepeatMode.OFF,
    val availablePlaylists: List<Playlist> = emptyList(),
    val currentPlaylist: Playlist? = null,
    val queue: List<Song> = emptyList()
)

enum class RepeatMode {
    OFF, ONE, ALL
}

class PlayerViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val repository: SongRepository = SongRepository()
    private val playlistCatalog: PlaylistCatalog = PlaylistCatalog(application)
    private val api: NeuroKaraokeApi = NeuroKaraokeApi()

    private val _uiState = MutableStateFlow(PlayerUiState())
    val uiState: StateFlow<PlayerUiState> = _uiState.asStateFlow()

    private var progressJob: Job? = null
    private var controllerFuture: ListenableFuture<MediaController>? = null
    private var mediaController: MediaController? = null

    init {
        // Load available playlists on startup
        loadAvailablePlaylists()
        // Initialize media controller connection
        initializeMediaController()
    }

    private fun initializeMediaController() {
        val context = getApplication<Application>()
        val sessionToken = SessionToken(
            context,
            ComponentName(context, MediaPlaybackService::class.java)
        )

        controllerFuture = MediaController.Builder(context, sessionToken).buildAsync()
        controllerFuture?.addListener({
            mediaController = controllerFuture?.get()
            setupPlayerListener()
        }, MoreExecutors.directExecutor())
    }

    private fun setupPlayerListener() {
        mediaController?.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _uiState.value = _uiState.value.copy(isPlaying = isPlaying)
                if (isPlaying) {
                    startProgressUpdates()
                } else {
                    stopProgressUpdates()
                }
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                when (playbackState) {
                    Player.STATE_ENDED -> handleSongEnded()
                    Player.STATE_READY -> {
                        val duration = mediaController?.duration?.coerceAtLeast(0L) ?: 0L
                        _uiState.value = _uiState.value.copy(
                            duration = duration,
                            currentSong = _uiState.value.currentSong?.copy(duration = duration)
                        )
                    }
                    Player.STATE_BUFFERING -> { }
                    Player.STATE_IDLE -> { }
                }
            }

            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                mediaItem?.mediaId?.let { mediaId ->
                    val song = _uiState.value.songs.find { it.id == mediaId }
                        ?: _uiState.value.allSongs.find { it.id == mediaId }
                    if (song != null && song.id != _uiState.value.currentSong?.id) {
                        _uiState.value = _uiState.value.copy(
                            currentSong = song,
                            progress = 0f,
                            currentPosition = 0L
                        )
                    }
                }
            }

            override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
                _uiState.value = _uiState.value.copy(isShuffleEnabled = shuffleModeEnabled)
            }

            override fun onRepeatModeChanged(repeatMode: Int) {
                val mode = when (repeatMode) {
                    Player.REPEAT_MODE_ONE -> RepeatMode.ONE
                    Player.REPEAT_MODE_ALL -> RepeatMode.ALL
                    else -> RepeatMode.OFF
                }
                _uiState.value = _uiState.value.copy(repeatMode = mode)
            }
        })
    }

    private fun startProgressUpdates() {
        progressJob?.cancel()
        progressJob = viewModelScope.launch {
            while (isActive) {
                val controller = mediaController ?: continue
                val position = controller.currentPosition.coerceAtLeast(0L)
                val duration = controller.duration.coerceAtLeast(1L)
                val progress = if (duration > 0) position.toFloat() / duration else 0f

                _uiState.value = _uiState.value.copy(
                    progress = progress.coerceIn(0f, 1f),
                    currentPosition = position,
                    duration = duration
                )
                delay(250L)
            }
        }
    }

    private fun stopProgressUpdates() {
        progressJob?.cancel()
        progressJob = null
    }

    private fun handleSongEnded() {
        when (_uiState.value.repeatMode) {
            RepeatMode.ONE -> {
                mediaController?.seekTo(0)
                mediaController?.play()
            }
            RepeatMode.ALL -> {
                playNext(wrapAround = true)
            }
            RepeatMode.OFF -> {
                playNext(wrapAround = false)
            }
        }
    }

    /**
     * Load songs from a playlist
     */
    fun loadPlaylist(playlistId: String) {
        if (_uiState.value.currentPlaylistId == playlistId && _uiState.value.songs.isNotEmpty()) {
            return // Already loaded
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            val playlist = _uiState.value.availablePlaylists.find { it.id == playlistId }

            repository.getPlaylistSongs(playlistId).fold(
                onSuccess = { songs ->
                    _uiState.value = _uiState.value.copy(
                        songs = songs,
                        queue = songs,
                        isLoading = false,
                        currentPlaylistId = playlistId,
                        currentPlaylist = playlist,
                        currentSong = _uiState.value.currentSong ?: songs.firstOrNull()
                    )
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message ?: "Failed to load playlist"
                    )
                }
            )
        }
    }

    /**
     * Play a specific song
     */
    fun playSong(song: Song) {
        if (song.audioUrl.isBlank()) {
            _uiState.value = _uiState.value.copy(
                error = "No audio URL available for this song"
            )
            return
        }

        _uiState.value = _uiState.value.copy(
            currentSong = song,
            progress = 0f,
            currentPosition = 0L,
            error = null
        )

        val controller = mediaController ?: return
        val songs = _uiState.value.queue.ifEmpty { _uiState.value.songs }
        val songIndex = songs.indexOfFirst { it.id == song.id }

        // Build media items for the entire playlist
        val mediaItems = songs.map { s ->
            MediaItem.Builder()
                .setUri(s.audioUrl)
                .setMediaId(s.id)
                .setMediaMetadata(
                    MediaMetadata.Builder()
                        .setTitle(s.title)
                        .setArtist("${s.artist} • ${s.coverArtist}")
                        .setArtworkUri(android.net.Uri.parse(s.coverUrl))
                        .build()
                )
                .build()
        }

        // Set the entire playlist and start at the selected song
        controller.setMediaItems(mediaItems, songIndex.coerceAtLeast(0), 0L)
        controller.prepare()
        controller.play()
    }

    /**
     * Play song by ID from current playlist
     */
    fun playSongById(songId: String) {
        val song = _uiState.value.songs.find { it.id == songId }
        song?.let { playSong(it) }
    }

    /**
     * Toggle play/pause
     */
    fun togglePlayPause() {
        val controller = mediaController ?: return
        if (controller.isPlaying) {
            controller.pause()
        } else {
            val currentSong = _uiState.value.currentSong
            if (controller.playbackState == Player.STATE_IDLE && currentSong != null) {
                playSong(currentSong)
            } else {
                controller.play()
            }
        }
    }

    /**
     * Play previous song
     */
    fun playPrevious() {
        val controller = mediaController ?: return

        // If more than 3 seconds into the song, restart it
        if (controller.currentPosition > 3000) {
            controller.seekTo(0)
            return
        }

        // Use the player's built-in previous functionality
        if (controller.hasPreviousMediaItem()) {
            controller.seekToPreviousMediaItem()
        } else if (_uiState.value.repeatMode == RepeatMode.ALL && controller.mediaItemCount > 0) {
            controller.seekTo(controller.mediaItemCount - 1, 0L)
        }
    }

    /**
     * Play next song
     */
    fun playNext(wrapAround: Boolean = false) {
        val controller = mediaController ?: return

        // Use the player's built-in next functionality
        if (controller.hasNextMediaItem()) {
            controller.seekToNextMediaItem()
        } else if (wrapAround && controller.mediaItemCount > 0) {
            controller.seekTo(0, 0L)
        } else {
            // Queue ended and not looping - play random songs from across all playlists
            playRandomSongFromAllPlaylists()
        }
    }

    /**
     * Play a random song from all available playlists (auto-play when queue ends)
     */
    private fun playRandomSongFromAllPlaylists() {
        viewModelScope.launch {
            // Ensure all songs are loaded first
            if (!_uiState.value.allSongsLoaded && !_uiState.value.isLoadingAllSongs) {
                loadAllSongsAndPlayRandom()
            } else if (_uiState.value.allSongsLoaded) {
                pickAndPlayRandomSong()
            } else {
                // Songs are currently loading, wait for them
                waitForSongsAndPlayRandom()
            }
        }
    }

    private suspend fun loadAllSongsAndPlayRandom() {
        _uiState.value = _uiState.value.copy(isLoadingAllSongs = true)

        val allSongs = mutableListOf<Song>()
        val playlists = _uiState.value.availablePlaylists

        for (playlist in playlists) {
            repository.getPlaylistSongs(playlist.id).onSuccess { songs ->
                allSongs.addAll(songs)
            }
        }

        _uiState.value = _uiState.value.copy(
            allSongs = allSongs.distinctBy { it.id }.toList(),
            isLoadingAllSongs = false,
            allSongsLoaded = true
        )

        pickAndPlayRandomSong()
    }

    private suspend fun waitForSongsAndPlayRandom() {
        // Wait for songs to finish loading (check every 100ms, max 10 seconds)
        var attempts = 0
        while (_uiState.value.isLoadingAllSongs && attempts < 100) {
            delay(100)
            attempts++
        }
        if (_uiState.value.allSongsLoaded) {
            pickAndPlayRandomSong()
        }
    }

    private fun pickAndPlayRandomSong() {
        val allSongs = _uiState.value.allSongs
        if (allSongs.isEmpty()) return

        // Exclude the current song to avoid immediate repeat
        val currentSongId = _uiState.value.currentSong?.id
        val availableSongs = if (currentSongId != null) {
            allSongs.filter { it.id != currentSongId }
        } else {
            allSongs
        }

        if (availableSongs.isEmpty()) return

        val randomSong = availableSongs.random()
        playSong(randomSong)
    }

    /**
     * Seek to position (0.0 to 1.0)
     */
    fun seekTo(progress: Float) {
        val controller = mediaController ?: return
        val duration = controller.duration
        if (duration > 0) {
            val position = (progress * duration).toLong()
            controller.seekTo(position)
            _uiState.value = _uiState.value.copy(
                progress = progress,
                currentPosition = position
            )
        }
    }

    /**
     * Toggle shuffle mode
     */
    fun toggleShuffle() {
        val controller = mediaController ?: return
        controller.shuffleModeEnabled = !controller.shuffleModeEnabled
    }

    /**
     * Cycle through repeat modes: OFF -> ALL -> ONE -> OFF
     */
    fun cycleRepeatMode() {
        val controller = mediaController ?: return
        val newMode = when (_uiState.value.repeatMode) {
            RepeatMode.OFF -> RepeatMode.ALL
            RepeatMode.ALL -> RepeatMode.ONE
            RepeatMode.ONE -> RepeatMode.OFF
        }
        _uiState.value = _uiState.value.copy(repeatMode = newMode)

        controller.repeatMode = when (newMode) {
            RepeatMode.OFF -> Player.REPEAT_MODE_OFF
            RepeatMode.ONE -> Player.REPEAT_MODE_ONE
            RepeatMode.ALL -> Player.REPEAT_MODE_ALL
        }
    }

    /**
     * Clear error
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    /**
     * Load available playlists from the catalog
     */
    fun loadAvailablePlaylists() {
        viewModelScope.launch {
            val playlists = playlistCatalog.getPlaylists()
            _uiState.value = _uiState.value.copy(availablePlaylists = playlists)

            // Auto-fetch names for playlists with empty names
            refreshPlaylistNames()
        }
    }

    /**
     * Fetch names and cover URLs from API for playlists missing info
     */
    private fun refreshPlaylistNames() {
        viewModelScope.launch {
            val playlists = _uiState.value.availablePlaylists
            var updated = false

            for (playlist in playlists) {
                if (playlist.title.isEmpty() || playlist.previewCovers.isEmpty()) {
                    api.fetchPlaylistInfo(playlist.id).onSuccess { info ->
                        val updatedPlaylist = playlist.copy(
                            title = if (playlist.title.isEmpty()) info.name else playlist.title,
                            coverUrl = if (playlist.coverUrl.isEmpty()) info.coverUrl else playlist.coverUrl,
                            previewCovers = if (playlist.previewCovers.isEmpty()) info.previewCovers else playlist.previewCovers
                        )
                        playlistCatalog.updatePlaylist(updatedPlaylist)
                        updated = true
                    }
                }
            }

            if (updated) {
                val refreshedPlaylists = playlistCatalog.getPlaylists()
                _uiState.value = _uiState.value.copy(availablePlaylists = refreshedPlaylists)
            }
        }
    }

    /**
     * Add a new playlist to the catalog by ID (fetches name from API)
     */
    fun addPlaylistById(id: String) {
        viewModelScope.launch {
            if (playlistCatalog.hasPlaylist(id)) {
                _uiState.value = _uiState.value.copy(error = "Playlist already exists")
                return@launch
            }

            api.fetchPlaylistInfo(id).fold(
                onSuccess = { info ->
                    val playlist = Playlist(
                        id = info.id,
                        title = info.name,
                        description = "",
                        coverUrl = info.coverUrl,
                        previewCovers = info.previewCovers
                    )
                    if (playlistCatalog.addPlaylist(playlist)) {
                        loadAvailablePlaylists()
                    }
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        error = "Failed to fetch playlist: ${error.message}"
                    )
                }
            )
        }
    }

    /**
     * Add a new playlist to the catalog with manual details
     */
    fun addPlaylist(id: String, name: String, description: String = "", coverUrl: String = "") {
        viewModelScope.launch {
            val playlist = Playlist(
                id = id,
                title = name,
                description = description,
                coverUrl = coverUrl
            )
            if (playlistCatalog.addPlaylist(playlist)) {
                loadAvailablePlaylists()
            }
        }
    }

    /**
     * Remove a playlist from the catalog
     */
    fun removePlaylist(playlistId: String) {
        viewModelScope.launch {
            if (playlistCatalog.removePlaylist(playlistId)) {
                loadAvailablePlaylists()
            }
        }
    }

    /**
     * Select and load a playlist
     */
    fun selectPlaylist(playlist: Playlist) {
        _uiState.value = _uiState.value.copy(currentPlaylist = playlist)
        loadPlaylist(playlist.id)
    }

    /**
     * Load all songs from all playlists for search functionality
     */
    fun loadAllSongs() {
        if (_uiState.value.allSongsLoaded || _uiState.value.isLoadingAllSongs) {
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingAllSongs = true)

            val allSongs = mutableListOf<Song>()
            val playlists = _uiState.value.availablePlaylists

            for (playlist in playlists) {
                repository.getPlaylistSongs(playlist.id).onSuccess { songs ->
                    allSongs.addAll(songs)
                    _uiState.value = _uiState.value.copy(
                        allSongs = allSongs.distinctBy { it.id }.toList()
                    )
                }
            }

            _uiState.value = _uiState.value.copy(
                allSongs = allSongs.distinctBy { it.id }.toList(),
                isLoadingAllSongs = false,
                allSongsLoaded = true
            )
        }
    }

    /**
     * Play a song from the all songs list (for search results)
     */
    fun playSongFromAllSongs(songId: String) {
        val allSongs = _uiState.value.allSongs
        val song = allSongs.find { it.id == songId }
            ?: _uiState.value.songs.find { it.id == songId }

        song?.let {
            // Update queue to allSongs so the song can be found
            if (allSongs.isNotEmpty() && allSongs.any { s -> s.id == songId }) {
                _uiState.value = _uiState.value.copy(queue = allSongs)
            }
            playSong(it)
        }
    }

    /**
     * Play a song with a custom queue (for external playlists like Explore)
     */
    fun playSongWithQueue(song: Song, queue: List<Song>) {
        _uiState.value = _uiState.value.copy(queue = queue)
        playSong(song)
    }

    /**
     * Get the catalog file path for manual editing
     */
    fun getPlaylistCatalogPath(): String = playlistCatalog.getFilePath()

    override fun onCleared() {
        super.onCleared()
        stopProgressUpdates()
        controllerFuture?.let { MediaController.releaseFuture(it) }
        mediaController = null
    }
}
