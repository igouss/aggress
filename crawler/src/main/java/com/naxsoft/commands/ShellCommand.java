package com.naxsoft.commands;

import com.naxsoft.parsingService.ShellService;

public class ShellCommand implements Command {
    private final ShellService shellService;

    public ShellCommand(ShellService shellService) {
        this.shellService = shellService;
    }

    @Override
    public void setUp() throws CLIException {

    }

    @Override
    public void start() throws CLIException {
        shellService.startHttpShellService();
    }

    @Override
    public void tearDown() throws CLIException {
        shellService.stop();
    }
}
