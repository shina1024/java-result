[![Java CI with Gradle](https://github.com/shina1024/java-result/actions/workflows/gradle.yml/badge.svg)](https://github.com/shina1024/java-result/actions/workflows/gradle.yml)

# java-result

A Java implementation of Rust's `Result<T, E>` type for functional error handling.

## Overview

This library provides a `Result` type that represents either a successful value (`Ok`) or an error (`Err`). It's designed to make error handling more explicit and functional, avoiding the need for exceptions in many cases.

## Features

- **Algebraic Data Type**: Sum type representing either success (`Ok`) or failure (`Err`)
- **Type-safe error handling**: Compile-time guarantees about error handling
- **Functional programming style**: Chain operations with `map`, `andThen`, `orElse`
- **Pattern matching support**: Leverage Java's switch expressions for clean control flow
- **Null-safe**: Proper handling of null values
- **Stream integration**: Convert to Java Streams for collection processing
- **Comprehensive API**: All the methods you'd expect from Rust's Result type

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
import jp.zodiac.javaresult.Result;

// Create results
Result<String, Exception> success = new Result.Ok<>("Hello");
Result<Integer, NumberFormatException> parseResult = new Result.Err<>(new NumberFormatException("Invalid number"));

// Basic operations
String value = success.unwrapOr("default");  // Safe unwrapping
Result<String, Exception> uppercased = success.map(String::toUpperCase);

// unwrap() requires exception handling
try {
    Integer number = parseResult.unwrap();  // Throws the contained NumberFormatException
} catch (NumberFormatException e) {
    // Handle specific error type
}

// Pattern matching (Java 21+)
String message = switch (success) {
    case Result.Ok(var value) -> "Success: " + value;
    case Result.Err(var error) -> "Error: " + error.getMessage();
};
```
