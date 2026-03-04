const { app, dialog, shell } = require('electron');
const https = require('https');

const GITHUB_REPO = 'aferilvt/neuro-karaoke-wrapper';
const RELEASES_PAGE = `https://github.com/${GITHUB_REPO}/releases`;

/**
 * Compare two semver strings. Returns true if remote is newer than current.
 */
function isNewerVersion(current, remote) {
  const parse = (v) => v.replace(/^v/, '').split('.').map((s) => parseInt(s, 10) || 0);
  const [cMaj, cMin, cPat = 0] = parse(current);
  const [rMaj, rMin, rPat = 0] = parse(remote);
  if (rMaj !== cMaj) return rMaj > cMaj;
  if (rMin !== cMin) return rMin > cMin;
  return rPat > cPat;
}

/**
 * Fetch the latest release metadata from GitHub API.
 */
function fetchLatestRelease() {
  return new Promise((resolve, reject) => {
    const options = {
      hostname: 'api.github.com',
      path: `/repos/${GITHUB_REPO}/releases/latest`,
      headers: { 'User-Agent': 'neuro-karaoke-wrapper-updater' }
    };

    https.get(options, (res) => {
      let data = '';
      res.on('data', (chunk) => { data += chunk; });
      res.on('end', () => {
        try {
          resolve(JSON.parse(data));
        } catch (e) {
          reject(e);
        }
      });
    }).on('error', reject);
  });
}

/**
 * Check GitHub for a newer release and show a dialog if one is available.
 * @param {BrowserWindow} mainWindow
 */
async function checkForUpdates(mainWindow) {
  try {
    const release = await fetchLatestRelease();
    const latestTag = release.tag_name;
    if (!latestTag) return;

    const currentVersion = app.getVersion();

    if (!isNewerVersion(currentVersion, latestTag)) return;

    const { response } = await dialog.showMessageBox(mainWindow, {
      type: 'info',
      title: 'Update Available',
      message: 'A new version of Neuro Karaoke Player is available!',
      detail: `Version ${latestTag} is available (you have v${currentVersion}).\nWould you like to open the download page?`,
      buttons: ['Download', 'Later'],
      defaultId: 0,
      cancelId: 1
    });

    if (response === 0) {
      const url = release.html_url || RELEASES_PAGE;
      if (url.startsWith('https://github.com/')) {
        shell.openExternal(url);
      } else {
        shell.openExternal(RELEASES_PAGE);
      }
    }
  } catch (error) {
    // Non-critical — silently swallow network/parse errors
    console.error('Update check failed:', error.message);
  }
}

module.exports = { checkForUpdates };
