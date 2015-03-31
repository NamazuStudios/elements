package com.namazustudios.promotion.dao.mongo.model;

import com.namazustudios.promotion.model.SocialCampaign;
import org.bson.types.ObjectId;
import org.mongodb.morphia.Key;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Indexed;
import org.mongodb.morphia.annotations.Property;
import org.mongodb.morphia.annotations.Reference;

import java.util.Date;
import java.util.List;

/**
 * Created by patricktwohig on 3/26/15.
 */
@Entity(value = "social_campaign", noClassnameStored = true)
public class MongoSocialCampaign {

    @Id
    private String objectId;

    @Property("link_url")
    private String linkUrl;

    @Property("allowed_entrant_types")
    private List<SocialCampaign.EntrantType> allowedEntrantTypes;

    @Property("begin_date")
    private Date beginDate;

    @Property("end_date")
    private Date endDate;

    public String getObjectId() {
        return objectId;
    }

    public void setObjectId(String objectId) {
        this.objectId = objectId;
    }

    public List<SocialCampaign.EntrantType> getAllowedEntrantTypes() {
        return allowedEntrantTypes;
    }

    public void setAllowedEntrantTypes(List<SocialCampaign.EntrantType> allowedEntrantTypes) {
        this.allowedEntrantTypes = allowedEntrantTypes;
    }

    public String getLinkUrl() {
        return linkUrl;
    }

    public void setLinkUrl(String linkUrl) {
        this.linkUrl = linkUrl;
    }

    public Date getBeginDate() {
        return beginDate;
    }

    public void setBeginDate(Date beginDate) {
        this.beginDate = beginDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }
}
