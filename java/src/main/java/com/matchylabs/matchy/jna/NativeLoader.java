package com.matchylabs.matchy.jna;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/**
 * Handles loading of the native matchy library.
 * 
 * This class detects the platform, extracts the appropriate native library
 * from the JAR's resources to a temporary directory, and configures JNA to find it.
 * 
 * Package-private - not exposed to users.
 */
class NativeLoader {
    
    private static final String LIBRARY_NAME = "matchy";
    private static volatile String extractedLibraryPath = null;
    private static volatile boolean initialized = false;
    
    /**
     * Prepare the native library for loading by JNA.
     * 
     * This extracts the library from JAR resources if needed and sets up
     * the path so JNA can find it.
     * 
     * @return the path to the library directory, or null if using system path
     * @throws UnsatisfiedLinkError if the library cannot be found
     */
    static synchronized String prepareLibrary() {
        if (initialized) {
            return extractedLibraryPath;
        }
        
        // First, try to extract from JAR resources
        String jarPath = tryExtractFromJar();
        if (jarPath != null) {
            extractedLibraryPath = jarPath;
            initialized = true;
            return extractedLibraryPath;
        }
        
        // Check if library exists in jna.library.path
        String jnaPath = findInPath(System.getProperty("jna.library.path"));
        if (jnaPath != null) {
            extractedLibraryPath = null;  // JNA will find it
            initialized = true;
            return null;
        }
        
        // Check if library exists in java.library.path
        String javaPath = findInPath(System.getProperty("java.library.path"));
        if (javaPath != null) {
            extractedLibraryPath = null;  // System will find it
            initialized = true;
            return null;
        }
        
        // Nothing found
        String platform = detectPlatform();
        throw new UnsatisfiedLinkError(
            "Native library '" + LIBRARY_NAME + "' not found. " +
            "Platform: " + platform + ". " +
            "Set -Djna.library.path=<path> or include native libraries in JAR."
        );
    }
    
    /**
     * Find the library in a path string and return the directory containing it.
     * 
     * @param pathString the path string to search (colon or semicolon separated)
     * @return the directory path if found, null otherwise
     */
    private static String findInPath(String pathString) {
        if (pathString == null || pathString.isEmpty()) {
            return null;
        }
        
        String libraryFileName = mapLibraryName(LIBRARY_NAME);
        String separator = File.pathSeparator;
        
        for (String dir : pathString.split(separator)) {
            File libFile = new File(dir, libraryFileName);
            if (libFile.exists() && libFile.isFile()) {
                return dir;
            }
        }
        return null;
    }
    
    /**
     * Try to extract the native library from JAR resources.
     * 
     * @return the directory containing the extracted library, or null if not in JAR
     */
    private static String tryExtractFromJar() {
        try {
            String platform = detectPlatform();
            String libraryFileName = mapLibraryName(LIBRARY_NAME);
            String resourcePath = "/native/" + platform + "/" + libraryFileName;
            
            // Debug logging
            if (Boolean.getBoolean("matchy.debug")) {
                System.err.println("[NativeLoader] Platform: " + platform);
                System.err.println("[NativeLoader] Looking for resource: " + resourcePath);
            }
            
            try (InputStream is = NativeLoader.class.getResourceAsStream(resourcePath)) {
                if (is == null) {
                    if (Boolean.getBoolean("matchy.debug")) {
                        System.err.println("[NativeLoader] Resource not found");
                    }
                    return null;
                }
                
                // Extract to temporary file
                Path tempLib = extractToTemp(is, libraryFileName);
                String result = tempLib.getParent().toAbsolutePath().toString();
                if (Boolean.getBoolean("matchy.debug")) {
                    System.err.println("[NativeLoader] Extracted to: " + result);
                }
                return result;
            }
        } catch (IOException e) {
            if (Boolean.getBoolean("matchy.debug")) {
                System.err.println("[NativeLoader] IOException: " + e.getMessage());
            }
            return null;
        }
    }
    
    /**
     * Detect the current platform and return the platform identifier.
     * Uses JNA-compatible naming: darwin-aarch64, darwin-x86-64, linux-x86-64, etc.
     * 
     * @return platform string like "linux-x86-64", "darwin-aarch64", etc.
     */
    private static String detectPlatform() {
        String osName = System.getProperty("os.name").toLowerCase();
        String osArch = System.getProperty("os.arch").toLowerCase();
        
        // Normalize OS name (use JNA naming convention)
        String os;
        if (osName.contains("linux")) {
            os = "linux";
        } else if (osName.contains("mac") || osName.contains("darwin")) {
            os = "darwin";  // JNA uses "darwin" not "macos"
        } else if (osName.contains("windows")) {
            os = "win32";   // JNA uses "win32" not "windows"
        } else {
            throw new UnsatisfiedLinkError("Unsupported operating system: " + osName);
        }
        
        // Normalize architecture (use JNA naming convention)
        String arch;
        if (osArch.equals("amd64") || osArch.equals("x86_64")) {
            arch = "x86-64";  // JNA uses "x86-64" not "x86_64"
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
     * Extract input stream to a temporary directory, preserving the original filename.
     * 
     * JNA looks for libraries by name, so we need to keep "libmatchy.dylib" etc.
     * 
     * @param is input stream to read from
     * @param fileName filename to use (e.g., "libmatchy.dylib")
     * @return path to the extracted file
     * @throws IOException if extraction fails
     */
    private static Path extractToTemp(InputStream is, String fileName) throws IOException {
        // Create a temporary directory
        Path tempDir = Files.createTempDirectory("matchy-native-");
        tempDir.toFile().deleteOnExit();
        
        // Create file with the original name in the temp directory
        Path tempFile = tempDir.resolve(fileName);
        tempFile.toFile().deleteOnExit();
        
        // Copy library to temp file
        Files.copy(is, tempFile, StandardCopyOption.REPLACE_EXISTING);
        
        // Make executable on Unix-like systems
        tempFile.toFile().setExecutable(true);
        tempFile.toFile().setReadable(true);
        
        return tempFile;
    }
}
