package com.namazustudios.socialengine.client.widget;

import com.namazustudios.socialengine.model.User;

/**
 * Created by patricktwohig on 5/6/15.
 */
public class UserLevelEnumDropDown extends EnumDropDown<User.Level> {

    public UserLevelEnumDropDown() {
        super(User.Level.class);
    }

}
