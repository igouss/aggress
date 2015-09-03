package com.naxsoft.database;

/**
 * Copyright NAXSoft 2015
 */
public class DatabaseException extends RuntimeException {
    public DatabaseException(String s, Exception e) {
        super(s, e);
    }
}
