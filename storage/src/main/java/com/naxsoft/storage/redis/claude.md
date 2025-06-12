# Redis Storage Package

This package provides Redis integration for caching, session storage, and temporary data persistence in the Aggress Web
Crawler system.

## Classes

### RedisDatabase.java

**Purpose:** Primary Redis client and service for caching and temporary data storage.  
**Architecture Role:** Data access layer implementation providing fast key-value storage and caching.  
**Dependencies:** Redis Java client (Lettuce), entity encoders, connection pooling  
**Used By:** Crawler services, session management, and caching layers

### package-info.java

**Purpose:** Package-level documentation and annotations for the Redis package.  
**Architecture Role:** Provides package metadata and Redis-specific patterns.

## Architecture Integration

This package implements **High-Performance Caching and Temporary Storage**:

- **Crawl State Management**: Track page crawl status and queue management
- **Session Storage**: Temporary data for multi-step crawling operations
- **Rate Limiting**: Track request counts and implement throttling
- **Cache Layer**: Fast access to frequently requested data

## Key Responsibilities

- **Key-Value Operations**: Get, set, delete operations for cached data
- **Expiration Management**: TTL-based data lifecycle for temporary storage
- **Queue Operations**: FIFO queues for crawl job management
- **Atomic Operations**: Transactions and atomic increments for counters
- **Connection Management**: Pool Redis connections for high concurrency

## Data Types Stored

- **WebPage Entities**: Crawl queue and page processing state
- **Product Entities**: Temporary storage during processing pipeline
- **Session Data**: Authentication tokens and crawler state
- **Rate Limiting Counters**: Request counts per domain/IP
- **Cache Entries**: Frequently accessed configuration and lookup data

## Design Patterns

- **Repository Pattern**: Abstracts Redis operations behind clean interfaces
- **Connection Pool**: Manages Redis connections efficiently for high load
- **Template Method**: Standardizes Redis operation patterns
- **Cache-Aside**: Manual cache management with fallback to primary storage

## Redis Features Used

- **Strings**: Simple key-value storage for entities and configuration
- **Sets**: Unique collections for tracking processed URLs
- **Lists**: FIFO queues for crawl job management
- **Hashes**: Structured data storage for complex objects
- **Expiration**: TTL for temporary data and cache invalidation

## Configuration

- **Connection**: Host, port, and authentication via AppProperties
- **Connection Pool**: Size and timeout settings for high concurrency
- **Serialization**: Binary encoding for performance via entity encoders
- **Persistence**: Configured for appropriate durability vs. performance

## Maintenance Notes

- Monitor memory usage and implement appropriate eviction policies
- Use Redis pipelining for bulk operations to improve performance
- Implement proper error handling for connection failures
- Consider Redis clustering for high-availability production deployments
- Regularly monitor slow queries and optimize data structures