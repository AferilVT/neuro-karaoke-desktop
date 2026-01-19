# Setup Guide

## Quick Start

1. **Install Node.js** (if not already installed)
   - Download from https://nodejs.org/
   - Choose the LTS version
   - Verify installation: `node --version`

2. **Install Dependencies**
   ```bash
   npm install
   ```

3. **Run the Application**
   ```bash
   npm start
   ```

The app will open and load the Neuro Karaoke website.

## Building for Windows

1. Install dependencies (if not done):
   ```bash
   npm install
   ```

2. Build the Windows installer:
   ```bash
   npm run build:win
   ```

3. Find the installer in the `dist` folder:
   - `Neuro Karaoke Setup X.X.X.exe` - Installer
   - You can distribute this file to other Windows users

## Building for Other Platforms

### macOS
```bash
npm run build:mac
```

### Linux
```bash
npm run build:linux
```

## Customization

### Change the App Icon

1. Create or download icons:
   - Windows: `.ico` file (256x256 or larger)
   - macOS: `.icns` file
   - Linux: `.png` file (512x512)

2. Place the files in the `assets` folder

3. Update `package.json` build configuration if needed

4. Rebuild the application

### Enable Developer Tools

In `main.js`, uncomment this line:

```javascript
mainWindow.webContents.openDevTools();
```

## Troubleshooting

### Application won't start
- Make sure Node.js is installed: `node --version`
- Try reinstalling dependencies: `rm -rf node_modules && npm install`
- Check for error messages in the console

### Build fails
- Make sure you have enough disk space
- Try clearing the cache: `npm cache clean --force`
- On Windows, you might need to run as Administrator

### Website won't load
- Check your internet connection
- Verify neurokaraoke.com is accessible in your browser
- Check firewall settings

## Development Tips

- Press `Alt` to temporarily show the menu bar
- The app remembers your login state between sessions
- To clear saved login: Delete the app data folder
  - Windows: `%APPDATA%\neuro-karaoke`
  - macOS: `~/Library/Application Support/neuro-karaoke`
  - Linux: `~/.config/neuro-karaoke`
