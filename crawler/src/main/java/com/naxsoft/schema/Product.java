
package com.naxsoft.schema;

import javax.annotation.Generated;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

@Generated("org.jsonschema2pojo")
public class Product {

    @SerializedName("mappings")
    @Expose
    private Mappings mappings;

    /**
     * 
     * @return
     *     The mappings
     */
    public Mappings getMappings() {
        return mappings;
    }

    /**
     * 
     * @param mappings
     *     The mappings
     */
    public void setMappings(Mappings mappings) {
        this.mappings = mappings;
    }

}
