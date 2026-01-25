const { Tray, Menu, app, nativeImage } = require('electron');

/**
 * Manages system tray icon and menu
 */
class TrayManager {
  constructor(iconPath) {
    this.iconPath = iconPath;
    this.tray = null;
    this.mainWindow = null;
  }

  /**
   * Create the system tray icon
   */
  create(mainWindow, onQuit) {
    this.mainWindow = mainWindow;
    this.tray = new Tray(this.getTrayIcon());
    this.tray.setToolTip('Neuro Karaoke');

    const contextMenu = Menu.buildFromTemplate([
      {
        label: 'Open',
        click: () => this.showWindow()
      },
      {
        label: 'Exit',
        click: () => onQuit()
      }
    ]);

    this.tray.setContextMenu(contextMenu);

    // Single click to toggle window visibility (Windows style)
    this.tray.on('click', () => this.toggleWindow());

    // Double click to show and focus
    this.tray.on('double-click', () => this.showWindow());

    return this.tray;
  }

  /**
   * Build a tray icon that renders correctly in the macOS menu bar.
   */
  getTrayIcon() {
    const icon = nativeImage.createFromPath(this.iconPath);
    if (icon.isEmpty()) {
      return this.iconPath;
    }

    if (process.platform === 'darwin') {
      const targetSize = 16;
      const size = icon.getSize();
      const resized = (size.width !== targetSize || size.height !== targetSize)
        ? icon.resize({ width: targetSize, height: targetSize })
        : icon;
      resized.setTemplateImage(true);
      return resized;
    }

    return icon;
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
   * Destroy the tray icon
   */
  destroy() {
    if (this.tray) {
      this.tray.destroy();
      this.tray = null;
    }
  }
}

module.exports = TrayManager;
