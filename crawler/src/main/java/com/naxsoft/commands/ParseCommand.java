package com.naxsoft.commands;

import com.naxsoft.parsingService.WebPageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.HashSet;
import java.util.Set;

/**
 * Parse raw web pages entries, generate JSON representation and sent it to Elasticsearch
 */
@Slf4j
@RequiredArgsConstructor
public class ParseCommand implements Command {
    private final static Set<String> VALID_CATEGORIES = new HashSet<>();

    static {
        VALID_CATEGORIES.add("firearm");
        VALID_CATEGORIES.add("reload");
        VALID_CATEGORIES.add("optic");
        VALID_CATEGORIES.add("ammo");
        VALID_CATEGORIES.add("misc");
    }

    private final WebPageService webPageService;


    @Override
    public void setUp() throws CLIException {
    }

    @Override
    public void start() throws CLIException {

//        Observable.from(webPageService.getUnparsedByType("productPageRaw"))
//                .doOnNext(webPageEntity -> log.info("Starting RAW page parsing {}", webPageEntity))
//                .map(productParserFactory::parse)
//                .doOnNext(productEntity -> log.info("Parsed page {}", productEntity))
//                .publish()
//                .autoConnect(2);

    }


    @Override
    public void tearDown() throws CLIException {
    }
}
