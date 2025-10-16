# Jarsplit sample

This is a sample project to show how you could split jar into multiple jars with backward compatibility.

https://jlbp.dev/JLBP-6 suggests "Rename both Java package and Maven ID", and they say
there will be issues if you just rename maven ID without renaming packages.

Here's a quote from JLBP-6:

> **Case 2: Rename Maven ID**. Build systems can unexpectedly pull in the JAR files of both the old artifact (g1:a1:1.0.0)
> and the new artifact (g2:a2:1.1.0) into their class path through transitive dependencies, because Maven artifact
> resolution treats the two artifacts as distinct. This results in “overlapping classes” since they have classes
> with the same fully qualified path. This can lead to runtime exceptions such as NoSuchMethodDefError and can be
> a compile-time error in Java 9 and later. **NEVER DO THIS**.

This sample shows that Gradle does **not** suffer from that issue.

# Project layout

- `v1.0` — single-jar `commons-compress.jar` version of the library `org.example:commons-compress`
- `v1.1` — multi-jar evolution: `commons-compress-core.jar`, `commons-compress-tar.jar`, ..., and `commons-compress.pom` bom
- `svg`: a library that depends on `commons-compress:1.0.0`
- `http`: a library that depends on `commons-compress:1.0.0`, however `http:1.1.0` switches to `commons-compress-tar:1.1.0` (only tar features are heeded in http)
- `app-gradle` — sample Gradle application that depends on both `svg:1.0.0` and `http:1.1.0`
- `app-maven` — sample Maven project that depends on both `svg:1.0.0` and `http:1.1.0`
- `app-maven4` — sample Maven 4 project that depends on both `svg:1.0.0` and `http:1.1.0`

# Expected behavior

If application upgrades `http:1.0.0` to `http:1.1.0`, it should continue to work even though `http:1.1.0` depends on `commons-compress-tar:1.1.0`.

Note: the test app does not use `commons-compress` directly, and it might even do not know about it.
The test app did not anticipate `commons-compress` split, and it should continue to work without modifications to pom.xml except bumping the version of `http:1.1.0`. 

# Actual behavior

Gradle resolves commons-compress artifacts to 1.1.0 as expected, and the test app works fine.

Maven 3.9.11 and 4.0.0-SNAPSHOT (as of 2025-10-10) cause the application to fail in the runtime since Maven resolves
`commons-compress` to 1.0.0.

# How to run

```bash
# execute ./test.sh or the commands below manually
(cd v1.0; ./gradlew publishAllPublicationsToRepoRepository publishToMavenLocal)
(cd v1.1; ./gradlew publishAllPublicationsToRepoRepository publishToMavenLocal)
(cd app-gradle; ./gradlew dependencies --configuration runtimeClasspath run)
(cd app-maven; ./mvnw -V dependency:tree verify)
(cd app-maven4; ./mvnw -V dependency:tree verify)
```

# Results

## Gradle 8.14.3

Gradle uses constraints like `commons-compress-tar:1.1.0 -> commons-compress:1.1.0`, so it bumps
`commons-compress` from 1.0.0 to 1.1.0 should an older version of the library be on the classpath.
This indicates **Gradle is not affected** by JLBP-6 as long as the new artifacts add constraints to the BOM.

```
------------------------------------------------------------
Root project 'app-gradle'
------------------------------------------------------------

runtimeClasspath - Runtime classpath of source set 'main'.
+--- org.example:svg:1.0.0
|    \--- org.example:commons-compress:1.0.0 -> 1.1.0
|         +--- org.example:commons-compress-core:1.1.0
|         |    \--- org.example:commons-compress:1.1.0 (c)
|         +--- org.example:commons-compress-tar:1.1.0
|         |    \--- org.example:commons-compress-core:1.1.0 (*)
|         \--- org.example:commons-compress-xz:1.1.0
|              \--- org.example:commons-compress-core:1.1.0 (*)
\--- org.example:http:1.1.0
     \--- org.example:commons-compress-tar:1.1.0 (*)

(c) - A dependency constraint, not a dependency. The dependency affected by the constraint occurs elsewhere in the tree.
(*) - Indicates repeated occurrences of a transitive dependency subtree. Gradle expands transitive dependency subtrees only once per project; repeat occurrences only display the root of the subtree, followed by this annotation.
```

The runtime gets 1.1.0 versions of the libraries, and the application works fine:

```
> Task :run
svg:
  CoreVersion = 1.1.0
  TarCompressor = 1.1.0
  XzCompressor = 1.1.0
http:
  CoreVersion = 1.1.0
  TarCompressor = 1.1.0
Using a new method from tar v1.1
TarCompressor.methodAddedIn11()
```

## Maven 3.9.11

Maven does not use `<dependencyManagement>` from `commons-compress-tar`, so it does not see `commons-compress:1.1.0` should be used.
This indicates **Maven 3.9.11 is affected** by JLBP-6.

