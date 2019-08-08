/*
 * Created by brightSPARK Labs
 * www.brightsparklabs.com
 */

package com.brightsparklabs.dropwizard.bundles.auth.external;

/**
 * Configuration for the {@link ExternallyAuthenticatedAuthBundle}.
 *
 * @author brightSPARK Labs
 */
public interface ExternallyAuthenticatedAuthBundleConfiguration
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

    /**
     * @return configuration for trusting external authentication providers.
     */
    ExternallyAuthenticatedAuthFilterFactory getExternallyAuthenticatedFilterFactory();
}
