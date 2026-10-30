package dev.getelements.elements.dao.mongo.test;

import dev.getelements.elements.dao.mongo.model.MongoUser;
import dev.getelements.elements.sdk.dao.UserDao;
import dev.getelements.elements.sdk.model.user.User;
import dev.morphia.Datastore;
import dev.morphia.UpdateOptions;
import org.bson.types.ObjectId;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import jakarta.inject.Inject;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static dev.getelements.elements.sdk.model.user.User.Level.USER;
import static dev.morphia.query.filters.Filters.eq;
import static dev.morphia.query.updates.UpdateOperators.unset;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * Covers the visibility convention for the superuser user listing: a user is hidden from the listing
 * only when soft-deleted (the {@code name} field is cleared), never merely because it lacks a
 * {@code linkedAccounts} array. Users created before linked accounts existed (or by provisioning paths
 * that do not populate them) must remain visible.
 */
@Guice(modules = IntegrationTestModule.class)
public class MongoUserDaoGetUsersVisibilityTest {

    private final String legacyUserName = "legacy-" + UUID.randomUUID();

    private final String legacyUserEmail = legacyUserName + "@example.com";

    private final String softDeletedUserName = "deleted-" + UUID.randomUUID();

    private final String softDeletedUserEmail = softDeletedUserName + "@example.com";

    private User legacyUser;

    private User softDeletedUser;

    private UserDao userDao;

    private Datastore datastore;

    @BeforeClass
    public void setup() {

        legacyUser = createUser(legacyUserName, legacyUserEmail);
        softDeletedUser = createUser(softDeletedUserName, softDeletedUserEmail);

        // Simulate a user created before the linked-accounts feature (or by a provisioning path that
        // does not populate it): the document is fully intact except for the linkedAccounts array.

        getDatastore()
                .find(MongoUser.class)
                .filter(eq("_id", new ObjectId(legacyUser.getId())))
                .update(new UpdateOptions(), unset("linkedAccounts"));

        // Guard the fixture: the unset must have actually applied, otherwise the tests below assert
        // nothing.

        assertFalse(getDatastore().find(MongoUser.class)
                        .filter(eq("_id", new ObjectId(legacyUser.getId())))
                        .first()
                        .getLinkedAccounts() != null,
                "Fixture broken: linkedAccounts was not unset.");

    }

    private User createUser(final String name, final String email) {
        final User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setLevel(USER);
        return getUserDao().createUser(user);
    }

    @Test
    public void userWithoutLinkedAccountsIsListed() {

        assertTrue(listedUserIds().contains(legacyUser.getId()),
                "A user lacking linkedAccounts must appear in the user listing.");

        final var search = getUserDao().getUsers(0, 1000, legacyUserName);
        assertTrue(search.getObjects().stream().anyMatch(u -> u.getId().equals(legacyUser.getId())),
                "A user lacking linkedAccounts must be found by search.");

    }

    @Test
    public void softDeletedUserIsNotListed() {

        getUserDao().softDeleteUser(softDeletedUser.getId());

        assertFalse(listedUserIds().contains(softDeletedUser.getId()),
                "A soft-deleted user must not appear in the user listing.");

        assertFalse(getUserDao().getUsers(0, 1000, softDeletedUserName).getObjects().stream()
                        .anyMatch(u -> u.getId().equals(softDeletedUser.getId())),
                "A soft-deleted user must not be found by search.");

    }

    /**
     * The suite's other tests create enough users that the target may fall beyond any single page, so the
     * listing is walked to exhaustion rather than sampled.
     */
    private Set<String> listedUserIds() {

        final var ids = new HashSet<String>();

        for (int offset = 0, total = Integer.MAX_VALUE; offset < total; ) {
            final var page = getUserDao().getUsers(offset, 100);
            total = (int) page.getTotal();
            page.getObjects().forEach(u -> ids.add(u.getId()));
            offset += 100;
        }

        return ids;

    }

    public UserDao getUserDao() {
        return userDao;
    }

    @Inject
    public void setUserDao(UserDao userDao) {
        this.userDao = userDao;
    }

    public Datastore getDatastore() {
        return datastore;
    }

    @Inject
    public void setDatastore(Datastore datastore) {
        this.datastore = datastore;
    }

}
