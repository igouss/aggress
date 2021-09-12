package com.naxsoft.commands;

import com.naxsoft.entity.WebPageEntity;
import com.naxsoft.parsers.webPageParsers.WebPageParserFactory;
import com.naxsoft.parsingService.WebPageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.HashSet;
import java.util.Set;

/**
 * Crawl pages from initial data-set walking breath first. For each page generate one or more sub-pages to parse.
 * Stop at leafs.
 * Process the stream of unparsed webpages. Processed web pages are saved into the
 * database and the page is marked as parsed
 */
@Slf4j
@RequiredArgsConstructor
public class CrawlCommand implements Command {

    private final WebPageService webPageService;
    private final WebPageParserFactory webPageParserFactory;

    @Override
    public void setUp() throws CLIException {
    }

    @Override
    public void start() throws CLIException {
        Set<WebPageEntity> pagesToParse = new HashSet<>();
        pagesToParse.addAll(webPageService.getUnparsedByType("frontPage"));
        pagesToParse.addAll(webPageService.getUnparsedByType("productList"));
        pagesToParse.addAll(webPageService.getUnparsedByType("productPage"));
    }

    @Override
    public void tearDown() throws CLIException {

    }
}
