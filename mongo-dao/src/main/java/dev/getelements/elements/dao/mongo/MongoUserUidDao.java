package dev.getelements.elements.dao.mongo;

import com.mongodb.DuplicateKeyException;
import com.mongodb.InsertOptions;
import com.mongodb.MongoWriteException;
import dev.getelements.elements.sdk.dao.Transaction;
import dev.getelements.elements.sdk.dao.UserUidDao;
import dev.getelements.elements.dao.mongo.model.MongoUser;
import dev.getelements.elements.dao.mongo.model.MongoUserUid;
import dev.getelements.elements.dao.mongo.model.MongoUserUidScheme;
import dev.getelements.elements.sdk.model.exception.DuplicateException;
import dev.getelements.elements.sdk.model.exception.InvalidDataException;
import dev.getelements.elements.sdk.model.exception.user.UserNotFoundException;
import dev.getelements.elements.sdk.model.Pagination;
import dev.getelements.elements.sdk.model.user.User;
import dev.getelements.elements.sdk.model.user.UserUid;
import dev.getelements.elements.sdk.model.util.MapperRegistry;
import dev.getelements.elements.sdk.model.util.ValidationHelper;
import dev.morphia.Datastore;
import dev.morphia.DeleteOptions;
import dev.morphia.ModifyOptions;
import dev.morphia.query.FindOptions;
import dev.morphia.query.Query;
import dev.morphia.query.filters.Filters;
import jakarta.inject.Inject;
import jakarta.inject.Provider;
import org.bson.types.ObjectId;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.regex.Pattern;

import static com.google.common.base.Strings.nullToEmpty;
import static com.mongodb.client.model.ReturnDocument.AFTER;
import static dev.morphia.query.filters.Filters.*;
import static dev.morphia.query.updates.UpdateOperators.set;
import static dev.morphia.query.updates.UpdateOperators.unset;

public class MongoUserUidDao implements UserUidDao {

    private Datastore datastore;

    private ValidationHelper validationHelper;

    private MongoDBUtils mongoDBUtils;

    private MapperRegistry dozerMapperRegistry;

    private MongoUserDao mongoUserDao;

    private Provider<Transaction> transactionProvider;

    @Override
    public Pagination<UserUid> getUserUids(int offset, int count, String search) {

        final String trimmedQueryString = nullToEmpty(search).trim();

        if (trimmedQueryString.isEmpty()) {
            throw new InvalidDataException("queryString must be specified.");
        }

        final Query<MongoUserUid> query = getDatastore().find(MongoUserUid.class);

        if(ObjectId.isValid(trimmedQueryString)) {
            final var user = datastore.find(MongoUser.class)
                    .filter(Filters.eq("_id", new ObjectId(search)))
                    .first();

            if(user != null) {
                query.filter(Filters.eq("user", user));
                return paginationFromQuery(query, offset, count);
            }
        }

        query.filter(
                or(
                        Filters.regex("_id.scheme", Pattern.compile(search)),
                        Filters.regex("_id.id", Pattern.compile(search))
                )
        );

        return paginationFromQuery(query, offset, count);
    }

    @Override
    public UserUid getUserUid(String id, String scheme) {
        return findUserUid(id, scheme)
                .orElseThrow(() -> new UserNotFoundException("UserUid with id " + id + " and/or scheme " + scheme + " not found."));
    }

    public Optional<UserUid> findUserUid(String id, String scheme) {

        if (id.isBlank() || scheme.isBlank()) throw new InvalidDataException("id AND scheme must be specified.");

        final var query = getDatastore().find(MongoUserUid.class);

        final var compoundId = new MongoUserUidScheme();
        compoundId.setId(id);
        compoundId.setScheme(scheme);

        query.filter(
                eq("_id", compoundId)
        );

        final var mongoUserUid = query.first();

        return mongoUserUid == null
                ? Optional.empty()
                : Optional.of(getDozerMapperRegistry().map(mongoUserUid, UserUid.class));

    }

    @Override
    public UserUid createUserUidStrict(UserUid userUid) {
        return createUserUid(userUid);
    }

