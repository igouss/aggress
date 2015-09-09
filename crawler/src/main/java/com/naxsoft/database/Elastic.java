//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.naxsoft.database;

import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

public class Elastic {
    TransportClient client = null;
    private Logger logger;

    public void setup() {
        this.logger = LoggerFactory.getLogger(this.getClass());
        Settings settings = ImmutableSettings.settingsBuilder().put("cluster.name", "elasticsearch").put("client.transport.sniff", true).build();
        this.client = new TransportClient(settings);
        this.client.addTransportAddress(new InetSocketTransportAddress("localhost", 9300));

        while(true) {
            int connectedNodes = this.client.connectedNodes().size();
            if(0 != connectedNodes) {
                return;
            }

            this.logger.info("Waiting for elastic to connect to a node...");

            try {
                Thread.sleep(TimeUnit.SECONDS.toMillis(5L));
            } catch (InterruptedException e) {
                this.logger.error("Thread sleep failed", e);
            }
        }
    }

    public void tearDown() {
        this.client.close();
    }

    public Client getClient() {
        return this.client;
    }
}
