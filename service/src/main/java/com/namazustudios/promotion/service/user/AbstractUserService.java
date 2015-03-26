package com.namazustudios.promotion.service.user;

import com.namazustudios.promotion.exception.NotFoundException;
import com.namazustudios.promotion.model.User;
import com.namazustudios.promotion.service.UserService;

import javax.inject.Inject;
import java.util.Objects;

/**
 * Created by patricktwohig on 3/26/15.
 */
public abstract class AbstractUserService implements UserService {

    @Inject
    private User currentUser;


    @Override
    public User getCurrentUser() {
        return currentUser;
    }

    @Override
    public void checkForCurrentUser(final String userId) {
        if (!Objects.equals(currentUser.getEmail(), userId) && !Objects.equals(currentUser.getName(), userId)) {
            throw new NotFoundException("User with id " + userId + " not found.");
        }
    }

}
