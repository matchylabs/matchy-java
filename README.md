# matchy-java

Java wrapper for [matchy](https://github.com/matchylabs/matchy) - fast IoC matching for threat intelligence.

## Status

🚧 **Work in Progress** - Initial JNA bindings and core wrapper classes implemented.

## Structure

```
matchy-java/
├── java/                   # Maven project
│   ├── pom.xml
│   └── src/main/java/com/matchylabs/matchy/
│       ├── jna/            # JNA bindings (package-private)
│       ├── Database.java   # Main API (coming soon)
│       ├── QueryResult.java
│       ├── DatabaseStats.java
│       └── OpenOptions.java
├── native/matchy/          # Git submodule to matchy core
└── examples/               # Usage examples (coming soon)
```

## Completed

- ✅ JNA bindings (NativeLoader, MatchyLibrary, NativeStructs)
- ✅ Core wrapper classes (QueryResult, DatabaseStats, OpenOptions)
- ✅ Exception handling (MatchyException)
- ✅ Maven project structure

## TODO

- [ ] Database class (open, query, close)
- [ ] DatabaseBuilder class
- [ ] Unit tests
- [ ] Processing API (Worker, FileReader)
- [ ] GitHub Actions CI/CD
- [ ] Documentation and examples
- [ ] Maven Central deployment

## License

Apache-2.0
