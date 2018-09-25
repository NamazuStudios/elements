package com.namazustudios.socialengine.model.gameon.game;

import com.namazustudios.socialengine.model.gameon.GameOnPrizeInfoType;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.List;
import java.util.Objects;

@ApiModel(description = "Corresponds to the GameOn Prize Claim Response: " +
                        "https://developer.amazon.com/docs/gameon/game-api-ref.html#fulfillprizelistresponse")
public class GameOnFulfillPrizeListResponse {

    @ApiModelProperty("The GameOn Assigned external player ID.")
    private String externalPlayerId;

    @ApiModelProperty("A list of Prize IDs that failed to award.")
    private List<String> failedAwardedPrizeIds;

    @ApiModelProperty("A list of successfully claimed prizes.")
    private List<GameOnClaimPrizeListResponse.ClaimedPrize> prizes;

    public String getExternalPlayerId() {
        return externalPlayerId;
    }

    public void setExternalPlayerId(String externalPlayerId) {
        this.externalPlayerId = externalPlayerId;
    }

    public List<String> getFailedAwardedPrizeIds() {
        return failedAwardedPrizeIds;
    }

    public void setFailedAwardedPrizeIds(List<String> failedAwardedPrizeIds) {
        this.failedAwardedPrizeIds = failedAwardedPrizeIds;
    }

    public List<GameOnClaimPrizeListResponse.ClaimedPrize> getPrizes() {
        return prizes;
    }

    public void setPrizes(List<GameOnClaimPrizeListResponse.ClaimedPrize> prizes) {
        this.prizes = prizes;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof GameOnClaimPrizeListResponse)) return false;
        GameOnClaimPrizeListResponse that = (GameOnClaimPrizeListResponse) object;
        return Objects.equals(getExternalPlayerId(), that.getExternalPlayerId()) &&
                Objects.equals(getFailedAwardedPrizeIds(), that.getFailedAwardedPrizeIds()) &&
                Objects.equals(getPrizes(), that.getPrizes());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getExternalPlayerId(), getFailedAwardedPrizeIds(), getPrizes());
    }

    @Override
    public String toString() {
        return "GameOnClaimPrizeListResponse{" +
                "externalPlayerId='" + externalPlayerId + '\'' +
                ", failedAwardedPrizeIds=" + failedAwardedPrizeIds +
                ", prizes=" + prizes +
                '}';
    }

    @ApiModel(description = "Corresponds to the Fulfilled Prize Response: " +
                            "https://developer.amazon.com/docs/gameon/game-api-ref.html#fulfillprizelistresponse_fulfilledprize")
    public static class ClaimedPrize {

        @ApiModelProperty("The GameOn match ID that was used to claim this prize.")
        private String matchId;

        @ApiModelProperty("The prize info, corresponds to the prize info when the prize was created.")
        private String prizeInfo;

        @ApiModelProperty("The prize info type.")
        private GameOnPrizeInfoType prizeInfoType;

        @ApiModelProperty("The claimed prize status.")
        private GameOnPrizeFulfilledStatus status;

        public String getMatchId() {
            return matchId;
        }

        public void setMatchId(String matchId) {
            this.matchId = matchId;
        }

        public String getPrizeInfo() {
            return prizeInfo;
        }

        public void setPrizeInfo(String prizeInfo) {
            this.prizeInfo = prizeInfo;
        }

        public GameOnPrizeInfoType getPrizeInfoType() {
            return prizeInfoType;
        }

        public void setPrizeInfoType(GameOnPrizeInfoType prizeInfoType) {
            this.prizeInfoType = prizeInfoType;
        }

        public GameOnPrizeFulfilledStatus getStatus() {
            return status;
        }

        public void setStatus(GameOnPrizeFulfilledStatus status) {
            this.status = status;
        }

        @Override
        public boolean equals(Object object) {
            if (this == object) return true;
            if (!(object instanceof ClaimedPrize)) return false;
            ClaimedPrize that = (ClaimedPrize) object;
            return Objects.equals(getMatchId(), that.getMatchId()) &&
                    Objects.equals(getPrizeInfo(), that.getPrizeInfo()) &&
                    getPrizeInfoType() == that.getPrizeInfoType() &&
                    getStatus() == that.getStatus();
        }

        @Override
        public int hashCode() {

            return Objects.hash(getMatchId(), getPrizeInfo(), getPrizeInfoType(), getStatus());
        }

        @Override
        public String toString() {
            return "ClaimedPrize{" +
                    "matchId='" + matchId + '\'' +
                    ", prizeInfo='" + prizeInfo + '\'' +
                    ", prizeInfoType=" + prizeInfoType +
                    ", status=" + status +
                    '}';
        }

    }

}
