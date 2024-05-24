package dev.getelements.elements.dao.mongo.test;

import dev.getelements.elements.dao.*;
import dev.morphia.Datastore;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import javax.inject.Inject;
import javax.inject.Provider;

@Guice(modules = IntegrationTestModule.class)
public class MongoTransactionTest {

    private Datastore datastore;

    private Provider<Transaction> transactionProvider;

    @DataProvider
    public static Object[][] daoClasses() {
        return new Object[][] {
                new Object[] { IndexDao.class },
                new Object[] { UserDao.class },
                new Object[] { GoogleSignInUserDao.class },
                new Object[] { ProfileDao.class },
                new Object[] { FacebookUserDao.class },
                new Object[] { ShortLinkDao.class },
                new Object[] { ApplicationDao.class },
                new Object[] { ApplicationConfigurationDao.class },
                new Object[] { IosApplicationConfigurationDao.class },
                new Object[] { GooglePlayApplicationConfigurationDao.class },
                new Object[] { FacebookApplicationConfigurationDao.class },
                new Object[] { MatchmakingApplicationConfigurationDao.class },
                new Object[] { FirebaseApplicationConfigurationDao.class },
                new Object[] { FirebaseUserDao.class },
                new Object[] { MatchDao.class },
                new Object[] { FCMRegistrationDao.class },
                new Object[] { SessionDao.class },
                new Object[] { GoogleSignInSessionDao.class },
                new Object[] { LeaderboardDao.class },
                new Object[] { ScoreDao.class },
                new Object[] { RankDao.class },
                new Object[] { FriendDao.class },
                new Object[] { FacebookFriendDao.class },
                new Object[] { ItemDao.class },
                new Object[] { InventoryItemDao.class },
                new Object[] { MissionDao.class },
                new Object[] { ProgressDao.class },
                new Object[] { RewardIssuanceDao.class },
                new Object[] { AppleIapReceiptDao.class },
                new Object[] { GooglePlayIapReceiptDao.class },
                new Object[] { AppleSignInUserDao.class },
                new Object[] { AppleSignInSessionDao.class },
                new Object[] { FollowerDao.class },
                new Object[] { TokensWithExpirationDao.class },
                new Object[] { DeploymentDao.class },
                new Object[] { DatabaseHealthStatusDao.class },
                new Object[] { MetadataSpecDao.class },
                new Object[] { SaveDataDocumentDao.class },
                new Object[] { AuthSchemeDao.class },
                new Object[] { CustomAuthUserDao.class },
                new Object[] { DistinctInventoryItemDao.class },
                new Object[] { FormidiumInvestorDao.class },
                new Object[] { WalletDao.class },
                new Object[] { SmartContractDao.class },
                new Object[] { VaultDao.class },
                new Object[] { LargeObjectDao.class },
                new Object[] { ScheduleDao.class },
                new Object[] { ScheduleEventDao.class }
        };
    }

    @Test(dataProvider = "daoClasses")
    public void testTransaction(final Class<?> daoT) {
        try (final var txn = getTransactionProvider().get()) {
            txn.getDao(daoT);
        }
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
