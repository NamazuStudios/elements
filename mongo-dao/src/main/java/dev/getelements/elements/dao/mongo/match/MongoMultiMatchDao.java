package dev.getelements.elements.dao.mongo.match;

import dev.getelements.elements.dao.mongo.MongoDBUtils;
import dev.getelements.elements.dao.mongo.MongoProfileDao;
import dev.getelements.elements.dao.mongo.UpdateBuilder;
import dev.getelements.elements.dao.mongo.application.MongoApplicationConfigurationDao;
import dev.getelements.elements.dao.mongo.application.MongoApplicationDao;
import dev.getelements.elements.dao.mongo.model.application.MongoMatchmakingApplicationConfiguration;
import dev.getelements.elements.dao.mongo.query.BooleanQueryParser;
import dev.getelements.elements.rt.exception.DuplicateProfileException;
import dev.getelements.elements.sdk.ElementRegistry;
import dev.getelements.elements.sdk.Event;
import dev.getelements.elements.sdk.dao.MultiMatchDao;
import dev.getelements.elements.sdk.model.ValidationGroups;
import dev.getelements.elements.sdk.model.exception.InternalException;
import dev.getelements.elements.sdk.model.exception.InvalidDataException;
import dev.getelements.elements.sdk.model.exception.MultiMatchNotFoundException;
import dev.getelements.elements.sdk.model.exception.profile.ProfileNotFoundException;
import dev.getelements.elements.sdk.model.match.MultiMatch;
import dev.getelements.elements.sdk.model.match.MultiMatchStatus;
import dev.getelements.elements.sdk.model.profile.Profile;
import dev.getelements.elements.sdk.model.util.MapperRegistry;
import dev.getelements.elements.sdk.model.util.ValidationHelper;
import dev.morphia.Datastore;
import dev.morphia.DeleteOptions;
import dev.morphia.ModifyOptions;
import dev.morphia.UpdateOptions;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.bouncycastle.math.raw.Mod;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

