package com.namazustudios.socialengine.dao.mongo;

import com.google.common.base.Function;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mongodb.DuplicateKeyException;
import com.namazustudios.socialengine.Constants;
import com.namazustudios.socialengine.ValidationHelper;
import com.namazustudios.socialengine.dao.SocialCampaignDao;
import com.namazustudios.socialengine.dao.mongo.model.MongoBasicEntrant;
import com.namazustudios.socialengine.dao.mongo.model.MongoShortLink;
import com.namazustudios.socialengine.dao.mongo.model.MongoSocialCampaign;
import com.namazustudios.socialengine.dao.mongo.model.MongoSteamEntrant;
import com.namazustudios.socialengine.exception.DuplicateException;
import com.namazustudios.socialengine.exception.InvalidDataException;
import com.namazustudios.socialengine.exception.NotFoundException;
import com.namazustudios.socialengine.exception.TooBusyException;
import com.namazustudios.socialengine.model.BasicEntrantProfile;
import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.SocialCampaign;
import com.namazustudios.socialengine.model.SocialCampaignEntry;
import com.namazustudios.socialengine.model.SteamEntrantProfile;
import org.mongodb.morphia.AdvancedDatastore;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.query.Query;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by patricktwohig on 3/26/15.
 */
@Singleton
public class MongoSocialCampaignDao implements SocialCampaignDao {

    @Inject
    private Datastore datastore;

    @Inject
    private MongoShortLinkDao mongoShortLinkDao;

    @Inject
    private Atomic atomic;

    @Inject
    @Named(Constants.QUERY_MAX_RESULTS)
    private int queryMaxResults;

    @Inject
    private ValidationHelper validationHelper;

    @Override
    public SocialCampaign createNewCampaign(SocialCampaign socialCampaign) {

        final MongoSocialCampaign mongoSocialCampaign = new MongoSocialCampaign();

        validate(socialCampaign);

        mongoSocialCampaign.setObjectId(socialCampaign.getName());
        mongoSocialCampaign.setLinkUrl(socialCampaign.getLinkUrl());
        mongoSocialCampaign.setBeginDate(socialCampaign.getBeginDate());
        mongoSocialCampaign.setEndDate(socialCampaign.getEndDate());
        mongoSocialCampaign.setAllowedEntrantTypes(socialCampaign.getAllowedEntrantTypes());

        try {
            datastore.save(mongoSocialCampaign);
        } catch (DuplicateKeyException ex) {
            throw new DuplicateException(ex);
        }

        return socialCampaign;

    }

    @Override
    public SocialCampaign updateSocialCampaign(SocialCampaign socialCampaign) {

        validate(socialCampaign);

        final MongoSocialCampaign mongoSocialCampaign = datastore.get(MongoSocialCampaign.class, socialCampaign.getName());

        if (mongoSocialCampaign == null) {
            throw new NotFoundException("Social campaign " + socialCampaign.getName() + " was not found.");
        }

        mongoSocialCampaign.setLinkUrl(socialCampaign.getLinkUrl());
        mongoSocialCampaign.setBeginDate(socialCampaign.getBeginDate());
        mongoSocialCampaign.setEndDate(socialCampaign.getEndDate());
        mongoSocialCampaign.setAllowedEntrantTypes(socialCampaign.getAllowedEntrantTypes());

        datastore.save(mongoSocialCampaign);

        return socialCampaign;

    }

    @Override
    public Pagination<SocialCampaign> getSocialCampaigns(int offset, int count) {

        final Query<MongoSocialCampaign> campaigns = datastore.find(MongoSocialCampaign.class);

        count = Math.min(count, queryMaxResults);
        campaigns.offset(offset).limit(count);

        final Pagination<SocialCampaign> socialCampaignPagination = new Pagination<>();

        socialCampaignPagination.setOffset(offset);
        socialCampaignPagination.setTotal((int)campaigns.getCollection().getCount());

        final Iterable<SocialCampaign> socialCampaignIterable =
                Iterables.transform(campaigns, new Function<MongoSocialCampaign, SocialCampaign>() {
                    @Override
                    public SocialCampaign apply(MongoSocialCampaign mongoSocialCampaign) {
                        return transform(mongoSocialCampaign);
                    }
                });

        socialCampaignPagination.setObjects(Lists.newArrayList(socialCampaignIterable));

        return socialCampaignPagination;

    }

    @Override
    public SocialCampaign getSocialCampaign(String name) {
        final MongoSocialCampaign mongoSocialCampaign = datastore.get(MongoSocialCampaign.class, name);

        if (mongoSocialCampaign == null) {
            throw new NotFoundException("Social campaign " + name + " was not found.");
        }

        return  transform(mongoSocialCampaign);
    }

