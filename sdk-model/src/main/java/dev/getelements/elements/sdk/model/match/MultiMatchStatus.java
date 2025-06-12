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
     * The match is in progress, no longer accepting new entries.
     */
    IN_PROGRESS,

    /**
     * The match has ended and will be deleted at some point in the future.
     */
    ENDED

}
