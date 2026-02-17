const DiscordRPC = require('discord-rpc');

/**
 * Manages Discord Rich Presence integration
 */
class DiscordManager {
  constructor(clientId) {
    this.clientId = clientId;
    this.client = null;
    this.currentSong = '';
    this.currentArtist = '';
    this.isPlaying = false;
    this.songStartTime = null;
    this.songDuration = null;
    this.songElapsed = 0; // Actual elapsed time from player
    this.pausedElapsed = 0; // Track elapsed time when paused
    this.albumArtUrl = null; // Store album art URL
    this.albumArtText = null; // Store album art credit text
    this.albumArtCredit = null;
    this.lastPresenceUpdate = 0;
    this.minPresenceIntervalMs = 15000;
    this.pendingPresenceTimer = null;
    this.isUpdatingPresence = false;
    this.pendingPresenceRequested = false;
    this.pendingPresenceForce = false;
    this.activityType = 2; // 0 = Playing, 2 = Listening
    this.songUrl = null; // Current page URL for Discord button
  }

  /**
   * Initialize and connect to Discord RPC
   */
  async init() {
    try {
      console.log('Initializing Discord RPC with Client ID:', this.clientId);
      DiscordRPC.register(this.clientId);
      this.client = new DiscordRPC.Client({ transport: 'ipc' });

      this.client.on('ready', () => {
        console.log('✓ Discord RPC connected successfully!');
        this.updatePresence();
      });

      this.client.on('error', (error) => {
        console.error('Discord RPC error:', error);
      });

      await this.client.login({ clientId: this.clientId });
      console.log('Discord RPC login completed');
    } catch (error) {
      console.error('❌ Failed to initialize Discord RPC:', error);
      console.error('Make sure Discord desktop app is running!');
      this.client = null;
    }
  }

  /**
   * Update song information
   */
  updateSong(title, artist = '') {
    const songChanged = this.currentSong !== title || this.currentArtist !== artist;
    this.currentSong = title;
    this.currentArtist = artist;

    if (songChanged && title) {
      // Reset state for new song
      this.isPlaying = true;
      this.songStartTime = Date.now();
      this.songElapsed = 0;
      this.pausedElapsed = 0;
      this.albumArtUrl = null; // Reset album art for new song
      this.albumArtText = null; // Reset album art credit
      this.albumArtCredit = null;
      this.lastPresenceUpdate = 0; // Force an immediate sync for new songs
      if (this.pendingPresenceTimer) {
        clearTimeout(this.pendingPresenceTimer);
        this.pendingPresenceTimer = null;
      }
      console.log('New song detected:', title, artist ? `by ${artist}` : '');
    }

    this.updatePresence({ force: true });
  }

  /**
   * Update playback state (playing/paused)
   */
  updatePlaybackState(playing) {
    const wasPlaying = this.isPlaying;
    this.isPlaying = playing;

    console.log('Playback state:', playing ? '▶ Playing' : '⏸ Paused');

    if (playing && !wasPlaying) {
      // Resuming playback - elapsed time will update via updateElapsed()
      if (!this.songStartTime) {
        // Starting fresh
        this.songStartTime = Date.now();
        console.log('Song started at:', new Date(this.songStartTime).toLocaleTimeString());
      }
      if (this.pausedElapsed) {
        this.songStartTime = Date.now() - (this.pausedElapsed * 1000);
      }
      console.log('Song resumed');
    } else if (!playing && wasPlaying) {
      // Pausing playback
      console.log('Song paused at', this.songElapsed, 'seconds');
      this.pausedElapsed = this.songElapsed;
    }

    this.updatePresence({ force: true });
  }

  /**
   * Update song duration
   */
  updateDuration(durationInSeconds) {
    console.log('Received song duration:', durationInSeconds, 'seconds');
    this.songDuration = durationInSeconds;
    this.updatePresence({ force: true });
  }

  /**
   * Update song elapsed time (from player)
   */
  updateElapsed(elapsedSeconds) {
    this.songElapsed = elapsedSeconds;

    // Update start time based on current elapsed time
    this.songStartTime = Date.now() - (elapsedSeconds * 1000);

    // Always update Discord when elapsed changes
    if (this.isPlaying) {
      const remaining = this.songDuration ? Math.max(this.songDuration - elapsedSeconds, 0) : null;
      console.log(
        `Syncing Discord time: elapsed=${elapsedSeconds}s` +
          (this.songDuration ? `, duration=${this.songDuration}s, remaining=${remaining}s` : ', duration=unknown')
      );
      this.updatePresence();
    }
  }

  /**
   * Update album art
   */
  updateAlbumArt(imageUrl, artText = null) {
    if (imageUrl) {
      console.log('Received album art URL:', imageUrl);
      this.albumArtUrl = imageUrl;
    }
    if (artText) {
      this.albumArtText = artText;
    }
    this.updatePresence({ force: true });
  }

