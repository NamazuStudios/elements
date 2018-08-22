package com.namazustudios.socialengine.model.gameon.admin;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;

@ApiModel(description = "Used by the GameOn API to create instances of prizes.  Used only in the Admin API.  " +
                        "See: https://developer.amazon.com/docs/gameon/admin-api-ref.html#addprizelistrequest")
public class GameOnAddPrizeListRequest implements Serializable {

    @NotNull
    @ApiModelProperty("Allows for the specification of one or more Prizes when creating a prize with Amazon GameOn")
    private List<Prize> prizes;

    public List<Prize> getPrizes() {
        return prizes;
    }

    public void setPrizes(List<Prize> prizes) {
        this.prizes = prizes;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof GameOnAddPrizeListRequest)) return false;
        GameOnAddPrizeListRequest that = (GameOnAddPrizeListRequest) object;
        return Objects.equals(getPrizes(), that.getPrizes());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getPrizes());
    }

    @Override
    public String toString() {
        return "AddPrizeListRequest{" +
                "prizes=" + prizes +
                '}';
    }

    @ApiModel("The Prize metadata itself.  " +
              "See: https://developer.amazon.com/docs/gameon/admin-api-ref.html#addprizelistrequest_prize")
    public static class Prize implements Serializable {

        @NotNull
        @ApiModelProperty("The title of the prize.")
        private String title;

        @NotNull
        @ApiModelProperty("A brief description of the prize.")
        private String description;

        @ApiModelProperty("The image URL for the prize.  This may be blank.")
        private String imageUrl;

        @NotNull
        @ApiModelProperty("The prize-info. Additional arbitrary metadata, used for claiming the prize later.  This " +
                          "should reflect, for example, an internal ID for the prize itself used by the application to " +
                          "uniquely identify it later.")
        private String prizeInfo;

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

        @Override
        public boolean equals(Object object) {
            if (this == object) return true;
            if (!(object instanceof Prize)) return false;
            Prize prize = (Prize) object;
            return Objects.equals(getTitle(), prize.getTitle()) &&
                    Objects.equals(getDescription(), prize.getDescription()) &&
                    Objects.equals(getImageUrl(), prize.getImageUrl()) &&
                    Objects.equals(getPrizeInfo(), prize.getPrizeInfo());
        }

        @Override
        public int hashCode() {
            return Objects.hash(getTitle(), getDescription(), getImageUrl(), getPrizeInfo());
        }
    }

}
