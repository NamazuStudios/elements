package dev.getelements.elements.sdk.service.auth;

import dev.getelements.elements.sdk.annotation.ElementPublic;
import dev.getelements.elements.sdk.annotation.ElementServiceExport;
import dev.getelements.elements.sdk.model.exception.user.UserNotFoundException;
import dev.getelements.elements.sdk.model.Pagination;
import dev.getelements.elements.sdk.model.user.UserUid;

import java.util.Optional;

@ElementPublic
@ElementServiceExport
public interface UserUidService {

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
    UserUid createUserUidStrict(UserUid userUid);

    /**
     * Attempts to create or update a new UserUid with the given userUid object.
     * @param userUid - the object to create or update in the db
     * @return - the resultant userUid with any modifications made as a result of the creation or update
     */
    UserUid createOrUpdateUserUid(UserUid userUid);

    /**
     * This will scrub all scheme ids for all UserUids referencing the user with the given id.
     * @param userId - the id of the user to search for
     */
    void softDeleteUserUidsForUserId(String userId);

}
