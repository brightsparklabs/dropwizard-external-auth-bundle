/*
 * Created by brightSPARK Labs
 * www.brightsparklabs.com
 */

package com.brightsparklabs.dropwizard.bundles.auth.external;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import io.dropwizard.auth.AuthenticationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.MultivaluedMap;
import java.security.Principal;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.StreamSupport;

/**
 * Determines if a user is authenticated based on the presence/values in various header fields. This
 * should only be used behind a reverse proxy where the proxy inserts appropriate 'internal' header
 * fields (and strips out any fields the end user provides in the request). Authenticator} for it to
 * determine if the user should be considered authenticated.
 *
 * @param <P>
 *         Type of {@link Principal} to return for authenticated users.
 *
 * @author brightSPARK Labs
 */
public class HeaderFieldsAuthenticator<P extends Principal>
        extends ExternalAuthenticator<MultivaluedMap<String, String>, P>
{
    // -------------------------------------------------------------------------
    // CONSTANTS
    // -------------------------------------------------------------------------

    /** Default field to use to extract the username */
    private final static String DEFAULT_FIELD_USERNAME = "X-Auth-Username";

    /** Default field to use to extract the firstname */
    private final static String DEFAULT_FIELD_FIRSTNAME = "X-Auth-Given-Name";

    /** Default field to use to extract the lastname */
    private final static String DEFAULT_FIELD_LASTNAME = "X-Auth-Family-Name";

    /** Default field to use to extract the email */
    private final static String DEFAULT_FIELD_EMAIL = "X-Auth-Email";

    /** Default field to use to extract the groups */
    private final static String DEFAULT_FIELD_GROUPS = "X-Auth-Groups";

    /** Default field to use to extract the roles */
    private final static String DEFAULT_FIELD_ROLES = "X-Auth-Roles";

    // -------------------------------------------------------------------------
    // CLASS VARIABLES
    // -------------------------------------------------------------------------

    /** Class logger */
    private static Logger logger = LoggerFactory.getLogger(HeaderFieldsAuthFilter.class);

    /** Splits comma separated strings */
    private final static Splitter splitOnCommas = Splitter.on(',').omitEmptyStrings().trimResults();

    // -------------------------------------------------------------------------
    // INSTANCE VARIABLES
    // -------------------------------------------------------------------------

    // -------------------------------------------------------------------------
    // CONSTRUCTION
    // -------------------------------------------------------------------------

    /**
     * Default constructor.
     *
     * @param externalUserToPrincipal
     *         Converts the internal user to the {@link Principal} used in the system.
     */
    public HeaderFieldsAuthenticator(final Function<InternalUser, P> externalUserToPrincipal)
    {
        super(externalUserToPrincipal);
    }

    // -------------------------------------------------------------------------
    // IMPLEMENTATION:  ExternalAuthenticator
    // -------------------------------------------------------------------------

    @Override
    public Optional<InternalUser> doAuthenticate(final MultivaluedMap<String, String> headers)
            throws AuthenticationException
    {
        logger.info("Authenticating via header fields ...");
        if (headers == null)
        {
            throw new AuthenticationException("No header fields provided to authenticator");
        }

        // extract groups and roles
        final ImmutableList<String> groups = headers.getOrDefault(DEFAULT_FIELD_GROUPS,
                ImmutableList.of())
                .stream()
                .map(splitOnCommas::split)
                .flatMap(x -> StreamSupport.stream(x.spliterator(), false))
                .collect(ImmutableList.toImmutableList());
        final ImmutableList<String> roles = headers.getOrDefault(DEFAULT_FIELD_ROLES,
                ImmutableList.of())
                .stream()
                .map(splitOnCommas::split)
                .flatMap(x -> StreamSupport.stream(x.spliterator(), false))
                .collect(ImmutableList.toImmutableList());

        final String username = headers.getFirst(DEFAULT_FIELD_USERNAME);
        try
        {
            final ImmutableInternalUser user = ImmutableInternalUser.builder()
                    .username(extractHeaderValue(DEFAULT_FIELD_USERNAME, headers))
                    .firstname(extractHeaderValue(DEFAULT_FIELD_FIRSTNAME, headers))
                    .lastname(extractHeaderValue(DEFAULT_FIELD_LASTNAME, headers))
                    .email(Optional.ofNullable(headers.getFirst(DEFAULT_FIELD_EMAIL)))
                    .groups(groups)
                    .roles(roles)
                    .build();
            logger.info("Authentication successful for username [{}]", username);
            return Optional.of(user);
        }
        catch (IllegalArgumentException ex)
        {
            logger.info("Authentication denied for username [{}] - {}", username, ex.getMessage());
            return Optional.empty();
        }
    }

    // -------------------------------------------------------------------------
    // PUBLIC METHODS
    // -------------------------------------------------------------------------

    // -------------------------------------------------------------------------
    // PRIVATE METHODS
    // -------------------------------------------------------------------------

    private String extractHeaderValue(String headerName, MultivaluedMap<String, String> headers)
            throws IllegalArgumentException
    {
        final String result = headers.getFirst(headerName);
        if (Strings.isNullOrEmpty(result))
        {
            throw new IllegalArgumentException(
                    "Request headers did not contain valid header field [" + headerName + "]");
        }
        return result;
    }
}
