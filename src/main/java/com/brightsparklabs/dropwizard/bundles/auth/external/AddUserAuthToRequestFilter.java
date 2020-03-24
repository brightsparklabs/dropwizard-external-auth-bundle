/*
 * Created by brightSPARK Labs in 2020.
 * www.brightsparklabs.com
 *
 * Refer to LICENSE at repository root for license details.
 */

package com.brightsparklabs.dropwizard.bundles.auth.external;

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.security.Principal;
import java.util.Optional;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import org.eclipse.jetty.security.DefaultUserIdentity;
import org.eclipse.jetty.security.UserAuthentication;
import org.eclipse.jetty.server.HttpConnection;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.UserIdentity;

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
public class AddUserAuthToRequestFilter<P extends Principal> implements ContainerRequestFilter {

    /** The principal converter to convert from Principal P to the InternalUser */
    private final PrincipalConverter<P> principalConverter;

    /**
     * Default constructor.
     *
     * @param principalConverter Converter between {@link InternalUser} and the {@link Principal}
     *     used in the system.
     */
    public AddUserAuthToRequestFilter(final PrincipalConverter<P> principalConverter) {
        this.principalConverter = requireNonNull(principalConverter);
    }

    @Override
    public void filter(final ContainerRequestContext ctx) throws IOException {
        // Get the authorised user principal
        final Optional<Principal> userPrincipal =
                Optional.ofNullable(ctx.getSecurityContext().getUserPrincipal());
        if (userPrincipal.isPresent()) {
            final Optional<Request> request =
                    Optional.ofNullable(
                            HttpConnection.getCurrentConnection().getHttpChannel().getRequest());
            if (request.isPresent()) {
                // Extract the username and set as the user identity in the request
                final Optional<InternalUser> maybeInternalUser =
                        this.principalConverter.convertToInternalUser((P) userPrincipal.get());
                if (maybeInternalUser.isPresent()) {
                    final Principal principal = maybeInternalUser.get()::getUsername;
                    final UserIdentity userId = new DefaultUserIdentity(null, principal, null);
                    request.get().setAuthentication(new UserAuthentication(null, userId));
                }
            }
        }
    }
}
