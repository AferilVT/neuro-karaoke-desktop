# Neuro Karaoke Desktop

A simple desktop wrapper for [neurokaraoke.com](https://www.neurokaraoke.com) built with Electron.

## Features

- 🎤 Full Neuro Karaoke website in a native desktop window
- 🔐 Discord login works seamlessly
- 💾 Persistent login sessions (stays logged in)
- 🎨 Clean UI without menu bars
- 🖥️ Cross-platform (Windows, macOS, Linux)

## Quick Start

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
