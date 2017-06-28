package com.namazustudios.socialengine.service.profile;

import com.namazustudios.socialengine.exception.NotFoundException;
import com.namazustudios.socialengine.model.profile.Profile;

import javax.inject.Provider;
import java.util.function.Supplier;

/**
 * Created by patricktwohig on 6/28/17.
 */
public class ProfileSupplierProvider implements Provider<Supplier<Profile>> {
    @Override
    public Supplier<Profile> get() {

        // TODO: Determine profile from scope of request

        return () -> {
            throw new NotFoundException();
        };

    }
}
