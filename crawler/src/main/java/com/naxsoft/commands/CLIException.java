package com.naxsoft.commands;


class CLIException extends RuntimeException {

    /**
     * Creates a new instance of {@link CLIException}.
     *
     * @param message the message
     */
    public CLIException(String message) {
        super(message);
    }

    /**
     * Creates a new instance of {@link CLIException}.
     *
     * @param message the message
     * @param cause   the cause
     */
    public CLIException(String message, Exception cause) {
        super(message, cause);
    }
}
