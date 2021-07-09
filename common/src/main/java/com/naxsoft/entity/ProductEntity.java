package com.naxsoft.entity;

import lombok.Value;

import java.util.Map;

@Value
public class ProductEntity {
    String productName;
    String url;
    String regularPrice;
    String specialPrice;
    String productImage;
    String description;
    Map<String, String> attr;
    String[] category;
}
