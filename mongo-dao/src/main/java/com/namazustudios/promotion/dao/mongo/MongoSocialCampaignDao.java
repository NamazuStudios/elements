package com.namazustudios.promotion.dao.mongo;

import com.google.common.base.Function;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.namazustudios.promotion.dao.SocialCampaignDao;
import com.namazustudios.promotion.dao.mongo.model.MongoBasicEntrant;
import com.namazustudios.promotion.dao.mongo.model.MongoShortLink;
import com.namazustudios.promotion.dao.mongo.model.MongoSocialCampaign;
import com.namazustudios.promotion.exception.InternalException;
import com.namazustudios.promotion.exception.InvalidDataException;
import com.namazustudios.promotion.exception.NotFoundException;
import com.namazustudios.promotion.exception.TooBusyException;
import com.namazustudios.promotion.model.BasicEntrant;
import com.namazustudios.promotion.model.Pagination;
import com.namazustudios.promotion.model.SocialCampaign;
import com.namazustudios.promotion.model.SocialCampaignEntry;
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
    @Named("com.namazustudios.promotion.query.max.results")
    private int queryMaxResults;

    @Override
    public SocialCampaign createNewCampaign(SocialCampaign socialCampaign) {

        final MongoSocialCampaign mongoSocialCampaign = new MongoSocialCampaign();

        validate(socialCampaign);

        mongoSocialCampaign.setObjectId(socialCampaign.getName());
        mongoSocialCampaign.setLinkUrl(socialCampaign.getLinkUrl());
        mongoSocialCampaign.setBeginDate(socialCampaign.getBeginDate());
        mongoSocialCampaign.setEndDate(socialCampaign.getEndDate());
        mongoSocialCampaign.setAllowedEntrantTypes(socialCampaign.getAllowedEntrantTypes());

        datastore.save(mongoSocialCampaign);

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
    public SocialCampaignEntry submitEntrant(final String campaign, final BasicEntrant entrant) {

        final MongoSocialCampaign mongoSocialCampaign = datastore.get(MongoSocialCampaign.class, campaign);

        if (mongoSocialCampaign == null) {
            throw new NotFoundException("Social campaign " + campaign + " was not found.");
        }

        validate(entrant);

        final Atomic.Once<MongoShortLink> mongoShortLinkOnce = atomic.once(new Atomic.Once<MongoShortLink>() {
            @Override
            public MongoShortLink call() throws Atomic.OptimistcException {
                return mongoShortLinkDao.createShortLinkFromURL(mongoSocialCampaign.getLinkUrl());
            }
        });

        try {

            final MongoBasicEntrant mongoBasicEntrant = new MongoBasicEntrant();
            mongoBasicEntrant.setEmail(entrant.getEmail());

            return atomic.performOptimisticUpsert(mongoBasicEntrant, new Atomic.CriticalOperation<SocialCampaignEntry>() {

                @Override
                public SocialCampaignEntry attempt(Datastore datastore) throws Atomic.OptimistcException {

                    mongoBasicEntrant.setSalutation(entrant.getSalutation());
                    mongoBasicEntrant.setFirstName(entrant.getFirstName());
                    mongoBasicEntrant.setLastName(entrant.getLastName());

                    final Map<MongoSocialCampaign, MongoShortLink> shortLinksByCampaign;

                    if (mongoBasicEntrant.getShortLinksByCampaign() == null) {
                        shortLinksByCampaign = Maps.newHashMap();
                    } else {
                        shortLinksByCampaign = new HashMap<>(mongoBasicEntrant.getShortLinksByCampaign());
                    }

                    MongoShortLink mongoShortLink = shortLinksByCampaign.get(mongoSocialCampaign);

                    if (mongoShortLink == null) {
                        mongoShortLink = mongoShortLinkOnce.call();
                        shortLinksByCampaign.put(mongoSocialCampaign, mongoShortLink);
                    }

                    final SocialCampaignEntry socialCampaignEntry = new SocialCampaignEntry();
                    socialCampaignEntry.setUniqueUrl(mongoShortLinkDao.getURLFromShortLink(mongoShortLink));
                    return socialCampaignEntry;

                }

            });

        } catch (Atomic.ContentionException ex) {
            throw new TooBusyException(ex);
        } catch (Atomic.OptimistcException ex) {
            throw new InternalException(ex);
        }

    }

    private MongoBasicEntrant getOrCreateMongoBasicEntrant(final BasicEntrant entrant) {
        return null;
    }

    public void validate(final SocialCampaign socialCampaign) {

        if (socialCampaign == null) {
            throw new InvalidDataException("Social campaign. must not be null.");
        }

        socialCampaign.setName(Strings.nullToEmpty(socialCampaign.getName()).trim());
        socialCampaign.setLinkUrl(Strings.nullToEmpty(socialCampaign.getLinkUrl()).trim());

        if (Strings.isNullOrEmpty(socialCampaign.getName())) {
            throw new InvalidDataException("Social campaign name not specified.", socialCampaign);
        }

        if (Strings.isNullOrEmpty(socialCampaign.getLinkUrl())) {
            throw new InvalidDataException("Social campaign link URL not specified.", socialCampaign);
        }

    }

    public void validate(final BasicEntrant entrant) {

        if (entrant == null) {
            throw new InvalidDataException("Entrant must not be null.");
        }

        entrant.setEmail(Strings.nullToEmpty(entrant.getEmail()).trim());
        entrant.setSalutation(Strings.nullToEmpty(entrant.getSalutation()).trim());
        entrant.setFirstName(Strings.nullToEmpty(entrant.getFirstName()).trim());
        entrant.setLastName(Strings.nullToEmpty(entrant.getLastName()).trim());

        if (Strings.isNullOrEmpty(entrant.getFirstName())) {
            throw new InvalidDataException("First name campaign name not specified.", entrant);
        }

        if (Strings.isNullOrEmpty(entrant.getLastName())) {
            throw new InvalidDataException("Last name not specified.", entrant);
        }

        if (Strings.isNullOrEmpty(entrant.getEmail())) {
            throw new InvalidDataException("Email not specified.", entrant);
        }

        if (entrant.getBirthday() == null) {
            throw new InvalidDataException("Birthday must not be null.", entrant);
        }

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
