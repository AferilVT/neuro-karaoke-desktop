const { contextBridge, ipcRenderer } = require('electron');

// Expose API to renderer
contextBridge.exposeInMainWorld('neuroKaraoke', {
  version: '1.1.0'
});

/**
 * Detects song title from the player UI
 */
class SongTitleDetector {
  constructor() {
    this.lastTitle = '';
    this.lastArtist = '';
    this.lastImageUrl = '';
    this.lastPlaylistId = null;
    this.lastUrl = '';
    this.lastSongUrl = '';
  }

  detectPlaylistId() {
    // Try to get playlist ID from URL
    const url = window.location.href;
    if (url !== this.lastUrl) {
      this.lastUrl = url;
    }
    const match = url.match(/\/playlist\/([a-zA-Z0-9-]+)/);
    if (match && match[1] !== this.lastPlaylistId) {
      this.lastPlaylistId = match[1];
      console.log('✓ Detected playlist ID:', match[1]);
      return match[1];
    }
    if (!match && url) {
      console.log('Playlist ID not found in URL:', url);
    }

    // Fallback: look for playlist links in the DOM
    const link = document.querySelector('a[href*="/playlist/"]');
    if (link) {
      const href = link.getAttribute('href') || '';
      const linkMatch = href.match(/\/playlist\/([a-zA-Z0-9-]+)/);
      if (linkMatch && linkMatch[1] !== this.lastPlaylistId) {
        this.lastPlaylistId = linkMatch[1];
        console.log('✓ Detected playlist ID from link:', linkMatch[1]);
        return linkMatch[1];
      }
    }

    // Fallback: scan elements for data-playlist-id / playlistid attributes
    const dataEl = document.querySelector('[data-playlist-id], [data-playlistid], [data-playlist], [playlist-id], [playlistid]');
    if (dataEl) {
      const dataId =
        dataEl.getAttribute('data-playlist-id') ||
        dataEl.getAttribute('data-playlistid') ||
        dataEl.getAttribute('data-playlist') ||
        dataEl.getAttribute('playlist-id') ||
        dataEl.getAttribute('playlistid');
      if (dataId && dataId !== this.lastPlaylistId) {
        this.lastPlaylistId = dataId;
        console.log('✓ Detected playlist ID from data attribute:', dataId);
        return dataId;
      }
    }

    // Fallback: scan inline scripts for playlistId
    const scripts = Array.from(document.querySelectorAll('script'));
    for (const script of scripts) {
      const text = script.textContent || '';
      const scriptMatch = text.match(/playlistId\"?\s*:\s*\"([a-zA-Z0-9-]+)\"/);
      if (scriptMatch && scriptMatch[1] !== this.lastPlaylistId) {
        this.lastPlaylistId = scriptMatch[1];
        console.log('✓ Detected playlist ID from script:', scriptMatch[1]);
        return scriptMatch[1];
      }
    }

    return null;
  }

  detect() {
    // Strategy 1: Global player (primary)
    const result = this.detectFromGlobalPlayer()
      || this.detectFromMobilePlayer()
      || this.detectFromDocumentTitle();

    if (result && result.title !== this.lastTitle && result.title.length > 0) {
      this.lastTitle = result.title;
      this.lastArtist = result.artist;
      return result;
    }

    return null;
  }

  detectSongUrl() {
    // If user is on a /song/ page, use that URL directly
    const url = window.location.href;
    if (url.includes('/song/') && url !== this.lastSongUrl) {
      this.lastSongUrl = url;
      return url;
    }
    return null;
  }

  detectImageUrl() {
    const globalPlayer = document.querySelector('.global-player, [class*="global-player"]');
    if (globalPlayer) {
      const img = globalPlayer.querySelector('img.lazy-media-image');
      if (img) {
        const imageUrl = img.getAttribute('src') || img.getAttribute('data-src');
        if (imageUrl && imageUrl !== this.lastImageUrl) {
          this.lastImageUrl = imageUrl;
          console.log('✓ Detected album art:', imageUrl);
          return imageUrl;
        }
      }
    }
    return null;
  }

  detectFromGlobalPlayer() {
    const globalPlayer = document.querySelector('.global-player, [class*="global-player"]');
    if (!globalPlayer) return null;

    const titleElement = globalPlayer.querySelector(
      'p.mud-typography-body2.theme-text-primary, p.mud-typography.theme-text-primary'
    );
    const artistElement = globalPlayer.querySelector(
      'span.mud-typography-caption.theme-text-secondary, span.mud-typography.theme-text-secondary'
    );

    if (titleElement) {
      const songTitle = titleElement.textContent.trim();
      const artist = artistElement ? artistElement.textContent.trim() : '';
      return { title: songTitle, artist: artist };
    }

    return null;
  }

  detectFromMobilePlayer() {
    const mobilePlayer = document.querySelector('.mobile-player');
    if (!mobilePlayer) return null;

    const titleEl = mobilePlayer.querySelector('p.mud-typography');
    const artistEl = mobilePlayer.querySelector('span.mud-typography');

    if (titleEl) {
      const songTitle = titleEl.textContent.trim();
      const artist = artistEl ? artistEl.textContent.trim() : '';
      return { title: songTitle, artist: artist };
    }

    return null;
  }

  detectFromDocumentTitle() {
    const patterns = [
      /^(.+?)\s*[-–|]\s*Neuro Karaoke/i,
      /^(.+?)\s*[-–|]\s*.*Karaoke/i,
    ];

    for (const pattern of patterns) {
      const match = document.title.match(pattern);
      if (match && match[1]) {
        const title = match[1].trim();
        if (title && title.length > 1) {
          // Try to split title - artist
          const parts = title.split(/\s*[-–]\s*/);
          if (parts.length >= 2) {
            return { title: parts[0], artist: parts[1] };
          }
          return { title: title, artist: '' };
        }
      }
    }

    return null;
  }

  reset() {
    this.lastTitle = '';
  }
}

/**
 * Detects playback state (playing/paused) from UI
 */
class PlaybackStateDetector {
  constructor() {
    this.lastState = null;
  }

