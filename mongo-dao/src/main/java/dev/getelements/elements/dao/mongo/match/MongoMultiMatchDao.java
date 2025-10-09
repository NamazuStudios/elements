package dev.getelements.elements.dao.mongo.match;

import dev.getelements.elements.dao.mongo.MongoDBUtils;
import dev.getelements.elements.dao.mongo.MongoProfileDao;
import dev.getelements.elements.dao.mongo.UpdateBuilder;
import dev.getelements.elements.dao.mongo.application.MongoApplicationConfigurationDao;
import dev.getelements.elements.dao.mongo.application.MongoApplicationDao;
import dev.getelements.elements.dao.mongo.model.MongoProfile;
import dev.getelements.elements.dao.mongo.model.application.MongoMatchmakingApplicationConfiguration;
import dev.getelements.elements.dao.mongo.query.BooleanQueryParser;
import dev.getelements.elements.rt.exception.DuplicateProfileException;
import dev.getelements.elements.sdk.ElementRegistry;
import dev.getelements.elements.sdk.Event;
import dev.getelements.elements.sdk.dao.MultiMatchDao;
import dev.getelements.elements.sdk.model.Pagination;
import dev.getelements.elements.sdk.model.ValidationGroups;
import dev.getelements.elements.sdk.model.application.MatchmakingApplicationConfiguration;
import dev.getelements.elements.sdk.model.exception.InternalException;
import dev.getelements.elements.sdk.model.exception.InvalidDataException;
import dev.getelements.elements.sdk.model.exception.InvalidMultiMatchPhaseException;
import dev.getelements.elements.sdk.model.exception.MultiMatchNotFoundException;
import dev.getelements.elements.sdk.model.exception.profile.ProfileNotFoundException;
import dev.getelements.elements.sdk.model.match.MultiMatch;
import dev.getelements.elements.sdk.model.profile.Profile;
import dev.getelements.elements.sdk.model.util.MapperRegistry;
import dev.getelements.elements.sdk.model.util.ValidationHelper;
import dev.morphia.Datastore;
import dev.morphia.DeleteOptions;
import dev.morphia.ModifyOptions;
import dev.morphia.query.FindOptions;
import dev.morphia.query.Query;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.google.common.base.Strings.nullToEmpty;
import static com.mongodb.client.model.ReturnDocument.AFTER;
import static dev.getelements.elements.sdk.ElementRegistry.ROOT;
import static dev.getelements.elements.sdk.model.match.MultiMatchStatus.*;
import static dev.morphia.query.Sort.ascending;
import static dev.morphia.query.filters.Filters.*;
import static dev.morphia.query.updates.UpdateOperators.set;
import static java.lang.System.currentTimeMillis;
import static java.util.Objects.requireNonNull;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;

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

    private MapperRegistry dozerMapperRegistry;

    @Override
    public List<MultiMatch> getAllMultiMatches(final String search) {

        final var trimmedSearch = nullToEmpty(search).trim();

        return getBooleanQueryParser()
                .parse(getBaseQuery(), trimmedSearch)
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
    public Pagination<MultiMatch> getMultiMatches(int offset, int count, String search) {

        final var trimmedSearch = nullToEmpty(search).trim();

        var query = getBaseQuery();

        if (!trimmedSearch.isEmpty()) {

            var parsedQuery = getBooleanQueryParser().parse(MongoMultiMatch.class, trimmedSearch);

            if (parsedQuery.isPresent()) {
                query = parsedQuery.get();
            } else {
                query.filter(text(trimmedSearch));
            }

        }

        return getMongoDBUtils().paginationFromQuery(
                query,
                offset,
                count,
                f -> getDozerMapper().map(f, MultiMatch.class)
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
                .flatMap(objectId -> getBaseQuery()
                        .filter(eq("_id", objectId))
                        .stream()
                        .findFirst()
                );
    }

    @Override
    public Optional<MultiMatch> findOldestAvailableMultiMatchCandidate(
            final MatchmakingApplicationConfiguration configuration,
            final String profileId,
            final String search) {

        final var trimmedSearch = nullToEmpty(search).trim();
        final var mongoProfile = getMongoProfileDao().getActiveMongoProfile(profileId);
        final var mongoMatchmakingApplicationConfiguration = getMongoApplicationConfigurationDao()
                .findMongoApplicationConfiguration(MongoMatchmakingApplicationConfiguration.class,
                        configuration.getParent().getId(),
                        configuration.getId()
                );

        var query = getBaseQuery();

        if (!trimmedSearch.isEmpty()) {

            var parsedQuery = getBooleanQueryParser().parse(MongoMultiMatch.class, trimmedSearch);

            if (parsedQuery.isPresent()) {
                query = parsedQuery.get();
            } else {
                query.filter(text(trimmedSearch));
            }

        }

        final var options = new FindOptions().sort(ascending("created"));

        return query
                .filter(eq("status", OPEN))
                .filter(eq("configuration", mongoMatchmakingApplicationConfiguration))
                .filter(ne("profiles", mongoProfile))                .stream(options)
                .findFirst()
                .map(mmm -> getMapperRegistry().map(mmm, MultiMatch.class));

    }

    @Override
    public MultiMatch addProfile(final String multiMatchId, final Profile profile) {

        final var query = getMongoDBUtils()
                .parse(multiMatchId)
                .map(objectId -> getBaseQuery().filter(eq("_id", objectId)))
                .orElseThrow(MultiMatchNotFoundException::new);

        final var mongoProfile = getMongoProfileDao().getActiveMongoProfile(profile);
        final var mongoProfileId = mongoProfile.getObjectId();
        final var mongoMultiMatch = query
                .stream()
                .findFirst()
                .orElseThrow(MultiMatchNotFoundException::new);

        if (!OPEN.equals(mongoMultiMatch.getStatus())) {
            throw new InvalidMultiMatchPhaseException(mongoMultiMatch.getStatus(), OPEN);
        }

        var profiles = mongoMultiMatch.getProfiles();
        profiles = profiles == null ? new ArrayList<>() : new ArrayList<>(profiles);

        if (profiles.size() >= mongoMultiMatch.getConfiguration().getMaxProfiles()) {
            throw new InvalidDataException("Maximum number of profiles reached for multi-match: " + mongoMultiMatch.getId());
        }

        final var exists = profiles
                .stream()
                .map(MongoProfile::getObjectId)
                .anyMatch(id -> id.equals(mongoProfileId));

        if (exists) {
            throw new DuplicateProfileException();
        }

        profiles.add(mongoProfile);

        final var updated = new UpdateBuilder()
                .with(set("profiles", profiles))
                .with(set("status", profiles.size() >= mongoMultiMatch.getConfiguration().getMaxProfiles()
                        ? FULL
                        : OPEN
                ))
                .execute(query, new ModifyOptions().returnDocument(AFTER));

        final var multiMatch = getMapperRegistry().map(updated, MultiMatch.class);

        getElementRegistry().publish(Event.builder()
                .argument(multiMatch)
                .argument(getMapperRegistry().map(mongoProfile, Profile.class))
                .named(MULTI_MATCH_ADD_PROFILE)
                .build()
        );

        return multiMatch;

    }

    @Override
    public MultiMatch removeProfile(final String multiMatchId, final Profile profile) {

        final var query = getMongoDBUtils()
                .parse(multiMatchId)
                .map(objectId -> getBaseQuery().filter(eq("_id", objectId)))
                .orElseThrow(MultiMatchNotFoundException::new);

        final var mongoProfile = getMongoProfileDao().getActiveMongoProfile(profile);
        final var mongoProfileId = mongoProfile.getObjectId();
        final var mongoMultiMatch = query
                .stream()
                .findFirst()
                .orElseThrow(MultiMatchNotFoundException::new);

        var profiles = mongoMultiMatch.getProfiles();

        profiles = profiles == null
                ? new ArrayList<>()
                : new ArrayList<>(profiles);

        final var removed = profiles.removeIf(p -> p.getObjectId().equals(mongoProfileId));

        if (!removed) {
            throw new ProfileNotFoundException();
        }

        final var updated = new UpdateBuilder()
                .with(set("profiles", profiles))
                .execute(query, new ModifyOptions().returnDocument(AFTER));

        final var multiMatch = getMapperRegistry().map(updated, MultiMatch.class);

        getElementRegistry().publish(Event.builder()
                .argument(multiMatch)
                .argument(getMapperRegistry().map(mongoProfile, Profile.class))
                .named(MULTI_MATCH_REMOVE_PROFILE)
                .build()
        );

        return multiMatch;

    }

    @Override
    public List<Profile> getProfiles(final String multiMatchId) {
        return findMongoMultiMatch(multiMatchId)
                .map(mongoMultiMatch -> mongoMultiMatch.getProfiles()
                        .stream()
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

        final var expiry = new Timestamp(currentTimeMillis() + MILLISECONDS.convert(
                mongoMatchmakingApplicationConfiguration.getTimeoutSeconds(),
                SECONDS
        ));

        final var mongoMultiMatch = getMapperRegistry().map(multiMatch, MongoMultiMatch.class);
        mongoMultiMatch.setExpiry(expiry);
        mongoMultiMatch.setCreated(new Timestamp(currentTimeMillis()));
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
    public MultiMatch updateMultiMatch(final String matchId, final MultiMatch multiMatch) {

        requireNonNull(multiMatch, "multiMatch");
        requireNonNull(matchId, "matchId");
        getValidationHelper().validateModel(multiMatch, ValidationGroups.Update.class);

        final var objectId = getMongoDBUtils()
                .parse(matchId)
                .orElseThrow(MultiMatchNotFoundException::new);

        final var query = getDatastore().find(MongoMultiMatch.class)
                .filter(eq("_id", objectId));

        final var mongoMatchmakingApplicationConfiguration = getMongoApplicationConfiguration(multiMatch);
        final var mongoMultiMatch = getMapperRegistry().map(multiMatch, MongoMultiMatch.class);

        final var updated = new UpdateBuilder()
                .with(set("status", mongoMultiMatch.getStatus()))
                .with(set("metadata", mongoMultiMatch.getMetadata()))
                .with(set("expiry", mongoMultiMatch.getExpiry()))
                .with(set("application", mongoMatchmakingApplicationConfiguration.getParent()))
                .with(set("configuration", mongoMatchmakingApplicationConfiguration))
                .execute(query, new ModifyOptions().upsert(false).returnDocument(AFTER));

        if (updated == null) {
            throw new MultiMatchNotFoundException();
        }

        final var result = getMapperRegistry().map(updated, MultiMatch.class);

        getElementRegistry().publish(Event.builder()
                .argument(result)
                .named(MULTI_MATCH_UPDATED)
                .build()
        );

        return result;

    }

    @Override
    public MultiMatch openMatch(final String multiMatchId) {

        final var objectId = getMongoDBUtils()
                .parse(multiMatchId)
                .orElseThrow(MultiMatchNotFoundException::new);

        final var query = getDatastore()
                .find(MongoMultiMatch.class)
                .filter(eq("_id", objectId));

        final var existing = query
                .stream()
                .findFirst()
                .orElseThrow(MultiMatchNotFoundException::new);

        query.filter(eq("status", CLOSED));

        final var updated = new UpdateBuilder()
                .with(set("status", OPEN))
                .execute(query, new ModifyOptions().returnDocument(AFTER));

        if (updated == null) {
            throw new InvalidMultiMatchPhaseException(existing.getStatus(), CLOSED);
        }

        return getMapperRegistry().map(updated, MultiMatch.class);

    }

    @Override
    public MultiMatch closeMatch(final String multiMatchId) {

        final var objectId = getMongoDBUtils()
                .parse(multiMatchId)
                .orElseThrow(MultiMatchNotFoundException::new);

        final var query = getDatastore()
                .find(MongoMultiMatch.class)
                .filter(eq("_id", objectId));

        final var existing = query
                .stream()
                .findFirst()
                .orElseThrow(MultiMatchNotFoundException::new);

        query.filter(or(
                eq("status", OPEN),
                eq("status", FULL)
        ));

        final var updated = new UpdateBuilder()
                .with(set("status", CLOSED))
                .execute(query, new ModifyOptions().returnDocument(AFTER));

        if (updated == null) {
            throw new InvalidMultiMatchPhaseException(existing.getStatus(), CLOSED);
        }

        return getMapperRegistry().map(updated, MultiMatch.class);

    }

    @Override
    public MultiMatch endMatch(final String multiMatchId) {

        final var objectId = getMongoDBUtils()
                .parse(multiMatchId)
                .orElseThrow(MultiMatchNotFoundException::new);

        final var query = getDatastore()
                .find(MongoMultiMatch.class)
                .filter(eq("_id", objectId));

        final var existing = query
                .stream()
                .findFirst()
                .orElseThrow(MultiMatchNotFoundException::new);

        final var expiry = new Timestamp(currentTimeMillis() + MILLISECONDS.convert(
                existing.getConfiguration().getLingerSeconds(),
                SECONDS
        ));

        query.filter(or(
                eq("status", OPEN),
                eq("status", FULL),
                eq("status", CLOSED)
        ));

        final var updated = new UpdateBuilder()
                .with(set("status", CLOSED))
                .with(set("expiry", expiry))
                .execute(query, new ModifyOptions().returnDocument(AFTER));

        if (updated == null) {
            throw new InvalidMultiMatchPhaseException(existing.getStatus(), CLOSED);
        }

        return getMapperRegistry().map(updated, MultiMatch.class);

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

        final var now = new Timestamp(currentTimeMillis());
        final var updates = new UpdateBuilder().with(set("expiry", now));

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

    @Override
    public void deleteAllMultiMatches() {

        datastore.find(MongoMultiMatch.class)
                 .delete(new DeleteOptions().multi(true));

        getElementRegistry().publish(Event.builder()
                .named(MULTI_MATCH_DELETED)
                .build()
        );

    }

    public Query<MongoMultiMatch> getBaseQuery() {
        final var now = new Timestamp(currentTimeMillis());
        return getDatastore().find(MongoMultiMatch.class).filter(lt("expiry", now));
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

    public MapperRegistry getDozerMapper() {
        return dozerMapperRegistry;
    }

    @Inject
    public void setDozerMapper(MapperRegistry dozerMapperRegistry) {
        this.dozerMapperRegistry = dozerMapperRegistry;
    }

}
