package dev.getelements.elements.dao.mongo;

import dev.getelements.elements.dao.*;
import dev.getelements.elements.model.application.Application;
import dev.getelements.elements.model.profile.Profile;
import dev.getelements.elements.model.user.User;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Guice;

import javax.inject.Inject;
import java.util.*;
import java.util.function.Function;
import java.util.stream.IntStream;

import static java.util.Collections.shuffle;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.*;

@Guice(modules = IntegrationTestModule.class)
public class MongoRankDaoIntegrationTest {

    private static final int TEST_USER_COUNT = 3000;

    private RankDao rankDao;

    private ScoreDao scoreDao;

    private FriendDao friendDao;

    private FollowerDao followerDao;

    private ApplicationDao applicationDao;

    private UserTestFactory userTestFactory;

    private ProfileTestFactory profileTestFactory;

    private Application application;

    private List<User> allUsers;

    private Map<String, Profile> allProfiles;

    private Map<Profile, List<Profile>> followers;

    @BeforeClass
    public void setupUsers() {
        this.allUsers = IntStream.range(0, TEST_USER_COUNT)
                .mapToObj(i -> getUserTestFactory().createTestUser())
                .collect(toUnmodifiableList());
    }

    @BeforeClass
    public void setupApplication() {
        final var application = new Application();
        application.setName("mock");
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

        final var random = new Random();

        final var followers = new HashMap<String , List<Profile>>();
        final var allProfiles = new ArrayList<>(this.allProfiles.values());

        for (final var profile : this.allProfiles.values()) {

            final var indices = IntStream.range(0, allProfiles.size())
                    .boxed()
                    .collect(toList());

            shuffle(indices, random);

            followers.put(profile.getId(), indices.stream()
                    .map(allProfiles::get)
                    .filter(p -> !profile.getId().equals(p.getId()))
                    .limit(allProfiles.size() / 10)
                    .collect(toUnmodifiableList()));

            followers
                    .get(profile.getId())
                    .forEach(p -> getFollowerDao().createFollowerForProfile(p.getId(), profile.getId()));

        }

    }

    @BeforeClass(dependsOnMethods = "setupAllProfiles")
    public void setupMutualFollowers() {

    }

    public void createScores() {

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
