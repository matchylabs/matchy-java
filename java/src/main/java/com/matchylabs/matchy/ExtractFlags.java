package com.matchylabs.matchy;

import com.matchylabs.matchy.jna.NativeStructs;

/**
 * Flags to configure what types of items to extract.
 * 
 * <p>Combine multiple flags with bitwise OR:
 * <pre>{@code
 * int flags = ExtractFlags.DOMAINS | ExtractFlags.IPV4 | ExtractFlags.IPV6;
 * Extractor extractor = Extractor.create(flags);
 * }</pre>
 */
public final class ExtractFlags {
    
    private ExtractFlags() {
        // Non-instantiable
    }
    
    /** Extract domain names (e.g., "example.com") */
    public static final int DOMAINS = NativeStructs.MATCHY_EXTRACT_DOMAINS;
    
    /** Extract email addresses (e.g., "user@example.com") */
    public static final int EMAILS = NativeStructs.MATCHY_EXTRACT_EMAILS;
    
    /** Extract IPv4 addresses */
    public static final int IPV4 = NativeStructs.MATCHY_EXTRACT_IPV4;
    
    /** Extract IPv6 addresses */
    public static final int IPV6 = NativeStructs.MATCHY_EXTRACT_IPV6;
    
    /** Extract file hashes (MD5, SHA1, SHA256, SHA384, SHA512) */
    public static final int HASHES = NativeStructs.MATCHY_EXTRACT_HASHES;
    
    /** Extract Bitcoin addresses */
    public static final int BITCOIN = NativeStructs.MATCHY_EXTRACT_BITCOIN;
    
    /** Extract Ethereum addresses */
    public static final int ETHEREUM = NativeStructs.MATCHY_EXTRACT_ETHEREUM;
    
    /** Extract Monero addresses */
    public static final int MONERO = NativeStructs.MATCHY_EXTRACT_MONERO;
    
    /** Extract all supported types */
    public static final int ALL = NativeStructs.MATCHY_EXTRACT_ALL;
}
