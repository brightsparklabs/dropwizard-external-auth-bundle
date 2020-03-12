/*
 * Created by brightSPARK Labs
 * www.brightsparklabs.com
 */

package com.brightsparklabs.dropwizard.bundles.auth.external;

import io.dropwizard.auth.AuthenticationException;

/**
 * Abstract class for listeners to authentication events.
 *
 * @author brightSPARK Labs
 */
public abstract class AuthenticationEventListener {

    /**
     * Listener function called on authentication success
     *
     * @param authenticatedUser the successfully-authenticated user
     */
    void onAuthenticationSuccess(InternalUser authenticatedUser){}

    /**
     * Listener function called on authentication failure - denied access
     */
    void onAuthenticationDenied(AuthenticationDeniedException authDeniedException){}

    /**
     * Listener function called on authentication failure - invalid authentication request
     *
     * @param authException the authentication exception that was thrown
     */
    void onAuthenticationError(AuthenticationException authException){}

}
