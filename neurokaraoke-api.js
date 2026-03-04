const https = require('https');

/**
 * Client for Neuro Karaoke API
 */
class NeuroKaraokeAPI {
  constructor() {
    this.baseUrl = 'https://idk.neurokaraoke.com';
    this.cache = new Map();
    this.maxCacheSize = 10;
  }

  /**
   * Fetch playlist data
   */
  async fetchPlaylist(playlistId) {
    // Check cache first
    if (this.cache.has(playlistId)) {
      return this.cache.get(playlistId);
    }

    return new Promise((resolve, reject) => {
      const url = `${this.baseUrl}/public/playlist/${playlistId}`;

      https.get(url, (res) => {
        let data = '';

        res.on('data', (chunk) => {
          data += chunk;
        });

        res.on('end', () => {
          try {
            const playlist = JSON.parse(data);
            // Evict oldest entry if cache is full
            if (this.cache.size >= this.maxCacheSize) {
              const oldestKey = this.cache.keys().next().value;
              this.cache.delete(oldestKey);
            }
            this.cache.set(playlistId, playlist);
            resolve(playlist);
          } catch (error) {
            reject(error);
          }
        });
      }).on('error', (error) => {
        reject(error);
      });
    });
  }

  /**
   * Find song in playlist by title
   */
  findSong(playlist, title, artist) {
    const songs = Array.isArray(playlist) ? playlist : (playlist && Array.isArray(playlist.songs) ? playlist.songs : null);
    if (!songs) {
      return null;
    }

    const normalize = (value) => {
      if (!value || typeof value !== 'string') return '';
      return value
        .toLowerCase()
        .normalize('NFKD')
        .replace(/[\u0300-\u036f]/g, '')
        .replace(/[''".,!?()[\]{}:;/-]/g, ' ')
        .replace(/\s+/g, ' ')
        .trim();
    };

    const toStr = (value) => {
      if (Array.isArray(value)) return value.join(', ');
      return value || '';
    };

    const titleNorm = normalize(title);
    const artistNorm = normalize(artist);

    let bestSong = null;
    let bestScore = 0;

    for (const song of songs) {
      if (!song || !song.title) continue;

      const songTitle = song.title;
      const songTitleNorm = normalize(songTitle);
      const coverArtistsNorm = normalize(toStr(song.coverArtists));
      const originalArtistsNorm = normalize(toStr(song.originalArtists));

      let score = 0;
      if (title && songTitle.toLowerCase() === title.toLowerCase()) score += 3;
      if (titleNorm && songTitleNorm === titleNorm) score += 2;
      if (titleNorm && (songTitleNorm.includes(titleNorm) || titleNorm.includes(songTitleNorm))) score += 1;

      if (artistNorm) {
        if (coverArtistsNorm.includes(artistNorm)) score += 2;
        if (originalArtistsNorm.includes(artistNorm)) score += 2;
      } else {
        score += 1; // no artist provided, allow title-only matches
      }

      if (score > bestScore) {
        bestScore = score;
        bestSong = song;
      }
    }

    return bestScore >= 3 ? bestSong : null;
  }

  /**
   * Get cover art URL from audio path
   */
  getCoverArtUrl(audioPath) {
    if (!audioPath) return null;

    // Handle relative paths from the songs API (absolutePath field)
    const fullUrl = audioPath.startsWith('http')
      ? audioPath
      : `https://storage.neurokaraoke.com/${audioPath}`;

    const imageUrl = fullUrl
      .replace('/audio/', '/images/')
      .replace(/\.v\d+\)?\.mp3$/, '.jpg')
      .replace(/\.mp3$/, '.jpg');

    return imageUrl;
  }

  /**
   * Get current playing song metadata
   */
  async getCurrentSongMetadata(playlistId, title, artist) {
    try {
      const playlist = await this.fetchPlaylist(playlistId);
      const song = this.findSong(playlist, title, artist);

      if (song) {
        const playlistArtCredit = playlist && !Array.isArray(playlist) ? playlist.artCredit : null;
        const playlistCover = playlist && !Array.isArray(playlist) ? playlist.cover : null;

        const artCredit =
          song.artCredit ||
          song.artCreditText ||
          song.coverArtCredit ||
          song.artBy ||
          song.artCreator ||
          playlistArtCredit ||
          null;

        const audioPath = song.audioUrl || song.absolutePath || null;

        const coverArtUrl =
          song.coverArt ||
          song.coverArtUrl ||
          this.getCoverArtUrl(audioPath) ||
          playlistCover ||
          null;

        const coverArtist = Array.isArray(song.coverArtists)
          ? song.coverArtists.join(', ')
          : (song.coverArtists || null);

        const originalArtist = Array.isArray(song.originalArtists)
          ? song.originalArtists.join(', ')
          : (song.originalArtists || null);

        return {
          songId: song.id || song.songId || null,
          title: song.title,
          originalArtist,
          coverArtist,
          artCredit,
          audioUrl: audioPath,
          coverArtUrl
        };
      }

      return null;
    } catch (error) {
      console.error('Failed to fetch song metadata:', error);
      return null;
    }
  }
}

module.exports = NeuroKaraokeAPI;
