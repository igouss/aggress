# Elasticsearch Storage Package

This package provides Elasticsearch integration for search indexing, querying, and document management of product data
in the Aggress Web Crawler system.

## Classes

### Elastic.java

**Purpose:** Primary Elasticsearch client and service for managing product search indices.  
**Architecture Role:** Data access layer implementation providing search and indexing capabilities.  
**Dependencies:** Elasticsearch Java client, ProductEntity, encoders  
**Used By:** Search services, data import/export tools, and query interfaces

### package-info.java

**Purpose:** Package-level documentation and annotations for the Elasticsearch package.  
**Architecture Role:** Provides package metadata and Elasticsearch-specific patterns.

## Architecture Integration

This package implements **Search and Analytics Storage**:

- **Full-Text Search**: Product name and description search capabilities
- **Faceted Search**: Category, price, retailer filtering
- **Analytics**: Aggregations for product statistics and trends
- **Scalability**: Distributed search across large product datasets

## Key Responsibilities

- **Index Management**: Create, update, and delete product indices
- **Document Operations**: Index, update, and delete product documents
- **Query Execution**: Execute complex search queries with filters and sorting
- **Bulk Operations**: Efficient batch indexing for large data imports
- **Health Monitoring**: Monitor cluster health and index statistics

## Search Capabilities

- **Product Search**: Full-text search across product names and descriptions
- **Filtering**: By retailer, category, price range, availability
- **Sorting**: By relevance, price, date added, alphabetical
- **Autocomplete**: Type-ahead suggestions for product searches
- **Analytics**: Product count by category, price distributions, retailer statistics

## Design Patterns

- **Repository Pattern**: Encapsulates Elasticsearch-specific data access logic
- **Builder Pattern**: Constructs complex search queries and aggregations
- **Connection Pool**: Manages Elasticsearch client connections efficiently
- **Circuit Breaker**: Handles Elasticsearch cluster failures gracefully

## Configuration

- **Index Mapping**: Defined in `elastic.product.guns.mapping.json`
- **Index Settings**: Configured in `elastic.product.guns.index.json`
- **Connection**: Host and port configured through AppProperties
- **Performance**: Bulk size, refresh intervals, and replica settings

## Maintenance Notes

- Monitor index performance and optimize mapping for query patterns
- Implement proper error handling for cluster connectivity issues
- Consider index lifecycle management for large datasets
- Update mapping carefully to avoid breaking existing data
- Use aliases for zero-downtime index updates during schema changes