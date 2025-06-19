package dev.getelements.elements.dao.mongo.test;

import dev.getelements.elements.rt.exception.DuplicateProfileException;
import dev.getelements.elements.sdk.ElementRegistry;
import dev.getelements.elements.sdk.dao.ApplicationConfigurationDao;
import dev.getelements.elements.sdk.dao.MultiMatchDao;
import dev.getelements.elements.sdk.model.application.Application;
import dev.getelements.elements.sdk.model.application.MatchmakingApplicationConfiguration;
import dev.getelements.elements.sdk.model.exception.profile.ProfileNotFoundException;
import dev.getelements.elements.sdk.model.match.MultiMatch;
import dev.getelements.elements.sdk.model.profile.Profile;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import static dev.getelements.elements.sdk.ElementRegistry.ROOT;
import static dev.getelements.elements.sdk.dao.MultiMatchDao.*;
import static dev.getelements.elements.sdk.model.match.MultiMatchStatus.IN_PROGRESS;
import static dev.getelements.elements.sdk.model.match.MultiMatchStatus.OPEN;
import static org.testng.Assert.*;

@Guice(modules = IntegrationTestModule.class)
public class MongoMultiMatchDaoTest {

    private static final int TEST_USER_COUNT = 100;

    private static final int TEST_MATCH_COUNT = 15;

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

    @Inject
    @Named(ROOT)
    private ElementRegistry elementRegistry;

    private Application application;

    private MatchmakingApplicationConfiguration applicationConfiguration;

    private final List<Profile> profiles = new CopyOnWriteArrayList<>();

    private final List<MultiMatch> matches = new CopyOnWriteArrayList<>();

    private final Map<String, Set<Profile>> profilesByMatch = new ConcurrentHashMap<>();

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
    public Object[][] allMatchesAndLowerProfiles() {

        final var count = profiles.size() / 2;

        return matches.stream()
                .flatMap(m -> profiles.stream()
                        .limit(count)
                        .map(p -> new Object[] {m, p})
                )
                .toArray(Object[][]::new);

    }

    @DataProvider
    public Object[][] allMatchesAndUpperProfiles() {

        final var count = profiles.size() / 2;

        return matches.stream()
                .flatMap(m -> profiles.stream()
                        .skip(count)
                        .map(p -> new Object[] {m, p})
                )
                .toArray(Object[][]::new);

    }

    @BeforeClass
    public void setUpApplication() {
        application = applicationTestFactory.createMockApplication("Test Multi Match Application");
    }

    @BeforeClass(dependsOnMethods = "setUpApplication")
    public void setupProfiles() {
        for (int i = 0; i < TEST_USER_COUNT; ++i) {
            final var user = userTestFactory.createTestUser();
            final var profile = profileTestFactory.makeMockProfile(user, application);
            profiles.add(profile);
        }
    }

    @BeforeClass
    public void setupEventHandlers() {
        elementRegistry.onEvent(ev -> {
            switch (ev.getEventName()) {
                case MULTI_MATCH_CREATED -> onCreate(ev.getEventArgument(0));
                case MULTI_MATCH_UPDATED -> onUpdate(ev.getEventArgument(0));
                case MULTI_MATCH_ADD_PROFILE -> onAddProfile(
                        ev.getEventArgument(0, MultiMatch.class),
                        ev.getEventArgument(1, Profile.class)
                );
                case MULTI_MATCH_REMOVE_PROFILE -> onRemoveProfile(
                        ev.getEventArgument(0, MultiMatch.class),
                        ev.getEventArgument(1, Profile.class)
                );
                case MULTI_MATCH_DELETED -> onDelete(ev.getEventArgument(0));
            }
        });
    }

    private void onCreate(final MultiMatch match) {
        matches.add(match);
    }

    private void onUpdate(final MultiMatch match) {
        final var removed = matches.removeIf(m -> m.getId().equals(match.getId()));
        assertTrue(removed, "Match not found for update: " + match.getId());
        matches.add(match);
    }

    private void onAddProfile(final MultiMatch match, final Profile profile) {
        profilesByMatch.compute(match.getId(), (id, profiles) -> {

            if (profiles == null) {
                profiles = new HashSet<>();
                profiles.add(profile);
            } else {
                profiles = new HashSet<>(profiles);
                assertTrue(profiles.add(profile), "Profile added twice.");
            }

            return profiles;

        });
    }

    private void onRemoveProfile(final MultiMatch match, final Profile profile) {
        profilesByMatch.compute(match.getId(), (id, profiles) -> {

            if (profiles == null) {
                profiles = new HashSet<>();
                profiles.add(profile);
            } else {
                profiles = new HashSet<>(profiles);
                assertTrue(profiles.remove(profile), "Profile not previously added.");
            }

            return profiles.isEmpty() ? null : profiles;

        });
    }

