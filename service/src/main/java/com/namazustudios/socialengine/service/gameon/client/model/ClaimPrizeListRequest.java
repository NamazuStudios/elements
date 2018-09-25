package com.namazustudios.socialengine.service.gameon.client.model;

import java.util.List;

public class ClaimPrizeListRequest {

    private List<String> awardedPrizeIds;

    public List<String> getAwardedPrizeIds() {
        return awardedPrizeIds;
    }

    public void setAwardedPrizeIds(List<String> awardedPrizeIds) {
        this.awardedPrizeIds = awardedPrizeIds;
    }

}
