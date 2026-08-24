package dev.getelements.elements.dao.mongo.test;

import dev.getelements.elements.sdk.ElementRegistry;
import dev.getelements.elements.sdk.Event;
import dev.getelements.elements.sdk.dao.*;
import dev.getelements.elements.sdk.model.user.User;
import dev.morphia.Datastore;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Provider;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static dev.getelements.elements.sdk.ElementRegistry.ROOT;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

@Guice(modules = IntegrationTestModule.class)
public class MongoTransactionTest {

    private Datastore datastore;

    private Provider<Transaction> transactionProvider;

    private UserTestFactory userTestFactory;

    @Inject
    @Named(ROOT)
    private ElementRegistry elementRegistry;

    @DataProvider
    public static Object[][] daoClasses() {
        return new Object[][] {
                new Object[] {IndexDao.class},
                new Object[] {UserDao.class},
                new Object[] {UserUidDao.class},
                new Object[] {ProfileDao.class},
                new Object[] {ApplicationDao.class},
                new Object[] {ApplicationConfigurationDao.class},
                new Object[] {MatchDao.class},
                new Object[] {MultiMatchDao.class},
                new Object[] {FCMRegistrationDao.class},
                new Object[] {SessionDao.class},
                new Object[] {LeaderboardDao.class},
                new Object[] {ScoreDao.class},
                new Object[] {RankDao.class},
                new Object[] {FriendDao.class},
                new Object[] {ItemDao.class},
                new Object[] {InventoryItemDao.class},
                new Object[] {MissionDao.class},
                new Object[] {ProgressDao.class},
                new Object[] {RewardIssuanceDao.class},
                new Object[] {AppleIapReceiptDao.class},
                new Object[] {GooglePlayIapReceiptDao.class},
                new Object[] {FollowerDao.class},
                new Object[] {TokensWithExpirationDao.class},
                new Object[] {DeploymentDao.class},
                new Object[] {DatabaseHealthStatusDao.class},
                new Object[] {MetadataDao.class},
                new Object[] {MetadataSpecDao.class},
                new Object[] {SaveDataDocumentDao.class},
                new Object[] {AuthSchemeDao.class},
                new Object[] {OidcAuthSchemeDao.class},
                new Object[] {OAuth2AuthSchemeDao.class},
                new Object[] {DistinctInventoryItemDao.class},
                new Object[] {WalletDao.class},
                new Object[] {SmartContractDao.class},
                new Object[] {VaultDao.class},
                new Object[] {LargeObjectDao.class},
                new Object[] {ScheduleDao.class},
                new Object[] {ScheduleEventDao.class},
                new Object[] {ScheduleProgressDao.class }
        };
    }

    @Test(dataProvider = "daoClasses")
    public void testTransaction(final Class<?> daoT) {
        try (final var txn = getTransactionProvider().get()) {
            txn.getDao(daoT);
        }
    }

    @Test
    public void testTransactionalEventPublishesImmediatelyAndPlainEventOnlyAfterCommit() {

        final var events = new CopyOnWriteArrayList<Event>();
        final var subscription = getElementRegistry().onEvent(events::add);

        final var toCreate = getUserTestFactory().buildTestUser();
        final User created;

        try {

            try (final var txn = getTransactionProvider().get()) {

                created = txn.getDao(UserDao.class).createUserStrict(toCreate);

                assertTrue(
                        hasTransactionalCreatedEvent(events, created.getId()),
                        "Expected the {User, Transaction} USER_CREATED event to fire immediately, inside the transaction"
                );

                assertFalse(
                        hasPlainCreatedEvent(events, created.getId()),
                        "Did not expect the plain USER_CREATED event before the transaction commits"
                );

                txn.commit();

            }

            assertTrue(
                    hasPlainCreatedEvent(events, created.getId()),
                    "Expected the plain USER_CREATED event to fire once the transaction commits"
            );

        } finally {
            subscription.unsubscribe();
        }

    }

    @Test
    public void testBufferedPlainEventIsDroppedOnRollback() {

        final var events = new CopyOnWriteArrayList<Event>();
        final var subscription = getElementRegistry().onEvent(events::add);

        final var toCreate = getUserTestFactory().buildTestUser();

        try {

            final String createdId;

            try (final var txn = getTransactionProvider().get()) {

                final var created = txn.getDao(UserDao.class).createUserStrict(toCreate);
                createdId = created.getId();

                assertTrue(
                        hasTransactionalCreatedEvent(events, createdId),
                        "Expected the {User, Transaction} USER_CREATED event to fire immediately, inside the transaction"
                );

                txn.rollback();

            }

            assertFalse(
                    hasPlainCreatedEvent(events, createdId),
                    "Did not expect the buffered plain USER_CREATED event to fire after a rollback"
            );

        } finally {
            subscription.unsubscribe();
        }

    }

    private boolean hasTransactionalCreatedEvent(final List<Event> events, final String userId) {
        return events.stream().anyMatch(ev ->
                UserDao.USER_CREATED.equals(ev.getEventName()) &&
                ev.getEventArguments().size() == 2 &&
                ev.getEventArgument(0, User.class).getId().equals(userId) &&
                ev.getEventArgument(1) instanceof Transaction
        );
    }

    private boolean hasPlainCreatedEvent(final List<Event> events, final String userId) {
        return events.stream().anyMatch(ev ->
                UserDao.USER_CREATED.equals(ev.getEventName()) &&
                ev.getEventArguments().size() == 1 &&
                ev.getEventArgument(0, User.class).getId().equals(userId)
        );
    }

    public UserTestFactory getUserTestFactory() {
        return userTestFactory;
    }

    @Inject
    public void setUserTestFactory(UserTestFactory userTestFactory) {
        this.userTestFactory = userTestFactory;
    }

    public ElementRegistry getElementRegistry() {
        return elementRegistry;
    }

    public Datastore getDatastore() {
        return datastore;
    }

    @Inject
    public void setDatastore(Datastore datastore) {
        this.datastore = datastore;
    }

    public Provider<Transaction> getTransactionProvider() {
        return transactionProvider;
    }

    @Inject
    public void setTransactionProvider(Provider<Transaction> transactionProvider) {
        this.transactionProvider = transactionProvider;
    }

}
