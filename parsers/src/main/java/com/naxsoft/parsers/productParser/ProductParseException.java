package com.naxsoft.parsers.productParser;

class ProductParseException extends Exception {
    static final long serialVersionUID = 1L;

    ProductParseException(Exception e) {
        super(e);
    }
}
