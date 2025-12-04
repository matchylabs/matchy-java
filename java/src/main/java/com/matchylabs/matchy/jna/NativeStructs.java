package com.matchylabs.matchy.jna;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;

import java.util.Arrays;
import java.util.List;

/**
 * JNA structure mappings for matchy C API.
 * 
 * These structures match the layout of C structs defined in matchy.h.
 * 
 * <p><b>Internal API - not for public use.</b></p>
 */
public class NativeStructs {
    
    /**
     * Maps to matchy_result_t in C.
     */
    @Structure.FieldOrder({"found", "prefix_len", "_data_cache", "_db_ref"})
    public static class MatchyResult extends Structure {
        public boolean found;
        public byte prefix_len;
        public Pointer _data_cache;
        public Pointer _db_ref;
    }
    
    /**
     * Maps to matchy_open_options_t in C.
     */
    @Structure.FieldOrder({"cache_capacity", "auto_reload", "reload_callback", "reload_callback_user_data"})
    public static class MatchyOpenOptions extends Structure {
        public int cache_capacity;
        public boolean auto_reload;
        public Pointer reload_callback;  // Function pointer (not used in initial version)
        public Pointer reload_callback_user_data;
        
        public MatchyOpenOptions() {
            super();
        }
    }
    
    /**
     * Maps to matchy_stats_t in C.
     */
    @Structure.FieldOrder({
        "total_queries", "queries_with_match", "queries_without_match",
        "cache_hits", "cache_misses", "ip_queries", "string_queries"
    })
    public static class MatchyStats extends Structure {
        public long total_queries;
        public long queries_with_match;
        public long queries_without_match;
        public long cache_hits;
        public long cache_misses;
        public long ip_queries;
        public long string_queries;
        
        public MatchyStats() {
            super();
        }
    }
}
