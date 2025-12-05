package com.matchylabs.matchy;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Extractor class.
 */
class ExtractorTest {
    
    /**
     * Test extracting domains from text.
     */
    @Test
    void testExtractDomains() throws Exception {
        try (Extractor extractor = Extractor.create(ExtractFlags.DOMAINS)) {
            List<ExtractedMatch> matches = extractor.extract(
                "Visit example.com and evil.example.org for more info.");
            
            assertFalse(matches.isEmpty());
            
            Set<String> domains = matches.stream()
                .filter(m -> m.getItemType() == ItemType.DOMAIN)
                .map(ExtractedMatch::getValue)
                .collect(Collectors.toSet());
            
            assertTrue(domains.contains("example.com"));
            assertTrue(domains.contains("evil.example.org"));
        }
    }
    
    /**
     * Test extracting IPv4 addresses.
     */
    @Test
    void testExtractIPv4() throws Exception {
        try (Extractor extractor = Extractor.create(ExtractFlags.IPV4)) {
            List<ExtractedMatch> matches = extractor.extract(
                "Server at 192.168.1.1 and 10.0.0.1");
            
            assertFalse(matches.isEmpty());
            
            Set<String> ips = matches.stream()
                .filter(m -> m.getItemType() == ItemType.IPV4)
                .map(ExtractedMatch::getValue)
                .collect(Collectors.toSet());
            
            assertTrue(ips.contains("192.168.1.1"));
            assertTrue(ips.contains("10.0.0.1"));
        }
    }
    
    /**
     * Test extracting IPv6 addresses.
     */
    @Test
    void testExtractIPv6() throws Exception {
        try (Extractor extractor = Extractor.create(ExtractFlags.IPV6)) {
            List<ExtractedMatch> matches = extractor.extract(
                "IPv6: 2001:db8::1 and ::ffff:192.168.1.1");
            
            assertFalse(matches.isEmpty());
            
            boolean foundIpv6 = matches.stream()
                .anyMatch(m -> m.getItemType() == ItemType.IPV6);
            assertTrue(foundIpv6);
        }
    }
    
    /**
     * Test extracting email addresses.
     */
    @Test
    void testExtractEmails() throws Exception {
        try (Extractor extractor = Extractor.create(ExtractFlags.EMAILS)) {
            List<ExtractedMatch> matches = extractor.extract(
                "Contact user@example.com or admin@test.org");
            
            assertFalse(matches.isEmpty());
            
            Set<String> emails = matches.stream()
                .filter(m -> m.getItemType() == ItemType.EMAIL)
                .map(ExtractedMatch::getValue)
                .collect(Collectors.toSet());
            
            assertTrue(emails.contains("user@example.com"));
            assertTrue(emails.contains("admin@test.org"));
        }
    }
    
    /**
     * Test extracting file hashes.
     */
    @Test
    void testExtractHashes() throws Exception {
        try (Extractor extractor = Extractor.create(ExtractFlags.HASHES)) {
            // MD5 (32 chars)
            String md5 = "d41d8cd98f00b204e9800998ecf8427e";
            // SHA256 (64 chars)
            String sha256 = "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855";
            
            List<ExtractedMatch> matches = extractor.extract(
                "MD5: " + md5 + " SHA256: " + sha256);
            
            assertFalse(matches.isEmpty());
            
            Set<String> hashes = matches.stream()
                .map(ExtractedMatch::getValue)
                .collect(Collectors.toSet());
            
            assertTrue(hashes.contains(md5));
            assertTrue(hashes.contains(sha256));
        }
    }
    
    /**
     * Test extracting all types at once.
     */
    @Test
    void testExtractAll() throws Exception {
        try (Extractor extractor = Extractor.create(ExtractFlags.ALL)) {
            String text = "Contact user@example.com at 192.168.1.1 about evil.com";
            
            List<ExtractedMatch> matches = extractor.extract(text);
            
            assertFalse(matches.isEmpty());
            
            // Should have found email, IPv4, and domain
            Set<ItemType> types = matches.stream()
                .map(ExtractedMatch::getItemType)
                .collect(Collectors.toSet());
            
            assertTrue(types.contains(ItemType.EMAIL));
            assertTrue(types.contains(ItemType.IPV4));
            assertTrue(types.contains(ItemType.DOMAIN));
        }
    }
    
    /**
     * Test that match offsets are correct.
     */
    @Test
    void testMatchOffsets() throws Exception {
        try (Extractor extractor = Extractor.create(ExtractFlags.IPV4)) {
            String text = "IP: 1.2.3.4 here";
            
            List<ExtractedMatch> matches = extractor.extract(text);
            assertEquals(1, matches.size());
            
            ExtractedMatch match = matches.get(0);
            assertEquals("1.2.3.4", match.getValue());
            assertEquals(4, match.getStart());  // Position of "1" after "IP: "
            assertEquals(11, match.getEnd());   // Position after "4"
            
            // Verify offset is correct
            String extracted = text.substring((int) match.getStart(), (int) match.getEnd());
            assertEquals("1.2.3.4", extracted);
        }
    }
    
    /**
     * Test empty input returns empty list.
     */
    @Test
    void testEmptyInput() throws Exception {
        try (Extractor extractor = Extractor.create()) {
            List<ExtractedMatch> matches = extractor.extract("");
            assertTrue(matches.isEmpty());
        }
    }
    
    /**
     * Test input with no matches returns empty list.
     */
    @Test
    void testNoMatches() throws Exception {
        try (Extractor extractor = Extractor.create(ExtractFlags.IPV4)) {
            List<ExtractedMatch> matches = extractor.extract(
                "This text has no IP addresses in it.");
            assertTrue(matches.isEmpty());
        }
    }
    
    /**
     * Test combining flags.
     */
    @Test
    void testCombinedFlags() throws Exception {
        int flags = ExtractFlags.DOMAINS | ExtractFlags.IPV4;
        
        try (Extractor extractor = Extractor.create(flags)) {
            List<ExtractedMatch> matches = extractor.extract(
                "Host: evil.com IP: 192.168.1.1 Email: user@test.com");
            
            // Should find domain and IP, but NOT email
            Set<ItemType> types = matches.stream()
                .map(ExtractedMatch::getItemType)
                .collect(Collectors.toSet());
            
            assertTrue(types.contains(ItemType.DOMAIN));
            assertTrue(types.contains(ItemType.IPV4));
            assertFalse(types.contains(ItemType.EMAIL));
        }
    }
    
    /**
     * Test that closed extractor throws exception.
     */
    @Test
    void testClosedExtractor() throws Exception {
        Extractor extractor = Extractor.create();
        extractor.close();
        
        assertTrue(extractor.isClosed());
        assertThrows(MatchyException.class, () -> extractor.extract("test"));
    }
    
    /**
     * Test closing multiple times is safe.
     */
    @Test
    void testDoubleClose() throws Exception {
        Extractor extractor = Extractor.create();
        extractor.close();
        extractor.close();  // Should not throw
        assertTrue(extractor.isClosed());
    }
    
    /**
     * Test byte array extraction.
     */
    @Test
    void testByteArrayExtraction() throws Exception {
        try (Extractor extractor = Extractor.create(ExtractFlags.DOMAINS)) {
            byte[] data = "Visit example.com".getBytes("UTF-8");
            List<ExtractedMatch> matches = extractor.extract(data);
            
            assertFalse(matches.isEmpty());
            assertEquals("example.com", matches.get(0).getValue());
        }
    }
}
