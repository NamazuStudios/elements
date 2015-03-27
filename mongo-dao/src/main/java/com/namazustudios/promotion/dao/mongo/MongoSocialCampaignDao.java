package com.namazustudios.promotion.dao.mongo;

import com.google.common.base.Function;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.namazustudios.promotion.dao.SocialCampaignDao;
import com.namazustudios.promotion.dao.mongo.model.MongoSocialCampaign;
import com.namazustudios.promotion.exception.NotFoundException;
import com.namazustudios.promotion.model.BasicEntrant;
import com.namazustudios.promotion.model.Pagination;
import com.namazustudios.promotion.model.SocialCampaign;
import com.namazustudios.promotion.model.SocialCampaignEntry;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.query.Query;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.Iterator;

/**
 * Created by patricktwohig on 3/26/15.
 */
@Singleton
public class MongoSocialCampaignDao implements SocialCampaignDao {

    @Inject
    private Datastore datastore;

    @Inject
    @Named("com.namazustudios.promotion.query.max.results")
    private int queryMaxResults;

    @Override
    public SocialCampaign createNewCampaign(SocialCampaign socialCampaign) {

        final MongoSocialCampaign mongoSocialCampaign = new MongoSocialCampaign();

        if (Strings.isNullOrEmpty(socialCampaign.getName())) {
            throw new IllegalArgumentException("Social campaign name not specified.");
        }

        if (Strings.isNullOrEmpty(socialCampaign.getLinkUrl())) {
            throw new IllegalArgumentException("Social campaign link URL not specified.");
        }

        socialCampaign.setName(socialCampaign.getName().trim());
        socialCampaign.setLinkUrl(socialCampaign.getLinkUrl().trim());

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

        verify(socialCampaign);

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
    public SocialCampaignEntry submitEntrant(String campaign, BasicEntrant entrant) {return null;}

    public void verify(final SocialCampaign socialCampaign) {

        if (Strings.isNullOrEmpty(socialCampaign.getName())) {
            throw new IllegalArgumentException("Social campaign name not specified.");
        }

        if (Strings.isNullOrEmpty(socialCampaign.getLinkUrl())) {
            throw new IllegalArgumentException("Social campaign link URL not specified.");
        }

        socialCampaign.setName(socialCampaign.getName().trim());
        socialCampaign.setLinkUrl(socialCampaign.getLinkUrl().trim());

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
