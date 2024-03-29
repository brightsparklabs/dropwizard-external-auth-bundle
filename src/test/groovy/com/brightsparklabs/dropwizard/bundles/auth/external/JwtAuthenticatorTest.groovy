/*
 * Maintained by brightSPARK Labs.
 * www.brightsparklabs.com
 *
 * Refer to LICENSE at repository root for license details.
 */

package com.brightsparklabs.dropwizard.bundles.auth.external


import com.google.common.collect.ImmutableSet
import io.jsonwebtoken.Jwts
import spock.lang.Specification
import spock.lang.Unroll

import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.PrivateKey

/**
 * Unit tests for {@link JwtAuthenticator}.
 *
 * @author brightSPARK Labs
 */
class JwtAuthenticatorTest extends Specification {

    final PrincipalConverter principalConverter = new IdentityPrincipalConverter();

    // -----------------------------------------------------------------------------
    // TESTS
    // -----------------------------------------------------------------------------

    def "DoAuthenticate"() {
        given:
        String key = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAmKF1iO+fjks9Oi69UezToxw/jNvYmvxKnBt6eBX12j+RYkvnF1dnufZ7y4JNloaIgb8m/piV5TAu/rF3ICsniKRKO2xOfiS/EJfguALnDyA2Ko6tHYjsdmzbL3+tKd/oXjFXbL59I2hYbxo3+gi16cCzvdI6jD91OJJ9BEFGSKvkmxXsvYNzMsEHfsiflyLTiUYHLOLVRXfT3AYP67nF2gVSkUmkNV1Gn7EyORTw2VoAJF3uI+J6cGivCGoi7ClisCLxUYgFfA8jvRzjLkkbeIvqlW3lnh+qGRwNw73fum8UqVs/Y6rOYfDfAHCO1IieroqnuUn2j6f1eyhjkZlXjwIDAQAB"
        def authenticator = new JwtAuthenticator<InternalUser>(principalConverter, key, [])

        // Hard coded, manually made using keycloak, with a long expiry (sometime in 2030).
        String jwt = "eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJGNWVxSUlSR1gwT2hIUmMtYl9TT0hGb2ZQbWVxNlcydUJaVVgxYWQxd0NjIn0.eyJqdGkiOiIyZmNkYmZmYy0wMzZhLTQzNWItODMzNy1hMGE2Njk3ZWE3ZGQiLCJleHAiOjE5MTgxODI3NzUsIm5iZiI6MCwiaWF0IjoxNTcyNTg2OTE0LCJpc3MiOiJodHRwOi8vbG9jYWxob3N0OjgwOTAvYXV0aC9yZWFsbXMvb3Ntb3NpcyIsImF1ZCI6WyJvc21vc2lzIiwiYWNjb3VudCJdLCJzdWIiOiIwMjk5ODEzYy1kY2YyLTQ1YWYtYjE4Ny02YmU4NTVlYjVmZjYiLCJ0eXAiOiJCZWFyZXIiLCJhenAiOiJvc21vc2lzIiwiYXV0aF90aW1lIjoxNTcyNTg2NzExLCJzZXNzaW9uX3N0YXRlIjoiMjUyM2ExMzQtYmQ4NC00ZjEwLWE5MzMtNjNmYzY2YWU3Y2FjIiwiYWNyIjoiMCIsImFsbG93ZWQtb3JpZ2lucyI6WyJodHRwOi8vbG9jYWxob3N0OjgwODAiXSwicmVhbG1fYWNjZXNzIjp7InJvbGVzIjpbIm9mZmxpbmVfYWNjZXNzIiwidW1hX2F1dGhvcml6YXRpb24iLCJBRE1JTiJdfSwicmVzb3VyY2VfYWNjZXNzIjp7Im9zbW9zaXMiOnsicm9sZXMiOlsiQ2xpZW50Um9sZSJdfSwiYWNjb3VudCI6eyJyb2xlcyI6WyJtYW5hZ2UtYWNjb3VudCIsIm1hbmFnZS1hY2NvdW50LWxpbmtzIiwidmlldy1wcm9maWxlIl19fSwic2NvcGUiOiJwcm9maWxlIGVtYWlsIiwiZW1haWxfdmVyaWZpZWQiOmZhbHNlLCJuYW1lIjoiYWRtaW4gYWRtaW4iLCJwcmVmZXJyZWRfdXNlcm5hbWUiOiJhZG1pbiIsImdpdmVuX25hbWUiOiJhZG1pbiIsImZhbWlseV9uYW1lIjoiYWRtaW4iLCJlbWFpbCI6ImFkbWluQGVtYWlsLmNvbSJ9.bjUt_5QMyKvcsTRwPQwBkW22n_6-0HZV3ecMumEYVl4vUM8fDm3nNymjbsELPo0A2oH5bPejtIBCw7Wma_f2PKsFOeS0pOfy2YzNkbmwQRPFVkCRMLwsaz9buNxwSYU7KU2NCzRRLoOlOtD4J0kYQquktuuunMFprTwwxCkAiWONlFDg2dkXM9B6SWVhlF511dZbVMppD0_Rk2e6jAx2Uef1CzSddAlZkV7VM_3Q7hHAy0FTDoBHh70MYY9jtaTxCXU8ws3W81BUp0RCvcBSW-_IB6u3M6GvreHGtMGcXtsx1ZjpcZcFPN0Diccsuvv328eP-R3K6zDorTaRzmEuYg"

        when:
        InternalUser user = authenticator.doAuthenticate(jwt)

        then:
        user.getRoles() == ImmutableSet.copyOf([
            // Realm roles.
            "offline_access",
            "uma_authorization",
            "ADMIN",
            // Client roles - osmosis.
            "ClientRole",
            // Client roles - account.
            "manage-account",
            "manage-account-links",
            "view-profile"
        ])
        user.getLastname() == "admin"
        user.getFirstname() == "admin"
        user.getUsername() == "admin"
        user.getEmail().get() == "admin@email.com"
    }

