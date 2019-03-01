package com.namazustudios.socialengine.model.gameon.game;

import com.namazustudios.socialengine.model.gameon.GameOnPrizeInfoType;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.List;
import java.util.Objects;

@ApiModel(description = "Corresponds to the GameOn Prize Details: " +
                        "https://developer.amazon.com/docs/gameon/game-api-ref.html#get-prize-details " +
                        "https://developer.amazon.com/docs/gameon/game-api-ref.html#getprizedetailsresponse")
public class GameOnGetPrizeDetailsResponse {
    @ApiModelProperty("Date prize was created.")
    private Long dateCreated;

    @ApiModelProperty("Date prize expires. (Optional)")
    private Long dateOfExpiration;

    @ApiModelProperty("Prize description.")
    private String description;

    @ApiModelProperty("Prize icon.")
    private String imageUrl;

    @ApiModelProperty("Prize ID.")
    private String prizeId;

    @ApiModelProperty("Describes what is contained in prizeInfo.")
    private GameOnPrizeInfoType prizeInfoType;

    @ApiModelProperty("Prize title.")
    private String title;

    public Long getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(Long dateCreated) {
        this.dateCreated = dateCreated;
    }

    public Long getDateOfExpiration() {
        return dateOfExpiration;
    }

    public void setDateOfExpiration(Long dateOfExpiration) {
        this.dateOfExpiration = dateOfExpiration;
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

    public String getPrizeId() {
        return prizeId;
    }

    public void setPrizeId(String prizeId) {
        this.prizeId = prizeId;
    }

    public GameOnPrizeInfoType getPrizeInfoType() {
        return prizeInfoType;
    }

    public void setPrizeInfoType(GameOnPrizeInfoType prizeInfoType) {
        this.prizeInfoType = prizeInfoType;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GameOnGetPrizeDetailsResponse that = (GameOnGetPrizeDetailsResponse) o;
        return Objects.equals(getDateCreated(), that.getDateCreated()) &&
                Objects.equals(getDateOfExpiration(), that.getDateOfExpiration()) &&
                Objects.equals(getDescription(), that.getDescription()) &&
                Objects.equals(getImageUrl(), that.getImageUrl()) &&
                Objects.equals(getPrizeId(), that.getPrizeId()) &&
                getPrizeInfoType() == that.getPrizeInfoType() &&
                Objects.equals(getTitle(), that.getTitle());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getDateCreated(), getDateOfExpiration(), getDescription(), getImageUrl(), getPrizeId(), getPrizeInfoType(), getTitle());
    }

    @Override
    public String toString() {
        return "GameOnGetPrizeDetailsResponse{" +
                "dateCreated=" + dateCreated +
                ", dateOfExpiration=" + dateOfExpiration +
                ", description='" + description + '\'' +
                ", imageUrl='" + imageUrl + '\'' +
                ", prizeId='" + prizeId + '\'' +
                ", prizeInfoType=" + prizeInfoType +
                ", title='" + title + '\'' +
                '}';
    }
}
