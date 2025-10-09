# Jarsplit sample

This is a sample project to show how you could split jar into multiple jars with backward compatibility.

https://jlbp.dev/JLBP-6 suggests "Rename both Java package and Maven ID", and they say
there will be issues if you just rename maven ID without renaming packages.

Here's a quote from JLBP-6:

> **Case 2: Rename Maven ID**. Build systems can unexpectedly pull in the JAR files of both the old artifact (g1:a1:1.0.0)
> and the new artifact (g2:a2:2.0.0) into their class path through transitive dependencies, because Maven artifact
> resolution treats the two artifacts as distinct. This results in “overlapping classes” since they have classes
> with the same fully-qualified path. This can lead to runtime exceptions such as NoSuchMethodDefError and can be
> a compile-time error in Java 9 and later. **NEVER DO THIS**.

This sample shows that Gradle does **not** suffer from that issue.

# Project layout

- `v1` — single-jar `commons-compress.jar` version of the library `org.example:commons-compress`
- `v2` — multi-jar evolution: `commons-compress-core.jar`, `commons-compress-tar.jar`, ..., and `commons-compress.pom` bom
- `lib-user-gradle` — sample Gradle project that depends on both old `commons-compress:1.0.0` and new `commons-compress-tar:2.0.0`
- `lib-user-maven` — sample Maven project that depends on both old `commons-compress:1.0.0` and new `commons-compress-tar:2.0.0`
- `lib-user-maven4` — sample Maven 4 project that depends on both old `commons-compress:1.0.0` and new `commons-compress-tar:2.0.0`

# How to run

```bash
# execute ./test.sh or the commands below manually
(cd v1; ./gradlew publishAllPublicationsToRepoRepository publishToMavenLocal)
(cd v2; ./gradlew publishAllPublicationsToRepoRepository publishToMavenLocal)
(cd lib-user-gradle; ./gradlew dependencies --configuration runtimeClasspath)
(cd lib-user-maven; mvn dependency:tree)
(cd lib-user-maven4; ./mvnw dependency:tree)
```

# Results

## Gradle 8.14.3

Gradle uses constraints like `commons-compress-tar:2.0.0 -> commons-compress:2.0.0`, so it bumps
`commons-compress` from 1.0.0 to 2.0.0 should an older version of the library be on the classpath.
This indicates **Gradle is not affected** by JLBP-6 as long as the new artifacts add constraints to the BOM.

```
------------------------------------------------------------
Root project 'lib-user-gradle'
------------------------------------------------------------

runtimeClasspath - Runtime classpath of source set 'main'.
+--- org.example:commons-compress:1.0.0 -> 2.0.0
|    +--- org.example:commons-compress-core:2.0.0
|    |    \--- org.example:commons-compress:2.0.0 (c)
|    +--- org.example:commons-compress-tar:2.0.0
|    |    +--- org.example:commons-compress-core:2.0.0 (*)
|    |    \--- org.example:commons-compress:2.0.0 (c)
|    \--- org.example:commons-compress-xz:2.0.0
|         +--- org.example:commons-compress-core:2.0.0 (*)
|         \--- org.example:commons-compress:2.0.0 (c)
\--- org.example:commons-compress-tar:2.0.0 (*)

(c) - A dependency constraint, not a dependency. The dependency affected by the constraint occurs elsewhere in the tree.
(*) - Indicates repeated occurrences of a transitive dependency subtree. Gradle expands transitive dependency subtrees only once per project; repeat occurrences only display the root of the subtree, followed by this annotation.
```

## Maven 3.9.11

Maven does not use `<dependencyManagement>` from `commons-compress-tar`, so it does not see `commons-compress:2.0.0` should be used.
This indicates **Maven 3.9.11 is affected** by JLBP-6.

```
[INFO] --- dependency:3.7.0:tree (default-cli) @ lib-user-maven ---
[INFO] org.example:lib-user-maven:jar:1.0.0
[INFO] +- org.example:commons-compress:jar:1.0.0:compile
[INFO] \- org.example:commons-compress-tar:jar:2.0.0:compile
[INFO]    \- org.example:commons-compress-core:jar:2.0.0:compile
```

## Maven 4.0.0-rc-4

Maven does not use `<dependencyManagement>` from `commons-compress-tar`, so it does not see `commons-compress:2.0.0` should be used.
This indicates **Maven 4.0.0-rc-4 is affected** by JLBP-6.

```
[INFO] --- dependency:3.9.0:tree (default-cli) @ lib-user-maven ---
[INFO] org.example:lib-user-maven:jar:1.0.0
[INFO] +- org.example:commons-compress:jar:1.0.0:compile
[INFO] \- org.example:commons-compress-tar:jar:2.0.0:compile
[INFO]    \- org.example:commons-compress-core:jar:2.0.0:compile
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

## `commons-compress:2.0.0`

BOM artifact with the list of module versions.
Note: it uses `<dependency>` so the users shoul get all the modules in the dependency tree if they
add a dependency on `commons-compress:2.0.0`.

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
    <version>2.0.0</version>
    <packaging>pom</packaging>
    <dependencies>
        <dependency>
            <groupId>org.example</groupId>
            <artifactId>commons-compress-core</artifactId>
            <version>2.0.0</version>
            <scope>compile</scope>
        </dependency>
        <dependency>
            <groupId>org.example</groupId>
            <artifactId>commons-compress-tar</artifactId>
            <version>2.0.0</version>
            <scope>compile</scope>
        </dependency>
        <dependency>
            <groupId>org.example</groupId>
            <artifactId>commons-compress-xz</artifactId>
            <version>2.0.0</version>
            <scope>compile</scope>
        </dependency>
    </dependencies>
</project>
```

## `commons-compress-core:2.0.0`

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
    <version>2.0.0</version>
    <dependencyManagement>
        <dependencies>
            <dependency>
                <groupId>org.example</groupId>
                <artifactId>commons-compress</artifactId>
                <version>2.0.0</version>
            </dependency>
        </dependencies>
    </dependencyManagement>
</project>
```

## `commons-compress-tar:2.0.0`

Tar support for the library. It depends on `commons-compress-core:2.0.0` which itself depends on the BOM 2.0.0.

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
    <version>2.0.0</version>
    <dependencies>
        <dependency>
            <groupId>org.example</groupId>
            <artifactId>commons-compress-core</artifactId>
            <version>2.0.0</version>
            <scope>compile</scope>
        </dependency>
    </dependencies>
</project>
```
