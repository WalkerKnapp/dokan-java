Dokany-Java
======

## Introduction
Dokany-Java is a Java wrapper for the [Dokany 1.0.x releases](https://github.com/dokan-dev/dokany/releases).

## Runtime Dependencies
- [Java JRE 1.8](https://java.com/en/download/manual.jsp)

All dependencies can be seen [here](build.gradle).

- [JNA](https://github.com/java-native-access/jna) - provides access to [native Dokany functions](https://dokan-dev.github.io/dokany-doc/html/struct_d_o_k_a_n___o_p_e_r_a_t_i_o_n_s.html)
- [SLF4J](https://www.slf4j.org/)
	
## How to Build
Requires [Java JDK 1.8](http://jdk.java.net/8/)

### Download
To use Dokany-Java, it is recommended to build with either Gradle or Maven. If not possible, it can be downloaded as a jar file [here](https://jitpack.io/com/github/WalkerKnapp/dokan-java/da4af36/dokan-java-da4af36.jar).
In the following examples, `COMMIT_ID` should be replaced with the short hash of the latest commit. In the final release, this can be changed to `vVERSION_NUMBER`.

**Gradle**
Add the following to your `build.gradle`:

```groovy
repositories {
  maven {
    url 'https://jitpack.io'
  }
}
dependencies {
  compile 'com.github.WalkerKnapp:dokan-java:COMMIT_ID'
}
```

**Maven**
Add the following to your `pom.xml`:
```xml
<repositories>
	<repository>
        <id>jitpack.io</id>
	    <url>https://jitpack.io</url>
	</repository>
</repositories>

<dependency>
	    <groupId>com.github.WalkerKnapp</groupId>
	    <artifactId>dokan-java</artifactId>
	    <version>da4af36</version>
</dependency>
```

## Development Examples
For an example on how to develop using this library, see the examples package [com.dokany.java.examples](src//test/java/com/dokany/java/examples/).
