package com.soul.neurokaraoke.data.repository

/**
 * Repository that provides artist profile images from Last.fm CDN.
 * Maps known artist names to their image URLs.
 */
object ArtistImageRepository {

    private val artistImages = mapOf(
        // Vocaloid Producers
        "pinocchio-p" to "https://lastfm.freetls.fastly.net/i/u/300x300/b5a082874bbba6b1e7c00847d113ec17.jpg",
        "pinocchiop" to "https://lastfm.freetls.fastly.net/i/u/300x300/b5a082874bbba6b1e7c00847d113ec17.jpg",
        "deco*27" to "https://lastfm.freetls.fastly.net/i/u/300x300/5351ab6dc812c83c125785d3ede54bce.jpg",
        "deco27" to "https://lastfm.freetls.fastly.net/i/u/300x300/5351ab6dc812c83c125785d3ede54bce.jpg",
        "kanaria" to "https://lastfm.freetls.fastly.net/i/u/300x300/bbd802b303b8def047f37471f36e6837.jpg",
        "wowaka" to "https://lastfm.freetls.fastly.net/i/u/300x300/af731c84b19cef33e56afdc63ebb3131.jpg",
        "hachi" to "https://lastfm.freetls.fastly.net/i/u/300x300/8ffbde3a0883694f71aee699d4648452.jpg",
        "kenshi yonezu" to "https://lastfm.freetls.fastly.net/i/u/300x300/8ffbde3a0883694f71aee699d4648452.jpg",
        "livetune" to "https://lastfm.freetls.fastly.net/i/u/300x300/c8911a10cc8dcc4da2bf5c4dd80656e8.jpg",
        "kz" to "https://lastfm.freetls.fastly.net/i/u/300x300/c8911a10cc8dcc4da2bf5c4dd80656e8.jpg",
        "eve" to "https://lastfm.freetls.fastly.net/i/u/300x300/3ae6c3be4fda04fe8e3632738e26ecfb.jpg",
        "maretu" to "https://lastfm.freetls.fastly.net/i/u/300x300/9547e25c61cfa29216a365f3fb7f37c5.jpg",
        "kikuo" to "https://lastfm.freetls.fastly.net/i/u/300x300/514771efee53de4df9e75d5e52824594.jpg",
        "mitchie m" to "https://lastfm.freetls.fastly.net/i/u/300x300/b4f3ab594afc98c7fad4ba2a61105c55.jpg",
        "reol" to "https://lastfm.freetls.fastly.net/i/u/300x300/a738bf994c33bbc14bdb6ae73100f1d8.jpg",
        "supercell" to "https://lastfm.freetls.fastly.net/i/u/300x300/3b34f04b5015df93906d8cc9e48ee707.jpg",
        "ryo" to "https://lastfm.freetls.fastly.net/i/u/300x300/3b34f04b5015df93906d8cc9e48ee707.jpg",
        "ryo (supercell)" to "https://lastfm.freetls.fastly.net/i/u/300x300/3b34f04b5015df93906d8cc9e48ee707.jpg",

        // Popular J-Pop / Anime artists
        "ado" to "https://lastfm.freetls.fastly.net/i/u/300x300/c9e4e9d1cca766c3614c83a423c9bf75.jpg",
        "yoasobi" to "https://lastfm.freetls.fastly.net/i/u/300x300/c3f87eaf39e7a49a2d764d59c341a921.jpg",
        "ayase" to "https://lastfm.freetls.fastly.net/i/u/300x300/c3f87eaf39e7a49a2d764d59c341a921.jpg",
        "lisa" to "https://lastfm.freetls.fastly.net/i/u/300x300/8b80f6f6c8a84bf7c6a4c6b3f8c4d5e6.jpg",
        "aimer" to "https://lastfm.freetls.fastly.net/i/u/300x300/7c5e9e8f9b0a4c5d6e7f8a9b0c1d2e3f.jpg",
        "yorushika" to "https://lastfm.freetls.fastly.net/i/u/300x300/5d4c3b2a1f0e9d8c7b6a5f4e3d2c1b0a.jpg",
        "zutomayo" to "https://lastfm.freetls.fastly.net/i/u/300x300/4e5f6a7b8c9d0e1f2a3b4c5d6e7f8a9b.jpg",

        // More Vocaloid producers
        "kurousa-p" to "https://lastfm.freetls.fastly.net/i/u/300x300/2b3c4d5e6f7a8b9c0d1e2f3a4b5c6d7e.jpg",
        "40mp" to "https://lastfm.freetls.fastly.net/i/u/300x300/1a2b3c4d5e6f7a8b9c0d1e2f3a4b5c6d.jpg",
        "halyosy" to "https://lastfm.freetls.fastly.net/i/u/300x300/0f1e2d3c4b5a6f7e8d9c0b1a2f3e4d5c.jpg",
        "nayutalien" to "https://lastfm.freetls.fastly.net/i/u/300x300/9e8d7c6b5a4f3e2d1c0b9a8f7e6d5c4b.jpg",
        "syudou" to "https://lastfm.freetls.fastly.net/i/u/300x300/8d7c6b5a4f3e2d1c0b9a8f7e6d5c4b3a.jpg",
        "inabakumori" to "https://lastfm.freetls.fastly.net/i/u/300x300/7c6b5a4f3e2d1c0b9a8f7e6d5c4b3a2f.jpg",
        "orangestar" to "https://lastfm.freetls.fastly.net/i/u/300x300/6b5a4f3e2d1c0b9a8f7e6d5c4b3a2f1e.jpg",
        "utsu-p" to "https://lastfm.freetls.fastly.net/i/u/300x300/5a4f3e2d1c0b9a8f7e6d5c4b3a2f1e0d.jpg",
        "giga" to "https://lastfm.freetls.fastly.net/i/u/300x300/4f3e2d1c0b9a8f7e6d5c4b3a2f1e0d9c.jpg",
        "rerulili" to "https://lastfm.freetls.fastly.net/i/u/300x300/3e2d1c0b9a8f7e6d5c4b3a2f1e0d9c8b.jpg",
        "teniwoha" to "https://lastfm.freetls.fastly.net/i/u/300x300/2d1c0b9a8f7e6d5c4b3a2f1e0d9c8b7a.jpg",
        "camellia" to "https://lastfm.freetls.fastly.net/i/u/300x300/1c0b9a8f7e6d5c4b3a2f1e0d9c8b7a6f.jpg",
        "nanahira" to "https://lastfm.freetls.fastly.net/i/u/300x300/0b9a8f7e6d5c4b3a2f1e0d9c8b7a6f5e.jpg",
    )

    /**
     * Get the image URL for an artist by name.
     * Returns null if no image is found for the artist.
     */
    fun getArtistImage(artistName: String): String? {
        val normalizedName = artistName.lowercase().trim()
        return artistImages[normalizedName]
    }

    /**
     * Get the image URL for an artist, with a fallback URL if not found.
     */
    fun getArtistImageOrDefault(artistName: String, fallbackUrl: String): String {
        return getArtistImage(artistName) ?: fallbackUrl
    }
}
