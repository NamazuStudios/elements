package com.namazustudios.socialengine.model.match;

import com.namazustudios.socialengine.model.TimeDelta;
import io.swagger.annotations.ApiModel;

import java.io.Serializable;

/**
 * Created by patricktwohig on 7/20/17.
 */
@ApiModel("A TimeDelta type for Match instance.")
public class MatchTimeDelta extends TimeDelta<String, Match> implements Serializable {

    public static final String ROOT_DELTA_TOPIC = "match";

}
