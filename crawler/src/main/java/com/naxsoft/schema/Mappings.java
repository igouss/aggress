
package com.naxsoft.schema;

import javax.annotation.Generated;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

@Generated("org.jsonschema2pojo")
public class Mappings {

    @SerializedName("guns")
    @Expose
    private Guns guns;

    /**
     * 
     * @return
     *     The guns
     */
    public Guns getGuns() {
        return guns;
    }

    /**
     * 
     * @param guns
     *     The guns
     */
    public void setGuns(Guns guns) {
        this.guns = guns;
    }

}