    def "doAuthenticateNoEmail"() {
        given:
        String key = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAty2VYUBIBOhYV6fgTt/yvTQZ9SUX7L+TIUZqcknAJ4LO/LTG5QbGgKiKRwb5F8vDDTRhqAOB2QrweTDCLRk3920zTCrvCYILTN9RWx9a+w1zFRTCkeDN3d6ekFg+FxkKsMROtDEIXktdNdbhwYwHnY8n1drxW9fNRsPFfMsO0/zI65g5f0I0UsX1JDWKIoNOZpWxSMCL9OcRFo3luPonw9gIr0c5G47O7PX+CUcx3G+VTvoftrNbGXa1s3J76/g/S0EbnwtpXSpUqn5nGKp6QvdGDlbkmfJDi+vgD6FK+WaNttlac+o1sxaxi20ZmEXnd3iJ6okvqjURtCchKsocPQIDAQAB"
        def authenticator = new JwtAuthenticator<InternalUser>(principalConverter, key, [])

        // Hard coded, manually made using keycloak, with a long expiry (sometime in 2030).
        String jwt = "eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJVeEh2ZUtxRDk4ay1aRlhMY2poNWsyQzdJZEZ4cFZ6NUU0cXpHbFR3NV9VIn0.eyJqdGkiOiIxY2VlYjViZS03MDlmLTQ5NzUtYjcwOC1jY2QwYWUyZDk0ZmUiLCJleHAiOjE4NjU2NDMxNzYsIm5iZiI6MCwiaWF0IjoxNTc3NjcyMTc5LCJpc3MiOiJodHRwOi8vbG9jYWxob3N0OjgwOTAvYXV0aC9yZWFsbXMvbWFzdGVyIiwiYXVkIjoiYWNjb3VudCIsInN1YiI6IjJkNzNkNDE4LWMzNzItNDM5NS1hOTMzLTRmOTc5OWZhOTQ2ZCIsInR5cCI6IkJlYXJlciIsImF6cCI6Im9zbW9zaXMiLCJhdXRoX3RpbWUiOjE1Nzc2NzE5NzYsInNlc3Npb25fc3RhdGUiOiJiMGNhZGQzZi1jMWZlLTQwMmItOTI5Zi1kOTJkODA2ZjRiZGEiLCJhY3IiOiIwIiwicmVhbG1fYWNjZXNzIjp7InJvbGVzIjpbIlRFU1QiLCJvZmZsaW5lX2FjY2VzcyIsInVtYV9hdXRob3JpemF0aW9uIl19LCJyZXNvdXJjZV9hY2Nlc3MiOnsiYWNjb3VudCI6eyJyb2xlcyI6WyJtYW5hZ2UtYWNjb3VudCIsIm1hbmFnZS1hY2NvdW50LWxpbmtzIiwidmlldy1wcm9maWxlIl19fSwic2NvcGUiOiJwcm9maWxlIGVtYWlsIiwiZW1haWxfdmVyaWZpZWQiOmZhbHNlLCJuYW1lIjoibm8gZW1haWwiLCJwcmVmZXJyZWRfdXNlcm5hbWUiOiJub2VtYWlsIiwiZ2l2ZW5fbmFtZSI6Im5vIiwiZmFtaWx5X25hbWUiOiJlbWFpbCJ9.Uu8mS2VOzHyvUG7qSYMmIvUmEEhLNb-1wBwOg2CrCRHOojf2PXp6kYs436uxJgJUET4lWnB8RVJR6VzGY7-6grdS-60AUX7-a5U4xzJ1_uh_MnFBdhWv_kuyZO6Hh1BfkC54RPo1QdxNLYDYX6hcOYs1KTi8t4psPj0MLyp74TN-vv_DOhaBK_COuYaNzs1m9RCDJFv78uNwCblYa8UKdtotdyxNLd2QIy0px5fAasw_Mmiizjqug0GRVY7RFx6TeRabus44WWMTtBSFWBKFbO8ml7oCgQ4xUuR8IMXseUwRSlG258HBv5vZaXuMygyDxOfUk1cQX-prMH0Sz4vF6Q"


        when:
        InternalUser user = authenticator.doAuthenticate(jwt)

        then:
        user.getRoles() == ImmutableSet.copyOf([
            // Realm roles.
            "TEST",
            "offline_access",
            "uma_authorization",
            // Client roles.
            "manage-account",
            "manage-account-links",
            "view-profile"
        ])
        user.getLastname() == "email"
        user.getFirstname() == "no"
        user.getUsername() == "noemail"
        !user.getEmail().isPresent()
    }

