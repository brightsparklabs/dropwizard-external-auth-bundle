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
    public InternalUser convertToInternalUser(P principal);

    public P convertToPrincipal(InternalUser transformedType);
}
