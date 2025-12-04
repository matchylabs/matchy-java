package com.matchylabs.matchy;

import com.matchylabs.matchy.jna.MatchyLibrary;
import com.matchylabs.matchy.jna.NativeStructs;
import com.sun.jna.Memory;
import com.sun.jna.Pointer;

import java.nio.file.Path;
import java.util.Objects;

/**
 * Main interface to a matchy database.
 * 
 * <p>Provides methods for querying IP addresses and patterns,
 * accessing statistics, and managing database resources.
 * 
 * <p>Example usage:
 * <pre>{@code
 * try (Database db = Database.open(Paths.get("threats.mxy"))) {
 *     QueryResult result = db.query("192.168.1.1");
 *     if (result.isMatch()) {
 *         System.out.println("Match found: " + result.getData());
 *     }
 * }
 * }</pre>
 * 
 * <p>Thread-safety: Database instances are thread-safe for queries.
 * Multiple threads can safely call query() concurrently.
 */
public class Database implements AutoCloseable {
    
    private final Pointer handle;
    // Keep reference to buffer memory to prevent GC while database is in use
    @SuppressWarnings("unused")
    private final Memory bufferMemory;
    private volatile boolean closed = false;
    
    /**
     * Private constructor for file-based databases - use static factory methods.
     */
    private Database(Pointer handle) {
        this.handle = Objects.requireNonNull(handle, "Database handle is null");
        this.bufferMemory = null;
    }
    
    /**
     * Private constructor for buffer-based databases - use static factory methods.
     */
    private Database(Pointer handle, Memory bufferMemory) {
        this.handle = Objects.requireNonNull(handle, "Database handle is null");
        this.bufferMemory = bufferMemory;
    }
    
    /**
     * Open a database file with default options.
     * 
     * @param path Path to .mxy database file
     * @return Database instance
     * @throws MatchyException if database cannot be opened
     */
    public static Database open(Path path) throws MatchyException {
        return open(path, OpenOptions.defaults());
    }
    
    /**
     * Open a database file with custom options.
     * 
     * @param path Path to .mxy database file
     * @param options Configuration options (cache size, auto-reload, etc.)
     * @return Database instance
     * @throws MatchyException if database cannot be opened
     */
    public static Database open(Path path, OpenOptions options) throws MatchyException {
        Objects.requireNonNull(path, "path cannot be null");
        Objects.requireNonNull(options, "options cannot be null");
        
        Pointer handle = MatchyLibrary.INSTANCE.matchy_open_with_options(
            path.toString(), 
            options.toNative()
        );
        
        if (handle == null) {
            throw new MatchyException("Failed to open database: " + path);
        }
        
        return new Database(handle);
    }
    
    /**
     * Open a database from a memory buffer.
     * 
     * <p>The buffer is copied to native memory that is managed by the Database instance.
     * 
     * @param buffer Database data
     * @return Database instance
     * @throws MatchyException if database cannot be opened
     */
    public static Database fromBuffer(byte[] buffer) throws MatchyException {
        Objects.requireNonNull(buffer, "buffer cannot be null");
        if (buffer.length == 0) {
            throw new MatchyException("Buffer cannot be empty");
        }
        
        // Allocate native memory and copy buffer
        Memory nativeBuffer = new Memory(buffer.length);
        nativeBuffer.write(0, buffer, 0, buffer.length);
        
        Pointer handle = MatchyLibrary.INSTANCE.matchy_open_buffer(nativeBuffer, buffer.length);
        
        if (handle == null) {
            throw new MatchyException("Failed to open database from buffer");
        }
        
        // NOTE: The native library takes ownership of the buffer, but we need to keep
        // the Memory object alive so JNA doesn't GC it. We return a Database that
        // holds the buffer.
        return new Database(handle, nativeBuffer);
    }
    
    /**
     * Query the database for an IP address or pattern.
     * 
     * <p>Automatically detects whether the query is an IP address or pattern.
     * 
     * @param query IP address (e.g., "192.168.1.1") or pattern (e.g., "*.evil.com")
     * @return Query result (never null)
     * @throws MatchyException if database is closed or query fails
     */
    public QueryResult query(String query) throws MatchyException {
        checkNotClosed();
        Objects.requireNonNull(query, "query cannot be null");
        
        // Use manual memory allocation for the result struct
        // This ensures we control the memory and can read it back reliably
        com.sun.jna.Memory resultMem = new com.sun.jna.Memory(24);
        resultMem.clear(); // Initialize to zero
        
        MatchyLibrary.INSTANCE.matchy_query_into(handle, query, resultMem);
        
        // Read result from memory
        NativeStructs.MatchyResult nativeResult = new NativeStructs.MatchyResult(resultMem);
        
        // Debug output
        if (Boolean.getBoolean("matchy.debug")) {
            byte[] raw = resultMem.getByteArray(0, 24);
            StringBuilder sb = new StringBuilder("[Database.query] raw bytes: ");
            for (byte b : raw) sb.append(String.format("%02x ", b & 0xff));
            System.err.println(sb.toString());
            
            System.err.println("[Database.query] query=" + query + 
                ", found=" + nativeResult.found + 
                ", prefix_len=" + nativeResult.prefix_len +
                ", _data_cache=" + nativeResult._data_cache +
                ", _db_ref=" + nativeResult._db_ref);
        }
        
        try {
            if (!nativeResult.isFound()) {
                return QueryResult.notFound();
            }
            
            // Convert to JSON for simplicity
            String json = MatchyLibrary.INSTANCE.matchy_result_to_json(nativeResult.getPointer());
            
            if (json == null) {
                return QueryResult.notFound();
            }
            
            return QueryResult.fromJson(json, nativeResult.prefix_len);
        } finally {
            // Free the native result structure
            MatchyLibrary.INSTANCE.matchy_free_result(nativeResult.getPointer());
        }
    }
    
