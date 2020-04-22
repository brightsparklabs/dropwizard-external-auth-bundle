/*
 * Created by brightSPARK Labs in 2020.
 * www.brightsparklabs.com
 *
 * Refer to LICENSE at repository root for license details.
 */

package com.brightsparklabs.dropwizard.bundles.auth.external;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.google.common.collect.ImmutableList;
import io.dropwizard.auth.AuthFilter;
import io.dropwizard.auth.Authorizer;
import io.dropwizard.auth.chained.ChainedAuthFilter;
import io.dropwizard.auth.oauth.OAuthCredentialAuthFilter;
import io.dropwizard.jackson.Discoverable;
import java.security.Principal;
import javax.validation.constraints.NotNull;
import org.hibernate.validator.constraints.NotEmpty;

/**
 * Factory for producing an {@link AuthFilter} which authenticates a user based on information
 * passed to it by an external authentication provider.
 *
 * <p>This will be created by Dropwizard + Jackson.
 */
@JsonTypeInfo(use = Id.NAME, property = "method")
public abstract class ExternallyAuthenticatedAuthFilterFactory implements Discoverable {

    // -------------------------------------------------------------------------
    // CONSTANTS
    // -------------------------------------------------------------------------

    /**
     * Default name of the field in the MDC (Mapped Diagnostic Context) that the authenticated
     * user's username is stored against. Set to: {@value DEFAULT_MDC_USERNAME_FIELD}.
     */
    public static final String DEFAULT_MDC_USERNAME_FIELD = "req.username";

    // -------------------------------------------------------------------------
    // INSTANCE VARIABLES
    // -------------------------------------------------------------------------

    /**
     * Field in the MDC (Mapped Diagnostic Context) that the authenticated user's username is stored
     * against. Default: {@value DEFAULT_MDC_USERNAME_FIELD}.
     */
    @JsonProperty private String mdcUsernameField = DEFAULT_MDC_USERNAME_FIELD;

    // -------------------------------------------------------------------------
    // PUBLIC METHODS
    // -------------------------------------------------------------------------

    /**
     * @return The field in the MDC (Mapped Diagnostic Context) that the authenticated user's
     *     username is stored against. Default: {@value DEFAULT_MDC_USERNAME_FIELD}.
     */
    String getMdcUsernameField() {
        return mdcUsernameField;
    }

    /**
     * Returns an {@link AuthFilter} which authenticates a user based on information passed to it by
     * an external authentication provider.
     *
     * @param principalConverter Converter between {@link InternalUser} and the {@link Principal}
     *     used in the system.
     * @param authorizer The {@link Authorizer} to use.
     * @param listeners The authentication event listeners
     * @param <P> The {@link Principal} the filter should return.
     * @return An {@link AuthFilter} which authenticates a user based on information passed to it by
     *     an external authentication provider.
     */
    public abstract <P extends Principal> AuthFilter<?, P> build(
            PrincipalConverter<P> principalConverter,
            Authorizer<P> authorizer,
            Iterable<AuthenticationEventListener> listeners);

    // -------------------------------------------------------------------------
    // INNER CLASSES
    // -------------------------------------------------------------------------

    /**
     * Factory for producing an {@link AuthFilter} which authenticates a user based on a JWT passed
     * to it by an external authentication provider.
     */
    @JsonTypeName("jwt")
    public static class JwtAuthFilterFactory<P extends Principal>
            extends ExternallyAuthenticatedAuthFilterFactory {
        // -------------------------------------------------------------------------
        // INSTANCE VARIABLES
        // -------------------------------------------------------------------------

        /** Public signing key used to sign the JWT */
        @NotEmpty @JsonProperty private String signingKey;

        // -------------------------------------------------------------------------
        // IMPLEMENTATION: ExternallyAuthenticatedAuthFilterFactory
        // -------------------------------------------------------------------------

        @Override
        public <E extends Principal> AuthFilter<?, E> build(
                final PrincipalConverter<E> principalConverter,
                final Authorizer<E> authorizer,
                final Iterable<AuthenticationEventListener> listeners) {
            if (signingKey == null) {
                throw new IllegalArgumentException(
                        "signingKey has not been defined in authentication configuration");
            }

            return new OAuthCredentialAuthFilter.Builder<E>()
                    .setAuthenticator(
                            new JwtAuthenticator<>(principalConverter, signingKey, listeners))
                    .setAuthorizer(authorizer)
                    .setPrefix("Bearer")
                    .buildAuthFilter();
        }
    }

