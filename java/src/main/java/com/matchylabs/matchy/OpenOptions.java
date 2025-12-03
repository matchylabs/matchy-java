package com.matchylabs.matchy;

import com.matchylabs.matchy.jna.NativeStructs;
import com.sun.jna.Pointer;

/**
 * Configuration options for opening a database.
 * 
 * Use the builder pattern to configure cache size and auto-reload behavior.
 * 
 * <pre>{@code
 * OpenOptions options = OpenOptions.defaults()
 *     .cacheCapacity(100_000)
 *     .autoReload(true);
 * Database db = Database.open(path, options);
 * }</pre>
 */
public class OpenOptions {
    
    private int cacheCapacity;
    private boolean autoReload;
    
    private OpenOptions() {
        // Default values (matching C API defaults)
        this.cacheCapacity = 10_000;
        this.autoReload = false;
    }
    
    /**
     * Create options with default values.
     * 
     * Defaults:
     * - cache capacity: 10,000
     * - auto-reload: false
     *
     * @return OpenOptions with defaults
     */
    public static OpenOptions defaults() {
        return new OpenOptions();
    }
    
    /**
     * Set the LRU cache capacity.
     * 
     * The cache stores recently queried results to speed up repeated queries.
     * Set to 0 to disable caching entirely.
     *
     * @param capacity cache capacity (0 to disable)
     * @return this OpenOptions for chaining
     */
    public OpenOptions cacheCapacity(int capacity) {
        if (capacity < 0) {
            throw new IllegalArgumentException("Cache capacity cannot be negative");
        }
        this.cacheCapacity = capacity;
        return this;
    }
    
    /**
     * Disable the query cache entirely.
     * 
     * Equivalent to {@code cacheCapacity(0)}.
     *
     * @return this OpenOptions for chaining
     */
    public OpenOptions noCache() {
        this.cacheCapacity = 0;
        return this;
    }
    
    /**
     * Enable or disable automatic database reloading.
     * 
     * When enabled, the database watches its source file and automatically
     * reloads when changes are detected. All queries transparently use the
     * latest version. Adds ~10-20ns overhead per query due to read lock.
     * 
     * Default: false
     *
     * @param enable true to enable auto-reload, false to disable
     * @return this OpenOptions for chaining
     */
    public OpenOptions autoReload(boolean enable) {
        this.autoReload = enable;
        return this;
    }
    
    /**
     * Get the configured cache capacity.
     *
     * @return cache capacity
     */
    public int getCacheCapacity() {
        return cacheCapacity;
    }
    
    /**
     * Check if auto-reload is enabled.
     *
     * @return true if auto-reload is enabled
     */
    public boolean isAutoReload() {
        return autoReload;
    }
    
    /**
     * Convert to native options structure.
     * 
     * Package-private - used internally.
     *
     * @return native options structure
     */
    NativeStructs.MatchyOpenOptions toNative() {
        NativeStructs.MatchyOpenOptions nativeOpts = new NativeStructs.MatchyOpenOptions();
        nativeOpts.cache_capacity = cacheCapacity;
        nativeOpts.auto_reload = autoReload;
        nativeOpts.reload_callback = Pointer.NULL;
        nativeOpts.reload_callback_user_data = Pointer.NULL;
        return nativeOpts;
    }
    
    @Override
    public String toString() {
        return String.format("OpenOptions{cacheCapacity=%d, autoReload=%s}",
            cacheCapacity, autoReload);
    }
}
