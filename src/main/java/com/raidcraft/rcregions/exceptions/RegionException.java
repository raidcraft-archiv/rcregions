package com.raidcraft.rcregions.exceptions;

/**
 * 02.01.12 - 16:37
 *
 * @author Silthus
 */
public class RegionException extends Throwable {

    /**
     * Constructs a new throwable with {@code null} as its detail message.
     * The cause is not initialized, and may subsequently be initialized by a
     * call to {@link #initCause}.
     * <p/>
     * <p>The {@link #fillInStackTrace()} method is called to initialize
     * the stack trace data in the newly created throwable.
     */
    public RegionException() {

        super("Es ist ein Regions Fehler aufgetreten. Bitte kontaktiere einen Admin deines Vertrauens.");
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
    public RegionException(String message) {

        super(message);
    }
}


