package dev.getelements.elements.sdk.model.match;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Indicates the current status of the {@link MultiMatch}.
 */
@Schema
public enum MultiMatchStatus {

    /**
     * The match is open and accepting new players.
     */
    OPEN,

    /**
     * The match is full and no longer accepting new players.
     */
    FULL,

    /**
     * The match is in progress.
     */
    IN_PROGRESS,

    /**
     * The match has ended and will be deleted at some point in the future.
     */
    ENDED

}
