package dev.getelements.elements.dao.mongo.provider;

import dev.getelements.elements.sdk.dao.Matchmaker;
import dev.getelements.elements.dao.mongo.match.MongoFIFOMatchmaker;
import dev.getelements.elements.sdk.model.exception.NotImplementedException;

import jakarta.inject.Inject;
import jakarta.inject.Provider;

/**
 * Created by patricktwohig on 7/28/17.
 */
public class MongoMatchmakerFactoryProvider implements Provider<Matchmaker.Factory> {

    private Provider<MongoFIFOMatchmaker> mongoFIFOMatchmakerProvider;

    @Override
    public Matchmaker.Factory get() {
        return matchingAlgorithm -> {
            switch (matchingAlgorithm) {
                case FIFO:
                    return getMongoFIFOMatchmakerProvider().get();
                default:
                    throw new NotImplementedException();
            }
        };
    }

    public Provider<MongoFIFOMatchmaker> getMongoFIFOMatchmakerProvider() {
        return mongoFIFOMatchmakerProvider;
    }

    @Inject
    public void setMongoFIFOMatchmakerProvider(Provider<MongoFIFOMatchmaker> mongoFIFOMatchmakerProvider) {
        this.mongoFIFOMatchmakerProvider = mongoFIFOMatchmakerProvider;
    }

}
