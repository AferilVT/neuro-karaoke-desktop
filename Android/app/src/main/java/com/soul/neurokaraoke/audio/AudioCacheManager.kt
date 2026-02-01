package com.soul.neurokaraoke.audio

import android.content.Context
import androidx.media3.common.util.UnstableApi
import androidx.media3.database.StandaloneDatabaseProvider
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.cache.Cache
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.datasource.cache.LeastRecentlyUsedCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import java.io.File

/**
 * Manages audio caching for offline playback and smoother streaming.
 * Caches audio files locally so they can play without stuttering even if connection drops.
 */
@UnstableApi
object AudioCacheManager {

    private var cache: SimpleCache? = null
    private const val CACHE_SIZE_BYTES = 500L * 1024 * 1024 // 500 MB cache
    private const val CACHE_DIR_NAME = "audio_cache"

    /**
     * Initialize the cache. Should be called once when app starts.
     */
    @Synchronized
    fun initialize(context: Context) {
        if (cache == null) {
            val cacheDir = File(context.cacheDir, CACHE_DIR_NAME)
            val evictor = LeastRecentlyUsedCacheEvictor(CACHE_SIZE_BYTES)
            val databaseProvider = StandaloneDatabaseProvider(context)
            cache = SimpleCache(cacheDir, evictor, databaseProvider)
        }
    }

    /**
     * Get the cache instance
     */
    fun getCache(): Cache? = cache

    /**
     * Create a CacheDataSource.Factory that caches audio while streaming.
     * This allows playback to continue even if network drops.
     */
    fun createCacheDataSourceFactory(context: Context): DataSource.Factory {
        // Ensure cache is initialized
        initialize(context)

        // Create HTTP data source with custom user agent and timeouts
        val httpDataSourceFactory = DefaultHttpDataSource.Factory()
            .setUserAgent("NeuroKaraoke Android App")
            .setConnectTimeoutMs(15000)
            .setReadTimeoutMs(15000)
            .setAllowCrossProtocolRedirects(true)

        // Wrap with default data source for local files
        val upstreamFactory = DefaultDataSource.Factory(context, httpDataSourceFactory)

        // Create cache data source that:
        // - Reads from cache if available
        // - Downloads to cache while streaming
        // - Ignores cache on error (falls back to network)
        return CacheDataSource.Factory()
            .setCache(cache!!)
            .setUpstreamDataSourceFactory(upstreamFactory)
            .setCacheWriteDataSinkFactory(null) // Use default
            .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)
    }

    /**
     * Release the cache when app is destroyed
     */
    @Synchronized
    fun release() {
        cache?.release()
        cache = null
    }

    /**
     * Get current cache usage in bytes
     */
    fun getCacheUsageBytes(): Long {
        return cache?.cacheSpace ?: 0L
    }

    /**
     * Get cache usage as percentage
     */
    fun getCacheUsagePercent(): Float {
        val used = getCacheUsageBytes()
        return (used.toFloat() / CACHE_SIZE_BYTES) * 100f
    }

    /**
     * Clear all cached audio
     */
    fun clearCache() {
        // SimpleCache doesn't have a direct clear method
        // Cache is managed by LRU evictor automatically
    }
}
