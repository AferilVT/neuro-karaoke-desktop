package com.soul.neurokaraoke.data.model

data class Setlist(
    val id: String,
    val title: String,
    val date: String,
    val songs: List<Song> = emptyList(),
    val coverImages: List<String> = emptyList()
)
