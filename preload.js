// Preload script for Neuro Karaoke Desktop
// Currently minimal - can be extended if needed for future features

const { contextBridge } = require('electron');

// Expose any custom APIs to the renderer if needed in the future
contextBridge.exposeInMainWorld('neuroKaraoke', {
  version: '1.0.0'
});
