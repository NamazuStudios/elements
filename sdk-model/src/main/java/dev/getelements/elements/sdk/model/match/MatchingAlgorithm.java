package dev.getelements.elements.sdk.model.match;

import java.io.Serializable;

/**
 * Represents the supported matching algorithms supported by SocialEngine.
 *
 * Created by patricktwohig on 7/27/17.
 */
public enum MatchingAlgorithm implements Serializable {

    /**
     * The simplest form of matching.  Implements a FIFO, first in, first out.  That is to
     * say that the first player will match immediately with the next player requesting
     * a match.  This does not consider skill, rank, or any other factors.
     */
    FIFO

}
