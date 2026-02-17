const { Tray, Menu, app, BrowserWindow } = require('electron');

/**
 * Manages system tray icon and menu
 */
class TrayManager {
  constructor(iconPath) {
    this.iconPath = iconPath;
    this.tray = null;
    this.mainWindow = null;
    this.onQuit = null;
    this.sleepTimer = null;
    this.sleepEndTime = null;
    this.sleepTickInterval = null;
  }

  /**
   * Create the system tray icon
   */
  create(mainWindow, onQuit) {
    this.mainWindow = mainWindow;
    this.onQuit = onQuit;
    this.tray = new Tray(this.iconPath);
    this.tray.setToolTip('Neuro Karaoke');

    this.rebuildMenu();

    // Single click to toggle window visibility (Windows style)
    this.tray.on('click', () => this.toggleWindow());

    // Double click to show and focus
    this.tray.on('double-click', () => this.showWindow());

    return this.tray;
  }

  /**
   * Build/rebuild the tray context menu
   */
  rebuildMenu() {
    const sleepActive = this.sleepTimer !== null;
    const remainingLabel = sleepActive ? this.formatRemaining() : null;

    const sleepSubmenu = [
      { label: '15 minutes', click: () => this.startSleepTimer(15) },
      { label: '30 minutes', click: () => this.startSleepTimer(30) },
      { label: '45 minutes', click: () => this.startSleepTimer(45) },
      { label: '1 hour', click: () => this.startSleepTimer(60) },
      { label: '2 hours', click: () => this.startSleepTimer(120) },
      { type: 'separator' },
      { label: 'Custom...', click: () => this.promptCustomSleepTimer() },
    ];

    if (sleepActive) {
      sleepSubmenu.push(
        { type: 'separator' },
        { label: `${remainingLabel} remaining`, enabled: false },
        { label: 'Cancel timer', click: () => this.cancelSleepTimer() }
      );
    }

    const contextMenu = Menu.buildFromTemplate([
      {
        label: 'Open',
        click: () => this.showWindow()
      },
      {
        label: sleepActive ? `Sleep Timer (${remainingLabel})` : 'Sleep Timer',
        submenu: sleepSubmenu
      },
      { type: 'separator' },
      {
        label: 'Exit',
        click: () => this.onQuit()
      }
    ]);

    this.tray.setContextMenu(contextMenu);
  }

  /**
   * Start a sleep timer
   */
  startSleepTimer(minutes) {
    this.cancelSleepTimer();

    this.sleepEndTime = Date.now() + (minutes * 60 * 1000);
    console.log(`Sleep timer set for ${minutes} minutes`);

    this.sleepTimer = setTimeout(() => {
      console.log('Sleep timer expired - pausing playback');
      this.pausePlayback();
      this.sleepTimer = null;
      this.sleepEndTime = null;
      this.stopTickInterval();
      this.rebuildMenu();
    }, minutes * 60 * 1000);

    // Update menu every 60 seconds to show remaining time
    this.sleepTickInterval = setInterval(() => {
      this.rebuildMenu();
    }, 60000);

    this.rebuildMenu();
  }

  /**
   * Cancel the sleep timer
   */
  cancelSleepTimer() {
    if (this.sleepTimer) {
      clearTimeout(this.sleepTimer);
      this.sleepTimer = null;
      this.sleepEndTime = null;
      console.log('Sleep timer cancelled');
    }
    this.stopTickInterval();
    this.rebuildMenu();
  }

  /**
   * Stop the tick interval
   */
  stopTickInterval() {
    if (this.sleepTickInterval) {
      clearInterval(this.sleepTickInterval);
      this.sleepTickInterval = null;
    }
  }

  /**
   * Format remaining time as a human-readable string
   */
  formatRemaining() {
    if (!this.sleepEndTime) return '';
    const remaining = Math.max(0, this.sleepEndTime - Date.now());
    const totalMinutes = Math.ceil(remaining / 60000);
    if (totalMinutes >= 60) {
      const hours = Math.floor(totalMinutes / 60);
      const mins = totalMinutes % 60;
      return mins > 0 ? `${hours}h ${mins}m` : `${hours}h`;
    }
    return `${totalMinutes}m`;
  }

