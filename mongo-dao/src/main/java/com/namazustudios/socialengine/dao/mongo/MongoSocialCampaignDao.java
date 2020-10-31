package com.namazustudios.socialengine.dao.mongo;

import com.google.common.base.Function;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.mongodb.DuplicateKeyException;
import com.namazustudios.socialengine.Constants;
import com.namazustudios.socialengine.util.ValidationHelper;
import com.namazustudios.socialengine.dao.SocialCampaignDao;
import com.namazustudios.socialengine.dao.mongo.model.MongoSocialCampaign;
import com.namazustudios.socialengine.exception.DuplicateException;
import com.namazustudios.socialengine.exception.InvalidDataException;
import com.namazustudios.socialengine.exception.NotFoundException;
import com.namazustudios.socialengine.exception.NotImplementedException;
import com.namazustudios.socialengine.model.*;
import dev.morphia.Datastore;
import dev.morphia.query.CountOptions;
import dev.morphia.query.Query;
import dev.morphia.query.experimental.filters.Filters;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

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
    private MongoConcurrentUtils mongoConcurrentUtils;

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

        final MongoSocialCampaign mongoSocialCampaign = datastore.find(MongoSocialCampaign.class)
                .filter(Filters.eq("name", socialCampaign.getName())).first();

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

        final Pagination<SocialCampaign> socialCampaignPagination = new Pagination<>();

        socialCampaignPagination.setOffset(offset);
        socialCampaignPagination.setTotal((int)campaigns.count(new CountOptions().skip(offset).limit(count)));

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
        final MongoSocialCampaign mongoSocialCampaign = datastore.find(MongoSocialCampaign.class)
                .filter(Filters.eq("name", name)).first();

        if (mongoSocialCampaign == null) {
            throw new NotFoundException("Social campaign " + name + " was not found.");
        }

        return  transform(mongoSocialCampaign);
    }

    @Override
    public SocialCampaignEntry submitEntrant(final String campaign, final BasicEntrantProfile entrant) {
        throw new NotImplementedException();
    }

    @Override
    public SocialCampaignEntry submitEntrant(final String campaign, final SteamEntrantProfile entrant) {
        throw new NotImplementedException();
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
