package com.brightsparklabs.dropwizard.bundles.auth.external;

import io.dropwizard.auth.Authenticator;

/**
 * An exception thrown to indicate that an {@link Authenticator} has determined that the supplied credentials are invalid.
 *
 * @author brightSPARK Labs
 */
public class AuthenticationDeniedException extends Exception {

    public AuthenticationDeniedException(String message) {
        super(message);
    }

    public AuthenticationDeniedException(String message, Throwable cause) {
        super(message, cause);
    }

    public AuthenticationDeniedException(Throwable cause) {
        super(cause);
    }
}