```
[INFO] --- dependency:3.7.0:tree (default-cli) @ app ---
[INFO] org.example:app:jar:1.0.0
[INFO] +- org.example:svg:jar:1.0.0:compile
[INFO] |  \- org.example:commons-compress:jar:1.0.0:runtime
[INFO] \- org.example:http:jar:1.1.0:compile
[INFO]    \- org.example:commons-compress-tar:jar:1.1.0:runtime
[INFO]       \- org.example:commons-compress-core:jar:1.1.0:runtime
```

However, the runtime gets 1.0.0 versions of the libraries, and the application crashes:

```
svg:
  CoreVersion = 1.0.0
  TarCompressor = 1.0.0
  XzCompressor = 1.0.0
http:
  CoreVersion = 1.0.0
  TarCompressor = 1.0.0
Using a new method from tar v1.1
[WARNING]
java.lang.Exception: The specified mainClass doesn't contain a main method with appropriate signature.
    at org.codehaus.mojo.exec.ExecJavaMojo.lambda$execute$0 (ExecJavaMojo.java:285)
    at java.lang.Thread.run (Thread.java:840)
Caused by: java.lang.NoSuchMethodError: 'void org.example.tar.TarCompressor.methodAddedIn11()'
    at org.example.http.HttpFactory.showVersions (HttpFactory.java:12)
    at org.example.app.Main.main (Main.java:9)
    at org.codehaus.mojo.exec.ExecJavaMojo.doMain (ExecJavaMojo.java:371)
    at org.codehaus.mojo.exec.ExecJavaMojo.doExec (ExecJavaMojo.java:360)
    at org.codehaus.mojo.exec.ExecJavaMojo.lambda$execute$0 (ExecJavaMojo.java:280)
    at java.lang.Thread.run (Thread.java:840)
```

## Maven 4.0.0-SNAPSHOT (8134db6f3c18ab2c68764a5ae05c9e08846b9787)

Maven does not use `<dependencyManagement>` from `commons-compress-tar`, so it does not see `commons-compress:1.1.0` should be used.
This indicates **Maven 4.0.0-rc-4 is affected** by JLBP-6.

```
[INFO] --- dependency:3.9.0:tree (default-cli) @ app ---
[INFO] org.example:app:jar:1.0.0
[INFO] +- org.example:svg:jar:1.0.0:compile
[INFO] |  \- org.example:commons-compress:jar:1.0.0:runtime
[INFO] \- org.example:http:jar:1.1.0:compile
[INFO]    \- org.example:commons-compress-tar:jar:1.1.0:runtime
[INFO]       \- org.example:commons-compress-core:jar:1.1.0:runtime
```

However, the runtime gets 1.0.0 versions of the libraries, and the application crashes:

```
[INFO] [stdout] svg:
[INFO] [stdout]   CoreVersion = 1.0.0
[INFO] [stdout]   TarCompressor = 1.0.0
[INFO] [stdout]   XzCompressor = 1.0.0
[INFO] [stdout] http:
[INFO] [stdout]   CoreVersion = 1.0.0
[INFO] [stdout]   TarCompressor = 1.0.0
[INFO] [stdout] Using a new method from tar v1.1
[WARNING]
java.lang.Exception: The specified mainClass doesn't contain a main method with appropriate signature.
    at org.codehaus.mojo.exec.ExecJavaMojo.lambda$execute$0(ExecJavaMojo.java:285)
    at java.lang.Thread.run(Thread.java:840)
Caused by: java.lang.NoSuchMethodError: 'void org.example.tar.TarCompressor.methodAddedIn11()'
    at org.example.http.HttpFactory.showVersions(HttpFactory.java:12)
    at org.example.app.Main.main(Main.java:9)
    at org.codehaus.mojo.exec.ExecJavaMojo.doMain(ExecJavaMojo.java:371)
    at org.codehaus.mojo.exec.ExecJavaMojo.doExec(ExecJavaMojo.java:360)
    at org.codehaus.mojo.exec.ExecJavaMojo.lambda$ex
```

# Published artifacts

## `commons-compress:1.0.0`

Single jar artifact.

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd" xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
    <!-- This module was also published with a richer model, Gradle metadata,  -->
    <!-- which should be used instead. Do not delete the following line which  -->
    <!-- is to indicate to Gradle or any Gradle module metadata file consumer  -->
    <!-- that they should prefer consuming it instead. -->
    <!-- do_not_remove: published-with-gradle-metadata -->
    <modelVersion>4.0.0</modelVersion>
    <groupId>org.example</groupId>
    <artifactId>commons-compress</artifactId>
    <version>1.0.0</version>
