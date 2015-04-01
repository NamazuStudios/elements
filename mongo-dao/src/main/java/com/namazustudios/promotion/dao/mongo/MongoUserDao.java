package com.namazustudios.promotion.dao.mongo;

import com.google.common.base.Function;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.mongodb.Mongo;
import com.mongodb.MongoException;
import com.namazustudios.promotion.dao.UserDao;
import com.namazustudios.promotion.dao.mongo.model.MongoUser;
import com.namazustudios.promotion.exception.InvalidDataException;
import com.namazustudios.promotion.exception.NotFoundException;
import com.namazustudios.promotion.model.Pagination;
import com.namazustudios.promotion.model.User;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.query.Query;
import org.mongodb.morphia.query.UpdateOperations;
import org.mongodb.morphia.query.UpdateResults;
import org.omg.CORBA.DynAnyPackage.Invalid;
import sun.plugin2.message.Message;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;
import java.security.MessageDigest;
import java.util.Arrays;

/**
 * Created by patricktwohig on 3/26/15.
 */
@Singleton
public class MongoUserDao implements UserDao {

    @Inject
    private Datastore datastore;

    @Inject
    @Named("com.namazustudios.promotion.query.max.results")
    private int queryMaxResults;

    @Inject
    @Named("com.namazustudios.promotion.password.digest")
    private Provider<MessageDigest> messageDigestProvider;

    @Override
    public User getUser(String userId) {

        final Query<MongoUser> query = datastore.createQuery(MongoUser.class);

        query.or(
                query.criteria("name").equal(userId),
                query.criteria("email").equal(userId)
        ).and(
                query.criteria("active").equal(true)
        );

        final MongoUser mongoUser = query.get();
        return transform(mongoUser);

    }

    @Override
    public Pagination<User> getUsers(int offset, int count) {

        final Query<MongoUser> query = datastore.createQuery(MongoUser.class);

        query.filter("active =", true);
        query.offset(offset).limit(Math.min(queryMaxResults, count));

        final Pagination<User> users = new Pagination<>();

        users.setOffset(offset);
        users.setTotal((int)query.getCollection().getCount());

        final Iterable<User> userIterable = Iterables.transform(query, new Function<MongoUser, User>() {
            @Override
            public User apply(MongoUser input) {
                return transform(input);
            }
        });

        users.setObjects(Lists.newArrayList(userIterable));

        return users;

    }

    @Override
    public User createUser(User user) {

        validate(user);

        final MongoUser mongoUser = new MongoUser();

        mongoUser.setName(user.getName());
        mongoUser.setEmail(user.getEmail());
        mongoUser.setLevel(user.getLevel());

        datastore.save(mongoUser);
        return transform(mongoUser);

    }

    @Override
    public User updateUser(User user) {

        validate(user);

        final Query<MongoUser> query = datastore.createQuery(MongoUser.class);
        final UpdateOperations<MongoUser> operations = datastore.createUpdateOperations(MongoUser.class);

        query.and(
                query.criteria("name").equal(user.getName()),
                query.criteria("email").equal(user.getEmail())
        );

        operations.set("name", user.getName());
        operations.set("email", user.getEmail());
        operations.set("level", user.getLevel());

        final MongoUser mongoUser = datastore.findAndModify(query, operations);

        if (mongoUser == null) {
            throw new NotFoundException("User with email/username does not exist: " +  user.getEmail() + "/" + user.getName());
        }

        return transform(mongoUser);
    }

    @Override
    public void deleteUser(String userId) {

        final Query<MongoUser> query = datastore.createQuery(MongoUser.class);
        final UpdateOperations<MongoUser> operations = datastore.createUpdateOperations(MongoUser.class);

        query.or(
                query.criteria("name").equal(userId),
                query.criteria("email").equal(userId)
        ).and(
                query.criteria("active").equal(true)
        );

        operations.set("active", false);

        final MongoUser mongoUser = datastore.findAndModify(query, operations);

        if (mongoUser == null) {
            throw new NotFoundException("User with userid does not exist:" + userId);
        }

    }

    @Override
    public User updateUserPassword(String userId, String password) {
        return null;
    }

    private User transform(final MongoUser mongoUser) {

        final User user = new User();

        user.setName(mongoUser.getObjectId());
        user.setEmail(mongoUser.getEmail());
        user.setLevel(mongoUser.getLevel());

        return user;

    }

    public void validate(final User user) {

        if (user == null) {
            throw new InvalidDataException("User must not be null.");
        }

        user.setEmail(Strings.emptyToNull(user.getEmail()).trim());
        user.setName(Strings.emptyToNull(user.getName()).trim());

        if (Strings.isNullOrEmpty(user.getEmail())) {
            throw new InvalidDataException("Email must not be null.");
        }

        if (Strings.isNullOrEmpty(user.getName())) {
            throw new InvalidDataException("User name must not be null.");
        }

        if (user.getLevel() == null) {
            throw new InvalidDataException("User level must be specified.");
        }

    }

}
