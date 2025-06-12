# Common Utilities Package

This package provides shared utility classes and infrastructure components used across all modules in the Aggress Web
Crawler system.

## Classes

### AppProperties.java

**Purpose:** Centralized configuration management for application properties.  
**Architecture Role:** Configuration abstraction layer providing access to properties from files, environment variables,
and defaults.  
**Dependencies:** Standard Java Properties API  
**Used By:** All modules for configuration access (database connections, API keys, etc.)

### ItemAppender.java

**Purpose:** Custom logging appender for structured log aggregation.  
**Architecture Role:** Logging infrastructure component for centralized log management and monitoring.  
**Dependencies:** Logback logging framework  
**Used By:** All modules through logging configuration

### PropertyNotFoundException.java

**Purpose:** Custom exception for missing or invalid configuration properties.  
**Architecture Role:** Error handling for configuration-related failures.  
**Dependencies:** Standard Java Exception hierarchy  
**Used By:** AppProperties and configuration-dependent components

### Tuple.java

**Purpose:** Generic data structure for holding pairs of related values.  
**Architecture Role:** Utility data structure for method returns and data coupling.  
**Dependencies:** None (pure utility class)  
**Used By:** Various modules for multi-value returns and data pairing

### package-info.java

**Purpose:** Package-level documentation and annotations for the utilities package.  
**Architecture Role:** Provides package metadata and common import declarations.

## Architecture Integration

This package provides **cross-cutting concerns** for the entire system:

- **Configuration Management**: Centralized property access pattern
- **Logging Infrastructure**: Structured logging for monitoring and debugging
- **Error Handling**: Standardized exception patterns
- **Utility Functions**: Common data structures and helper methods

## Design Patterns

- **Singleton Pattern**: AppProperties manages global configuration state
- **Exception Translation**: Converts low-level errors to domain-specific exceptions
- **Utility Class Pattern**: Static methods for common operations
- **Configuration Pattern**: Externalized configuration management

## Maintenance Notes

- AppProperties changes may require updates across all modules
- ItemAppender configuration is in logback.xml files
- Add new utility classes here for cross-module functionality
- Ensure thread safety for shared utility components