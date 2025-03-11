package dev.getelements.elements.dao.mongo.test;


import dev.getelements.elements.sdk.dao.*;
import dev.getelements.elements.sdk.model.exception.NotFoundException;
import dev.getelements.elements.sdk.model.Pagination;
import dev.getelements.elements.sdk.model.goods.ItemCategory;
import dev.getelements.elements.sdk.model.user.User;
import dev.getelements.elements.sdk.model.application.Application;
import dev.getelements.elements.sdk.model.goods.Item;
import dev.getelements.elements.sdk.model.inventory.InventoryItem;
import dev.getelements.elements.sdk.model.mission.*;
import dev.getelements.elements.sdk.model.profile.Profile;
import dev.getelements.elements.sdk.model.reward.Reward;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import jakarta.inject.Inject;
import java.util.*;

import static dev.getelements.elements.sdk.model.reward.RewardIssuance.State.*;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
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

    private InventoryItemDao inventoryItemDao;

    private MissionDao missionDao;

    private ProgressDao progressDao;

    private RewardIssuanceDao rewardIssuanceDao;

    private Application testApplication;

    private Item testFiniteItem;

    private Item testRepeatItem;

    private User testUser;

    private Profile testProfile;

    private Mission testFiniteMission;

    private Mission testRepeatingMission;

    private UserTestFactory userTestFactory;

    @BeforeClass
    public void setupTestMission() {
        testApplication = makeTestApplication();
        testUser = getUserTestFactory().createTestUser();
        testProfile = buildTestProfile();
        testFiniteItem = buildFiniteTestItem();
        testRepeatItem = buildRepeatTestItem();
        testFiniteMission = buildTestFiniteMission();
        testRepeatingMission = buildTestRepeatingMission();
    }

    public Application makeTestApplication() {
        final Application application = new Application();
        application.setName("mock_progress");
        application.setDescription("A mock application for testing Progress.");
        return getApplicationDao().createOrUpdateInactiveApplication(application);
    }

    private Profile buildTestProfile() {
        final Profile profile =  new Profile();
        profile.setUser(testUser);
        profile.setApplication(testApplication);
        profile.setDisplayName(format("display-name-%s", testUser.getName()));
        profile.setImageUrl(format("http://example.com/%s.png", testUser.getName()));
        return getProfileDao().createOrReactivateProfile(profile);
    }

    private Item buildFiniteTestItem() {
        final Item testItem = new Item();
        testItem.setName("coin");
        testItem.setDisplayName("Magical Coins");
        testItem.setDescription("Magical Coins for Magic!");
        testItem.setTags(of("a").collect(toList()));
        testItem.addMetadata("consumable", true);
        testItem.setCategory(ItemCategory.FUNGIBLE);
        return getItemDao().createItem(testItem);
    }

    private Item buildRepeatTestItem() {
        final Item testItem = new Item();
        testItem.setName("potion");
        testItem.setDisplayName("Magical Potions");
        testItem.setDescription("Magical Coins for Magic!");
        testItem.setTags(of("repeat").collect(toList()));
        testItem.addMetadata("consumable", true);
        testItem.setCategory(ItemCategory.FUNGIBLE);
        return getItemDao().createItem(testItem);
    }

    private Mission buildTestFiniteMission() {
        final Mission testMission = new Mission();
        testMission.setName("tally_me_banana_finite");
        testMission.setDisplayName("Tally me Banana");
        testMission.setDescription("Collect all the bananas");
        testMission.setTags(of("finite").collect(toList()));
        testMission.setSteps(asList(
            testStep("Collect 5", "Collect 5 Bananas", 5, testFiniteItem),
            testStep("Collect 10", "Collect 10 Bananas", 10, testFiniteItem),
            testStep("Collect 15", "Collect 15 Bananas", 15, testFiniteItem)
        ));
        testMission.addMetadata("foo", "bar");
        return getMissionDao().createMission(testMission);
    }

    private Mission buildTestRepeatingMission() {
        final Mission testMission = new Mission();
        testMission.setName("tally_me_banana_repeating");
        testMission.setDisplayName("Tally me Banana");
        testMission.setDescription("Collect all the bananas");
        testMission.setTags(of("repeating").collect(toList()));
        testMission.setSteps(asList(
            testStep("Collect 5", "Collect 5 Bananas", 5, testRepeatItem),
            testStep("Collect 10", "Collect 10 Bananas", 10, testRepeatItem),
            testStep("Collect 15", "Collect 15 Bananas", 15, testRepeatItem)
        ));
        testMission.setFinalRepeatStep(testStep("Collect 5", "Collect 5 Bananas", 5, testRepeatItem));
        testMission.addMetadata("foo", "bar");
        return getMissionDao().createMission(testMission);
    }

    private Step testStep(final String displayName, final String description, final int count, final Item item) {
        final Step step = new Step();
        step.setCount(count);
        step.setDisplayName(displayName);
        step.setDescription(description);
        step.setRewards(asList(testReward(count * 10, item)));
        step.addMetadata("foo", count * 100);
        return step;
    }

    private Reward testReward(final int quantity, final Item item) {
        final Reward reward = new Reward();
        reward.setItem(item);
        reward.setQuantity(quantity);
        reward.addMetadata("bar", 100);
        return reward;
    }

    @DataProvider
    public Object[][] getMissions() {
        return of(testFiniteMission, testRepeatingMission)
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
        progressMissionInfo.setTags(mission.getTags());
        progress.setMission(progressMissionInfo);

        final Progress created = getProgressDao().createOrGetExistingProgress(progress);
        assertNotNull(created.getId());
        assertEquals(created.getProfile(), testProfile);
        assertEquals(created.getCurrentStep(), mission.getSteps().get(0));
        assertEquals(created.getRemaining(), created.getCurrentStep().getCount());
        assertEquals(created.getRemaining(), mission.getSteps().get(0).getCount());
        assertTrue(created.getRewardIssuances().isEmpty());

        assertNotNull(created.getMission());
        assertEquals(created.getMission().getId(), mission.getId());
        assertEquals(created.getMission().getName(), mission.getName());
        assertEquals(created.getMission().getDisplayName(), mission.getDisplayName());
        assertEquals(created.getMission().getDescription(), mission.getDescription());
        assertEquals(created.getMission().getFinalRepeatStep(), mission.getFinalRepeatStep());
        assertEquals(created.getMission().getSteps(), mission.getSteps());
        assertEquals(created.getMission().getFinalRepeatStep(), mission.getFinalRepeatStep());
        assertEquals(created.getMission().getTags(), mission.getTags());

        progress.setId(null);
        progress.setCurrentStep(null);
        progress.setRemaining(null);

        final Progress recreated = getProgressDao().createOrGetExistingProgress(progress);
        assertNotNull(recreated.getMission());
        assertEquals(recreated.getMission().getId(), mission.getId());
        assertEquals(recreated.getMission().getName(), mission.getName());
        assertEquals(recreated.getMission().getDisplayName(), mission.getDisplayName());
        assertEquals(recreated.getMission().getDescription(), mission.getDescription());
        assertEquals(recreated.getMission().getFinalRepeatStep(), mission.getFinalRepeatStep());
        assertEquals(recreated.getMission().getSteps(), mission.getSteps());
        assertEquals(recreated.getMission().getFinalRepeatStep(), mission.getFinalRepeatStep());
        assertEquals(recreated.getMission().getTags(), mission.getTags());

    }

    @Test(dependsOnMethods = "testCreateProgress")
    public void testGetProgressByMissionTags() {
        final Pagination<Progress> finiteProgressPagination = getProgressDao()
            .getProgresses(testProfile, 0, 20, of("finite").collect(toList()));

        final Pagination<Progress> repeatingProgressPagination = getProgressDao()
            .getProgresses(testProfile, 0, 20, of("repeating").collect(toList()));

        assertEquals(finiteProgressPagination.getObjects().size(), 1);
        assertEquals(repeatingProgressPagination.getObjects().size(), 1);
        assertEquals(finiteProgressPagination.getObjects().get(0).getMission().getId(), testFiniteMission.getId());
        assertEquals(repeatingProgressPagination.getObjects().get(0).getMission().getId(), testRepeatingMission.getId());

    }

    @DataProvider
    public Object[][] getFiniteProgresses() {
        final Progress progress = getProgressDao().getProgressForProfileAndMission(testProfile, testFiniteMission.getId());
        return new Object[][] {new Object[] {progress}};
    }

    @Test(dataProvider = "getFiniteProgresses", dependsOnMethods = "testGetProgressByMissionTags")
    public void testAdvancementThroughFiniteMission(Progress progress) {
        final Queue<Step> steps = new LinkedList<>(progress.getMission().getSteps());
        testAdvancement(steps, progress);
    }

    @DataProvider
    public Object[][] getRepeatingProgresses() {
        final Progress progress = getProgressDao().getProgressForProfileAndMission(testProfile, testRepeatingMission.getId());
        return new Object[][] {new Object[] {progress}};
    }

    @Test(dataProvider = "getRepeatingProgresses", dependsOnMethods = "testGetProgressByMissionTags")
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

            while (progress.getRemaining() > 1) {
                progress = getProgressDao().advanceProgress(progress, 1);
                if(progress.getRewardIssuances() == null){
                    progress.setRewardIssuances(emptyList());
                }
                assertEquals(progress.getRewardIssuances().size(), expectedRewards);
            }

            progress = getProgressDao().advanceProgress(progress, 1);

            if (progress.getCurrentStep() == null) {
                assertNull(progress.getCurrentStep());
            } else {
                assertEquals(progress.getCurrentStep().getCount(), progress.getRemaining());
            }

            for (final Reward reward : step.getRewards()) {
                // there may be more than one matching reward, e.g. if the final repeat step induces multiple issuances
                final Set<String> matchingRewardIssuanceContexts = progress
                        .getRewardIssuances()
                        .stream()
                        .filter(ri -> Objects.equals(ri.getItem(), reward.getItem()))
                        .filter(ri -> Objects.equals(ri.getItemQuantity(), reward.getQuantity()))
                        .filter(ri -> Objects.equals(ri.getState(), ISSUED))
                        .map(ri -> ri.getContext())
                        .collect(toSet());

                // so just make sure we have at least one matching issuance
                assertTrue(matchingRewardIssuanceContexts.size() > 0);
            }

            expectedRewards += step.getRewards().size();

            if (!steps.isEmpty()) assertEquals(progress.getCurrentStep(), steps.peek());

        } while (!steps.isEmpty());

        assertEquals(progress.getRewardIssuances().size(), expectedRewards);
    }

    @Test(dependsOnMethods = {"testAdvancementThroughFiniteMission", "testAdvancementThroughRepeatingMission"})
    public void testRedeem() {

        final Pagination<Progress> progressPagination = getProgressDao().getProgresses(testProfile, 0, 20, emptyList());

        final List<Progress> progresses = progressPagination.getObjects();

        final List<InventoryItem> inventoryItemList = progressPagination.getObjects()
            .stream()
            .flatMap(progress -> progress.getRewardIssuances().stream())
            .map(ri -> getRewardIssuanceDao().redeem(ri))
            .collect(toList());

        progressPagination.getObjects()
            .stream()
            .flatMap(progress -> progress.getRewardIssuances().stream())
            .forEach(ri -> {

                try {
                    getRewardIssuanceDao().delete(ri.getId());
                } catch (NotFoundException nfe) {
                    // pass
                }

                try {
                    getRewardIssuanceDao().getRewardIssuance(ri.getId());
                } catch (NotFoundException nfe) {
                    return;
                }

                fail("not deleted");

            });

        assertTrue(inventoryItemList.size() > 0);
        inventoryItemList.stream().forEach(ii -> getInventoryItemDao().getInventoryItem(ii.getId()));
        progressPagination.getObjects().stream().forEach(ri -> getProgressDao().getProgress(ri.getId()));
        progressPagination.forEach(progress -> getProgressDao().deleteProgress(progress.getId()));

    }

    @Test(dependsOnMethods = "testRedeem")
    public void testFiniteOverkill() {

        final Progress progress = new Progress();
        final Profile active = getProfileDao().getActiveProfile(testProfile.getId());
        progress.setProfile(active);

        final ProgressMissionInfo progressMissionInfo = new ProgressMissionInfo();
        progressMissionInfo.setId(testFiniteMission.getId());
        progressMissionInfo.setName(testFiniteMission.getName());
        progressMissionInfo.setDisplayName(testFiniteMission.getDisplayName());
        progressMissionInfo.setDescription(testFiniteMission.getDescription());
        progressMissionInfo.setSteps(testFiniteMission.getSteps());
        progressMissionInfo.setFinalRepeatStep(testFiniteMission.getFinalRepeatStep());
        progressMissionInfo.setMetadata(testFiniteMission.getMetadata());
        progressMissionInfo.setTags(testFiniteMission.getTags());
        progress.setMission(progressMissionInfo);

        final Progress created = getProgressDao().createOrGetExistingProgress(progress);
        testOverkill(created);

    }

    @Test(dependsOnMethods = "testRedeem")
    public void testRepeatingOverkill() {

        final Progress progress = new Progress();
        final Profile active = getProfileDao().getActiveProfile(testProfile.getId());
        progress.setProfile(active);

        final ProgressMissionInfo progressMissionInfo = new ProgressMissionInfo();
        progressMissionInfo.setId(testRepeatingMission.getId());
        progressMissionInfo.setName(testRepeatingMission.getName());
        progressMissionInfo.setDisplayName(testRepeatingMission.getDisplayName());
        progressMissionInfo.setDescription(testRepeatingMission.getDescription());
        progressMissionInfo.setSteps(testRepeatingMission.getSteps());
        progressMissionInfo.setFinalRepeatStep(testRepeatingMission.getFinalRepeatStep());
        progressMissionInfo.setMetadata(testRepeatingMission.getMetadata());
        progressMissionInfo.setTags(testRepeatingMission.getTags());
        progress.setMission(progressMissionInfo);

        final Progress created = getProgressDao().createOrGetExistingProgress(progress);
        testOverkill(created);

    }

    private void testOverkill(Progress progress) {
        progress = getProgressDao().advanceProgress(progress, 7);
        assertNotNull(progress);
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

    public InventoryItemDao getInventoryItemDao() {
        return inventoryItemDao;
    }

    @Inject
    public void setInventoryItemDao(InventoryItemDao inventoryItemDao) {
        this.inventoryItemDao = inventoryItemDao;
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

    public RewardIssuanceDao getRewardIssuanceDao() {
        return rewardIssuanceDao;
    }

    @Inject
    public void setRewardIssuanceDao(RewardIssuanceDao rewardIssuanceDao) {
        this.rewardIssuanceDao = rewardIssuanceDao;
    }

    public UserTestFactory getUserTestFactory() {
        return userTestFactory;
    }

    @Inject
    public void setUserTestFactory(UserTestFactory userTestFactory) {
        this.userTestFactory = userTestFactory;
    }
}
