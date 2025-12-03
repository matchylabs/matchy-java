package com.matchylabs.matchy;

import com.matchylabs.matchy.jna.NativeStructs;

/**
 * Statistics about database query performance.
 * 
 * Provides metrics on query counts, cache effectiveness, and query types.
 */
public class DatabaseStats {
    
    private final long totalQueries;
    private final long queriesWithMatch;
    private final long queriesWithoutMatch;
    private final long cacheHits;
    private final long cacheMisses;
    private final long ipQueries;
    private final long stringQueries;
    
    private DatabaseStats(long totalQueries, long queriesWithMatch, long queriesWithoutMatch,
                         long cacheHits, long cacheMisses, long ipQueries, long stringQueries) {
        this.totalQueries = totalQueries;
        this.queriesWithMatch = queriesWithMatch;
        this.queriesWithoutMatch = queriesWithoutMatch;
        this.cacheHits = cacheHits;
        this.cacheMisses = cacheMisses;
        this.ipQueries = ipQueries;
        this.stringQueries = stringQueries;
    }
    
    /**
     * Create DatabaseStats from native structure.
     *
     * @param nativeStats native stats structure
     * @return DatabaseStats instance
     */
    static DatabaseStats fromNative(NativeStructs.MatchyStats nativeStats) {
        return new DatabaseStats(
            nativeStats.total_queries,
            nativeStats.queries_with_match,
            nativeStats.queries_without_match,
            nativeStats.cache_hits,
            nativeStats.cache_misses,
            nativeStats.ip_queries,
            nativeStats.string_queries
        );
    }
    
    /**
     * Get total number of queries executed.
     *
     * @return total query count
     */
    public long getTotalQueries() {
        return totalQueries;
    }
    
    /**
     * Get number of queries that found a match.
     *
     * @return query count with matches
     */
    public long getQueriesWithMatch() {
        return queriesWithMatch;
    }
    
    /**
     * Get number of queries that found no match.
     *
     * @return query count without matches
     */
    public long getQueriesWithoutMatch() {
        return queriesWithoutMatch;
    }
    
    /**
     * Get number of cache hits.
     * 
     * Cache hits are queries served from the LRU cache without
     * performing a full lookup.
     *
     * @return cache hit count
     */
    public long getCacheHits() {
        return cacheHits;
    }
    
    /**
     * Get number of cache misses.
     * 
     * Cache misses are queries that required a full lookup.
     *
     * @return cache miss count
     */
    public long getCacheMisses() {
        return cacheMisses;
    }
    
    /**
     * Get number of IP address queries.
     *
     * @return IP query count
     */
    public long getIpQueries() {
        return ipQueries;
    }
    
    /**
     * Get number of string queries (patterns/literals).
     *
     * @return string query count
     */
    public long getStringQueries() {
        return stringQueries;
    }
    
    /**
     * Calculate cache hit rate.
     * 
     * Returns a value between 0.0 and 1.0, or 0.0 if no cache operations.
     *
     * @return cache hit rate (0.0 to 1.0)
     */
    public double getCacheHitRate() {
        long total = cacheHits + cacheMisses;
        if (total == 0) {
            return 0.0;
        }
        return (double) cacheHits / total;
    }
    
    @Override
    public String toString() {
        return String.format(
            "DatabaseStats{total=%d, matches=%d, noMatch=%d, cacheHits=%d, " +
            "cacheMisses=%d, hitRate=%.1f%%, ipQueries=%d, stringQueries=%d}",
            totalQueries, queriesWithMatch, queriesWithoutMatch, 
            cacheHits, cacheMisses, getCacheHitRate() * 100,
            ipQueries, stringQueries
        );
    }
}
