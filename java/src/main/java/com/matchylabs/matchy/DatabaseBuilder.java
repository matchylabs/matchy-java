package com.matchylabs.matchy;

import com.google.gson.Gson;
import com.matchylabs.matchy.jna.MatchyLibrary;
import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.Pointer;

import java.nio.file.Path;
import java.util.Map;
import java.util.Objects;

/**
 * Builder for creating matchy databases programmatically.
 * 
 * <p>Allows adding IP addresses, CIDR ranges, and patterns with associated data,
 * then building an in-memory database or saving to file.
 * 
 * <p>Example usage:
 * <pre>{@code
 * try (DatabaseBuilder builder = new DatabaseBuilder()) {
 *     builder.add("1.2.3.4", Map.of("threat_level", "high"))
 *            .add("10.0.0.0/8", Map.of("type", "internal"))
 *            .add("*.evil.com", Map.of("category", "malware"))
 *            .setDescription("Threat database v1.0");
 *     
 *     builder.save(Paths.get("threats.mxy"));
 * }
 * }</pre>
 * 
 * <p>Thread-safety: DatabaseBuilder is NOT thread-safe. Use from a single thread.
 */
public class DatabaseBuilder implements AutoCloseable {
    
    private Pointer handle;
    private final Gson gson;
    private volatile boolean closed = false;
    
    /**
     * Create a new database builder.
     * 
     * @throws MatchyException if builder cannot be created
     */
    public DatabaseBuilder() throws MatchyException {
        this.handle = MatchyLibrary.INSTANCE.matchy_builder_new();
        if (handle == null) {
            throw new MatchyException("Failed to create database builder");
        }
        this.gson = new Gson();
    }
    
    /**
     * Add an entry with associated data.
     * 
     * <p>Automatically detects whether the key is an IP address, CIDR range, or pattern.
     * 
     * @param key IP address (e.g., "1.2.3.4"), CIDR (e.g., "10.0.0.0/8"), 
     *            or pattern (e.g., "*.evil.com")
     * @param data Associated data (will be converted to JSON)
     * @return this builder for chaining
     * @throws MatchyException if entry cannot be added or builder is closed
     */
    public DatabaseBuilder add(String key, Map<String, Object> data) throws MatchyException {
        checkNotClosed();
        Objects.requireNonNull(key, "key cannot be null");
        Objects.requireNonNull(data, "data cannot be null");
        
        String json = gson.toJson(data);
        return addJson(key, json);
    }
    
    /**
     * Add an entry with associated data as JSON string.
     * 
     * <p>Automatically detects whether the key is an IP address, CIDR range, or pattern.
     * 
     * @param key IP address (e.g., "1.2.3.4"), CIDR (e.g., "10.0.0.0/8"), 
     *            or pattern (e.g., "*.evil.com")
     * @param jsonData Associated data as JSON string
     * @return this builder for chaining
     * @throws MatchyException if entry cannot be added or builder is closed
     */
    public DatabaseBuilder addJson(String key, String jsonData) throws MatchyException {
        checkNotClosed();
        Objects.requireNonNull(key, "key cannot be null");
        Objects.requireNonNull(jsonData, "jsonData cannot be null");
        
        int result = MatchyLibrary.INSTANCE.matchy_builder_add(handle, key, jsonData);
        
        if (result != MatchyLibrary.MATCHY_SUCCESS) {
            throw new MatchyException("Failed to add entry '" + key + "': " + getErrorMessage(result));
        }
        
        return this;
    }
    
    /**
     * Set database description.
     * 
     * @param description Description text
     * @return this builder for chaining
     * @throws MatchyException if description cannot be set or builder is closed
     */
    public DatabaseBuilder setDescription(String description) throws MatchyException {
        checkNotClosed();
        Objects.requireNonNull(description, "description cannot be null");
        
        int result = MatchyLibrary.INSTANCE.matchy_builder_set_description(handle, description);
        
        if (result != MatchyLibrary.MATCHY_SUCCESS) {
            throw new MatchyException("Failed to set description: " + getErrorMessage(result));
        }
        
        return this;
    }
    
