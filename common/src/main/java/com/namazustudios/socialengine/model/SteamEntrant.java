package com.namazustudios.socialengine.model;

import javax.validation.constraints.Pattern;

/**
 * Extends the {@link com.namazustudios.socialengine.model.BasicEntrant} to include the
 * user's Steam ID.
 *
 * Created by patricktwohig on 3/18/15.
 */
public class SteamEntrant extends BasicEntrant {

    @Pattern(regexp = "\\s*")
    private String steamId;

    public String getSteamId() {
        return steamId;
    }

    public void setSteamId(String steamId) {
        this.steamId = steamId;
    }

}
