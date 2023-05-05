/*
 * Maintained by brightSPARK Labs.
 * www.brightsparklabs.com
 *
 * Refer to LICENSE at repository root for license details.
 */

package com.brightsparklabs.dropwizard.bundles.auth.external;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

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
import java.util.Set;

/**
 * Authenticates a user based on the presence of a valid JSON Web Token (JWT).
 *
 * @param <P> Type of {@link Principal} to return for authenticated users.
 * @author brightSPARK Labs
 */
public class JwtAuthenticator<P extends Principal> extends ExternalAuthenticator<String, P> {
    // -------------------------------------------------------------------------
    // CONSTANTS
    // -------------------------------------------------------------------------

    private static final String CLAIM_FIELD_USERNAME = "preferred_username";
    private static final String CLAIM_FIELD_FIRSTNAME = "given_name";
    private static final String CLAIM_FIELD_LASTNAME = "family_name";
    private static final String CLAIM_FIELD_EMAIL = "email";
    private static final String CLAIM_FIELD_RESOURCE_ACCESS = "resource_access";
    private static final String CLAIM_FIELD_REALM_ACCESS = "realm_access";

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
     * @param principalConverter Converter between {@link InternalUser} and the {@link Principal}
     *     used in the system.
     * @param signingKey signing key to use to validate tokens.
     */
    JwtAuthenticator(
            final PrincipalConverter<P> principalConverter,
            final String signingKey,
            final Iterable<AuthenticationEventListener> listeners) {
        super(principalConverter, listeners);
        final X509EncodedKeySpec spec = new X509EncodedKeySpec(Decoders.BASE64.decode(signingKey));
        Key key = null;
        try {
            key = KeyFactory.getInstance("RSA").generatePublic(spec);
        } catch (Exception ex) {
            logger.error("Could not process public key", ex);
        }
        jwtParser = Jwts.parser().setSigningKey(key);
    }

    // -------------------------------------------------------------------------
    // IMPLEMENTATION:  ExternalAuthenticator
    // -------------------------------------------------------------------------

    @Override
    public InternalUser doAuthenticate(final String jwt)
            throws AuthenticationException, AuthenticationDeniedException {
        logger.info("Authenticating via JWT [{}] ...", jwt);
        final Jws<Claims> jws;
        try {
            jws = jwtParser.parseClaimsJws(jwt);
        } catch (JwtException ex) {
            logger.info("Authentication failed - JWT is invalid [{}]", ex.getMessage());
            throw new AuthenticationException(ex);
        }

        final Claims claims = jws.getBody();
        logger.info("JWT contains: {}", claims);

        // extract groups and roles
        final ImmutableList<String> roles = getRoles(claims);

        try {
            final ImmutableInternalUser user =
                    ImmutableInternalUser.builder()
                            .username(extractClaimsValue(CLAIM_FIELD_USERNAME, claims))
                            .firstname(extractClaimsValue(CLAIM_FIELD_FIRSTNAME, claims))
                            .lastname(extractClaimsValue(CLAIM_FIELD_LASTNAME, claims))
                            .email(Optional.ofNullable(claims.get(CLAIM_FIELD_EMAIL, String.class)))
                            // TODO: extract groups
                            .groups(ImmutableSet.of())
                            .roles(ImmutableSet.copyOf(roles))
                            .logoutUrl(claims.getIssuer() + "/protocol/openid-connect/logout")
                            .build();
            logger.info("Authentication successful for username [{}]", user.getUsername());
            return user;
        } catch (IllegalArgumentException ex) {
            final String errorMessage =
                    String.format(
                            "Authentication denied for JWT [%s] - %s",
                            claims.toString(), ex.getMessage());
            logger.info(errorMessage);
            throw new AuthenticationDeniedException(errorMessage);
        }
    }

    // -------------------------------------------------------------------------
    // PUBLIC METHODS
    // -------------------------------------------------------------------------

    // -------------------------------------------------------------------------
    // PRIVATE METHODS
    // -------------------------------------------------------------------------

    private String extractClaimsValue(final String claimField, final Claims claims)
            throws IllegalArgumentException {
        final String result = claims.get(claimField, String.class);
        if (Strings.isNullOrEmpty(result)) {
            throw new IllegalArgumentException(
                    "JWT did not contain valid claim field [" + claimField + "]");
        }
        return result;
    }

    private ImmutableList<String> getRoles(final Claims claims) {
        // Roles come from realm_access and resource_access

        final Set<String> roles = Sets.newHashSet();

        // resource_access contains the roles for all the appropriate clients.
        @SuppressWarnings("unchecked")
        final Map<String, Map<String, List<String>>> resource =
                claims.get(CLAIM_FIELD_RESOURCE_ACCESS, Map.class);

        resource.values().stream() //
                .map(this::getRolesFrom) //
                .forEach(roles::addAll);

        @SuppressWarnings("unchecked")
        final Map<String, List<String>> realm = claims.get(CLAIM_FIELD_REALM_ACCESS, Map.class);

        roles.addAll(getRolesFrom(realm));

        return ImmutableList.copyOf(roles);
    }

    private ImmutableList<String> getRolesFrom(final Map<String, List<String>> container) {
        return ImmutableList.copyOf(container.getOrDefault("roles", ImmutableList.of()));
    }
}
