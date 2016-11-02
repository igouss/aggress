package com.naxsoft.parsingService;

import io.vertx.core.Vertx;
import io.vertx.ext.shell.ShellServiceOptions;
import io.vertx.ext.shell.command.CommandBuilder;
import io.vertx.ext.shell.command.CommandRegistry;
import io.vertx.ext.shell.term.HttpTermOptions;

public class ShellService {
    private final Vertx vertx;
    private io.vertx.ext.shell.ShellService service;

    public ShellService(Vertx vertx) {
        this.vertx = vertx;

        CommandBuilder builder = CommandBuilder.command("my-command");
        builder.processHandler(handler -> {
            handler.write("Hello World");
            handler.end();
        });

        CommandRegistry registry = CommandRegistry.getShared(vertx);
        registry.registerCommand(builder.build(vertx));
    }

    public void startHttpShellService() {
        if (service != null) {
            service.stop();
        }


        service = io.vertx.ext.shell.ShellService.create(vertx,
                new ShellServiceOptions().setHttpOptions(
                        new HttpTermOptions().
                                setHost("localhost").
                                setPort(9090)
                )
        );
        service.start();
    }

    public void stop() {
        if (service != null) {
            service.stop();
            service = null;
        }
    }
}
