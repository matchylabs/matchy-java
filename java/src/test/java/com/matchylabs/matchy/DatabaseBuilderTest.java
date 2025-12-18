package com.matchylabs.matchy;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for DatabaseBuilder class.
 */
class DatabaseBuilderTest {
    
    @TempDir
    Path tempDir;
    
    /**
     * Test adding various entry types.
     */
    @Test
    void testAddEntries() throws Exception {
        try (DatabaseBuilder builder = new DatabaseBuilder()) {
            // Add IP address
            builder.add("1.2.3.4", Map.of("type", "ip"));
            
            // Add CIDR range
            builder.add("10.0.0.0/8", Map.of("type", "cidr"));
            
            // Add glob pattern
            builder.add("*.example.com", Map.of("type", "glob"));
            
            // Add literal string
            builder.add("literal.example.com", Map.of("type", "literal"));
            
            assertFalse(builder.isClosed());
        }
    }
    
    /**
     * Test adding entries with JSON strings.
     */
    @Test
    void testAddJson() throws Exception {
        try (DatabaseBuilder builder = new DatabaseBuilder()) {
            builder.addJson("1.1.1.1", "{\"name\":\"cloudflare\",\"speed\":\"fast\"}");
            
            Path dbPath = tempDir.resolve("test_json.mxy");
            builder.save(dbPath);
            
            // Verify database was created
            assertTrue(Files.exists(dbPath));
        }
    }
    
    /**
     * Test setting description.
     */
    @Test
    void testDescription() throws Exception {
        Path dbPath = tempDir.resolve("test_desc.mxy");
        
        try (DatabaseBuilder builder = new DatabaseBuilder()) {
            builder.add("1.2.3.4", Map.of("test", "value"))
                   .setDescription("This is a test database");
            
            builder.save(dbPath);
        }
        
        // Open and verify it works
        try (Database db = Database.open(dbPath)) {
            QueryResult result = db.query("1.2.3.4");
            assertTrue(result.isMatch());
        }
    }
    
    /**
     * Test saving to file.
     */
    @Test
    void testSave() throws Exception {
        Path dbPath = tempDir.resolve("test_save.mxy");
        
        try (DatabaseBuilder builder = new DatabaseBuilder()) {
            builder.add("8.8.8.8", Map.of("service", "dns"));
            builder.save(dbPath);
        }
        
        // Verify file exists and is non-empty
        assertTrue(Files.exists(dbPath));
        assertTrue(Files.size(dbPath) > 0);
        
        // Verify we can open it
        try (Database db = Database.open(dbPath)) {
            QueryResult result = db.query("8.8.8.8");
            assertTrue(result.isMatch());
        }
    }
    
    /**
     * Test building in memory.
     */
    @Test
    void testBuildInMemory() throws Exception {
        try (DatabaseBuilder builder = new DatabaseBuilder()) {
            builder.add("192.168.1.1", Map.of("location", "internal"));
            
            // Build to bytes
            byte[] bytes = builder.toBytes();
            assertNotNull(bytes);
            assertTrue(bytes.length > 0);
            
            // Create database from bytes
            try (Database db = Database.fromBuffer(bytes)) {
                QueryResult result = db.query("192.168.1.1");
                assertTrue(result.isMatch());
            }
        }
    }
    
    /**
     * Test build() convenience method.
     */
    @Test
    void testBuildConvenience() throws Exception {
        try (DatabaseBuilder builder = new DatabaseBuilder()) {
            builder.add("10.20.30.40", Map.of("test", "data"));
            
            try (Database db = builder.build()) {
                QueryResult result = db.query("10.20.30.40");
                assertTrue(result.isMatch());
            }
        }
    }
    
    /**
     * Test fluent API chaining.
     */
    @Test
    void testFluentApi() throws Exception {
        Path dbPath = tempDir.resolve("test_fluent.mxy");
        
        try (DatabaseBuilder builder = new DatabaseBuilder()) {
            builder.add("1.1.1.1", Map.of("a", "1"))
                   .add("2.2.2.2", Map.of("b", "2"))
                   .add("3.3.3.3", Map.of("c", "3"))
                   .setDescription("Fluent test")
                   .save(dbPath);
        }
        
        assertTrue(Files.exists(dbPath));
    }
    
    /**
     * Test that operations fail on closed builder.
     */
    @Test
    void testClosedBuilder() throws Exception {
        DatabaseBuilder builder = new DatabaseBuilder();
        builder.close();
        
        assertTrue(builder.isClosed());
        
        // Operations should fail
        assertThrows(MatchyException.class, () -> 
            builder.add("1.1.1.1", Map.of("test", "value")));
        
        assertThrows(MatchyException.class, () -> 
            builder.setDescription("test"));
        
        assertThrows(MatchyException.class, () -> 
            builder.save(tempDir.resolve("test.mxy")));
    }
    
    /**
     * Test building empty database (should work).
     */
    @Test
    void testEmptyDatabase() throws Exception {
        Path dbPath = tempDir.resolve("empty.mxy");
        
        try (DatabaseBuilder builder = new DatabaseBuilder()) {
            // Don't add any entries
            builder.save(dbPath);
        }
        
        // Should be able to open (but queries won't match anything)
        try (Database db = Database.open(dbPath)) {
            QueryResult result = db.query("1.1.1.1");
            assertFalse(result.isMatch());
        }
    }
    
