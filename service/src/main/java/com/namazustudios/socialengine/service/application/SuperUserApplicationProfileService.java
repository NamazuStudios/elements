package com.namazustudios.socialengine.service.application;

import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.application.ApplicationProfile;
import com.namazustudios.socialengine.model.application.PSNApplicationProfile;
import com.namazustudios.socialengine.service.ApplicationProfileService;

/**
 * Created by patricktwohig on 7/13/15.
 */
public class SuperUserApplicationProfileService implements ApplicationProfileService {

    @Override
    public Pagination<ApplicationProfile> getApplicationProfiles(String applicationNameOrId, int offset, int count) {
        return null;
    }

    @Override
    public Pagination<ApplicationProfile> getApplicationProfiles(String applicationNameOrId, int offset, int count, String search) {
        return null;
    }

    @Override
    public ApplicationProfile getApplicationProfile(String applicationNameOrId, String applicationProfileNameOrId) {
        return null;
    }

    @Override
    public <T extends ApplicationProfile> T getApplicationProfile(String applicationNameOrId, String applicationProfileNameOrId, Class<T> type) {
        return null;
    }

    @Override
    public PSNApplicationProfile updateApplicationProfile(String applicationNameOrId, PSNApplicationProfile psnApplicationProfile) {
        return null;
    }

    @Override
    public void deleteApplicationProfile(String applicationNameOrId, String applicationProfileNameOrId) {

    }

}
