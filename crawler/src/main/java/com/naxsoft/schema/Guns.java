package com.naxsoft.schema;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import javax.annotation.Generated;

@Generated("org.jsonschema2pojo")
public class Guns {

    @SerializedName("properties")
    @Expose
    private Properties properties;

    /**
     * @return The properties
     */
    public Properties getProperties() {
        return properties;
    }

    /**
     * @param properties The properties
     */
    public void setProperties(Properties properties) {
        this.properties = properties;
    }

}
