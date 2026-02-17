const { app, BrowserWindow, ipcMain, shell } = require('electron');
const path = require('path');
const config = require('./config');
const DiscordManager = require('./discord-manager');
const TrayManager = require('./tray-manager');
const NeuroKaraokeAPI = require('./neurokaraoke-api');

const isDev = !app.isPackaged;

// Application state
let mainWindow = null;
let isQuitting = false;
let trayAvailable = false;

// Managers
let discordManager = null;
let trayManager = null;
let apiClient = null;

// Current state
let currentPlaylistId = null;

// Set app ID for Windows taskbar grouping
app.setAppUserModelId(config.APP.ID);

/**
 * Get asset path (works in both dev and production)
 */
function getAssetPath(filename) {
  if (isDev) {
    return path.join(__dirname, 'assets', filename);
  }
  return path.join(process.resourcesPath, 'assets', filename);
}

/**
 * Create the main application window
 */
function createWindow() {
  mainWindow = new BrowserWindow({
    width: config.WINDOW.WIDTH,
    height: config.WINDOW.HEIGHT,
    minWidth: config.WINDOW.MIN_WIDTH,
    minHeight: config.WINDOW.MIN_HEIGHT,
    webPreferences: {
      nodeIntegration: false,
      contextIsolation: true,
      partition: config.APP.PARTITION,
      preload: path.join(__dirname, 'preload.js'),
      backgroundThrottling: false // Keep media session active when window is hidden
    },
    backgroundColor: config.WINDOW.BACKGROUND_COLOR,
    autoHideMenuBar: true,
    icon: getAssetPath('neurokaraoke.ico')
  });

  // Set custom user agent
  mainWindow.webContents.setUserAgent(config.APP.USER_AGENT);

  // Hide menu bar
  mainWindow.setMenuBarVisibility(false);

  // Load the site
  mainWindow.loadURL(config.URL.SITE);

  // Minimize to tray instead of closing (only if tray is available)
  mainWindow.on('close', (event) => {
    if (!isQuitting) {
      // On Linux without tray support, quit the app instead of hiding
      if (process.platform === 'linux' && !trayAvailable) {
        isQuitting = true;
        return; // Allow the close to proceed
      }
      event.preventDefault();
      mainWindow.hide();
    }
  });

  mainWindow.on('closed', () => {
    mainWindow = null;
  });

  // Capture song ID from the playCount API request the Blazor app makes
  mainWindow.webContents.session.webRequest.onCompleted(
    { urls: ['*://api.neurokaraoke.com/api/songs/playCount/*'] },
    (details) => {
      if (details.method !== 'PUT') return;
      const match = details.url.match(/\/playCount\/([0-9a-f-]{36})$/i);
      if (match) {
        const songId = match[1];
        const songUrl = `https://www.neurokaraoke.com/song/${songId}`;
        console.log('✓ Captured song ID from playCount:', songId);
        discordManager?.updateSongUrl(songUrl);
      }
    }
  );

  // Block navigation away from allowed hosts
  mainWindow.webContents.on('will-navigate', (event, url) => {
    const isAllowed = config.URL.ALLOWED_HOSTS.some(host => url.startsWith(host));
    if (!isAllowed) {
      event.preventDefault();
      shell.openExternal(url);
    }
  });

  // Open external links in default browser
  mainWindow.webContents.setWindowOpenHandler(({ url }) => {
    shell.openExternal(url);
    return { action: 'deny' };
  });

  return mainWindow;
}

/**
 * Setup IPC handlers for communication with preload script
 */
