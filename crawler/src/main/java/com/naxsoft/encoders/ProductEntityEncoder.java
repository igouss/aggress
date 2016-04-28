package com.naxsoft.encoders;


import com.naxsoft.entity.ProductEntity;

public class ProductEntityEncoder extends Encoder<ProductEntity> {
    /**
     * @param value
     * @return
     */
    public ProductEntity decode(String value) {
        return fromJson(value, ProductEntity.class);
    }
}
