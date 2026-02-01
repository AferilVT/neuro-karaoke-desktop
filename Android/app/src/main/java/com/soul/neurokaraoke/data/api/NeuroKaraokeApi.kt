package com.soul.neurokaraoke.data.api

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

data class ApiPlaylistInfo(
    val id: String,
    val name: String,
    val coverUrl: String,
    val previewCovers: List<String> = emptyList()
)

data class ApiSong(
    val playlistName: String?,
    val title: String,
    val originalArtists: String?,
    val coverArtists: String?,
    val coverArt: String?,
    val audioUrl: String?,
    val artCredit: String? = null
) {
    /**
     * Derive cover art URL from audio URL
     * Example: https://storage.neurokaraoke.com/audio/FEX%20-%20Subways.mp3
     * Becomes: https://storage.neurokaraoke.com/images/FEX%20-%20Subways.jpg
     */
    fun getCoverArtUrl(): String? {
        // Use explicit coverArt if available
        if (!coverArt.isNullOrBlank()) return coverArt

        // Derive from audio URL
        return audioUrl?.replace("/audio/", "/images/")
            ?.replace(Regex("\\.v\\d+\\)?\\.mp3$"), ".jpg")
            ?.replace(".mp3", ".jpg")
    }
}

data class ApiPublicPlaylist(
    val id: String,
    val name: String,
    val description: String?,
    val coverUrl: String?,
    val mosaicCovers: List<String>,
    val songCount: Int,
    val createdBy: String?
)

class NeuroKaraokeApi {
    companion object {
        private const val BASE_URL = "https://idk.neurokaraoke.com"
        private const val API_URL = "https://api.neurokaraoke.com"
    }

    private val cache = mutableMapOf<String, List<ApiSong>>()

