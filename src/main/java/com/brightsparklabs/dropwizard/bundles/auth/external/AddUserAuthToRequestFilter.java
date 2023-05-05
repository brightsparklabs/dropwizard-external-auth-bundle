/*
 * Maintained by brightSPARK Labs.
 * www.brightsparklabs.com
 *
 * Refer to LICENSE at repository root for license details.
 */

package com.brightsparklabs.dropwizard.bundles.auth.external;

import static java.util.Objects.requireNonNull;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;

import org.eclipse.jetty.security.DefaultUserIdentity;
import org.eclipse.jetty.security.UserAuthentication;
import org.eclipse.jetty.server.HttpConnection;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.UserIdentity;
import org.slf4j.MDC;

import java.io.IOException;
import java.security.Principal;
import java.util.Optional;

/**
 * A {@link ContainerRequestFilter} that adds the authenticated user's username to the request
 * authentication.
 *
 * <p>By adding the authenticated user's username to the request's authentication, it can be logged
 * as part of auditing.
 *
 * @param <P> the type of principal retrieved from authentication
 * @author brightSPARK Labs
 */
public class AddUserAuthToRequestFilter<P extends Principal>
        implements ContainerRequestFilter, ContainerResponseFilter {

    // -------------------------------------------------------------------------
    // CLASS VARIABLES
    // -------------------------------------------------------------------------

    /** The principal converter to convert from Principal P to the InternalUser */
    private final PrincipalConverter<P> principalConverter;

    /** MDC key to store the authenticated user's username against. */
    private final String mdcUsernameField;

    // -------------------------------------------------------------------------
    // PUBLIC METHODS
    // -------------------------------------------------------------------------

    /**
     * Default constructor.
     *
     * @param principalConverter Converter between {@link InternalUser} and the {@link Principal}
     *     used in the system.
     * @param mdcUsernameField MDC key to store the authenticated user's username against.
     */
    public AddUserAuthToRequestFilter(
            final PrincipalConverter<P> principalConverter, final String mdcUsernameField) {
        this.principalConverter = requireNonNull(principalConverter);
        this.mdcUsernameField = mdcUsernameField;
    }

    // -------------------------------------------------------------------------
    // IMPLEMENTATION: ContainerRequestFilter
    // -------------------------------------------------------------------------

    @Override
    public void filter(final ContainerRequestContext ctx) throws IOException {
        final Optional<Request> request =
                Optional.ofNullable(
                        HttpConnection.getCurrentConnection().getHttpChannel().getRequest());
        if (!request.isPresent()) {
            return;
        }

        // Get the authorised user principal
        final Optional<Principal> userPrincipal =
                Optional.ofNullable(ctx.getSecurityContext().getUserPrincipal());
        if (!userPrincipal.isPresent()) {
            return;
        }

        final Optional<InternalUser> maybeInternalUser =
                this.principalConverter.convertToInternalUser((P) userPrincipal.get());
        if (!maybeInternalUser.isPresent()) {
            return;
        }

        // Extract the username and set as the user identity in the request
        final InternalUser internalUser = maybeInternalUser.get();
        // Using lambda to provide implementation of the Principal functional interface
        final Principal principal = internalUser::getUsername;
        final UserIdentity userId = new DefaultUserIdentity(null, principal, null);
        request.get().setAuthentication(new UserAuthentication(null, userId));
        MDC.put(mdcUsernameField, principal.getName());
    }

    // -------------------------------------------------------------------------
    // IMPLEMENTATION: ContainerResponseFilter
    // -------------------------------------------------------------------------

    @Override
    public void filter(
            final ContainerRequestContext requestContext,
            final ContainerResponseContext responseContext)
            throws IOException {
        MDC.remove(mdcUsernameField);
    }
}
