/*
 * Maintained by brightSPARK Labs.
 * www.brightsparklabs.com
 *
 * Refer to LICENSE at repository root for license details.
 */

package com.brightsparklabs.dropwizard.bundles.auth.external;

import io.dropwizard.auth.Authenticator;

/**
 * An exception thrown to indicate that an {@link Authenticator} has determined that the supplied
 * credentials are invalid.
 *
 * @author brightSPARK Labs
 */
public class AuthenticationDeniedException extends Exception {

    /**
     * Constructs a new authentication denied exception with the specified detail message.
     *
     * @param message the detail message.
     */
    public AuthenticationDeniedException(String message) {
        super(message);
    }

    /**
     * Constructs a new authentication denied exception with the specified detail message and cause.
     *
     * @param message the detail message.
     * @param cause the cause of the exception.
     */
    public AuthenticationDeniedException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new authentication denied exception with the specified cause.
     *
     * @param cause the cause of the exception.
     */
    public AuthenticationDeniedException(Throwable cause) {
        super(cause);
    }
}
