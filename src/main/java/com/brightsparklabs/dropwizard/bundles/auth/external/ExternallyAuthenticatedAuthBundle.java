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

import java.security.Principal;
import java.util.function.Function;

/**
 * Bundle to support authenticating users who have been already authenticated by an external
 * system.
 *
 * @param <T>
 *         Type of configuration required to configure the bundle.
 * @param <P>
 *         Type of {@link Principal} to return for authenticated users.
 *
 * @author brightSPARK Labs
 */
public class ExternallyAuthenticatedAuthBundle<P extends Principal, T extends ExternallyAuthenticatedAuthBundleConfiguration>
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

    /** Type of {@link Principal} to return for authenticated users */
    private final Class<P> principalClazz;

    /** Converts the internal user to the {@link Principal} used in the system */
    private final Function<InternalUser, P> externalUserToPrincipal;

    // -------------------------------------------------------------------------
    // CONSTRUCTION
    // -------------------------------------------------------------------------

    public ExternallyAuthenticatedAuthBundle(Class<P> principalClazz,
            Function<InternalUser, P> converter)
    {
        this.principalClazz = principalClazz;
        this.externalUserToPrincipal = converter;
    }

    // -------------------------------------------------------------------------
    // PUBLIC METHODS
    // -------------------------------------------------------------------------

    @Override
    public void initialize(final Bootstrap<?> bootstrap) { }

    @Override
    public void run(final T configuration, final Environment environment) throws Exception
    {
        final ExternallyAuthenticatedAuthFilterFactory authFilterFactory
                = configuration.getExternallyAuthenticatedFilterFactory();
        final AuthFilter<?, P> authFilter = authFilterFactory.build(externalUserToPrincipal);

        environment.jersey().register(new AuthDynamicFeature(authFilter));
        // Support using @Auth to inject a custom Principal type into resources
        environment.jersey().register(new AuthValueFactoryProvider.Binder<>(principalClazz));
    }

    // -------------------------------------------------------------------------
    // PUBLIC METHODS
    // -------------------------------------------------------------------------

    // -------------------------------------------------------------------------
    // PRIVATE METHODS
    // -------------------------------------------------------------------------
}
