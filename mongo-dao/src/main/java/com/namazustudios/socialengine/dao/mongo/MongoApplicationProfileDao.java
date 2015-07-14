package com.namazustudios.socialengine.dao.mongo;

import com.namazustudios.socialengine.dao.ApplicationProfileDao;
import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.application.ApplicationProfile;
import com.namazustudios.socialengine.model.application.PSNApplicationProfile;

/**
 * Created by patricktwohig on 7/13/15.
 */
public class MongoApplicationProfileDao implements ApplicationProfileDao {

    @Override
    public PSNApplicationProfile createOrUpdateInactiveApplicationProfile(String applicationNameOrId,
                                                                          PSNApplicationProfile psnApplicationProfile) {
        return null;
    }

    @Override
    public Pagination<ApplicationProfile> getActiveApplicationProfiles(String applicationNameOrId,
                                                                       int offset, int count) {
        return null;
    }

    @Override
    public Pagination<ApplicationProfile> getActiveApplicationProfiles(String applicationNameOrId,
                                                                       int offset, int count, String search) {
        return null;
    }

    @Override
    public ApplicationProfile getApplicationProfile(String applicationNameOrId,
                                                    String applicationProfileNameOrId) {
        return null;
    }

    @Override
    public <T extends ApplicationProfile> T getApplicationProfile(String applicationNameOrId,
                                                                  String applicationProfileNameOrId,
                                                                  Class<T> type) {
        return null;
    }

    @Override
    public PSNApplicationProfile createApplicationProfile(String applicationNameOrId,
                                                          PSNApplicationProfile psnApplicationProfile) {
        return null;
    }

    @Override
    public PSNApplicationProfile updateApplicationProfile(String applicationNameOrId,
                                                          String applicationProfileNameOrId,
                                                          PSNApplicationProfile psnApplicationProfile) {
        return null;
    }

    @Override
    public void softDeleteApplicationProfile(String applicationNameOrId,
                                             String applicationProfileNameOrId,
                                             Class<? extends ApplicationProfile> type) {

    }

}
