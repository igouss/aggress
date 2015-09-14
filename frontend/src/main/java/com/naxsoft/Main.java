package com.naxsoft;


import ratpack.func.Action;
import ratpack.groovy.Groovy;
import ratpack.handling.Chain;
import ratpack.handling.Context;
import ratpack.handling.Handler;
import ratpack.server.RatpackServer;
import ratpack.server.RatpackServerSpec;

import java.io.File;
import java.nio.file.FileSystems;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import static ratpack.groovy.Groovy.*;

/**
 * Copyright NAXSoft 2015
 */
public class Main {
    public static void main(String... args) throws Exception {
        RatpackServer.start(Groovy.Script.app());


//        RatpackServer server = RatpackServer.of(spec -> {spec.
//                serverConfig(c -> {
//                    c.baseDir(new File("H:\\projects\\sprut\\frontend\\basedir"));
//                    c.port(8080);
//                }).
//                handlers(chain -> {
//                    chain.
//                            get("", ctx -> {
//                                ctx.render("Hello world");
//                            }).
//                            get(":name", ctx1 -> {
//                            });
//                });
//        });
//
//        server.start();

//        RatpackServer.start(ratpackServerSpec -> ratpackServerSpec.handlers(chain -> chain
//                        .get("", ctx -> {
//                            Map<String, ?> model = new HashMap<>();
//                            String id = "";
//                            ctx.render(groovyMarkupTemplate(model, id));
//                        }).get("", ctx -> {
//
//                        }))
//        );
    }
}

