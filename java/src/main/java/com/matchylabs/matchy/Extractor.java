package com.matchylabs.matchy;

import com.matchylabs.matchy.jna.MatchyLibrary;
import com.matchylabs.matchy.jna.NativeStructs;
import com.sun.jna.Memory;
import com.sun.jna.Pointer;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Extracts indicators of compromise (IoCs) from text.
 * 
 * <p>The extractor can find domains, email addresses, IP addresses, file hashes,
 * and cryptocurrency addresses in a single pass over the input data.
 * 
 * <p>Example usage:
 * <pre>{@code
 * try (Extractor extractor = Extractor.create(ExtractFlags.ALL)) {
 *     List<ExtractedMatch> matches = extractor.extract("Check evil.com and 192.168.1.1");
 *     for (ExtractedMatch match : matches) {
 *         System.out.println(match.getItemType() + ": " + match.getValue());
 *     }
 * }
 * }</pre>
 * 
 * <p>For efficiency, you can also extract specific types:
 * <pre>{@code
 * int flags = ExtractFlags.DOMAINS | ExtractFlags.IPV4 | ExtractFlags.IPV6;
 * try (Extractor extractor = Extractor.create(flags)) {
 *     // Only extracts domains and IP addresses
 *     List<ExtractedMatch> matches = extractor.extract(text);
 * }
 * }</pre>
 * 
 * <p>Thread-safety: Extractor instances are thread-safe for extraction.
 * Multiple threads can safely call extract() concurrently.
 */
public class Extractor implements AutoCloseable {
    
    private final Pointer handle;
    private volatile boolean closed = false;
    
    /**
     * Private constructor - use static factory methods.
     */
    private Extractor(Pointer handle) {
        this.handle = Objects.requireNonNull(handle, "Extractor handle is null");
    }
    
    /**
     * Create an extractor that extracts all supported types.
     * 
     * @return new Extractor instance
     * @throws MatchyException if the extractor cannot be created
     */
    public static Extractor create() throws MatchyException {
        return create(ExtractFlags.ALL);
    }
    
    /**
     * Create an extractor with specified extraction types.
     * 
     * @param flags bitmask of ExtractFlags values
     * @return new Extractor instance
     * @throws MatchyException if the extractor cannot be created
     * @see ExtractFlags
     */
    public static Extractor create(int flags) throws MatchyException {
        Pointer handle = MatchyLibrary.INSTANCE.matchy_extractor_create(flags);
        if (handle == null) {
            throw new MatchyException("Failed to create extractor");
        }
        return new Extractor(handle);
    }
    
    /**
     * Extract matches from a string.
     * 
     * @param text the text to search
     * @return list of extracted matches (may be empty, never null)
     * @throws MatchyException if extraction fails or extractor is closed
     */
    public List<ExtractedMatch> extract(String text) throws MatchyException {
        Objects.requireNonNull(text, "text cannot be null");
        return extract(text.getBytes(StandardCharsets.UTF_8));
    }
    
    /**
     * Extract matches from a byte array.
     * 
     * @param data the data to search
     * @return list of extracted matches (may be empty, never null)
     * @throws MatchyException if extraction fails or extractor is closed
     */
    public List<ExtractedMatch> extract(byte[] data) throws MatchyException {
        checkNotClosed();
        Objects.requireNonNull(data, "data cannot be null");
        
        if (data.length == 0) {
            return Collections.emptyList();
        }
        
        // Copy data to native memory
        Memory nativeData = new Memory(data.length);
        nativeData.write(0, data, 0, data.length);
        
        // Allocate the matches structure
        NativeStructs.MatchyMatches matches = new NativeStructs.MatchyMatches();
        
        try {
            int result = MatchyLibrary.INSTANCE.matchy_extractor_extract_chunk(
                handle, nativeData, data.length, matches);
            
            if (result != MatchyLibrary.MATCHY_SUCCESS) {
                throw new MatchyException("Extraction failed with error code: " + result);
            }
            
            // Convert native matches to Java objects
            return convertMatches(matches);
        } finally {
            // Free the native matches
            MatchyLibrary.INSTANCE.matchy_matches_free(matches);
        }
    }
    
    /**
     * Convert native matches structure to Java list.
     */
    private List<ExtractedMatch> convertMatches(NativeStructs.MatchyMatches matches) {
        if (matches.count == 0 || matches.items == null) {
            return Collections.emptyList();
        }
        
        List<ExtractedMatch> result = new ArrayList<>((int) matches.count);
        
        // Calculate the size of each MatchyMatch structure
        NativeStructs.MatchyMatch template = new NativeStructs.MatchyMatch();
        int structSize = template.size();
        
        for (int i = 0; i < matches.count; i++) {
            // Read each match from the array
            Pointer matchPtr = matches.items.share(i * structSize);
            NativeStructs.MatchyMatch nativeMatch = new NativeStructs.MatchyMatch(matchPtr);
            
            ItemType itemType = ItemType.fromNative(nativeMatch.item_type & 0xFF);
            if (itemType != null && nativeMatch.value != null) {
                result.add(new ExtractedMatch(
                    itemType,
                    nativeMatch.value,
                    nativeMatch.start,
                    nativeMatch.end
                ));
            }
        }
        
        return result;
    }
    
    /**
     * Close the extractor and free all resources.
     * 
     * <p>After calling this method, no other methods can be called on this instance.
     * Calling close() multiple times is safe (subsequent calls are no-ops).
     */
    @Override
    public void close() {
        if (!closed) {
            closed = true;
            MatchyLibrary.INSTANCE.matchy_extractor_free(handle);
        }
    }
    
    /**
     * Check if extractor is closed.
     * 
     * @return true if closed
     */
    public boolean isClosed() {
        return closed;
    }
    
    /**
     * Check that extractor is not closed, throwing exception if it is.
     */
    private void checkNotClosed() throws MatchyException {
        if (closed) {
            throw new MatchyException("Extractor is closed");
        }
    }
}
