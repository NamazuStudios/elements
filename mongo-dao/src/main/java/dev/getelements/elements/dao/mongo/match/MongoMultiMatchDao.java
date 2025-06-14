package dev.getelements.elements.dao.mongo.match;

import dev.getelements.elements.dao.mongo.MongoDBUtils;
import dev.getelements.elements.dao.mongo.MongoProfileDao;
import dev.getelements.elements.dao.mongo.UpdateBuilder;
import dev.getelements.elements.dao.mongo.application.MongoApplicationConfigurationDao;
import dev.getelements.elements.dao.mongo.application.MongoApplicationDao;
import dev.getelements.elements.dao.mongo.model.application.MongoMatchmakingApplicationConfiguration;
import dev.getelements.elements.dao.mongo.query.BooleanQueryParser;
import dev.getelements.elements.sdk.ElementRegistry;
import dev.getelements.elements.sdk.Event;
import dev.getelements.elements.sdk.annotation.ElementEventProducer;
import dev.getelements.elements.sdk.dao.MultiMatchDao;
import dev.getelements.elements.sdk.model.ValidationGroups;
import dev.getelements.elements.sdk.model.exception.DuplicateException;
import dev.getelements.elements.sdk.model.exception.MultiMatchNotFoundException;
import dev.getelements.elements.sdk.model.exception.profile.ProfileNotFoundException;
import dev.getelements.elements.sdk.model.match.MultiMatch;
import dev.getelements.elements.sdk.model.profile.Profile;
import dev.getelements.elements.sdk.model.util.MapperRegistry;
import dev.getelements.elements.sdk.model.util.ValidationHelper;
import dev.morphia.Datastore;
import dev.morphia.UpdateOptions;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.util.List;
import java.util.Optional;

import static com.google.common.base.Strings.nullToEmpty;
import static dev.getelements.elements.sdk.ElementRegistry.ROOT;
import static dev.morphia.query.filters.Filters.eq;
import static dev.morphia.query.updates.UpdateOperators.set;
import static java.util.Objects.requireNonNull;

@ElementEventProducer(
        value = MultiMatchDao.MULTI_MATCH_CREATED,
        parameters = MultiMatch.class,
        description = "Called when a multi-match was created."
)
@ElementEventProducer(
        value = MultiMatchDao.MULTI_MATCH_ADD_PROFILE,
        parameters = {MultiMatch.class, Profile.class},
        description = "Called when a profile is added to a multi match."
)
@ElementEventProducer(
        value = MultiMatchDao.MULTI_MATCH_REMOVE_PROFILE,
        parameters = {MultiMatch.class, Profile.class},
        description = "Called when a profile is aremoved from a multi match."
)
@ElementEventProducer(
        value = MultiMatchDao.MULTI_MATCH_UPDATED,
        parameters = MultiMatch.class,
        description = "Called when a multi-match was updated."
)
@ElementEventProducer(
        value = MultiMatchDao.MULTI_MATCH_DELETED,
        parameters = MultiMatch.class,
        description = "Called when a multi-match was deleted."
)
public class MongoMultiMatchDao implements MultiMatchDao {

    private Datastore datastore;

    private MongoDBUtils mongoDBUtils;

    private MapperRegistry mapperRegistry;

    private BooleanQueryParser booleanQueryParser;

    private ValidationHelper validationHelper;

    private MongoProfileDao mongoProfileDao;

    private MongoApplicationDao mongoApplicationDao;

    private MongoApplicationConfigurationDao mongoApplicationConfigurationDao;

    private ElementRegistry elementRegistry;

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

                    final var query = getDatastore().find(MongoMultiMatch.class)
                            .filter(eq("match", mongoMultiMatch))
                            .filter(eq("profile", mongoProfile));

                    final var result = new UpdateBuilder()
                            .with(set("match", mongoMultiMatch))
                            .with(set("profile", mongoProfile))
                            .execute(query, new UpdateOptions().upsert(true));

                    if (result.getModifiedCount() == 0) {
                        throw new DuplicateException();
                    }

                    final var multiMatch = getMapperRegistry().map(mongoMultiMatch, MultiMatch.class);

                    getElementRegistry().publish(Event.builder()
                            .argument(multiMatch)
                            .argument(getMapperRegistry().map(mongoProfile, Profile.class))
                            .named(MULTI_MATCH_ADD_PROFILE)
                            .build()
                    );

