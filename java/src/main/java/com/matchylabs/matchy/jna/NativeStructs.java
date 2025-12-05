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
    
    // Extraction type flags
    public static final int MATCHY_EXTRACT_DOMAINS = 1 << 0;
    public static final int MATCHY_EXTRACT_EMAILS = 1 << 1;
    public static final int MATCHY_EXTRACT_IPV4 = 1 << 2;
    public static final int MATCHY_EXTRACT_IPV6 = 1 << 3;
    public static final int MATCHY_EXTRACT_HASHES = 1 << 4;
    public static final int MATCHY_EXTRACT_BITCOIN = 1 << 5;
    public static final int MATCHY_EXTRACT_ETHEREUM = 1 << 6;
    public static final int MATCHY_EXTRACT_MONERO = 1 << 7;
    public static final int MATCHY_EXTRACT_ALL = 255;
    
    // Item type constants
    public static final int MATCHY_ITEM_TYPE_DOMAIN = 0;
    public static final int MATCHY_ITEM_TYPE_EMAIL = 1;
    public static final int MATCHY_ITEM_TYPE_IPV4 = 2;
    public static final int MATCHY_ITEM_TYPE_IPV6 = 3;
    public static final int MATCHY_ITEM_TYPE_MD5 = 4;
    public static final int MATCHY_ITEM_TYPE_SHA1 = 5;
    public static final int MATCHY_ITEM_TYPE_SHA256 = 6;
    public static final int MATCHY_ITEM_TYPE_SHA384 = 7;
    public static final int MATCHY_ITEM_TYPE_SHA512 = 8;
    public static final int MATCHY_ITEM_TYPE_BITCOIN = 9;
    public static final int MATCHY_ITEM_TYPE_ETHEREUM = 10;
    public static final int MATCHY_ITEM_TYPE_MONERO = 11;
    
    /**
     * Maps to matchy_result_t in C.
     */
    @Structure.FieldOrder({"found", "prefix_len", "_data_cache", "_db_ref"})
    public static class MatchyResult extends Structure {
        /** Whether a match was found (C bool = 1 byte) */
        public byte found;
        /** Network prefix length for IP results */
        public byte prefix_len;
        /** Internal pointer to cached DataValue */
        public Pointer _data_cache;
        /** Internal database reference */
        public Pointer _db_ref;
        
        public MatchyResult() {
            super();
        }
        
        public MatchyResult(Pointer p) {
            super(p);
            read();
        }
        
        /** Check if a match was found */
        public boolean isFound() {
            return found != 0;
        }
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
    
    /**
     * Maps to matchy_match_t in C.
     * A single extracted match.
     */
    @Structure.FieldOrder({"item_type", "value", "start", "end"})
    public static class MatchyMatch extends Structure {
        /** Item type (one of MATCHY_ITEM_TYPE_* constants) */
        public byte item_type;
        /** The extracted value as a null-terminated string */
        public String value;
        /** Byte offset where the match starts in the input */
        public long start;
        /** Byte offset where the match ends in the input (exclusive) */
        public long end;
        
        public MatchyMatch() {
            super();
        }
        
        public MatchyMatch(Pointer p) {
            super(p);
            read();
        }
    }
    
    /**
     * Maps to matchy_matches_t in C.
     * Array of extracted matches.
     */
    @Structure.FieldOrder({"items", "count", "_internal"})
    public static class MatchyMatches extends Structure {
        /** Pointer to array of matches */
        public Pointer items;
        /** Number of matches */
        public long count;
        /** Internal pointer (do not use) */
        public Pointer _internal;
        
        public MatchyMatches() {
            super();
        }
        
        public MatchyMatches(Pointer p) {
            super(p);
            read();
        }
    }
}
