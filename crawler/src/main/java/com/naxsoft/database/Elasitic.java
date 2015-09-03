package com.naxsoft.database;

import com.naxsoft.parsers.productParser.ProductParserFactory;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 * Copyright NAXSoft 2015
 */
public class Elasitic {
    TransportClient client = null;
    private Logger logger;

    public void setup() {
        logger = LoggerFactory.getLogger(ProductParserFactory.class);
        Settings settings = ImmutableSettings.settingsBuilder()
                .put("cluster.name", "elasticsearch")
                .put("client.transport.sniff", true)
                .build();
        client = new TransportClient(settings);
        client.addTransportAddress(new InetSocketTransportAddress("localhost", 9300));
        while (true) {
            int conntectedNodes = client.connectedNodes().size();
            if (0 == conntectedNodes) {
                logger.info("Waiting for elastic to connect to a node...");
                try {
                    Thread.sleep(TimeUnit.SECONDS.toMillis(5));
                } catch (InterruptedException e) {
                    logger.error("Thread sleep failed", e);
                }
            } else {
                break;
            }
        }
    }

    public void tearDown() {
        client.close();
    }

    public Client getClient() {
        return client;
    }
}
