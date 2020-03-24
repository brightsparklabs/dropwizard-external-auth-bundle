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

    private final PrincipalConverter<P> principalConverter;

    public AddUserAuthToRequestFilter(final PrincipalConverter<P> principalConverter) {
        this.principalConverter = requireNonNull(principalConverter);
    }

    @Override
    public void filter(ContainerRequestContext ctx) throws IOException {
        // Get the authorised user principal
        final Optional<Principal> userPrincipal =
                Optional.ofNullable(ctx.getSecurityContext().getUserPrincipal());
        if (userPrincipal.isPresent()) {
            final Request request =
                    HttpConnection.getCurrentConnection().getHttpChannel().getRequest();
            if (request != null) {
                // Extract the username and set as the user identity in the request
                final InternalUser internalUser =
                        this.principalConverter.convertToInternalUser((P) userPrincipal.get());
                final Principal principal = internalUser::getUsername;
                final UserIdentity userId = new DefaultUserIdentity(null, principal, null);
                request.setAuthentication(new UserAuthentication(null, userId));
            }
        }
    }
}
