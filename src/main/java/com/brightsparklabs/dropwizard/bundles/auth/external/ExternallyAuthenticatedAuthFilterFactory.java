/*
 * Created by brightSPARK Labs
 * www.brightsparklabs.com
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
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.NotNull;
import java.security.Principal;
import java.util.function.Function;

/**
 * Factory for producing an {@link AuthFilter} which authenticates a user based on information
 * passed to it by an external authentication provider.
 * <p>
 * This will be created by Dropwizard + Jackson.
 */
@JsonTypeInfo(use = Id.NAME, property = "method")
public abstract class ExternallyAuthenticatedAuthFilterFactory implements Discoverable
{

    // -------------------------------------------------------------------------
    // PUBLIC METHODS
    // -------------------------------------------------------------------------

    /**
     * Returns an {@link AuthFilter} which authenticates a user based on information passed to it by
     * an external authentication provider.
     *
     * @param externalUserToPrincipal Converts the internal user to the {@link Principal} used in
     *                                the system.
     * @param authorizer              The {@link Authorizer} to use.
     * @param <P>                     The {@link Principal} the filter should return.
     * @return An {@link AuthFilter} which authenticates a user based on information passed to it by
     * an external authentication provider.
     */
    public abstract <P extends Principal> AuthFilter<?, P> build(
            Function<InternalUser, P> externalUserToPrincipal, Authorizer<P> authorizer);


    // -------------------------------------------------------------------------
    // INNER CLASSES
    // -------------------------------------------------------------------------

    /**
     * Factory for producing an {@link AuthFilter} which authenticates a user based on a JWT passed
     * to it by an external authentication provider.
     */
    @JsonTypeName("jwt")
    public static class JwtAuthFilterFactory<P extends Principal>
            extends ExternallyAuthenticatedAuthFilterFactory
    {
        // -------------------------------------------------------------------------
        // INSTANCE VARIABLES
        // -------------------------------------------------------------------------

        /** Public signing key used to sign the JWT */
        @NotEmpty
        @JsonProperty
        private String signingKey;

        // -------------------------------------------------------------------------
        // IMPLEMENTATION: ExternallyAuthenticatedAuthFilterFactory
        // -------------------------------------------------------------------------

        @Override
        public <E extends Principal> AuthFilter<?, E> build(
                final Function<InternalUser, E> externalUserToPrincipal,
                final Authorizer<E> authorizer)
        {
            if (signingKey == null)
            {
                throw new IllegalArgumentException(
                        "signingKey has not been defined in authentication configuration");
            }

            return new OAuthCredentialAuthFilter.Builder<E>() //
                    .setAuthenticator(new JwtAuthenticator<>(externalUserToPrincipal, signingKey))
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
            extends ExternallyAuthenticatedAuthFilterFactory
    {
        // -------------------------------------------------------------------------
        // IMPLEMENTATION: ExternallyAuthenticatedAuthFilterFactory
        // -------------------------------------------------------------------------

        @Override
        public <E extends Principal> AuthFilter<?, E> build(
                final Function<InternalUser, E> externalUserToPrincipal,
                final Authorizer<E> authorizer)
        {
            return new HeaderFieldsAuthFilter.Builder<E>() //
                    .setAuthenticator(new HeaderFieldsAuthenticator<>(externalUserToPrincipal)) //
                    .setAuthorizer(authorizer) //
                    .buildAuthFilter();
        }
    }

    /**
     * Factory for producing an {@link AuthFilter} which delegates to a chain of others. Wrapper
     * around https://www.dropwizard.io/en/stable/manual/auth.html#chained-factories
     */
    @JsonTypeName("chained")
    public static class ChainedAuthFilterFactory<P extends Principal>
            extends ExternallyAuthenticatedAuthFilterFactory
    {

        // -------------------------------------------------------------------------
        // INSTANCE VARIABLES
        // -------------------------------------------------------------------------

        /** The AuthFilters that this will delegate to */
        @NotEmpty
        @JsonProperty
        private ImmutableList<ExternallyAuthenticatedAuthFilterFactory> delegates;

        // -------------------------------------------------------------------------
        // IMPLEMENTATION: ExternallyAuthenticatedAuthFilterFactory
        // -------------------------------------------------------------------------

        @SuppressWarnings("unchecked")
        @Override
        public <E extends Principal> AuthFilter<?, E> build(
                final Function<InternalUser, E> externalUserToPrincipal,
                final Authorizer<E> authorizer)
        {
            final ImmutableList<AuthFilter> authFilters = delegates.stream()
                    .map(d -> d.build(externalUserToPrincipal, authorizer))
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
    public static class DevAuthFilterFactory extends ExternallyAuthenticatedAuthFilterFactory
    {
        // -------------------------------------------------------------------------
        // INSTANCE VARIABLES
        // -------------------------------------------------------------------------

        /**
         * User to return
         */
        @NotNull
        @JsonProperty
        private InternalUser user;

        // -------------------------------------------------------------------------
        // IMPLEMENTATION: ExternallyAuthenticatedAuthFilterFactory
        // -------------------------------------------------------------------------

        @Override
        public <E extends Principal> AuthFilter<?, E> build(
                final Function<InternalUser, E> externalUserToPrincipal,
                final Authorizer<E> authorizer)
        {
            return new DevAuthFilter.Builder<E>() //
                    .setAuthenticator(new DevAuthenticator<>(externalUserToPrincipal, user)) //
                    .setAuthorizer(authorizer) //
                    .buildAuthFilter();
        }
    }
}
