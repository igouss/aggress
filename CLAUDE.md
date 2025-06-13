/# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

**Aggress** is a multi-module Java/Kotlin web crawler system designed to scrape Canadian firearms and outdoor equipment
retailers. The system consists of a distributed architecture with separate crawling, indexing, and search components.

## Build System & Common Commands

This project uses **Gradle 3.5** with a multi-module setup. Default tasks are `clean` and `build`.

### Core Build Commands

```bash
# Build all modules (default)
./gradlew

# Build specific module
./gradlew :crawler:build

# Clean build
./gradlew clean build

# Build Docker images
./gradlew buildDockerImages
```

### Testing

```bash
# Run crawler tests (requires flag)
./gradlew -Dcrawler.test=true test

# Run integration tests
./gradlew :crawler:integrationTest

# Test specific module
./gradlew :frontend:test
```

### Docker Development

```bash
# Build all Docker images using Jib (Eclipse Temurin Java 24)
./gradlew buildDockerImages

# Build specific components
./gradlew :crawler:jibDockerBuild
./gradlew :frontend:jibDockerBuild  
./gradlew :webadmin:jibDockerBuild

# Push to registry (if configured)
./gradlew :crawler:jib
./gradlew :frontend:jib
./gradlew :webadmin:jib

# Run full stack
cd docker && docker-compose up
```

## Architecture & Module Structure

### Core Modules

- **`crawler/`** - Main crawling engine with site-specific parsers for 40+ Canadian retailers
- **`frontend/`** - Public search interface (Vert.x + Thymeleaf, port 8080)
- **`webadmin/`** - Basic admin interface (Kotlin, port 8081)
- **`crawler-admin/`** - Rich admin panel with AdminLTE UI
- **`storage/`** - Elasticsearch and Redis integration layer
- **`common/`** - Shared utilities, models, and logging configuration

### Technology Stack

- **Java 24** (primary), **Kotlin** (webadmin)
- **Vert.x** (async web framework)
- **Elasticsearch 7.17.24** with Java API Client (search engine)
- **Redis** (caching/storage)
- **Dagger 2** (dependency injection)
- **RxJava 1.x** (reactive programming)
- **JSoup** (HTML parsing)
- **Thymeleaf** (templating)
- **Lombok** (code generation)

## Configuration Requirements

### Required Setup Before Running

1. **Create config file**: `crawler/src/main/resources/config.properties`
   ```properties
   canadiangunnutzLogin=
   canadiangunnutzPassword=
   redisHost=localhost
   redisPort=6379
   ```

2. **Import SSL certificates** (required for HTTPS crawling):
   ```bash
   # Linux/Mac
   ./crawler/src/main/resources/startssl-java-master/import-certs.sh
   
   # Windows  
   crawler\src\main\resources\startssl-java-master\import-certs.bat
   ```

## Running Components

### Crawler Operations

```bash
# Full crawl cycle (all operations)
java -jar crawler/build/libs/crawler-all-1.0-SNAPSHOT.jar -clean -populate -crawl -parse

# Individual operations
java -jar crawler-all-1.0-SNAPSHOT.jar -createESIndex
java -jar crawler-all-1.0-SNAPSHOT.jar -clean
java -jar crawler-all-1.0-SNAPSHOT.jar -populate
java -jar crawler-all-1.0-SNAPSHOT.jar -crawl
java -jar crawler-all-1.0-SNAPSHOT.jar -parse
```

### Web Services

```bash
# Frontend (port 8080)
java -jar frontend/build/libs/frontend-1.0-SNAPSHOT.jar

# WebAdmin (port 8081) 
java -jar webadmin/build/libs/webadmin-1.0-SNAPSHOT.jar
```

## Development Patterns

### Dependency Management

- Uses `gradle/libs.versions.toml` for centralized version management
- Dagger 2 for dependency injection throughout modules
- Annotation processing configured for Dagger code generation

### Code Conventions

- Java 24 target compatibility with latest language features
- RxJava 1.x for asynchronous operations (not RxJava 2)
- Incremental compilation enabled
- Compiler warnings for unchecked and deprecated usage
- Lombok edge version for Java 24 compatibility

### Module Dependencies

```
common ← storage ← crawler
       ← frontend
       ← webadmin  
       ← crawler-admin
```

### Docker Image Building

- Uses **Jib 3.4.5** for containerization (no Dockerfiles needed)
- All modules configured with `eclipse-temurin:24-jre` base image
- Automatic SSL certificate copying for crawler module
- Static assets (basedir) automatically copied for web modules

### Testing Strategy

- Uses **JUnit 5** with JUnit Platform integration
- Crawler tests require `-Dcrawler.test=true` flag
- Separate integration tests in `integrationTest` task
- Test logging shows full stack traces and exceptions
- Parallel test execution (5 forks, 50 tests per fork)

### Java 24 Compatibility

- **Important**: To avoid native access warnings, set environment variable:
  ```bash
  export GRADLE_OPTS="--enable-native-access=ALL-UNNAMED --add-opens=java.base/java.lang=ALL-UNNAMED"
  ```
- Alternative: Run commands with the environment variable:
  ```bash
  GRADLE_OPTS="--enable-native-access=ALL-UNNAMED" ./gradlew build
  ```

## Elasticsearch Integration

The crawler creates and manages Elasticsearch indices for product search using the modern **Java API Client** (replacing
deprecated High Level REST Client).

Product mapping and index configuration is defined in:

- `crawler/src/main/resources/elastic.product.guns.index.json`
- `crawler/src/main/resources/elastic.product.guns.mapping.json`

**Note**: Both storage and frontend modules now use the modern Elasticsearch Java API Client, eliminating all deprecated
Elasticsearch warnings.

## Development Guidelines

### Class Documentation Strategy

In each Java class directory, create a `claude.md` file with a small description about the class and how it fits into
the application architecture. Keep these files up-to-date during refactoring to maintain clear documentation of the
codebase structure and purpose of each class.

## Additional Development Guidance

- Always use latest features of Spring Boot
- After making code changes, make sure it compiles