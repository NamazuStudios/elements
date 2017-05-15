package com.namazustudios.socialengine.client.rest.client;

import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestException;
import org.fusesource.restygwt.client.Dispatcher;
import org.fusesource.restygwt.client.Method;

/**
 * Ensures that all settings are correctly configured to make CORS compatible requests.
 *
 * Created by patricktwohig on 5/11/17.
 */
public class CORSRequestDispatcher implements Dispatcher {

    @Override
    public Request send(Method method, RequestBuilder builder) throws RequestException {
        builder.setIncludeCredentials(true);
        return builder.send();
    }

}
