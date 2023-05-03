/*
 * Maintained by brightSPARK Labs.
 * www.brightsparklabs.com
 *
 * Refer to LICENSE at repository root for license details.
 */

package com.brightsparklabs.dropwizard.bundles.auth.external

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings
import spock.lang.Specification

import java.security.Principal

/**
 * Units tests for {@link ExternallyAuthenticatedAuthBundleTest}.
 *
 * @author brightSPARK Labs
 */
@SuppressFBWarnings("SE_NO_SERIALVERSIONID")
class ExternallyAuthenticatedAuthBundleTest extends Specification {

    /** Empty test class representing the principal */
    abstract class TestUserPrincipal implements Principal{}

    /** Empty test class representing the authentication listener */
    class TestAuthenticationListener implements AuthenticationEventListener{}

    final PrincipalConverter<TestUserPrincipal> principalConverter = new IdentityPrincipalConverter()

    def initialiseWithListeners() {
        given:
        TestAuthenticationListener listener = new TestAuthenticationListener();

        when: 'Initialising the bundle with listeners'
        def bundle = new ExternallyAuthenticatedAuthBundle<>(TestUserPrincipal.class, principalConverter, listener, listener, listener);

        then: 'No exception should be thrown, and expect the right number of listeners'
        noExceptionThrown()
        bundle.getAuthenticationEventListeners().size() == 3
    }

    def addListenersPostInitialisation() {
        given:
        TestAuthenticationListener listener = new TestAuthenticationListener();

        when: 'Adding listeners to the bundle'
        def bundle = new ExternallyAuthenticatedAuthBundle<>(TestUserPrincipal.class, principalConverter, listener, listener);
        bundle.addAuthenticationEventListener(listener)
        bundle.addAuthenticationEventListener(listener)

        then: 'No exception should be thrown, and expect the right number of listeners'
        noExceptionThrown()
        bundle.getAuthenticationEventListeners().size() == 4
    }
}
