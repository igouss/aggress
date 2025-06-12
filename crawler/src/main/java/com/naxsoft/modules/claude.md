# Dependency Injection Modules Package

This package contains Dagger 2 dependency injection modules that configure and provide all major system components for
the Aggress Web Crawler.

## Classes

### CommandModule.java

**Purpose:** Provides dependency injection configuration for command pattern implementations.  
**Architecture Role:** DI module for CLI command objects and command factory configuration.  
**Dependencies:** Command implementations  
**Provides:** Command instances, command factory

### ElasticModule.java

**Purpose:** Configures Elasticsearch client and search services.  
**Architecture Role:** DI module for search infrastructure and document management.  
**Dependencies:** Elasticsearch client libraries, configuration  
**Provides:** Elasticsearch client, search services

### EncoderModule.java

**Purpose:** Configures data encoding and serialization services.  
**Architecture Role:** DI module for entity serialization across storage backends.  
**Dependencies:** Encoder implementations  
**Provides:** Entity encoders for Redis and Elasticsearch

### EventBusModule.java

**Purpose:** Configures event bus for inter-component communication.  
**Architecture Role:** DI module for asynchronous event handling and messaging.  
**Dependencies:** Event bus implementation  
**Provides:** Event bus instance, event handlers

### HttpClientModule.java

**Purpose:** Configures HTTP client infrastructure for web crawling.  
**Architecture Role:** DI module for HTTP communication and connection management.  
**Dependencies:** AsyncHttpClient, proxy configuration  
**Provides:** HTTP client instances, connection pools

### MetricsRegistryModule.java

**Purpose:** Configures metrics collection and monitoring infrastructure.  
**Architecture Role:** DI module for application performance monitoring.  
**Dependencies:** Metrics library  
**Provides:** Metrics registry, performance counters

### ProductParserFactoryModule.java

**Purpose:** Configures factory for creating site-specific product page parsers.  
**Architecture Role:** DI module for product data extraction services.  
**Dependencies:** Product parser implementations  
**Provides:** Product parser factory, parser instances

### ProductServiceModule.java

**Purpose:** Configures high-level product processing and management services.  
**Architecture Role:** DI module for product workflow coordination.  
**Dependencies:** Product parsers, storage services  
**Provides:** Product service instances

### RedisModule.java

**Purpose:** Configures Redis client and caching services.  
**Architecture Role:** DI module for fast key-value storage and caching.  
**Dependencies:** Redis client libraries, connection configuration  
**Provides:** Redis client, cache services

### SchedulerModule.java

**Purpose:** Configures task scheduling and job coordination services.  
**Architecture Role:** DI module for crawler task management and timing.  
**Dependencies:** Scheduler implementation  
**Provides:** Scheduler instances, job coordination

### VertxModule.java

**Purpose:** Configures Vert.x reactive framework components.  
**Architecture Role:** DI module for reactive programming and async operations.  
**Dependencies:** Vert.x libraries  
**Provides:** Vert.x instances, event loops

### WebPageParserFactoryModule.java

**Purpose:** Configures factory for creating site-specific web page parsers.  
**Architecture Role:** DI module for web page analysis and link extraction.  
**Dependencies:** Web page parser implementations  
**Provides:** Web page parser factory, parser instances

### WebPageServiceModule.java

**Purpose:** Configures high-level web page processing and management services.  
**Architecture Role:** DI module for web page workflow coordination.  
**Dependencies:** Web page parsers, storage services  
**Provides:** Web page service instances

### package-info.java

**Purpose:** Package-level documentation and annotations for the modules package.  
**Architecture Role:** Provides package metadata and DI architecture patterns.

## Architecture Integration

This package implements the **Dependency Injection Configuration Layer**:

- **Compile-Time DI**: Dagger 2 generates dependency injection code at compile time
- **Modular Configuration**: Each major system component has its own module
- **Singleton Management**: Ensures single instances of expensive resources
- **Configuration Binding**: Connects configuration properties to service instances

## Module Dependencies

```
ApplicationComponent
├── CommandModule
├── HttpClientModule
│   ├── ProxyManager
│   └── ConnectionPool
├── StorageModules
│   ├── RedisModule
│   └── ElasticModule
├── ParsingModules
│   ├── ProductParserFactoryModule
│   └── WebPageParserFactoryModule
├── ServiceModules
│   ├── ProductServiceModule
│   └── WebPageServiceModule
└── InfrastructureModules
    ├── VertxModule
    ├── SchedulerModule
    ├── EventBusModule
    └── MetricsRegistryModule
```

## Design Patterns

- **Module Pattern**: Encapsulates related dependency configurations
- **Factory Pattern**: Modules often provide factories for complex object creation
- **Singleton Pattern**: Expensive resources (clients, pools) are singletons
- **Provider Pattern**: Custom providers for complex initialization logic

## Configuration Scope

- **Singleton**: Database clients, connection pools, parsers factories
- **Instance**: Commands, services that maintain state
- **Prototype**: Lightweight objects created per request

## Maintenance Notes

- Add new modules when introducing major new system components
- Keep modules focused on single areas of responsibility
- Use `@Provides` methods for complex object construction
- Consider `@Binds` for simple interface-to-implementation binding
- Ensure proper lifecycle management for resources like connections
- Update ApplicationComponent when adding new modules