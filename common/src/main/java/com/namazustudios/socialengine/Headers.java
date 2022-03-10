package com.namazustudios.socialengine;

import com.namazustudios.socialengine.model.session.SessionCreation;

/**
 * A place to store the non-standard HTTP header (and related) constants.
 *
 * Created by patricktwohig on 6/26/17.
 */
public interface Headers {

    /**
     * Used to trigger a Long-Polling type of requests.  This requests that the server wait on a response until certain
     * conditions trigger a response.  The server will make its best effort attempt to wait until either the response is
     * ready or the timeout hits.  However, the serer may elect to terminate the request sooner than specified.
     *
     * A value of 0 indicates that the server should determine timeout, but hold the requests for as long as reasonably
     * possible.
     */
    String REQUEST_LONG_POLL_TIMEOUT = "SocialEngine-LongPoll-Timeout";

    /**
     * An API-wide specification for the header specified by {@link #REQUEST_LONG_POLL_TIMEOUT}.
     */
    String REQUEST_LONG_POLL_TIMEOUT_DESCRIPTION = "The maximum amount time the server will wait until a " +
            "request returns a default set of data for long polling.  Specifying a zero will request that the " +
            "server wait indefinitely until responding.  Though, the server may enforce a practical upper limit " +
            "on the amount of time it takes to return.  Omitting this header will prompt the server to treat " +
            "the request as a normal request.";

    /**
     * Specifies the profile ID to use when authing the request.
     */
    String PROFILE_ID = "Elements-ProfileId";

    /**
     * Specifies the Session ID used by Elements required to send any request.
     */
    String GLOBAL_SECRET = "Elements-GlobalSecret";

    /**
     * Specifies the Session ID used by social engine.  The Session ID corresponds to {@link SessionCreation} and
     * is used for authentication.
     */
    String SESSION_SECRET = "Elements-SessionSecret";

    /**
     * Header constant for deprecated header name.  This will still work transparently, but will not match the
     * documentation.  Used for backwards compatibility only.
     *
     * @deprecated use {@link #SESSION_SECRET}
     */
    @Deprecated
    String SOCIALENGINE_SESSION_SECRET = "SocialEngine-SessionSecret";

    /**
     * Specifies the user's Facebook OAuth token used to interact with the Facebook API.
     */
    String FACEBOOK_OAUTH_TOKEN = "Facebook-OAuthToken";

    /**
     * Preferred auth method, bearer.
     */
    String BEARER = "Bearer";

    String ORIGIN = "Origin";

    String AC_ALLOW_ORIGIN = "Access-Control-Allow-Origin";

    String AC_ALLOW_HEADERS = "Access-Control-Allow-Headers";

    String AC_ALLOW_HEADERS_VALUE =
        "X-HTTP-Method-Override, " +
        "Content-Type, " +
        "SocialEngine-SessionSecret, " +
        "Elements-SessionSecret, " +
        "Authorization";

    String AC_ALLOW_CREDENTIALS = "Access-Control-Allow-Credentials";

    String AC_ALLOW_CREDENTIALS_VALUE = "true";

    String AC_ALLOW_ALLOW_METHODS = "Access-Control-Allow-Methods";

    String AC_ALLOW_ALLOW_METHODS_VALUE = "GET, POST, PUT, PATCH, DELETE";

}