    private void onDelete(final MultiMatch match) {
        profilesByMatch.remove(match.getId());
        assertTrue(matches.removeIf(m -> m.getId().equals(match.getId())), "Expected match to exist.");
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

    @Test(groups = "createMultiMatch", threadPoolSize = 10, invocationCount = TEST_MATCH_COUNT)
    public void testCreateMultiMatch() {

        final var match = new MultiMatch();
        match.setStatus(OPEN);
        match.setConfiguration(applicationConfiguration);

        final var result = multiMatchDao.createMultiMatch(match);
        assertNotNull(result.getId());
        assertEquals(result.getStatus(), match.getStatus());
        assertEquals(result.getMetadata(), match.getMetadata());
        assertEquals(result.getConfiguration(), match.getConfiguration());

    }

    @Test(groups = "createMultiMatch",
          dependsOnMethods = "testCreateMultiMatch")
    public void testAlLCreatedProperly() {
        assertEquals(
                matches.size(),
                TEST_MATCH_COUNT,
                "Expected " + TEST_MATCH_COUNT + " matches to be created."
        );
    }

    @Test(
            threadPoolSize = 10,
            groups = "addProfilesToMultiMatch",
            dependsOnGroups = "createMultiMatch",
            dataProvider = "allMatchesAndProfiles"
    )
    public void testAddProfileToMultiMatch(final MultiMatch match, final Profile profile) {
        multiMatchDao.addProfile(match.getId(), profile);
    }

    @Test(
            threadPoolSize = 10,
            groups = "addProfilesToMultiMatch",
            dependsOnGroups = "createMultiMatch",
            dependsOnMethods = "testAddProfileToMultiMatch",
            dataProvider = "allMatchesAndProfiles",
            expectedExceptions = DuplicateProfileException.class
    )
    public void testDoubleAddProfileToMultiMatch(final MultiMatch match, final Profile profile) {
        multiMatchDao.addProfile(match.getId(), profile);
    }

    @Test(
            threadPoolSize = 10,
            groups = "addProfilesToMultiMatch",
            dependsOnGroups = "createMultiMatch",
            dependsOnMethods = "testDoubleAddProfileToMultiMatch",
            dataProvider = "allMatches"
    )
    public void testProfilesWereAddedToMultiMatch(final MultiMatch match) {
        final var actual = multiMatchDao.getProfiles(match.getId());
        final var expected = profilesByMatch.get(match.getId());
        assertEquals(actual.size(), expected.size(), "Profile count mismatch for match: " + match.getId());
        assertTrue(expected.containsAll(actual), "Expected profiles not found in match: " + match.getId());
    }

    @Test(
            threadPoolSize = 10,
            groups = "removeProfilesFromMultiMatch",
            dependsOnGroups = "addProfilesToMultiMatch",
            dataProvider = "allMatchesAndLowerProfiles"
    )
    public void testRemoveProfileFromMultiMatch(final MultiMatch match, final Profile profile) {
        multiMatchDao.removeProfile(match.getId(), profile);
    }

    @Test(
            threadPoolSize = 10,
            groups = "removeProfilesFromMultiMatch",
            dependsOnGroups = "addProfilesToMultiMatch",
            dependsOnMethods = "testRemoveProfileFromMultiMatch",
            dataProvider = "allMatchesAndLowerProfiles",
            expectedExceptions = ProfileNotFoundException.class
    )
    public void testDoubleRemoveProfileFromMultiMatch(final MultiMatch match, final Profile profile) {
        multiMatchDao.removeProfile(match.getId(), profile);
    }

    @Test(
            threadPoolSize = 10,
            groups = "removeProfilesFromMultiMatch",
            dependsOnGroups = "addProfilesToMultiMatch",
            dependsOnMethods = "testDoubleRemoveProfileFromMultiMatch",
            dataProvider = "allMatches"
    )
    public void testProfilesWereRemovedFromMultiMatch(final MultiMatch match) {
        final var actual = multiMatchDao.getProfiles(match.getId());
        final var expected = profilesByMatch.get(match.getId());
        assertEquals(actual.size(), expected.size(), "Profile count mismatch for match: " + match.getId());
        assertTrue(expected.containsAll(actual), "Expected profiles not found in match: " + match.getId());
    }

    @Test(
            threadPoolSize = 10,
            groups = "updateMultiMatch",
            dependsOnGroups = "removeProfilesFromMultiMatch",
            dataProvider = "allMatches"
    )
    public void testUpdateMultiMatch(final MultiMatch match) {
        match.setStatus(IN_PROGRESS);
        multiMatchDao.updateMultiMatch(match);
    }

    @Test(
            threadPoolSize = 10,
            groups = "updateMultiMatch",
            dependsOnGroups = "removeProfilesFromMultiMatch",
            dependsOnMethods = "testUpdateMultiMatch"
    )
    public void testMultiMatchesUpdated() {
        matches.forEach(m -> assertEquals(m.getStatus(), IN_PROGRESS));
    }

    @Test(
            threadPoolSize = 10,
            groups = "getMultiMatches",
            dependsOnGroups = "updateMultiMatch",
            dataProvider = "allMatches"
    )
    public void testFindMultiMatch(final MultiMatch match) {
        final var actual = multiMatchDao.findMultiMatch(match.getId()).get();
        assertEquals(actual, match, "Match not found: " + match.getId());
    }

    @Test(
            threadPoolSize = 10,
            groups = "getMultiMatches",
            dependsOnGroups = "updateMultiMatch",
            dataProvider = "allMatches"
    )
    public void testGetMultiMatch(final MultiMatch match) {
        final var actual = multiMatchDao.getMultiMatch(match.getId());
        assertEquals(actual, match, "Match not found: " + match.getId());
    }

}
