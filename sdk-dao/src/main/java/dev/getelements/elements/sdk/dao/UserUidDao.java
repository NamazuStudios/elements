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
     * This will scrub all scheme ids for all UserUids referencing the user with the given id.
     * @param user - the id of the user to search for
     */
    void softDeleteUserUidsForUserId(User user);
}
