package com.naxsoft.handlers;

import org.elasticsearch.client.transport.TransportClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Copyright NAXSoft 2015
 */
public class SearchVerboseHandler extends AbstractSearchHandler {
    private static final Logger logger = LoggerFactory.getLogger(SearchHandler.class);

    public SearchVerboseHandler(TransportClient client) {
        super(client);
    }
}

