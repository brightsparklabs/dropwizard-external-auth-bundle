/*
 * Created by brightSPARK Labs in 2020.
 * www.brightsparklabs.com
 *
 * Refer to LICENSE at repository root for license details.
 */

package com.brightsparklabs.dropwizard.bundles.auth.external;

import io.dropwizard.auth.AuthenticationException;

/**
 * Interface for listeners to authentication events.
 *
 * @author brightSPARK Labs
 */
public interface AuthenticationEventListener {
    // TODO: Clean interface with abstract impl 'AbstractAuthenticationEventListener'

    /**
     * Listener function called on authentication success
     *
     * @param authenticatedUser the successfully-authenticated user
     */
    default void onAuthenticationSuccess(InternalUser authenticatedUser) {
        // Do nothing by default
    }

    /** Listener function called on authentication failure - denied access */
    default void onAuthenticationDenied(AuthenticationDeniedException authDeniedException) {
        // Do nothing by default
    }

    /**
     * Listener function called on authentication failure - invalid authentication request
     *
     * @param authException the authentication exception that was thrown
     */
    default void onAuthenticationError(AuthenticationException authException) {
        // Do nothing by default
    }
}
