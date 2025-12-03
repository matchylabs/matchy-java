package com.matchylabs.matchy;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

/**
 * Result of a database query.
 * 
 * Contains the match status, optional data payload, and metadata like
 * network prefix length for IP matches.
 */
public class QueryResult {
    
    private static final Gson GSON = new Gson();
    
    private final boolean found;
    private final int prefixLen;
    private final JsonObject data;
    
    private QueryResult(boolean found, int prefixLen, JsonObject data) {
        this.found = found;
        this.prefixLen = prefixLen;
        this.data = data;
    }
    
    /**
     * Create a "not found" result.
     *
     * @return QueryResult indicating no match
     */
    public static QueryResult notFound() {
        return new QueryResult(false, 0, null);
    }
    
    /**
     * Create a result from JSON data.
     *
     * @param json JSON string containing match data
     * @param prefixLen network prefix length (for IP matches)
     * @return QueryResult with parsed data
     */
    static QueryResult fromJson(String json, int prefixLen) {
        if (json == null || json.isEmpty()) {
            return new QueryResult(true, prefixLen, new JsonObject());
        }
        
        try {
            JsonObject data = GSON.fromJson(json, JsonObject.class);
            return new QueryResult(true, prefixLen, data);
        } catch (Exception e) {
            // If JSON parsing fails, treat as empty data
            return new QueryResult(true, prefixLen, new JsonObject());
        }
    }
    
    /**
     * Check if a match was found.
     *
     * @return true if query matched, false otherwise
     */
    public boolean isMatch() {
        return found;
    }
    
    /**
     * Get the network prefix length (for IP matches).
     * 
     * For IPv4, this is 0-32. For IPv6, this is 0-128.
     * For non-IP matches, this is 0.
     *
     * @return network prefix length
     */
    public int getPrefixLength() {
        return prefixLen;
    }
    
    /**
     * Get the match data as a JsonObject.
     * 
     * Returns null if no match was found, or an empty JsonObject
     * if the match has no data.
     *
     * @return match data, or null if not found
     */
    public JsonObject getData() {
        return data;
    }
    
    /**
     * Get the match data as a JSON string.
     * 
     * Returns null if no match was found.
     *
     * @return JSON string, or null if not found
     */
    public String getDataAsJson() {
        if (data == null) {
            return null;
        }
        return GSON.toJson(data);
    }
    
    @Override
    public String toString() {
        if (!found) {
            return "QueryResult{found=false}";
        }
        return String.format("QueryResult{found=true, prefixLen=%d, data=%s}", 
            prefixLen, data);
    }
}
