package com.namazustudios.socialengine.model.gameon;

/**
 * Session device OS Type.  Note, lowercase notation is used to be consistent with Amazon's APIs
 */
public enum DeviceOSType {

    /**
     * Amazon FireOS
     */
    fireos,

    /**
     * Android
     */
    android,

    /**
     * Apple/iOS
     */
    ios,

    /**
     * PC/Windows
     */
    pc,

    /**
     * Apple/OSX
     */
    mac,

    /**
     * Linux
     */
    linux,

    /**
     * Microsoft Xbox
     */
    xbox,

    /**
     * Sony Plastation
     */
    playstation,

    /**
     * Nintendo
     */
    nintendo,

    /**
     * HTML5/JavaScript Games
     */
    html;

    /**
     * The default type, as a hardcoded string.  (Designated this way to be used with enum constants).
     */
    public static final String DEFAULT_TYPE_STRING = "html";

    /**
     * The default type when not specified.  Currently defaults to {@link #html} per Amazon's recommendation.
     *
     * @return the default OS type.
     */
    public static DeviceOSType getDefault() {
        return html;
    }

}
