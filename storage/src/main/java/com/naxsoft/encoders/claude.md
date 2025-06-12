# Data Encoders Package

This package provides serialization and encoding utilities for converting domain entities to and from various storage
formats used in Redis and Elasticsearch.

## Classes

### Encoder.java

**Purpose:** Abstract base class defining the contract for data serialization/deserialization.  
**Architecture Role:** Interface abstraction for pluggable encoding strategies.  
**Dependencies:** Common entity package  
**Used By:** Concrete encoder implementations and storage services

### ProductEntityEncoder.java

**Purpose:** Specialized encoder for ProductEntity serialization to JSON and binary formats.  
**Architecture Role:** Data access layer component handling product data persistence formats.  
**Dependencies:** ProductEntity, JSON libraries  
**Used By:** Redis and Elasticsearch storage services for product data

### WebPageEntityEncoder.java

**Purpose:** Specialized encoder for WebPageEntity serialization for crawl state persistence.  
**Architecture Role:** Data access layer component managing web page metadata persistence.  
**Dependencies:** WebPageEntity, JSON libraries  
**Used By:** Redis storage service for crawl queue and page state management

### package-info.java

**Purpose:** Package-level documentation and annotations for the encoders package.  
**Architecture Role:** Provides package metadata and common encoding patterns.

## Architecture Integration

This package implements the **Data Access Layer** encoding strategy:

- **Storage Abstraction**: Decouples domain objects from storage format details
- **Multi-Format Support**: Handles JSON (Elasticsearch) and binary (Redis) serialization
- **Version Compatibility**: Manages data format evolution and backward compatibility
- **Performance Optimization**: Efficient serialization for high-volume crawler data

## Design Patterns

- **Strategy Pattern**: Pluggable encoding algorithms for different storage backends
- **Template Method**: Base encoder class defines common serialization workflow
- **Factory Pattern**: Creates appropriate encoders for specific entity types
- **Adapter Pattern**: Adapts domain objects to storage-specific formats

## Storage Targets

- **Redis**: Binary encoding for fast caching and temporary storage
- **Elasticsearch**: JSON encoding for search indexing and querying
- **File System**: JSON encoding for backup and data export

## Maintenance Notes

- When adding new entity fields, update corresponding encoder methods
- Maintain backward compatibility for existing serialized data
- Consider compression for large objects to reduce storage overhead
- Validate deserialization with unit tests for all supported formats