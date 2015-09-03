package com.naxsoft.entity;

import java.util.HashMap;

/**
 * Copyright NAXSoft 2015
 */
public class Product {
    HashMap<String, String> properties = new HashMap<>();
    private int id;

    public HashMap<String, String> getProperties() {
        return properties;
    }

    public void setPropertie(String key, String value) {
        properties.put(key, value);
    }


    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }
}
