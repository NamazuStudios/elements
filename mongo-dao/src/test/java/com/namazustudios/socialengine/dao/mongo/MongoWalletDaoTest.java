package com.namazustudios.socialengine.dao.mongo;

import com.namazustudios.socialengine.dao.WalletDao;
import com.namazustudios.socialengine.model.blockchain.BlockchainNetwork;
import com.namazustudios.socialengine.model.blockchain.wallet.Wallet;
import com.namazustudios.socialengine.model.blockchain.wallet.WalletIdentityPair;
import com.namazustudios.socialengine.model.user.User;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.IntStream;

import static com.namazustudios.socialengine.rt.util.Hex.Case.UPPER;
import static com.namazustudios.socialengine.rt.util.Hex.forNibble;
import static java.util.stream.Collectors.toList;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

@Guice(modules = IntegrationTestModule.class)
public class MongoWalletDaoTest {

    private static final int TEST_USER_COUNT = 10;

    private WalletDao underTest;

    private UserTestFactory userTestFactory;

    private User trudyUser;

    private List<User> regularUsers;

    private final Map<String, Wallet> wallets = new ConcurrentHashMap<>();

    @BeforeClass
    public void createTestUsers() {

        trudyUser = getUserTestFactory().createTestUser();

        regularUsers = IntStream.range(0, TEST_USER_COUNT)
                .mapToObj(i -> getUserTestFactory().createTestUser())
                .collect(toList());

    }

    @DataProvider
    public Object[][] regularUsers() {
        return regularUsers
                .stream()
                .map(u -> new Object[]{u})
                .toArray(Object[][]::new);
    }

    @DataProvider
    public Object[][] wallets() {
        return wallets
                .values()
                .stream()
                .map(u -> new Object[]{u})
                .toArray(Object[][]::new);
    }

    public String randomKey() {

        final var key = new char[256];
        final var random = ThreadLocalRandom.current();

        for (int i = 0; i < key.length; ++i) {
            key[i] = forNibble(random.nextInt(0xF), UPPER);
        }

        return new String(key);

    }

    @Test(dataProvider = "regularUsers")
    public void testCreateWallets(final User user) {
        for (var network : BlockchainNetwork.values()) {

            final var wallet = new Wallet();
            wallet.setDisplayName("Wallet for User " + user.getName());
            wallet.setUser(user);
            wallet.setEncryption(new HashMap<>());
            wallet.setNetworks(List.of(network));
            wallet.setProtocol(network.protocol());

            final var identity = new WalletIdentityPair();
            identity.setEncrypted(false);
            identity.setAddress(randomKey());
            identity.setPrivateKey(randomKey());
            wallet.setIdentities(List.of(identity));

            final var created = getUnderTest().createWallet(wallet);
            assertNotNull(created.getId());
            assertEquals(created.getDisplayName(), wallet.getDisplayName());
            assertEquals(created.getEncryption(), wallet.getEncryption());
            assertEquals(created.getNetworks(), wallet.getNetworks());
            assertEquals(created.getUser(), wallet.getUser());
            assertEquals(created.getProtocol(), wallet.getProtocol());

            wallets.put(created.getId(), created);

        }
    }

    @Test(dependsOnMethods = "testCreateWallets", dataProvider = "wallets")
    public void testUpdateWallet(final Wallet wallet) {
        final var update = new Wallet();
        update.setId(wallet.getId());
        update.setDefaultIdentity(wallet.getDefaultIdentity());
        update.setDisplayName(wallet.getDisplayName());
        update.setEncryption(wallet.getEncryption());
        update.setUser(wallet.getUser());
        update.setNetworks(wallet.getNetworks());
        update.setProtocol(wallet.getProtocol());

        final var identity = new WalletIdentityPair();
        identity.setEncrypted(false);
        identity.setAddress(randomKey());
        identity.setPrivateKey(randomKey());
        update.setIdentities(List.of(identity));

        final var updated = getUnderTest().updateWallet(update);
        assertEquals(updated, update);

    }

    public WalletDao getUnderTest() {
        return underTest;
    }

    @Inject
    public void setUnderTest(WalletDao underTest) {
        this.underTest = underTest;
    }

    public UserTestFactory getUserTestFactory() {
        return userTestFactory;
    }

    @Inject
    public void setUserTestFactory(UserTestFactory userTestFactory) {
        this.userTestFactory = userTestFactory;
    }

}
