package com.namazustudios.socialengine.dao;

import com.namazustudios.socialengine.model.User;

public interface MockUserDao {


    User createMockUser(Integer lifetimeInSeconds, String password);

}
