# Building matchy-java

## Automatic Native Library Build

The Maven build automatically compiles the native Rust library before running tests. No manual steps are required.

### Build Process

1. **Maven triggers cargo**: During the `generate-resources` phase, Maven runs `cargo build --release --lib` in `native/matchy/`
2. **Tests use the built library**: The surefire plugin is configured with `-Djna.library.path` pointing to `native/matchy/target/release`
3. **Incremental builds**: Cargo's incremental compilation means subsequent builds complete in ~0.1s when nothing has changed

### Commands

```bash
# Full build and test (builds Rust library automatically)
mvn clean test

# Just compile Java code (also builds Rust library)
mvn compile

# Package (includes building native library)
mvn package
```

### Requirements

- **Java 11+**: For compiling and running the Java code
- **Maven**: For building the project
- **Rust/Cargo**: For compiling the native library (installed automatically via rustup or your package manager)

### How It Works

The `pom.xml` includes:

1. **Properties** defining the native library paths:
   ```xml
   <native.matchy.dir>${project.basedir}/../native/matchy</native.matchy.dir>
   <native.target.dir>${native.matchy.dir}/target/release</native.target.dir>
   ```

2. **exec-maven-plugin** to build the Rust library:
   ```xml
   <plugin>
     <groupId>org.codehaus.mojo</groupId>
     <artifactId>exec-maven-plugin</artifactId>
     <execution>
       <phase>generate-resources</phase>
       <goals><goal>exec</goal></goals>
       <configuration>
         <executable>cargo</executable>
         <arguments>
           <argument>build</argument>
           <argument>--release</argument>
           <argument>--lib</argument>
         </arguments>
       </configuration>
     </execution>
   </plugin>
   ```

3. **Surefire plugin** configured to find the native library:
   ```xml
   <plugin>
     <groupId>org.apache.maven.plugins</groupId>
     <artifactId>maven-surefire-plugin</artifactId>
     <configuration>
       <argLine>-Djna.library.path=${native.target.dir}</argLine>
     </configuration>
   </plugin>
   ```

### CI/CD

The GitHub Actions workflows have been simplified to just run `mvn test` - the native library build is handled automatically by Maven.

### Multi-Platform Builds

For cross-platform releases:

1. Build on each target platform (Linux x64, Linux ARM64, macOS, Windows)
2. Maven will automatically build the appropriate native library for each platform
3. Collect the built libraries from `native/matchy/target/release/`
4. Bundle them into the final JAR with platform-specific paths
