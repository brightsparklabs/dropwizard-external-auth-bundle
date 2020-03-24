/*
 * Created by brightSPARK Labs in 2020.
 * www.brightsparklabs.com
 *
 * Refer to LICENSE at repository root for license details.
 */

package com.brightsparklabs.dropwizard.bundles.auth.external;

public class IdentityPrincipalConverter implements PrincipalConverter<InternalUser> {
    @Override
    public InternalUser convertToInternalUser(InternalUser principal) {
        return principal;
    }

    @Override
    public InternalUser convertToPrincipal(InternalUser transformedType) {
        return transformedType;
    }
}
