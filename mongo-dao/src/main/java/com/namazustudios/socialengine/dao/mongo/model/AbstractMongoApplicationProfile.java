package com.namazustudios.socialengine.dao.mongo.model;

import com.namazustudios.socialengine.model.application.Application;
import com.namazustudios.socialengine.model.application.Platform;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.*;

/**
 * Created by patricktwohig on 7/10/15.
 */
public abstract class AbstractMongoApplicationProfile {

    @Id
    private Key id;

    @Property("platform")
    private Platform platform;

    /**
     * Gets the ID.
     *
     * @return the id
     */
    public Key getId() {
        return id;
    }

    /**
     * Sets the ID.
     *
     * @param id id
     */
    public void setId(Key id) {
        this.id = id;
    }

    public Platform getPlatform() {
        return platform;
    }

    public void setPlatform(Platform platform) {
        this.platform = platform;
    }

    /**
     * A special compound key which contains an {@link ObjectId} and a {@link Reference} to
     * an instance of {@link Application}.  This ensures that the
     *
     * Created by patricktwohig on 7/10/15.
     */
    @Embedded
    public class Key {

        private ObjectId id = new ObjectId();

        @Reference
        private Application application;

        public ObjectId getId() {
            return id;
        }

        public void setId(ObjectId id) {
            this.id = id;
        }

        public Application getApplication() {
            return application;
        }

        public void setApplication(Application application) {
            this.application = application;
        }

    }

}
