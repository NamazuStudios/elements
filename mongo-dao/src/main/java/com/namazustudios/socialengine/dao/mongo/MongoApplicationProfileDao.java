package com.namazustudios.socialengine.dao.mongo;

import com.google.common.base.Function;
import com.google.common.base.Strings;
import com.mongodb.MongoCommandException;
import com.namazustudios.socialengine.ValidationHelper;
import com.namazustudios.socialengine.dao.ApplicationProfileDao;
import com.namazustudios.socialengine.dao.mongo.model.AbstractMongoApplicationProfile;
import com.namazustudios.socialengine.dao.mongo.model.MongoApplication;
import com.namazustudios.socialengine.dao.mongo.model.MongoPSNApplicationProfile;
import com.namazustudios.socialengine.exception.*;
import com.namazustudios.socialengine.fts.ObjectIndex;
import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.application.ApplicationProfile;
import com.namazustudios.socialengine.model.application.PSNApplicationProfile;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.flexible.core.QueryNodeException;
import org.apache.lucene.queryparser.flexible.standard.StandardQueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.TermQuery;
import org.mongodb.morphia.AdvancedDatastore;
import org.mongodb.morphia.query.Query;
import org.mongodb.morphia.query.UpdateOperations;

import javax.inject.Inject;

/**
 * Created by patricktwohig on 7/13/15.
 */
public class MongoApplicationProfileDao implements ApplicationProfileDao {

    @Inject
    private ValidationHelper validationHelper;

    @Inject
    private ObjectIndex objectIndex;

    @Inject
    private StandardQueryParser standardQueryParser;

    @Inject
    private MongoDBUtils mongoDBUtils;

    @Inject
    private AdvancedDatastore datastore;

    @Inject
    private MongoApplicationDao mongoApplicationDao;

    @Override
    public PSNApplicationProfile createOrUpdateInactiveApplicationProfile(final String applicationNameOrId,
                                                                          final PSNApplicationProfile psnApplicationProfile) {

        final MongoApplication mongoApplication = mongoApplicationDao.getActiveMongoApplication(applicationNameOrId);
        validate(psnApplicationProfile);

        final Query<MongoPSNApplicationProfile> query = datastore.createQuery(MongoPSNApplicationProfile.class);

        query.and(
            query.criteria("name").equal(psnApplicationProfile.getNpIdentifier()),
            query.criteria("active").equal(false)
        );

        final UpdateOperations<MongoPSNApplicationProfile> updateOperations =
            datastore.createUpdateOperations(MongoPSNApplicationProfile.class);

        updateOperations.set("np_id", psnApplicationProfile.getNpIdentifier().trim());
        updateOperations.set("client_secret", Strings.nullToEmpty(psnApplicationProfile.getClientSecret()).trim());
        updateOperations.set("active", true);
        updateOperations.set("platform", psnApplicationProfile.getPlatform());
        updateOperations.set("parent", mongoApplication);

        final MongoPSNApplicationProfile mongoPSNApplicationProfile;

        try {
            mongoPSNApplicationProfile = datastore.findAndModify(query, updateOperations, false, true);
        } catch (MongoCommandException ex) {
            if (ex.getErrorCode() == 11000) {
                throw new DuplicateException(ex);
            } else {
                throw new InternalException(ex);
            }
        }

        objectIndex.index(mongoPSNApplicationProfile);
        return transform(mongoPSNApplicationProfile);

    }

    @Override
    public Pagination<ApplicationProfile> getActiveApplicationProfiles(final String applicationNameOrId,
                                                                       final int offset, final int count) {

        final MongoApplication mongoApplication = mongoApplicationDao.getActiveMongoApplication(applicationNameOrId);
        final Query<AbstractMongoApplicationProfile> query = datastore.createQuery(AbstractMongoApplicationProfile.class);

        query.filter("active =", true);
        query.filter("parent_application =", mongoApplication);
        query.field("class_heierarchy").hasThisOne(AbstractMongoApplicationProfile.class.getName());

        return mongoDBUtils.paginationFromQuery(query, offset, count, new Function<AbstractMongoApplicationProfile, ApplicationProfile>() {
            @Override
            public ApplicationProfile apply(AbstractMongoApplicationProfile input) {
                return transform(input);
            }
        });

    }