  detect() {
    const state = this.detectFromMedia() ?? this.detectFromSvgIcon() ?? this.detectFromAriaLabel();

    if (state !== null && state !== this.lastState) {
      this.lastState = state;
      return state;
    }

    return null;
  }

  detectFromMedia() {
    const media = document.querySelector('audio, video');
    if (!media) return null;
    if (media.paused === true) return false;
    if (media.paused === false) return true;
    return null;
  }

  detectFromSvgIcon() {
    const playerContainer = document.querySelector('.global-player, .mobile-player');
    if (!playerContainer) return null;

    const buttons = playerContainer.querySelectorAll('button');

    for (const button of buttons) {
      const svg = button.querySelector('svg');
      if (!svg) continue;

      const paths = Array.from(svg.querySelectorAll('path'));

      for (const path of paths) {
        const d = path.getAttribute('d') || '';

        // Pause icon (two vertical bars) = Song is PLAYING
        if (d.includes('M6') && d.includes('h4V5H6') && d.includes('zm8')) {
          console.log('✓ Detected PAUSE icon - Song is PLAYING');
          return true;
        }

        // Play icon (triangle) = Song is PAUSED
        if (d.includes('M8 5v14l11-7') || (d.includes('M8') && d.includes('l11-7'))) {
          console.log('⏸ Detected PLAY icon - Song is PAUSED');
          return false;
        }
      }
    }

    return null;
  }

