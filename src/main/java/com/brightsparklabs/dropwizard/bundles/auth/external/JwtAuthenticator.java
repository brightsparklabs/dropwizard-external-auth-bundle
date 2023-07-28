/*
 * Maintained by brightSPARK Labs.
 * www.brightsparklabs.com
 *
 * Refer to LICENSE at repository root for license details.
 */

package com.brightsparklabs.dropwizard.bundles.auth.external;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
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

    /*
     * The claim names below are taken from: https://www.iana.org/assignments/jwt/jwt.xhtml#claims
     */

    /** Name of the claim containing the username of the user. */
    private static final String CLAIM_FIELD_USERNAME = "preferred_username";

    /** Name of the claim containing the firstname of the user. */
    private static final String CLAIM_FIELD_FIRSTNAME = "given_name";

    /** Name of the claim containing the lastname of the user. */
    private static final String CLAIM_FIELD_LASTNAME = "family_name";

    /** Name of the claim containing the email of the user. */
    private static final String CLAIM_FIELD_EMAIL = "email";

    /** Name of the claim containing the roles of the user. */
    private static final String CLAIM_FIELD_ROLES = "roles";

    /** Name of the claim containing the groups the user is a member of. */
    private static final String CLAIM_FIELD_GROUPS = "groups";

    /*
     * The claim names below are specific to Keycloak. Included for backwards compatibility.
     */

    /** Name of the claim containing the realm roles of the user. */
    private static final String CLAIM_FIELD_REALM_ACCESS = "realm_access";

    /** Name of the claim containing the client roles of the user. */
    private static final String CLAIM_FIELD_RESOURCE_ACCESS = "resource_access";

    // -------------------------------------------------------------------------
    // CLASS VARIABLES
    // -------------------------------------------------------------------------

    /** Class logger */
    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticator.class);

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
        jwtParser = Jwts.parserBuilder().setSigningKey(key).build();
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

        try {
            final InternalUser user =
                    ImmutableInternalUser.builder()
                            .username(extractClaimsValue(CLAIM_FIELD_USERNAME, claims))
                            .firstname(extractClaimsValue(CLAIM_FIELD_FIRSTNAME, claims))
                            .lastname(extractClaimsValue(CLAIM_FIELD_LASTNAME, claims))
                            .email(Optional.ofNullable(claims.get(CLAIM_FIELD_EMAIL, String.class)))
                            .groups(getGroups(claims))
                            .roles(getRoles(claims))
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

    /**
     * Returns the value of the specified claim as a String.
     *
     * @param claims The claims associated with the user.
     * @return The value of the specified claim as a String.
     */
    private String extractClaimsValue(final String claimField, final Claims claims)
            throws IllegalArgumentException {
        final String result = claims.get(claimField, String.class);
        if (Strings.isNullOrEmpty(result)) {
            throw new IllegalArgumentException(
                    "JWT did not contain valid claim field [" + claimField + "]");
        }
        return result;
    }

    /**
     * Returns the user's groups from the {@value #CLAIM_FIELD_GROUPS} claim.
     *
     * @param claims The claims associated with the user.
     * @return The user's groups from the {@value #CLAIM_FIELD_GROUPS} claim.
     */
    private ImmutableList<String> getGroups(final Claims claims) {
        @SuppressWarnings("unchecked")
        final List<String> groups = claims.get(CLAIM_FIELD_GROUPS, List.class);
        return groups == null ? ImmutableList.of() : ImmutableList.copyOf(groups);
    }

    /**
     * Returns the user's roles from the {@value #CLAIM_FIELD_ROLES} claim from:
     *
     * <ol>
     *   <li>The supplied claims from the root.
     *   <li>Within the {@value #CLAIM_FIELD_REALM_ACCESS} claim.
     *   <li>Within the {@value #CLAIM_FIELD_RESOURCE_ACCESS} claim.
     * </ol>
     *
     * >
     *
     * @param claims The claims associated with the user.
     * @return The user's groups from the {@value #CLAIM_FIELD_GROUPS} claim.
     */
    private ImmutableSet<String> getRoles(final Claims claims) {
        @SuppressWarnings("unchecked")
        final List<String> directRoles = claims.get(CLAIM_FIELD_ROLES, List.class);
        final Set<String> roles = Sets.newHashSet();
        Optional.ofNullable(directRoles).ifPresent(roles::addAll);

        /* Keycloak nested roles under other claims. Include them as well for backwards
         * compatibility of this library.
         */

        @SuppressWarnings("unchecked")
        final Map<String, List<String>> realmRoles =
                claims.get(CLAIM_FIELD_REALM_ACCESS, Map.class);
        Optional.ofNullable(realmRoles).map(this::getRolesFrom).ifPresent(roles::addAll);

        @SuppressWarnings("unchecked")
        final Map<String, Map<String, List<String>>> clientRoles =
                claims.get(CLAIM_FIELD_RESOURCE_ACCESS, Map.class);
        Optional.ofNullable(clientRoles).orElse(ImmutableMap.of()).values().stream()
                .map(this::getRolesFrom)
                .forEach(roles::addAll);

        return ImmutableSet.copyOf(roles);
    }

    /**
     * Returns the user's roles from the {@value #CLAIM_FIELD_ROLES} field.
     *
     * @param container The container which contains the roles claim.
     * @return The user's roles from the {@value #CLAIM_FIELD_ROLES} field.
     */
    private ImmutableList<String> getRolesFrom(final Map<String, List<String>> container) {
        return ImmutableList.copyOf(container.getOrDefault(CLAIM_FIELD_ROLES, ImmutableList.of()));
    }
}
