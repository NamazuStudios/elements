package dev.getelements.elements.service.auth;

import dev.getelements.elements.sdk.model.Pagination;
import dev.getelements.elements.sdk.model.user.UserUid;
import dev.getelements.elements.sdk.service.auth.UserUidService;

import java.util.Optional;

public class AnonUserUidService implements UserUidService {

    @Override
    public Pagination<UserUid> getUserUids(int offset, int count, String search) {
        return null;
    }

    @Override
    public UserUid getUserUid(String id, String scheme) {
        return null;
    }

    @Override
    public Optional<UserUid> findUserUid(String id, String scheme) {
        return UserUidService.super.findUserUid(id, scheme);
    }

    @Override
    public UserUid createUserUidStrict(UserUid userUid) {
        return null;
    }

    @Override
    public UserUid createOrUpdateUserUid(UserUid userUid) {
        return null;
    }

    @Override
    public void softDeleteUserUidsForUserId(String userId) {

    }
}
