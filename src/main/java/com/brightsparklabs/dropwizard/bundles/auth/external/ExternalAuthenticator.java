/*
 * Maintained by brightSPARK Labs.
 * www.brightsparklabs.com
 *
 * Refer to LICENSE at repository root for license details.
 */

package com.brightsparklabs.dropwizard.bundles.auth.external;

import io.dropwizard.auth.AuthenticationException;
import io.dropwizard.auth.Authenticator;
import java.security.Principal;
import java.util.Optional;

/**
 * Authenticates a user based on information passed to it by an external authentication provider.
 *
 * @param <C> Type of credentials the authenticator requires.
 * @param <P> Type of {@link Principal} to return for authenticated users.
 * @author brightSPARK Labs
 */
public abstract class ExternalAuthenticator<C, P extends Principal> implements Authenticator<C, P> {
    // -------------------------------------------------------------------------
    // CONSTANTS
    // -------------------------------------------------------------------------

    // -------------------------------------------------------------------------
    // CLASS VARIABLES
    // -------------------------------------------------------------------------

    // -------------------------------------------------------------------------
    // INSTANCE VARIABLES
    // -------------------------------------------------------------------------

    /** Converter between {@link InternalUser} and the {@link Principal} used in the system. */
    private final PrincipalConverter<P> principalConverter;

    private final Iterable<AuthenticationEventListener> authenticationEventListeners;

    // -------------------------------------------------------------------------
    // CONSTRUCTION
    // -------------------------------------------------------------------------

    /**
     * Creates a new authenticator which validates JWTs using the specified public signing key. This
     * should be the signing key of the Identity Provider who signed the JWT.
     *
     * @param principalConverter Converter between {@link InternalUser} and the {@link Principal}
     *     used in the system.
     * @param listeners The authentication event listeners
     */
    public ExternalAuthenticator(
            final PrincipalConverter<P> principalConverter,
            final Iterable<AuthenticationEventListener> listeners) {
        this.principalConverter = principalConverter;
        this.authenticationEventListeners = listeners;
    }

    // -------------------------------------------------------------------------
    // IMPLEMENTATION:  Authenticator
    // -------------------------------------------------------------------------

    @Override
    public Optional<P> authenticate(final C credentials) throws AuthenticationException {
        try {
            final InternalUser authenticatedInternalUser = doAuthenticate(credentials);
            authenticationEventListeners.forEach(
                    listener -> listener.onAuthenticationSuccess(authenticatedInternalUser));
            return Optional.of(principalConverter.convertToPrincipal(authenticatedInternalUser));
        } catch (AuthenticationDeniedException authDeniedException) {
            // Call listener functions and return an empty optional to indicate authentication was
            // denied
            authenticationEventListeners.forEach(
                    listener -> listener.onAuthenticationDenied(authDeniedException));
            return Optional.empty();
        } catch (AuthenticationException authException) {
            // Call listener functions and propagate exception
            authenticationEventListeners.forEach(
                    listener -> listener.onAuthenticationError(authException));
            throw authException;
        }
    }

    // -------------------------------------------------------------------------
    // PUBLIC METHODS
    // -------------------------------------------------------------------------

    /**
     * Authenticates a user based on the credentials provided.
     *
     * @param credentials The credentials used to authenticate the user.
     * @throws AuthenticationException Exception to indicate that the Authenticator is unable to
     *     check the validity of the given credentials.
     * @throws AuthenticationDeniedException Exception to indicate that an Authenticator has
     *     determined that the supplied credentials are invalid.
     * @return Authenticated internal user object.
     */
    public abstract InternalUser doAuthenticate(final C credentials)
            throws AuthenticationException, AuthenticationDeniedException;

    // -------------------------------------------------------------------------
    // PRIVATE METHODS
    // -------------------------------------------------------------------------
}
