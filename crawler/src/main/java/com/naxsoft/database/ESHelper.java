package com.naxsoft.database;

import org.elasticsearch.action.admin.indices.create.CreateIndexRequestBuilder;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.admin.indices.mapping.get.GetMappingsResponse;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingRequestBuilder;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.logging.ESLogger;
import org.elasticsearch.common.logging.ESLoggerFactory;
import org.elasticsearch.common.xcontent.XContentBuilder;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;


public class ESHelper {
    private static ESLogger logger = ESLoggerFactory.getLogger(ESHelper.class.getName());

    /**
     * Define a type for a given index and if exists with its mapping definition (loaded in classloader)
     * @param client Elasticsearch client
     * @param index Index name
     * @param type Type name
     * @param xcontent If you give an xcontent, it will be used to define the mapping
     * @throws Exception
     */
    public static void pushMapping(Client client, String index, String type, XContentBuilder xcontent) throws Exception {
        if (logger.isTraceEnabled()) logger.trace("pushMapping("+index+","+type+")");

        // If type does not exist, we create it
        boolean mappingExist = isMappingExist(client, index, type);
        if (!mappingExist) {
            if (logger.isDebugEnabled()) logger.debug("Mapping ["+index+"]/["+type+"] doesn't exist. Creating it.");

            String source = null;

            // Read the mapping json file if exists and use it
            if (xcontent == null) source = readJsonDefinition(type);

            if (source != null || xcontent != null) {
                PutMappingRequestBuilder pmrb = client.admin().indices()
                        .preparePutMapping(index)
                        .setType(type);

                if (source != null) {
                    if (logger.isTraceEnabled()) logger.trace("Mapping for ["+index+"]/["+type+"]="+source);
                    pmrb.setSource(source);
                }

                if (xcontent != null) {
                    if (logger.isTraceEnabled()) logger.trace("Mapping for ["+index+"]/["+type+"]="+xcontent.string());
                    pmrb.setSource(xcontent);
                }

                // Create type and mapping
                PutMappingResponse response = pmrb.execute().actionGet();
                if (!response.isAcknowledged()) {
                    throw new Exception("Could not define mapping for type ["+index+"]/["+type+"].");
                } else {
                    if (logger.isDebugEnabled()) logger.debug("Mapping definition for ["+index+"]/["+type+"] succesfully created.");
                }
            } else {
                if (logger.isDebugEnabled()) logger.debug("No mapping definition for ["+index+"]/["+type+"]. Ignoring.");
            }
        } else {
            if (logger.isDebugEnabled()) logger.debug("Mapping ["+index+"]/["+type+"] already exists.");
        }
        if (logger.isTraceEnabled()) logger.trace("/pushMapping("+index+","+type+")");
    }

    /**
     * Check if a mapping (aka a type) already exists in an index
     * @param client Elasticsearch client
     * @param index Index name
     * @param type Mapping name
     * @return true if mapping exists
     */
    public static boolean isMappingExist(Client client, String index, String type) {
        GetMappingsResponse mappingsResponse = client.admin().indices().prepareGetMappings(index).setTypes(type).get();
        if (mappingsResponse.getMappings().get(index) == null) {
            return false;
        }
        return mappingsResponse.getMappings().get(index).containsKey(type);
    }

    /**
     * Create a default index with our default settings (shortcut to {@link #createIndexIfNeeded(Client, String, String, String)})
     * @param client Elasticsearch client
     */
    public static void createIndexIfNeeded(Client client) {
        createIndexIfNeeded(client, null, null, null);
    }

