package com.namazustudios.socialengine.client.controlpanel;

/**
 * Used as the nexus for the configuration as handed through the config.js script.
 *
 * Created by patricktwohig on 5/9/17.
 */
public class Config {

    /**
     * Gets the API Root URL from the configuration.
     *
     * @return the API root url
     */
    public static native String getApiRoot() /*-{
        return $wnd.config.apiRootUrl;
    }-*/;

}
