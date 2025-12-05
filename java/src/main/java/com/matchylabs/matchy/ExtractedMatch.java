package com.matchylabs.matchy;

import java.util.Objects;

/**
 * A single match extracted from text.
 * 
 * <p>Contains the extracted value, its type, and the byte offsets
 * in the original input where it was found.
 */
public final class ExtractedMatch {
    
    private final ItemType itemType;
    private final String value;
    private final long start;
    private final long end;
    
    /**
     * Create a new extracted match.
     * 
     * @param itemType the type of item extracted
     * @param value the extracted value
     * @param start byte offset where the match starts (0-indexed)
     * @param end byte offset where the match ends (exclusive)
     */
    public ExtractedMatch(ItemType itemType, String value, long start, long end) {
        this.itemType = Objects.requireNonNull(itemType, "itemType cannot be null");
        this.value = Objects.requireNonNull(value, "value cannot be null");
        this.start = start;
        this.end = end;
    }
    
    /**
     * Get the type of extracted item.
     * 
     * @return item type
     */
    public ItemType getItemType() {
        return itemType;
    }
    
    /**
     * Get the extracted value.
     * 
     * @return the extracted string
     */
    public String getValue() {
        return value;
    }
    
    /**
     * Get the byte offset where this match starts in the input.
     * 
     * @return start offset (0-indexed)
     */
    public long getStart() {
        return start;
    }
    
    /**
     * Get the byte offset where this match ends in the input.
     * 
     * @return end offset (exclusive)
     */
    public long getEnd() {
        return end;
    }
    
    @Override
    public String toString() {
        return String.format("ExtractedMatch{type=%s, value='%s', start=%d, end=%d}",
            itemType, value, start, end);
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ExtractedMatch that = (ExtractedMatch) o;
        return start == that.start && 
               end == that.end && 
               itemType == that.itemType && 
               value.equals(that.value);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(itemType, value, start, end);
    }
}
