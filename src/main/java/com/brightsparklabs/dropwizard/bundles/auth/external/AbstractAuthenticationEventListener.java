/*
 * Created by brightSPARK Labs
 * www.brightsparklabs.com
 */

package com.brightsparklabs.dropwizard.bundles.auth.external;

import io.dropwizard.auth.AuthenticationException;


/**
 * Convenience class for implementing a {@link AuthenticationEventListener}.
 *
 * All implemented methods are empty, so they can be optionally overridden.
 */
public abstract class AbstractAuthenticationEventListener implements AuthenticationEventListener {
    @Override
    public void onAuthenticationSuccess(InternalUser authenticatedUser) {
        // Do nothing by default
    }

    @Override
    public void onAuthenticationDenied(AuthenticationDeniedException authDeniedException) {
        // Do nothing by default
    }

    @Override
    public void onAuthenticationError(AuthenticationException authException) {
        // Do nothing by default
    }
}
