package com.namazustudios.socialengine.model.gameon.game;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.List;
import java.util.Objects;

@ApiModel(description = "Corresponds to the GameOn Fulfill Prize List Request:  " +
                        "https://developer.amazon.com/docs/gameon/game-api-ref.html#fulfillprizelistrequest")
public class GameOnFulfillPrizeRequest {

    @ApiModelProperty("A list of awarded prize IDs.")
    private List<String> awardedPrizeIds;

    public List<String> getAwardedPrizeIds() {
        return awardedPrizeIds;
    }

    public void setAwardedPrizeIds(List<String> awardedPrizeIds) {
        this.awardedPrizeIds = awardedPrizeIds;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof GameOnClaimPrizeListRequest)) return false;
        GameOnClaimPrizeListRequest that = (GameOnClaimPrizeListRequest) object;
        return Objects.equals(getAwardedPrizeIds(), that.getAwardedPrizeIds());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getAwardedPrizeIds());
    }

    @Override
    public String toString() {
        return "GameOnClaimPrizeListRequest{" +
                "awardedPrizeIds=" + awardedPrizeIds +
                '}';
    }

}
