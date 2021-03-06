/*
 * Created by brightSPARK Labs in 2020.
 * www.brightsparklabs.com
 *
 * Refer to LICENSE at repository root for license details.
 */

package com.brightsparklabs.dropwizard.bundles.auth.external;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.collect.ImmutableSet;
import java.security.Principal;
import java.util.Optional;
import org.immutables.value.Value;

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

    @Value.Derived
    public String getName() {
        return getFirstname() + " " + getLastname();
    }

    /** @return user's username (login). */
    public abstract String getUsername();

    /** @return user's firstname. */
    public abstract String getFirstname();

    /** @return user's lastname. */
    public abstract String getLastname();

    /** @return user's Optional email. */
    public abstract Optional<String> getEmail();

    /** @return user's group memberships. */
    public abstract ImmutableSet<String> getGroups();

    /** @return user's roles. */
    public abstract ImmutableSet<String> getRoles();

    /** @return URL for logging out the user. */
    public abstract Optional<String> getLogoutUrl();
}
