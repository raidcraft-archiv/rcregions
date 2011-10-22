package com.silthus.rcregions;

/**
 * 22.10.11 - 00:41
 *
 * @author Silthus
 */
public class UnknownRegionException extends Exception {

    /**
     * Constructs a new exception with <code>null</code> as its detail message.
     * The cause is not initialized, and may subsequently be initialized by a
     * call to {@link #initCause}.
     */
    public UnknownRegionException() {
        super("Unknown WorldGuard Region!");
    }

    /**
     * Constructs a new exception with the specified detail message.  The
     * cause is not initialized, and may subsequently be initialized by
     * a call to {@link #initCause}.
     *
     * @param message the detail message. The detail message is saved for
     *                later retrieval by the {@link #getMessage()} method.
     */
    public UnknownRegionException(String message) {
        super(message);
    }
}
