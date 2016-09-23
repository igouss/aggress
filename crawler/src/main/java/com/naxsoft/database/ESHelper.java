package com.naxsoft.database;

import org.elasticsearch.action.admin.indices.create.CreateIndexRequestBuilder;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.admin.indices.mapping.get.GetMappingsResponse;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingRequestBuilder;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Not used now. For later use.
 */
public class ESHelper {
    private static final Logger LOGGER = LoggerFactory.getLogger(ESHelper.class);

    /**
     * Define a type for a given index and if exists with its mapping definition (loaded in classloader)
     *
     * @param client   Elasticsearch client
     * @param index    Index name
     * @param type     Type name
     * @param xcontent If you give an xcontent, it will be used to define the mapping
     * @throws Exception
     */
    public static void pushMapping(Client client, String index, String type, XContentBuilder xcontent) throws Exception {
        if (LOGGER.isTraceEnabled()) LOGGER.trace("pushMapping(" + index + "," + type + ")");

        // If type does not exist, we create it
        boolean mappingExist = isMappingExist(client, index, type);
        if (!mappingExist) {
            if (LOGGER.isDebugEnabled())
                LOGGER.debug("Mapping [" + index + "]/[" + type + "] doesn't exist. Creating it.");

            String source = null;

            // Read the mapping json file if exists and use it
            if (null == xcontent) source = readJsonDefinition(type);

            if (null != source || null != xcontent) {
                PutMappingRequestBuilder pmrb = client.admin().indices()
                        .preparePutMapping(index)
                        .setType(type);

                if (null != source) {
                    if (LOGGER.isTraceEnabled()) LOGGER.trace("Mapping for [" + index + "]/[" + type + "]=" + source);
                    pmrb.setSource(source);
                }

                if (null != xcontent) {
                    if (LOGGER.isTraceEnabled())
                        LOGGER.trace("Mapping for [" + index + "]/[" + type + "]=" + xcontent.string());
                    pmrb.setSource(xcontent);
                }

                // Create type and mapping
                PutMappingResponse response = pmrb.execute().actionGet();
                if (!response.isAcknowledged()) {
                    throw new Exception("Could not define mapping for type [" + index + "]/[" + type + "].");
                } else {
                    if (LOGGER.isDebugEnabled())
                        LOGGER.debug("Mapping definition for [" + index + "]/[" + type + "] succesfully created.");
                }
            } else {
                if (LOGGER.isDebugEnabled())
                    LOGGER.debug("No mapping definition for [" + index + "]/[" + type + "]. Ignoring.");
            }
        } else {
            if (LOGGER.isDebugEnabled()) LOGGER.debug("Mapping [" + index + "]/[" + type + "] already exists.");
        }
        if (LOGGER.isTraceEnabled()) LOGGER.trace("/pushMapping(" + index + "," + type + ")");
    }

    /**
     * Check if a mapping (aka a type) already exists in an index
     *
     * @param client Elasticsearch client
     * @param index  Index name
     * @param type   Mapping name
     * @return true if mapping exists
     */
    public static boolean isMappingExist(Client client, String index, String type) {
        GetMappingsResponse mappingsResponse = client.admin().indices().prepareGetMappings(index).setTypes(type).get();
        return null != mappingsResponse.getMappings().get(index) && mappingsResponse.getMappings().get(index).containsKey(type);
    }

    /**
     * Create a default index with our default settings (shortcut to {@link #createIndexIfNeeded(Client, String, String, String)})
     *
     * @param client Elasticsearch client
     */
    public static void createIndexIfNeeded(Client client) {
        createIndexIfNeeded(client, null, null, null);
    }