    @Unroll
    def "testClaims #description"() {
        given:
        final KeyPair keyPair = createKeyPair()
        final publicKey = Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded())
        def authenticator = new JwtAuthenticator<InternalUser>(principalConverter, publicKey, [])

        final String jwt =
                createJwt("test.user", "Test", "User", "test.user@test.test",
                groups,
                roles,
                realmRoles,
                clientRoles,
                keyPair.getPrivate()
                );

        when:
        InternalUser user = authenticator.doAuthenticate(jwt)

        then:
        user.name == "Test User"
        user.firstname == "Test"
        user.lastname == "User"
        user.groups == ImmutableSet.copyOf(groups ?: [])

        def allClientRoles = clientRoles?.values()?.flatten() ?: []
        user.roles == ImmutableSet.builder()
                .addAll(roles ?: [])
                .addAll(realmRoles ?: [])
                .addAll(allClientRoles).build()

        // spotless:off
        where:
        description       | groups       | roles                             | realmRoles            | clientRoles
        "wth groups"      | ["G1", "G2"] | null                              | null                  | null
        "wth roles"       | null         | ["R1", "R2", "GR1", "GR2", "GR3"] | null                  | null
        "wth realmRoles"  | null         | null                              | ["RR1", "RR2", "GR1"] | null
        "wth clientRoles" | null         | null                              | null                  |
                [
                        client1: ["C1R1", "C1R2", "GR2", "COMMON_CLIENT"],
                        client2: ["C2R1", "C2R2", "GR3", "COMMON_CLIENT"],
                ] as Map<String, List<String>>;

        "wth groups+roles+realmRoles+clientRoles" |
                ["G1", "G2"]                |
                ["R1", "R2", "GR1", "GR2", "GR3"] |
                ["RR1", "RR2", "GR1"]               |
                [
                        client1: ["C1R1", "C1R2", "GR2", "COMMON_CLIENT"],
                        client2: ["C2R1", "C2R2", "GR3", "COMMON_CLIENT"],
                ] as Map<String, List<String>>;
        // spotless:on
    }


    // -----------------------------------------------------------------------------
    // FIXTURES
    // -----------------------------------------------------------------------------

    /**
     * @return A nre RSA 2048-bit key pair.
     */
    def createKeyPair() {
        final KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA")
        generator.initialize(2048)
        return generator.generateKeyPair()
    }

    /**
     * Returns a new JWT containing the supplied information and signed with the provided private key.
     *
     * @param username Username to include.
     * @param firstname Firstname to include.
     * @param lastname Last to include.
     * @param email Email to include.
     * @param groups Group memberships to include.
     * @param roles Roles to include.
     * @param realmRoles Realm roles (Keycloak) to include.
     * @param clientRoles Client roles (Keycloak) to include.
     * @param privateKey Private key to sign the JWT with.
     * @return The newly created JWT.
     */
    String createJwt(String username,
            String firstname, String lastname, String email, List<String> groups,
            List<String> roles, List<String> realmRoles = [], Map<String,
            List<String>> clientRoles = [:], PrivateKey privateKey) {

        Map<String, Object> claims = [
            (JwtAuthenticator.CLAIM_FIELD_USERNAME) : username,
            (JwtAuthenticator.CLAIM_FIELD_FIRSTNAME): firstname,
            (JwtAuthenticator.CLAIM_FIELD_LASTNAME) : lastname,
            (JwtAuthenticator.CLAIM_FIELD_EMAIL)    : email,
        ]

        if (groups != null) {
            claims[JwtAuthenticator.CLAIM_FIELD_GROUPS] = groups
        }

        if (roles != null) {
            claims[JwtAuthenticator.CLAIM_FIELD_ROLES] = roles
        }

        if (realmRoles != null) {
            claims[JwtAuthenticator.CLAIM_FIELD_REALM_ACCESS] = [roles: realmRoles]
        }

        if (clientRoles != null) {
            /*
             * Transforms:
             *
             *   [
             *      client1: ["ROLE1", "ROLE2"],
             *      client2: ["ROLE3", "ROLE4"],
             *   ]
             *
             * Into:
             *
             *   [
             *      client1:
             *        roles: ["ROLE1", "ROLE2"],
             *      client2:
             *        roles: ["ROLE3", "ROLE4"],
             *   ]
             */
            def reshapeClientRoles =
                    clientRoles
                    .collectEntries { entry -> [(entry.key): [roles: entry.value]] }
            claims[JwtAuthenticator.CLAIM_FIELD_RESOURCE_ACCESS] = reshapeClientRoles
        }


        final String jwt = Jwts.builder()
                .setClaims(claims)
                .signWith(privateKey)
                .compact()
        return jwt
    }
}