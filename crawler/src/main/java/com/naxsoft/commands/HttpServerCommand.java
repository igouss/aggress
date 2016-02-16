package com.naxsoft.commands;

import com.naxsoft.ExecutionContext;
import com.naxsoft.http.Server;

/**
 * Copyright NAXSoft 2015
 */
public class HttpServerCommand implements Command {
    Server server;

    @Override
    public void setUp(ExecutionContext context) throws CLIException {
        server = new Server();
    }

    @Override
    public void run() throws CLIException {
        try {
            server.start();
        } catch (Exception e) {
            throw new CLIException("Failed to start HTTP server", e);
        }
    }

    @Override
    public void tearDown() throws CLIException {
        try {
            server.stop();
        } catch (Exception e) {
            throw new CLIException("Failed to stop HTTP server", e);
        } finally {
            server = null;
        }
    }
}