  detectFromAriaLabel() {
    const playButtons = document.querySelectorAll('[aria-label*="play" i], [aria-label*="pause" i]');

    for (const button of playButtons) {
      const ariaLabel = (button.getAttribute('aria-label') || '').toLowerCase();
      const isPlaying = ariaLabel.includes('pause') && !ariaLabel.includes('play');

      console.log('Playback from aria-label:', isPlaying ? 'Playing' : 'Paused', '- label:', ariaLabel);
      return isPlaying;
    }

    return null;
  }
}

/**
 * Detects song duration and elapsed time from the player UI
 */
class SongDurationDetector {
  constructor() {
    this.lastDuration = null;
    this.lastElapsed = null;
    this.lastProgressValue = null;
    this.progressMode = null; // 'elapsed' or 'remaining'
    this.lastDebugAt = 0;
    this.debugIntervalMs = 5000;
  }

  detectFromMedia() {
    const media = document.querySelector('audio, video');
    if (!media) return null;

    const elapsed = Number.isFinite(media.currentTime) ? Math.floor(media.currentTime) : null;
    const duration = Number.isFinite(media.duration) && media.duration > 0
      ? Math.floor(media.duration)
      : null;

    return { media, elapsed, duration };
  }

  parseTimeText(text) {
    const match = text.match(/^(-)?(\d+):(\d{2})(?::(\d{2}))?$/);
    if (!match) return null;
    const negative = Boolean(match[1]);
    const parts = match.slice(2).filter(Boolean).map((value) => parseInt(value, 10));
    let seconds = 0;
    if (parts.length === 3) {
      seconds = (parts[0] * 3600) + (parts[1] * 60) + parts[2];
    } else {
      seconds = (parts[0] * 60) + parts[1];
    }
    return { seconds, negative, raw: text };
  }

  extractTimesFromText(text) {
    const matches = [];
    const re = /-?\d+:\d{2}(?::\d{2})?/g;
    let match;
    while ((match = re.exec(text)) !== null) {
      const parsed = this.parseTimeText(match[0]);
      if (parsed) {
        matches.push(parsed);
      }
    }
    return matches;
  }

  detectElapsedFromSlider(progressContainer, durationSeconds) {
    const slider = progressContainer.querySelector('[role="slider"], input[type="range"]');
    if (!slider) return null;

    const valueRaw = slider.getAttribute('aria-valuenow') ?? slider.value;
    const maxRaw = slider.getAttribute('aria-valuemax') ?? slider.max;
    const value = parseFloat(valueRaw);
    const max = parseFloat(maxRaw);

    if (!Number.isFinite(value) || !Number.isFinite(max) || max <= 0) {
      return null;
    }

    if (durationSeconds && max <= 1) {
      return Math.round(durationSeconds * value);
    }

    if (durationSeconds && max <= 100 && max !== durationSeconds) {
      return Math.round(durationSeconds * (value / max));
    }

    if (durationSeconds && max !== durationSeconds) {
      return Math.round(durationSeconds * (value / max));
    }

    return Math.round(value);
  }

  detect() {
    const mediaInfo = this.detectFromMedia();
    if (mediaInfo && mediaInfo.duration && mediaInfo.duration !== this.lastDuration) {
      this.lastDuration = mediaInfo.duration;
      console.log('✓ Detected song duration from media element:', mediaInfo.duration, 'seconds');
      return mediaInfo.duration;
    }

    const durationElement = this.findDurationElement();

    if (durationElement) {
      const durationText = durationElement.textContent.trim();
      console.log('Found duration element with text:', durationText);

      const timeMatch = durationText.match(/(\d+):(\d+)/);
      if (timeMatch) {
        const minutes = parseInt(timeMatch[1], 10);
        const seconds = parseInt(timeMatch[2], 10);
        const totalSeconds = minutes * 60 + seconds;

        if (totalSeconds !== this.lastDuration && totalSeconds > 0) {
          this.lastDuration = totalSeconds;
          console.log('✓ Detected song duration:', durationText, '=', totalSeconds, 'seconds');
          return totalSeconds;
        }
      }
    } else {
      console.log('Duration element not found');
    }

    return null;
  }