    /**
     * Test invalid entry handling.
     */
    @Test
    void testInvalidEntry() {
        assertThrows(NullPointerException.class, () -> {
            try (DatabaseBuilder builder = new DatabaseBuilder()) {
                builder.add(null, Map.of("test", "value"));
            }
        });
        
        assertThrows(NullPointerException.class, () -> {
            try (DatabaseBuilder builder = new DatabaseBuilder()) {
                builder.add("1.1.1.1", null);
            }
        });
    }
    
    /**
     * Test that multiple builders don't interfere with each other.
     */
    @Test
    void testMultipleBuilders() throws Exception {
        Path db1Path = tempDir.resolve("db1.mxy");
        Path db2Path = tempDir.resolve("db2.mxy");
        
        // Create first database with one entry
        try (DatabaseBuilder builder1 = new DatabaseBuilder()) {
            builder1.add("1.1.1.1", Map.of("name", "first"));
            builder1.save(db1Path);
        }
        
        // Create second database with different entry  
        try (DatabaseBuilder builder2 = new DatabaseBuilder()) {
            builder2.add("2.2.2.2", Map.of("name", "second"));
            builder2.save(db2Path);
        }
        
        // Verify first database only has first entry
        try (Database db1 = Database.open(db1Path)) {
            assertTrue(db1.query("1.1.1.1").isMatch(), "DB1 should match 1.1.1.1");
            assertFalse(db1.query("2.2.2.2").isMatch(), "DB1 should NOT match 2.2.2.2");
        }
        
        // Verify second database only has second entry
        try (Database db2 = Database.open(db2Path)) {
            assertFalse(db2.query("1.1.1.1").isMatch(), "DB2 should NOT match 1.1.1.1");
            assertTrue(db2.query("2.2.2.2").isMatch(), "DB2 should match 2.2.2.2");
        }
    }
    
    @Test
    void testSetCaseInsensitive() throws Exception {
        Path dbPath = tempDir.resolve("test_case_insensitive.mxy");
        
        try (DatabaseBuilder builder = new DatabaseBuilder()) {
            try {
                builder.setCaseInsensitive(true)
                       .add("Evil.COM", Map.of("type", "malicious"));
            } catch (UnsatisfiedLinkError e) {
                org.junit.jupiter.api.Assumptions.assumeTrue(false, 
                    "Native library doesn't support setCaseInsensitive");
            }
            builder.save(dbPath);
        }
        
        try (Database db = Database.open(dbPath)) {
            assertTrue(db.query("Evil.COM").isMatch(), "Should match original case");
            assertTrue(db.query("evil.com").isMatch(), "Should match lowercase");
            assertTrue(db.query("EVIL.COM").isMatch(), "Should match uppercase");
        }
    }
    
    @Test
    void testSetUpdateUrl() throws Exception {
        Path dbPath = tempDir.resolve("test_update_url.mxy");
        String updateUrl = "https://example.com/threats.mxy";
        
        try (DatabaseBuilder builder = new DatabaseBuilder()) {
            try {
                builder.add("1.2.3.4", Map.of("test", "value"))
                       .setUpdateUrl(updateUrl)
                       .save(dbPath);
            } catch (UnsatisfiedLinkError e) {
                org.junit.jupiter.api.Assumptions.assumeTrue(false,
                    "Native library doesn't support setUpdateUrl");
            }
        }
        
        try (Database db = Database.open(dbPath)) {
            String retrievedUrl = db.getUpdateUrl();
            assertEquals(updateUrl, retrievedUrl);
        }
    }
    
    @Test
    void testSetSchemaUnknown() throws Exception {
        try (DatabaseBuilder builder = new DatabaseBuilder()) {
            try {
                builder.setSchema("nonexistent_schema");
                fail("Expected MatchyException for unknown schema");
            } catch (UnsatisfiedLinkError e) {
                org.junit.jupiter.api.Assumptions.assumeTrue(false,
                    "Native library doesn't support setSchema");
            } catch (MatchyException ex) {
                assertTrue(ex.getMessage().contains("Unknown schema") || 
                           ex.getMessage().contains("Schema"));
            }
        }
    }
    
    @Test
    void testSetSchemaValid() throws Exception {
        Path dbPath = tempDir.resolve("test_schema_valid.mxy");
        
        try (DatabaseBuilder builder = new DatabaseBuilder()) {
            try {
                builder.setSchema("threatdb");
            } catch (UnsatisfiedLinkError e) {
                org.junit.jupiter.api.Assumptions.assumeTrue(false,
                    "Native library doesn't support setSchema");
            }
            
            builder.add("1.2.3.4", Map.of(
                "threat_level", "high",
                "category", "malware",
                "source", "test"
            ));
            builder.save(dbPath);
        }
        
        try (Database db = Database.open(dbPath)) {
            QueryResult result = db.query("1.2.3.4");
            assertTrue(result.isMatch());
            assertEquals("high", result.getData().get("threat_level").getAsString());
        }
    }
    
    @Test
    void testSetSchemaRejectsInvalid() throws Exception {
        try (DatabaseBuilder builder = new DatabaseBuilder()) {
            try {
                builder.setSchema("threatdb");
            } catch (UnsatisfiedLinkError e) {
                org.junit.jupiter.api.Assumptions.assumeTrue(false,
                    "Native library doesn't support setSchema");
            }
            
            try {
                builder.add("1.2.3.4", Map.of(
                    "threat_level", "invalid_level",
                    "category", "malware"
                ));
                fail("Expected MatchyException for invalid schema data");
            } catch (MatchyException ex) {
                assertTrue(ex.getMessage().toLowerCase().contains("validation") ||
                           ex.getMessage().toLowerCase().contains("schema"));
            }
        }
    }
}
