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
        .replace(/[’'".,!?()[\]{}:;/-]/g, ' ')
        .replace(/\s+/g, ' ')
        .trim();
    };

    const titleNorm = normalize(title);
    const artistNorm = normalize(artist);

    let bestSong = null;
    let bestScore = 0;

    for (const song of songs) {
      if (!song || !song.title) continue;

      const songTitle = song.title;
      const songTitleNorm = normalize(songTitle);
      const coverArtistsNorm = normalize(song.coverArtists || '');
      const originalArtistsNorm = normalize(song.originalArtists || '');

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

    return bestScore > 0 ? bestSong : null;
  }

  /**
   * Get cover art URL from audio URL
   */
  getCoverArtUrl(audioUrl) {
    if (!audioUrl) return null;

    // Try to convert audio URL to image URL
    // Example: https://storage.neurokaraoke.com/audio/FEX%20-%20Subways%20of%20Your%20Mind%20-%20Evil.v1%29.mp3
    // Convert to: https://storage.neurokaraoke.com/images/FEX%20-%20Subways%20of%20Your%20Mind%20-%20Evil.jpg

    const imageUrl = audioUrl
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
          song.art_credit ||
          song.artCreditText ||
          song.art_credit_text ||
          song.coverArtCredit ||
          song.cover_art_credit ||
          song.coverArtBy ||
          song.cover_art_by ||
          song.albumArtCredit ||
          song.album_art_credit ||
          song.artBy ||
          song.art_by ||
          song.artCreator ||
          song.art_creator ||
          playlistArtCredit ||
          null;

        if (!artCredit) {
          console.log('No art credit field found for song. Available keys:', Object.keys(song));
        }

        const coverArtUrl =
          song.coverArt ||
          song.cover_art ||
          song.coverArtUrl ||
          song.cover_art_url ||
          this.getCoverArtUrl(song.audioUrl) ||
          playlistCover ||
          null;

        return {
          title: song.title,
          originalArtist: song.originalArtists,
          coverArtist: song.coverArtists,
          artCredit, // Album art creator
          audioUrl: song.audioUrl,
          coverArtUrl
        };
      }

      console.log('No matching song found in playlist for:', title, '-', artist || 'Unknown');
      return null;
    } catch (error) {
      console.error('Failed to fetch song metadata:', error);
      return null;
    }
  }
}

module.exports = NeuroKaraokeAPI;
