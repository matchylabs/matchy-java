# WARP.md

This file provides guidance to WARP (warp.dev) when working with code in this repository.

## Project Overview

**matchy-java** is a Java wrapper for [matchy](https://github.com/matchylabs/matchy), providing JNA bindings to the native matchy library for fast IoC (Indicator of Compromise) matching.

**Status**: 🚧 Work in Progress
- JNA bindings implemented (NativeLoader, MatchyLibrary, NativeStructs)
- Core wrapper classes implemented (QueryResult, DatabaseStats, OpenOptions, MatchyException)
- Database and DatabaseBuilder classes pending
- No tests or CI/CD yet

### Architecture

```
matchy-java/
├── java/                          # Maven project
│   ├── pom.xml                    # Maven configuration (Java 11, JNA 5.14.0, Gson)
│   └── src/main/java/com/matchylabs/matchy/
│       ├── jna/                   # Package-private JNA bindings layer
│       │   ├── NativeLoader.java  # Platform detection & native library loading
│       │   ├── MatchyLibrary.java # JNA interface to matchy C API
│       │   └── NativeStructs.java # JNA structure mappings (MatchyResult, etc.)
│       ├── Database.java          # Main public API (TODO)
│       ├── DatabaseBuilder.java   # Database builder API (TODO)
│       ├── QueryResult.java       # Query result wrapper
│       ├── DatabaseStats.java     # Database statistics
│       ├── OpenOptions.java       # Database open configuration
│       └── MatchyException.java   # Exception type
├── native/matchy/                 # Git submodule to matchy core (Rust)
└── examples/                      # Usage examples (empty, coming soon)
```

### Design Principles

1. **Clean Java API**: JNA bindings are package-private; users interact with idiomatic Java classes
2. **Resource safety**: Database handles and native resources must be properly managed
3. **Builder pattern**: Use builders for configuration (OpenOptions, DatabaseBuilder)
4. **Exception handling**: Convert C error codes to MatchyException with descriptive messages
5. **Platform independence**: NativeLoader handles Windows/macOS/Linux + x86_64/aarch64 detection

## Development Workflow

### Prerequisites

- **Java**: JDK 11+ (configured in pom.xml)
- **Maven**: 3.6+ for building
- **Rust**: Required to build the native matchy library (see native/matchy/WARP.md)
- **Git submodules**: `git submodule update --init --recursive`

### Building

#### Build Native Library First

The Java wrapper requires the compiled native matchy library. Build it from the submodule:

```bash
# Build native matchy library (release mode for production)
cd native/matchy
cargo build --release

# The library will be at:
# - macOS: native/matchy/target/release/libmatchy.dylib
# - Linux: native/matchy/target/release/libmatchy.so
# - Windows: native/matchy/target/release/matchy.dll
```

For development of the native library, see `native/matchy/WARP.md`.

#### Build Java Project

```bash
cd java

# Compile Java code
mvn compile

# Run tests (when implemented)
mvn test

# Package JAR (includes sources and javadoc)
mvn package

# Install to local Maven repository
mvn install

# Clean build artifacts
mvn clean
```

#### Run Single Test

```bash
# Run specific test class
mvn test -Dtest=DatabaseTest

# Run specific test method
mvn test -Dtest=DatabaseTest#testQuery

# Run with debug output
mvn test -X
```

### Code Quality

```bash
# Format code (if using a formatter plugin)
mvn spotless:apply  # if spotless is configured

# Generate Javadoc
mvn javadoc:javadoc

# Open generated docs
open java/target/site/apidocs/index.html

# Check for dependency updates
mvn versions:display-dependency-updates
```

### Native Library Loading

The NativeLoader class handles platform-specific library loading:

1. **Platform detection**: Detects OS (linux/macos/windows) and architecture (x86_64/aarch64)
2. **Resource lookup**: Searches for library at `/native/{platform}/{libname}` in JAR
3. **Temporary extraction**: Extracts library to temp file for System.load()
4. **One-time initialization**: Library loads once on first MatchyLibrary.INSTANCE access

**For development**, the native library must be either:
- In JAR resources at `src/main/resources/native/{platform}/`
- In system library path (LD_LIBRARY_PATH, DYLD_LIBRARY_PATH, PATH)
- Specified via `-Djna.library.path=path/to/native/libs`

Example for development:
```bash
# Set library path for testing
export LD_LIBRARY_PATH=$PWD/native/matchy/target/release:$LD_LIBRARY_PATH  # Linux
export DYLD_LIBRARY_PATH=$PWD/native/matchy/target/release:$DYLD_LIBRARY_PATH  # macOS

# Or use Maven property
mvn test -Djna.library.path=../native/matchy/target/release
```

## Implementation Patterns

### JNA Structure Mapping

JNA structures in NativeStructs.java must match C struct layouts exactly:

```java
// Must match C struct field order and types
@Structure.FieldOrder({"found", "prefix_len", "_data_cache", "_db_ref"})
static class MatchyResult extends Structure {
    public boolean found;        // C: bool (1 byte)
    public byte prefix_len;      // C: uint8_t
    public Pointer _data_cache;  // C: void*
    public Pointer _db_ref;      // C: void*
}
```

**Critical**: If the C struct changes in matchy.h, update the corresponding Java structure immediately. Field order, types, and padding must match exactly.

### Error Handling Pattern

Convert C error codes to Java exceptions with descriptive messages:

```java
// In wrapper class (Database.java)
Pointer dbPtr = MatchyLibrary.INSTANCE.matchy_open(path);
if (dbPtr == null) {
    throw new MatchyException("Failed to open database: " + path);
}

// For functions returning error codes
int result = MatchyLibrary.INSTANCE.matchy_builder_add(builder, key, data);
if (result != MatchyLibrary.MATCHY_SUCCESS) {
    throw new MatchyException("Failed to add entry: error code " + result);
}
```

### Resource Management Pattern

Native resources must be explicitly freed. Use try-with-resources when Database implements AutoCloseable:

```java
// Database.java should implement AutoCloseable
public class Database implements AutoCloseable {
    private final Pointer nativeHandle;
    
    @Override
    public void close() {
        if (nativeHandle != null) {
            MatchyLibrary.INSTANCE.matchy_close(nativeHandle);
        }
    }
}

// User code
try (Database db = Database.open("threats.mxy")) {
    QueryResult result = db.query("192.168.1.1");
    // ...
} // Automatically closed
```

### Builder Pattern

Use builders for complex configuration:

```java
Database db = Database.builder()
    .path("threats.mxy")
    .cacheCapacity(100_000)
    .autoReload(true)
    .build();

// Or with OpenOptions
OpenOptions options = OpenOptions.defaults()
    .cacheCapacity(50_000)
    .noCache();  // Fluent API
Database db = Database.open("threats.mxy", options);
```

## Key TODOs

Based on README.md, these are the next implementation priorities:

### 1. Database Class (High Priority)

Implement the main Database API:
- `static Database open(String path)` and `open(String path, OpenOptions options)`
- `static Database fromBuffer(byte[] buffer)`
- `QueryResult query(String text)` - main query method
- `void clearCache()` - clear LRU cache
- `DatabaseStats getStats()` - get query statistics
- `String getMetadata()` - database metadata
- `boolean hasIpData()`, `hasStringData()`, `hasGlobData()`, `hasLiteralData()`
- `String getFormat()` - get database format version
- `void close()` - free native resources
- Implement `AutoCloseable` for try-with-resources

Reference the C API in MatchyLibrary for all available functions.

### 2. DatabaseBuilder Class (High Priority)

Builder for creating databases programmatically:
- `DatabaseBuilder()` constructor
- `DatabaseBuilder add(String key, JsonObject data)` - add entry with metadata
- `DatabaseBuilder add(String key, String jsonData)` - add entry with JSON string
- `DatabaseBuilder setDescription(String description)` - set database description
- `Database build()` - build in-memory database
- `void save(String path)` - save to file
- `byte[] toBytes()` - serialize to byte array

### 3. Unit Tests (High Priority)

Create test files in `src/test/java/com/matchylabs/matchy/`:
- `DatabaseTest.java` - test open, query, close
- `DatabaseBuilderTest.java` - test database creation
- `QueryResultTest.java` - test result parsing
- `OpenOptionsTest.java` - test configuration
- `ExceptionTest.java` - test error handling

Use JUnit 5 (already in pom.xml dependencies).

### 4. Processing API (Medium Priority)

Implement batch processing utilities (matching Rust processing module):
- `Worker.java` - batch processing with extractor + multiple databases
- `FileReader.java` - streaming file I/O with gzip support
- `MatchResult.java` - match results without file context
- `LineMatch.java` - match results with line numbers

See `native/matchy/WARP.md` "Processing Module API" section for design.

### 5. CI/CD (Medium Priority)

Create `.github/workflows/ci.yml`:
- Build native library (Rust) for Linux/macOS/Windows
- Build Java wrapper (Maven)
- Run tests
- Generate Javadoc
- Create release artifacts with platform-specific native libraries

### 6. Examples and Documentation (Low Priority)

Create example programs in `examples/`:
- `BasicQuery.java` - simple query example
- `BuildDatabase.java` - building database example
- `BatchProcessing.java` - processing files example

## Git Submodule Management

The native matchy library is a Git submodule:

```bash
# Initialize submodule (first time)
git submodule update --init --recursive

# Update submodule to latest upstream
cd native/matchy
git pull origin main
cd ../..
git add native/matchy
git commit -m "Update matchy submodule"

# Pull updates including submodules
git pull --recurse-submodules
```

**Important**: When making changes to matchy-java that depend on new matchy C API features, coordinate submodule updates carefully.

## Testing Strategy

### Unit Tests
- Test each public method in isolation
- Mock native calls where possible (or use test databases)
- Test error conditions (invalid paths, null pointers, corrupt data)
- Test resource cleanup (no memory leaks)

### Integration Tests
- Test with real .mxy database files
- Test IP queries (IPv4, IPv6, CIDRs)
- Test string queries (exact matches, glob patterns)
- Test database building end-to-end
- Test multi-threaded access (thread safety of native library)

### Test Data
Store test databases in `src/test/resources/`:
- `test-ips.mxy` - IP address database
- `test-strings.mxy` - string/pattern database
- `test-combined.mxy` - mixed IP and string data

## Common Pitfalls

### JNA Structure Padding

JNA automatically handles structure padding, but if queries return incorrect data, verify:
1. Field order matches C struct exactly (`@Structure.FieldOrder`)
2. Java types match C types (bool→boolean, uint8_t→byte, size_t→NativeLong)
3. Test on multiple platforms (padding differs between architectures)

### Native Library Not Found

If you see `UnsatisfiedLinkError`:
1. Check native library was built (`ls native/matchy/target/release/`)
2. Verify library is in JAR resources or library path
3. Check platform detection in NativeLoader (add debug logging)
4. Use `-Djna.library.path` for development

### Memory Leaks

Native resources must be freed:
1. Always call `matchy_close()` on database handles
2. Call `matchy_free_result()` for result pointers (if used)
3. Call `matchy_free_string()` for returned strings
4. Implement `AutoCloseable` on Database for RAII-style cleanup
5. Write tests that check resource cleanup

### Thread Safety

The native matchy library is thread-safe for queries (read operations) but:
1. Database opening/closing must be synchronized
2. Builder operations are NOT thread-safe
3. Cache operations are thread-safe (internally synchronized in C)

Test multi-threaded access explicitly.

## Maven Central Deployment (Future)

The pom.xml is configured for Maven Central deployment:
- GPG signing plugin configured
- Source and javadoc JARs generated
- OSSRH repository configured

Before deploying:
1. Create Sonatype JIRA account
2. Request com.matchylabs groupId
3. Configure GPG keys
4. Set credentials in `~/.m2/settings.xml`
5. Deploy: `mvn clean deploy`

## Related Documentation

- **Native matchy**: See `native/matchy/WARP.md` for Rust library development
- **C API Reference**: See `native/matchy/include/matchy.h` for complete C API
- **Matchy Book**: https://matchylabs.github.io/matchy/ for user documentation
- **Rust API docs**: `cd native/matchy && cargo doc --open`

## License

Apache-2.0 (matching native matchy library)
