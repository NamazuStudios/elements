package com.namazustudios.socialengine.model.gameon.game;

public enum  MatchFilter {

    claimed_prizes   { @Override public String toString() { return "claimed-prizes"; }},
    unclaimed_prizes { @Override public String toString() { return "unclaimed-prizes"; }},
    fulfilled_prizes { @Override public String toString() { return "fulfilled-prizes"; }},
    prizes_won       { @Override public String toString() { return "prizes-won"; }},
    no_prizes_won    { @Override public String toString() { return "prizes-won"; }},
    live,
    all;

    private static final MatchFilter[] values = values();

    /**
     * Converts the actual parameter value from a string.
     *
     * @param stringValue the string value
     * @return the {@link MatchType}
     */
    public static MatchFilter fromString(final String stringValue) {
        if (stringValue == null) return null;
        for (final MatchFilter m : values) if (stringValue.equals(m.toString())) return m;
        throw new IllegalArgumentException("Unknown Value: " + stringValue);
    }

}
