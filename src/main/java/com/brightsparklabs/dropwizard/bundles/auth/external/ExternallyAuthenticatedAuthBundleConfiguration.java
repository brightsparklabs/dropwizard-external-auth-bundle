/*
 * Created by brightSPARK Labs in 2020.
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

    /** @return configuration for trusting external authentication providers. */
    ExternallyAuthenticatedAuthFilterFactory getExternallyAuthenticatedFilterFactory();
}