  detectElapsed() {
    const progressContainer = document.querySelector('.desktop-progress-container');
    const mediaInfo = this.detectFromMedia();
    if (mediaInfo && mediaInfo.elapsed !== null) {
      if (mediaInfo.duration && mediaInfo.duration !== this.lastDuration) {
        this.lastDuration = mediaInfo.duration;
      }
      if (mediaInfo.elapsed !== this.lastElapsed) {
        this.lastElapsed = mediaInfo.elapsed;
        console.log(`✓ Detected elapsed from media element: ${mediaInfo.elapsed}s`);
        return mediaInfo.elapsed;
      }
    }

    if (!progressContainer) {
      console.log('Progress container not found for elapsed time');
      return null;
    }

    const durationSeconds = this.lastDuration;

    const slider = progressContainer.querySelector('[role="slider"], input[type="range"]');
    const sliderElapsed = this.detectElapsedFromSlider(progressContainer, durationSeconds);
    if (sliderElapsed !== null && sliderElapsed !== this.lastElapsed) {
      this.lastElapsed = sliderElapsed;
      console.log(`✓ Detected elapsed from slider: ${sliderElapsed}s`);
      return sliderElapsed;
    }

    const times = this.extractTimesFromText(progressContainer.textContent || '');

    if (times.length >= 2) {
      const durationCandidate = Math.max(...times.map((time) => time.seconds));
      const ordered = times;

      let progressCandidate = ordered[0].seconds;
      let progressIsRemaining = ordered[0].negative;

      if (!progressIsRemaining && ordered.length >= 2 && ordered[1].seconds >= ordered[0].seconds) {
        progressCandidate = ordered[0].seconds;
      } else {
        const smallest = times.reduce((min, time) => (time.seconds < min.seconds ? time : min), times[0]);
        progressCandidate = smallest.seconds;
        progressIsRemaining = smallest.negative;
      }

      const effectiveDuration = durationSeconds || durationCandidate;

      if (!this.progressMode && this.lastProgressValue !== null) {
        if (progressCandidate > this.lastProgressValue) {
          this.progressMode = 'elapsed';
        } else if (progressCandidate < this.lastProgressValue) {
          this.progressMode = 'remaining';
        }
      }

      if (progressIsRemaining && !this.progressMode) {
        this.progressMode = 'remaining';
      }

      const elapsed = (this.progressMode === 'remaining' && effectiveDuration)
        ? Math.max(effectiveDuration - progressCandidate, 0)
        : progressCandidate;

      // Always return elapsed time if it changed
      if (elapsed !== this.lastElapsed) {
        this.lastElapsed = elapsed;
        console.log(
          `✓ Detected elapsed: ${elapsed}s (${this.progressMode || 'elapsed?'})` +
          `, progress=${progressCandidate}s, duration=${effectiveDuration || 'unknown'}s`
        );
        this.lastProgressValue = progressCandidate;
        return elapsed;
      }
      this.lastProgressValue = progressCandidate;
    } else if (times.length === 1) {
      // Only one time found - might be just elapsed or just duration
      console.log(`Only found one time: ${times[0].raw} (${times[0].seconds}s)`);
    }

    const now = Date.now();
    if (now - this.lastDebugAt > this.debugIntervalMs) {
      this.lastDebugAt = now;
      const sliderValue = slider ? (slider.getAttribute('aria-valuenow') ?? slider.value) : null;
      const sliderMax = slider ? (slider.getAttribute('aria-valuemax') ?? slider.max) : null;
      console.log('Elapsed debug snapshot:', {
        durationSeconds,
        lastElapsed: this.lastElapsed,
        progressMode: this.progressMode,
        progressContainerText: (progressContainer.textContent || '').trim().slice(0, 200),
        timeTokens: times.map((time) => time.raw),
        sliderValue,
        sliderMax,
        mediaElapsed: mediaInfo ? mediaInfo.elapsed : null,
        mediaDuration: mediaInfo ? mediaInfo.duration : null
      });
    }

    return null;
  }

