package com.namazustudios.socialengine.model;

import com.namazustudios.socialengine.Constants;
import io.swagger.annotations.ApiModel;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

/**
 * Extends the {@link BasicEntrantProfile} to include the
 * user's Steam ID.
 *
 * Created by patricktwohig on 3/18/15.
 */
@ApiModel
public class SteamEntrantProfile extends BasicEntrantProfile {

    @NotNull
    @Pattern(regexp = Constants.Regexp.NON_BLANK_STRING)
    private String steamId;

    public String getSteamId() {
        return steamId;
    }

    public void setSteamId(String steamId) {
        this.steamId = steamId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SteamEntrantProfile)) return false;

        SteamEntrantProfile that = (SteamEntrantProfile) o;

        return getSteamId() != null ? getSteamId().equals(that.getSteamId()) : that.getSteamId() == null;
    }

    @Override
    public int hashCode() {
        return getSteamId() != null ? getSteamId().hashCode() : 0;
    }

}