    /**
     * Build and save database to file.
     * 
     * @param path Path where database should be saved
     * @throws MatchyException if database cannot be saved or builder is closed
     */
    public void save(Path path) throws MatchyException {
        checkNotClosed();
        Objects.requireNonNull(path, "path cannot be null");
        
        int result = MatchyLibrary.INSTANCE.matchy_builder_save(handle, path.toString());
        
        if (result != MatchyLibrary.MATCHY_SUCCESS) {
            throw new MatchyException("Failed to save database to '" + path + "': " + getErrorMessage(result));
        }
    }
    
    /**
     * Build database in memory and return as byte array.
     * 
     * <p>The returned byte array can be used with {@link Database#fromBuffer(byte[])}.
     * 
     * @return Database as byte array
     * @throws MatchyException if database cannot be built or builder is closed
     */
    public byte[] toBytes() throws MatchyException {
        checkNotClosed();
        
        // Allocate memory for output pointers (8 bytes each for 64-bit pointers)
        // Memory constructor already zeros the allocation
        Memory bufferPtr = new Memory(8);
        Memory sizePtr = new Memory(8);
        
        int result = MatchyLibrary.INSTANCE.matchy_builder_build(handle, bufferPtr, sizePtr);
        
        if (result != MatchyLibrary.MATCHY_SUCCESS) {
            throw new MatchyException("Failed to build database: " + getErrorMessage(result));
        }
        
        // Read the buffer pointer and size
        Pointer buffer = bufferPtr.getPointer(0);
        long size = sizePtr.getLong(0);
        
        if (Boolean.getBoolean("matchy.debug")) {
            System.err.println("matchy_builder_build returned: buffer=" + buffer + ", size=" + size);
        }
        
        if (buffer == null || size == 0) {
            throw new MatchyException("Failed to build database: null buffer returned");
        }
        
        // Copy to byte array
        byte[] data = buffer.getByteArray(0, (int) size);
        
        // Free the native buffer (allocated by Rust using libc::malloc)
        // JNA's Native.free calls standard free()
        Native.free(Pointer.nativeValue(buffer));
        
        return data;
    }
    
    /**
     * Build database in memory.
     * 
     * <p>Convenience method that calls {@link #toBytes()} and creates a Database instance.
     * 
     * @return In-memory database
     * @throws MatchyException if database cannot be built or builder is closed
     */
    public Database build() throws MatchyException {
        byte[] data = toBytes();
        return Database.fromBuffer(data);
    }
    
    /**
     * Free builder resources.
     * 
     * <p>After calling this method, no other methods can be called on this instance.
     * Calling close() multiple times is safe (subsequent calls are no-ops).
     */
    @Override
    public void close() {
        if (!closed && handle != null) {
            closed = true;
            MatchyLibrary.INSTANCE.matchy_builder_free(handle);
            handle = null;
        }
    }
    
    /**
     * Check if builder is closed.
     * 
     * @return true if closed
     */
    public boolean isClosed() {
        return closed;
    }
    
    /**
     * Check that builder is not closed, throwing exception if it is.
     */
    private void checkNotClosed() throws MatchyException {
        if (closed) {
            throw new MatchyException("DatabaseBuilder is closed");
        }
    }
    
    /**
     * Convert error code to human-readable message.
     */
    private String getErrorMessage(int errorCode) {
        switch (errorCode) {
            case MatchyLibrary.MATCHY_ERROR_FILE_NOT_FOUND:
                return "File not found";
            case MatchyLibrary.MATCHY_ERROR_INVALID_FORMAT:
                return "Invalid format";
            case MatchyLibrary.MATCHY_ERROR_CORRUPT_DATA:
                return "Corrupt data";
            case MatchyLibrary.MATCHY_ERROR_OUT_OF_MEMORY:
                return "Out of memory";
            case MatchyLibrary.MATCHY_ERROR_INVALID_PARAM:
                return "Invalid parameter";
            case MatchyLibrary.MATCHY_ERROR_IO:
                return "I/O error";
            default:
                return "Error code " + errorCode;
        }
    }
}
