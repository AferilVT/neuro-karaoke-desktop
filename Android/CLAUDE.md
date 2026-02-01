# Neuro Karaoke Project Overview

This document provides a comprehensive overview of the Neuro Karaoke Android application, including its structure, key technologies, and instructions on how to build and run the app.

## High-Level Description

The Neuro Karaoke app is a music player application that allows users to browse and play karaoke songs from the Neuro Karaoke website (neurokaraoke.com). It features a modern user interface built with Jetpack Compose and uses ExoPlayer (Media3) for audio playback with media notification support. The app connects to the Neuro Karaoke API to fetch playlists and songs.

## Package Name

`com.soul.neurokaraoke`

## Project Structure

The project is a single-module Android application with the following structure:

-   **`app` module:** The main application module.
    -   **`src/main/java/com/soul/neurokaraoke`:** The root package for the application's source code.
        -   **`MainActivity.kt`:** The main entry point of the application.
        -   **`data`:** Contains the data layer of the application.
            -   **`api/NeuroKaraokeApi.kt`:** Handles communication with the Neuro Karaoke API.
            -   **`api/LyricsApi.kt`:** Fetches lyrics from lrclib.net API.
            -   **`model/`:** Defines the data models:
                -   `Song.kt` - Song data with Singer enum (NEURO, EVIL, DUET, OTHER)
                -   `Playlist.kt` - Playlist with id, title, coverUrl, previewCovers
                -   `User.kt` - Discord user data for authentication
            -   **`repository/`:** Manages data sources:
                -   `SongRepository.kt` - Song data management
                -   `AuthRepository.kt` - Discord OAuth authentication
                -   `UserPlaylistRepository.kt` - User-created playlists (local storage)
                -   `ArtistImageRepository.kt` - Maps artist names to Last.fm image URLs
            -   **`PlaylistCatalog.kt`:** Manages local playlist catalog.
        -   **`audio`:** Audio processing components.
            -   **`AudioCacheManager.kt`:** Manages 500MB audio cache for offline/smooth playback.
            -   **`EqualizerManager.kt`:** Equalizer and bass boost controls.
        -   **`navigation`:** Defines the navigation graph and screens.
        -   **`service`:**
            -   **`MediaPlaybackService.kt`:** Background media service with notification support and audio caching.
        -   **`ui`:** Contains the UI layer.
            -   **`MainScreen.kt`:** Main screen with navigation drawer, scaffold, and mini player.
            -   **`components/`:** Reusable UI components.
            -   **`screens/`:** The different screens:
                -   `home/HomeScreen.kt` - Main home with Cover Distribution & Top Genres
                -   `setlist/SetlistScreen.kt` - Playlist grid with 2x2 cover previews
                -   `setlist/SetlistDetailScreen.kt` - Detailed playlist view (website-style)
                -   `player/PlayerScreen.kt` - Full player with lyrics, equalizer, queue
                -   `search/SearchScreen.kt` - Search across ALL playlists
                -   `library/FavoritesScreen.kt` - Favorites with Discord sign-in
                -   `library/PlaylistsScreen.kt` - User-created playlists
            -   **`theme/`:** Theme colors and styling (Neuro/Evil themes).
        -   **`viewmodel/`:**
            -   `PlayerViewModel.kt` - Manages player state via MediaController
            -   `AuthViewModel.kt` - Manages Discord authentication

## Key Technologies and Libraries

-   **Kotlin:** The primary programming language
-   **Jetpack Compose:** Modern Android UI toolkit
-   **ExoPlayer (Media3):** Audio playback with MediaSession support
-   **MediaSessionService:** Background playback with notification controls
-   **Media3 DataSource:** Audio caching with SimpleCache and CacheDataSource
-   **Coil:** Image loading library
-   **Jetpack Navigation:** Screen navigation
-   **Coroutines and Flow:** Async programming
-   **Android AudioFX:** Equalizer and BassBoost effects

## API Integration

- **Base URL:** `https://idk.neurokaraoke.com`
- **Endpoints:**
  - `GET /public/playlist/{playlistId}` - Returns playlist info and songs
- **Storage URL:** `https://storage.neurokaraoke.com` - Audio and images
- **Lyrics API:** `https://lrclib.net/api/search` - Synced lyrics fetching

## Features Implemented

### 1. Search All Songs
- Search screen loads songs from ALL playlists (not just current)
- Shows loading progress as songs are fetched
- Results update in real-time

### 2. Media Notifications
- Background playback via `MediaPlaybackService`
- Lock screen controls
- Notification with play/pause, next, previous
- Album art displayed in notification

### 3. Queue System
- View Queue button in full player
- Shows "Now Playing" and "Up Next" sections
- Tap songs in queue to play them

