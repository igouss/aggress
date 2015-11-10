package com.naxsoft.database;

import org.elasticsearch.action.admin.indices.delete.DeleteIndexResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.logging.ESLogger;
import org.elasticsearch.common.logging.Loggers;


public class IndexService {
    private static final long serialVersionUID = 1L;
    private static final ESLogger logger = Loggers.getLogger(IndexService.class);

    Client client;

    /**
     * Create an index
     * @param index
     * @param type
     * @param analyzer
     * @return
     * @throws RestAPIException
     */
    public Boolean createIndex(String index, String type, String analyzer) throws RestAPIException {
        if (logger.isDebugEnabled()) logger.debug("createIndex({}, {}, {})", index, type, analyzer);
        Boolean output = false;
        try {
            if (ESHelper.isTypeExist(client, index, type)) {
                throw new RestAPIException("Type already exists");
            }

            ESHelper.pushMapping(client, index, type, null);

            output = true;
        } catch (Exception e) {
            logger.error("Can not create Index({}, {}, {}) : {}", index, type, analyzer, e.getMessage());
        }
        if (logger.isDebugEnabled()) logger.debug("/createIndex({}, {}, {})={}", index, type, analyzer, output);
        return output;
    }

    /**
     * Delete an index
     * @param index
     */
    public void delete(String index) throws RestAPIException {
        if (logger.isDebugEnabled()) logger.debug("delete({})", index);
        try {
            DeleteIndexResponse dir = client.admin().indices().prepareDelete(index).execute().actionGet();
            if (!dir.isAcknowledged()) throw new RestAPIException("ES did not acknowledge index removal...");
        } catch (Exception e) {
            logger.error("Can not delete Index({}) : {}", index, e.getMessage());
            throw new RestAPIException("Error while removing index : " + e.getMessage());
        }
        if (logger.isDebugEnabled()) logger.debug("/delete({})", index);
    }
}
