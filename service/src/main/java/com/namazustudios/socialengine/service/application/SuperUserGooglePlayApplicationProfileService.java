package com.namazustudios.socialengine.service.application;

import com.namazustudios.socialengine.model.application.GooglePlayApplicationProfile;
import com.namazustudios.socialengine.service.GooglePlayApplicationProfileService;

/**
 * Created by patricktwohig on 5/25/17.
 */
public class SuperUserGooglePlayApplicationProfileService implements GooglePlayApplicationProfileService {
    @Override
    public void deleteApplicationProfile(final String applicationNameOrId,
                                         final String applicationProfileNameOrId) {

    }

    @Override
    public GooglePlayApplicationProfile getApplicationProfile(final String applicationNameOrId,
                                                              final String applicationProfileNameOrId) {
        return null;
    }

    @Override
    public GooglePlayApplicationProfile createApplicationProfile(final String applicationNameOrId,
                                                                 final GooglePlayApplicationProfile googlePlayApplicationProfile) {
        return null;
    }

    @Override
    public GooglePlayApplicationProfile updateApplicationProfile(final String applicationNameOrId,
                                                                 final String applicationProfileNameOrId,
                                                                 final GooglePlayApplicationProfile googlePlayApplicationProfile) {
        return null;
    }
}
