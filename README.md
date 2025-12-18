# matchy-java

Java wrapper for [matchy](https://github.com/matchylabs/matchy) - fast IoC matching for threat intelligence.

## Installation

### Maven (GitHub Packages)

Add the repository and dependency to your `pom.xml`:

```xml
<repositories>
    <repository>
        <id>github</id>
        <url>https://maven.pkg.github.com/matchylabs/matchy-java</url>
    </repository>
</repositories>

<dependency>
    <groupId>com.matchylabs</groupId>
    <artifactId>matchy-java</artifactId>
    <version>0.2.0</version>
</dependency>
```

> **Note**: GitHub Packages requires authentication. Add a server entry to `~/.m2/settings.xml` with your GitHub username and a token with `read:packages` scope.

### Manual

Download the JAR from [Releases](https://github.com/matchylabs/matchy-java/releases) and add it to your classpath.

## Quick Start

### Querying a Database

```java
import com.matchylabs.matchy.*;
import java.nio.file.Paths;

try (Database db = Database.open(Paths.get("threats.mxy"))) {
    // Query an IP address
    QueryResult result = db.query("192.168.1.1");
    
    if (result.isMatch()) {
        System.out.println("Match found!");
        System.out.println("Data: " + result.getData());
        System.out.println("Prefix length: " + result.getPrefixLength());
    }
    
    // Query a domain
    QueryResult domainResult = db.query("evil.example.com");
}
```

### Building a Database

```java
import com.matchylabs.matchy.*;
import java.nio.file.Paths;
import java.util.Map;

try (DatabaseBuilder builder = new DatabaseBuilder()) {
    // Add IP addresses and CIDRs
    builder.add("1.2.3.4", Map.of("threat", "malware", "confidence", 95));
    builder.add("10.0.0.0/8", Map.of("type", "internal"));
    
    // Add patterns (glob syntax)
    builder.add("*.evil.com", Map.of("category", "phishing"));
    builder.add("malware-*.example.org", Map.of("category", "c2"));
    
    // Set metadata
    builder.setDescription("Threat database v1.0");
    
    // Save to file
    builder.save(Paths.get("threats.mxy"));
    
    // Or build in-memory
    try (Database db = builder.build()) {
        QueryResult result = db.query("1.2.3.4");
    }
}
```

### Configuration Options

```java
import com.matchylabs.matchy.*;
import java.nio.file.Paths;

// Custom cache size
OpenOptions options = OpenOptions.defaults()
    .cacheCapacity(100_000);  // LRU cache for query results

try (Database db = Database.open(Paths.get("threats.mxy"), options)) {
    // Queries are cached for faster repeated lookups
}

// Auto-reload on file changes
OpenOptions watchOptions = OpenOptions.defaults()
    .autoReload(true);  // Automatically reload when file changes

// Disable caching entirely
OpenOptions noCacheOptions = OpenOptions.defaults()
    .noCache();

// Auto-update from remote URL
OpenOptions autoUpdateOptions = OpenOptions.defaults()
    .autoUpdate(true)
    .updateIntervalSecs(3600)
    .cacheDir("/tmp/matchy-cache");
```

### Case-Insensitive Matching

```java
try (DatabaseBuilder builder = new DatabaseBuilder()) {
    builder.setCaseInsensitive(true)
           .add("Evil.COM", Map.of("type", "malicious"))
           .save(Paths.get("threats.mxy"));
}

try (Database db = Database.open(Paths.get("threats.mxy"))) {
    db.query("evil.com").isMatch();  // true
    db.query("EVIL.COM").isMatch();  // true
}
```

### Database Validation

```java
// Validate database file
Database.validate(Paths.get("threats.mxy"), ValidationLevel.STANDARD);

// Strict validation
Database.validate(Paths.get("threats.mxy"), ValidationLevel.STRICT);
```

### Error Handling

```java
import com.matchylabs.matchy.*;
import java.nio.file.Paths;

try (Database db = Database.open(Paths.get("threats.mxy"))) {
    QueryResult result = db.query("192.168.1.1");
    // ...
} catch (MatchyException e) {
    // Handle database errors (file not found, corrupt data, etc.)
    System.err.println("Matchy error: " + e.getMessage());
}
```

## API Reference

### Database

Main class for querying matchy databases.

| Method | Description |
|--------|-------------|
| `Database.open(Path)` | Open a database file |
| `Database.open(Path, OpenOptions)` | Open with custom options |
| `Database.fromBuffer(byte[])` | Open from memory |
| `Database.validate(Path, ValidationLevel)` | Validate a database file |
| `Database.hasAutoUpdate()` | Check if auto-update feature is available |
| `Database.getVersion()` | Get native library version |
| `query(String)` | Query IP address or pattern |
| `getStats()` | Get query statistics |
| `clearCache()` | Clear the LRU cache |
| `getMetadata()` | Get database metadata as JSON |
| `getUpdateUrl()` | Get embedded update URL (if set) |
| `hasIpData()` / `hasStringData()` | Check data types |
| `close()` | Free resources (use try-with-resources) |

### DatabaseBuilder

Create databases programmatically.

| Method | Description |
|--------|-------------|
| `add(String, Map)` | Add entry with data |
| `addJson(String, String)` | Add entry with JSON string |
| `setDescription(String)` | Set database description |
| `setCaseInsensitive(boolean)` | Enable case-insensitive matching |
| `setSchema(String)` | Enable schema validation (e.g., "threatdb") |
| `setUpdateUrl(String)` | Embed update URL in database |
| `save(Path)` | Save to file |
| `build()` | Build in-memory Database |
| `toBytes()` | Build as byte array |

### QueryResult

| Method | Description |
|--------|-------------|
| `isMatch()` | Whether query matched |
| `getData()` | Match data as JsonObject |
| `getDataAsJson()` | Match data as JSON string |
| `getPrefixLength()` | Network prefix (for IP matches) |

### OpenOptions

Configuration for opening databases.

| Method | Description |
|--------|-------------|
| `OpenOptions.defaults()` | Create default options |
| `cacheCapacity(int)` | Set LRU cache size |
| `noCache()` | Disable caching |
| `autoReload(boolean)` | Auto-reload on file changes |
| `autoUpdate(boolean)` | Enable HTTP auto-update |
| `updateIntervalSecs(int)` | Update check interval |
| `cacheDir(String)` | Directory for cached updates |

## Requirements

- **Java**: 11+
- **Platforms**: Linux (x86_64, aarch64), macOS (x86_64, aarch64), Windows (x86_64)

## Thread Safety

- `Database` instances are thread-safe for queries
- `DatabaseBuilder` is NOT thread-safe (use from a single thread)

## More Information

- [matchy documentation](https://matchylabs.github.io/matchy/) - concepts, file format, CLI
- [matchy repository](https://github.com/matchylabs/matchy) - native library

## License

Apache-2.0