    @Override
    public Pagination<ApplicationProfile> getActiveApplicationProfiles(final String applicationNameOrId,
                                                                       final int offset, final int count,
                                                                       final String search) {

        final BooleanQuery booleanQuery = new BooleanQuery();

        try {

            final Term activeTerm = new Term("active", "true");
            final Term applicationIdTerm = new Term("applicationId");
            final Term applicationNameTerm = new Term("applicationName");

            booleanQuery.add(new TermQuery(activeTerm), BooleanClause.Occur.FILTER);
            booleanQuery.add(new TermQuery(applicationIdTerm), BooleanClause.Occur.SHOULD);
            booleanQuery.add(new TermQuery(applicationNameTerm), BooleanClause.Occur.SHOULD);
            booleanQuery.add(standardQueryParser.parse(search, "name"), BooleanClause.Occur.FILTER);

        } catch (QueryNodeException ex) {
            throw new BadQueryException(ex);
        }

        return mongoDBUtils.paginationFromSearch(AbstractMongoApplicationProfile.class, booleanQuery, offset, count,
                new Function<AbstractMongoApplicationProfile, ApplicationProfile>() {
                    @Override
                    public ApplicationProfile apply(AbstractMongoApplicationProfile input) {
                        return transform(input);
                    }
                });

    }

    @Override
    public PSNApplicationProfile getPSNApplicationProfile(final String applicationNameOrId,
                                                          final String applicationProfileNameOrId) {

        final MongoApplication mongoApplication = mongoApplicationDao.getActiveMongoApplication(applicationNameOrId);
        final Query<MongoPSNApplicationProfile> query = datastore.createQuery(MongoPSNApplicationProfile.class);

        query.filter("active =", true);
        query.filter("parent_application =", mongoApplication);
        query.field("class_heierarchy").hasThisOne(PSNApplicationProfile.class.getName());
        query.or(
                query.criteria("_id").equal(applicationProfileNameOrId),
                query.criteria("name").equal(applicationProfileNameOrId)
        );

        final MongoPSNApplicationProfile mongoPSNApplicationProfile = query.get();

        if (mongoPSNApplicationProfile == null) {
            throw new NotFoundException("application profile " + applicationProfileNameOrId + " not found.");
        }

        return transform(mongoPSNApplicationProfile);

    }

    @Override
    public PSNApplicationProfile updateApplicationProfile(final String applicationNameOrId,
                                                          final String applicationProfileNameOrId,
                                                          final PSNApplicationProfile psnApplicationProfile) {

        final MongoApplication mongoApplication = mongoApplicationDao.getActiveMongoApplication(applicationNameOrId);
        validate(psnApplicationProfile);

        final Query<MongoPSNApplicationProfile> query = datastore.createQuery(MongoPSNApplicationProfile.class);

        query.filter("active =", true);
        query.filter("parent_application =", mongoApplication);
        query.field("class_heierarchy").hasThisOne(PSNApplicationProfile.class.getName());
        query.or(
                query.criteria("_id").equal(applicationProfileNameOrId),
                query.criteria("name").equal(applicationProfileNameOrId)
        );

        final UpdateOperations<MongoPSNApplicationProfile> updateOperations =
                datastore.createUpdateOperations(MongoPSNApplicationProfile.class);

        updateOperations.set("np_id", psnApplicationProfile.getNpIdentifier().trim());
        updateOperations.set("client_secret", Strings.nullToEmpty(psnApplicationProfile.getClientSecret()).trim());
        updateOperations.set("platform", psnApplicationProfile.getPlatform());
        updateOperations.set("parent", mongoApplication);

        final MongoPSNApplicationProfile mongoPSNApplicationProfile;

        try {
            mongoPSNApplicationProfile = datastore.findAndModify(query, updateOperations, false, false);
        } catch (MongoCommandException ex) {
            if (ex.getErrorCode() == 11000) {
                throw new DuplicateException(ex);
            } else {
                throw new InternalException(ex);
            }
        }

        if (mongoPSNApplicationProfile == null) {
            throw new NotFoundException("profile with ID not found: " + mongoPSNApplicationProfile);
        }

        objectIndex.index(mongoPSNApplicationProfile);
        return transform(mongoPSNApplicationProfile);

    }