    /**
     * Create an index with our default settings
     *
     * @param client   Elasticsearch client
     * @param index    Index name : default to SMDSearchProperties.INDEX_NAME
     * @param type     Type name : SMDSearchProperties.INDEX_TYPE_DOC
     * @param analyzer Analyzer to apply : default to "default"
     */
    public static void createIndexIfNeeded(Client client, String index, String type, String analyzer) {
        if (LOGGER.isDebugEnabled()) LOGGER.debug("createIndexIfNeeded({}, {}, {})", index, type, analyzer);

        try {
            // We check first if index already exists
            if (!isIndexExist(client, index)) {
                if (LOGGER.isDebugEnabled()) LOGGER.debug("Index {} doesn't exist. Creating it.", index);

                CreateIndexRequestBuilder cirb = client.admin().indices().prepareCreate(index);

                String source = readJsonDefinition("_settings");
                if (null != source) {
                    if (LOGGER.isTraceEnabled()) LOGGER.trace("Mapping for [{}]={}", index, source);
                    cirb.setSettings(source);
                }

                CreateIndexResponse createIndexResponse = cirb.execute().actionGet();
                if (!createIndexResponse.isAcknowledged())
                    throw new Exception("Could not create index [" + index + "].");
            }
        } catch (Exception e) {
            LOGGER.warn("createIndexIfNeeded() : Exception raised : {}", e.getClass());
            if (LOGGER.isDebugEnabled()) LOGGER.debug("- Exception stacktrace :", e);
        }

        if (LOGGER.isDebugEnabled()) LOGGER.debug("/createIndexIfNeeded()");
    }

    /**
     * Create an index without pushing the mapping
     *
     * @param client    Elasticsearch client
     * @param indexName Index name
     */
    public static void createIndexIfNeededNoMapping(Client client, String indexName) {
        if (LOGGER.isDebugEnabled()) LOGGER.debug("createIndexIfNeeded({})", indexName);

        try {
            // We check first if index already exists
            if (!isIndexExist(client, indexName)) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Index {} doesn't exist. Creating it.", indexName);
                }

                CreateIndexRequestBuilder cirb = client.admin().indices().prepareCreate(indexName);
                CreateIndexResponse createIndexResponse = cirb.execute().actionGet();
                if (!createIndexResponse.isAcknowledged())
                    throw new Exception("Could not create index [" + indexName + "].");
            }

        } catch (Exception e) {
            LOGGER.warn("createIndexIfNeeded() : Exception raised : {}", e.getClass());
            if (LOGGER.isDebugEnabled()) LOGGER.debug("- Exception stacktrace :", e);
        }

        if (LOGGER.isDebugEnabled()) LOGGER.debug("/createIndexIfNeededNoMapping({})");
    }

    /**
     * Check if an index already exists
     *
     * @param client Elasticsearch client
     * @param index  Index name
     * @return true if index already exists
     * @throws Exception
     */
    public static boolean isIndexExist(Client client, String index) throws Exception {
        return client.admin().indices().prepareExists(index).execute().actionGet().isExists();
    }

    /**
     * Check if a type already exists
     *
     * @param client Elasticsearch client
     * @param index  Index name
     * @param type   Type name
     * @return true if index already exists
     * @throws Exception
     */
    public static boolean isTypeExist(Client client, String index, String type) throws Exception {
        return client.admin().indices().prepareExists(index, type).execute().actionGet().isExists();
    }

    /**
     * Read the mapping for a type.<br>
     * Shortcut to readFileInClasspath("/estemplate/" + type + ".json");
     *
     * @param type Type name
     * @return Mapping if exists. Null otherwise.
     * @throws Exception
     */
    private static String readJsonDefinition(String type) throws Exception {
        return readFileInClasspath("/estemplate/" + type + ".json");
    }

    /**
     * Read a file in classpath and return its content
     *
     * @param url File URL Example : /es/twitter/_settings.json
     * @return File content or null if file doesn't exist
     * @throws Exception
     */
    public static String readFileInClasspath(String url) throws Exception {
        StringBuilder bufferJSON = new StringBuilder();

        try {
            InputStream ips = ESHelper.class.getResourceAsStream(url);
            InputStreamReader ipsr = new InputStreamReader(ips);
            BufferedReader br = new BufferedReader(ipsr);
            String line;

            while (null != (line = br.readLine())) {
                bufferJSON.append(line);
            }
            br.close();
        } catch (Exception ignore) {
            return null;
        }

        return bufferJSON.toString();
    }
}
