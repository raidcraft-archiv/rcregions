package com.raidcraft.rcregions.exceptions;

/**
 * 21.01.12 - 09:48
 *
 * @author Silthus
 */
public class UnconfiguredConfigException extends Throwable {


    /**
     * Constructs a new throwable with {@code null} as its detail message.
     * The cause is not initialized, and may subsequently be initialized by a
     * call to {@link #initCause}.
     * <p/>
     * <p>The {@link #fillInStackTrace()} method is called to initialize
     * the stack trace data in the newly created throwable.
     */
    public UnconfiguredConfigException() {

        super("There seems to be a problem with your config. Please check...");
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
    public UnconfiguredConfigException(String message) {

        super(message);
    }
}

