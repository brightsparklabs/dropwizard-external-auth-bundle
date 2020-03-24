/*
 * Created by brightSPARK Labs in 2020.
 * www.brightsparklabs.com
 *
 * Refer to LICENSE at repository root for license details.
 */

package com.brightsparklabs.dropwizard.bundles.auth.external;

import io.dropwizard.auth.AuthenticationException;
import java.security.Principal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Authenticator for use during development only.
 *
 * @param <P> Type of {@link Principal} to return for authenticated users.
 * @author brightSPARK Labs
 */
public class DevAuthenticator<P extends Principal> extends ExternalAuthenticator<String, P> {
    // -------------------------------------------------------------------------
    // CONSTANTS
    // -------------------------------------------------------------------------

    // -------------------------------------------------------------------------
    // CLASS VARIABLES
    // -------------------------------------------------------------------------

    /** Class logger */
    private static Logger logger = LoggerFactory.getLogger(DevAuthenticator.class);

    // -------------------------------------------------------------------------
    // INSTANCE VARIABLES
    // -------------------------------------------------------------------------

    /** User to return */
    private final InternalUser user;

    // -------------------------------------------------------------------------
    // CONSTRUCTION
    // -------------------------------------------------------------------------

    /**
     * Creates a new authenticator which always returns the User from configuration. For use during
     * development only
     *
     * @param principalConverter Converter between {@link InternalUser} and the {@link Principal}
     *     used in the system.
     * @param user user to return
     */
    DevAuthenticator(
            final PrincipalConverter<P> principalConverter,
            final InternalUser user,
            final Iterable<AuthenticationEventListener> listeners) {
        super(principalConverter, listeners);
        this.user = user;
    }

    // -------------------------------------------------------------------------
    // IMPLEMENTATION:  ExternalAuthenticator
    // -------------------------------------------------------------------------

    @Override
    public InternalUser doAuthenticate(final String credentials)
            throws AuthenticationException, AuthenticationDeniedException {
        logger.warn("********** USING DEV MODE AUTHENTICATOR. DO NOT USE IN PRODUCTION **********");
        return user;
    }

    // -------------------------------------------------------------------------
    // PUBLIC METHODS
    // -------------------------------------------------------------------------

    // -------------------------------------------------------------------------
    // PRIVATE METHODS
    // -------------------------------------------------------------------------
}
