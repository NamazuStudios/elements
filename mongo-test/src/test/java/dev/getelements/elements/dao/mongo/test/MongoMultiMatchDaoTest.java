package dev.getelements.elements.dao.mongo.test;

import dev.getelements.elements.rt.exception.DuplicateException;
import dev.getelements.elements.sdk.dao.ApplicationConfigurationDao;
import dev.getelements.elements.sdk.dao.MultiMatchDao;
import dev.getelements.elements.sdk.model.application.Application;
import dev.getelements.elements.sdk.model.application.MatchmakingApplicationConfiguration;
import dev.getelements.elements.sdk.model.match.MultiMatch;
import dev.getelements.elements.sdk.model.profile.Profile;
import jakarta.inject.Inject;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static dev.getelements.elements.sdk.model.match.MultiMatchStatus.OPEN;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

@Guice(modules = IntegrationTestModule.class)
public class MongoMultiMatchDaoTest {

    private static final int TEST_USER_COUNT = 100;

    @Inject
    private MultiMatchDao multiMatchDao;

    @Inject
    private UserTestFactory userTestFactory;

    @Inject
    private ProfileTestFactory profileTestFactory;

    @Inject
    private ApplicationTestFactory applicationTestFactory;

    @Inject
    private ApplicationConfigurationDao applicationConfigurationDao;

    private Application application;

    private MatchmakingApplicationConfiguration applicationConfiguration;

    private final List<Profile> profiles = new CopyOnWriteArrayList<>();

    private final List<MultiMatch> matches = new CopyOnWriteArrayList<>();

    @DataProvider
    public Object[][] allMatches() {
        return matches.stream()
                .map(m -> new Object[]{m})
                .toArray(Object[][]::new);
    }

    @DataProvider
    public Object[][] allMatchesAndProfiles() {
        return matches.stream()
                .flatMap(m -> profiles.stream().map(p -> new Object[] {m, p}))
                .toArray(Object[][]::new);
    }

    @DataProvider
    public Object[][] allMatchesAndSomeProfiles() {
        return matches.stream()
                .flatMap(m -> profiles.stream()
                        .limit(profiles.size() / 2)
                        .map(p -> new Object[] {m, p})
                )
                .toArray(Object[][]::new);
    }

    @BeforeClass
    public void setUpApplication() {
        application = applicationTestFactory.createAtomicApplication("Test Multi Match Application");
    }

    @BeforeClass(dependsOnMethods = "setupApplication")
    public void setupProfiles() {
        for (int i = 0; i < TEST_USER_COUNT; ++i) {
            final var user = userTestFactory.createTestUser();
            final var profile = profileTestFactory.makeMockProfile(user, application);
            profiles.add(profile);
        }
    }

    @BeforeClass(dependsOnMethods = "setUpApplication")
    public void setupApplicationConfiguration() {

        final var config = new MatchmakingApplicationConfiguration();
        config.setName("test_multi_match");
        config.setDescription("Test Multi Match");
        config.setParent(application);

        applicationConfiguration = applicationConfigurationDao.createApplicationConfiguration(
                application.getId(),
                config
        );

    }

    @Test(groups = "createMultiMatch", threadPoolSize = 10, invocationCount = 10)
    public void testCreateMultiMatch() {

        final var match = new MultiMatch();
        match.setStatus(OPEN);
        match.setConfiguration(applicationConfiguration);

        final var result = multiMatchDao.createMultiMatch(match);
        assertNotNull(result.getId());
        assertEquals(result.getStatus(), match.getStatus());
        assertEquals(result.getMetadata(), match.getMetadata());
        assertEquals(result.getConfiguration(), match.getConfiguration());
        matches.add(match);

    }

    @Test(
            groups = "addProfilesToMultiMatch",
            dependsOnGroups = "createMultiMatch",
            dataProvider = "allMatchesAndProfiles"
    )
    public void testAddProfileToMultiMatch(final MultiMatch match, final Profile profile) {
        multiMatchDao.addProfile(match.getId(), profile);
    }

    @Test(
            groups = "addProfilesToMultiMatch",
            dependsOnGroups = "createMultiMatch",
            dependsOnMethods = "testAddProfileToMultiMatch",
            dataProvider = "allMatchesAndProfiles",
            expectedExceptions = DuplicateException.class
    )
    public void testDoubleAddProfileToMultiMatch(final MultiMatch match, final Profile profile) {
        multiMatchDao.addProfile(match.getId(), profile);
    }
    
}
