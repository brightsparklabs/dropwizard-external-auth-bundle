/*
 * Maintained by brightSPARK Labs.
 * www.brightsparklabs.com
 *
 * Refer to LICENSE at repository root for license details.
 */

package com.brightsparklabs.dropwizard.bundles.auth.external;

import io.dropwizard.auth.AuthFilter;
import io.dropwizard.auth.Authenticator;
import jakarta.annotation.Priority;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.SecurityContext;
import java.io.IOException;
import java.security.Principal;

/**
 * An {@link AuthFilter} which always returns a static {@link Principal}. Should only be used for
 * development.
 *
 * @author brightSPARK Labs
 */
@Priority(Priorities.AUTHENTICATION)
public class DevAuthFilter<P extends Principal> extends AuthFilter<String, P> {
    // -------------------------------------------------------------------------
    // CONSTANTS
    // -------------------------------------------------------------------------

    // -------------------------------------------------------------------------
    // CLASS VARIABLES
    // -------------------------------------------------------------------------

    // -------------------------------------------------------------------------
    // INSTANCE VARIABLES
    // -------------------------------------------------------------------------

    // -------------------------------------------------------------------------
    // CONSTRUCTION
    // -------------------------------------------------------------------------

    /** Hidden constructor, use {@link Builder} instead. */
    private DevAuthFilter() {}

    // -------------------------------------------------------------------------
    // IMPLEMENTATION: AuthFiler
    // -------------------------------------------------------------------------

    @Override
    public void filter(final ContainerRequestContext requestContext) throws IOException {
        if (!authenticate(requestContext, "dev", SecurityContext.BASIC_AUTH)) {
            throw new WebApplicationException(unauthorizedHandler.buildResponse(prefix, realm));
        }
    }

    // -------------------------------------------------------------------------
    // PUBLIC METHODS
    // -------------------------------------------------------------------------

    // -------------------------------------------------------------------------
    // PRIVATE METHODS
    // -------------------------------------------------------------------------

    // -------------------------------------------------------------------------
    // INNER CLASSES
    // -------------------------------------------------------------------------

    /**
     * Builder for {@link DevAuthFilter}.
     *
     * <p>An {@link Authenticator} must be provided during the building process.
     *
     * @param <P> the type of the principal
     */
    public static class Builder<P extends Principal>
            extends AuthFilterBuilder<String, P, DevAuthFilter<P>> {
        @Override
        protected DevAuthFilter<P> newInstance() {
            return new DevAuthFilter<>();
        }
    }
}