  /**
   * Prompt the user for a custom sleep timer duration
   */
  promptCustomSleepTimer() {
    const prompt = new BrowserWindow({
      width: 300,
      height: 145,
      resizable: false,
      minimizable: false,
      maximizable: false,
      alwaysOnTop: true,
      modal: true,
      parent: this.mainWindow,
      icon: this.iconPath,
      show: false,
      autoHideMenuBar: true,
      webPreferences: {
        nodeIntegration: false,
        contextIsolation: true
      }
    });

    const html = `<!DOCTYPE html>
<html><head><style>
  * { box-sizing: border-box; }
  html, body { overflow: hidden; margin: 0; }
  body { font-family: system-ui, -apple-system, sans-serif; padding: 14px;
         background: #1e1e2e; color: #cdd6f4; display: flex; flex-direction: column; gap: 10px; }
  label { font-size: 14px; }
  input { width: 100%; padding: 6px 8px; border: 1px solid #45475a;
          border-radius: 6px; background: #313244; color: #cdd6f4; font-size: 14px; outline: none; }
  input:focus { border-color: #89b4fa; }
  .buttons { display: flex; gap: 8px; justify-content: flex-end; }
  button { padding: 6px 16px; border: none; border-radius: 6px; cursor: pointer; font-size: 13px; }
  .cancel { background: #45475a; color: #cdd6f4; }
  .ok { background: #89b4fa; color: #1e1e2e; font-weight: 600; }
  button:hover { opacity: 0.85; }
</style></head><body>
  <label>Sleep timer &mdash; enter minutes:</label>
  <input type="number" id="mins" min="1" max="1440" placeholder="e.g. 90" autofocus>
  <div class="buttons">
    <button class="cancel" onclick="window.close()">Cancel</button>
    <button class="ok" id="ok">Start</button>
  </div>
  <script>
    const input = document.getElementById('mins');
    document.getElementById('ok').addEventListener('click', () => {
      const v = parseInt(input.value, 10);
      if (v > 0) { document.title = String(v); window.close(); }
    });
    input.addEventListener('keydown', (e) => {
      if (e.key === 'Enter') document.getElementById('ok').click();
      if (e.key === 'Escape') window.close();
    });
  </script>
</body></html>`;

    prompt.loadURL('data:text/html;charset=utf-8,' + encodeURIComponent(html));
    prompt.once('ready-to-show', () => prompt.show());

    // Use page-title-updated to get the result before the window closes
    prompt.webContents.on('page-title-updated', (_event, title) => {
      const minutes = parseInt(title, 10);
      if (minutes > 0) {
        this.startSleepTimer(minutes);
      }
    });
  }

  /**
   * Pause playback by executing JS in the renderer
   */
  pausePlayback() {
    if (!this.mainWindow || this.mainWindow.isDestroyed()) return;
    this.mainWindow.webContents.executeJavaScript(`
      (function() {
        var media = document.querySelector('audio, video');
        if (media && !media.paused) {
          media.pause();
          console.log('Sleep timer: paused playback');
        }
      })();
    `).catch(err => console.error('Sleep timer pause failed:', err));
  }

  /**
   * Show the main window
   */
  showWindow() {
    if (!this.mainWindow || this.mainWindow.isDestroyed()) {
      console.warn('Cannot show window: window is destroyed');
      return;
    }
    this.mainWindow.show();
    this.mainWindow.focus();
  }

  /**
   * Toggle window visibility
   */
  toggleWindow() {
    if (!this.mainWindow || this.mainWindow.isDestroyed()) {
      return;
    }

    if (this.mainWindow.isVisible()) {
      this.mainWindow.hide();
    } else {
      this.showWindow();
    }
  }

  /**
   * Update tray tooltip text
   */
  updateTooltip(text) {
    if (this.tray) {
      this.tray.setToolTip(text);
    }
  }

  /**
   * Check if tray icon was created and is available
   * Note: On some Linux DEs (GNOME 3+), tray may be created but not visible
   */
  isAvailable() {
    return this.tray !== null && !this.tray.isDestroyed();
  }

  /**
   * Destroy the tray icon
   */
  destroy() {
    this.cancelSleepTimer();
    if (this.tray) {
      this.tray.destroy();
      this.tray = null;
    }
  }
}

module.exports = TrayManager;
