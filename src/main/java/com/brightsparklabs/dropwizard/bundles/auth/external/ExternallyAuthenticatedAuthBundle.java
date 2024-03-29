/*
 * Maintained by brightSPARK Labs.
 * www.brightsparklabs.com
 *
 * Refer to LICENSE at repository root for license details.
 */

package com.brightsparklabs.dropwizard.bundles.auth.external;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableList;
import io.dropwizard.auth.AuthDynamicFeature;
import io.dropwizard.auth.AuthFilter;
import io.dropwizard.auth.AuthValueFactoryProvider;
import io.dropwizard.auth.AuthenticationException;
import io.dropwizard.auth.Authorizer;
import io.dropwizard.auth.PermitAllAuthorizer;
import io.dropwizard.core.ConfiguredBundle;
import io.dropwizard.core.setup.Bootstrap;
import io.dropwizard.core.setup.Environment;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import jakarta.ws.rs.ext.ExceptionMapper;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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

    /** Converter between {@link InternalUser} and the {@link Principal} used in the system. */
    private final PrincipalConverter<P> principalConverter;

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
     * @param listeners Listeners to notify on authentication events.
     */
    public ExternallyAuthenticatedAuthBundle(
            final Class<P> principalClazz,
            final PrincipalConverter<P> converter,
            final AuthenticationEventListener... listeners) {
        this(principalClazz, converter, new PermitAllAuthorizer<P>(), false, listeners);
    }

    /**
     * Constructor. This will use the provided Authorizer, and register {@link
     * RolesAllowedDynamicFeature}
     *
     * @param principalClazz Type of {@link Principal} to return for authenticated users
     * @param converter Converter between {@link InternalUser} and the {@link Principal} used in the
     *     system.
     * @param authorizer the {@link Authorizer} to use.
     * @param listeners Listeners to notify on authentication events.
     */
    public ExternallyAuthenticatedAuthBundle(
            final Class<P> principalClazz,
            final PrincipalConverter<P> converter,
            final Authorizer<P> authorizer,
            final AuthenticationEventListener... listeners) {
        this(principalClazz, converter, authorizer, true, listeners);
    }

    /**
     * Constructor for an AuthBundle that uses an {@link InternalUser} as principal. This will
     * default to PermitAll authorization.
     *
     * @param listeners The authentication event listeners
     */
    public ExternallyAuthenticatedAuthBundle(final AuthenticationEventListener... listeners) {
        this(new PermitAllAuthorizer<>(), false, listeners);
    }

    /**
     * Constructor for an AuthBundle that uses an {@link InternalUser} as principal. This will use
     * the provided Authorizer, and register {@link RolesAllowedDynamicFeature}
     *
     * @param authorizer the {@link Authorizer} to use.
     * @param listeners The authentication event listeners
     */
    public ExternallyAuthenticatedAuthBundle(
            final Authorizer<InternalUser> authorizer,
            final AuthenticationEventListener... listeners) {
        this(authorizer, true, listeners);
    }

    /**
     * Private Constructor for {@link ExternallyAuthenticatedAuthBundle} that use {@link
     * InternalUser} as the principal.
     *
     * @param authorizer the {@link Authorizer} to use.
     * @param setupRolesAllowedDynamicFeature determines whether to allow dynamic, role based
     *     Authorization
     * @param listeners The authentication event listeners
     */
    private ExternallyAuthenticatedAuthBundle(
            final Authorizer<InternalUser> authorizer,
            final boolean setupRolesAllowedDynamicFeature,
            final AuthenticationEventListener... listeners) {
        this(
                (Class<P>) InternalUser.class,
                (PrincipalConverter<P>) new IdentityPrincipalConverter(),
                (Authorizer<P>) authorizer,
                setupRolesAllowedDynamicFeature,
                listeners);
    }

    /**
     * Private Constructor, for convenience.
     *
     * @param principalClazz Type of {@link Principal} to return for authenticated users
     * @param converter Converter between {@link InternalUser} and the {@link Principal} used in the
     *     system.
     * @param authorizer the {@link Authorizer} to use.
     * @param setupRolesAllowedDynamicFeature determines whether to allow dynamic, role based
     *     Authorization
     * @param listeners Listeners to notify on authentication events.
     */
    private ExternallyAuthenticatedAuthBundle(
            final Class<P> principalClazz,
            final PrincipalConverter<P> converter,
            final Authorizer<P> authorizer,
            final boolean setupRolesAllowedDynamicFeature,
            final AuthenticationEventListener... listeners) {
        this.principalClazz = principalClazz;
        this.principalConverter = requireNonNull(converter);
        this.authorizer = requireNonNull(authorizer);
        this.setupRolesAllowedDynamicFeature = setupRolesAllowedDynamicFeature;
        this.authenticationEventListeners = new ArrayList<>(Arrays.asList(listeners));
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

        // Handle authentication exceptions.
        environment.jersey().register(new AuthenticationExceptionMapper());
        environment.jersey().register(new AuthenticationDeniedExceptionMapper());

        // Add the user authentication to the request
        environment
                .jersey()
                .register(
                        new AddUserAuthToRequestFilter<>(
                                principalConverter,
                                configuration
                                        .getExternallyAuthenticatedFilterFactory()
                                        .getMdcUsernameField()));

        final AuthFilter<?, P> authFilter =
                authFilterFactory.build(
                        principalConverter, authorizer, authenticationEventListeners);
        environment.jersey().register(new AuthDynamicFeature(authFilter));

        // Support using @Auth to inject a custom Principal type into resources.
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
        return ImmutableList.copyOf(this.authenticationEventListeners);
    }

    // -------------------------------------------------------------------------
    // PRIVATE METHODS
    // -------------------------------------------------------------------------

    // -------------------------------------------------------------------------
    // INNER CLASSES
    // -------------------------------------------------------------------------

    /** Maps the {@link AuthenticationException} to a {@link Status#UNAUTHORIZED}. */
    private static class AuthenticationExceptionMapper
            implements ExceptionMapper<AuthenticationException> {
        @Override
        public Response toResponse(final AuthenticationException exception) {
            return Response.status(Status.UNAUTHORIZED).build();
        }
    }

    /** Maps the {@link AuthenticationException} to a {@link Status#UNAUTHORIZED}. */
    private static class AuthenticationDeniedExceptionMapper
            implements ExceptionMapper<AuthenticationDeniedException> {
        @Override
        public Response toResponse(final AuthenticationDeniedException exception) {
            return Response.status(Status.UNAUTHORIZED).build();
        }
    }
}
