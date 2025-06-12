# Crawler Commands Package

This package implements the Command pattern for different crawler operations, providing a clean CLI interface and
modular execution of crawler functionality.

## Classes

### Command.java

**Purpose:** Abstract base class defining the common interface for all crawler commands.  
**Architecture Role:** Command pattern foundation providing consistent execution and error handling.  
**Dependencies:** Base logging and configuration infrastructure  
**Used By:** All concrete command implementations

### CLIException.java

**Purpose:** Custom exception for command-line interface and command execution errors.  
**Architecture Role:** Error handling abstraction for command-specific failures.  
**Dependencies:** Standard Java exception hierarchy  
**Used By:** All command implementations for error reporting

### CleanDBCommand.java

**Purpose:** Implements database and cache cleanup operations.  
**Architecture Role:** Data management command that clears Redis and Elasticsearch storage.  
**Dependencies:** Storage services (Redis, Elasticsearch)  
**Used By:** CLI with -clean flag for fresh crawler runs

### PopulateDBCommand.java

**Purpose:** Implements initial URL queue population for crawling operations.  
**Architecture Role:** Data initialization command that seeds the crawl queue with starting URLs.  
**Dependencies:** Storage services, site configuration  
**Used By:** CLI with -populate flag to initialize crawl jobs

### CrawlCommand.java

**Purpose:** Implements the main web crawling operations across all configured sites.  
**Architecture Role:** Core crawling command that downloads and processes web pages.  
**Dependencies:** HTTP client, web page parsers, storage services  
**Used By:** CLI with -crawl flag for main crawling operations

### ParseCommand.java

**Purpose:** Implements product data extraction from downloaded web pages.  
**Architecture Role:** Data processing command that extracts structured product information.  
**Dependencies:** Product parsers, storage services, parsing factories  
**Used By:** CLI with -parse flag for content analysis

### CreateESIndexCommand.java

**Purpose:** Implements Elasticsearch index creation and configuration.  
**Architecture Role:** Infrastructure command that prepares search indices.  
**Dependencies:** Elasticsearch client, index configuration files  
**Used By:** CLI with -createESIndex flag for search infrastructure setup

### package-info.java

**Purpose:** Package-level documentation and annotations for the commands package.  
**Architecture Role:** Provides package metadata and command pattern documentation.

## Architecture Integration

This package implements the **Command Layer**:

- **CLI Interface**: Clean separation between command-line parsing and business logic
- **Modular Operations**: Each major crawler function is encapsulated in a command
- **Error Handling**: Consistent error reporting and exit code management
- **Dependency Injection**: Commands receive dependencies through Dagger injection

## Command Execution Flow

1. **Argument Parsing**: CLI arguments determine which commands to execute
2. **Dependency Injection**: Dagger provides required services to commands
3. **Validation**: Commands validate prerequisites and configuration
4. **Execution**: Core business logic execution with progress reporting
5. **Cleanup**: Resource cleanup and result reporting

## Design Patterns

- **Command Pattern**: Encapsulates operations as objects for flexible execution
- **Template Method**: Base Command class defines common execution workflow
- **Dependency Injection**: Commands receive services rather than creating them
- **Chain of Responsibility**: Commands can be chained for complex workflows

## Command Dependencies

- **Storage Commands**: CleanDB, PopulateDB, CreateESIndex require storage services
- **Processing Commands**: Crawl, Parse require HTTP client and parser services
- **All Commands**: Require configuration, logging, and error handling infrastructure

## Execution Examples

```bash
# Full crawler pipeline
java -jar crawler.jar -createESIndex -clean -populate -crawl -parse

# Individual operations
java -jar crawler.jar -clean
java -jar crawler.jar -crawl
java -jar crawler.jar -parse
```

## Maintenance Notes

- Add new commands by extending Command base class and updating CLI parsing
- Ensure proper resource cleanup in command execution methods
- Implement progress reporting for long-running operations
- Consider command composition for complex multi-step operations
- Add comprehensive logging for debugging and monitoring