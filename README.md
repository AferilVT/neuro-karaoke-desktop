# Neuro Karaoke Player

![Page Views](https://count.getloli.com/get/@aferilvt:neuro-karaoke-wrapper?theme=booru-jaypee)

A cross-platform karaoke player for [neurokaraoke.com](https://neurokaraoke.com) — available as a desktop app (Electron) and an Android app (Jetpack Compose).
All credits go to the website creator "Soul". These are companion apps for the website with extra features.

Check [wiki](https://github.com/AferilVT/neuro-karaoke-wrapper.wiki) for API documentation

## Features

### Desktop (Electron)
- **System Tray Integration** — Minimize to tray, single-click to show/hide
- **Media Key Support** — Control playback with your keyboard's media keys
- **Always on Top** — Pin the window above other applications
- **Song Title Detection** — Shows current song in window title and tray tooltip
- **Discord Rich Presence** — Shows your current song as an activity in Discord (requires login with Discord)

### Android
- **Browse Setlists & Playlists** — Grid view with 2x2 cover previews, detailed playlist screens
- **Search All Songs** — Search across all playlists with real-time results
- **Media Playback** — Background playback with lock screen controls and media notifications
- **Queue Management** — View and interact with the playback queue
- **Synced Lyrics** — Auto-scrolling lyrics from NeuroKaraoke API with lrclib.net fallback
- **Audio Caching** — 500MB disk cache for smooth offline-capable playback
- **Equalizer & Bass Boost** — 5-band EQ with presets (Normal, Bass, Rock, Pop, Jazz, Classical)
- **User Playlists** — Create custom playlists, add/remove songs, play all or shuffle
- **Artists Browser** — Browse songs by original artist, sorted by cover count, with Last.fm images
- **Playback Persistence** — Remembers last played song and position across app restarts
- **Theme Support** — Neuro (cyan), Evil (pink), Duet (purple), and Auto (switches based on current song)
- **Discord Sign-in** — OAuth2 flow prepared (pending backend integration)

## Installation

### Windows
Download and run `Neuro.Karaoke.Setup.exe` from the [latest release](../../releases/latest).

### macOS
Download `Neuro.Karaoke.Player.Setup.dmg` from the [latest release](../../releases/latest), open it, and drag the app to your Applications folder.

Apple Silicon users can also download `Neuro.Karaoke.Player.Setup.Apple.Silicon.zip`.

> **Note:** The app is unsigned. On first launch, right-click the app and select "Open" to bypass Gatekeeper, or go to System Settings → Privacy & Security and click "Open Anyway".

### Linux

Available for both **x86_64** and **arm64** architectures. Download from the [latest release](../../releases/latest).

#### Debian/Ubuntu
```bash
# x86_64
sudo dpkg -i Neuro.Karaoke.Player-x86_64.deb

# arm64
sudo dpkg -i Neuro.Karaoke.Player-arm64.deb
```

#### Fedora/RHEL
```bash
# x86_64
sudo rpm -i Neuro.Karaoke.Player-x86_64.rpm

# arm64
sudo rpm -i Neuro.Karaoke.Player-arm64.rpm
```

#### Other Distributions
```bash
# x86_64
chmod +x Neuro.Karaoke.Player-x86_64.AppImage
./Neuro.Karaoke.Player-x86_64.AppImage

# arm64
chmod +x Neuro.Karaoke.Player-arm64.AppImage
./Neuro.Karaoke.Player-arm64.AppImage
```

### Android
Download `Neuro.Karaoke.Player.apk` from the [latest release](../../releases/latest) and install it on your device.

## Usage

1. Launch the application
2. Log in to your neurokaraoke.com account (optional but required for Discord RPC to work)
3. On desktop, use the tray icon to show/hide the window
4. Control playback with media keys or on-screen controls

## Tech Stack

### Desktop
- **Electron** — Desktop app framework
- **Node.js** — Runtime
- **discord-rpc** — Discord Rich Presence integration

### Android
- **Kotlin** — Primary language
- **Jetpack Compose** — UI toolkit with Material3
- **Media3 / ExoPlayer** — Audio playback with MediaSession
- **MediaSessionService** — Background playback and notification controls
- **Coil** — Image loading
- **Jetpack Navigation** — Screen navigation
- **Coroutines & Flow** — Asynchronous programming
- **Android AudioFX** — Equalizer and BassBoost effects

## Development

### Desktop

#### Prerequisites
- Node.js 18+
- npm or yarn

#### Setup
```bash
# Install dependencies
npm install

# Run in development mode
npm start
```

### Android

#### Prerequisites
- Android Studio
- Android device or emulator

#### Setup
1. Open the project in Android Studio
2. Connect a device or start an emulator
3. Click **Run**, or build from the command line:
   ```bash
   ./gradlew assembleDebug
   ```

## Building

### Desktop
```bash
# Build for Windows
npm run build:win

# Build for Linux (using Docker)
./scripts/build-linux-docker.sh

# Build for macOS
npm run build:mac
```

### Android
```bash
./gradlew assembleDebug
```

## Project Structure

### Desktop (Electron)

| File | Description |
|------|-------------|
| `main.js` | Main Electron process (window management, IPC handling) |
| `preload.js` | Bridge script for secure renderer communication |
| `tray-manager.js` | System tray icon and menu logic |
| `discord-manager.js` | Discord Rich Presence integration |
| `neurokaraoke-api.js` | NeuroKaraoke Playback API client |
| `config.js` | Application configuration |
| `assets/` | Application icons and resources |
| `scripts/` | Build scripts (Linux Docker build) |

## License

MIT
