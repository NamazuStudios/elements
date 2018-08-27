package com.namazustudios.socialengine.model.gameon.game;

/**
 * Designates the match type when requesting matches from Amazon GameOn.
 */
public enum  MatchType {

    developer,
    player_generated { public String toString() { return "player-generated"; } },
    all;

    private static final MatchType[] values = values();

    /**
     * Converts the actual parameter value from a string.
     *
     * @param stringValue the string value
     * @return the {@link MatchType}
     */
    public static MatchType fromString(final String stringValue) {
        if (stringValue == null) return null;
        for (final MatchType m : values) if (stringValue.equals(m.toString())) return m;
        throw new IllegalArgumentException("Unknown Value: " + stringValue);
    }

}
