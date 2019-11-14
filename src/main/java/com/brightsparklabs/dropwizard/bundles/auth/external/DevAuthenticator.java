/*
 * Created by brightSPARK Labs
 * www.brightsparklabs.com
 */

package com.brightsparklabs.dropwizard.bundles.auth.external;

import io.dropwizard.auth.AuthenticationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.Principal;
import java.util.Optional;
import java.util.function.Function;

/**
 * Authenticator for use during development only.
 *
 * @param <P>
 *         Type of {@link Principal} to return for authenticated users.
 *
 * @author brightSPARK Labs
 */
public class DevAuthenticator<P extends Principal> extends ExternalAuthenticator<String, P>
{
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
     * @param externalUserToPrincipal
     *         Converts the internal user to the {@link Principal} used in the system.
     * @param user
     *         user to return
     */
    DevAuthenticator(final Function<InternalUser, P> externalUserToPrincipal,
            final InternalUser user)
    {
        super(externalUserToPrincipal);
        this.user = user;
    }

    // -------------------------------------------------------------------------
    // IMPLEMENTATION:  ExternalAuthenticator
    // -------------------------------------------------------------------------

    @Override
    public Optional<InternalUser> doAuthenticate(final String credentials)
            throws AuthenticationException
    {
        logger.warn("********** USING DEV MODE AUTHENTICATOR. DO NOT USE IN PRODUCTION **********");
        return Optional.of(user);
    }

    // -------------------------------------------------------------------------
    // PUBLIC METHODS
    // -------------------------------------------------------------------------

    // -------------------------------------------------------------------------
    // PRIVATE METHODS
    // -------------------------------------------------------------------------
}
