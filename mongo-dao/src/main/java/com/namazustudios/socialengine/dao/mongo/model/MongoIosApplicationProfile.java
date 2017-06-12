package com.namazustudios.socialengine.dao.mongo.model;

import org.mongodb.morphia.annotations.Entity;

/**
 * Created by patricktwohig on 5/31/17.
 */
@Entity(value = "application_profile", noClassnameStored = true)
public class MongoIosApplicationProfile extends AbstractMongoApplicationProfile {
    // TODO This will likely be populated with more information.
}
