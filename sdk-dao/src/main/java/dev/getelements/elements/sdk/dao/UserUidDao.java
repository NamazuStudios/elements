package dev.getelements.elements.sdk.dao;

import dev.getelements.elements.sdk.model.exception.ForbiddenException;
import dev.getelements.elements.sdk.model.exception.user.UserNotFoundException;
import dev.getelements.elements.sdk.model.Pagination;
import dev.getelements.elements.sdk.model.user.User;
import dev.getelements.elements.sdk.model.user.UserUid;
import dev.getelements.elements.sdk.annotation.ElementServiceExport;

import java.util.Optional;

import static com.google.common.base.Strings.nullToEmpty;

/**
 * This is the UserUidDao which is used to manage associated unique user ids. These will represent different
 * methods of authentication and associate them with a singular User object. All user id schemes that begin with
 * 'dev.getelements' are reserved.
 * Created by keithhudnall on 1/29/25.
 */

@ElementServiceExport
public interface UserUidDao {

    String SCHEME_NAME = "dev.getelements.auth.scheme.name";
    String SCHEME_EMAIL = "dev.getelements.auth.scheme.email";
    String SCHEME_PHONE_NUMBER = "dev.getelements.auth.scheme.phone";

    /**
     * Gets all user uids for a given user
     *
     * @return the user uid pagination object
     */
    default List<UserUid> getAllUserIdsForUser(final User user) {
        return getAllUserIdsForUser(user.getId());
    }

    /**
     * Gets all user uids for a given user
     *
     * @return the user uid pagination object
     */
    List<UserUid> getAllUserIdsForUser(String userId);

    /**
     * Attempts to get a certain number of user uids
     * @param offset - the page offset
     * @param count - total results per page
     * @param search - search query - valid search properties are scheme, uid, and user id
     * @return the user uid pagination object
     */
    Pagination<UserUid> getUserUids(int offset, int count, String search);

    /**
     * Gets the user uid with the given name or id
     * @param id - the id associated with the scheme
     * @param scheme - the scheme name
     * @return the user uid object
     */
    UserUid getUserUid(String id, String scheme);

    /**
     * Gets the user uid with the given name or id
     * @param id - the id associated with the scheme
     * @param scheme - the scheme name
     * @return the user uid object
     */
    default Optional<UserUid> findUserUid(String id, String scheme) {
        try {
            return Optional.of(getUserUid(id, scheme));
        } catch (UserNotFoundException unfe) {
            return Optional.empty();
        }
    }

    /**
     * Attempts to create a new UserUid with the given userUid object.
     * Using "Strict" semantics, if the user exists then this will throw an exception.
     * @param userUid - the object to create in the db
     * @return - the resultant userUid with any modifications made as a result of the creation
     */
    @Deprecated
    UserUid createUserUidStrict(UserUid userUid);

    /**
     * Attempts to create a new UserUid with the given userUid object.
     * Using "Strict" semantics, if the user exists then this will throw an exception.
     * @param userUid - the object to create in the db
     * @return - the resultant userUid with any modifications made as a result of the creation
     */
    UserUid createUserUid(UserUid userUid);

    /**
     * Deletes a user uid by its UserUid object.
     *
     * @param userUid the UserUid object to delete
     * @throws UserNotFoundException if the UserUid with the given scheme and id does not exist
     */
    default void deleteUserUid(final UserUid userUid) {
        if (!tryDeleteUserUid(userUid.getScheme(), userUid.getId())) {
            throw new UserNotFoundException("UserUid with id " + userUid.getId() + " not found.");
        }
    }

    /**
     * Deletes a user uid by its scheme and user id.
     *
     * @param scheme Corresponds to {@link UserUid#getScheme()}
     * @param id Corresponds to {@link UserUid#getId()}
     * @throws UserNotFoundException if the UserUid with the given scheme and id does not exist
     */
    default void deleteUserUid(final String scheme, final String id) {
        if (!tryDeleteUserUid(scheme, id)) {
            throw new UserNotFoundException("UserUid with id " + id + " and/or scheme " + scheme + " not found.");
        }
    }

    /**
     * Deletes a user uid by its UserUid object.
     *
     * @param userUid the UserUid object to delete
     * @return true if the UserUid was found and deleted, false otherwise
     */
    default boolean tryDeleteUserUid(final UserUid userUid) {
        return tryDeleteUserUid(userUid.getScheme(), userUid.getId());
    }

    /**
     * Deletes a user uid by its scheme and user id.
     *
     * @param scheme Corresponds to {@link UserUid#getScheme()}
     * @param id Corresponds to {@link UserUid#getId()}
     * @return true if the UserUid was found and deleted, false otherwise
     */
    boolean tryDeleteUserUid(String scheme, String id);

    /**
     * Soft deletes the user by removing all ID references to the user in the UserUid records. This does not delete
     * the actual User object, just the references to it in UserUid records.
     *
     * It is STRONGLY RECOMMENDED to use this method from within a transaction to ensure data integrity as it requires
     * multiple operations to complete. Outside of a transaction, there is a substantial risk of partial completion
     * leaving the associated data in an inconsistent state.
     *
     * @param user the user to soft delete
     */
    default void softDeleteUser(final User user) {
        softDeleteUser(user.getId());
    }

    /**
     * Soft deletes the user by removing all ID references to the user in the UserUid records. This does not delete
     * the actual User object, just the references to it in UserUid records.
     *
     * It is STRONGLY RECOMMENDED to use this method from within a transaction to ensure data integrity as it requires
     * multiple operations to complete. Outside of a transaction, there is a substantial risk of partial completion
     * leaving the associated data in an inconsistent state.
     *
     * @param userId the user to soft delete
     * @throws UserNotFoundException if the user with the given id does not exist
     */
    default void softDeleteUser(final String userId) {
        if (!trySoftDeleteUser(userId)) {
            throw new UserNotFoundException("User with id " + userId + " not found.");
        }
    }

    /**
     * Soft deletes the user by removing all ID references to the user in the UserUid records. This does not delete
     * the actual User object, just the references to it in UserUid records.
     *
     * It is STRONGLY RECOMMENDED to use this method from within a transaction to ensure data integrity as it requires
     * multiple operations to complete. Outside of a transaction, there is a substantial risk of partial completion
     * leaving the associated data in an inconsistent state.
     *
     * @param userId the user to soft delete
     * @return true if the user was found and soft deleted, false otherwise
     */
    boolean trySoftDeleteUser(final String userId);

    /**
     * This will scrub all scheme ids for all UserUids referencing the user with the given id.
     *
     * It is STRONGLY RECOMMENDED to use this method from within a transaction to ensure data integrity as it requires
     * multiple operations to complete. Outside of a transaction, there is a substantial risk of partial completion
     * leaving the associated data in an inconsistent state.
     *
     * @param user - the id of the user to search for
     * @deprecated Use {@link #softDeleteUser(User)} instead.
     */
    default void softDeleteUserUidsForUserId(final User user) {
        softDeleteUser(user);
    }

}
