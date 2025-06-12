# HTTP Crawler Infrastructure Package

This package provides the core HTTP client infrastructure for web crawling, including asynchronous HTTP handling, cookie
management, and proxy rotation capabilities.

## Classes

### HttpClient.java

**Purpose:** Abstract interface defining the contract for HTTP client operations.  
**Architecture Role:** Abstraction layer for HTTP operations allowing pluggable client implementations.  
**Dependencies:** None (interface definition)  
**Used By:** Concrete HTTP client implementations and crawler services

### AhcHttpClient.java

**Purpose:** AsyncHttpClient-based implementation of HTTP client interface.  
**Architecture Role:** Primary HTTP client providing async request handling with connection pooling.  
**Dependencies:** AsyncHttpClient library, cookie management, proxy management  
**Used By:** Crawler commands and web page downloaders

### Cookie.java

**Purpose:** Interface defining the contract for HTTP cookie management.  
**Architecture Role:** Cookie abstraction for different cookie implementation strategies.  
**Dependencies:** None (interface definition)  
**Used By:** Cookie implementations and HTTP client services

### DefaultCookie.java

**Purpose:** Default implementation of cookie interface for standard HTTP cookie handling.  
**Architecture Role:** Cookie management implementation supporting standard HTTP cookie operations.  
**Dependencies:** Cookie interface, HTTP standards  
**Used By:** HTTP client for session management and authentication

### AbstractCompletionHandler.java

**Purpose:** Base class for asynchronous HTTP request completion handlers.  
**Architecture Role:** Template for async HTTP response processing with common error handling.  
**Dependencies:** AsyncHttpClient completion handler API  
**Used By:** Concrete completion handlers for specific response processing

### ProxyManager.java

**Purpose:** Manages proxy rotation and configuration for anonymous crawling.  
**Architecture Role:** Infrastructure component providing proxy rotation and failure handling.  
**Dependencies:** Proxy configuration, connection validation  
**Used By:** HTTP client for IP rotation and anonymity

### package-info.java

**Purpose:** Package-level documentation and annotations for the crawler infrastructure package.  
**Architecture Role:** Provides package metadata and HTTP client architecture patterns.

## Architecture Integration

This package provides the **HTTP Communication Layer**:

- **Asynchronous Operations**: Non-blocking HTTP requests for high throughput
- **Connection Management**: Connection pooling and reuse for efficiency
- **Session Management**: Cookie handling for authenticated crawling
- **Anonymity Features**: Proxy rotation to avoid IP blocking
- **Error Handling**: Robust error handling for network failures

## Key Features

- **Async HTTP Requests**: High-performance non-blocking HTTP operations
- **Cookie Support**: Automatic cookie handling for session management
- **Proxy Rotation**: Automatic proxy switching for anonymity
- **Connection Pooling**: Efficient connection reuse across requests
- **Request Rate Limiting**: Configurable delays to respect robots.txt
- **SSL/TLS Support**: HTTPS crawling with certificate validation
- **Compression**: Automatic gzip/deflate content decompression

## Design Patterns

- **Strategy Pattern**: Pluggable HTTP client implementations
- **Template Method**: Common async completion handling patterns
- **Factory Pattern**: Creates appropriate client instances with configuration
- **Proxy Pattern**: ProxyManager adds proxy functionality to HTTP requests
- **Observer Pattern**: Completion handlers observe request lifecycle

## Configuration Options

- **Connection Limits**: Max connections per host and total
- **Timeouts**: Request, connection, and read timeout settings
- **User Agents**: Configurable user agent strings for different sites
- **Proxy Settings**: Proxy servers, authentication, and rotation policies
- **SSL Settings**: Certificate validation and protocol configurations

## Performance Considerations

- **Connection Pooling**: Reuses HTTP connections to reduce overhead
- **Async Processing**: Non-blocking operations for high concurrency
- **Compression**: Reduces bandwidth usage for large responses
- **Keep-Alive**: HTTP persistent connections for multiple requests
- **DNS Caching**: Reduces DNS lookup overhead for repeated requests

## Maintenance Notes

- Monitor connection pool usage and adjust limits for optimal performance
- Implement proper cleanup for connections and resources
- Update User-Agent strings periodically to avoid detection
- Monitor proxy health and implement failover mechanisms
- Consider HTTP/2 support for improved performance with modern sites