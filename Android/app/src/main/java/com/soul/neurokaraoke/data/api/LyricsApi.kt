package com.soul.neurokaraoke.data.api

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

data class LyricsResult(
    val id: Int,
    val trackName: String,
    val artistName: String,
    val albumName: String?,
    val syncedLyrics: String?, // LRC format with timestamps
    val plainLyrics: String?
)

data class LyricLine(
    val timestamp: Long, // in milliseconds
    val text: String
)

class LyricsApi {
    companion object {
        private const val BASE_URL = "https://lrclib.net/api"
        private const val USER_AGENT = "NeuroKaraoke Android App (https://github.com/user/neurokaraoke)"
    }

    /**
     * Search for lyrics by track name and artist
     */
    suspend fun searchLyrics(trackName: String, artistName: String): Result<LyricsResult?> = withContext(Dispatchers.IO) {
        try {
            val encodedTrack = URLEncoder.encode(trackName, "UTF-8")
            val encodedArtist = URLEncoder.encode(artistName, "UTF-8")
            val url = URL("$BASE_URL/get?track_name=$encodedTrack&artist_name=$encodedArtist")

            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.setRequestProperty("User-Agent", USER_AGENT)
            connection.connectTimeout = 10000
            connection.readTimeout = 10000

            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val response = connection.inputStream.bufferedReader().use { it.readText() }
                val json = JSONObject(response)
                Result.success(parseLyricsResult(json))
            } else if (responseCode == HttpURLConnection.HTTP_NOT_FOUND) {
                // No lyrics found, try search endpoint
                searchLyricsFallback(trackName, artistName)
            } else {
                Result.failure(Exception("HTTP $responseCode"))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    /**
     * Fallback search using the search endpoint
     */
    private suspend fun searchLyricsFallback(trackName: String, artistName: String): Result<LyricsResult?> = withContext(Dispatchers.IO) {
        try {
            // Try with just track name first
            val query = "$trackName $artistName"
            val encodedQuery = URLEncoder.encode(query, "UTF-8")
            val url = URL("$BASE_URL/search?q=$encodedQuery")

            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.setRequestProperty("User-Agent", USER_AGENT)
            connection.connectTimeout = 10000
            connection.readTimeout = 10000

            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val response = connection.inputStream.bufferedReader().use { it.readText() }
                val jsonArray = JSONArray(response)

                if (jsonArray.length() > 0) {
                    // Return first result
                    val json = jsonArray.getJSONObject(0)
                    Result.success(parseLyricsResult(json))
                } else {
                    Result.success(null)
                }
            } else {
                Result.success(null)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    private fun parseLyricsResult(json: JSONObject): LyricsResult {
        return LyricsResult(
            id = json.optInt("id", 0),
            trackName = json.optString("trackName", ""),
            artistName = json.optString("artistName", ""),
            albumName = json.optString("albumName", null),
            syncedLyrics = json.optString("syncedLyrics", null).takeIf { it.isNotBlank() && it != "null" },
            plainLyrics = json.optString("plainLyrics", null).takeIf { it.isNotBlank() && it != "null" }
        )
    }

    /**
     * Parse LRC format lyrics into list of timed lines
     * LRC format: [mm:ss.xx]lyrics text
     */
    fun parseSyncedLyrics(lrcContent: String): List<LyricLine> {
        val lines = mutableListOf<LyricLine>()
        val regex = Regex("""\[(\d{2}):(\d{2})\.(\d{2,3})\](.*)""")

        for (line in lrcContent.lines()) {
            val match = regex.find(line)
            if (match != null) {
                val (minutes, seconds, millis, text) = match.destructured
                val millisPart = if (millis.length == 2) millis.toInt() * 10 else millis.toInt()
                val timestamp = minutes.toLong() * 60 * 1000 +
                               seconds.toLong() * 1000 +
                               millisPart
                lines.add(LyricLine(timestamp, text.trim()))
            }
        }

        return lines.sortedBy { it.timestamp }
    }

    /**
     * Parse plain lyrics into lines (no timestamps)
     */
    fun parsePlainLyrics(plainContent: String): List<LyricLine> {
        return plainContent.lines()
            .filter { it.isNotBlank() }
            .mapIndexed { index, text ->
                // Assign fake timestamps for display (won't be used for syncing)
                LyricLine(index * 3000L, text.trim())
            }
    }
}