### 4. Lyrics (lrclib.net)
- Fetches synced lyrics from lrclib.net API
- Auto-scrolls to current lyric line during playback
- Falls back to plain lyrics if synced not available
- Shows loading/error/not found states

### 5. Audio Caching (Spotify-like)
- 500MB disk cache for audio files
- Aggressive buffering: 60s min, 3 min max buffer
- 30 second back-buffer for rewinding
- Seamless playback even with connection drops

### 6. Equalizer & Bass Boost
- 5-band equalizer with presets (Normal, Bass, Rock, Pop, Jazz, Classical)
- Bass boost with adjustable strength
- Settings persist across sessions

### 7. Setlist Detail Screen
- Website-style playlist detail view
- Blurred background with cover art
- 2x2 cover grid, playlist title, song count, duration
- Play, Shuffle, Favorite buttons
- Scrollable song list

### 8. User Playlists
- Create custom playlists with name and cover image
- Delete playlists
- Stored locally via SharedPreferences

### 9. Theme Support
- **Neuro theme** (cyan/teal accent)
- **Evil theme** (pink/magenta gradient)
- **Duet theme** (amethyst purple)
- **Auto theme** - Automatically switches based on current song's singer
- All screens use `MaterialTheme.colorScheme.primary` for consistency
- Theme selector in navigation drawer with 4 options: Auto, Neuro, Evil, Duet

### 12. Artists Screen
- Browse songs by original artist
- Artists derived dynamically from song catalog
- Sorted by song count (most covered artists first)
- Artist detail screen shows all songs by that artist
- Artist images from Last.fm CDN via `ArtistImageRepository`

### 13. Artist Images
- `ArtistImageRepository.kt` maps artist names to Last.fm image URLs
- Falls back to song cover art if no artist image found
- **Verified working images:** Pinocchio-P, DECO*27, Kanaria, wowaka, Hachi, livetune, Eve, Maretu, Kikuo, Mitchie M, Reol, supercell, Ado
- **To add more artists:** Add entries to the `artistImages` map:
  ```kotlin
  "artist name" to "https://lastfm.freetls.fastly.net/i/u/300x300/[image_hash].jpg"
  ```
- Find image hashes by visiting `https://www.last.fm/music/[ArtistName]` and inspecting the profile image URL

### 10. Discord Sign-in (Prepared)
- Discord OAuth2 flow set up
- Sign-in button in navigation drawer
- User profile display when logged in
- Note: Backend integration needed to complete OAuth token exchange

### 11. Favorites (Prepared)
- Shows sign-in prompt for non-authenticated users
- Ready to display favorites when synced from server
- Heart icon on player (needs backend integration)

## Hardcoded Stats (Pending Database Access)

**Cover Distribution (Total: 1267):**
- Neuro V3: 516
- Evil: 424
- Duet: 174
- Other: 153

**Top Genres:**
- Electronic: 402
- J-Pop: 363
- Alternative Rock: 278
- Vocaloid: 264
- Pop: 262
- Rock: 184
- Anime: 149
- Pop Rock: 139

## Building and Running the App

1.  Open the project in Android Studio.
2.  Connect a device or start an emulator.
3.  Click the "Run" button in the toolbar.

## Key Files for Common Tasks

- **Playback control:** `viewmodel/PlayerViewModel.kt`
- **Media service:** `service/MediaPlaybackService.kt`
- **Audio caching:** `audio/AudioCacheManager.kt`
- **Equalizer:** `audio/EqualizerManager.kt`
- **Lyrics:** `data/api/LyricsApi.kt`
- **API calls:** `data/api/NeuroKaraokeApi.kt`
- **Authentication:** `data/repository/AuthRepository.kt`, `viewmodel/AuthViewModel.kt`
- **User playlists:** `data/repository/UserPlaylistRepository.kt`
- **Setlist grid:** `ui/screens/setlist/SetlistScreen.kt`
- **Setlist detail:** `ui/screens/setlist/SetlistDetailScreen.kt`
- **Home screen:** `ui/screens/home/HomeScreen.kt`
- **Full player:** `ui/screens/player/PlayerScreen.kt`
- **Theme colors:** `ui/theme/Color.kt`
- **Navigation:** `navigation/Screen.kt`, `navigation/NavGraph.kt`

## TODO / Pending Work

1. **Discord OAuth Backend Integration:** The OAuth flow opens Discord authorization, but the token exchange requires backend support at `neurokaraoke.com/signin-discord`

2. **Favorites Sync:** Once authentication works, favorites can be synced from the server

3. **Dynamic Stats:** Replace hardcoded Cover Distribution and Top Genres with actual database values when available

4. **Android Auto & TV Support:** Prepared but disabled for now - requires additional testing and MediaLibraryService implementation

5. **Add Songs to User Playlists:** UI exists but functionality to add songs to custom playlists not yet implemented
