/*
 * Created by brightSPARK Labs
 * www.brightsparklabs.com
 */

package com.brightsparklabs.dropwizard.bundles.auth.external;

import com.google.common.collect.ImmutableSet;
import org.immutables.value.Value;

import java.security.Principal;

/**
 * An authenticated user as used internally by this bundle. Clients should translate this into the
 * type of {@link Principal} used within their systems.
 *
 * @author brightSPARK Labs
 */
@Value.Immutable
public abstract class InternalUser implements Principal
{
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
    public String getName()
    {
        return getFirstname() + " " + getLastname();
    }

    /**
     * @return user's username (login).
     */
    public abstract String getUsername();

    /**
     * @return user's firstname.
     */
    public abstract String getFirstname();

    /**
     * @return user's lastname.
     */
    public abstract String getLastname();

    /**
     * @return user's email.
     */
    public abstract String getEmail();

    /**
     * @return user's group memberships.
     */
    public abstract ImmutableSet<String> getGroups();

    /**
     * @return user's roles.
     */
    public abstract ImmutableSet<String> getRoles();
}