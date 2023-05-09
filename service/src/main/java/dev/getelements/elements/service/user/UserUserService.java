package dev.getelements.elements.service.user;

import com.google.common.collect.Lists;
import dev.getelements.elements.exception.ForbiddenException;
import dev.getelements.elements.model.Pagination;
import dev.getelements.elements.model.user.User;
import dev.getelements.elements.model.user.UserCreateRequest;
import dev.getelements.elements.model.user.UserCreateResponse;
import dev.getelements.elements.model.user.UserUpdateRequest;
import dev.getelements.elements.service.UserService;

import static com.google.common.base.Strings.isNullOrEmpty;
import static com.google.common.base.Strings.nullToEmpty;

/**
 * Created by patricktwohig on 3/26/15.
 */
public class UserUserService extends AnonUserService implements UserService {

    @Override
    public User getUser(String userId) {
        checkForCurrentUser(userId);
        return getCurrentUser();
    }

    @Override
    public Pagination<User> getUsers(int offset, int count) {
        if (offset < 0) {
            throw new IllegalArgumentException("Invalid offset: " + offset);
        } else if (offset == 0) {

            // The only user you are allowed to see is yourself.

            final Pagination<User> entry = new Pagination<>();
            entry.setOffset(0);
            entry.setTotal(1);
            entry.setObjects(Lists.newArrayList(getCurrentUser()));
            return entry;

        } else {
            return new Pagination<>();
        }
    }

    @Override
    public Pagination<User> getUsers(int offset, int count, String search) {
        return getUsers(offset, count);
    }

    @Override
    public UserCreateResponse createUser(UserCreateRequest user) {
        throw new ForbiddenException();
    }

    @Override
    public User updateUser(final String userId, final UserUpdateRequest userUpdateRequest) {

        checkForCurrentUser(userId);

        final User user = new User();

        // Regular users cannot change their own level or change their name.  The underlying DAO
        // may support name changes, but this cannot be done here.

        user.setId(userId);
        user.setLevel(User.Level.USER);
        user.setName(getCurrentUser().getName());
        user.setEmail(getCurrentUser().getEmail());

        // Regular users can't use this call to deactivate their user as well.  This must be done through
        // a delete operation.

        user.setActive(true);

        final var password = nullToEmpty(userUpdateRequest.getPassword()).trim();

        return isNullOrEmpty(password) ?
            getUserDao().updateActiveUser(user) :
            getUserDao().updateActiveUser(user, password);

    }

    @Override
    public void deleteUser(String userId) {
        // The user can only delete his or her own account.
        checkForCurrentUser(userId);
        getUserDao().softDeleteUser(userId);
    }

}
