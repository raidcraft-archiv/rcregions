package com.raidcraft.rcregions.exceptions;

/**
 * 21.01.12 - 10:29
 *
 * @author Silthus
 */
public class UnknownDistrictException extends RegionException {

    /**
     * Constructs a new throwable with {@code null} as its detail message.
     * The cause is not initialized, and may subsequently be initialized by a
     * call to {@link #initCause}.
     * <p/>
     * <p>The {@link #fillInStackTrace()} method is called to initialize
     * the stack trace data in the newly created throwable.
     */
    public UnknownDistrictException() {

        super("The district you are looking for is not registered. Please check your config...");
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
    public UnknownDistrictException(String message) {

        super(message);
    }
}
