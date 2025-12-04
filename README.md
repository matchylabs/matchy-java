# matchy-java

Java wrapper for [matchy](https://github.com/matchylabs/matchy) - fast IoC matching for threat intelligence.

## Status

✅ **Core functionality complete** - Ready for use. Fat JAR releases coming soon.

## Structure

```
matchy-java/
├── java/                   # Maven project
│   ├── pom.xml
│   └── src/main/java/com/matchylabs/matchy/
│       ├── jna/            # JNA bindings (package-private)
│       ├── Database.java   # Main API for querying databases
│       ├── DatabaseBuilder.java  # Build databases programmatically
│       ├── QueryResult.java
│       ├── DatabaseStats.java
│       └── OpenOptions.java
├── native/matchy/          # Git submodule to matchy core
└── examples/               # Usage examples (coming soon)
```

## Completed

- ✅ JNA bindings (NativeLoader, MatchyLibrary, NativeStructs)
- ✅ Core wrapper classes (QueryResult, DatabaseStats, OpenOptions)
- ✅ Database class (open, query, close, stats)
- ✅ DatabaseBuilder class (create databases programmatically)
- ✅ Exception handling (MatchyException)
- ✅ Unit tests
- ✅ GitHub Actions CI/CD (multi-platform, Java 11/17/21)

## TODO

- [ ] Fat JAR releases with bundled native libraries
- [ ] Processing API (Worker, FileReader)
- [ ] Documentation and examples
- [ ] Maven Central deployment

## License

Apache-2.0
