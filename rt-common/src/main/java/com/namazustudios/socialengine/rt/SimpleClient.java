package com.namazustudios.socialengine.rt;

import com.namazustudios.socialengine.model.User;

/**
 * Represents a currently connected client.  This includes both a
 * unique connection ID and the {@link User} which is associated
 * with the connection.
 *
 * Created by patricktwohig on 7/26/15.
 */
public abstract class SimpleClient implements Client {

    private String id;

    public SimpleClient(String id) {
        this.id = id;
    }

    @Override
    public String getId() {
        return id;
    }

}