                    return multiMatch;

                })
                .orElseThrow(MultiMatchNotFoundException::new);

    }

    @Override
    public MultiMatch removeProfile(final String multiMatchId, final Profile profile) {

        final var mongoProfile = getMongoProfileDao().getActiveMongoProfile(profile);

        return findMongoMultiMatch(multiMatchId).map(mongoMultiMatch -> {

                    final var result = getDatastore().find(MongoMultiMatch.class)
                            .filter(eq("match", mongoMultiMatch))
                            .filter(eq("profile", mongoProfile))
                            .delete();

                    if (result.getDeletedCount() == 0) {
                        throw new ProfileNotFoundException();
                    }

                    final var multiMatch = getMapperRegistry().map(mongoMultiMatch, MultiMatch.class);

                    getElementRegistry().publish(Event.builder()
                            .argument(multiMatch)
                            .argument(getMapperRegistry().map(mongoProfile, Profile.class))
                            .named(MULTI_MATCH_REMOVE_PROFILE)
                            .build()
                    );

                    return multiMatch;

                })
                .orElseThrow(MultiMatchNotFoundException::new);

    }

    @Override
    public List<Profile> getProfiles(final String multiMatchId) {
        return findMongoMultiMatch(multiMatchId)
                .map(mongoMultiMatch -> getDatastore()
                        .find(MongoMultiMatchProfile.class)
                        .filter(eq("match", mongoMultiMatch))
                        .stream()
                        .map(MongoMultiMatchProfile::getProfile)
                        .map(mp -> getMapperRegistry().map(mongoMultiMatch, Profile.class))
                        .toList()
                )
                .orElseThrow(MultiMatchNotFoundException::new);
    }

    @Override
    public MultiMatch createMultiMatch(final MultiMatch multiMatch) {

        requireNonNull(multiMatch, "multiMatch");
        getValidationHelper().validateModel(multiMatch, ValidationGroups.Insert.class);

        final var mongoMultiMatch = getMapperRegistry().map(multiMatch, MongoMultiMatch.class);

        final var mongoApplication = getMongoApplicationDao().findActiveMongoApplication(multiMatch
                .getConfiguration()
                .getParent()
                .getId()
        );

        final var mongoMatchmakingApplicationConfiguration = getMongoApplicationConfigurationDao()
                .findMongoApplicationConfiguration(
                        MongoMatchmakingApplicationConfiguration.class,
                        multiMatch.getConfiguration().getParent().getId(),
                        multiMatch.getConfiguration().getId()
                );

        final var createdMongoMultiMatch = getDatastore().save(mongoMultiMatch);
        final var createdMultiMatch = getMapperRegistry().map(createdMongoMultiMatch, MultiMatch.class);

        getElementRegistry().publish(Event.builder()
                .argument(createdMultiMatch)
                .named(MULTI_MATCH_CREATED)
                .build()
        );

        return createdMultiMatch;

    }

    @Override
    public MultiMatch updateMultiMatch(final MultiMatch multiMatch) {

        requireNonNull(multiMatch, "multiMatch");
        getValidationHelper().validateModel(multiMatch, ValidationGroups.Update.class);

        final var objectId = getMongoDBUtils()
                .parse(multiMatch.getId())
                .orElseThrow(MultiMatchNotFoundException::new);

        final var query = getDatastore().find(MongoMultiMatch.class)
                .filter(eq("_id", objectId));


    }

    @Override
    public boolean tryDeleteMultiMatch(final String multiMatchId) {
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

    public ValidationHelper getValidationHelper() {
        return validationHelper;
    }

    @Inject
    public void setValidationHelper(ValidationHelper validationHelper) {
        this.validationHelper = validationHelper;
    }

    public MongoProfileDao getMongoProfileDao() {
        return mongoProfileDao;
    }

    @Inject
    public void setMongoProfileDao(MongoProfileDao mongoProfileDao) {
        this.mongoProfileDao = mongoProfileDao;
    }

    public MongoApplicationDao getMongoApplicationDao() {
        return mongoApplicationDao;
    }

    @Inject
    public void setMongoApplicationDao(MongoApplicationDao mongoApplicationDao) {
        this.mongoApplicationDao = mongoApplicationDao;
    }

    public MongoApplicationConfigurationDao getMongoApplicationConfigurationDao() {
        return mongoApplicationConfigurationDao;
    }

    @Inject
    public void setMongoApplicationConfigurationDao(MongoApplicationConfigurationDao mongoApplicationConfigurationDao) {
        this.mongoApplicationConfigurationDao = mongoApplicationConfigurationDao;
    }

    public ElementRegistry getElementRegistry() {
        return elementRegistry;
    }

    @Inject
    public void setElementRegistry(@Named(ROOT) ElementRegistry elementRegistry) {
        this.elementRegistry = elementRegistry;
    }

}
