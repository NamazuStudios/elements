package dev.getelements.elements.sdk.model.match;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Indicates the current status of the {@link MultiMatch}.
 */
@Schema
public enum MultiMatchStatus {

    /**
     * The match is open and accepting new players. Wen the match hits the maximum number of players defined in the
     * configuration it will automatically transition to FULL.
     */
    OPEN,

    /**
     * The match is full and no longer accepting new players. But may be reopened if a player leaves. If players leaves
     * while the match is FULL the match will automatically transition back to OPEN.
     */
    FULL,

    /**
     * The match is closed to new players and will not accept new players, even if somebody leaves. The match may be
     * reopened, though explicit actions, but it will not automatically transition back to OPEN if a player leaves
     * thereby effectively locking new players out regardless of player departures.
     */
    CLOSED,
    
    /**
     * The match has ended and will be deleted shortly, based on the configuration of the match. No new players may
     * join, nor the match reopened. Matches in this state will linger for a brief period of time for the sake of
     * reporting or end of game victory celebration. Code must always consider matches in this state as effectively
     * deleted.
     */
    ENDED

}
