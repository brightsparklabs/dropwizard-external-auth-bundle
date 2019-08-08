/*
 * Created by brightSPARK Labs
 * www.brightsparklabs.com
 */

package com.brightsparklabs.dropwizard.bundles.auth.external;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import io.dropwizard.auth.AuthenticationException;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.Key;
import java.security.KeyFactory;
import java.security.Principal;
import java.security.spec.X509EncodedKeySpec;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

/**
 * Authenticates a user based on the presence of a valid JSON Web Token (JWT).
 *
 * @param <P>
 *         Type of {@link Principal} to return for authenticated users.
 *
 * @author brightSPARK Labs
 */
public class JwtAuthenticator<P extends Principal> extends ExternalAuthenticator<String, P>
{
    // -------------------------------------------------------------------------
    // CONSTANTS
    // -------------------------------------------------------------------------

    // -------------------------------------------------------------------------
    // CLASS VARIABLES
    // -------------------------------------------------------------------------

    /** Class logger */
    private static Logger logger = LoggerFactory.getLogger(JwtAuthenticator.class);

    // -------------------------------------------------------------------------
    // INSTANCE VARIABLES
    // -------------------------------------------------------------------------

    /** Parser for validating tokens */
    private final JwtParser jwtParser;

    // -------------------------------------------------------------------------
    // CONSTRUCTION
    // -------------------------------------------------------------------------

    /**
     * Creates a new authenticator which validates JWTs using the specified public signing key. This
     * should be the signing key of the Identity Provider who signed the JWT.
     *
     * @param externalUserToPrincipal
     *         Converts the internal user to the {@link Principal} used in the system.
     * @param signingKey
     *         signing key to use to validate tokens.
     */
    JwtAuthenticator(final Function<InternalUser, P> externalUserToPrincipal,
            final String signingKey)
    {
        super(externalUserToPrincipal);
        final X509EncodedKeySpec spec = new X509EncodedKeySpec(Decoders.BASE64.decode(signingKey));
        Key key = null;
        try
        {
            key = KeyFactory.getInstance("RSA").generatePublic(spec);
        }
        catch (Exception ex)
        {
            logger.error("Could not process public key", ex);
        }
        jwtParser = Jwts.parser().setSigningKey(key);
    }

    // -------------------------------------------------------------------------
    // IMPLEMENTATION:  ExternalAuthenticator
    // -------------------------------------------------------------------------

    @Override
    public Optional<InternalUser> doAuthenticate(final String jwt) throws AuthenticationException
    {
        logger.info("Authenticating via JWT [{}] ...", jwt);
        final Jws<Claims> jws;
        try
        {
            jws = jwtParser.parseClaimsJws(jwt);
        }
        catch (JwtException ex)
        {
            logger.info("Authentication denied - JWT is invalid [{}]", ex.getMessage());
            return Optional.empty();
        }

        final Claims claims = jws.getBody();
        logger.info("JWT contains: {}", claims);

        // extract groups and roles
        @SuppressWarnings("unchecked")
        final Map<String, Object> account = (Map<String, Object>) claims.get("resource_access",
                Map.class).get("account");
        @SuppressWarnings("unchecked")
        final List<String> roles = (List<String>) account.getOrDefault("roles", ImmutableList.of());

        final ImmutableInternalUser user = ImmutableInternalUser.builder()
                .firstname(claims.get("given_name", String.class))
                .lastname(claims.get("family_name", String.class))
                .username(claims.get("preferred_username", String.class))
                .email(claims.get("email", String.class))
                // TODO: extract groups
                .groups(ImmutableSet.of())
                .roles(ImmutableSet.copyOf(roles))
                .build();
        logger.info("Authentication successful for username [{}]", user.getUsername());
        return Optional.of(user);
    }

    // -------------------------------------------------------------------------
    // PUBLIC METHODS
    // -------------------------------------------------------------------------

    // -------------------------------------------------------------------------
    // PRIVATE METHODS
    // -------------------------------------------------------------------------
}