/*
 * Created by brightSPARK Labs
 * www.brightsparklabs.com
 */

package com.brightsparklabs.dropwizard.bundles.auth.external;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import io.dropwizard.auth.AuthFilter;
import io.dropwizard.auth.oauth.OAuthCredentialAuthFilter;

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
@JsonSubTypes({
        @Type(value = ExternallyAuthenticatedAuthFilterFactory.JwtAuthFilterFactory.class, name = "jwt"),
        @Type(value = ExternallyAuthenticatedAuthFilterFactory.HttpHeadersAuthFilterFactory.class, name = "httpHeaders"),
        @Type(value = ExternallyAuthenticatedAuthFilterFactory.DevAuthFilterFactory.class, name = "dev") })
public abstract class ExternallyAuthenticatedAuthFilterFactory
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

    /**
     * Returns an {@link AuthFilter} which authenticates a user based on information passed to it by
     * an external authentication provider.
     *
     * @param externalUserToPrincipal
     *         Converts the internal user to the {@link Principal} used in the system.
     * @param <P>
     *         The {@link Principal} the filter should return.
     *
     * @return An {@link AuthFilter} which authenticates a user based on information passed to it by
     * an external authentication provider.
     */
    public abstract <P extends Principal> AuthFilter<?, P> build(
            Function<InternalUser, P> externalUserToPrincipal);

    // -------------------------------------------------------------------------
    // PRIVATE METHODS
    // -------------------------------------------------------------------------

    // -------------------------------------------------------------------------
    // INNER CLASSES
    // -------------------------------------------------------------------------

    /**
     * Factory for producing an {@link AuthFilter} which authenticates a user based on a JWT passed
     * to it by an external authentication provider.
     */
    public static class JwtAuthFilterFactory<P extends Principal>
            extends ExternallyAuthenticatedAuthFilterFactory
    {
        // -------------------------------------------------------------------------
        // INSTANCE VARIABLES
        // -------------------------------------------------------------------------

        /** Public signing key used to sign the JWT */
        @NotNull
        @JsonProperty
        private String signingKey;

        // -------------------------------------------------------------------------
        // IMPLEMENTATION: ExternallyAuthenticatedAuthFilterFactory
        // -------------------------------------------------------------------------

        @Override
        public <E extends Principal> AuthFilter<?, E> build(
                final Function<InternalUser, E> externalUserToPrincipal)
        {
            if (signingKey == null)
            {
                throw new IllegalArgumentException(
                        "signingKey has not been defined in authentication configuration");
            }

            return new OAuthCredentialAuthFilter.
                    Builder<E>().setAuthenticator(new JwtAuthenticator<>(externalUserToPrincipal,
                    signingKey)).setPrefix("Bearer").buildAuthFilter();
        }
    }

    /**
     * Factory for producing an {@link AuthFilter} which authenticates a user based on HTTP headers
     * passed to it by an external authentication provider.
     */
    public static class HttpHeadersAuthFilterFactory
            extends ExternallyAuthenticatedAuthFilterFactory
    {
        // -------------------------------------------------------------------------
        // IMPLEMENTATION: ExternallyAuthenticatedAuthFilterFactory
        // -------------------------------------------------------------------------

        @Override
        public <E extends Principal> AuthFilter<?, E> build(
                final Function<InternalUser, E> externalUserToPrincipal)
        {
            return new HeaderFieldsAuthFilter.
                    Builder<E>().setAuthenticator(new HeaderFieldsAuthenticator<>(
                    externalUserToPrincipal)).buildAuthFilter();
        }
    }

    /**
     * Factory for producing an {@link AuthFilter} which returns a fixed principal. Should only be
     * used for development.
     */
    public static class DevAuthFilterFactory extends ExternallyAuthenticatedAuthFilterFactory
    {
        // -------------------------------------------------------------------------
        // INSTANCE VARIABLES
        // -------------------------------------------------------------------------

        /** User to return */
        @NotNull
        @JsonProperty
        private InternalUser user;

        // -------------------------------------------------------------------------
        // IMPLEMENTATION: ExternallyAuthenticatedAuthFilterFactory
        // -------------------------------------------------------------------------

        @Override
        public <E extends Principal> AuthFilter<?, E> build(
                final Function<InternalUser, E> externalUserToPrincipal)
        {
            if (user == null)
            {
                throw new IllegalArgumentException(
                        "user has not been defined in authentication configuration");
            }

            return new DevAuthFilter.
                    Builder<E>().setAuthenticator(new DevAuthenticator<>(externalUserToPrincipal,
                    user)).buildAuthFilter();
        }
    }
}