  findDurationElement() {
    // Try direct selector first
    let element = document.querySelector('.desktop-duration-time');
    if (element) return element;

    // Search in progress container
    const progressContainer = document.querySelector('.desktop-progress-container');
    if (!progressContainer) return null;

    const timeSpans = progressContainer.querySelectorAll('span');
    for (const span of timeSpans) {
      const text = span.textContent.trim();
      if (text.match(/^\d+:\d+$/)) {
        const match = text.match(/(\d+):(\d+)/);
        if (match) {
          const totalSecs = parseInt(match[1]) * 60 + parseInt(match[2]);
          // Duration should be at least 30 seconds
          if (totalSecs > 30) {
            return span;
          }
        }
      }
    }

    return null;
  }

  reset() {
    this.lastDuration = null;
    this.lastElapsed = null;
    this.lastProgressValue = null;
    this.progressMode = null;
  }
}

/**
 * Manages navigator.mediaSession API for OS media controls integration
 */
class MediaSessionManager {
  constructor() {
    this.currentTitle = '';
    this.currentArtist = '';
    this.currentArtwork = '';
    this.isPlaying = false;
  }

  init() {
    if (!('mediaSession' in navigator)) {
      console.warn('MediaSession API not supported');
      return;
    }

    // Set up action handlers for media keys
    const actions = [
      ['play', () => this.triggerPlayPause()],
      ['pause', () => this.triggerPlayPause()],
      ['previoustrack', () => this.triggerPrevious()],
      ['nexttrack', () => this.triggerNext()],
      ['seekbackward', (details) => this.triggerSeek(-10)],
      ['seekforward', (details) => this.triggerSeek(10)],
    ];

    for (const [action, handler] of actions) {
      try {
        navigator.mediaSession.setActionHandler(action, handler);
        console.log(`✓ MediaSession: registered ${action} handler`);
      } catch (error) {
        console.warn(`MediaSession: ${action} not supported`, error);
      }
    }

    console.log('✓ MediaSession API initialized');
  }

  triggerPlayPause() {
    // .play-btn is the play button on Neuro Karaoke
    const playBtn = document.querySelector('.play-btn');
    if (playBtn) {
      playBtn.click();
      console.log('MediaSession: triggered play/pause');
      return;
    }
    // Fallback: toggle media element directly
    const media = document.querySelector('audio, video');
    if (media) {
      if (media.paused) media.play(); else media.pause();
      console.log('MediaSession: toggled media element');
    }
  }

  triggerNext() {
    const playBtn = document.querySelector('.play-btn');
    if (playBtn && playBtn.nextElementSibling) {
      playBtn.nextElementSibling.click();
      console.log('MediaSession: triggered next track');
    }
  }

  triggerPrevious() {
    const playBtn = document.querySelector('.play-btn');
    if (playBtn && playBtn.previousElementSibling) {
      playBtn.previousElementSibling.click();
      console.log('MediaSession: triggered previous track');
    }
  }

  triggerSeek(offsetSeconds) {
    const media = document.querySelector('audio, video');
    if (media && Number.isFinite(media.currentTime)) {
      media.currentTime = Math.max(0, media.currentTime + offsetSeconds);
      console.log(`MediaSession: seeked ${offsetSeconds}s`);
    }
  }

