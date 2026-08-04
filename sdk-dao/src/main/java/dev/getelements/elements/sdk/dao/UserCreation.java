package dev.getelements.elements.sdk.dao;

import dev.getelements.elements.sdk.model.exception.DuplicateException;
import dev.getelements.elements.sdk.model.user.User;
import dev.getelements.elements.sdk.model.user.UserUid;
import dev.getelements.elements.sdk.model.user.VerificationStatus;

import java.util.Map;

/**
 * Builds a {@link User} incrementally, together with explicitly-requested {@link UserUid} links, committing
 * everything when {@link #create()} is called.
 * <p>
 * Obtained via {@link UserDao#newEmptyUser()}. Unlike {@link UserDao#createUserStrict(User)} and its
 * siblings, this performs no automatic UserUid linking for email/name/phone — every scheme association must
 * be requested explicitly via {@link #uid(String, String, VerificationStatus)}. This exists for callers
 * (e.g. identity-provider integrations) that need full control over which schemes get linked, in what order,
 * and that would otherwise collide with the automatic linking performed by {@link UserDao#createUserStrict(User)}.
 */
public interface UserCreation {

    /**
     * Sets the user's level.
     *
     * @param level the level
     * @return this instance
     */
    UserCreation level(User.Level level);

    /**
     * Sets the user's email.
     *
     * @param email the email
     * @return this instance
     */
    UserCreation email(String email);

    /**
     * Sets the user's name.
     *
     * @param name the name
     * @return this instance
     */
    UserCreation name(String name);

    /**
     * Sets the user's first name.
     *
     * @param firstName the first name
     * @return this instance
     */
    UserCreation firstName(String firstName);

    /**
     * Sets the user's last name.
     *
     * @param lastName the last name
     * @return this instance
     */
    UserCreation lastName(String lastName);

    /**
     * Sets the user's preferred username.
     *
     * @param preferredUsername the preferred username
     * @return this instance
     */
    UserCreation preferredUsername(String preferredUsername);

    /**
     * Records a linked-account profile snapshot for the given scheme.
     *
     * @param scheme the scheme the claims came from
     * @param claims the claims
     * @return this instance
     */
    UserCreation linkedAccountProfile(String scheme, Map<String, String> claims);

    /**
     * Requests that a {@link UserUid} be linked to this user once created. If a UserUid with the same
     * (scheme, id) already exists but is orphaned (unresolvable to any user), it is replaced. If it
     * resolves to a different, existing user, {@link #create()} throws {@link DuplicateException}.
     *
     * @param scheme the UserUid scheme
     * @param id the scheme-specific id
     * @param status the verification status to assign the link
     * @return this instance
     */
    UserCreation uid(String scheme, String id, VerificationStatus status);

    /**
     * Commits the {@link User} and every requested {@link UserUid} link.
     *
     * @return the created {@link User}
     * @throws DuplicateException if a requested UserUid is already linked to a different, existing user
     */
    User create();

}
