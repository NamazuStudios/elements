package com.namazustudios.promotion.model;

/**
 * Extends the {@link com.namazustudios.promotion.model.BasicEntrant} to include the
 * user's Steam ID.
 *
 * Created by patricktwohig on 3/18/15.
 */
public class SteamEntrant extends BasicEntrant {

    private String steamId;

    public String getSteamId() {
        return steamId;
    }

    public void setSteamId(String steamId) {
        this.steamId = steamId;
    }

}