  updateMetadata(title, artist, artwork) {
    if (!('mediaSession' in navigator)) return;

    // Only update if something changed
    if (title === this.currentTitle && artist === this.currentArtist && artwork === this.currentArtwork) {
      return;
    }

    this.currentTitle = title || '';
    this.currentArtist = artist || '';
    this.currentArtwork = artwork || '';

    const metadata = {
      title: this.currentTitle || 'Neuro Karaoke',
      artist: this.currentArtist || 'Neuro',
      album: 'Neuro Karaoke',
    };

    // Add artwork if available
    if (this.currentArtwork) {
      metadata.artwork = [
        { src: this.currentArtwork, sizes: '512x512', type: 'image/png' },
        { src: this.currentArtwork, sizes: '256x256', type: 'image/png' },
        { src: this.currentArtwork, sizes: '128x128', type: 'image/png' },
      ];
    }

    try {
      navigator.mediaSession.metadata = new MediaMetadata(metadata);
      console.log('✓ MediaSession metadata updated:', metadata.title, '-', metadata.artist);
    } catch (error) {
      console.error('Failed to set MediaSession metadata:', error);
    }
  }

  updatePlaybackState(playing) {
    if (!('mediaSession' in navigator)) return;

    this.isPlaying = playing;
    navigator.mediaSession.playbackState = playing ? 'playing' : 'paused';
    console.log('MediaSession playback state:', playing ? 'playing' : 'paused');
  }

  updatePositionState(duration, position) {
    if (!('mediaSession' in navigator)) return;

    try {
      if (duration > 0) {
        navigator.mediaSession.setPositionState({
          duration: duration,
          position: Math.min(position || 0, duration),
          playbackRate: 1.0,
        });
      }
    } catch (error) {
      // Position state may not be supported on all platforms
    }
  }
}

/**
 * Main song detection manager
 */
class SongDetectionManager {
  constructor() {
    this.titleDetector = new SongTitleDetector();
    this.playbackDetector = new PlaybackStateDetector();
    this.durationDetector = new SongDurationDetector();
    this.mediaSessionManager = new MediaSessionManager();
    this.boundMedia = null;
    this.onMediaPlay = null;
    this.onMediaPause = null;
    this.lastArtwork = '';
  }

  detectAll() {
    this.bindMediaEvents();
    const songInfo = this.titleDetector.detect();
    const playbackState = this.playbackDetector.detect();
    const duration = this.durationDetector.detect();
    const elapsed = this.durationDetector.detectElapsed();
    const playlistId = this.titleDetector.detectPlaylistId();
    const imageUrl = this.titleDetector.detectImageUrl();
    const songUrl = this.titleDetector.detectSongUrl();

    if (playlistId !== null) {
      ipcRenderer.send('playlist-id', playlistId);
    }

    if (songUrl !== null) {
      ipcRenderer.send('song-url', songUrl);
    }

    if (songInfo !== null) {
      this.durationDetector.reset(); // Reset duration for new song
      ipcRenderer.send('update-song', songInfo);

      // Update MediaSession with new song info
      this.mediaSessionManager.updateMetadata(
        songInfo.title,
        songInfo.artist,
        this.lastArtwork
      );

      // Immediately check playback and duration for new song
      this.playbackDetector.detect();
      this.durationDetector.detect();
    }

    if (playbackState !== null) {
      ipcRenderer.send('playback-state', playbackState);
      // Update MediaSession playback state
      this.mediaSessionManager.updatePlaybackState(playbackState);
    }

    if (duration !== null) {
      ipcRenderer.send('song-duration', duration);
    }

    if (elapsed !== null) {
      ipcRenderer.send('song-elapsed', elapsed);
      // Update MediaSession position
      const currentDuration = this.durationDetector.lastDuration;
      if (currentDuration) {
        this.mediaSessionManager.updatePositionState(currentDuration, elapsed);
      }
    }

    if (imageUrl !== null) {
      ipcRenderer.send('album-art', imageUrl);
      // Update MediaSession artwork
      if (imageUrl !== this.lastArtwork) {
        this.lastArtwork = imageUrl;
        this.mediaSessionManager.updateMetadata(
          this.titleDetector.lastTitle,
          this.titleDetector.lastArtist,
          imageUrl
        );
      }
    }
  }

