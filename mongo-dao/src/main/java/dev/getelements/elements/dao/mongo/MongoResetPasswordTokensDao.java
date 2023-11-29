package dev.getelements.elements.dao.mongo;

import dev.getelements.elements.dao.ResetPasswordTokensDao;
import dev.getelements.elements.dao.mongo.model.MongoPasswordResetToken;
import dev.morphia.Datastore;

public class MongoResetPasswordTokensDao implements ResetPasswordTokensDao {
    private MongoDBUtils mongoDBUtils;
    private Datastore datastore;


    @Override
    public String insertPasswordResetToken(int expiry, String userId, String email) {
        getMongoDBUtils().performV(ds -> getDatastore().insert(new MongoPasswordResetToken(userId, email, expiry)));

        return "";
    }


    @Override
    public int getTokenExpiry(String tokenId) {
        return 0;
    }

    @Override
    public void removeTokensForEmail(String email) {

    }

    @Override
    public void removeTokenById(String tokenId) {

    }

    private MongoDBUtils getMongoDBUtils() {
        return this.mongoDBUtils;
    }

    private Datastore getDatastore() {
        return this.datastore;
    }
}
