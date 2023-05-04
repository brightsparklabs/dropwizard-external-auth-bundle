# dropwizard-external-auth-bundle

[![Build Status](https://github.com/brightsparklabs/dropwizard-external-auth-bundle/actions/workflows/unit_tests.yml/badge.svg)](https://github.com/brightsparklabs/dropwizard-external-auth-bundle/actions/workflows/unit_tests.yml)

A Dropwizard bundle which allows your service to trust authentication from an
external identity provider.

Currently focused around supporting [Keycloak
Gatekeeper](https://github.com/keycloak/keycloak-gatekeeper).

## Usage

- Include bundle. E.g. via `gradle`:

        # build.gradle
        repositories {
            mavenCentral()
        }

        dependencies {
            implementation "com.brightsparklabs:dropwizard-external-auth-bundle:$version"
        }

- Ensure bundle configuration settings into configuration class:

        public class MyConfiguration extends Configuration
                implements ExternallyAuthenticatedAuthBundleConfiguration
        {
            @NotNull
            @JsonProperty("auth")
            private ExternallyAuthenticatedAuthFilterFactory externallyAuthenticatedAuthFilterFactory;

            @Override
            public ExternallyAuthenticatedAuthFilterFactory getExternallyAuthenticatedFilterFactory()
            {
                return externallyAuthenticatedAuthFilterFactory;
            }
        }

- Configure bundle in configuration file:

        # config.yml

        auth:
          # external id provider will supply details of the authenticated user via various http headers
          #method: httpHeaders

          # external id provider will supply details of the authenticated user via a JSON Web Token (JWT)
          #method: jwt
          #signingKey: <INSERT THE BASE64 ENCODED KEY USED BY ID PROVIDER TO SIGN THE JWT>

         # DEVELOPMENT ONLY - external id provider will always return the user defined here
         method: dev
         user:
           firstname: Test
           lastname: User
           username: test.user
           email: test.user@email.test
           groups:
             - GROUP_1
             - GROUP_2
             - GROUP_3
           roles:
             - ROLE_1
             - ROLE_2
             - ROLE_3

- Add bundle during initialization:

        public class MyApplication extends Application<MyConfiguration> {
            public void initialize(Bootstrap<MyConfiguration> bootstrap)
            {
                // create converter to map principal to what your app uses
                final PrincipalConverter<User> converter = new PrincipalConverter<User> { ... }

                // Optionally create an Authorizer
                final Authorizer<User> authorizer = (p, r) -> p.getRoles().contains(r);

                // create and apply the bundle
                final ExternallyAuthenticatedAuthBundle<User, MyConfiguration> authBundle
                        = new ExternallyAuthenticatedAuthBundle<>(User.class, converter, authorizer);
                bootstrap.addBundle(authBundle);
            }
        }

- Access your server and ensure:
    - If using `method: httpHeaders`, all mandatory headers are provided in request.
    - If using `method: jwt`, a valid JWT is in the header `Authorization: Bearer <jwt>`.

# Request Logs

The name of the `Principal` (i.e. `Principal#getName()`) will be automatically added to the
request such that it will appear correctly in the Dropwizard request logs.

# MDC

The name of the `Principal` will be automatically added to the SLF4J MDC
(Mapped Diagnostic Context). The default name for the MDC key is `req.username`. This can be
changed by adding the following to the configuration:

```
# config.yml

auth:
  # Key in the MDC that the authenticated user's username is stored against.
  mdcUsernameField: theUser
```

## Extending

To create your own instances of `ExternallyAuthenticatedAuthFilterFactory` from configuration
(i.e. instead of the bundled `jwt`/`httpHeaders`/`dev`):

1. Add a file to:

        .../resources/META-INF/services/com.brightsparklabs.dropwizard.bundles.auth.external.ExternallyAuthenticatedAuthFilterFactory

2. Add the full class name of your subclass of `ExternallyAuthenticatedAuthFilterFactory` inside.

## Development

- Publish new versions via:

```bash
# Set env vars.
export ORG_GRADLE_PROJECT_signingKey=<secrets.PGP_SIGNING_KEY>
export ORG_GRADLE_PROJECT_signingPassword=<secrets.PGP_SIGNING_PASSWORD>
export ORG_GRADLE_PROJECT_sonatypeUsername=<secrets.MAVEN_CENTRAL_USERNAME>
export ORG_GRADLE_PROJECT_sonatypePassword=<secrets.MAVEN_CENTRAL_PASSWORD>
# Run the publishToMavenCentral gradle task
./gradlew publishToMavenCentral
```

## Licenses

Refer to the `LICENSE` file for details.
