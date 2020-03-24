/*
 * Created by brightSPARK Labs in 2020.
 * www.brightsparklabs.com
 *
 * Refer to LICENSE at repository root for license details.
 */

package com.brightsparklabs.dropwizard.bundles.auth.external;

import java.security.Principal;
import java.util.Optional;
import javax.validation.constraints.NotNull;
import org.eclipse.jetty.server.Authentication;

/**
 * Interface for converting between {@link InternalUser} and the {@link Principal} used in the
 * system.
 *
 * @param <P> The {@link Principal} type to convert
 */
public interface PrincipalConverter<P extends Principal> {
    /**
     * Convert the principal {@link P} to an {@link InternalUser}.
     *
     * <p>For audit logging purposes, the returned {@link InternalUser#getUsername()} should return
     * a non-null entry. This value will be passed into {@link
     * org.eclipse.jetty.server.Request#setAuthentication(Authentication)} which is captured and
     * logged by default in logback.
     *
     * @param principal principal to convert
     * @return the optionally converted {@link InternalUser}, or {@link Optional#empty()} if the
     *     conversion cannot be done.
     */
    Optional<InternalUser> convertToInternalUser(P principal);

    /**
     * Convert the principal {@link InternalUser} to an {@link P}.
     *
     * @param internalUser internal user to convert
     * @return the converted {@link P}
     */
    @NotNull
    P convertToPrincipal(InternalUser internalUser);
}
