/*
 * Created by brightSPARK Labs
 * www.brightsparklabs.com
 */

package com.brightsparklabs.dropwizard.bundles.auth.external;

import io.dropwizard.ConfiguredBundle;
import io.dropwizard.auth.AuthDynamicFeature;
import io.dropwizard.auth.AuthFilter;
import io.dropwizard.auth.AuthValueFactoryProvider;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

/**
 * Bundle to support authenticating users who have been already authenticated by an external
 * system.
 *
 * @author brightSPARK Labs
 */
public class ExternallyAuthenticatedAuthBundle<T extends ExternallyAuthenticatedAuthBundleConfiguration>
        implements ConfiguredBundle<T>
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

    // -------------------------------------------------------------------------
    // CONSTRUCTION
    // -------------------------------------------------------------------------

    // -------------------------------------------------------------------------
    // PUBLIC METHODS
    // -------------------------------------------------------------------------

    @Override
    public void initialize(final Bootstrap<?> bootstrap) { }

    @Override
    public void run(final T configuration, final Environment environment) throws Exception
    {
        final ExternallyAuthenticatedAuthFilterFactory authFilterFactory
                = configuration.getExternallyAuthenticatedConfiguration();
        final AuthFilter<?, ExternalUser> authFilter = authFilterFactory.build();

        environment.jersey().register(new AuthDynamicFeature(authFilter));
        // Support using @Auth to inject a custom Principal type into resources
        environment.jersey().register(new AuthValueFactoryProvider.Binder<>(ExternalUser.class));
    }

    // -------------------------------------------------------------------------
    // PUBLIC METHODS
    // -------------------------------------------------------------------------

    // -------------------------------------------------------------------------
    // PRIVATE METHODS
    // -------------------------------------------------------------------------
}
