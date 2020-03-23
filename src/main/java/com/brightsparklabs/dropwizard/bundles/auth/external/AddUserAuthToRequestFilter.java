/*
 * Created by brightSPARK Labs in 2020.
 * www.brightsparklabs.com
 *
 * Refer to LICENSE at repository root for license details.
 */

package com.brightsparklabs.dropwizard.bundles.auth.external;

import java.io.IOException;
import java.security.Principal;
import java.util.Optional;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.ext.Provider;
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
 */
@Provider
public class AddUserAuthToRequestFilter implements ContainerRequestFilter {

    @Override
    public void filter(ContainerRequestContext ctx) throws IOException {
        // Get the authorised user principal
        final Optional<Principal> userPrincipal =
                Optional.ofNullable(ctx.getSecurityContext().getUserPrincipal());
        if (userPrincipal.isPresent() && userPrincipal.get() instanceof InternalUser) {
            final Request request =
                    HttpConnection.getCurrentConnection().getHttpChannel().getRequest();
            if (request != null) {
                final InternalUser internalUser = (InternalUser) userPrincipal.get();
                // Add the principal to the request to make it available for audit logging
                final Principal principal = internalUser::getUsername;
                final UserIdentity userId = new DefaultUserIdentity(null, principal, null);
                request.setAuthentication(new UserAuthentication(null, userId));
            }
        }
    }
}

