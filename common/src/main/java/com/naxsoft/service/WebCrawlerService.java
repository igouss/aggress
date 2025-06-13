package com.naxsoft.service;

import com.naxsoft.entity.WebPageEntity;
import com.naxsoft.service.dto.CrawlRequest;
import com.naxsoft.service.dto.CrawlResult;
import com.naxsoft.service.dto.CrawlStats;
import com.naxsoft.service.dto.PageType;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Modern, clean service interface for web crawling operations.
 * <p>
 * This interface replaces Observable-based APIs with CompletableFuture for:
 * - Better composability and error handling
 * - Framework-agnostic design (no RxJava dependency)
 * - Easy testing and mocking
 * - Type-safe request/response objects
 * <p>
 * During migration, this runs alongside existing Observable-based services.
 */
public interface WebCrawlerService {

    /**
     * Start a crawl operation for the specified request parameters.
     *
     * @param request Crawl configuration and parameters
     * @return Future containing crawl results
     */
    CompletableFuture<CrawlResult> startCrawl(CrawlRequest request);

    /**
     * Get unparsed pages of a specific type for processing.
     *
     * @param pageType Type of pages to retrieve
     * @param limit    Maximum number of pages to return
     * @return Future containing list of unparsed pages
     */
    CompletableFuture<List<WebPageEntity>> getUnparsedPages(PageType pageType, int limit);

    /**
     * Get current crawler statistics and metrics.
     *
     * @return Future containing current crawl statistics
     */
    CompletableFuture<CrawlStats> getCrawlStatistics();

    /**
     * Parse a specific web page and extract products.
     *
     * @param page Web page entity to parse
     * @return Future containing parsing success status
     */
    CompletableFuture<Boolean> parsePage(WebPageEntity page);

    /**
     * Add a new web page to the crawl queue.
     *
     * @param page Web page entity to add
     * @return Future containing the assigned page ID
     */
    CompletableFuture<String> addWebPage(WebPageEntity page);

    /**
     * Check if the crawler service is healthy and operational.
     *
     * @return Future containing health status
     */
    CompletableFuture<Boolean> isHealthy();

    /**
     * Get pages by type with pagination support.
     *
     * @param pageType Type of pages to retrieve
     * @param offset   Starting offset for pagination
     * @param limit    Maximum number of pages to return
     * @return Future containing paginated list of pages
     */
    CompletableFuture<List<WebPageEntity>> getPagesByType(PageType pageType, int offset, int limit);

    /**
     * Stop all crawling operations gracefully.
     *
     * @return Future that completes when all operations are stopped
     */
    CompletableFuture<Void> stopCrawling();
}