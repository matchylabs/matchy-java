package com.matchylabs.matchy;

import com.matchylabs.matchy.jna.NativeStructs;

/**
 * Types of items that can be extracted from text.
 */
public enum ItemType {
    /** Domain name (e.g., "example.com") */
    DOMAIN(NativeStructs.MATCHY_ITEM_TYPE_DOMAIN),
    
    /** Email address (e.g., "user@example.com") */
    EMAIL(NativeStructs.MATCHY_ITEM_TYPE_EMAIL),
    
    /** IPv4 address (e.g., "192.168.1.1") */
    IPV4(NativeStructs.MATCHY_ITEM_TYPE_IPV4),
    
    /** IPv6 address (e.g., "2001:db8::1") */
    IPV6(NativeStructs.MATCHY_ITEM_TYPE_IPV6),
    
    /** MD5 hash (32 hex characters) */
    MD5(NativeStructs.MATCHY_ITEM_TYPE_MD5),
    
    /** SHA1 hash (40 hex characters) */
    SHA1(NativeStructs.MATCHY_ITEM_TYPE_SHA1),
    
    /** SHA256 hash (64 hex characters) */
    SHA256(NativeStructs.MATCHY_ITEM_TYPE_SHA256),
    
    /** SHA384 hash (96 hex characters) */
    SHA384(NativeStructs.MATCHY_ITEM_TYPE_SHA384),
    
    /** SHA512 hash (128 hex characters) */
    SHA512(NativeStructs.MATCHY_ITEM_TYPE_SHA512),
    
    /** Bitcoin address */
    BITCOIN(NativeStructs.MATCHY_ITEM_TYPE_BITCOIN),
    
    /** Ethereum address */
    ETHEREUM(NativeStructs.MATCHY_ITEM_TYPE_ETHEREUM),
    
    /** Monero address */
    MONERO(NativeStructs.MATCHY_ITEM_TYPE_MONERO);
    
    private final int nativeValue;
    
    ItemType(int nativeValue) {
        this.nativeValue = nativeValue;
    }
    
    /**
     * Get the native C API value for this item type.
     * 
     * @return native constant value
     */
    public int getNativeValue() {
        return nativeValue;
    }
    
    /**
     * Convert a native item type value to the enum.
     * 
     * @param nativeValue the native constant value
     * @return corresponding ItemType, or null if unknown
     */
    public static ItemType fromNative(int nativeValue) {
        for (ItemType type : values()) {
            if (type.nativeValue == nativeValue) {
                return type;
            }
        }
        return null;
    }
}
