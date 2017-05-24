package com.namazustudios.socialengine.model;

import io.swagger.annotations.ApiModel;

/**
 * Created by patricktwohig on 3/25/15.
 */
@ApiModel
public class SocialCampaignEntry {

    private ShortLink shortLink;

    public ShortLink getShortLink() {
        return shortLink;
    }

    public void setShortLink(ShortLink shortLink) {
        this.shortLink = shortLink;
    }

}
