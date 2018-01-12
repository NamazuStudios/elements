package com.namazustudios.socialengine.dao.mongo.model;

/**
 * Maps to {@link com.namazustudios.socialengine.model.application.MatchmakingApplicationConfiguration}.
 */
public class MongoMatchmakingApplicationConfiguration extends MongoApplicationConfiguration {

    private MongoCallbackDefinition success;

    public MongoCallbackDefinition getSuccess() {
        return success;
    }

    public void setSuccess(MongoCallbackDefinition success) {
        this.success = success;
    }

}
