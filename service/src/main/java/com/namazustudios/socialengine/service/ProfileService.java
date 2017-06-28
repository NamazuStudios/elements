package com.namazustudios.socialengine.service;

import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.profile.Profile;

/**
 * Created by patricktwohig on 6/27/17.
 */
public interface ProfileService {

    Pagination<Profile> getProfiles(int offset, int count);

    Pagination<Profile> getProfiles(int offset, int count, String search);

    Profile getProfile(String profileId);

    Profile getCurrentProfile();

    Profile updateProfile(Profile profile);

    Profile createProfile(Profile profile);

    void deleteProfile(String profileId);

}