    /**
     * Get database statistics.
     * 
     * <p>Returns information about query counts, cache performance, etc.
     * 
     * @return Database statistics
     * @throws MatchyException if database is closed
     */
    public DatabaseStats getStats() throws MatchyException {
        checkNotClosed();
        
        NativeStructs.MatchyStats nativeStats = new NativeStructs.MatchyStats();
        MatchyLibrary.INSTANCE.matchy_get_stats(handle, nativeStats);
        
        return DatabaseStats.fromNative(nativeStats);
    }
    
    /**
     * Clear the query cache.
     * 
     * <p>Forces all subsequent queries to perform fresh lookups.
     * Useful for benchmarking.
     * 
     * @throws MatchyException if database is closed
     */
    public void clearCache() throws MatchyException {
        checkNotClosed();
        MatchyLibrary.INSTANCE.matchy_clear_cache(handle);
    }
    
    /**
     * Get database metadata as JSON.
     * 
     * <p>Returns MMDB metadata if available (for IP or combined databases).
     * 
     * @return JSON metadata string, or null if no metadata available
     * @throws MatchyException if database is closed
     */
    public String getMetadata() throws MatchyException {
        checkNotClosed();
        return MatchyLibrary.INSTANCE.matchy_metadata(handle);
    }
    
    /**
     * Check if database supports IP address lookups.
     * 
     * @return true if database contains IP data
     * @throws MatchyException if database is closed
     */
    public boolean hasIpData() throws MatchyException {
        checkNotClosed();
        return MatchyLibrary.INSTANCE.matchy_has_ip_data(handle);
    }
    
    /**
     * Check if database supports string lookups (literals or globs).
     * 
     * @return true if database contains string data
     * @throws MatchyException if database is closed
     */
    public boolean hasStringData() throws MatchyException {
        checkNotClosed();
        return MatchyLibrary.INSTANCE.matchy_has_string_data(handle);
    }
    
    /**
     * Check if database supports literal (exact string) lookups.
     * 
     * @return true if database contains literal data
     * @throws MatchyException if database is closed
     */
    public boolean hasLiteralData() throws MatchyException {
        checkNotClosed();
        return MatchyLibrary.INSTANCE.matchy_has_literal_data(handle);
    }
    
    /**
     * Check if database supports glob pattern lookups.
     * 
     * @return true if database contains glob data
     * @throws MatchyException if database is closed
     */
    public boolean hasGlobData() throws MatchyException {
        checkNotClosed();
        return MatchyLibrary.INSTANCE.matchy_has_glob_data(handle);
    }
    
    /**
     * Get database format description.
     * 
     * @return Format string (e.g., "IP database", "Pattern database", "Combined IP+Pattern database")
     * @throws MatchyException if database is closed
     */
    public String getFormat() throws MatchyException {
        checkNotClosed();
        return MatchyLibrary.INSTANCE.matchy_format(handle);
    }
    
    /**
     * Get number of patterns in database.
     * 
     * @return Pattern count (0 if no patterns)
     * @throws MatchyException if database is closed
     */
    public long getPatternCount() throws MatchyException {
        checkNotClosed();
        return MatchyLibrary.INSTANCE.matchy_pattern_count(handle);
    }
    
    /**
     * Get pattern string by ID.
     * 
     * @param patternId Pattern ID
     * @return Pattern string, or null if not found
     * @throws MatchyException if database is closed
     */
    public String getPatternString(int patternId) throws MatchyException {
        checkNotClosed();
        return MatchyLibrary.INSTANCE.matchy_get_pattern_string(handle, patternId);
    }
    
    /**
     * Close the database and free all resources.
     * 
     * <p>After calling this method, no other methods can be called on this instance.
     * Calling close() multiple times is safe (subsequent calls are no-ops).
     */
    @Override
    public void close() {
        if (!closed) {
            closed = true;
            MatchyLibrary.INSTANCE.matchy_close(handle);
        }
    }
    
    /**
     * Check if database is closed.
     * 
     * @return true if closed
     */
    public boolean isClosed() {
        return closed;
    }
    
    /**
     * Check that database is not closed, throwing exception if it is.
     */
    private void checkNotClosed() throws MatchyException {
        if (closed) {
            throw new MatchyException("Database is closed");
        }
    }
}
