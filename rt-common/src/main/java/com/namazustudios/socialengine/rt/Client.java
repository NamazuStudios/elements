package com.namazustudios.socialengine.rt;

import com.namazustudios.socialengine.model.User;

/**
 * Represents a currently connected client.  This includes both a
 * unique connection ID and the {@link User} which is associated
 * with the connection.
 *
 * Created by patricktwohig on 7/26/15.
 */
public class Client {

    private String id;

    private User user;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

}
