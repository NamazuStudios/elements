package dev.getelements.elements.model;

import io.swagger.annotations.ApiModel;

import java.io.Serializable;

/**
 * Created by patricktwohig on 3/25/15.
 */
@ApiModel
public class SocialCampaignEntry implements Serializable {

    private ShortLink shortLink;

    public ShortLink getShortLink() {
        return shortLink;
    }

    public void setShortLink(ShortLink shortLink) {
        this.shortLink = shortLink;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SocialCampaignEntry)) return false;

        SocialCampaignEntry that = (SocialCampaignEntry) o;

        return getShortLink() != null ? getShortLink().equals(that.getShortLink()) : that.getShortLink() == null;
    }

    @Override
    public int hashCode() {
        return getShortLink() != null ? getShortLink().hashCode() : 0;
    }

}
