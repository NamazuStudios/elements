package com.namazustudios.socialengine.model;

import com.namazustudios.socialengine.Constants;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

/**
 * Extends the {@link BasicEntrantProfile} to include the
 * user's Steam ID.
 *
 * Created by patricktwohig on 3/18/15.
 */
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

}
