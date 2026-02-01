package com.soul.neurokaraoke.data

import com.soul.neurokaraoke.data.model.Artist
import com.soul.neurokaraoke.data.model.Playlist
import com.soul.neurokaraoke.data.model.Setlist
import com.soul.neurokaraoke.data.model.Singer
import com.soul.neurokaraoke.data.model.Song

object MockData {

    val sampleSongs = listOf(
        Song(
            id = "1",
            title = "God-ish",
            artist = "Pinocchio-P",
            coverUrl = "https://i.ytimg.com/vi/example1/maxresdefault.jpg",
            duration = 213000,
            singer = Singer.NEURO
        ),
        Song(
            id = "2",
            title = "KING",
            artist = "Kanaria",
            coverUrl = "https://i.ytimg.com/vi/example2/maxresdefault.jpg",
            duration = 195000,
            singer = Singer.EVIL
        ),
        Song(
            id = "3",
            title = "Villain",
            artist = "flower",
            coverUrl = "https://i.ytimg.com/vi/example3/maxresdefault.jpg",
            duration = 240000,
            singer = Singer.NEURO
        ),
        Song(
            id = "4",
            title = "Love Trial",
            artist = "40mP",
            coverUrl = "https://i.ytimg.com/vi/example4/maxresdefault.jpg",
            duration = 258000,
            singer = Singer.DUET
        ),
        Song(
            id = "5",
            title = "Alien Alien",
            artist = "Nayutalien",
            coverUrl = "https://i.ytimg.com/vi/example5/maxresdefault.jpg",
            duration = 180000,
            singer = Singer.NEURO
        ),
        Song(
            id = "6",
            title = "World is Mine",
            artist = "ryo",
            coverUrl = "https://i.ytimg.com/vi/example6/maxresdefault.jpg",
            duration = 274000,
            singer = Singer.EVIL
        ),
        Song(
            id = "7",
            title = "Rolling Girl",
            artist = "wowaka",
            coverUrl = "https://i.ytimg.com/vi/example7/maxresdefault.jpg",
            duration = 196000,
            singer = Singer.NEURO
        ),
        Song(
            id = "8",
            title = "Melt",
            artist = "ryo",
            coverUrl = "https://i.ytimg.com/vi/example8/maxresdefault.jpg",
            duration = 254000,
            singer = Singer.DUET
        ),
        Song(
            id = "9",
            title = "Senbonzakura",
            artist = "Kurousa-P",
            coverUrl = "https://i.ytimg.com/vi/example9/maxresdefault.jpg",
            duration = 240000,
            singer = Singer.NEURO
        ),
        Song(
            id = "10",
            title = "Two-Faced Lovers",
            artist = "wowaka",
            coverUrl = "https://i.ytimg.com/vi/example10/maxresdefault.jpg",
            duration = 187000,
            singer = Singer.EVIL
        ),
        Song(
            id = "11",
            title = "Ghost Rule",
            artist = "DECO*27",
            coverUrl = "https://i.ytimg.com/vi/example11/maxresdefault.jpg",
            duration = 210000,
            singer = Singer.NEURO
        ),
        Song(
            id = "12",
            title = "Blessing",
            artist = "halyosy",
            coverUrl = "https://i.ytimg.com/vi/example12/maxresdefault.jpg",
            duration = 285000,
            singer = Singer.DUET
        )
    )

    val recentSetlistSongs = sampleSongs.take(5)

    val trendingSongs = sampleSongs.shuffled().take(6)

    val madeForYouSongs = sampleSongs.shuffled().take(4)

