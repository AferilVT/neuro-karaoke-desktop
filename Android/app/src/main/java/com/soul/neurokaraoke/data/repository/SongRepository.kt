package com.soul.neurokaraoke.data.repository

import com.soul.neurokaraoke.data.api.ApiSong
import com.soul.neurokaraoke.data.api.NeuroKaraokeApi
import com.soul.neurokaraoke.data.model.Singer
import com.soul.neurokaraoke.data.model.Song

class SongRepository(
    private val api: NeuroKaraokeApi = NeuroKaraokeApi()
) {
    /**
     * Fetch songs from a playlist
     */
    suspend fun getPlaylistSongs(playlistId: String): Result<List<Song>> {
        return api.fetchPlaylist(playlistId).map { apiSongs ->
            apiSongs.mapIndexed { index, apiSong ->
                apiSong.toSong(playlistId, index)
            }
        }
    }

    /**
     * Find a specific song in a playlist
     */
    fun findSong(playlistId: String, title: String, artist: String? = null): Song? {
        return api.findSong(playlistId, title, artist)?.toSong(playlistId, 0)
    }

    private fun ApiSong.toSong(playlistId: String, index: Int): Song {
        // Determine singer based on cover artists
        val singer = when {
            coverArtists?.contains("Evil", ignoreCase = true) == true &&
            coverArtists.contains("Neuro", ignoreCase = true) -> Singer.DUET
            coverArtists?.contains("Evil", ignoreCase = true) == true -> Singer.EVIL
            else -> Singer.NEURO
        }

        return Song(
            id = "${playlistId}_$index",
            title = title,
            artist = originalArtists ?: "Unknown Artist",
            coverUrl = getCoverArtUrl() ?: "",
            audioUrl = audioUrl ?: "",
            duration = 0L, // Duration not provided by API
            singer = singer,
            artCredit = artCredit?.takeIf { it.isNotBlank() }
        )
    }
}
