/*
 * Created by brightSPARK Labs
 * www.brightsparklabs.com
 */

package com.brightsparklabs.dropwizard.bundles.auth.external;

import io.dropwizard.auth.AuthenticationException;

/**
 * Interface for listeners to authentication events
 *
 * @author brightSPARK Labs
 */
public interface AuthenticationEventListener {

    /**
     * Handler function for handling authentication success
     *
     * @param authenticatedUser the successfully-authenticated user
     */
    void onAuthenticationSuccess(InternalUser authenticatedUser);

    /**
     * Handler function for handling authentication failure - denied access
     */
    void onAuthenticationDenied(AuthenticationDeniedException authDeniedException);

    /**
     * Handler function for handling authentication failure - invalid authentication request
     *
     * @param authException the authentication exception that was thrown
     */
    void onAuthenticationError(AuthenticationException authException);

}
