package com.namazustudios.socialengine.dao;

import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.profile.Profile;

/**
 * Created by patricktwohig on 6/28/17.
 */
public interface ProfileDao {


    Pagination<Profile> getActiveProfiles(int offset, int count);

    Pagination<Profile> getActiveProfiles(int offset, int count, String search);

    Profile getActiveProfile(String profileId);

    Profile updateActiveProfile(Profile profile);

    Profile createOrReactivateProfile(Profile profile);

    void softDeleteProfile(String profileId);

}
