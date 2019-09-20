# dropwizard-external-auth-bundle

[ ![Download](https://api.bintray.com/packages/brightsparklabs/maven/dropwizard-external-auth-bundle/images/download.svg) ](https://bintray.com/brightsparklabs/maven/dropwizard-external-auth-bundle/_latestVersion)

A Dropwizard bundle which allows your service to trust authentication from an
external identity provider.

Currently focused around supporting [Keycloak
Gatekeeper](https://github.com/keycloak/keycloak-gatekeeper).

# Usage

- Include bundle. E.g. via `gradle`:

        # build.gradle
        repositories {
            jcenter()
            // ALTERNATIVELY: directly use the BSL repo on bintray
            maven { url "https://dl.bintray.com/brightsparklabs/maven" }
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
                final Function<InternalUser, User> converter = user -> ImmutableUser.builder()
                        .username(user.getUsername())
                        .firstname(user.getFirstname())
                        .lastname(user.getLastname())
                        .email(user.getEmail())
                        .groups(user.getGroups())
                        .roles(user.getRoles())
                        .build();
                // create and apply the bundle
                final ExternallyAuthenticatedAuthBundle<User, MyConfiguration> authBundle
                        = new ExternallyAuthenticatedAuthBundle<>(User.class, converter);
                bootstrap.addBundle(authBundle);
            }
        }

- Access your server and ensure:
    - If using `method: httpHeaders`, all mandatory headers are provided in request.
    - If using `method: jwt`, a valid JWT is in the header `Authorization: Bearer <jwt>`.

# Development

- Publish new versions via:

        export BINTRAY_USER=<user>
        export BINTRAY_KEY=<key>
        ./gradlew bintrayUpload

# Licenses

Refer to the `LICENSE` file for details.
