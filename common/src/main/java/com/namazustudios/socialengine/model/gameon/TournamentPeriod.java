package com.namazustudios.socialengine.model.gameon;

import io.swagger.annotations.ApiModel;

@ApiModel("Tournament filter enumeration.  Used with the filter to filter by specific intervals.")
public enum TournamentPeriod {
    day,
    week,
    month,
    all
}
