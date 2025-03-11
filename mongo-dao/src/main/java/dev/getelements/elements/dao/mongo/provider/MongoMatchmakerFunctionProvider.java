package dev.getelements.elements.dao.mongo.provider;

import dev.getelements.elements.sdk.dao.Matchmaker;
import dev.getelements.elements.dao.mongo.match.MongoFIFOMatchmaker;
import dev.getelements.elements.sdk.model.exception.NotImplementedException;
import dev.getelements.elements.sdk.model.match.MatchingAlgorithm;

import jakarta.inject.Inject;
import jakarta.inject.Provider;
import java.util.function.Function;

/**
 * Created by patricktwohig on 7/28/17.
 */
public class MongoMatchmakerFunctionProvider implements Provider<Function<MatchingAlgorithm, Matchmaker>> {

    private Provider<MongoFIFOMatchmaker> mongoFIFOMatchmakerProvider;

    @Override
    public Function<MatchingAlgorithm, Matchmaker> get() {
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
