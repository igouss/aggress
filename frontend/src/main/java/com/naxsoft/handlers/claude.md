# Frontend HTTP Handlers Package

This package contains HTTP request handlers for the frontend web interface, implementing the controller layer of the MVC
pattern for the public search interface.

## Classes

### IndexHandler.java

**Purpose:** Handles requests for the main index page and home page functionality.  
**Architecture Role:** Controller for the main landing page and navigation interface.  
**Dependencies:** Template engine, application context, search services  
**Used By:** Vert.x router for index page requests (GET /)

### SearchHandler.java

**Purpose:** Handles product search requests and result rendering.  
**Architecture Role:** Controller for search functionality including query processing and result display.  
**Dependencies:** Elasticsearch client, template engine, search parameters  
**Used By:** Vert.x router for search requests (GET /search, POST /search)

### package-info.java

**Purpose:** Package-level documentation and annotations for the handlers package.  
**Architecture Role:** Provides package metadata and HTTP handler patterns.

## Architecture Integration

This package implements the **Web Controller Layer**:

- **Request Processing**: Handle HTTP requests and extract parameters
- **Business Logic**: Coordinate search operations and data retrieval
- **Response Generation**: Generate HTML responses using template engine
- **Error Handling**: Handle errors gracefully with user-friendly error pages

## Request Handling Patterns

### IndexHandler Responsibilities

- **Home Page Rendering**: Display main search interface and navigation
- **Category Display**: Show product categories and retailer listings
- **Statistics**: Display summary statistics about available products
- **Navigation**: Provide links to search functionality and help pages

### SearchHandler Responsibilities

- **Query Processing**: Parse and validate search parameters and filters
- **Search Execution**: Execute Elasticsearch queries with proper error handling
- **Result Processing**: Format search results for template rendering
- **Pagination**: Handle result pagination and page navigation
- **Filter Management**: Process category, price, and retailer filters

## Design Patterns

- **Command Pattern**: Handlers encapsulate request processing operations
- **Template Method**: Common request handling workflow across handlers
- **Strategy Pattern**: Different handling strategies for GET vs POST requests
- **Chain of Responsibility**: Request filtering and validation chains

## HTTP Operations

### Supported Request Types

- **GET /**: Main index page with search interface
- **GET /search**: Search results page with query parameters
- **POST /search**: Search form submission with filters
- **GET /static/***: Static resource serving (CSS, JS, images)

### Request Parameters

- **q**: Search query string for full-text search
- **category**: Product category filter
- **retailer**: Retailer name filter
- **minPrice**: Minimum price filter
- **maxPrice**: Maximum price filter
- **page**: Result page number for pagination
- **sort**: Sort order (relevance, price, name, date)

## Template Integration

- **Thymeleaf Templates**: Server-side HTML rendering with dynamic content
- **Model Binding**: Pass search results and metadata to templates
- **Fragment Reuse**: Common page elements (header, footer, navigation)
- **Internationalization**: Support for multiple languages (future)

## Error Handling

- **Graceful Degradation**: Handle search service failures gracefully
- **User Feedback**: Display helpful error messages for invalid requests
- **Logging**: Comprehensive logging for debugging and monitoring
- **Fallback Pages**: Static error pages when template rendering fails

## Performance Considerations

- **Caching**: Cache frequently accessed data and template fragments
- **Async Processing**: Non-blocking request handling with Vert.x
- **Connection Pooling**: Efficient database connection management
- **Resource Optimization**: Minified CSS/JS and optimized images

## Maintenance Notes

- Monitor search performance and optimize slow queries
- Implement proper input validation and sanitization
- Add comprehensive error handling for edge cases
- Consider implementing search analytics and user behavior tracking
- Update responsive design for mobile device compatibility