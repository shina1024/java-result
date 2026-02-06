[![Java CI with Gradle](https://github.com/shina1024/java-result/actions/workflows/gradle.yml/badge.svg)](https://github.com/shina1024/java-result/actions/workflows/gradle.yml)

# java-result

A Java implementation inspired by Rust's `Result<T, E>` type for functional error handling.
In this library, the error type is `E extends Exception`.

## Overview

This library provides a `Result` type that represents either a successful value (`Ok`) or an error (`Err`). It's designed to make error handling more explicit and functional, avoiding the need for exceptions in many cases.

## Features

- **Algebraic Data Type**: Sum type representing either success (`Ok`) or failure (`Err`)
- **Type-safe error handling**: Explicit success/error branches with generic types
- **Functional programming style**: Chain operations with `map`, `andThen`, `orElse`
- **Pattern matching support**: Leverage Java's switch expressions for clean control flow
- **Defined null policy**: `Ok` may contain `null`; `Err` and functional arguments must be non-null
- **Stream integration**: Convert to Java Streams for collection processing
- **Rust-inspired API**: Core `Result` operations such as `map`, `mapErr`, `andThen`, and `orElse`

## Requirements

- Java 17 or higher
- **Java 21+ recommended**: Enhanced pattern matching with switch expressions for cleaner algebraic data type handling

## Installation

### Gradle (build.gradle.kts)

```kotlin
repositories {
    maven("https://jitpack.io")
}

dependencies {
    implementation("com.github.shina1024:java-result:1.0.0")
}
```

### Maven (pom.xml)

```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>

<dependencies>
    <dependency>
        <groupId>com.github.shina1024</groupId>
        <artifactId>java-result</artifactId>
        <version>1.0.0</version>
    </dependency>
</dependencies>
```

## Usage

```java
import java.io.IOException;
import jp.zodiac.javaresult.Result;

// Create results
Result<String, IOException> success = new Result.Ok<>("Hello");
Result<String, IOException> readResult = new Result.Err<>(new IOException("Failed to read file"));

// Basic operations
String value = success.unwrapOr("default");  // Safe unwrapping
Result<String, IOException> uppercased = success.map(String::toUpperCase);

// unwrap() requires handling when E is a checked exception
try {
    String content = readResult.unwrap();  // Throws the contained IOException
} catch (IOException e) {
    // Handle specific error type
}

// Pattern matching (Java 21+)
String message = switch (success) {
    case Result.Ok(var value) -> "Success: " + value;
    case Result.Err(var error) -> "Error: " + error.getMessage();
};
```
