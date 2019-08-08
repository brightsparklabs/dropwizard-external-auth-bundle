/*
 * Created by brightSPARK Labs
 * www.brightsparklabs.com
 */

package com.brightsparklabs.dropwizard.bundles.auth.external;

import io.dropwizard.auth.AuthFilter;
import io.dropwizard.auth.Authenticator;

import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.SecurityContext;
import java.io.IOException;
import java.security.Principal;

/**
 * An {@link AuthFilter} which supplies all the headers from the HTTP request to an {@link
 * Authenticator} for it to determine if the user should be considered authenticated.
 *
 * @author brightSPARK Labs
 */
@Priority(Priorities.AUTHENTICATION)
public class HeaderFieldsAuthFilter<P extends Principal>
        extends AuthFilter<MultivaluedMap<String, String>, P>
{
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

    /**
     * Hidden constructor, use {@link Builder} instead.
     */
    private HeaderFieldsAuthFilter() { }

    // -------------------------------------------------------------------------
    // IMPLEMENTATION: AuthFiler
    // -------------------------------------------------------------------------

    @Override
    public void filter(final ContainerRequestContext requestContext) throws IOException
    {
        final MultivaluedMap<String, String> headers = requestContext.getHeaders();

        if (!authenticate(requestContext, headers, SecurityContext.BASIC_AUTH))
        {
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
     * Builder for {@link HeaderFieldsAuthFilter}.
     * <p>An {@link Authenticator} must be provided during the building process.</p>
     *
     * @param <P>
     *         the type of the principal
     */
    public static class Builder<P extends Principal>
            extends AuthFilterBuilder<MultivaluedMap<String, String>, P, HeaderFieldsAuthFilter<P>>
    {
        @Override
        protected HeaderFieldsAuthFilter<P> newInstance()
        {
            return new HeaderFieldsAuthFilter<>();
        }
    }
}
