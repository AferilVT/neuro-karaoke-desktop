# Neuro Karaoke Player

A desktop wrapper application for [neurokaraoke.com](https://www.neurokaraoke.com) built with Electron.
All Credits go to the Website Creator "Soul". This is just a desktop version of the website with features

## Features

- **System Tray Integration**: Minimize to tray, single-click to show/hide
- **Media Key Support**: Control playback with your keyboard's media keys
- **Song Title Detection**: Shows current song in window title and tray tooltip
- **Discord Rich Presence**: Shows your current song as a Activity in discord (Requires login with discord)
  
## Installation

### Windows

Download and run `Neuro.Karaoke.Setup.exe` from the [latest release](../../releases/latest).

### macOS

Download `Neuro.Karaoke.Player.Setup.dmg` from the [latest release](../../releases/latest), open it, and drag the app to your Applications folder.

or

Download `Neuro.Karaoke.Player.Setup.Apple.Silicon.zip` from [latest release](../../releases/latest), unzip it, and drag the `.app` to your Applications folder

> **Note:** The app is unsigned. On first launch, right-click the app and select "Open" to bypass Gatekeeper, or go to System Settings → Privacy & Security and click "Open Anyway".

### Linux

#### Debian/Ubuntu
Download `Neuro.Karaoke.Player-x86_64.deb` or `arm64.deb` from the [latest release](../../releases/latest), then:
```bash
sudo dpkg -i Neuro.Karaoke.Player-x86_64.deb
```

or

```bash
sudo dpkg -i Neuro.Karaoke.Player-arm64.deb
```

#### Fedora/RHEL
Download `Neuro.Karaoke.Player-x86_64.rpm` or `arm64.rpm` from the [latest release](../../releases/latest), then:
```bash
sudo rpm -i Neuro.Karaoke.Player-x86_64.rpm
```

or

```bash
sudo rpm -i Neuro.Karaoke.Player-arm64.rpm
```

#### Other Distributions
Download `Neuro.Karaoke.Player-x86_64.AppImage` or `arm64.AppImage` from the [latest release](../../releases/latest), then:
```bash
chmod +x Neuro.Karaoke.Player-x86_64.AppImage
./Neuro.Karaoke.Player-x86_64.AppImage
```

or

```bash
chmod +x Neuro.Karaoke.Player-arm64.AppImage
./Neuro.Karaoke.Player-arm64.AppImage
```

## Usage

1. Launch the application
2. Log in to your neurokaraoke.com account (Optional but required for RPC to work)
3. Use the tray icon to show/hide the window
4. Control playback with media keys or on-screen controls


## Development

### Prerequisites
- Node.js 18+
- npm or yarn

### Setup
```bash
# Install dependencies
npm install

# Run in development mode
npm start
```

### Building
```bash
# Build for Windows
npm run build:win

# Build for Linux (using Docker)
./scripts/build-linux-docker.sh

# Build for macOS
npm run build:mac
```

### Project Structure

| File | Description |
|------|-------------|
| `main.js` | Main Electron process (window management, IPC handling) |
| `preload.js` | Bridge script for secure renderer communication |
| `tray-manager.js` | System tray icon and menu logic |
| `discord-manager.js` | Discord Rich Presence integration |
| `neurokaraoke-api.js` | NeuroKaraoke Playback API client |
| `config.js` | Application configuration |
| `assets/` | Application icons and resources |
| `scripts/` | Build scripts (Linux Docker build) |lication icons

## License

MIT
