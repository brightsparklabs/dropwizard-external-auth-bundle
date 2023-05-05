/*
 * Maintained by brightSPARK Labs.
 * www.brightsparklabs.com
 *
 * Refer to LICENSE at repository root for license details.
 */

package com.brightsparklabs.dropwizard.bundles.auth.external;

/**
 * Configuration for the {@link ExternallyAuthenticatedAuthBundle}.
 *
 * @author brightSPARK Labs
 */
public interface ExternallyAuthenticatedAuthBundleConfiguration {

    // -------------------------------------------------------------------------
    // CONSTANTS
    // -------------------------------------------------------------------------

    // -------------------------------------------------------------------------
    // CLASS VARIABLES
    // -------------------------------------------------------------------------

    // -------------------------------------------------------------------------
    // PUBLIC METHODS
    // -------------------------------------------------------------------------

    /**
     * Gets configuration for trusting external authentication providers.
     *
     * @return Factory for producing an AuthFilter which authenticates a user.
     */
    ExternallyAuthenticatedAuthFilterFactory getExternallyAuthenticatedFilterFactory();
}
