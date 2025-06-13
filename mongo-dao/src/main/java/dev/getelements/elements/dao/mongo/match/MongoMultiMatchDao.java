package dev.getelements.elements.dao.mongo.match;

import dev.getelements.elements.dao.mongo.MongoDBUtils;
import dev.getelements.elements.dao.mongo.MongoProfileDao;
import dev.getelements.elements.dao.mongo.UpdateBuilder;
import dev.getelements.elements.dao.mongo.model.MongoProfile;
import dev.getelements.elements.dao.mongo.query.BooleanQueryParser;
import dev.getelements.elements.sdk.dao.MultiMatchDao;
import dev.getelements.elements.sdk.model.application.MatchmakingApplicationConfiguration;
import dev.getelements.elements.sdk.model.exception.MultiMatchNotFoundException;
import dev.getelements.elements.sdk.model.match.MultiMatch;
import dev.getelements.elements.sdk.model.profile.Profile;
import dev.getelements.elements.sdk.model.util.MapperRegistry;
import dev.morphia.Datastore;
import dev.morphia.query.updates.UpdateOperator;
import jakarta.inject.Inject;

import java.util.List;
import java.util.Optional;

import static com.google.common.base.Strings.nullToEmpty;
import static dev.morphia.query.filters.Filters.eq;
import static dev.morphia.query.updates.UpdateOperators.set;

public class MongoMultiMatchDao implements MultiMatchDao {

    private Datastore datastore;

    private MongoDBUtils mongoDBUtils;

    private MapperRegistry mapperRegistry;

    private BooleanQueryParser booleanQueryParser;

    private MongoProfileDao mongoProfileDao;

    @Override
    public List<MultiMatch> getAllMultiMatches(final String search) {

        final var trimmedSearch = nullToEmpty(search).trim();

        if (trimmedSearch.isEmpty()) {
            return List.of();
        }

        final var parsed = getBooleanQueryParser()
                .parse(MongoMultiMatch.class, trimmedSearch);

        return parsed
                .filter(q -> getMongoDBUtils().isIndexedQuery(q))
                .map(q -> q
                        .stream()
                        .map(mmm -> getMapperRegistry().map(mmm, MultiMatch.class))
                        .toList()
                ).orElseGet(List::of);

    }

    @Override
    public Optional<MultiMatch> findMultiMatch(final String multiMatchId) {
        return findMongoMultiMatch(multiMatchId)
                .map(mmm -> getMapperRegistry().map(mmm, MultiMatch.class));
    }

    public Optional<MongoMultiMatch> findMongoMultiMatch(final String multiMatchId) {
        return getMongoDBUtils()
                .parse(multiMatchId)
                .flatMap(objectId -> getDatastore()
                        .find(MongoMultiMatch.class)
                        .filter(eq("_id", objectId))
                        .stream()
                        .findFirst()
                );
    }

    @Override
    public MultiMatch addProfile(final String multiMatchId, final Profile profile) {
        final var mongoProfile = getMongoProfileDao().getActiveMongoProfile(profile);

        return findMongoMultiMatch(multiMatchId).map(mongoMultiMatch -> {
                    final var query = getDatastore().find(MongoMultiMatch.class);

                    new UpdateBuilder()
                            .with(set("match", mongoMultiMatch))
                            .with(set("profile", mongoProfile));
                    return getMapperRegistry().map(mongoMultiMatch, MultiMatch.class);
                })
                .orElseThrow(MultiMatchNotFoundException::new);
    }

    @Override
    public MultiMatch removeProfile(final String multiMatchId, final Profile profile) {
        return null;
    }

    @Override
    public List<Profile> getProfiles(final String multiMatchId) {
        return List.of();
    }

    @Override
    public MultiMatch createMultiMatch(MatchmakingApplicationConfiguration configuration) {
        return null;
    }

    @Override
    public MultiMatch updateMultiMatch(MultiMatch multiMatch) {
        return null;
    }

    @Override
    public boolean tryDeleteMultiMatch(String multiMatchId) {
        return false;
    }

    public Datastore getDatastore() {
        return datastore;
    }

    @Inject
    public void setDatastore(Datastore datastore) {
        this.datastore = datastore;
    }

    public MongoDBUtils getMongoDBUtils() {
        return mongoDBUtils;
    }

    @Inject
    public void setMongoDBUtils(MongoDBUtils mongoDBUtils) {
        this.mongoDBUtils = mongoDBUtils;
    }

    public MapperRegistry getMapperRegistry() {
        return mapperRegistry;
    }

    @Inject
    public void setMapperRegistry(MapperRegistry mapperRegistry) {
        this.mapperRegistry = mapperRegistry;
    }

    public BooleanQueryParser getBooleanQueryParser() {
        return booleanQueryParser;
    }

    @Inject
    public void setBooleanQueryParser(BooleanQueryParser booleanQueryParser) {
        this.booleanQueryParser = booleanQueryParser;
    }

    public MongoProfileDao getMongoProfileDao() {
        return mongoProfileDao;
    }

    @Inject
    public void setMongoProfileDao(MongoProfileDao mongoProfileDao) {
        this.mongoProfileDao = mongoProfileDao;
    }

}
