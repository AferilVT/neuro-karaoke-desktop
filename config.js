/**
 * Application configuration
 */
module.exports = {
  // Discord Rich Presence
  // Replace with your Discord Application ID from https://discord.com/developers/applications
  DISCORD_CLIENT_ID: '1464192200849100862',

  // Window settings
  WINDOW: {
    WIDTH: 1200,
    HEIGHT: 800,
    MIN_WIDTH: 800,
    MIN_HEIGHT: 600,
    BACKGROUND_COLOR: '#1a1a1a'
  },

  // URLs
  URL: {
    SITE: 'https://www.neurokaraoke.com/',
    ALLOWED_HOSTS: ['https://www.neurokaraoke.com', 'https://neurokaraoke.com']
  },

  // App metadata
  APP: {
    ID: 'com.neurokaraoke.app',
    NAME: 'Neuro Karaoke',
    PARTITION: 'persist:neurokaraoke',
    USER_AGENT: 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 Chrome/120.0.0.0 Safari/537.36'
  }
};
