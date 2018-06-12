package com.naxsoft.parsers.productParser;

class ProductParseException extends Exception {
    private static final long serialVersionUID = 1;

    ProductParseException(Exception e) {
        super(e);
    }
}