    @Override
    public UserUid createUserUid(UserUid userUid) {
        validate(userUid);

        final var user = getMongoUserForId(userUid.getUserId());
        final var id = createSchemeId(userUid);
        final MongoUserUid mongoUserUid = new MongoUserUid();
        mongoUserUid.setUser(user);
        mongoUserUid.setId(id);

        try {
            getDatastore().insert(mongoUserUid);
        } catch (MongoWriteException ex) {
            throw new DuplicateException(ex);
        }

        addLinkedAccount(user, userUid);

        return getDozerMapperRegistry().map(mongoUserUid, UserUid.class);
    }

    @Override
    public void softDeleteUserUidsForUserId(final User user) {

        getTransactionProvider().get().performAndClose(txn -> {

            //Remove all UserUIDs for User
            final var mongoUser = getDozerMapperRegistry().map(user, MongoUser.class);

            getDatastore().find(MongoUserUid.class)
                    .filter(
                            eq("user", mongoUser)
                    ).delete(new DeleteOptions().multi(true));

            //Clear all linkedAccounts
            final var objectId = getMongoDBUtils().parseOrThrow(user.getId(), UserNotFoundException::new);
            final var query = getDatastore().find(MongoUser.class);

            query.filter(
                    and(
                            eq("_id", objectId)
                    )
            );

            final var builder = new UpdateBuilder();

            builder.with(
                    unset("linkedAccounts")
            );

            return getMongoDBUtils().perform(ds ->
                    builder.execute(query, new ModifyOptions().upsert(false))
            );
        });
    }

    private Pagination<UserUid> paginationFromQuery(final Query<MongoUserUid> query, final int offset, final int count) {
        return getMongoDBUtils().paginationFromQuery(query, offset, count, u -> getDozerMapperRegistry().map(u, UserUid.class), new FindOptions());
    }

    private void validate(final UserUid userUid) {

        if (userUid == null) {
            throw new InvalidDataException("User must not be null.");
        }

        getValidationHelper().validateModel(userUid);
    }

    private MongoUser getMongoUserForId(final String userId) {
        return getMongoUserDao().getMongoUser(userId);
    }

    private MongoUserUidScheme createSchemeId(final UserUid userUid) {
        final MongoUserUidScheme scheme = new MongoUserUidScheme();
        scheme.setId(userUid.getId());
        scheme.setScheme(userUid.getScheme());
        return scheme;
    }

    private void addLinkedAccount(final MongoUser user, final UserUid userUid) {

        getTransactionProvider().get().performAndClose(txn -> {

            if (user.getLinkedAccounts() == null) {
                user.setLinkedAccounts(new HashSet<>());
            }

            user.getLinkedAccounts().add(userUid.getScheme());

            final var query = getDatastore().find(MongoUser.class);

            query.filter(
                    and(
                            eq("_id", user.getObjectId())
                    )
            );

            final var builder = new UpdateBuilder();

            builder.with(
                    set("linkedAccounts", user.getLinkedAccounts())
            );

            return getMongoDBUtils().perform(ds ->
                    builder.execute(query, new ModifyOptions().upsert(true))
            );
        });

    }

    public Datastore getDatastore() {
        return datastore;
    }

    @Inject
    public void setDatastore(Datastore datastore) {
        this.datastore = datastore;
    }

    public ValidationHelper getValidationHelper() {
        return validationHelper;
    }

    @Inject
    public void setValidationHelper(ValidationHelper validationHelper) {
        this.validationHelper = validationHelper;
    }

    public MongoDBUtils getMongoDBUtils() {
        return mongoDBUtils;
    }

    @Inject
    public void setMongoDBUtils(MongoDBUtils mongoDBUtils) {
        this.mongoDBUtils = mongoDBUtils;
    }

    public MapperRegistry getDozerMapperRegistry() {
        return dozerMapperRegistry;
    }

    @Inject
    public void setDozerMapperRegistry(MapperRegistry dozerMapperRegistry) {
        this.dozerMapperRegistry = dozerMapperRegistry;
    }

    public MongoUserDao getMongoUserDao() {
        return mongoUserDao;
    }

    @Inject
    public void setMongoUserDao(MongoUserDao mongoUserDao) {
        this.mongoUserDao = mongoUserDao;
    }

    public Provider<Transaction> getTransactionProvider() {
        return transactionProvider;
    }

    @Inject
    public void setTransactionProvider(Provider<Transaction> transactionProvider) {
        this.transactionProvider = transactionProvider;
    }

}