</project>
```

## `commons-compress:1.1.0`

BOM artifact with the list of module versions.
Note: it uses `<dependency>` so the users shoul get all the modules in the dependency tree if they
add a dependency on `commons-compress:1.1.0`.

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd" xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
    <!-- This module was also published with a richer model, Gradle metadata,  -->
    <!-- which should be used instead. Do not delete the following line which  -->
    <!-- is to indicate to Gradle or any Gradle module metadata file consumer  -->
    <!-- that they should prefer consuming it instead. -->
    <!-- do_not_remove: published-with-gradle-metadata -->
    <modelVersion>4.0.0</modelVersion>
    <groupId>org.example</groupId>
    <artifactId>commons-compress</artifactId>
    <version>1.1.0</version>
    <packaging>pom</packaging>
    <dependencies>
        <dependency>
            <groupId>org.example</groupId>
            <artifactId>commons-compress-core</artifactId>
            <version>1.1.0</version>
            <scope>compile</scope>
        </dependency>
        <dependency>
            <groupId>org.example</groupId>
            <artifactId>commons-compress-tar</artifactId>
            <version>1.1.0</version>
            <scope>compile</scope>
        </dependency>
        <dependency>
            <groupId>org.example</groupId>
            <artifactId>commons-compress-xz</artifactId>
            <version>1.1.0</version>
            <scope>compile</scope>
        </dependency>
    </dependencies>
</project>
```

## `commons-compress-core:1.1.0`

Core classes for the library. It refers to `commons-compress` BOM to align versions.

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd" xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
    <!-- This module was also published with a richer model, Gradle metadata,  -->
    <!-- which should be used instead. Do not delete the following line which  -->
    <!-- is to indicate to Gradle or any Gradle module metadata file consumer  -->
    <!-- that they should prefer consuming it instead. -->
    <!-- do_not_remove: published-with-gradle-metadata -->
    <modelVersion>4.0.0</modelVersion>
    <groupId>org.example</groupId>
    <artifactId>commons-compress-core</artifactId>
    <version>1.1.0</version>
    <dependencyManagement>
        <dependencies>
            <dependency>
                <groupId>org.example</groupId>
                <artifactId>commons-compress</artifactId>
                <version>1.1.0</version>
            </dependency>
        </dependencies>
    </dependencyManagement>
</project>
```

## `commons-compress-tar:1.1.0`

Tar support for the library. It depends on `commons-compress-core:1.1.0` which itself depends on the BOM 1.1.0.

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd" xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
    <!-- This module was also published with a richer model, Gradle metadata,  -->
    <!-- which should be used instead. Do not delete the following line which  -->
    <!-- is to indicate to Gradle or any Gradle module metadata file consumer  -->
    <!-- that they should prefer consuming it instead. -->
    <!-- do_not_remove: published-with-gradle-metadata -->
    <modelVersion>4.0.0</modelVersion>
    <groupId>org.example</groupId>
    <artifactId>commons-compress-tar</artifactId>
    <version>1.1.0</version>
    <dependencies>
        <dependency>
            <groupId>org.example</groupId>
            <artifactId>commons-compress-core</artifactId>
            <version>1.1.0</version>
            <scope>compile</scope>
        </dependency>
    </dependencies>
</project>
```

## `svg:1.0.0`

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd" xmlns="http://maven.apache.org/POM/4.0.0"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
  <!-- This module was also published with a richer model, Gradle metadata,  -->
  <!-- which should be used instead. Do not delete the following line which  -->
  <!-- is to indicate to Gradle or any Gradle module metadata file consumer  -->
  <!-- that they should prefer consuming it instead. -->
  <!-- do_not_remove: published-with-gradle-metadata -->
  <modelVersion>4.0.0</modelVersion>
  <groupId>org.example</groupId>
  <artifactId>svg</artifactId>
  <version>1.0.0</version>
  <dependencies>
    <dependency>
      <groupId>org.example</groupId>
      <artifactId>commons-compress</artifactId>
      <version>1.0.0</version>
      <scope>runtime</scope>
    </dependency>
  </dependencies>
</project>
```

## `http:1.1.0`

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd" xmlns="http://maven.apache.org/POM/4.0.0"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
  <!-- This module was also published with a richer model, Gradle metadata,  -->
  <!-- which should be used instead. Do not delete the following line which  -->
  <!-- is to indicate to Gradle or any Gradle module metadata file consumer  -->
  <!-- that they should prefer consuming it instead. -->
  <!-- do_not_remove: published-with-gradle-metadata -->
  <modelVersion>4.0.0</modelVersion>
  <groupId>org.example</groupId>
  <artifactId>http</artifactId>
  <version>1.1.0</version>
  <dependencies>
    <dependency>
      <groupId>org.example</groupId>
      <artifactId>commons-compress-tar</artifactId>
      <version>1.1.0</version>
      <scope>runtime</scope>
    </dependency>
  </dependencies>
</project>
```