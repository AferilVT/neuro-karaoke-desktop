package com.soul.neurokaraoke.data

import android.content.Context
import com.soul.neurokaraoke.data.model.Playlist
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

/**
 * Manages the local playlist catalog stored in internal storage.
 *
 * On first run, copies default playlists from assets to internal storage.
 * You can edit the playlists.json file in the app's internal storage:
 * /data/data/com.soul.neurokaraoke/files/playlists.json
 *
 * Or use adb:
 * adb pull /data/data/com.soul.neurokaraoke/files/playlists.json
 * adb push playlists.json /data/data/com.soul.neurokaraoke/files/
 */
class PlaylistCatalog(private val context: Context) {

    private val fileName = "playlists.json"
    private val file: File
        get() = File(context.filesDir, fileName)

    /**
     * Load all playlists from the catalog
     */
    suspend fun getPlaylists(): List<Playlist> = withContext(Dispatchers.IO) {
        ensureFileExists()
        try {
            val json = file.readText()
            parsePlaylistsJson(json)
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    /**
     * Add a new playlist to the catalog
     */
    suspend fun addPlaylist(playlist: Playlist): Boolean = withContext(Dispatchers.IO) {
        try {
            val playlists = getPlaylists().toMutableList()

            // Check if already exists
            if (playlists.any { it.id == playlist.id }) {
                return@withContext false
            }

            playlists.add(playlist)
            savePlaylists(playlists)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Update an existing playlist
     */
    suspend fun updatePlaylist(playlist: Playlist): Boolean = withContext(Dispatchers.IO) {
        try {
            val playlists = getPlaylists().toMutableList()
            val index = playlists.indexOfFirst { it.id == playlist.id }

            if (index == -1) {
                return@withContext false
            }

            playlists[index] = playlist
            savePlaylists(playlists)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Remove a playlist from the catalog
     */
    suspend fun removePlaylist(playlistId: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val playlists = getPlaylists().toMutableList()
            val removed = playlists.removeAll { it.id == playlistId }

            if (removed) {
                savePlaylists(playlists)
            }
            removed
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Check if a playlist exists in the catalog
     */
    suspend fun hasPlaylist(playlistId: String): Boolean = withContext(Dispatchers.IO) {
        getPlaylists().any { it.id == playlistId }
    }

    /**
     * Save playlists to file
     */
    private fun savePlaylists(playlists: List<Playlist>) {
        val jsonArray = JSONArray()

        playlists.forEach { playlist ->
            val previewCoversArray = JSONArray().apply {
                playlist.previewCovers.forEach { put(it) }
            }
            val obj = JSONObject().apply {
                put("id", playlist.id)
                put("name", playlist.title)
                put("description", playlist.description)
                put("coverUrl", playlist.coverUrl)
                put("previewCovers", previewCoversArray)
            }
            jsonArray.put(obj)
        }

        val root = JSONObject().apply {
            put("playlists", jsonArray)
        }

        file.writeText(root.toString(2))
    }

    /**
     * Ensure the playlists file exists (copy from assets if needed)
     */
    private fun ensureFileExists() {
        if (!file.exists()) {
            copyFromAssets()
        }
    }

    /**
     * Copy default playlists from assets to internal storage
     */
    private fun copyFromAssets() {
        try {
            context.assets.open(fileName).use { input ->
                file.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
        } catch (e: Exception) {
            // If assets file doesn't exist, create empty catalog
            e.printStackTrace()
            val empty = JSONObject().apply {
                put("playlists", JSONArray())
            }
            file.writeText(empty.toString(2))
        }
    }

    /**
     * Parse playlists JSON
     */
    private fun parsePlaylistsJson(json: String): List<Playlist> {
        val playlists = mutableListOf<Playlist>()

        try {
            val root = JSONObject(json)
            val array = root.optJSONArray("playlists") ?: return emptyList()

            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)

                // Parse preview covers array
                val previewCovers = mutableListOf<String>()
                val previewArray = obj.optJSONArray("previewCovers")
                if (previewArray != null) {
                    for (j in 0 until previewArray.length()) {
                        previewCovers.add(previewArray.getString(j))
                    }
                }

                playlists.add(
                    Playlist(
                        id = obj.getString("id"),
                        title = obj.optString("name", "Unknown Playlist"),
                        description = obj.optString("description", ""),
                        coverUrl = obj.optString("coverUrl", ""),
                        previewCovers = previewCovers
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return playlists
    }

    /**
     * Reset catalog to default (from assets)
     */
    suspend fun resetToDefault() = withContext(Dispatchers.IO) {
        file.delete()
        copyFromAssets()
    }

    /**
     * Get the file path for manual editing
     */
    fun getFilePath(): String = file.absolutePath
}
