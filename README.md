# Neuro Karaoke Desktop

A simple desktop wrapper for [neurokaraoke.com](https://www.neurokaraoke.com) built with Electron.

## Features

-  Full Neuro Karaoke website in a native desktop window
-  Discord login works seamlessly
-  Persistent login sessions (stays logged in)
-  Clean UI without menu bars
-  Cross-platform (Windows, macOS, Linux)

## Installation

### Windows

1. **Download the installer** from the [Releases](https://github.com/YOUR_USERNAME/neuro-karaoke-desktop/releases) page
2. Download `Neuro.Karaoke.Setup.exe`
3. **Run the installer**
4. Follow the installation wizard
5. Launch "Neuro Karaoke" from your Start Menu or Desktop shortcut

### Linux

1. **Download the AppImage** from the [Releases](https://github.com/YOUR_USERNAME/neuro-karaoke-desktop/releases) page
2. Download `Neuro-Karaoke-1.0.0.AppImage`
3. **Make it executable**:
   ```bash
   chmod +x Neuro-Karaoke-1.0.0.AppImage
   ```
4. **Run the app**:
   ```bash
   ./Neuro-Karaoke-1.0.0.AppImage
   ```

### macOS

1. **Download the DMG** from the [Releases](https://github.com/YOUR_USERNAME/neuro-karaoke-desktop/releases) page
2. Open the DMG file
3. Drag "Neuro Karaoke" to your Applications folder
4. Launch from Applications

## Quick Start (Development)

### Install Dependencies

```bash
npm install
```

### Run the App

```bash
npm start
```

### Build for Windows

```bash
npm run build:win
```

The installer will be created in the `dist` folder.

## What It Does

This app simply wraps the Neuro Karaoke website in a desktop window, providing a native app experience while using the full functionality of the website including:

- Discord authentication
- Song browsing and playback
- Playlists and favorites
- All website features

## Project Structure

```
Neuro Karaoke/
├── main.js          # Main Electron process
├── preload.js       # Preload script
├── package.json     # Dependencies and build config
├── assets/          # App icons
├── .gitignore       # Git ignore rules
└── README.md        # This file
```

## Development

To enable DevTools, uncomment this line in `main.js`:

```javascript
mainWindow.webContents.openDevTools();
```

## Building

The app uses `electron-builder` for packaging:

- **Windows**: `npm run build:win` creates an installer
- **macOS**: `npm run build:mac` creates a DMG
- **Linux**: `npm run build:linux` creates an AppImage/deb/rpm

## License

This is a wrapper application. Neuro Karaoke content and branding belong to their respective owners.

## Credits

Built with [Electron](https://www.electronjs.org/)

Original website: [Neuro-Karaoke](https://neurokaraoke.com)
