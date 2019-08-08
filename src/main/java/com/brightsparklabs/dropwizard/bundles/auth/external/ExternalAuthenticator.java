/*
 * Created by brightSPARK Labs
 * www.brightsparklabs.com
 */

package com.brightsparklabs.dropwizard.bundles.auth.external;

import io.dropwizard.auth.AuthenticationException;
import io.dropwizard.auth.Authenticator;

import java.security.Principal;
import java.util.Optional;
import java.util.function.Function;

/**
 * Authenticates a user based on information passed to it by an external authentication provider.
 *
 * @param <C>
 *         Type of credentials the authenticator requires.
 * @param <P>
 *         Type of {@link Principal} to return for authenticated users.
 *
 * @author brightSPARK Labs
 */
public abstract class ExternalAuthenticator<C, P extends Principal> implements Authenticator<C, P>
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

    /** Converts an internal user to the principal used in the system */
    private final Function<InternalUser, P> externalUserToPrincipal;

    // -------------------------------------------------------------------------
    // CONSTRUCTION
    // -------------------------------------------------------------------------

    /**
     * Creates a new authenticator which validates JWTs using the specified public signing key. This
     * should be the signing key of the Identity Provider who signed the JWT.
     *
     * @param externalUserToPrincipal
     *         Converts the internal user to the {@link Principal} used in the system.
     */
    ExternalAuthenticator(final Function<InternalUser, P> externalUserToPrincipal)
    {
        this.externalUserToPrincipal = externalUserToPrincipal;
    }

    // -------------------------------------------------------------------------
    // IMPLEMENTATION:  Authenticator
    // -------------------------------------------------------------------------

    @Override
    public Optional<P> authenticate(final C credentials) throws AuthenticationException
    {
        final Optional<InternalUser> user = doAuthenticate(credentials);
        if (!user.isPresent())
        {
            return Optional.empty();
        }
        final P principal = externalUserToPrincipal.apply(user.get());
        return Optional.of(principal);
    }

    // -------------------------------------------------------------------------
    // PUBLIC METHODS
    // -------------------------------------------------------------------------

    public abstract Optional<InternalUser> doAuthenticate(final C credentials)
            throws AuthenticationException;

    // -------------------------------------------------------------------------
    // PRIVATE METHODS
    // -------------------------------------------------------------------------
}
