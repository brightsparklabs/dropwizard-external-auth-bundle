/*
 * Created by brightSPARK Labs
 * www.brightsparklabs.com
 */

package com.brightsparklabs.dropwizard.bundles.auth.external

import spock.lang.Specification

import javax.ws.rs.core.MultivaluedHashMap
import java.util.function.Function

/**
 * Units tests for {@link HeaderFieldsAuthenticator}.
 *
 * @author brightSPARK Labs
 */
class HeaderFieldsAuthenticatorTest extends Specification {
    def "authenticate success"() {
        given:
        def username = "${firstname}.${lastname}".toString()
        def email = "${username}@email.com".toString()
        def headers = createHeaders(username, firstname, lastname, email, groups, roles)
        def converter = new Function<InternalUser, InternalUser>() {
            @Override
            InternalUser apply(final InternalUser externalUser) {
                return externalUser
            }
        }
        def instance = new HeaderFieldsAuthenticator(converter)

        when:
        def result = instance.authenticate(headers).get()

        then:
        result.username == username
        result.firstname == firstname
        result.lastname == lastname
        result.email == email
        result.groups.join(',') == expectedGroups
        result.roles.join(',') == expectedRoles

        where:
        firstname | lastname | groups              | roles                 | expectedGroups | expectedRoles
        "first"   | "last"   | "g1,g2,g3"          | "r1,r2,r3"            | "g1,g2,g3"     | "r1,r2,r3"
        "first"   | "last"   | " ,  g1,  g2,   g3" | "  r1,   r2,   r3,, " | "g1,g2,g3"     | "r1,r2,r3"
        "first"   | "last"   | null                | "  r1,   r2,   r3,, " | ""             | "r1,r2,r3"
        "first"   | "last"   | " ,  g1,  g2,   g3" | null                  | "g1,g2,g3"     | ""
        "first"   | "last"   | null                | null                  | ""             | ""
    }

    def "authenticate denied"() {
        given:
        def headers = createHeaders(username, firstname, lastname, email, groups, roles)
        def instance = new HeaderFieldsAuthenticator()

        when:
        def result = instance.authenticate(headers)

        then:
        !result.isPresent()

        where:
        username     | firstname | email  | lastname               | groups | roles
        null         | "first"   | "last" | "first.last@email.com" | null   | null
        "first.last" | null      | "last" | "first.last@email.com" | null   | null
        "first.last" | "first"   | null   | "first.last@email.com" | null   | null
        "first.last" | "first"   | "last" | null                   | null   | null
    }

    // ------------------------------------------------------------------------------
    // FIXTURES
    // ------------------------------------------------------------------------------

    def createHeaders(username, firstname, lastname, email, groups, roles) {
        def headers = new MultivaluedHashMap<String, String>()
        headers.with {
            if (username) addAll("X-Auth-Username", username)
            if (firstname) addAll("X-Auth-Given-Name", firstname)
            if (lastname) addAll("X-Auth-Family-Name", lastname)
            if (email) addAll("X-Auth-Email", email)
            if (groups) addAll("X-Auth-Groups", groups)
            if (roles) addAll("X-Auth-Roles", roles)
        }
        return headers
    }
}
