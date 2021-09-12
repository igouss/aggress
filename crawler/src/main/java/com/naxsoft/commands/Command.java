package com.naxsoft.commands;

interface Command {
    /**
     * Set up the command execution environment.
     */
    void setUp() throws CLIException;

    /**
     * Executes the command.
     *
     * @throws CLIException If anything went wrong.
     */
    void start() throws CLIException;

    /**
     * The command has been executed. Use this method to cleanup the environment.
     *
     * @throws CLIException if anything went wrong
     */
    void tearDown() throws CLIException;
}
