package com.namazustudios.socialengine.model.gameon.admin;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.List;
import java.util.Objects;

@ApiModel(description =
    "Used by the GameOn API to return instances of prizes that were created or failed to create.  Used only in the " +
    "Admin API.  See: https://developer.amazon.com/docs/gameon/admin-api-ref.html#addprizelistrequest")
public class GameOnAddPrizeListResponse {

    private List<Prize> addedPrizes;

    private List<Prize> failedPrizes;

    public List<Prize> getAddedPrizes() {
        return addedPrizes;
    }

    public void setAddedPrizes(List<Prize> addedPrizes) {
        this.addedPrizes = addedPrizes;
    }

    public List<Prize> getFailedPrizes() {
        return failedPrizes;
    }

    public void setFailedPrizes(List<Prize> failedPrizes) {
        this.failedPrizes = failedPrizes;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof GameOnAddPrizeListResponse)) return false;
        GameOnAddPrizeListResponse that = (GameOnAddPrizeListResponse) object;
        return Objects.equals(getAddedPrizes(), that.getAddedPrizes()) &&
                Objects.equals(getFailedPrizes(), that.getFailedPrizes());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getAddedPrizes(), getFailedPrizes());
    }

    @Override
    public String toString() {
        return "AddPrizeListResponse{" +
                "addedPrizes=" + addedPrizes +
                ", failedPrizes=" + failedPrizes +
                '}';
    }

    @ApiModel(description =
        "The metadata returns from creating a prize.  " +
        "See: https://developer.amazon.com/docs/gameon/admin-api-ref.html#addprizelistresponse_prize")
    public static class Prize {

        @ApiModelProperty("The ID of the prize.")
        private String prizeId;

        @ApiModelProperty("The title of the prize.")
        private String title;

        @ApiModelProperty("A brief description of the prize.")
        private String description;

        @ApiModelProperty("The image URL for the prize.")
        private String imageUrl;

        @ApiModelProperty("The prize info. (Use for redeeming prizes after a tournament).")
        private String prizeInfo;

        @ApiModelProperty("The prize info type.")
        private PrizeInfoType prizeInfoType;

        public String getPrizeId() {
            return prizeId;
        }

        public void setPrizeId(String prizeId) {
            this.prizeId = prizeId;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getImageUrl() {
            return imageUrl;
        }

        public void setImageUrl(String imageUrl) {
            this.imageUrl = imageUrl;
        }

        public String getPrizeInfo() {
            return prizeInfo;
        }

        public void setPrizeInfo(String prizeInfo) {
            this.prizeInfo = prizeInfo;
        }

        public PrizeInfoType getPrizeInfoType() {
            return prizeInfoType;
        }

        public void setPrizeInfoType(PrizeInfoType prizeInfoType) {
            this.prizeInfoType = prizeInfoType;
        }

        @Override
        public boolean equals(Object object) {
            if (this == object) return true;
            if (!(object instanceof Prize)) return false;
            Prize prize = (Prize) object;
            return Objects.equals(getPrizeId(), prize.getPrizeId()) &&
                    Objects.equals(getTitle(), prize.getTitle()) &&
                    Objects.equals(getDescription(), prize.getDescription()) &&
                    Objects.equals(getImageUrl(), prize.getImageUrl()) &&
                    Objects.equals(getPrizeInfo(), prize.getPrizeInfo()) &&
                    getPrizeInfoType() == prize.getPrizeInfoType();
        }

        @Override
        public int hashCode() {
            return Objects.hash(getPrizeId(), getTitle(), getDescription(), getImageUrl(), getPrizeInfo(), getPrizeInfoType());
        }

        @Override
        public String toString() {
            return "Prize{" +
                    "prizeId='" + prizeId + '\'' +
                    ", title='" + title + '\'' +
                    ", description='" + description + '\'' +
                    ", imageUrl='" + imageUrl + '\'' +
                    ", prizeInfo='" + prizeInfo + '\'' +
                    ", prizeInfoType=" + prizeInfoType +
                    '}';
        }

    }

}
