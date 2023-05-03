/*
 * Maintained by brightSPARK Labs.
 * www.brightsparklabs.com
 *
 * Refer to LICENSE at repository root for license details.
 */

package com.brightsparklabs.dropwizard.bundles.auth.external;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.collect.ImmutableSet;

import org.immutables.value.Value;

import java.security.Principal;
import java.util.Optional;

/**
 * An authenticated user as used internally by this bundle. Clients should translate this into the
 * type of {@link Principal} used within their systems.
 *
 * @author brightSPARK Labs
 */
@Value.Immutable
@JsonDeserialize(builder = ImmutableInternalUser.Builder.class)
public abstract class InternalUser implements Principal {
    // -------------------------------------------------------------------------
    // CONSTANTS
    // -------------------------------------------------------------------------

    // -------------------------------------------------------------------------
    // CLASS VARIABLES
    // -------------------------------------------------------------------------

    // -------------------------------------------------------------------------
    // PUBLIC METHODS
    // -------------------------------------------------------------------------

    @Override
    @Value.Derived
    public String getName() {
        return getFirstname() + " " + getLastname();
    }

    /**
     * Returns the user's username (login).
     *
     * @return user's username (login).
     */
    public abstract String getUsername();

    /**
     * Returns the user's firstname.
     *
     * @return user's firstname.
     */
    public abstract String getFirstname();

    /**
     * Returns the user's lastname.
     *
     * @return user's lastname.
     */
    public abstract String getLastname();

    /**
     * Returns the user's Optional email.
     *
     * @return user's Optional email.
     */
    public abstract Optional<String> getEmail();

    /**
     * Returns the user's group memberships.
     *
     * @return user's group memberships.
     */
    public abstract ImmutableSet<String> getGroups();

    /**
     * Returns the user's roles.
     *
     * @return user's roles.
     */
    public abstract ImmutableSet<String> getRoles();

    /**
     * Returns the URL for logging out the user.
     *
     * @return URL for logging out the user.
     */
    public abstract Optional<String> getLogoutUrl();
}