    /**
     * Fetch playlist songs from API
     */
    suspend fun fetchPlaylist(playlistId: String): Result<List<ApiSong>> = withContext(Dispatchers.IO) {
        // Check cache first
        cache[playlistId]?.let { return@withContext Result.success(it) }

        var connection: HttpURLConnection? = null
        try {
            val url = URL("$BASE_URL/public/playlist/$playlistId")
            connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 10000
            connection.readTimeout = 10000

            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val response = connection.inputStream.bufferedReader().use { it.readText() }
                val songs = parsePlaylistResponse(response)
                cache[playlistId] = songs
                Result.success(songs)
            } else {
                Result.failure(Exception("HTTP error: $responseCode"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        } finally {
            connection?.disconnect()
        }
    }

    private fun parsePlaylistResponse(json: String): List<ApiSong> {
        val songs = mutableListOf<ApiSong>()
        try {
            // API returns an object with "songs" array and metadata
            val rootObject = JSONObject(json)
            val playlistName = rootObject.optString("name").takeIf { it.isNotEmpty() }
            val jsonArray = rootObject.optJSONArray("songs") ?: JSONArray()

            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                songs.add(
                    ApiSong(
                        playlistName = playlistName,
                        title = obj.optString("title", "Unknown"),
                        originalArtists = obj.optString("originalArtists"),
                        coverArtists = obj.optString("coverArtists"),
                        coverArt = obj.optString("coverArt"),
                        audioUrl = obj.optString("audioUrl"),
                        artCredit = obj.optString("artCredit")
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return songs
    }

    /**
     * Fetch playlist info (name, cover) from API
     */
    suspend fun fetchPlaylistInfo(playlistId: String): Result<ApiPlaylistInfo> = withContext(Dispatchers.IO) {
        var connection: HttpURLConnection? = null
        try {
            val url = URL("$BASE_URL/public/playlist/$playlistId")
            connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 10000
            connection.readTimeout = 10000

            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val response = connection.inputStream.bufferedReader().use { it.readText() }
                val rootObject = JSONObject(response)

                // Handle cover URL - prepend base URL if it's a relative path
                val rawCoverUrl = rootObject.optString("cover", "")
                val coverUrl = when {
                    rawCoverUrl.isEmpty() -> ""
                    rawCoverUrl.startsWith("http") -> rawCoverUrl
                    rawCoverUrl.startsWith("/") -> "https://storage.neurokaraoke.com$rawCoverUrl"
                    else -> "https://storage.neurokaraoke.com/$rawCoverUrl"
                }

                // Extract first 4 unique song covers for preview grid
                val previewCovers = mutableListOf<String>()
                val songsArray = rootObject.optJSONArray("songs")
                if (songsArray != null) {
                    for (i in 0 until minOf(songsArray.length(), 20)) {
                        if (previewCovers.size >= 4) break
                        val songObj = songsArray.getJSONObject(i)

                        // Try coverArt first, then derive from audioUrl
                        var coverArtUrl = songObj.optString("coverArt", "")
                        if (coverArtUrl.isBlank()) {
                            val audioUrl = songObj.optString("audioUrl", "")
                            if (audioUrl.isNotBlank()) {
                                coverArtUrl = audioUrl
                                    .replace("/audio/", "/images/")
                                    .replace(Regex("\\.v\\d+\\)?\\.mp3$"), ".jpg")
                                    .replace(".mp3", ".jpg")
                            }
                        }

                        if (coverArtUrl.isNotBlank() && coverArtUrl !in previewCovers) {
                            previewCovers.add(coverArtUrl)
                        }
                    }
                }

                val info = ApiPlaylistInfo(
                    id = playlistId,
                    name = rootObject.optString("name", "Unknown Playlist"),
                    coverUrl = coverUrl,
                    previewCovers = previewCovers
                )
                Result.success(info)
            } else {
                Result.failure(Exception("HTTP error: $responseCode"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        } finally {
            connection?.disconnect()
        }
    }

    /**
     * Fetch all public playlists from API
     */
    suspend fun fetchPublicPlaylists(): Result<List<ApiPublicPlaylist>> = withContext(Dispatchers.IO) {
        var connection: HttpURLConnection? = null
        try {
            val url = URL("$API_URL/api/playlist/public")
            connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 15000
            connection.readTimeout = 15000

            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val response = connection.inputStream.bufferedReader().use { it.readText() }
                val playlists = parsePublicPlaylistsResponse(response)
                Result.success(playlists)
            } else {
                Result.failure(Exception("HTTP error: $responseCode"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        } finally {
            connection?.disconnect()
        }
    }

    private fun parsePublicPlaylistsResponse(json: String): List<ApiPublicPlaylist> {
        val playlists = mutableListOf<ApiPublicPlaylist>()
        try {
            val jsonArray = JSONArray(json)
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)

                // Parse cover URL from media object
                val mediaObj = obj.optJSONObject("media")
                val coverUrl = mediaObj?.let { media ->
                    val cloudflareId = media.optString("cloudflareId", "")
                    val absolutePath = media.optString("absolutePath", "")
                    when {
                        cloudflareId.isNotBlank() -> "https://imagedelivery.net/OEm2wS2prJtrPEAfkXmXvw/$cloudflareId/public"
                        absolutePath.isNotBlank() -> absolutePath
                        else -> null
                    }
                }

                // Parse mosaic covers from mosaicMedia array
                val mosaicCovers = mutableListOf<String>()
                val mosaicArray = obj.optJSONArray("mosaicMedia")
                if (mosaicArray != null) {
                    for (j in 0 until minOf(mosaicArray.length(), 4)) {
                        val mosaicObj = mosaicArray.getJSONObject(j)
                        val cloudflareId = mosaicObj.optString("cloudflareId", "")
                        val absolutePath = mosaicObj.optString("absolutePath", "")
                        val mosaicUrl = when {
                            cloudflareId.isNotBlank() -> "https://imagedelivery.net/OEm2wS2prJtrPEAfkXmXvw/$cloudflareId/public"
                            absolutePath.isNotBlank() -> absolutePath
                            else -> null
                        }
                        mosaicUrl?.let { mosaicCovers.add(it) }
                    }
                }

                playlists.add(
                    ApiPublicPlaylist(
                        id = obj.getString("id"),
                        name = obj.optString("name", "Unknown Playlist"),
                        description = obj.optString("description").takeIf { it.isNotEmpty() },
                        coverUrl = coverUrl,
                        mosaicCovers = mosaicCovers,
                        songCount = obj.optInt("songCount", 0),
                        createdBy = obj.optString("createdBy").takeIf { it.isNotEmpty() }
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return playlists
    }

    /**
     * Find a song in cached playlist by title
     */
    fun findSong(playlistId: String, title: String, artist: String? = null): ApiSong? {
        val songs = cache[playlistId] ?: return null

        val normalize = { value: String? ->
            value?.lowercase()
                ?.replace(Regex("[\\u0300-\\u036f]"), "")
                ?.replace(Regex("['\".,!?()\\[\\]{}:;/-]"), " ")
                ?.replace(Regex("\\s+"), " ")
                ?.trim()
                ?: ""
        }

        val titleNorm = normalize(title)
        val artistNorm = normalize(artist)

        var bestSong: ApiSong? = null
        var bestScore = 0

        for (song in songs) {
            val songTitleNorm = normalize(song.title)
            val coverArtistsNorm = normalize(song.coverArtists)
            val originalArtistsNorm = normalize(song.originalArtists)

            var score = 0
            if (song.title.equals(title, ignoreCase = true)) score += 3
            if (songTitleNorm == titleNorm) score += 2
            if (songTitleNorm.contains(titleNorm) || titleNorm.contains(songTitleNorm)) score += 1

            if (artistNorm.isNotEmpty()) {
                if (coverArtistsNorm.contains(artistNorm)) score += 2
                if (originalArtistsNorm.contains(artistNorm)) score += 2
            } else {
                score += 1
            }

            if (score > bestScore) {
                bestScore = score
                bestSong = song
            }
        }

        return if (bestScore > 0) bestSong else null
    }
}
