# Product Page Parsers Package

This package contains specialized parsers for extracting structured product data from individual retailer product pages.
Each parser handles the unique HTML structure and data extraction logic for specific Canadian firearms and outdoor
equipment retailers.

## Core Classes

### ProductParser.java

**Purpose:** Abstract interface defining the contract for product page parsing operations.  
**Architecture Role:** Parsing abstraction layer allowing pluggable parser implementations.  
**Dependencies:** ProductEntity domain model  
**Used By:** All concrete product parser implementations

### AbstractRawPageParser.java

**Purpose:** Base class providing common functionality for raw HTML product page parsing.  
**Architecture Role:** Template method implementation with shared parsing utilities.  
**Dependencies:** JSoup HTML parsing, ProductEntity, common utilities  
**Used By:** All site-specific product parser implementations

### ProductParserFactory.java

**Purpose:** Factory class for creating appropriate product parsers based on URL/domain.  
**Architecture Role:** Factory pattern implementation for parser selection and instantiation.  
**Dependencies:** All product parser implementations  
**Used By:** Product parsing services and command implementations

### ProductParseException.java

**Purpose:** Custom exception for product parsing failures and data extraction errors.  
**Architecture Role:** Error handling abstraction for parsing-specific failures.  
**Dependencies:** Standard Java exception hierarchy  
**Used By:** All product parser implementations for error reporting

### NoopParser.java

**Purpose:** Null object pattern implementation for unsupported or disabled parsers.  
**Architecture Role:** Default parser that safely handles unsupported domains.  
**Dependencies:** ProductParser interface  
**Used By:** Factory when no specific parser is available for a domain

## Site-Specific Parsers (40+ Implementations)

Each retailer has a specialized parser class following the naming pattern `[RetailerName]RawPageParser.java`:

### Major Canadian Retailers Covered

- **Cabelas**: Large outdoor retailer with complex product catalogs
- **Canadian Tire**: Major retail chain with firearms section
- **Bass Pro Shops**: Outdoor equipment and firearms specialist
- **Sail**: Quebec-based outdoor equipment retailer
- **Al Flaherty's**: Specialized firearms retailer
- **Bullseye North**: Online firearms and outdoor equipment
- **Dante Sports**: Regional sporting goods retailer
- **And 35+ additional specialized retailers**

## Architecture Integration

This package implements **Product Data Extraction**:

- **HTML Parsing**: JSoup-based HTML content analysis and data extraction
- **Data Normalization**: Convert retailer-specific formats to standardized ProductEntity
- **Error Handling**: Robust parsing with fallback strategies for site changes
- **Validation**: Data quality checks and field validation

## Common Parsing Patterns

### Product Information Extracted

- **Basic Details**: Name, description, SKU, brand
- **Pricing**: Current price, sale price, currency
- **Availability**: In stock, out of stock, backorder status
- **Categories**: Product categories and classifications
- **Images**: Product image URLs
- **Specifications**: Technical details and specifications
- **Retailer Info**: Store name, product URL, last updated

### HTML Parsing Techniques

- **CSS Selectors**: Target specific HTML elements containing product data
- **XPath Expressions**: Complex path-based element selection
- **Regular Expressions**: Extract structured data from text content
- **JSON-LD**: Parse structured data markup when available
- **Microdata**: Extract schema.org product markup

## Design Patterns

- **Template Method**: AbstractRawPageParser defines common parsing workflow
- **Strategy Pattern**: Different parsing strategies for different HTML structures
- **Factory Pattern**: ProductParserFactory creates appropriate parser instances
- **Null Object**: NoopParser provides safe default behavior

## Data Quality Features

- **Field Validation**: Ensure extracted data meets quality standards
- **Data Cleaning**: Remove HTML tags, normalize whitespace, format prices
- **Fallback Extraction**: Multiple extraction strategies for critical fields
- **Error Logging**: Detailed logging for debugging parser failures

## Maintenance Challenges

- **Site Changes**: Retailers frequently update HTML structure breaking parsers
- **Rate Limiting**: Respect robots.txt and implement appropriate delays
- **Anti-Bot Measures**: Handle CAPTCHA, JavaScript rendering, and bot detection
- **Data Consistency**: Maintain consistent data formats across diverse sites

## Testing Strategy

- **HTML Fixtures**: Saved HTML pages for regression testing
- **Data Validation**: Automated checks for required fields and data quality
- **Site Monitoring**: Regular checks for parser functionality
- **Performance Testing**: Ensure parsing speed meets throughput requirements

## Maintenance Notes

- Monitor each retailer site for HTML structure changes
- Implement comprehensive unit tests with real HTML samples
- Use CSS selector specificity to reduce brittleness to minor changes
- Consider headless browser integration for JavaScript-heavy sites
- Document parsing logic for each site to aid future maintenance
- Implement parser health monitoring and alerting for failures