    /**
     * Create an index with our default settings
     * @param client Elasticsearch client
     * @param index Index name : default to SMDSearchProperties.INDEX_NAME
     * @param type Type name : SMDSearchProperties.INDEX_TYPE_DOC
     * @param analyzer Analyzer to apply : default to "default"
     */
    public static void createIndexIfNeeded(Client client, String index, String type, String analyzer) {
        if (logger.isDebugEnabled()) logger.debug("createIndexIfNeeded({}, {}, {})", index, type, analyzer);

        String indexName = index;
        String typeName = type;
        String analyzerName = analyzer == null ? "default" : analyzer;

        try {
            // We check first if index already exists
            if (!isIndexExist(client, indexName)) {
                if (logger.isDebugEnabled()) logger.debug("Index {} doesn't exist. Creating it.", indexName);

                CreateIndexRequestBuilder cirb = client.admin().indices().prepareCreate(indexName);

                String source = readJsonDefinition("_settings");
                if (source !=  null) {
                    if (logger.isTraceEnabled()) logger.trace("Mapping for [{}]={}", indexName, source);
                    cirb.setSettings(source);
                }

                CreateIndexResponse createIndexResponse = cirb.execute().actionGet();
                if (!createIndexResponse.isAcknowledged()) throw new Exception("Could not create index ["+indexName+"].");
            }
        } catch (Exception e) {
            logger.warn("createIndexIfNeeded() : Exception raised : {}", e.getClass());
            if (logger.isDebugEnabled()) logger.debug("- Exception stacktrace :", e);
        }

        if (logger.isDebugEnabled()) logger.debug("/createIndexIfNeeded()");
    }

    /**
     * Create an index without pushing the mapping
     * @param client Elasticsearch client
     * @param indexName Index name
     *
     */
    public static void createIndexIfNeededNoMapping(Client client, String indexName) {
        if (logger.isDebugEnabled()) logger.debug("createIndexIfNeeded({})", indexName);

        try {
            // We check first if index already exists
            if (!isIndexExist(client, indexName)) {
                if (logger.isDebugEnabled()) logger.debug("Index {} doesn't exist. Creating it.", indexName);

                CreateIndexRequestBuilder cirb = client.admin().indices().prepareCreate(indexName);
                CreateIndexResponse createIndexResponse = cirb.execute().actionGet();
                if (!createIndexResponse.isAcknowledged()) throw new Exception("Could not create index ["+indexName+"].");
            }

        } catch (Exception e) {
            logger.warn("createIndexIfNeeded() : Exception raised : {}", e.getClass());
            if (logger.isDebugEnabled()) logger.debug("- Exception stacktrace :", e);
        }

        if (logger.isDebugEnabled()) logger.debug("/createIndexIfNeededNoMapping({})");
    }

    /**
     * Check if an index already exists
     * @param client Elasticsearch client
     * @param index Index name
     * @return true if index already exists
     * @throws Exception
     */
    public static boolean isIndexExist(Client client, String index) throws Exception {
        return client.admin().indices().prepareExists(index).execute().actionGet().isExists();
    }

    /**
     * Check if a type already exists
     * @param client Elasticsearch client
     * @param index Index name
     * @param type Type name
     * @return true if index already exists
     * @throws Exception
     */
    public static boolean isTypeExist(Client client, String index, String type) throws Exception {
        return client.admin().indices().prepareExists(index, type).execute().actionGet().isExists();
    }

    /**
     * Read the mapping for a type.<br>
     * Shortcut to readFileInClasspath("/estemplate/" + type + ".json");
     * @param type Type name
     * @return Mapping if exists. Null otherwise.
     * @throws Exception
     */
    private static String readJsonDefinition(String type) throws Exception {
        return readFileInClasspath("/estemplate/" + type + ".json");
    }

    /**
     * Read a file in classpath and return its content
     * @param url File URL Example : /es/twitter/_settings.json
     * @return File content or null if file doesn't exist
     * @throws Exception
     */
    public static String readFileInClasspath(String url) throws Exception {
        StringBuffer bufferJSON = new StringBuffer();

        try {
            InputStream ips= ESHelper.class.getResourceAsStream(url);
            InputStreamReader ipsr = new InputStreamReader(ips);
            BufferedReader br = new BufferedReader(ipsr);
            String line;

            while ((line=br.readLine())!=null){
                bufferJSON.append(line);
            }
            br.close();
        } catch (Exception e){
            return null;
        }

        return bufferJSON.toString();
    }
}
