package dev.getelements.elements.dao.mongo;

import dev.getelements.elements.dao.*;
import dev.getelements.elements.model.application.Application;
import dev.getelements.elements.model.leaderboard.Leaderboard;
import dev.getelements.elements.model.leaderboard.Rank;
import dev.getelements.elements.model.leaderboard.Score;
import dev.getelements.elements.model.profile.Profile;
import dev.getelements.elements.model.user.User;
import dev.getelements.elements.util.PaginationWalker;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import static dev.getelements.elements.model.leaderboard.Leaderboard.ScoreStrategyType.OVERWRITE_IF_GREATER;
import static dev.getelements.elements.model.leaderboard.Leaderboard.TimeStrategyType.ALL_TIME;
import static java.lang.Math.min;
import static java.util.List.copyOf;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.*;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

@Guice(modules = IntegrationTestModule.class)
public class MongoRankDaoIntegrationTest {

    private static final int TEST_USER_COUNT = 3000;

    private static final int TEST_BATCH_SIZE = TEST_USER_COUNT / 10;

    private static final int TEST_MUTUAL_FOLLOWER_COUNT = TEST_BATCH_SIZE / 2;

    private static final String LEADERBOARD_NAME = "follower_integration_test";

    private RankDao rankDao;

    private ScoreDao scoreDao;

    private FriendDao friendDao;

    private FollowerDao followerDao;

    private ApplicationDao applicationDao;

    private LeaderboardDao leaderboardDao;

    private UserTestFactory userTestFactory;

    private ProfileTestFactory profileTestFactory;

    private Application application;

    private List<User> allUsers;

    private Map<String, Profile> allProfiles;

    private Map<String, List<Profile>> followers;

    private Map<String, List<Profile>> mutualFollowers;

    @BeforeClass
    public void setupUsers() {
        this.allUsers = IntStream.range(0, TEST_USER_COUNT)
                .mapToObj(i -> getUserTestFactory().createTestUser())
                .collect(toUnmodifiableList());
    }

    @BeforeClass
    public void setupApplication() {
        final var application = new Application();
        application.setName("rank_dao_integration_test");
        application.setDescription("A mock application.");
        this.application = getApplicationDao().createOrUpdateInactiveApplication(application);
    }

    @BeforeClass(dependsOnMethods = {"setupUsers", "setupApplication"})
    public void setupAllProfiles() {
        this.allProfiles = allUsers
                .stream()
                .map(user -> getProfileTestFactory().makeMockProfile(user, application))
                .collect(toUnmodifiableMap(Profile::getId, identity()));
    }

    @BeforeClass(dependsOnMethods = "setupAllProfiles")
    public void setupFollowers() {

        final var followers = new HashMap<String , List<Profile>>();
        final var mutualFollowers = new HashMap<String , List<Profile>>();
        final var allProfiles = new LinkedList<>(this.allProfiles.values());

        while (!allProfiles.isEmpty()) {

            final var upper = min(allProfiles.size(), TEST_BATCH_SIZE);
            final var batch = allProfiles.subList(0, upper);

            final var profile = batch.remove(0);
            batch.forEach(follower -> getFollowerDao().createFollowerForProfile(follower.getId(), profile.getId()));

            final var mutualBatch = batch.subList(0, TEST_MUTUAL_FOLLOWER_COUNT);
            mutualBatch.forEach(follower -> getFollowerDao().createFollowerForProfile(profile.getId(), follower.getId()));

            followers.put(profile.getId(), copyOf(batch));
            mutualFollowers.put(profile.getId(), copyOf(mutualBatch));
            batch.clear();

        }

        this.followers = followers;
        this.mutualFollowers = mutualFollowers;

    }

    @BeforeClass
    public void createLeaderboard() {
        final var leaderboard = new Leaderboard();
        leaderboard.setName(LEADERBOARD_NAME);
        leaderboard.setTitle("Test Followers");
        leaderboard.setScoreUnits("Points");
        leaderboard.setScoreStrategyType(OVERWRITE_IF_GREATER);
        leaderboard.setTimeStrategyType(ALL_TIME);
        getLeaderboardDao().createLeaderboard(leaderboard);
    }

