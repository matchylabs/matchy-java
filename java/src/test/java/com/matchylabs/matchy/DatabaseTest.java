package com.matchylabs.matchy;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Database class.
 */
class DatabaseTest {
    
    @TempDir
    Path tempDir;
    
    /**
     * Test basic database lifecycle: build, save, open, query, close.
     */
    @Test
    void testBasicLifecycle() throws Exception {
        Path dbPath = tempDir.resolve("test.mxy");
        
        // Build and save database
        try (DatabaseBuilder builder = new DatabaseBuilder()) {
            builder.add("1.2.3.4", Map.of("threat_level", "high", "source", "test"))
                   .add("192.168.1.0/24", Map.of("type", "internal"))
                   .add("*.evil.com", Map.of("category", "malware"))
                   .setDescription("Test database");
            
            builder.save(dbPath);
        }
        
        // Open and query database
        try (Database db = Database.open(dbPath)) {
            assertFalse(db.isClosed());
            
            // Query IP that matches
            QueryResult result1 = db.query("1.2.3.4");
            assertTrue(result1.isMatch());
            assertNotNull(result1.getData());
            
            // Query IP in CIDR range
            QueryResult result2 = db.query("192.168.1.100");
            assertTrue(result2.isMatch());
            
            // Query IP that doesn't match
            QueryResult result3 = db.query("8.8.8.8");
            assertFalse(result3.isMatch());
            
            // Query pattern
            QueryResult result4 = db.query("subdomain.evil.com");
            assertTrue(result4.isMatch());
            
            // Query non-matching pattern
            QueryResult result5 = db.query("good.com");
            assertFalse(result5.isMatch());
        }
    }
    
    /**
     * Test database with custom options.
     */
    @Test
    void testWithOptions() throws Exception {
        Path dbPath = tempDir.resolve("test_opts.mxy");
        
        // Build database
        try (DatabaseBuilder builder = new DatabaseBuilder()) {
            builder.add("10.0.0.1", Map.of("name", "test"));
            builder.save(dbPath);
        }
        
        // Open with custom options
        OpenOptions options = OpenOptions.defaults()
            .cacheCapacity(1000)
            .autoReload(false);
        
        try (Database db = Database.open(dbPath, options)) {
            QueryResult result = db.query("10.0.0.1");
            assertTrue(result.isMatch());
        }
    }
    
    /**
     * Test database statistics.
     */
    @Test
    void testStats() throws Exception {
        Path dbPath = tempDir.resolve("test_stats.mxy");
        
        // Build database
        try (DatabaseBuilder builder = new DatabaseBuilder()) {
            builder.add("1.1.1.1", Map.of("dns", "cloudflare"));
            builder.save(dbPath);
        }
        
        try (Database db = Database.open(dbPath)) {
            // Initial stats
            DatabaseStats stats1 = db.getStats();
            assertEquals(0, stats1.getTotalQueries());
            
            // Do some queries
            db.query("1.1.1.1");
            db.query("8.8.8.8");
            db.query("1.1.1.1"); // Cache hit
            
            // Check stats
            DatabaseStats stats2 = db.getStats();
            assertEquals(3, stats2.getTotalQueries());
            assertEquals(2, stats2.getQueriesWithMatch());
            assertEquals(1, stats2.getQueriesWithoutMatch());
            assertTrue(stats2.getCacheHits() > 0);
        }
    }
    
    /**
     * Test cache operations.
     */
    @Test
    void testCache() throws Exception {
        Path dbPath = tempDir.resolve("test_cache.mxy");
        
        // Build database
        try (DatabaseBuilder builder = new DatabaseBuilder()) {
            builder.add("1.2.3.4", Map.of("test", "value"));
            builder.save(dbPath);
        }
        
        try (Database db = Database.open(dbPath)) {
            // Query to populate cache
            db.query("1.2.3.4");
            
            DatabaseStats stats1 = db.getStats();
            long cacheHits1 = stats1.getCacheHits();
            
            // Query again (should be cache hit)
            db.query("1.2.3.4");
            
            DatabaseStats stats2 = db.getStats();
            assertTrue(stats2.getCacheHits() > cacheHits1);
            
            // Clear cache
            db.clearCache();
            
            // Query again (should be cache miss)
            db.query("1.2.3.4");
            
            DatabaseStats stats3 = db.getStats();
            assertTrue(stats3.getCacheMisses() > stats2.getCacheMisses());
        }
    }
    
    /**
     * Test database introspection methods.
     */
    @Test
    void testIntrospection() throws Exception {
        Path dbPath = tempDir.resolve("test_intro.mxy");
        
        // Build mixed database (IP + pattern)
        try (DatabaseBuilder builder = new DatabaseBuilder()) {
            builder.add("1.2.3.4", Map.of("type", "ip"));
            builder.add("*.example.com", Map.of("type", "pattern"));
            builder.save(dbPath);
        }
        
        try (Database db = Database.open(dbPath)) {
            assertTrue(db.hasIpData());
            assertTrue(db.hasStringData());
            
            String format = db.getFormat();
            assertNotNull(format);
            assertTrue(format.contains("IP") || format.contains("Pattern") || format.contains("Combined"));
            
            String metadata = db.getMetadata();
            // Metadata may be null for pattern-only databases
        }
    }
    
    /**
     * Test building in-memory database.
     */
    @Test
    void testInMemoryBuild() throws Exception {
        try (DatabaseBuilder builder = new DatabaseBuilder()) {
            builder.add("10.10.10.10", Map.of("location", "test"));
            
            // Build in memory
            try (Database db = builder.build()) {
                QueryResult result = db.query("10.10.10.10");
                assertTrue(result.isMatch());
            }
        }
    }
    
    /**
     * Test that operations fail on closed database.
     */
    @Test
    void testClosedDatabase() throws Exception {
        Path dbPath = tempDir.resolve("test_closed.mxy");
        
        // Build database
        try (DatabaseBuilder builder = new DatabaseBuilder()) {
            builder.add("1.1.1.1", Map.of("test", "value"));
            builder.save(dbPath);
        }
        
        Database db = Database.open(dbPath);
        db.close();
        
        assertTrue(db.isClosed());
        
        // Operations should fail
        assertThrows(MatchyException.class, () -> db.query("1.1.1.1"));
        assertThrows(MatchyException.class, db::getStats);
        assertThrows(MatchyException.class, db::clearCache);
    }
    
    /**
     * Test opening non-existent database.
     */
    @Test
    void testOpenNonExistent() {
        Path nonExistent = tempDir.resolve("does_not_exist.mxy");
        assertThrows(MatchyException.class, () -> Database.open(nonExistent));
    }
}
