package com.namazustudios.socialengine.dao.mongo;

import com.namazustudios.socialengine.dao.*;
import com.namazustudios.socialengine.model.User;
import com.namazustudios.socialengine.model.application.Application;
import com.namazustudios.socialengine.model.goods.Item;
import com.namazustudios.socialengine.model.mission.*;
import com.namazustudios.socialengine.model.profile.Profile;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import javax.inject.Inject;
import java.util.LinkedList;
import java.util.Queue;
import java.util.stream.Stream;

import static com.namazustudios.socialengine.model.User.Level.USER;
import static com.namazustudios.socialengine.model.mission.PendingReward.State.PENDING;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static java.util.stream.Stream.of;
import static org.testng.Assert.*;

@Guice(modules = IntegrationTestModule.class)
public class MongoProgressDaoTest  {

    private UserDao userDao;

    private ProfileDao profileDao;

    private ApplicationDao applicationDao;

    private ItemDao itemDao;

    private MissionDao missionDao;

    private ProgressDao progressDao;

    private Application testApplication;

    private Item testItem;

    private User testUser;

    private Profile testProfile;

    private Mission testFiniteMission;

    private Mission testRepeatingMission;

    @BeforeClass
    public void setupTestMission() {
        testApplication = makeTestApplication();
        testUser = buildTestUser();
        testProfile = buildTestProfile();
        testItem = buildTestItem();
        testFiniteMission = buildTestFiniteMission();
        testRepeatingMission = buildTestRepeatingMission();
    }

    public Application makeTestApplication() {
        final Application application = new Application();
        application.setName("mock");
        application.setDescription("A mock application.");
        return getApplicationDao().createOrUpdateInactiveApplication(application);
    }

    private User buildTestUser() {
        final User testUser = new User();
        testUser.setName("testy.mctesterson.4");
        testUser.setEmail("testy.mctesterson.4@example.com");
        testUser.setLevel(USER);
        return getUserDao().createOrReactivateUser(testUser);
    }

    private Profile buildTestProfile() {
        final Profile profile =  new Profile();
        profile.setUser(testUser);
        profile.setApplication(testApplication);
        profile.setDisplayName(format("display-name-%s", testUser.getName()));
        profile.setImageUrl(format("http://example.com/%s.png", testUser.getName()));
        return getProfileDao().createOrReactivateProfile(profile);
    }

    private Item buildTestItem() {
        final Item testItem = new Item();
        testItem.setName("coin");
        testItem.setDisplayName("Magical Coins");
        testItem.setDescription("Magical Coins for Magic!");
        testItem.setTags(of("a").collect(toSet()));
        testItem.addMetadata("consumable", true);
        return getItemDao().createItem(testItem);
    }

    private Mission buildTestFiniteMission() {
        final Mission testMission = new Mission();
        testMission.setName("tally_me_banana_finite");
        testMission.setDisplayName("Tally me Banana");
        testMission.setDescription("Collect all the bananas");
        testMission.setTags(Stream.of("a", "b", "c").collect(toList()));
        testMission.setSteps(asList(
            testStep("Collect 5", "Collect 5 Bananas", 5),
            testStep("Collect 10", "Collect 10 Bananas", 10),
            testStep("Collect 15", "Collect 15 Bananas", 15)
        ));
        testMission.addMetadata("foo", "bar");
        return getMissionDao().createMission(testMission);
    }

    private Mission buildTestRepeatingMission() {
        final Mission testMission = new Mission();
        testMission.setName("tally_me_banana_repeating");
        testMission.setDisplayName("Tally me Banana");
        testMission.setDescription("Collect all the bananas");
        testMission.setTags(Stream.of("a", "b", "c").collect(toList()));
        testMission.setSteps(asList(
            testStep("Collect 5", "Collect 5 Bananas", 5),
            testStep("Collect 10", "Collect 10 Bananas", 10),
            testStep("Collect 15", "Collect 15 Bananas", 15)
        ));
        testMission.setFinalRepeatStep(testStep("Collect 5", "Collect 5 Bananas", 5));
        testMission.addMetadata("foo", "bar");
        return getMissionDao().createMission(testMission);
    }

    private Step testStep(final String displayName, final String description, final int count) {
        final Step step = new Step();
        step.setCount(count);
        step.setDisplayName(displayName);
        step.setDescription(description);
        step.setRewards(asList(testReward(count * 10)));
        step.addMetadata("foo", count * 100);
        return step;
    }

    private Reward testReward(final int quantity) {
        final Reward reward = new Reward();
        reward.setItem(testItem);
        reward.setQuantity(quantity);
        reward.addMetadata("bar", 100);
        return reward;
    }

    @DataProvider
    public Object[][] getMissions() {
        return Stream.of(testFiniteMission, testRepeatingMission)
            .map(m -> new Object[]{m})
            .toArray(Object[][]::new);
    }