    @BeforeClass(dependsOnMethods = {"setupAllProfiles", "createLeaderboard"})
    public void createScores() {

        double value = 0;

        for (var profile : allProfiles.values()) {
            var score = new Score();
            score.setProfile(profile);
            score.setPointValue(value += 10);
            getScoreDao().createOrUpdateScore("follower_integration_test", score);
        }

    }

    @DataProvider
    public Object[][] profilesWithMutualFollowers() {
        return allProfiles
                .entrySet()
                .stream()
                .filter(e -> mutualFollowers.containsKey(e.getKey()))
                .map(e -> new Object[]{e.getKey(), e.getValue()})
                .toArray(Object[][]::new);
    }


    @Test(dataProvider = "profilesWithMutualFollowers")
    public void testGetRanksForMutualFollowers(final String profileId, final Profile profile) {

        final var ranks = new PaginationWalker().toList(((offset, count) -> getRankDao()
                .getRanksForMutualFollowers(
                        LEADERBOARD_NAME,
                        profileId,
                        offset, count,
                        0
                )));

        checkMutualPostConditions(ranks, profileId);

    }

    @Test(dataProvider = "profilesWithMutualFollowers")
    public void testGetRanksForMutualFollowersRelative(final String profileId, final Profile profile) {

        final var ranks = new PaginationWalker().toList(((offset, count) -> getRankDao()
                .getRanksForMutualFollowersRelative(
                    LEADERBOARD_NAME,
                    profileId,
                    offset, count,
                    0
        )));

        checkMutualPostConditions(ranks, profileId);

    }

    private void checkMutualPostConditions(final List<Rank> ranks, final String profileId) {

        final var expected = mutualFollowers.get(profileId)
                .stream()
                .map(Profile::getId)
                .collect(toSet());

        final var actual = ranks.stream()
                .map(r -> r.getScore().getProfile().getId())
                .collect(toSet());

        assertTrue(actual.contains(profileId), "Results do not contain the profile.");
        assertTrue(actual.containsAll(expected), "Results do not contain all mutual followers.");

        actual.remove(profileId);
        actual.removeAll(expected);

        assertEquals(actual.size(), 0, "Expected that the result contains only mutual followers.");

    }

    public RankDao getRankDao() {
        return rankDao;
    }

    @Inject
    public void setRankDao(RankDao rankDao) {
        this.rankDao = rankDao;
    }

    public ScoreDao getScoreDao() {
        return scoreDao;
    }

    @Inject
    public void setScoreDao(ScoreDao scoreDao) {
        this.scoreDao = scoreDao;
    }

    public FriendDao getFriendDao() {
        return friendDao;
    }

    @Inject
    public void setFriendDao(FriendDao friendDao) {
        this.friendDao = friendDao;
    }

    public FollowerDao getFollowerDao() {
        return followerDao;
    }

    @Inject
    public void setFollowerDao(FollowerDao followerDao) {
        this.followerDao = followerDao;
    }

    public ApplicationDao getApplicationDao() {
        return applicationDao;
    }

    @Inject
    public void setApplicationDao(ApplicationDao applicationDao) {
        this.applicationDao = applicationDao;
    }

    public LeaderboardDao getLeaderboardDao() {
        return leaderboardDao;
    }

    @Inject
    public void setLeaderboardDao(LeaderboardDao leaderboardDao) {
        this.leaderboardDao = leaderboardDao;
    }

    public UserTestFactory getUserTestFactory() {
        return userTestFactory;
    }

    @Inject
    public void setUserTestFactory(UserTestFactory userTestFactory) {
        this.userTestFactory = userTestFactory;
    }

    public ProfileTestFactory getProfileTestFactory() {
        return profileTestFactory;
    }

    @Inject
    public void setProfileTestFactory(ProfileTestFactory profileTestFactory) {
        this.profileTestFactory = profileTestFactory;
    }

}
