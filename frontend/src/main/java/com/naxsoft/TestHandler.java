package com.naxsoft;

import ratpack.handling.Context;
import ratpack.handling.Handler;

/**
 * Copyright NAXSoft 2015
 */
public class TestHandler implements Handler {

    public TestHandler() {
        System.out.println("TestHandler");
    }

    @Override
    public void handle(Context ctx) throws Exception {
//        params.q
             ctx.getResponse().send("Hello again");
    }
}
