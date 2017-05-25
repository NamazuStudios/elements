package com.namazustudios.socialengine.service.application;

import com.namazustudios.socialengine.model.application.IosApplicationProfile;
import com.namazustudios.socialengine.service.IosApplicationProfileService;

/**
 * Created by patricktwohig on 5/25/17.
 */
public class SuperUserIosApplicationProfileService implements IosApplicationProfileService{

    @Override
    public void deleteApplicationProfile(final String applicationNameOrId,
                                         final String applicationProfileNameOrId) {

    }

    @Override
    public IosApplicationProfile getPSNApplicationProfile(final String applicationNameOrId,
                                                          final String applicationProfileNameOrId) {
        return null;
    }

    @Override
    public IosApplicationProfile createApplicationProfile(final String applicationNameOrId,
                                                          final IosApplicationProfile iosApplicationProfile) {
        return null;
    }

    @Override
    public IosApplicationProfile updateApplicationProfile(final String applicationNameOrId,
                                                          final String applicationProfileNameOrId,
                                                          final IosApplicationProfile iosApplicationProfile) {
        return null;
    }
}
