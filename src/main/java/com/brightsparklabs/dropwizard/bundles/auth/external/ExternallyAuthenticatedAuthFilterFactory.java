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

/**
 * Factory for producing an {@link AuthFilter} which authenticates a user based on information
 * passed to it by an external authentication provider.
 */
@JsonTypeInfo(use = Id.NAME, property = "method")
@JsonSubTypes({
        @Type(value = ExternallyAuthenticatedAuthFilterFactory.JwtAuthFilterFactory.class, name = "jwt"),
        @Type(value = ExternallyAuthenticatedAuthFilterFactory.HttpHeadersAuthFilterFactory.class, name = "httpHeaders") })
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
     * @return An {@link AuthFilter} which authenticates a user based on information passed to it by
     * an external authentication provider.
     */
    public abstract AuthFilter<?, ExternalUser> build();

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
    public static class JwtAuthFilterFactory extends ExternallyAuthenticatedAuthFilterFactory
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
        public AuthFilter<?, ExternalUser> build()
        {
            return new OAuthCredentialAuthFilter.
                    Builder<ExternalUser>().setAuthenticator(new JwtAuthenticator(signingKey))
                    .setPrefix("Bearer")
                    .buildAuthFilter();
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
        public AuthFilter<?, ExternalUser> build()
        {
            return new HeaderFieldsAuthFilter.
                    Builder<ExternalUser>().setAuthenticator(new HeaderFieldsAuthenticator())
                    .buildAuthFilter();
        }
    }
}
