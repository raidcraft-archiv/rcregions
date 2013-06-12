package com.raidcraft.rcregions.exceptions;

import de.raidcraft.api.RaidCraftException;

/**
 * 31.12.11 - 11:25
 *
 * @author Silthus
 */
public class WrongSignFormatException extends RaidCraftException {

    /**
     * Constructs a new throwable with {@code null} as its detail message.
     * The cause is not initialized, and may subsequently be initialized by a
     * call to {@link #initCause}.
     * <p/>
     * <p>The {@link #fillInStackTrace()} method is called to initialize
     * the stack trace data in the newly created throwable.
     */
    public WrongSignFormatException() {

        super("Falsche Schild Formatierung!");
    }

    /**
     * Constructs a new throwable with the specified detail message.  The
     * cause is not initialized, and may subsequently be initialized by
     * a call to {@link #initCause}.
     * <p/>
     * <p>The {@link #fillInStackTrace()} method is called to initialize
     * the stack trace data in the newly created throwable.
     *
     * @param message the detail message. The detail message is saved for
     *                later retrieval by the {@link #getMessage()} method.
     */
    public WrongSignFormatException(String message) {

        super(message);
    }
}
