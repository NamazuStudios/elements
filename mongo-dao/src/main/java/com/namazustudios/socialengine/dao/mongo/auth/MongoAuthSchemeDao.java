package com.namazustudios.socialengine.dao.mongo.auth;

import com.namazustudios.socialengine.dao.AuthSchemeDao;
import com.namazustudios.socialengine.dao.mongo.MongoDBUtils;
import com.namazustudios.socialengine.dao.mongo.model.auth.MongoAuthScheme;
import com.namazustudios.socialengine.exception.NotFoundException;
import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.ValidationGroups;
import com.namazustudios.socialengine.model.auth.*;
import com.namazustudios.socialengine.util.ValidationHelper;
import dev.morphia.Datastore;
import dev.morphia.query.FindOptions;
import dev.morphia.query.Query;
import dev.morphia.query.experimental.filters.Filters;
import org.dozer.Mapper;

import javax.inject.Inject;
import java.util.List;

public class MongoAuthSchemeDao implements AuthSchemeDao {

    private MongoDBUtils mongoDBUtils;

    private Datastore datastore;

    private Mapper beanMapper;

    private ValidationHelper validationHelper;

    @Override
    public Pagination<AuthScheme> getAuthSchemes(int offset, int count, List<String> tags) {

        final Query<MongoAuthScheme> mongoQuery = getDatastore().find(MongoAuthScheme.class);

        if (tags != null && !tags.isEmpty()) {
            mongoQuery.filter(Filters.in("tags", tags));
        }

        return getMongoDBUtils().paginationFromQuery(mongoQuery, offset, count, input -> transform(input), new FindOptions());
    }

    @Override
    public AuthScheme getAuthScheme(String authSchemeId) {
        var mongoAuthScheme = getDatastore().find(MongoAuthScheme.class)
                .filter(Filters.or(
                                Filters.eq("_id", authSchemeId)
                        )
                ).first();

        if(null == mongoAuthScheme) {
            throw new NotFoundException("Unable to find auth scheme with an id of " + authSchemeId);
        }

        return transform(mongoAuthScheme);
    }

    @Override
    public UpdateAuthSchemeResponse updateAuthScheme(UpdateAuthSchemeRequest updateAuthSchemeRequest) {
        getValidationHelper().validateModel(updateAuthSchemeRequest, ValidationGroups.Update.class);

        final var objectId = getMongoDBUtils().parseOrThrowNotFoundException(updateAuthSchemeRequest.getAuthSchemeId());
        final var query = getDatastore().find(MongoAuthScheme.class);

        return null;
    }

    @Override
    public CreateAuthSchemeResponse createAuthScheme(CreateAuthSchemeRequest authSchemeRequest) {
        return null;
    }

    @Override
    public void deleteAuthScheme(String authSchemeId) {

    }

    private AuthScheme transform(MongoAuthScheme authScheme)
    {
        return getBeanMapper().map(authScheme, AuthScheme.class);
    }

    public MongoDBUtils getMongoDBUtils() {
        return mongoDBUtils;
    }

    public ValidationHelper getValidationHelper() {
        return validationHelper;
    }

    @Inject
    public void setValidationHelper(ValidationHelper validationHelper) {
        this.validationHelper = validationHelper;
    }

    @Inject
    public void setMongoDBUtils(MongoDBUtils mongoDBUtils) {
        this.mongoDBUtils = mongoDBUtils;
    }

    public Mapper getBeanMapper() {
        return beanMapper;
    }

    @Inject
    public void setBeanMapper(Mapper beanMapper) {
        this.beanMapper = beanMapper;
    }

    public Datastore getDatastore() {
        return datastore;
    }

    @Inject
    public void setDatastore(Datastore datastore) {
        this.datastore = datastore;
    }
}
