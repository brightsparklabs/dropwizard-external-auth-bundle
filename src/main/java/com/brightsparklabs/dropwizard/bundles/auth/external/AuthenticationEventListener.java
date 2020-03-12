/*
 * Created by brightSPARK Labs
 * www.brightsparklabs.com
 */

package com.brightsparklabs.dropwizard.bundles.auth.external;

/**
 * Interface for listeners to authentication events
 *
 * @author brightSPARK Labs
 */
public interface AuthenticationEventListener {

    /**
     * Handler function for handling authentication success
     */
    void handleAuthenticationSuccess();

    /**
     * Handler function for handling authentication failure - denied access
     */
    void handleAuthenticationDenied();

    /**
     * Handler function for handling authentication failure - invalid authentication request
     */
    void handleAuthenticationInvalid();

}
