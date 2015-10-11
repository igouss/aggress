
package com.naxsoft.schema;

import javax.annotation.Generated;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

@Generated("org.jsonschema2pojo")
public class Schema {

    @SerializedName("product")
    @Expose
    private Product product;

    /**
     * 
     * @return
     *     The product
     */
    public Product getProduct() {
        return product;
    }

    /**
     * 
     * @param product
     *     The product
     */
    public void setProduct(Product product) {
        this.product = product;
    }

}
