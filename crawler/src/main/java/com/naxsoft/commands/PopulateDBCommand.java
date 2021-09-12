package com.naxsoft.commands;


import com.naxsoft.parsingService.WebPageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Add initial dataset to the database.
 * The crawling is basically breath first search from that dataset
 */
@Slf4j
@RequiredArgsConstructor
public class PopulateDBCommand implements Command {
    private final WebPageService webPageService;

    @Override
    public void setUp() {
    }

    @Override
    public void start() throws CLIException {
    }

    @Override
    public void tearDown() throws CLIException {
    }
}
