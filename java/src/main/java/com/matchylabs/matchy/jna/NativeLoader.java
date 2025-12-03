package com.matchylabs.matchy.jna;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/**
 * Handles loading of the native matchy library.
 * 
 * This class detects the platform, extracts the appropriate native library
 * from the JAR's resources to a temporary directory, and loads it.
 * 
 * Package-private - not exposed to users.
 */
class NativeLoader {
    
    private static final String LIBRARY_NAME = "matchy";
    private static boolean loaded = false;
    
    /**
     * Load the native library if not already loaded.
     * 
     * @throws UnsatisfiedLinkError if the library cannot be loaded
     */
    static synchronized void loadNativeLibrary() {
        if (loaded) {
            return;
        }
        
        try {
            String platform = detectPlatform();
            String libraryFileName = mapLibraryName(LIBRARY_NAME);
            String resourcePath = "/native/" + platform + "/" + libraryFileName;
            
            // Try to load from JAR resources
            try (InputStream is = NativeLoader.class.getResourceAsStream(resourcePath)) {
                if (is == null) {
                    throw new UnsatisfiedLinkError(
                        "Native library not found in JAR: " + resourcePath + 
                        " (platform: " + platform + ")"
                    );
                }
                
                // Extract to temporary file
                Path tempLib = extractToTemp(is, libraryFileName);
                
                // Load the library
                System.load(tempLib.toAbsolutePath().toString());
                loaded = true;
                
            }
        } catch (IOException e) {
            throw new UnsatisfiedLinkError(
                "Failed to extract native library: " + e.getMessage()
            );
        }
    }
    
    /**
     * Detect the current platform and return the platform identifier.
     * 
     * @return platform string like "linux-x86_64", "macos-aarch64", etc.
     */
    private static String detectPlatform() {
        String osName = System.getProperty("os.name").toLowerCase();
        String osArch = System.getProperty("os.arch").toLowerCase();
        
        // Normalize OS name
        String os;
        if (osName.contains("linux")) {
            os = "linux";
        } else if (osName.contains("mac") || osName.contains("darwin")) {
            os = "macos";
        } else if (osName.contains("windows")) {
            os = "windows";
        } else {
            throw new UnsatisfiedLinkError("Unsupported operating system: " + osName);
        }
        
        // Normalize architecture
        String arch;
        if (osArch.equals("amd64") || osArch.equals("x86_64")) {
            arch = "x86_64";
        } else if (osArch.equals("aarch64") || osArch.equals("arm64")) {
            arch = "aarch64";
        } else {
            throw new UnsatisfiedLinkError("Unsupported architecture: " + osArch);
        }
        
        return os + "-" + arch;
    }
    
    /**
     * Map library name to platform-specific filename.
     * 
     * @param libName library name without prefix/suffix
     * @return platform-specific library filename
     */
    private static String mapLibraryName(String libName) {
        String osName = System.getProperty("os.name").toLowerCase();
        
        if (osName.contains("windows")) {
            return libName + ".dll";
        } else if (osName.contains("mac") || osName.contains("darwin")) {
            return "lib" + libName + ".dylib";
        } else {
            // Linux and other Unix-like systems
            return "lib" + libName + ".so";
        }
    }
    
    /**
     * Extract input stream to a temporary file.
     * 
     * @param is input stream to read from
     * @param fileName filename for the temporary file
     * @return path to the extracted file
     * @throws IOException if extraction fails
     */
    private static Path extractToTemp(InputStream is, String fileName) throws IOException {
        // Get file extension
        String suffix = "";
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex > 0) {
            suffix = fileName.substring(dotIndex);
        }
        
        // Create temporary file
        Path tempFile = Files.createTempFile("matchy-native-", suffix);
        tempFile.toFile().deleteOnExit();
        
        // Copy library to temp file
        Files.copy(is, tempFile, StandardCopyOption.REPLACE_EXISTING);
        
        // Make executable on Unix-like systems
        tempFile.toFile().setExecutable(true);
        tempFile.toFile().setReadable(true);
        
        return tempFile;
    }
}
