# Crawler Application Root Package

This package contains the main application entry point and core dependency injection configuration for the Aggress Web
Crawler system.

## Classes

### Crawler.java

**Purpose:** Main application entry point and command-line interface for the crawler system.  
**Architecture Role:** Application bootstrap class that initializes dependency injection and executes crawler
commands.  
**Dependencies:** Dagger 2 components, command implementations, CLI argument parsing  
**Used By:** Standalone execution, Docker containers, and Kubernetes deployments

### ApplicationComponent.java

**Purpose:** Dagger 2 root component interface defining the main dependency injection graph.  
**Architecture Role:** Dependency injection configuration root that wires together all application modules.  
**Dependencies:** All Dagger modules (Redis, Elasticsearch, HTTP client, parsers, etc.)  
**Used By:** Crawler main class for application initialization

### package-info.java

**Purpose:** Package-level documentation and annotations for the root crawler package.  
**Architecture Role:** Provides package metadata and main application architecture overview.

## Architecture Integration

This package provides the **Application Bootstrap Layer**:

- **Dependency Injection Root**: Configures and initializes all application components
- **Command-Line Interface**: Processes CLI arguments and executes appropriate operations
- **Application Lifecycle**: Manages startup, execution, and shutdown sequences
- **Error Handling**: Top-level exception handling and exit code management

## Application Commands

The Crawler supports these primary operations:

- **-createESIndex**: Initialize Elasticsearch indices for product storage
- **-clean**: Clear all existing data from storage backends
- **-populate**: Populate initial URL queues for crawling
- **-crawl**: Execute web crawling operations across all configured sites
- **-parse**: Parse downloaded content and extract product information

## Design Patterns

- **Dependency Injection**: Dagger 2 for compile-time dependency resolution
- **Command Pattern**: Separate command classes for each crawler operation
- **Factory Pattern**: Creates appropriate command instances based on CLI args
- **Template Method**: Common application lifecycle across different commands

## Execution Modes

- **Standalone JAR**: Direct Java execution for development and testing
- **Docker Container**: Containerized execution for consistent environments
- **Kubernetes Job**: Scheduled or triggered execution in Kubernetes clusters
- **Development Mode**: IDE execution with hot reloading capabilities

## Maintenance Notes

- ApplicationComponent must include all new Dagger modules
- Add new CLI options in Crawler.java main method
- Ensure proper resource cleanup in application shutdown hooks
- Update Docker health checks when adding new dependencies
- Consider startup time optimizations for Kubernetes job execution