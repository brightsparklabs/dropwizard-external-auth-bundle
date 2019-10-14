/*
 * Created by brightSPARK Labs
 * www.brightsparklabs.com
 */

package com.brightsparklabs.dropwizard.bundles.auth.external;

import io.dropwizard.ConfiguredBundle;
import io.dropwizard.auth.*;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import org.glassfish.jersey.server.filter.RolesAllowedDynamicFeature;
import org.immutables.value.internal.$guava$.collect.$ComputationException;

import java.security.Principal;
import java.util.Optional;
import java.util.function.Function;

import static java.util.Objects.*;

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

    /** Authorizer to use - will default to permit all*/
    private final Authorizer<P> authorizer;

    private final boolean setupRolesAllowedDynamicFeature;

    // -------------------------------------------------------------------------
    // CONSTRUCTION
    // -------------------------------------------------------------------------

    /**
     * Constructor.  This will default to PermitAll authorization.
     *
     * @param principalClazz Type of {@link Principal} to return for authenticated users
     * @param converter Converts the internal user to the {@link Principal} used in the system
     */
    public ExternallyAuthenticatedAuthBundle(final Class<P> principalClazz,
            final Function<InternalUser, P> converter)
    {
        this(principalClazz, converter, new PermitAllAuthorizer<P>(), false);
    }

    /**
     * Constructor.  This will use the provided Authorizer, and register {@link RolesAllowedDynamicFeature}
     *
     * @param principalClazz Type of {@link Principal} to return for authenticated users
     * @param converter Converts the internal user to the {@link Principal} used in the system
     * @param authorizer the {@link Authorizer} to use.
     */
    public ExternallyAuthenticatedAuthBundle(final Class<P> principalClazz,
            final Function<InternalUser, P> converter,
            final Authorizer<P> authorizer)
    {
        this(principalClazz, converter, authorizer, true);
    }

    /**
     * Private Constructor, for convenience.
     *
     * @param principalClazz Type of {@link Principal} to return for authenticated users
     * @param converter Converts the internal user to the {@link Principal} used in the system
     * @param authorizer the {@link Authorizer} to use.
     * @param setupRolesAllowedDynamicFeature determines whether to allow dynamic, role based
     *                                        Authorization
     */
    private ExternallyAuthenticatedAuthBundle(final Class<P> principalClazz,
            final Function<InternalUser, P> converter,
            final Authorizer<P> authorizer,
            final boolean setupRolesAllowedDynamicFeature)
    {
        this.principalClazz = principalClazz;
        this.externalUserToPrincipal = requireNonNull(converter);
        this.authorizer = requireNonNull(authorizer);
        this.setupRolesAllowedDynamicFeature = setupRolesAllowedDynamicFeature;
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
        final AuthFilter<?, P> authFilter = authFilterFactory.build(externalUserToPrincipal,
                authorizer);

        environment.jersey().register(new AuthDynamicFeature(authFilter));
        // Support using @Auth to inject a custom Principal type into resources
        environment.jersey().register(new AuthValueFactoryProvider.Binder<>(principalClazz));

        // Allow dynamic authorization.
        if (setupRolesAllowedDynamicFeature)
        {
            environment.jersey().register(RolesAllowedDynamicFeature.class);
        }
    }

    // -------------------------------------------------------------------------
    // PUBLIC METHODS
    // -------------------------------------------------------------------------

    // -------------------------------------------------------------------------
    // PRIVATE METHODS
    // -------------------------------------------------------------------------
}