    @Test(dataProvider = "getMissions")
    public void testCreateProgress(final Mission mission) {

        final Progress progress = new Progress();
        final Profile active = getProfileDao().getActiveProfile(testProfile.getId());
        progress.setProfile(active);

        final ProgressMissionInfo progressMissionInfo = new ProgressMissionInfo();
        progressMissionInfo.setId(mission.getId());
        progressMissionInfo.setName(mission.getName());
        progressMissionInfo.setDisplayName(mission.getDisplayName());
        progressMissionInfo.setDescription(mission.getDescription());
        progressMissionInfo.setSteps(mission.getSteps());
        progressMissionInfo.setFinalRepeatStep(mission.getFinalRepeatStep());
        progressMissionInfo.setMetadata(mission.getMetadata());
        progress.setMission(progressMissionInfo);

        final Progress created = getProgressDao().createProgress(progress);
        assertNotNull(created.getId());
        assertEquals(created.getProfile(), testProfile);
        assertEquals(created.getCurrentStep(), mission.getSteps().get(0));
        assertEquals(created.getRemaining(), created.getCurrentStep().getCount());
        assertEquals(created.getRemaining(), mission.getSteps().get(0).getCount());
        assertTrue(created.getPendingRewards().isEmpty());

        assertNotNull(created.getMission());
        assertEquals(created.getMission().getId(), mission.getId());
        assertEquals(created.getMission().getName(), mission.getName());
        assertEquals(created.getMission().getDisplayName(), mission.getDisplayName());
        assertEquals(created.getMission().getDescription(), mission.getDescription());
        assertEquals(created.getMission().getFinalRepeatStep(), mission.getFinalRepeatStep());
        assertEquals(created.getMission().getSteps(), mission.getSteps());
        assertEquals(created.getMission().getFinalRepeatStep(), mission.getFinalRepeatStep());

    }

    @DataProvider
    public Object[][] getFiniteProgresses() {
        return getProgressDao()
            .getProgressesForProfileAndMission(testProfile, testFiniteMission.getId())
            .stream()
            .map(p -> new Object[]{p})
            .toArray(Object[][]::new);
    }

    @Test(dataProvider = "getFiniteProgresses", dependsOnMethods = "testCreateProgress")
    public void testAdvancementThroughFiniteMission(Progress progress) {
        final Queue<Step> steps = new LinkedList<>(progress.getMission().getSteps());
        testAdvancement(steps, progress);
    }

    @DataProvider
    public Object[][] getRepeatingProgresses() {
        return getProgressDao()
            .getProgressesForProfileAndMission(testProfile, testRepeatingMission.getId())
            .stream()
            .map(p -> new Object[]{p})
            .toArray(Object[][]::new);
    }

    @Test(dataProvider = "getRepeatingProgresses", dependsOnMethods = "testCreateProgress")
    public void testAdvancementThroughRepeatingMission(final Progress progress) {

        final Queue<Step> steps = new LinkedList<>(progress.getMission().getSteps());
        assertNotNull(progress.getMission().getFinalRepeatStep());

        for (int i = 0; i < 10; ++i) {
            steps.add(progress.getMission().getFinalRepeatStep());
        }

        testAdvancement(steps, progress);

    }

    public void testAdvancement(final Queue<Step> steps, Progress progress) {

        int expectedRewards = 0;

        do {

            final Step step = steps.remove();

            progress = getProgressDao().advanceProgress(progress, progress.getRemaining() - 1);
            assertEquals(progress.getPendingRewards().size(), expectedRewards);

            progress = getProgressDao().advanceProgress(progress, 1);
            expectedRewards += step.getRewards().size();

            assertEquals(progress.getPendingRewards().size(), expectedRewards);
            if (!steps.isEmpty()) assertEquals(progress.getCurrentStep(), steps.peek());

            final PendingReward pendingReward = progress.getPendingRewards().get(expectedRewards - 1);
            assertNotNull(pendingReward.getId());
            assertEquals(pendingReward.getStep(), step);
            assertEquals(pendingReward.getReward(), step.getRewards().get(0));
            assertEquals(pendingReward.getState(), PENDING);

        } while (!steps.isEmpty());

    }

    public UserDao getUserDao() {
        return userDao;
    }

    @Inject
    public void setUserDao(UserDao userDao) {
        this.userDao = userDao;
    }

    public ProfileDao getProfileDao() {
        return profileDao;
    }

    @Inject
    public void setProfileDao(ProfileDao profileDao) {
        this.profileDao = profileDao;
    }

    public ItemDao getItemDao() {
        return itemDao;
    }

    @Inject
    public void setItemDao(ItemDao itemDao) {
        this.itemDao = itemDao;
    }

    public MissionDao getMissionDao() {
        return missionDao;
    }

    @Inject
    public void setMissionDao(MissionDao missionDao) {
        this.missionDao = missionDao;
    }

    public ProgressDao getProgressDao() {
        return progressDao;
    }

    @Inject
    public void setProgressDao(ProgressDao progressDao) {
        this.progressDao = progressDao;
    }

    public ApplicationDao getApplicationDao() {
        return applicationDao;
    }

    @Inject
    public void setApplicationDao(ApplicationDao applicationDao) {
        this.applicationDao = applicationDao;
    }

}
