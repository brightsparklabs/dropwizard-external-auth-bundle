/*
 * Created by brightSPARK Labs in 2020.
 * www.brightsparklabs.com
 *
 * Refer to LICENSE at repository root for license details.
 */

package com.brightsparklabs.dropwizard.bundles.auth.external;

import java.security.Principal;

/**
 * Interface for converting between {@link InternalUser} and the {@link Principal} used in the
 * system.
 *
 * @param <P> The {@link Principal} type to convert
 */
public interface PrincipalConverter<P extends Principal> {
    /**
     * Convert the principal {@link P} to an {@link InternalUser}
     *
     * @param principal principal to convert
     * @return the converted {@link InternalUser}
     */
    InternalUser convertToInternalUser(P principal);

    /**
     * Convert the principal {@link InternalUser} to an {@link P}
     *
     * @param transformedType internal user to conver to convert
     * @return the converted {@link P}
     */
    P convertToPrincipal(InternalUser transformedType);
}