import static com.google.common.base.Strings.nullToEmpty;
import static com.mongodb.client.model.ReturnDocument.AFTER;
import static dev.getelements.elements.sdk.ElementRegistry.ROOT;
import static dev.morphia.aggregation.expressions.BooleanExpressions.not;
import static dev.morphia.query.filters.Filters.eq;
import static dev.morphia.query.filters.Filters.exists;
import static dev.morphia.query.updates.UpdateOperators.set;
import static java.util.Objects.requireNonNull;

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

        final var parsed = getBooleanQueryParser()
                .parse(MongoMultiMatch.class, trimmedSearch);

        return getBooleanQueryParser()
                .parse(MongoMultiMatch.class, trimmedSearch)
                .filter(q -> getMongoDBUtils().isIndexedQuery(q))
                .map(q -> q
                        .stream()
                        .map(mmm -> getMapperRegistry().map(mmm, MultiMatch.class))
                        .toList()
                )
                .orElseGet(() -> getDatastore()
                        .find(MongoMultiMatch.class)
                        .stream()
                        .map(mmm -> getMapperRegistry().map(mmm, MultiMatch.class))
                        .toList()
                );

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

                    final var id = new MongoMultiMatchProfile.ID(mongoMultiMatch, mongoProfile);

                    final var count = getDatastore().find(MongoMultiMatchProfile.class)
                            .filter(eq("match", mongoMultiMatch))
                            .count();

                    if (count >= mongoMultiMatch.getConfiguration().getMaxProfiles()) {
                        throw new InvalidDataException("Maximum number of profiles reached for multi-match: " + mongoMultiMatch.getId());
                    }

                    final var query = getDatastore().find(MongoMultiMatchProfile.class)
                            .filter(eq("_id", id));

                    final var result = new UpdateBuilder()
                            .with(set("_id", id))
                            .with(set("match", mongoMultiMatch))
                            .with(set("profile", mongoProfile))
                            .execute(query, new UpdateOptions().upsert(true));

                    if (result.getUpsertedId() == null) {
                        throw new DuplicateProfileException();
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

                    final var id = new MongoMultiMatchProfile.ID(mongoMultiMatch, mongoProfile);

                    final var result = getDatastore().find(MongoMultiMatchProfile.class)
                            .filter(eq("_id", id))
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
                        .map(mp -> getMapperRegistry().map(mp, Profile.class))
                        .toList()
                )
                .orElseThrow(MultiMatchNotFoundException::new);
    }

    @Override
    public MultiMatch createMultiMatch(final MultiMatch multiMatch) {

        requireNonNull(multiMatch, "multiMatch");
        getValidationHelper().validateModel(multiMatch, ValidationGroups.Insert.class);

        final var mongoMatchmakingApplicationConfiguration = getMongoApplicationConfiguration(multiMatch);

        final var mongoMultiMatch = getMapperRegistry().map(multiMatch, MongoMultiMatch.class);
        mongoMultiMatch.setConfiguration(mongoMatchmakingApplicationConfiguration);
        mongoMultiMatch.setApplication(mongoMatchmakingApplicationConfiguration.getParent());

        final var inserted = getDatastore().save(mongoMultiMatch);

        final var result =  getMapperRegistry().map(inserted, MultiMatch.class);

        getElementRegistry().publish(Event.builder()
                .argument(result)
                .named(MULTI_MATCH_CREATED)
                .build()
        );

        return result;
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

        final var mongoMatchmakingApplicationConfiguration = getMongoApplicationConfiguration(multiMatch);

        final var updated = new UpdateBuilder()
                .with(set("status", multiMatch.getStatus()))
                .with(set("application", mongoMatchmakingApplicationConfiguration.getParent()))
                .with(set("configuration", mongoMatchmakingApplicationConfiguration))
                .with(set("metadata", mongoMatchmakingApplicationConfiguration.getMetadata()))
                .execute(query, new ModifyOptions().upsert(false).returnDocument(AFTER));

        if (updated == null) {
            throw new MultiMatchNotFoundException();
        }

        final var result =  getMapperRegistry().map(updated, MultiMatch.class);

        getElementRegistry().publish(Event.builder()
                .argument(result)
                .named(MULTI_MATCH_UPDATED)
                .build()
        );

        return result;

    }

    private MongoMatchmakingApplicationConfiguration getMongoApplicationConfiguration(final MultiMatch multiMatch) {

        final var applicationId = multiMatch.getConfiguration().getParent().getId();
        getValidationHelper().validateModel(multiMatch.getConfiguration());

        final var mongoApplication = getMongoApplicationDao()
                .findActiveMongoApplication(applicationId)
                .orElseThrow(() -> new InvalidDataException("Application not found: " + applicationId));

        return getMongoApplicationConfigurationDao()
                .findMongoApplicationConfiguration(
                        MongoMatchmakingApplicationConfiguration.class,
                        mongoApplication.getObjectId().toHexString(),
                        multiMatch.getConfiguration().getId()
                )
                .orElseThrow(InvalidDataException::new);

    }

    @Override
    public boolean tryDeleteMultiMatch(final String multiMatchId) {

        final var objectId = getMongoDBUtils()
                .parse(multiMatchId)
                .orElseThrow(MultiMatchNotFoundException::new);

        final var multiMatchQuery = getDatastore().find(MongoMultiMatch.class)
                .filter(eq("_id", objectId));

        final var mongoMultiMatch = multiMatchQuery.first();

        if (mongoMultiMatch == null) {
            return false;
        }

        getDatastore().find(MongoMultiMatchProfile.class)
                .filter(eq("match", mongoMultiMatch))
                .delete(new DeleteOptions().multi(true));

        final var result = multiMatchQuery.delete();

        if (result.getDeletedCount() > 1) {
            throw new InternalException("More than one multi-match deleted, this should not happen.");
        }

        final var deleted = getMapperRegistry().map(mongoMultiMatch, MultiMatch.class);

        getElementRegistry().publish(Event.builder()
                .argument(deleted)
                .named(MULTI_MATCH_DELETED)
                .build()
        );

        getDatastore().find(MongoMultiMatchProfile.class)
                .filter(eq("match", mongoMultiMatch))
                .delete();

        return true;

    }

    @Override
    public boolean tryExpireMultiMatch(final String multiMatchId) {

        final var objectId = getMongoDBUtils()
                .parse(multiMatchId)
                .orElseThrow(MultiMatchNotFoundException::new);

        final var multiMatchQuery = getDatastore()
                .find(MongoMultiMatch.class)
                .filter(eq("_id", objectId));

        final var mongoMultiMatch = multiMatchQuery.first();

        if (mongoMultiMatch == null) {
            return false;
        }

        final var multiMatchProfileQuery = getDatastore().find(MongoMultiMatchProfile.class)
                .filter(eq("match", mongoMultiMatch));

        final var now = new Timestamp(System.currentTimeMillis());
        final var updates = new UpdateBuilder().with(set("expiry", now));

        updates.execute(
                multiMatchProfileQuery,
                new UpdateOptions().multi(true)
        );

        final var expiredMultiMatch = updates.execute(
                multiMatchQuery,
                new ModifyOptions().returnDocument(AFTER)
        );

        if (expiredMultiMatch == null) {
            return false;
        }

        final var expired = getMapperRegistry().map(expiredMultiMatch, MultiMatch.class);

        getElementRegistry().publish(Event.builder()
                .argument(expired)
                .named(MULTI_MATCH_EXPIRED)
                .build()
        );

        getElementRegistry().publish(Event.builder()
                .argument(expired)
                .named(MULTI_MATCH_UPDATED)
                .build()
        );

        return true;

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
