# Domain Entities Package

This package contains the core domain model entities that represent the fundamental data structures used throughout the
Aggress Web Crawler system.

## Classes

### ProductEntity.java

**Purpose:** Core domain object representing a product scraped from retailer websites.  
**Architecture Role:** Central data model for product information including name, price, description, URL, category, and
retailer details.  
**Dependencies:** None (pure data model)  
**Used By:** All modules for product data representation and persistence

### WebPageEntity.java

**Purpose:** Domain object representing a web page that has been or needs to be crawled.  
**Architecture Role:** Tracks web page metadata including URL, last crawl time, content hash, and processing status.  
**Dependencies:** None (pure data model)  
**Used By:** Crawler module for managing crawl queue and page state tracking

### package-info.java

**Purpose:** Package-level documentation and annotations for the entity package.  
**Architecture Role:** Provides package metadata and Javadoc for the domain model.

## Architecture Integration

This package forms the **core data model layer** of the system:

- **Storage Layer**: Entities are persisted to Redis and Elasticsearch
- **Crawler Layer**: Creates and populates these entities during crawling
- **Frontend Layer**: Displays product data from these entities
- **WebAdmin Layer**: Manages and monitors entity data

## Design Patterns

- **Domain Model Pattern**: Encapsulates business logic and data
- **Data Transfer Object**: Carries data between architectural layers
- **Immutable Objects**: Ensures data consistency across concurrent operations

## Maintenance Notes

- When adding new fields, update all encoders in the storage module
- Consider database migration scripts for schema changes
- Ensure backward compatibility for serialized data in Redis/Elasticsearch