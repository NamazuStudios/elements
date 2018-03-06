package com.namazustudios.socialengine.client.rest.client;

import com.namazustudios.socialengine.model.User;
import com.namazustudios.socialengine.model.session.SessionCreation;
import com.namazustudios.socialengine.model.session.UsernamePasswordSessionRequest;
import org.fusesource.restygwt.client.MethodCallback;
import org.fusesource.restygwt.client.Options;
import org.fusesource.restygwt.client.RestService;

import javax.ws.rs.DELETE;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;

/**
 * Created by patricktwohig on 4/16/15.
 */
@Path("session")
@Options()
public interface LoginClient extends RestService {

    /**
     * Invokes POST /session
     *
     * @param usernamePasswordSessionRequest the username/password request instance
     * @param methodCallback the method callback
     */
    @POST
    void login(final UsernamePasswordSessionRequest usernamePasswordSessionRequest,
               final MethodCallback<SessionCreation> methodCallback);

    /**
     * Destroys the user's current session.
     *
     * @param methodCallback
     */
    @DELETE
    void logout(final MethodCallback<Void> methodCallback);

}