  detectTitle() {
    const songInfo = this.titleDetector.detect();
    if (songInfo !== null) {
      this.durationDetector.reset();
      ipcRenderer.send('update-song', songInfo);
      this.playbackDetector.detect();
      this.durationDetector.detect();
    }
  }

  detectPlayback() {
    const state = this.playbackDetector.detect();
    if (state !== null) {
      ipcRenderer.send('playback-state', state);
      this.mediaSessionManager.updatePlaybackState(state);
    }
  }

  detectDuration() {
    const duration = this.durationDetector.detect();
    if (duration !== null) {
      ipcRenderer.send('song-duration', duration);
    }
  }

  bindMediaEvents() {
    const media = document.querySelector('audio, video');
    if (!media || media === this.boundMedia) return;

    if (this.boundMedia && this.onMediaPlay && this.onMediaPause) {
      this.boundMedia.removeEventListener('play', this.onMediaPlay);
      this.boundMedia.removeEventListener('playing', this.onMediaPlay);
      this.boundMedia.removeEventListener('pause', this.onMediaPause);
      this.boundMedia.removeEventListener('ended', this.onMediaPause);
    }

    this.boundMedia = media;
    this.onMediaPlay = () => {
      ipcRenderer.send('playback-state', true);
      this.mediaSessionManager.updatePlaybackState(true);
    };
    this.onMediaPause = () => {
      ipcRenderer.send('playback-state', false);
      this.mediaSessionManager.updatePlaybackState(false);
    };
    media.addEventListener('play', this.onMediaPlay);
    media.addEventListener('playing', this.onMediaPlay);
    media.addEventListener('pause', this.onMediaPause);
    media.addEventListener('ended', this.onMediaPause);
  }
}

// Initialize when DOM is ready
window.addEventListener('DOMContentLoaded', () => {
  console.log('Neuro Karaoke wrapper loaded');

  const manager = new SongDetectionManager();

  // Initialize MediaSession API for hardware media key support
  manager.mediaSessionManager.init();

  // Debounce helper to prevent excessive detection calls
  let debounceTimer = null;
  const debouncedDetectAll = () => {
    if (debounceTimer) return;
    debounceTimer = setTimeout(() => {
      debounceTimer = null;
      manager.detectAll();
    }, 500);
  };

  // Setup MutationObserver to watch for DOM changes (debounced)
  const observer = new MutationObserver(debouncedDetectAll);

  // Setup title observer for document title changes
  const titleObserver = new MutationObserver(() => {
    manager.detectTitle();
  });

  let fallbackInterval = null;

  // Start observing after a short delay to let the page load
  setTimeout(() => {
    // Observe body for DOM changes
    observer.observe(document.body, {
      childList: true,
      subtree: true,
      characterData: false
    });

    // Observe title element
    const titleElement = document.querySelector('title');
    if (titleElement) {
      titleObserver.observe(titleElement, {
        childList: true,
        characterData: true,
        subtree: true
      });
    }

    // Periodic fallback check - reduced to every 3 seconds (observer handles most changes)
    fallbackInterval = setInterval(() => {
      manager.detectAll();
    }, 3000);

    // Initial detection
    manager.detectAll();
  }, 2000);

  // Cleanup on page unload to prevent leaks
  window.addEventListener('beforeunload', () => {
    observer.disconnect();
    titleObserver.disconnect();
    if (fallbackInterval) clearInterval(fallbackInterval);
    if (debounceTimer) clearTimeout(debounceTimer);
    // Remove media event listeners
    if (manager.boundMedia && manager.onMediaPlay && manager.onMediaPause) {
      manager.boundMedia.removeEventListener('play', manager.onMediaPlay);
      manager.boundMedia.removeEventListener('playing', manager.onMediaPlay);
      manager.boundMedia.removeEventListener('pause', manager.onMediaPause);
      manager.boundMedia.removeEventListener('ended', manager.onMediaPause);
    }
  });
});
