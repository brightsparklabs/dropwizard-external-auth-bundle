package com.brightsparklabs.dropwizard.bundles.auth.external

import spock.lang.Specification

import java.util.function.Function

/**
 *
 *
 * @author brightSPARK Labs
 */
class JwtAuthenticatorTest extends Specification {

    def "DoAuthenticate"() {

        given:
        String key = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAmKF1iO+fjks9Oi69UezToxw/jNvYmvxKnBt6eBX12j+RYkvnF1dnufZ7y4JNloaIgb8m/piV5TAu/rF3ICsniKRKO2xOfiS/EJfguALnDyA2Ko6tHYjsdmzbL3+tKd/oXjFXbL59I2hYbxo3+gi16cCzvdI6jD91OJJ9BEFGSKvkmxXsvYNzMsEHfsiflyLTiUYHLOLVRXfT3AYP67nF2gVSkUmkNV1Gn7EyORTw2VoAJF3uI+J6cGivCGoi7ClisCLxUYgFfA8jvRzjLkkbeIvqlW3lnh+qGRwNw73fum8UqVs/Y6rOYfDfAHCO1IieroqnuUn2j6f1eyhjkZlXjwIDAQAB"
        def authenticator = new JwtAuthenticator<InternalUser>(Function.identity(), key)

        // Hard coded, manually made using keycloak, with a long expiry (sometime in 2030)
        String jwt = "eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJGNWVxSUlSR1gwT2hIUmMtYl9TT0hGb2ZQbWVxNlcydUJaVVgxYWQxd0NjIn0.eyJqdGkiOiIyZmNkYmZmYy0wMzZhLTQzNWItODMzNy1hMGE2Njk3ZWE3ZGQiLCJleHAiOjE5MTgxODI3NzUsIm5iZiI6MCwiaWF0IjoxNTcyNTg2OTE0LCJpc3MiOiJodHRwOi8vbG9jYWxob3N0OjgwOTAvYXV0aC9yZWFsbXMvb3Ntb3NpcyIsImF1ZCI6WyJvc21vc2lzIiwiYWNjb3VudCJdLCJzdWIiOiIwMjk5ODEzYy1kY2YyLTQ1YWYtYjE4Ny02YmU4NTVlYjVmZjYiLCJ0eXAiOiJCZWFyZXIiLCJhenAiOiJvc21vc2lzIiwiYXV0aF90aW1lIjoxNTcyNTg2NzExLCJzZXNzaW9uX3N0YXRlIjoiMjUyM2ExMzQtYmQ4NC00ZjEwLWE5MzMtNjNmYzY2YWU3Y2FjIiwiYWNyIjoiMCIsImFsbG93ZWQtb3JpZ2lucyI6WyJodHRwOi8vbG9jYWxob3N0OjgwODAiXSwicmVhbG1fYWNjZXNzIjp7InJvbGVzIjpbIm9mZmxpbmVfYWNjZXNzIiwidW1hX2F1dGhvcml6YXRpb24iLCJBRE1JTiJdfSwicmVzb3VyY2VfYWNjZXNzIjp7Im9zbW9zaXMiOnsicm9sZXMiOlsiQ2xpZW50Um9sZSJdfSwiYWNjb3VudCI6eyJyb2xlcyI6WyJtYW5hZ2UtYWNjb3VudCIsIm1hbmFnZS1hY2NvdW50LWxpbmtzIiwidmlldy1wcm9maWxlIl19fSwic2NvcGUiOiJwcm9maWxlIGVtYWlsIiwiZW1haWxfdmVyaWZpZWQiOmZhbHNlLCJuYW1lIjoiYWRtaW4gYWRtaW4iLCJwcmVmZXJyZWRfdXNlcm5hbWUiOiJhZG1pbiIsImdpdmVuX25hbWUiOiJhZG1pbiIsImZhbWlseV9uYW1lIjoiYWRtaW4iLCJlbWFpbCI6ImFkbWluQGVtYWlsLmNvbSJ9.bjUt_5QMyKvcsTRwPQwBkW22n_6-0HZV3ecMumEYVl4vUM8fDm3nNymjbsELPo0A2oH5bPejtIBCw7Wma_f2PKsFOeS0pOfy2YzNkbmwQRPFVkCRMLwsaz9buNxwSYU7KU2NCzRRLoOlOtD4J0kYQquktuuunMFprTwwxCkAiWONlFDg2dkXM9B6SWVhlF511dZbVMppD0_Rk2e6jAx2Uef1CzSddAlZkV7VM_3Q7hHAy0FTDoBHh70MYY9jtaTxCXU8ws3W81BUp0RCvcBSW-_IB6u3M6GvreHGtMGcXtsx1ZjpcZcFPN0Diccsuvv328eP-R3K6zDorTaRzmEuYg"


        when:
        Optional<InternalUser> user = authenticator.doAuthenticate(jwt)

        then:
        def u  = user.get()
        u.getRoles().contains("ADMIN")
        u.getLastname() == "admin"
        u.getFirstname() == "admin"
        u.getUsername() == "admin"
        u.getEmail() == "admin@email.com"

    }


}
