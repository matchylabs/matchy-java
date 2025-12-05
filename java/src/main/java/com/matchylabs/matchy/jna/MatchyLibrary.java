package com.matchylabs.matchy.jna;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Pointer;

import java.io.File;

/**
 * JNA interface to the matchy C API.
 * 
 * This interface declares native methods that map directly to functions
 * in matchy.h. JNA handles the marshalling between Java and C.
 * 
 * <p><b>Internal API - not for public use.</b></p>
 */
public interface MatchyLibrary extends Library {
    
    // Singleton instance - loads library on first access
    MatchyLibrary INSTANCE = loadLibrary();
    
    // Error codes (from matchy.h)
    int MATCHY_SUCCESS = 0;
    int MATCHY_ERROR_FILE_NOT_FOUND = -1;
    int MATCHY_ERROR_INVALID_FORMAT = -2;
    int MATCHY_ERROR_CORRUPT_DATA = -3;
    int MATCHY_ERROR_OUT_OF_MEMORY = -4;
    int MATCHY_ERROR_INVALID_PARAM = -5;
    int MATCHY_ERROR_IO = -6;
    
    // Database operations
    Pointer matchy_open(String filename);
    Pointer matchy_open_with_options(String filename, NativeStructs.MatchyOpenOptions options);
    Pointer matchy_open_buffer(Pointer buffer, long size);
    void matchy_close(Pointer db);
    
    // Query operations
    NativeStructs.MatchyResult matchy_query(Pointer db, String query);
    void matchy_query_into(Pointer db, String query, Pointer result);
    void matchy_free_result(Pointer result);
    
    // Result conversion
    String matchy_result_to_json(Pointer result);
    void matchy_free_string(Pointer string);
    
    // Database builder operations
    Pointer matchy_builder_new();
    int matchy_builder_add(Pointer builder, String key, String json_data);
    int matchy_builder_set_description(Pointer builder, String description);
    int matchy_builder_save(Pointer builder, String filename);
    int matchy_builder_build(Pointer builder, Pointer buffer_out, Pointer size_out);
    void matchy_builder_free(Pointer builder);
    
    // Stats and metadata
    void matchy_get_stats(Pointer db, NativeStructs.MatchyStats stats);
    void matchy_clear_cache(Pointer db);
    String matchy_metadata(Pointer db);
    
    // Database introspection
    boolean matchy_has_ip_data(Pointer db);
    boolean matchy_has_string_data(Pointer db);
    boolean matchy_has_literal_data(Pointer db);
    boolean matchy_has_glob_data(Pointer db);
    String matchy_format(Pointer db);
    
    // Pattern operations
    String matchy_get_pattern_string(Pointer db, int pattern_id);
    long matchy_pattern_count(Pointer db);
    
    // Version
    String matchy_version();
    
    // Extractor operations
    Pointer matchy_extractor_create(int flags);
    int matchy_extractor_extract_chunk(Pointer extractor, Pointer data, long len, NativeStructs.MatchyMatches matches);
    void matchy_matches_free(NativeStructs.MatchyMatches matches);
    void matchy_extractor_free(Pointer extractor);
    String matchy_item_type_name(byte item_type);
    
    /**
     * Load the native library and return the JNA interface.
     * 
     * @return MatchyLibrary instance
     * @throws UnsatisfiedLinkError if library cannot be loaded
     */
    static MatchyLibrary loadLibrary() {
        // Prepare native library (extract from JAR if needed)
        String libraryPath = NativeLoader.prepareLibrary();
        
        // If we extracted to a temp directory, tell JNA where to look
        if (libraryPath != null) {
            String currentPath = System.getProperty("jna.library.path", "");
            if (currentPath.isEmpty()) {
                System.setProperty("jna.library.path", libraryPath);
            } else {
                System.setProperty("jna.library.path", libraryPath + File.pathSeparator + currentPath);
            }
        }
        
        // Let JNA load the library
        return Native.load("matchy", MatchyLibrary.class);
    }
}
