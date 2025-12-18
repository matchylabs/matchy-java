package com.matchylabs.matchy.jna;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.Union;

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
    
    // Validation level constants
    public static final int MATCHY_VALIDATION_STANDARD = 0;
    public static final int MATCHY_VALIDATION_STRICT = 1;
    
    // Data type constants (MMDB format)
    public static final int MATCHY_DATA_TYPE_EXTENDED = 0;
    public static final int MATCHY_DATA_TYPE_POINTER = 1;
    public static final int MATCHY_DATA_TYPE_UTF8_STRING = 2;
    public static final int MATCHY_DATA_TYPE_DOUBLE = 3;
    public static final int MATCHY_DATA_TYPE_BYTES = 4;
    public static final int MATCHY_DATA_TYPE_UINT16 = 5;
    public static final int MATCHY_DATA_TYPE_UINT32 = 6;
    public static final int MATCHY_DATA_TYPE_MAP = 7;
    public static final int MATCHY_DATA_TYPE_INT32 = 8;
    public static final int MATCHY_DATA_TYPE_UINT64 = 9;
    public static final int MATCHY_DATA_TYPE_UINT128 = 10;
    public static final int MATCHY_DATA_TYPE_ARRAY = 11;
    public static final int MATCHY_DATA_TYPE_BOOLEAN = 14;
    public static final int MATCHY_DATA_TYPE_FLOAT = 15;
    
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
    @Structure.FieldOrder({
        "cache_capacity", "auto_reload", "auto_update", 
        "update_interval_secs", "cache_dir",
        "reload_callback", "reload_callback_user_data"
    })
    public static class MatchyOpenOptions extends Structure {
        public int cache_capacity;
        public boolean auto_reload;
        public boolean auto_update;
        public int update_interval_secs;
        public String cache_dir;
        public Pointer reload_callback;
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
    
    @Structure.FieldOrder({"db", "data_ptr"})
    public static class MatchyEntry extends Structure {
        public Pointer db;
        public Pointer data_ptr;
        
        public MatchyEntry() {
            super();
        }
        
        public MatchyEntry(Pointer p) {
            super(p);
            read();
        }
    }
    
    public static class MatchyEntryDataValue extends Union {
        public int pointer;
        public Pointer utf8_string;
        public double double_value;
        public Pointer bytes;
        public short uint16;
        public int uint32;
        public int int32;
        public long uint64;
        public byte[] uint128 = new byte[16];
        public byte boolean_value;
        public float float_value;
        
        public MatchyEntryDataValue() {
            super();
        }
        
        public MatchyEntryDataValue(Pointer p) {
            super(p);
        }
        
        public String getUtf8String() {
            return utf8_string != null ? utf8_string.getString(0) : null;
        }
        
        public byte[] getBytes(int size) {
            return bytes != null ? bytes.getByteArray(0, size) : null;
        }
        
        public boolean getBoolean() {
            return boolean_value != 0;
        }
    }
    
    @Structure.FieldOrder({"has_data", "type_", "value", "data_size", "offset"})
    public static class MatchyEntryData extends Structure {
        public byte has_data;
        public int type_;
        public MatchyEntryDataValue value;
        public int data_size;
        public int offset;
        
        public MatchyEntryData() {
            super();
        }
        
        public MatchyEntryData(Pointer p) {
            super(p);
            read();
        }
        
        public boolean hasData() {
            return has_data != 0;
        }
        
        public void setActiveUnionField() {
            switch (type_) {
                case MATCHY_DATA_TYPE_POINTER:
                case MATCHY_DATA_TYPE_UINT32:
                    value.setType(int.class);
                    break;
                case MATCHY_DATA_TYPE_UTF8_STRING:
                case MATCHY_DATA_TYPE_BYTES:
                    value.setType(Pointer.class);
                    break;
                case MATCHY_DATA_TYPE_DOUBLE:
                    value.setType(double.class);
                    break;
                case MATCHY_DATA_TYPE_UINT16:
                    value.setType(short.class);
                    break;
                case MATCHY_DATA_TYPE_INT32:
                    value.setType(int.class);
                    break;
                case MATCHY_DATA_TYPE_UINT64:
                    value.setType(long.class);
                    break;
                case MATCHY_DATA_TYPE_UINT128:
                    value.setType(byte[].class);
                    break;
                case MATCHY_DATA_TYPE_BOOLEAN:
                    value.setType(byte.class);
                    break;
                case MATCHY_DATA_TYPE_FLOAT:
                    value.setType(float.class);
                    break;
            }
            value.read();
        }
    }
    
    @Structure.FieldOrder({"entry_data", "next"})
    public static class MatchyEntryDataList extends Structure {
        public MatchyEntryData entry_data;
        public Pointer next;
        
        public MatchyEntryDataList() {
            super();
        }
        
        public MatchyEntryDataList(Pointer p) {
            super(p);
            read();
        }
        
        public MatchyEntryDataList getNext() {
            if (next == null || next == Pointer.NULL) {
                return null;
            }
            return new MatchyEntryDataList(next);
        }
    }
    
    @Structure.FieldOrder({"path", "success", "error", "generation"})
    public static class MatchyReloadEvent extends Structure {
        public String path;
        public byte success;
        public String error;
        public long generation;
        
        public MatchyReloadEvent() {
            super();
        }
        
        public MatchyReloadEvent(Pointer p) {
            super(p);
            read();
        }
        
        public boolean isSuccess() {
            return success != 0;
        }
    }
}
