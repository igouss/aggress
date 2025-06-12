# Web Parsing Infrastructure Root Package

This package serves as the root for all web parsing functionality in the Aggress Web Crawler, organizing the parsing
architecture into specialized sub-packages.

## Package Structure

### Sub-Packages

#### `/productParser`

**Purpose:** Contains parsers for extracting structured product data from individual product pages.  
**Scope:** Raw HTML product page analysis and data extraction  
**Parser Count:** 40+ site-specific product page parsers for Canadian retailers

#### `/webPageParsers`

**Purpose:** Contains parsers for analyzing web page structure and extracting navigation links.  
**Scope:** Front page analysis, category discovery, and product page URL extraction  
**Parser Count:** 35+ retailer-specific web page parsers

## Architecture Integration

This package organizes the **Content Parsing Layer**:

- **Two-Phase Parsing**: Web page discovery followed by product data extraction
- **Site-Specific Logic**: Customized parsers for each retailer's unique HTML structure
- **Factory Pattern**: Parser factories create appropriate parsers for each domain
- **Error Handling**: Robust parsing with fallback strategies for site changes

## Parsing Workflow

1. **Web Page Parsing**: Discover product URLs from category and listing pages
2. **Content Download**: Fetch individual product pages via HTTP client
3. **Product Parsing**: Extract structured product data from HTML content
4. **Data Validation**: Validate and normalize extracted product information
5. **Storage**: Persist products to Elasticsearch and cache in Redis

## Design Principles

- **Site Isolation**: Each retailer has independent parsing logic
- **Maintainability**: Clear separation between different parsing concerns
- **Extensibility**: Easy addition of new retailer parsers
- **Robustness**: Graceful handling of HTML structure changes

## Parser Categories

- **Front Page Parsers**: Extract navigation and category links
- **Product List Parsers**: Extract product URLs from category/search pages
- **Product Page Parsers**: Extract detailed product information
- **Multi-Page Parsers**: Handle paginated product listings

## Maintenance Notes

- Each retailer requires specialized parsing logic due to unique HTML structures
- Monitor for site changes that break existing parsers
- Implement comprehensive testing for parser accuracy
- Consider headless browser integration for JavaScript-heavy sites
- Document parsing patterns for easier maintenance