  /**
   * Update the current page URL for the Discord button
   */
  updateSongUrl(url) {
    if (url && url !== this.songUrl) {
      this.songUrl = url;
      console.log('Updated song URL for Discord button:', url);
      this.updatePresence({ force: true });
    }
  }

  updateAlbumArtCredit(credit) {
    if (!credit || typeof credit !== 'string') return;
    const cleaned = credit.replace(/^Art by:\s*/i, '').trim();
    this.albumArtCredit = cleaned;
    this.albumArtText = cleaned;
    console.log('Received album art credit:', this.albumArtCredit);
    this.updatePresence({ force: true });
  }

  /**
   * Update Discord Rich Presence with current state
   */
  async updatePresence({ force = false } = {}) {
    if (!this.client) {
      console.log('Discord RPC client not initialized, skipping presence update');
      return;
    }

    const now = Date.now();
    if (!force && now - this.lastPresenceUpdate < this.minPresenceIntervalMs) {
      if (!this.pendingPresenceTimer) {
        const delay = this.minPresenceIntervalMs - (now - this.lastPresenceUpdate);
        this.pendingPresenceTimer = setTimeout(() => {
          this.pendingPresenceTimer = null;
          this.updatePresence();
        }, delay);
      }
      return;
    }

    if (this.isUpdatingPresence) {
      this.pendingPresenceRequested = true;
      this.pendingPresenceForce = this.pendingPresenceForce || force;
      return;
    }

    this.isUpdatingPresence = true;

    try {
      if (!this.currentSong || this.currentSong.trim() === '') {
        console.log('No current song, clearing Discord presence');
        await this.client.clearActivity();
        this.lastPresenceUpdate = Date.now();
        return;
      }

      console.log('Updating Discord presence:', {
        song: this.currentSong,
        playing: this.isPlaying,
        duration: this.songDuration,
        startTime: this.songStartTime
      });

      const largeImageKey = this.albumArtUrl || 'neurokaraoke';

      const truncate = (value, max) => {
        if (!value || value.length <= max) return value;
        return `${value.slice(0, max - 1)}…`;
      };

      const detailsText = this.currentSong;
      const stateText = this.currentArtist || 'Listening';
      

      const activity = {
        type: this.activityType,
        details: truncate(detailsText, 128),
        state: truncate(stateText, 128),
        largeImageKey,
        largeImageText: this.albumArtText || "Neuro Karaoke",
        smallImageKey: this.isPlaying ? 'play' : 'pause',
        smallImageText: this.isPlaying ? 'Playing' : 'Paused',
        instance: false,
        buttons: [
          { label: 'Listen on Neuro Karaoke', url: this.songUrl || 'https://www.neurokaraoke.com/' }
        ]
      };

      // Only show progress bar when playing
      if (this.isPlaying && this.songStartTime && this.songDuration) {
        const startTimestamp = Math.floor(this.songStartTime / 1000);
        activity.startTimestamp = startTimestamp;
        activity.endTimestamp = startTimestamp + this.songDuration;
        console.log(`Setting synced timestamps: elapsed=${this.songElapsed}s, duration=${this.songDuration}s`);
      } else if (!this.isPlaying) {
        // Discord RPC has no pause state for the progress bar; omit timestamps to prevent countdown.
        activity.state = this.currentArtist ? `${this.currentArtist} · Paused` : 'Paused';
        console.log('Paused - updating presence without timestamps');
      }

      const pid = process.pid;
      if (typeof this.client.request === 'function') {
        const payload = {
          pid,
          activity: {
            type: activity.type,
            state: activity.state,
            details: activity.details,
            timestamps: activity.startTimestamp || activity.endTimestamp ? {
              start: activity.startTimestamp,
              end: activity.endTimestamp
            } : undefined,
            assets: activity.largeImageKey || activity.largeImageText || activity.smallImageKey || activity.smallImageText ? {
              large_image: activity.largeImageKey,
              large_text: activity.largeImageText,
              small_image: activity.smallImageKey,
              small_text: activity.smallImageText
            } : undefined,
            buttons: activity.buttons,
            instance: !!activity.instance
          }
        };
        await this.client.request('SET_ACTIVITY', payload);
      } else {
        await this.client.setActivity(activity);
      }
      this.lastPresenceUpdate = Date.now();
      console.log('✓ Discord presence updated successfully');
    } catch (error) {
      console.error('❌ Failed to update Discord presence:', error);
    } finally {
      this.isUpdatingPresence = false;
      if (this.pendingPresenceRequested) {
        const forceNext = this.pendingPresenceForce;
        this.pendingPresenceRequested = false;
        this.pendingPresenceForce = false;
        this.updatePresence({ force: forceNext });
      }
    }
  }

  /**
   * Clean up Discord RPC connection
   */
  destroy() {
    if (this.pendingPresenceTimer) {
      clearTimeout(this.pendingPresenceTimer);
      this.pendingPresenceTimer = null;
    }
    if (this.client) {
      this.client.removeAllListeners();
      this.client.clearActivity();
      this.client.destroy();
      this.client = null;
    }
  }
}

module.exports = DiscordManager;
