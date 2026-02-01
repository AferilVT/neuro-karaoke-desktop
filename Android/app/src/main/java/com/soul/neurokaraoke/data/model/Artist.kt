package com.soul.neurokaraoke.data.model

data class Artist(
    val id: String,
    val name: String,
    val imageUrl: String,
    val songCount: Int = 0,
    val songs: List<Song> = emptyList()
)