    val sampleArtists = listOf(
        Artist(
            id = "1",
            name = "Pinocchio-P",
            imageUrl = "https://example.com/artists/pinocchio-p.jpg",
            songCount = 15
        ),
        Artist(
            id = "2",
            name = "DECO*27",
            imageUrl = "https://example.com/artists/deco27.jpg",
            songCount = 23
        ),
        Artist(
            id = "3",
            name = "wowaka",
            imageUrl = "https://example.com/artists/wowaka.jpg",
            songCount = 12
        ),
        Artist(
            id = "4",
            name = "ryo (supercell)",
            imageUrl = "https://example.com/artists/ryo.jpg",
            songCount = 18
        ),
        Artist(
            id = "5",
            name = "Kanaria",
            imageUrl = "https://example.com/artists/kanaria.jpg",
            songCount = 8
        ),
        Artist(
            id = "6",
            name = "Nayutalien",
            imageUrl = "https://example.com/artists/nayutalien.jpg",
            songCount = 10
        )
    )

    val samplePlaylists = listOf(
        Playlist(
            id = "1",
            title = "Neuro's Greatest Hits",
            description = "Top performed songs by Neuro-sama",
            coverUrl = "https://example.com/playlists/neuro-hits.jpg",
            songs = sampleSongs.filter { it.singer == Singer.NEURO },
            isNew = true
        ),
        Playlist(
            id = "2",
            title = "Evil's Dark Collection",
            description = "Evil Neuro's favorite songs",
            coverUrl = "https://example.com/playlists/evil-collection.jpg",
            songs = sampleSongs.filter { it.singer == Singer.EVIL },
            isNew = false
        ),
        Playlist(
            id = "3",
            title = "Twin Duets",
            description = "Neuro and Evil singing together",
            coverUrl = "https://example.com/playlists/duets.jpg",
            songs = sampleSongs.filter { it.singer == Singer.DUET },
            isNew = true
        ),
        Playlist(
            id = "4",
            title = "Vocaloid Classics",
            description = "Classic vocaloid songs covered by Neuro",
            coverUrl = "https://example.com/playlists/vocaloid.jpg",
            songs = sampleSongs.take(8),
            isNew = false
        ),
        Playlist(
            id = "5",
            title = "High Energy",
            description = "Upbeat songs for karaoke sessions",
            coverUrl = "https://example.com/playlists/energy.jpg",
            songs = sampleSongs.shuffled().take(6),
            isNew = true
        ),
        Playlist(
            id = "6",
            title = "Community Favorites",
            description = "Most requested songs by the community",
            coverUrl = "https://example.com/playlists/community.jpg",
            songs = sampleSongs.shuffled().take(7),
            isNew = false
        )
    )

    val sampleSetlists = listOf(
        Setlist(
            id = "1",
            title = "January 2026 Karaoke Stream",
            date = "2026-01-15",
            songs = sampleSongs.take(8),
            coverImages = listOf(
                "https://example.com/setlists/jan2026-1.jpg",
                "https://example.com/setlists/jan2026-2.jpg"
            )
        ),
        Setlist(
            id = "2",
            title = "New Year Special 2026",
            date = "2026-01-01",
            songs = sampleSongs.shuffled().take(10),
            coverImages = listOf(
                "https://example.com/setlists/ny2026-1.jpg",
                "https://example.com/setlists/ny2026-2.jpg"
            )
        ),
        Setlist(
            id = "3",
            title = "December 2025 Karaoke",
            date = "2025-12-20",
            songs = sampleSongs.shuffled().take(7),
            coverImages = listOf(
                "https://example.com/setlists/dec2025-1.jpg"
            )
        ),
        Setlist(
            id = "4",
            title = "Winter Special 2025",
            date = "2025-12-10",
            songs = sampleSongs.shuffled().take(9),
            coverImages = listOf(
                "https://example.com/setlists/winter2025-1.jpg",
                "https://example.com/setlists/winter2025-2.jpg"
            )
        )
    )

    val topGenres = listOf(
        "J-Pop",
        "Vocaloid",
        "Anime",
        "Rock",
        "Electronic",
        "Ballad"
    )

    data class CoverDistribution(
        val label: String,
        val count: Int,
        val total: Int
    )

    val coverDistribution = listOf(
        CoverDistribution("Neuro", 45, 100),
        CoverDistribution("Evil", 35, 100),
        CoverDistribution("Duet", 20, 100)
    )
}
