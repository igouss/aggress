package com.naxsoft.commands;

import com.naxsoft.storage.elasticsearch.Elastic;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Create Elasticsearch index
 */
@Slf4j
@RequiredArgsConstructor
public class CreateESIndexCommand implements Command {
    private final Elastic elastic;

    @Override
    public void setUp() throws CLIException {
    }

    @Override
    public void start() throws CLIException {
        elastic.createIndex("product", "guns");
    }

    @Override
    public void tearDown() throws CLIException {
    }
}
