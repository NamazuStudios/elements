package com.namazustudios.socialengine.dao.mongo;

import com.namazustudios.socialengine.ValidationHelper;
import com.namazustudios.socialengine.dao.ApplicationProfileDao;
import com.namazustudios.socialengine.dao.mongo.model.MongoPSNApplicationProfile;
import com.namazustudios.socialengine.exception.InvalidDataException;
import com.namazustudios.socialengine.fts.ObjectIndex;
import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.application.ApplicationProfile;
import com.namazustudios.socialengine.model.application.PSNApplicationProfile;
import org.apache.lucene.queryparser.flexible.standard.StandardQueryParser;
import org.mongodb.morphia.AdvancedDatastore;

import javax.inject.Inject;

/**
 * Created by patricktwohig on 7/13/15.
 */
public class MongoApplicationProfileDao implements ApplicationProfileDao {

    @Inject
    private ValidationHelper validationHelper;

    @Inject
    private ObjectIndex objectIndex;

    @Inject
    private StandardQueryParser standardQueryParser;

    @Inject
    private MongoDBUtils mongoDBUtils;

    @Inject
    private AdvancedDatastore datastore;

    @Override
    public PSNApplicationProfile createOrUpdateInactiveApplicationProfile(final String applicationNameOrId,
                                                                          final PSNApplicationProfile psnApplicationProfile) {
        validate(psnApplicationProfile);
        return null;
    }

    @Override
    public Pagination<ApplicationProfile> getActiveApplicationProfiles(final String applicationNameOrId,
                                                                       final int offset, final int count) {

        return null;
    }

    @Override
    public Pagination<ApplicationProfile> getActiveApplicationProfiles(final String applicationNameOrId,
                                                                       final int offset, final int count, final String search) {
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
        validate(psnApplicationProfile);
        return null;
    }

    @Override
    public PSNApplicationProfile updateApplicationProfile(String applicationNameOrId,
                                                          String applicationProfileNameOrId,
                                                          PSNApplicationProfile psnApplicationProfile) {
        validate(psnApplicationProfile);
        return null;
    }

    @Override
    public void softDeleteApplicationProfile(String applicationNameOrId,
                                             String applicationProfileNameOrId,
                                             Class<? extends ApplicationProfile> type) {

    }

    public void validate(final PSNApplicationProfile psnApplicationProfile) {

        if (psnApplicationProfile == null) {
            throw new InvalidDataException("psnApplicationProfile must not be null.");
        }

        switch (psnApplicationProfile.getPlatform()) {
            case PSN_PS4:
            case PSN_VITA:
                break;
            default:
                throw new InvalidDataException("platform not supported: " + psnApplicationProfile.getPlatform());
        }

        validationHelper.validateModel(psnApplicationProfile);

    }

    public MongoPSNApplicationProfile transform(final PSNApplicationProfile psnApplicationProfile) {

        final MongoPSNApplicationProfile mongoPSNApplicationProfile = new MongoPSNApplicationProfile();

        mongoPSNApplicationProfile.setObjectId(psnApplicationProfile.getId());
        mongoPSNApplicationProfile.setPlatform(psnApplicationProfile.getPlatform());
        mongoPSNApplicationProfile.setClientSecret(psnApplicationProfile.getClientSecret());
        mongoPSNApplicationProfile.setNpIdentifier(psnApplicationProfile.getNpIdentifier());

        return mongoPSNApplicationProfile;

    }

    public PSNApplicationProfile transform(final MongoPSNApplicationProfile mongoPSNApplicationProfile) {

        final PSNApplicationProfile psnApplicationProfile = new PSNApplicationProfile();

        psnApplicationProfile.setId(mongoPSNApplicationProfile.getObjectId());
        psnApplicationProfile.setPlatform(mongoPSNApplicationProfile.getPlatform());
        psnApplicationProfile.setNpIdentifier(mongoPSNApplicationProfile.getNpIdentifier());
        psnApplicationProfile.setClientSecret(mongoPSNApplicationProfile.getClientSecret());

        return psnApplicationProfile;

    }

}
