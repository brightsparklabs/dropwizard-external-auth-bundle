/*
 * Created by brightSPARK Labs in 2020.
 * www.brightsparklabs.com
 *
 * Refer to LICENSE at repository root for license details.
 */

package com.brightsparklabs.dropwizard.bundles.auth.external;

import static java.util.Objects.requireNonNull;

import io.dropwizard.ConfiguredBundle;
import io.dropwizard.auth.AuthDynamicFeature;
import io.dropwizard.auth.AuthFilter;
import io.dropwizard.auth.AuthValueFactoryProvider;
import io.dropwizard.auth.Authorizer;
import io.dropwizard.auth.PermitAllAuthorizer;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import org.glassfish.jersey.server.filter.RolesAllowedDynamicFeature;

/**
 * Bundle to support authenticating users who have been already authenticated by an external system.
 *
 * @param <T> Type of configuration required to configure the bundle.
 * @param <P> Type of {@link Principal} to return for authenticated users.
 * @author brightSPARK Labs
 */
public class ExternallyAuthenticatedAuthBundle<
                P extends Principal, T extends ExternallyAuthenticatedAuthBundleConfiguration>
        implements ConfiguredBundle<T> {
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

    /** Authorizer to use - will default to permit all */
    private final Authorizer<P> authorizer;

    /** determines whether to allow dynamic, role based Authorization */
    private final boolean setupRolesAllowedDynamicFeature;

    /** listeners for authentication events */
    private final List<AuthenticationEventListener> authenticationEventListeners;

    // -------------------------------------------------------------------------
    // CONSTRUCTION
    // -------------------------------------------------------------------------

    /**
     * Constructor. This will default to PermitAll authorization.
     *
     * @param principalClazz Type of {@link Principal} to return for authenticated users
     * @param converter Converts the internal user to the {@link Principal} used in the system
     */
    public ExternallyAuthenticatedAuthBundle(
            final Class<P> principalClazz,
            final Function<InternalUser, P> converter,
            final AuthenticationEventListener... listeners) {
        this(principalClazz, converter, new PermitAllAuthorizer<P>(), false, listeners);
    }

    /**
     * Constructor. This will use the provided Authorizer, and register {@link
     * RolesAllowedDynamicFeature}
     *
     * @param principalClazz Type of {@link Principal} to return for authenticated users
     * @param converter Converts the internal user to the {@link Principal} used in the system
     * @param authorizer the {@link Authorizer} to use.
     */
    public ExternallyAuthenticatedAuthBundle(
            final Class<P> principalClazz,
            final Function<InternalUser, P> converter,
            final Authorizer<P> authorizer,
            final AuthenticationEventListener... listeners) {
        this(principalClazz, converter, authorizer, true, listeners);
    }

    /**
     * Private Constructor, for convenience.
     *
     * @param principalClazz Type of {@link Principal} to return for authenticated users
     * @param converter Converts the internal user to the {@link Principal} used in the system
     * @param authorizer the {@link Authorizer} to use.
     * @param setupRolesAllowedDynamicFeature determines whether to allow dynamic, role based
     *     Authorization
     */
    private ExternallyAuthenticatedAuthBundle(
            final Class<P> principalClazz,
            final Function<InternalUser, P> converter,
            final Authorizer<P> authorizer,
            final boolean setupRolesAllowedDynamicFeature,
            final AuthenticationEventListener... listeners) {
        this.principalClazz = principalClazz;
        this.externalUserToPrincipal = requireNonNull(converter);
        this.authorizer = requireNonNull(authorizer);
        this.setupRolesAllowedDynamicFeature = setupRolesAllowedDynamicFeature;
        this.authenticationEventListeners =
                new ArrayList<AuthenticationEventListener>(Arrays.asList(listeners));
    }

    // -------------------------------------------------------------------------
    // PUBLIC METHODS
    // -------------------------------------------------------------------------

    @Override
    public void initialize(final Bootstrap<?> bootstrap) {}

    @Override
    public void run(final T configuration, final Environment environment) throws Exception {
        final ExternallyAuthenticatedAuthFilterFactory authFilterFactory =
                configuration.getExternallyAuthenticatedFilterFactory();
        final AuthFilter<?, P> authFilter =
                authFilterFactory.build(
                        externalUserToPrincipal, authorizer, authenticationEventListeners);

        environment.jersey().register(new AuthDynamicFeature(authFilter));
        // Support using @Auth to inject a custom Principal type into resources
        environment.jersey().register(new AuthValueFactoryProvider.Binder<>(principalClazz));

        // Allow dynamic authorization.
        if (setupRolesAllowedDynamicFeature) {
            environment.jersey().register(RolesAllowedDynamicFeature.class);
        }
    }

    // -------------------------------------------------------------------------
    // PUBLIC METHODS
    // -------------------------------------------------------------------------

    /**
     * Add an authentication listener
     *
     * @param listener the listener to add
     */
    public void addAuthenticationEventListener(final AuthenticationEventListener listener) {
        final AuthenticationEventListener safeListener = requireNonNull(listener);
        this.authenticationEventListeners.add(safeListener);
    }

    /**
     * Removes an authentication listener
     *
     * @param listener the listener to remove
     */
    public void removeAuthenticationEventListener(final AuthenticationEventListener listener) {
        final AuthenticationEventListener safeListener = requireNonNull(listener);
        this.authenticationEventListeners.remove(safeListener);
    }

    /**
     * Get the authentication listeners
     *
     * @return the authentication listeners
     */
    public List<AuthenticationEventListener> getAuthenticationEventListeners() {
        return this.authenticationEventListeners;
    }

    // -------------------------------------------------------------------------
    // PRIVATE METHODS
    // -------------------------------------------------------------------------
}