    @Override
    public void softDeleteApplicationProfile(final String applicationNameOrId,
                                             final String applicationProfileNameOrId) {

        final MongoApplication mongoApplication = mongoApplicationDao.getActiveMongoApplication(applicationNameOrId);
        final Query<MongoPSNApplicationProfile> query = datastore.createQuery(MongoPSNApplicationProfile.class);

        query.filter("active =", true);
        query.filter("parent_application =", mongoApplication);
        query.field("class_heierarchy").hasThisOne(PSNApplicationProfile.class.getName());
        query.or(
                query.criteria("_id").equal(applicationProfileNameOrId),
                query.criteria("name").equal(applicationProfileNameOrId)
        );

        final UpdateOperations<MongoPSNApplicationProfile> updateOperations =
                datastore.createUpdateOperations(MongoPSNApplicationProfile.class);

        updateOperations.set("active", false);

        final MongoPSNApplicationProfile mongoPSNApplicationProfile;

        try {
            mongoPSNApplicationProfile = datastore.findAndModify(query, updateOperations, false, false);
        } catch (MongoCommandException ex) {
            if (ex.getErrorCode() == 11000) {
                throw new DuplicateException(ex);
            } else {
                throw new InternalException(ex);
            }
        }

        if (mongoPSNApplicationProfile == null) {
            throw new NotFoundException("profile with ID not found: " + mongoPSNApplicationProfile);
        }

        objectIndex.index(mongoPSNApplicationProfile);

    }

    public void validate(final PSNApplicationProfile psnApplicationProfile) {

        if (psnApplicationProfile == null) {
            throw new InvalidDataException("psnApplicationProfile must not be null.");
        }

        switch (psnApplicationProfile.getPlatform()) {
            case PSN_PS4:
            case PSN_VITA:
                break;
            default:
                throw new InvalidDataException("platform not supported: " + psnApplicationProfile.getPlatform());
        }

        validationHelper.validateModel(psnApplicationProfile);

    }

    public MongoPSNApplicationProfile transform(final PSNApplicationProfile psnApplicationProfile) {

        final MongoPSNApplicationProfile mongoPSNApplicationProfile = new MongoPSNApplicationProfile();

        mongoPSNApplicationProfile.setObjectId(psnApplicationProfile.getId());
        mongoPSNApplicationProfile.setPlatform(psnApplicationProfile.getPlatform());
        mongoPSNApplicationProfile.setClientSecret(psnApplicationProfile.getClientSecret());
        mongoPSNApplicationProfile.setNpIdentifier(psnApplicationProfile.getNpIdentifier());

        return mongoPSNApplicationProfile;

    }

    public ApplicationProfile transform(final AbstractMongoApplicationProfile abstractMongoApplicationProfile) {

        final ApplicationProfile applicationProfile = new ApplicationProfile() {};

        applicationProfile.setId(abstractMongoApplicationProfile.getObjectId());
        applicationProfile.setPlatform(abstractMongoApplicationProfile.getPlatform());

        return applicationProfile;

    }

    public PSNApplicationProfile transform(final MongoPSNApplicationProfile mongoPSNApplicationProfile) {

        final PSNApplicationProfile psnApplicationProfile = new PSNApplicationProfile();

        psnApplicationProfile.setId(mongoPSNApplicationProfile.getObjectId());
        psnApplicationProfile.setPlatform(mongoPSNApplicationProfile.getPlatform());
        psnApplicationProfile.setNpIdentifier(mongoPSNApplicationProfile.getNpIdentifier());
        psnApplicationProfile.setClientSecret(mongoPSNApplicationProfile.getClientSecret());

        return psnApplicationProfile;

    }

}