    /**
     * Factory for producing an {@link AuthFilter} which authenticates a user based on HTTP headers
     * passed to it by an external authentication provider.
     */
    @JsonTypeName("httpHeaders")
    public static class HttpHeadersAuthFilterFactory
            extends ExternallyAuthenticatedAuthFilterFactory {
        // -------------------------------------------------------------------------
        // IMPLEMENTATION: ExternallyAuthenticatedAuthFilterFactory
        // -------------------------------------------------------------------------

        @Override
        public <E extends Principal> AuthFilter<?, E> build(
                final PrincipalConverter<E> principalConverter,
                final Authorizer<E> authorizer,
                final Iterable<AuthenticationEventListener> listeners) {
            return new HeaderFieldsAuthFilter.Builder<E>()
                    .setAuthenticator(
                            new HeaderFieldsAuthenticator<>(principalConverter, listeners))
                    .setAuthorizer(authorizer)
                    .buildAuthFilter();
        }
    }

    /**
     * Factory for producing an {@link AuthFilter} which delegates to a chain of others. Wrapper
     * around https://www.dropwizard.io/en/stable/manual/auth.html#chained-factories
     */
    @JsonTypeName("chained")
    public static class ChainedAuthFilterFactory<P extends Principal>
            extends ExternallyAuthenticatedAuthFilterFactory {

        // -------------------------------------------------------------------------
        // INSTANCE VARIABLES
        // -------------------------------------------------------------------------

        /** The AuthFilters that this will delegate to */
        @NotEmpty @JsonProperty
        private ImmutableList<ExternallyAuthenticatedAuthFilterFactory> delegates;

        // -------------------------------------------------------------------------
        // IMPLEMENTATION: ExternallyAuthenticatedAuthFilterFactory
        // -------------------------------------------------------------------------

        @SuppressWarnings("unchecked")
        @Override
        public <E extends Principal> AuthFilter<?, E> build(
                final PrincipalConverter<E> principalConverter,
                final Authorizer<E> authorizer,
                final Iterable<AuthenticationEventListener> listeners) {
            final ImmutableList<AuthFilter> authFilters =
                    delegates.stream()
                            .map(d -> d.build(principalConverter, authorizer, listeners))
                            .collect(ImmutableList.toImmutableList());

            // This is "unchecked" because we may have different types of C (credentials)
            // which is ok.
            // We're not actually checking that we have the same E for all delegates either.
            return new ChainedAuthFilter(authFilters);
        }
    }

    /**
     * Factory for producing an {@link AuthFilter} which returns a fixed principal. Should only be
     * used for development.
     */
    @JsonTypeName("dev")
    public static class DevAuthFilterFactory extends ExternallyAuthenticatedAuthFilterFactory {
        // -------------------------------------------------------------------------
        // INSTANCE VARIABLES
        // -------------------------------------------------------------------------

        /** User to return */
        @NotNull @JsonProperty private InternalUser user;

        // -------------------------------------------------------------------------
        // IMPLEMENTATION: ExternallyAuthenticatedAuthFilterFactory
        // -------------------------------------------------------------------------

        @Override
        public <E extends Principal> AuthFilter<?, E> build(
                final PrincipalConverter<E> principalConverter,
                final Authorizer<E> authorizer,
                final Iterable<AuthenticationEventListener> listeners) {
            return new DevAuthFilter.Builder<E>()
                    .setAuthenticator(new DevAuthenticator<>(principalConverter, user, listeners))
                    .setAuthorizer(authorizer)
                    .buildAuthFilter();
        }
    }
}
