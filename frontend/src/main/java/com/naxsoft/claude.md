# Frontend Application Root Package

This package contains the main application entry point and core configuration for the public-facing search interface of
the Aggress Web Crawler system.

## Classes

### Server.java

**Purpose:** Main application entry point and web server bootstrap for the frontend interface.  
**Architecture Role:** Application bootstrap that initializes Vert.x web server and routing configuration.  
**Dependencies:** Vert.x web framework, HTTP handlers, application context  
**Used By:** Standalone execution, Docker containers, and Kubernetes deployments

### ApplicationContext.java

**Purpose:** Application-wide context and configuration management for the frontend services.  
**Architecture Role:** Configuration and service locator providing access to shared resources.  
**Dependencies:** Storage services, configuration properties, service instances  
**Used By:** HTTP handlers and service components for resource access

### package-info.java

**Purpose:** Package-level documentation and annotations for the frontend root package.  
**Architecture Role:** Provides package metadata and frontend architecture overview.

## Architecture Integration

This package provides the **Web Application Layer**:

- **HTTP Server**: Vert.x-based web server for handling public search requests
- **Request Routing**: URL routing to appropriate handler implementations
- **Static Content**: Serves CSS, JavaScript, and image resources
- **Template Rendering**: Thymeleaf template processing for dynamic content

## Frontend Capabilities

- **Product Search**: Full-text search across all crawled product data
- **Filtering**: Search results filtering by retailer, category, price range
- **Sorting**: Result sorting by relevance, price, name, date
- **Pagination**: Efficient pagination for large result sets
- **Responsive Design**: Mobile-friendly interface using Bootstrap CSS

## Technology Stack

- **Web Framework**: Vert.x for reactive, non-blocking HTTP handling
- **Template Engine**: Thymeleaf for server-side HTML rendering
- **CSS Framework**: Bootstrap for responsive design
- **JavaScript**: jQuery for client-side interactions
- **Search Backend**: Elasticsearch for product search queries

## Design Patterns

- **MVC Pattern**: Separation of model (data), view (templates), and controller (handlers)
- **Front Controller**: Single entry point routing requests to appropriate handlers
- **Template Method**: Common request handling patterns across different pages
- **Service Locator**: ApplicationContext provides access to shared services

## Request Flow

1. **HTTP Request**: Client browser sends search or page request
2. **Routing**: Vert.x router directs request to appropriate handler
3. **Service Layer**: Handler interacts with search services and data access
4. **Template Rendering**: Thymeleaf processes templates with search results
5. **HTTP Response**: Rendered HTML sent back to client browser

## Configuration

- **Server Port**: Default 8080, configurable via environment variables
- **Search Backend**: Elasticsearch connection configuration
- **Template Location**: Thymeleaf template directory configuration
- **Static Resources**: CSS, JS, and image serving configuration

## Maintenance Notes

- Monitor search performance and optimize Elasticsearch queries
- Implement proper error handling for search service failures
- Consider caching strategies for frequently accessed content
- Update responsive design for mobile device compatibility
- Implement analytics tracking for search usage patterns