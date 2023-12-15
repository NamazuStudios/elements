package dev.getelements.elements.dao.mongo;

import com.mongodb.client.result.DeleteResult;
import dev.getelements.elements.dao.TokensWithExpirationDao;
import dev.getelements.elements.dao.mongo.model.MongoTokenWithExpiration;
import dev.getelements.elements.dao.mongo.model.mission.MongoMission;
import dev.getelements.elements.exception.NotFoundException;
import dev.getelements.elements.model.token.TokenWithExpiration;
import dev.getelements.elements.model.user.User;
import dev.morphia.Datastore;
import dev.morphia.DeleteOptions;
import dev.morphia.query.Query;
import org.bson.types.ObjectId;
import org.dozer.Mapper;

import javax.inject.Inject;
import java.sql.Timestamp;
import java.util.Objects;

import static dev.morphia.query.filters.Filters.eq;

public class MongoTokensWithExpirationDao implements TokensWithExpirationDao {
    private MongoDBUtils mongoDBUtils;
    private Datastore datastore;
    private Mapper dozerMapper;


    @Override
    public String createToken(TokenWithExpiration token) {
        MongoTokenWithExpiration mongoToken = mapToken(token);
        getMongoDBUtils().performV(ds -> getDatastore().insert(mongoToken));

        return mongoToken.getId().toString();
    }

    @Override
    public Timestamp getTokenExpiry(String tokenId) {
        final var query = getDatastore().find(MongoTokenWithExpiration.class);

        query.filter(eq("_id", new ObjectId(tokenId)));

        return Objects.requireNonNull(query.first()).getExpiry();
    }

    @Override
    public void deleteTokensByUser(User user) {
        final var query = getDatastore().find(MongoTokenWithExpiration.class);

        query.filter(eq("email", user.getEmail()));

        query.delete(new DeleteOptions().multi(true));
    }

    @Override
    public void deleteToken(String tokenId) {
        final ObjectId id = getMongoDBUtils().parseOrThrowNotFoundException(tokenId);

        final var query = getDatastore().find(MongoTokenWithExpiration.class);
        query.filter(eq("_id", id));

        final DeleteResult deleteResult = query.delete();

        if (deleteResult.getDeletedCount() == 0) {
            throw new NotFoundException("Token not found: " + tokenId);
        }
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
