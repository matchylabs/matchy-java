package com.matchylabs.matchy;

import java.io.InputStream;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for native library resource loading.
 */
public class ResourceTest {
    
    @Test
    public void testResourcePath() {
        // Detect platform
        String platform = detectPlatform();
        String libraryFileName = mapLibraryName("matchy");
        String resourcePath = "/native/" + platform + "/" + libraryFileName;
        
        System.out.println("Platform: " + platform);
        System.out.println("Looking for: " + resourcePath);
        
        // Try to load the resource
        InputStream is = ResourceTest.class.getResourceAsStream(resourcePath);
        if (is != null) {
            System.out.println("FOUND resource in JAR!");
            try { is.close(); } catch (Exception e) {}
        } else {
            // Resource not in JAR is fine - library may be loaded from jna.library.path
            System.out.println("Resource not in JAR (will use jna.library.path)");
        }
        
        // This test just verifies platform detection works
        assertNotNull(platform);
        assertNotNull(libraryFileName);
    }
    
    private static String detectPlatform() {
        String osName = System.getProperty("os.name").toLowerCase();
        String osArch = System.getProperty("os.arch").toLowerCase();
        
        String os;
        if (osName.contains("linux")) {
            os = "linux";
        } else if (osName.contains("mac") || osName.contains("darwin")) {
            os = "darwin";
        } else if (osName.contains("windows")) {
            os = "win32";
        } else {
            throw new RuntimeException("Unsupported OS: " + osName);
        }
        
        String arch;
        if (osArch.equals("amd64") || osArch.equals("x86_64")) {
            arch = "x86-64";
        } else if (osArch.equals("aarch64") || osArch.equals("arm64")) {
            arch = "aarch64";
        } else {
            throw new RuntimeException("Unsupported arch: " + osArch);
        }
        
        return os + "-" + arch;
    }
    
    private static String mapLibraryName(String libName) {
        String osName = System.getProperty("os.name").toLowerCase();
        if (osName.contains("windows")) {
            return libName + ".dll";
        } else if (osName.contains("mac") || osName.contains("darwin")) {
            return "lib" + libName + ".dylib";
        } else {
            return "lib" + libName + ".so";
        }
    }
}
