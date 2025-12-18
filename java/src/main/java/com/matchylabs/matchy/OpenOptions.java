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
    private boolean autoUpdate;
    private int updateIntervalSecs;
    private String cacheDir;
    private ReloadCallback reloadCallback;
    
    private OpenOptions() {
        this.cacheCapacity = 10_000;
        this.autoReload = false;
        this.autoUpdate = false;
        this.updateIntervalSecs = 3600;
        this.cacheDir = null;
        this.reloadCallback = null;
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
     * Enable automatic updates from the database's embedded URL.
     * 
     * When enabled, periodically checks the database's embedded update URL for new
     * versions using HTTP conditional GET (ETag). Database must have an update URL
     * embedded in metadata. Updates are downloaded to cacheDir (or system default).
     * 
     * Default: false
     *
     * @param enable true to enable auto-update
     * @return this OpenOptions for chaining
     */
    public OpenOptions autoUpdate(boolean enable) {
        this.autoUpdate = enable;
        return this;
    }
    
    /**
     * Set how often to check for remote updates.
     * 
     * Only used when autoUpdate is true.
     * Default: 3600 (1 hour)
     *
     * @param seconds interval in seconds
     * @return this OpenOptions for chaining
     */
    public OpenOptions updateIntervalSecs(int seconds) {
        if (seconds < 0) {
            throw new IllegalArgumentException("Update interval cannot be negative");
        }
        this.updateIntervalSecs = seconds;
        return this;
    }
    
    /**
     * Set the cache directory for downloaded updates.
     * 
     * If not set, uses system default (~/.cache/matchy/ on Unix).
     *
     * @param dir cache directory path
     * @return this OpenOptions for chaining
     */
    public OpenOptions cacheDir(String dir) {
        this.cacheDir = dir;
        return this;
    }
    
    /**
     * Set a callback to be notified when database reloads.
     * 
     * The callback is invoked from a native watcher thread - keep processing minimal.
     * Do not call matchy methods from within the callback.
     *
     * @param callback callback to invoke on reload events
     * @return this OpenOptions for chaining
     */
    public OpenOptions reloadCallback(ReloadCallback callback) {
        this.reloadCallback = callback;
        return this;
    }
    
    public int getCacheCapacity() {
        return cacheCapacity;
    }
    
    public boolean isAutoReload() {
        return autoReload;
    }
    
    public boolean isAutoUpdate() {
        return autoUpdate;
    }
    
    public int getUpdateIntervalSecs() {
        return updateIntervalSecs;
    }
    
    public String getCacheDir() {
        return cacheDir;
    }
    
    public ReloadCallback getReloadCallback() {
        return reloadCallback;
    }
    
    NativeStructs.MatchyOpenOptions toNative() {
        return toNative(null);
    }
    
    NativeStructs.MatchyOpenOptions toNative(CallbackHolder callbackHolder) {
        NativeStructs.MatchyOpenOptions nativeOpts = new NativeStructs.MatchyOpenOptions();
        nativeOpts.cache_capacity = cacheCapacity;
        nativeOpts.auto_reload = autoReload;
        nativeOpts.auto_update = autoUpdate;
        nativeOpts.update_interval_secs = updateIntervalSecs;
        nativeOpts.cache_dir = cacheDir;
        
        if (reloadCallback != null && callbackHolder != null) {
            callbackHolder.setCallback(reloadCallback);
            nativeOpts.reload_callback = callbackHolder.getNativeCallback();
            nativeOpts.reload_callback_user_data = Pointer.NULL;
        } else {
            nativeOpts.reload_callback = Pointer.NULL;
            nativeOpts.reload_callback_user_data = Pointer.NULL;
        }
        
        return nativeOpts;
    }
    
    @Override
    public String toString() {
        return String.format("OpenOptions{cacheCapacity=%d, autoReload=%s, autoUpdate=%s, updateIntervalSecs=%d}",
            cacheCapacity, autoReload, autoUpdate, updateIntervalSecs);
    }
}
