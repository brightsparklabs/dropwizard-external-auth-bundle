/*
 * Created by brightSPARK Labs in 2020.
 * www.brightsparklabs.com
 *
 * Refer to LICENSE at repository root for license details.
 */

package com.brightsparklabs.dropwizard.bundles.auth.external

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings
import io.dropwizard.auth.AuthenticationException
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
        def listener = Mock(AuthenticationEventListener.class)

        def username = "${firstname}.${lastname}".toString()
        def email = "${username}@email.com".toString()
        def headers = createHeaders(username, firstname, lastname, email, groups, roles)
        def converter = Function.identity()
        def instance = new HeaderFieldsAuthenticator(converter, [listener])

        when:
        def result = instance.authenticate(headers).get()

        then:
        result.username == username
        result.firstname == firstname
        result.lastname == lastname
        result.email == Optional.of(email)
        result.groups.join(',') == expectedGroups
        result.roles.join(',') == expectedRoles
        1 * listener.onAuthenticationSuccess(*_)

        where:
        firstname | lastname | groups              | roles                 | expectedGroups | expectedRoles
        "first"   | "last"   | "g1,g2,g3"          | "r1,r2,r3"            | "g1,g2,g3"     | "r1,r2,r3"
        "first"   | "last"   | " ,  g1,  g2,   g3" | "  r1,   r2,   r3,, " | "g1,g2,g3"     | "r1,r2,r3"
        "first"   | "last"   | null                | "  r1,   r2,   r3,, " | ""             | "r1,r2,r3"
        "first"   | "last"   | " ,  g1,  g2,   g3" | null                  | "g1,g2,g3"     | ""
        "first"   | "last"   | null                | null                  | ""             | ""
    }

    def "authenticate success no email"() {
        given:
        def listener = Mock(AuthenticationEventListener.class)

        def firstname = "first"
        def lastname = "last"
        def username = "${firstname}.${lastname}".toString()
        def email = null
        def headers = createHeaders(username, firstname, lastname, email, null, null)
        def converter = Function.identity()
        def instance = new HeaderFieldsAuthenticator(converter, [listener])

        when:
        def result = instance.authenticate(headers).get()

        then:
        result.username == username
        result.firstname == firstname
        result.lastname == lastname
        result.email == Optional.ofNullable(email)
        result.groups.join(',') == ""
        result.roles.join(',') == ""
        1 * listener.onAuthenticationSuccess(*_)
    }

    def "authenticate denied"() {
        given:
        def listener = Mock(AuthenticationEventListener.class)

        def headers = createHeaders(username, firstname, lastname, email, groups, roles)
        def converter = Function.identity()
        def instance = new HeaderFieldsAuthenticator(converter, [listener])

        when:
        def result = instance.authenticate(headers)

        then:
        !result.isPresent()
        1 * listener.onAuthenticationDenied(*_)

        where:
        username     | firstname | email  | lastname               | groups | roles
        null         | "first"   | "last" | "first.last@email.com" | null   | null
        "first.last" | null      | "last" | "first.last@email.com" | null   | null
        "first.last" | "first"   | "last" | null                   | null   | null
    }

    def "authenticate invalid"() {
        given:
        def listener = Mock(AuthenticationEventListener.class)

        def converter = Function.identity()
        def instance = new HeaderFieldsAuthenticator(converter, [listener])

        when:
        instance.authenticate(null)

        then:
        thrown AuthenticationException
        1 * listener.onAuthenticationError(*_)
    }

    // ------------------------------------------------------------------------------
    // FIXTURES
    // ------------------------------------------------------------------------------

    @SuppressFBWarnings("SE_NO_SERIALVERSIONID")
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
