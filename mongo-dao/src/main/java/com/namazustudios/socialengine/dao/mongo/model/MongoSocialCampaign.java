package com.namazustudios.socialengine.dao.mongo.model;

import com.namazustudios.socialengine.model.SocialCampaign;
import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Id;
import dev.morphia.annotations.Property;

import java.util.Date;
import java.util.List;

/**
 * Created by patricktwohig on 3/26/15.
 */
@Entity(value = "social_campaign", noClassnameStored = true)
public class MongoSocialCampaign {

    @Id
    private String objectId;

    @Property
    private String linkUrl;

    @Property
    private List<SocialCampaign.EntrantType> allowedEntrantTypes;

    @Property
    private Date beginDate;

    @Property
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
