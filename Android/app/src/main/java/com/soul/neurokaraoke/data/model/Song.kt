package com.soul.neurokaraoke.data.model

enum class Singer {
    NEURO,
    EVIL,
    DUET,
    OTHER
}

data class Song(
    val id: String,
    val title: String,
    val artist: String,
    val coverUrl: String,
    val audioUrl: String = "",
    val duration: Long = 0L,
    val singer: Singer = Singer.NEURO,
    val artCredit: String? = null
) {
    /** The cover artist (who performed this cover) based on singer */
    val coverArtist: String
        get() = when (singer) {
            Singer.NEURO -> "Neuro-sama"
            Singer.EVIL -> "Evil Neuro"
            Singer.DUET -> "Neuro & Evil"
            Singer.OTHER -> "Neuro-sama"
        }
}