function setupIpcHandlers() {
  // Playlist ID updates
  ipcMain.on('playlist-id', async (_event, playlistId) => {
    if (playlistId !== currentPlaylistId) {
      currentPlaylistId = playlistId;
      console.log('Playlist changed to:', playlistId);

      // Fetch playlist data
      try {
        await apiClient.fetchPlaylist(playlistId);
        console.log('✓ Playlist data cached');
      } catch (error) {
        console.error('Failed to fetch playlist:', error);
      }
    }
  });

  // Song info updates
  ipcMain.on('update-song', async (_event, songInfo) => {
    if (!mainWindow || mainWindow.isDestroyed()) return;

    console.log('Received song info:', songInfo);

    if (songInfo && songInfo.title && songInfo.title.trim()) {
      const displayTitle = songInfo.artist
        ? `${songInfo.title} - ${songInfo.artist}`
        : songInfo.title;

      mainWindow.setTitle(`${displayTitle} - ${config.APP.NAME}`);
      trayManager?.updateTooltip(displayTitle);
      discordManager?.updateSong(songInfo.title, songInfo.artist);

      // Fetch metadata from API if we have a playlist
      if (currentPlaylistId && apiClient) {
        try {
          console.log('Fetching metadata for:', songInfo.title, '-', songInfo.artist || 'Unknown', 'playlist:', currentPlaylistId);
          const metadata = await apiClient.getCurrentSongMetadata(
            currentPlaylistId,
            songInfo.title,
            songInfo.artist
          );

          if (metadata) {
            console.log('✓ Got metadata from API:', metadata);

            // Update Discord with album art + credit
            if (metadata.artCredit) {
              discordManager?.updateAlbumArtCredit(metadata.artCredit);
            }
            if (metadata.coverArtUrl) {
              discordManager?.updateAlbumArt(metadata.coverArtUrl);
            }

            // Update artist if API has better info
            if (metadata.coverArtist && !songInfo.artist) {
              discordManager?.updateSong(songInfo.title, metadata.coverArtist);
            }
          }
          if (!metadata) {
            console.log('No metadata returned from API for', songInfo.title);
          }
        } catch (error) {
          console.error('Failed to get metadata from API:', error);
        }
      } else {
        console.log('No playlist ID yet; skipping metadata fetch for', songInfo.title);
      }
    } else {
      mainWindow.setTitle(config.APP.NAME);
      trayManager?.updateTooltip(config.APP.NAME);
      discordManager?.updateSong('', '');
    }
  });

  // Song URL updates (for Discord RPC button)
  ipcMain.on('song-url', (_event, url) => {
    discordManager?.updateSongUrl(url);
  });

  // Playback state updates
  ipcMain.on('playback-state', (_event, playing) => {
    discordManager?.updatePlaybackState(playing);
  });

  // Song duration updates
  ipcMain.on('song-duration', (_event, durationInSeconds) => {
    discordManager?.updateDuration(durationInSeconds);
  });

  // Song elapsed time updates
  ipcMain.on('song-elapsed', (_event, elapsedSeconds) => {
    discordManager?.updateElapsed(elapsedSeconds);
  });

  // Album art updates (from DOM)
  ipcMain.on('album-art', (_event, imageUrl) => {
    // Only use DOM album art if API didn't provide one
    if (imageUrl) {
      console.log('Received album art from DOM:', imageUrl);
      discordManager?.updateAlbumArt(imageUrl);
    }
  });
}

/**
 * Handle application quit
 */
function handleQuit() {
  isQuitting = true;
  app.quit();
}

/**
 * Initialize application
 */
async function initialize() {
  createWindow();

  // Create system tray (Linux and macOS use PNG, Windows uses ICO)
  const trayIconName = process.platform === 'win32'
    ? 'neurokaraoke.ico'
    : 'neurokaraoke.png';
  trayManager = new TrayManager(getAssetPath(trayIconName));
  try {
    trayManager.create(mainWindow, handleQuit);
    trayAvailable = trayManager.isAvailable();
    if (!trayAvailable) {
      console.warn('System tray is not available - app will quit on close');
    }
  } catch (error) {
    console.error('Failed to create tray icon:', error);
    trayAvailable = false;
  }

  // Initialize API client
  apiClient = new NeuroKaraokeAPI();

  // Setup IPC handlers
  setupIpcHandlers();

  // Initialize Discord RPC (non-blocking)
  discordManager = new DiscordManager(config.DISCORD_CLIENT_ID);
  // discordManager.setCustomStatusMessage("{song} by {artist}");
  discordManager.init().catch((error) => {
    console.error('Discord RPC initialization failed:', error);
  });
}

// App lifecycle events
app.whenReady().then(initialize);

app.on('window-all-closed', () => {
  // On macOS, keep running in tray (standard behavior)
  // On Linux without tray, quit the app
  // On Windows, keep running in tray
  if (process.platform === 'linux' && !trayAvailable) {
    app.quit();
  }
});

app.on('activate', () => {
  // On macOS, re-create window when dock icon is clicked
  if (BrowserWindow.getAllWindows().length === 0) {
    createWindow();
  } else if (mainWindow) {
    mainWindow.show();
  }
});

app.on('will-quit', () => {
  // Clean up managers
  discordManager?.destroy();
  trayManager?.destroy();
});

app.on('before-quit', () => {
  isQuitting = true;
});
