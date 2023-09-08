package dev.getelements.elements.dao.mongo.formidium;

import dev.getelements.elements.dao.FormidiumInvestorDao;
import dev.getelements.elements.dao.mongo.MongoDBUtils;
import dev.getelements.elements.dao.mongo.MongoUserDao;
import dev.getelements.elements.dao.mongo.model.formidium.MongoFormidiumInvestor;
import dev.getelements.elements.dao.mongo.model.formidium.MongoFormidiumInvestorId;
import dev.getelements.elements.exception.NotFoundException;
import dev.getelements.elements.model.Pagination;
import dev.getelements.elements.model.formidium.FormidiumInvestor;
import dev.getelements.elements.util.ValidationHelper;
import dev.morphia.Datastore;
import org.dozer.Mapper;

import javax.inject.Inject;
import java.util.Optional;

import static dev.morphia.query.filters.Filters.eq;

public class MongoFormidiumInvestorDao implements FormidiumInvestorDao {

    private Mapper mapper;

    private Datastore datastore;

    private MongoUserDao mongoUserDao;

    private MongoDBUtils mongoDBUtils;

    private ValidationHelper validationHelper;

    @Override
    public FormidiumInvestor createInvestor(final String formidiumInvestorId, final String userId) {

        final var mongoUser = getMongoUserDao().getActiveMongoUser(userId);
        final var mongoFormidiumInvestorId = new MongoFormidiumInvestorId(mongoUser.getObjectId());

        final var mongoFormidiumInvestor = new MongoFormidiumInvestor();
        mongoFormidiumInvestor.setId(mongoFormidiumInvestorId);
        mongoFormidiumInvestor.setUser(mongoUser);
        mongoFormidiumInvestor.setFormidiumInvestorId(formidiumInvestorId);
        getMongoDBUtils().performV(d -> getDatastore().insert(mongoFormidiumInvestor));

        return getMapper().map(mongoFormidiumInvestor, FormidiumInvestor.class);
    }

    @Override
    public Pagination<FormidiumInvestor> getFormidiumInvestors(final String userId, final int offset, final int count) {

        final var query = getDatastore().find(MongoFormidiumInvestor.class);

        if (userId != null) {
                getMongoUserDao()
                        .findActiveMongoUser(userId)
                        .ifPresent(u -> query.filter(eq("user", u)));
        }

        return getMongoDBUtils().paginationFromQuery(query, offset, count, FormidiumInvestor.class);

    }

    @Override
    public Optional<FormidiumInvestor> findFormidiumInvestor(final String formidiumInvestorId, final String userId) {

        final var mongoFormidiumInvestorId = MongoFormidiumInvestorId
            .tryParse(formidiumInvestorId)
            .orElseThrow(NotFoundException::new);

        final var query = getDatastore()
                .find(MongoFormidiumInvestor.class)
                .filter(eq("_id", mongoFormidiumInvestorId));

        if (userId != null) {
            final var mongoUser = getMongoUserDao().getActiveMongoUser(userId);
            query.filter(eq("user", mongoUser));
        }

        final var mongoFormidiumInvestor = query.first();
        return Optional.ofNullable(mongoFormidiumInvestor).map(o -> getMapper().map(o, FormidiumInvestor.class));

    }

    @Override
    public void deleteFormidiumInvestor(final String formidiumInvestorId) {

        final var mongoFormidiumInvestorId = MongoFormidiumInvestorId
                .tryParse(formidiumInvestorId)
                .orElseThrow(NotFoundException::new);

        final var result = getDatastore()
                .find(MongoFormidiumInvestor.class)
                .filter(eq("_id", mongoFormidiumInvestorId))
                .delete();

        if (result.getDeletedCount() == 0) {
            throw new NotFoundException();
        }

    }

    public Mapper getMapper() {
        return mapper;
    }

    @Inject
    public void setMapper(Mapper mapper) {
        this.mapper = mapper;
    }

    public Datastore getDatastore() {
        return datastore;
    }

    @Inject
    public void setDatastore(Datastore datastore) {
        this.datastore = datastore;
    }

    public MongoUserDao getMongoUserDao() {
        return mongoUserDao;
    }

    @Inject
    public void setMongoUserDao(MongoUserDao mongoUserDao) {
        this.mongoUserDao = mongoUserDao;
    }

    public MongoDBUtils getMongoDBUtils() {
        return mongoDBUtils;
    }

    @Inject
    public void setMongoDBUtils(MongoDBUtils mongoDBUtils) {
        this.mongoDBUtils = mongoDBUtils;
    }

    public ValidationHelper getValidationHelper() {
        return validationHelper;
    }

    @Inject
    public void setValidationHelper(ValidationHelper validationHelper) {
        this.validationHelper = validationHelper;
    }

}
