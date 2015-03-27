package com.namazustudios.promotion.dao.mongo.model;

import com.namazustudios.promotion.model.SocialCampaign;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Indexed;
import org.mongodb.morphia.annotations.Property;
import org.mongodb.morphia.annotations.Reference;

import java.util.List;

/**
 * Created by patricktwohig on 3/26/15.
 */
@Entity("social_campaign")
public class MongoSocialCampaign {

    @Id
    private ObjectId objectId;

    @Indexed
    @Property
    private String name;

    @Property
    private String linkUrl;

    @Property
    private List<SocialCampaign.EntrantType> allowedEntrantTypes;

    public List<SocialCampaign.EntrantType> getAllowedEntrantTypes() {
        return allowedEntrantTypes;
    }

    public void setAllowedEntrantTypes(List<SocialCampaign.EntrantType> allowedEntrantTypes) {
        this.allowedEntrantTypes = allowedEntrantTypes;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ObjectId getObjectId() {
        return objectId;
    }

    public void setObjectId(ObjectId objectId) {
        this.objectId = objectId;
    }

    public String getLinkUrl() {
        return linkUrl;
    }

    public void setLinkUrl(String linkUrl) {
        this.linkUrl = linkUrl;
    }

}
