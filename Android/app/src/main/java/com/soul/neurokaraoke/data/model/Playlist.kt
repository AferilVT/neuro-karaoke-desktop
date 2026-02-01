package com.soul.neurokaraoke.data.model

data class Playlist(
    val id: String,
    val title: String,
    val description: String = "",
    val coverUrl: String = "",
    val previewCovers: List<String> = emptyList(),
    val songs: List<Song> = emptyList(),
    val songCount: Int = 0,
    val isPublic: Boolean = true,
    val isNew: Boolean = false
)
