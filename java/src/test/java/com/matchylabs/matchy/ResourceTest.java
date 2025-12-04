package com.matchylabs.matchy;

import java.io.InputStream;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ResourceTest {
    
    @Test
    public void testResourcePath() {
        // Print what platform we detect
        String osName = System.getProperty("os.name").toLowerCase();
        String osArch = System.getProperty("os.arch").toLowerCase();
        System.out.println("OS: " + osName + ", Arch: " + osArch);
        
        // Try to load the resource
        String resourcePath = "/native/darwin-aarch64/libmatchy.dylib";
        System.out.println("Looking for: " + resourcePath);
        
        InputStream is = ResourceTest.class.getResourceAsStream(resourcePath);
        if (is != null) {
            System.out.println("FOUND resource!");
            try { is.close(); } catch (Exception e) {}
        } else {
            System.out.println("NOT FOUND");
            
            // Try without leading slash
            is = ResourceTest.class.getResourceAsStream("native/darwin-aarch64/libmatchy.dylib");
            System.out.println("Without leading slash: " + (is != null ? "FOUND" : "NOT FOUND"));
            if (is != null) try { is.close(); } catch (Exception e) {}
            
            // Try using classloader
            is = ResourceTest.class.getClassLoader().getResourceAsStream("native/darwin-aarch64/libmatchy.dylib");
            System.out.println("Using classloader: " + (is != null ? "FOUND" : "NOT FOUND"));
            if (is != null) try { is.close(); } catch (Exception e) {}
        }
        
        // This test is just for debugging
        assertTrue(true);
    }
}
