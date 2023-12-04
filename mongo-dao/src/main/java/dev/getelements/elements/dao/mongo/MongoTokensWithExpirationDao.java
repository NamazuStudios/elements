package dev.getelements.elements.dao.mongo;

import dev.getelements.elements.dao.TokensWithExpirationDao;
import dev.getelements.elements.dao.mongo.model.MongoTokenWithExpiration;
import dev.getelements.elements.model.token.TokenWithExpiration;
import dev.morphia.Datastore;
import dev.morphia.DeleteOptions;
import org.bson.types.ObjectId;
import org.dozer.Mapper;

import javax.inject.Inject;
import java.util.Objects;

import static dev.morphia.query.filters.Filters.eq;

public class MongoTokensWithExpirationDao implements TokensWithExpirationDao {
    private MongoDBUtils mongoDBUtils;
    private Datastore datastore;
    private Mapper dozerMapper;


    @Override
    public String insertToken(TokenWithExpiration token) {
        MongoTokenWithExpiration mongoToken = mapToken(token);
        getMongoDBUtils().performV(ds -> getDatastore().insert(mongoToken));

        return mongoToken.getId().toString();
    }

    @Override
    public int getTokenExpiry(String tokenId) {
        final var query = getDatastore().find(MongoTokenWithExpiration.class);

        query.filter(eq("_id", new ObjectId(tokenId)));

        return Objects.requireNonNull(query.first()).getExpiry();
    }

    @Override
    public void removeTokensForEmail(String email) {
        final var query = getDatastore().find(MongoTokenWithExpiration.class);

        query.filter(eq("email", email));

        query.delete(new DeleteOptions().multi(true));
    }

    @Override
    public void removeTokenById(String tokenId) {
        final var query = getDatastore().find(MongoTokenWithExpiration.class);

        query.filter(eq("_id", new ObjectId(tokenId)));

        query.delete();
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

    private MongoTokenWithExpiration mapToken(TokenWithExpiration token) {
        return getDozerMapper().map(token, MongoTokenWithExpiration.class);
    }

    public Mapper getDozerMapper() {
        return dozerMapper;
    }

    @Inject
    public void setDozerMapper(Mapper dozerMapper) {
        this.dozerMapper = dozerMapper;
    }
}