    @Override
    public SocialCampaignEntry submitEntrant(final String campaign, final BasicEntrantProfile entrant) {

        validate(entrant);

        final MongoSocialCampaign mongoSocialCampaign = datastore.get(MongoSocialCampaign.class, campaign);

        if (mongoSocialCampaign == null) {
            throw new NotFoundException("Social campaign " + campaign + " was not found.");
        }

        final Atomic.Once<MongoShortLink> mongoShortLinkOnce = atomic.once(new Atomic.Once<MongoShortLink>() {
            @Override
            public MongoShortLink call() {
                return mongoShortLinkDao.createShortLinkFromURL(mongoSocialCampaign.getLinkUrl());
            }
        });

        try {

            final Query<MongoBasicEntrant> query = datastore.createQuery(MongoBasicEntrant.class);
            query.filter("email = ", entrant.getEmail());

            return atomic.performOptimisticUpsert(query, new Atomic.CriticalOperationWithModel<SocialCampaignEntry, MongoBasicEntrant>() {
                @Override
                public SocialCampaignEntry attempt(AdvancedDatastore datastore, MongoBasicEntrant model) throws Atomic.ContentionException {

                    model.setSalutation(entrant.getSalutation());
                    model.setFirstName(entrant.getFirstName());
                    model.setLastName(entrant.getLastName());

                    final Map<String, MongoShortLink> shortLinksByCampaign;

                    if (model.getShortLinksByCampaign() == null) {
                        shortLinksByCampaign = Maps.newHashMap();
                    } else {
                        shortLinksByCampaign = new HashMap<>(model.getShortLinksByCampaign());
                    }

                    MongoShortLink mongoShortLink = shortLinksByCampaign.get(mongoSocialCampaign.getObjectId());

                    if (mongoShortLink == null) {
                        mongoShortLink = mongoShortLinkOnce.call();
                        shortLinksByCampaign.put(mongoSocialCampaign.getObjectId(), mongoShortLink);
                    }

                    final SocialCampaignEntry socialCampaignEntry = new SocialCampaignEntry();
                    socialCampaignEntry.setUniqueUrl(mongoShortLinkDao.getURLFromShortLink(mongoShortLink));
                    return socialCampaignEntry;

                }
            });

        } catch (Atomic.ConflictException ex) {
            throw new TooBusyException(ex);
        }

    }

    @Override
    public SocialCampaignEntry submitEntrant(final String campaign, final SteamEntrantProfile entrant) {
        final MongoSocialCampaign mongoSocialCampaign = datastore.get(MongoSocialCampaign.class, campaign);

        if (mongoSocialCampaign == null) {
            throw new NotFoundException("Social campaign " + campaign + " was not found.");
        }

        validate(entrant);

        final Atomic.Once<MongoShortLink> mongoShortLinkOnce = atomic.once(new Atomic.Once<MongoShortLink>() {
            @Override
            public MongoShortLink call() {
                return mongoShortLinkDao.createShortLinkFromURL(mongoSocialCampaign.getLinkUrl());
            }
        });

        try {

            final MongoSteamEntrant mongoSteamEntrant = new MongoSteamEntrant();

            final Query<MongoSteamEntrant> query = datastore.createQuery(MongoSteamEntrant.class);
            query.filter("email = ", entrant.getEmail());

            return atomic.performOptimisticUpsert(query, new Atomic.CriticalOperationWithModel<SocialCampaignEntry, MongoSteamEntrant>() {
                @Override
                public SocialCampaignEntry attempt(AdvancedDatastore datastore, MongoSteamEntrant model) throws Atomic.ContentionException {

                    model.setSalutation(entrant.getSalutation());
                    model.setFirstName(entrant.getFirstName());
                    model.setLastName(entrant.getLastName());
                    model.setSteamId(entrant.getSteamId());

                    final Map<String, MongoShortLink> shortLinksByCampaign;

                    if (mongoSteamEntrant.getShortLinksByCampaign() == null) {
                        shortLinksByCampaign = Maps.newHashMap();
                    } else {
                        shortLinksByCampaign = new HashMap<>(mongoSteamEntrant.getShortLinksByCampaign());
                    }

                    MongoShortLink mongoShortLink = shortLinksByCampaign.get(mongoSocialCampaign.getObjectId());

                    if (mongoShortLink == null) {
                        mongoShortLink = mongoShortLinkOnce.call();
                        shortLinksByCampaign.put(mongoSocialCampaign.getObjectId(), mongoShortLink);
                    }

                    final SocialCampaignEntry socialCampaignEntry = new SocialCampaignEntry();
                    socialCampaignEntry.setUniqueUrl(mongoShortLinkDao.getURLFromShortLink(mongoShortLink));
                    return socialCampaignEntry;

                }
            });

        } catch (Atomic.ConflictException ex) {
            throw new TooBusyException(ex);
        }

    }

    public void validate(final SocialCampaign socialCampaign) {

        if (socialCampaign == null) {
            throw new InvalidDataException("Social campaign. must not be null.");
        }

        validationHelper.validateModel(socialCampaign);

    }

    public void validate(final BasicEntrantProfile entrant) {

        if (entrant == null) {
            throw new InvalidDataException("Entrant must not be null.");
        }

        validationHelper.validateModel(entrant);

        entrant.setEmail(Strings.nullToEmpty(entrant.getEmail()).trim());
        entrant.setSalutation(Strings.nullToEmpty(entrant.getSalutation()).trim());
        entrant.setFirstName(Strings.nullToEmpty(entrant.getFirstName()).trim());
        entrant.setLastName(Strings.nullToEmpty(entrant.getLastName()).trim());

    }

    public void validate(final SteamEntrantProfile entrant) {

        validate((BasicEntrantProfile)entrant);
        entrant.setLastName(Strings.nullToEmpty(entrant.getSteamId()).trim());

    }

    public SocialCampaign transform(MongoSocialCampaign mongoSocialCampaign) {

        final SocialCampaign socialCampaign = new SocialCampaign();

        socialCampaign.setLinkUrl(mongoSocialCampaign.getLinkUrl());
        socialCampaign.setName(mongoSocialCampaign.getObjectId());
        socialCampaign.setAllowedEntrantTypes(mongoSocialCampaign.getAllowedEntrantTypes());
        socialCampaign.setBeginDate(mongoSocialCampaign.getBeginDate());
        socialCampaign.setEndDate(mongoSocialCampaign.getEndDate());

        return socialCampaign;
    }

}
