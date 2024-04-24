package dev.getelements.elements.dao.mongo.test;

import dev.getelements.elements.dao.*;
import dev.getelements.elements.dao.mongo.mission.MongoScheduleProgressDao;
import dev.getelements.elements.model.application.Application;
import dev.getelements.elements.model.goods.ItemCategory;
import dev.getelements.elements.model.mission.Mission;
import dev.getelements.elements.model.mission.Progress;
import dev.getelements.elements.model.mission.Schedule;
import dev.getelements.elements.model.mission.ScheduleEvent;
import dev.getelements.elements.model.profile.Profile;
import dev.getelements.elements.model.user.User;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.List;
import java.util.Objects;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toUnmodifiableList;
import static org.testng.Assert.*;

@Guice(modules = IntegrationTestModule.class)
public class MongoScheduleProgressDaoTest {

    private final int TEST_USER_COUNT = 5;

    private final int TEST_EVENT_COUNT = 5;

    private final int TEST_MISSION_COUNT = 5;

    private ProgressDao progressDao;

    private ScheduleDao scheduleDao;

    private ScheduleEventDao scheduleEventDao;

    private ItemTestFactory itemTestFactory;

    private MissionTestFactory missionTestFactory;

    private UserTestFactory userTestFactory;

    private ProfileTestFactory profileTestFactory;

    private ApplicationTestFactory applicationTestFactory;

    private Schedule schedule;

    private Application application;

    private List<User> users;

    private List<Profile> profiles;

    private List<Mission> missions;

    private List<ScheduleEvent> scheduleEvents;

    private Provider<Transaction> transactionProvider;

    @BeforeClass
    public void setupUsers() {
        users = IntStream.range(0, TEST_USER_COUNT)
                .mapToObj(i -> getUserTestFactory().createTestUser())
                .collect(toUnmodifiableList());
    }

    @BeforeClass
    public void setupApplication() {
        application = getApplicationTestFactory()
                .createMockApplication("Schedule Progress Dao.");
    }

    @BeforeClass(dependsOnMethods = {"setupUsers", "setupApplication"} )
    public void setupProfiles() {
        profiles = users.stream()
                .map(user -> getProfileTestFactory().makeMockProfile(user, application))
                .collect(toList());
    }

    @BeforeClass
    public void setupSchedule() {
        final var schedule = new Schedule();
        schedule.setName("schedule_progress_dao_integration_test");
        schedule.setDescription("Schedule Progress Dao Integration Test.");
        schedule.setDisplayName("Schedule Progress Dao Integration Test.");
        this.schedule = getScheduleDao().create(schedule);
    }

    @BeforeClass(dependsOnMethods = "setupSchedule")
    public void setupScheduleEvents() {

        final var item = getItemTestFactory().createTestItem(ItemCategory.FUNGIBLE, true);

        scheduleEvents = IntStream.range(0, TEST_EVENT_COUNT)
                .mapToObj(eventIdx -> {

                    final var event = new ScheduleEvent();

                    final var missions = IntStream.range(0, TEST_MISSION_COUNT)
                            .mapToObj(missionIdx -> getMissionTestFactory().createTestFiniteMission(
                                    "sp_" + missionIdx,
                                    item)
                            )
                            .collect(toList());

                    event.setSchedule(schedule);
                    event.setMissions(missions);

                    return getScheduleEventDao().createScheduleEvent(event);

                })
                .collect(toList());

    }

    @BeforeClass(dependsOnMethods = "setupScheduleEvents")
    public void buildMissions() {
        missions = scheduleEvents.stream()
                .flatMap(e -> e.getMissions().stream())
                .collect(toList());
    }

    @DataProvider
    public Object[][] profiles() {
        return profiles.stream()
                .map(o -> new Object[]{o})
                .toArray(Object[][]::new);
    }

    @DataProvider
    public Object[][] profilesAndMissions() {
        return profiles.stream()
                .flatMap(profile -> missions
                        .stream()
                        .map(mission -> new Object[]{profile, mission})
                )
                .toArray(Object[][]::new);
    }

    @DataProvider
    public Object[][] profilesAndScheduleEvents() {
        return profiles.stream()
                .flatMap(profile -> scheduleEvents
                        .stream()
                        .map(scheduleEvent -> new Object[]{profile, scheduleEvent})
                )
                .toArray(Object[][]::new);
    }

    @Test(dataProvider = "profilesAndMissions")
    public void testPreConditionNoProgress(final Profile profile, final Mission mission) {
        getProgressDao()
                .findProgressForProfileAndMission(profile, mission.getId())
                .ifPresent(p -> Assert.fail("Progress with id should not exist: " + p.getId()));
    }

    @Test(dependsOnMethods = "testPreConditionNoProgress", dataProvider = "profilesAndScheduleEvents")
    public void activateSingleEvent(final Profile profile, final ScheduleEvent scheduleEvent) {

        final var progresses = getTransactionProvider().get().performAndClose(txn -> {
            final var scheduleProgressDao = txn.getDao(ScheduleProgressDao.class);
            return scheduleProgressDao.assignProgressesForMissionsIn(
                    schedule.getId(),
                    profile.getId(),
                    List.of(scheduleEvent)
            );
        });

        progresses.forEach(progress -> getProgressDao().getProgress(progress.getId()));
        checkScheduleEvent(scheduleEvent, profile);

    }

    @Test(dependsOnMethods = "activateSingleEvent", dataProvider = "profilesAndScheduleEvents")
    public void activateSingleEventAgainDoesNotDuplicate(final Profile profile, final ScheduleEvent scheduleEvent) {
        // This can repeat the previous test just to ensure that the list does not grow indefinitely. I just wanted
        // this to be reported as a second test even though it is functionally identical.
        activateSingleEvent(profile, scheduleEvent);
    }

    @Test(dependsOnMethods = "activateSingleEventAgainDoesNotDuplicate", dataProvider = "profiles")
    public void activateAllScheduleEvents(final Profile profile) {

        final var progresses = getTransactionProvider().get().performAndClose(txn -> {
            final var scheduleEventDao = txn.getDao(ScheduleEventDao.class);
            final var scheduleProgressDao = txn.getDao(ScheduleProgressDao.class);
            final var scheduleEvents = scheduleEventDao.getAllScheduleEvents(schedule.getId());
            return scheduleProgressDao.assignProgressesForMissionsIn(
                    schedule.getId(),
                    profile.getId(),
                    scheduleEvents
            );
        });

        final var scheduleEvents = scheduleEventDao.getAllScheduleEvents(schedule.getId());
        scheduleEvents.forEach(scheduleEvent -> checkScheduleEvent(scheduleEvent, profile));
        progresses.forEach(progress -> getProgressDao().getProgress(progress.getId()));

    }

    private void checkScheduleEvent(final ScheduleEvent scheduleEvent, final Profile profile) {
        scheduleEvent.getMissions().forEach(mission -> {
            final var progress = getProgressDao().getProgressForProfileAndMission(profile, mission.getId());

            assertEquals(progress.getMission().getId(), mission.getId());
            assertNotNull(progress.getId());
            assertEquals(progress.getProfile(), profile);
            assertEquals(progress.getCurrentStep(), mission.getSteps().get(0));
            assertEquals(progress.getRemaining(), progress.getCurrentStep().getCount());
            assertEquals(progress.getRemaining(), mission.getSteps().get(0).getCount());
            Assert.assertTrue(progress.getRewardIssuances().isEmpty());

            assertNotNull(progress.getMission());
            assertEquals(progress.getMission().getId(), mission.getId());
            assertEquals(progress.getMission().getName(), mission.getName());
            assertEquals(progress.getMission().getDisplayName(), mission.getDisplayName());
            assertEquals(progress.getMission().getDescription(), mission.getDescription());
            assertEquals(progress.getMission().getFinalRepeatStep(), mission.getFinalRepeatStep());
            assertEquals(progress.getMission().getSteps(), mission.getSteps());
            assertEquals(progress.getMission().getFinalRepeatStep(), mission.getFinalRepeatStep());
            assertEquals(progress.getMission().getTags(), mission.getTags());

            assertEquals(progress.getSchedules().size(), 1);
            assertEquals(progress.getScheduleEvents().size(), 1);

            final var s = progress.getSchedules().get(0);
            final var se = progress.getScheduleEvents().get(0);

            assertEquals(s.getId(), schedule.getId());
            assertEquals(se.getId(), scheduleEvent.getId());

        });
    }

    @Test(dependsOnMethods = "activateAllScheduleEvents", dataProvider = "profilesAndScheduleEvents")
    public void deactivateSingleEvent(final Profile profile, final ScheduleEvent scheduleEvent) {

        final List<Progress> progresses = getTransactionProvider().get().performAndClose(txn -> {
            final var mongoScheduleProgressDao = txn.getDao(MongoScheduleProgressDao.class);

            final var toRetain = scheduleEvents
                    .stream()
                    .filter(sev -> !scheduleEvent.getId().equals(sev.getId()))
                    .collect(toList());

            return mongoScheduleProgressDao.unassignProgressesForMissionsNotIn(
                    schedule.getId(),
                    profile.getId(),
                    toRetain
            );

        });

        progresses.forEach(p -> {

            final var wasScheduleEventRemoved = p.getScheduleEvents()
                    .stream()
                    .noneMatch((e -> Objects.equals(e.getId(), scheduleEvent.getId())));

            assertTrue(wasScheduleEventRemoved, "Expected schedule event to be removed.");

            if (p.getSchedules().isEmpty() != p.getScheduleEvents().isEmpty()) {
                fail("Schedules and events must co-exist.");
            }

            final var fetched = getProgressDao().findProgress(p.getId());

            if (p.getSchedules().isEmpty()) {
                assertTrue(fetched.isEmpty(), "Expected Progress to be deleted.");
            } else {
                assertTrue(fetched.isPresent(), "Expected progress to be present.");
            }

            if (p.getScheduleEvents().isEmpty()) {
                assertTrue(fetched.isEmpty(), "Expected Progress to be deleted.");
            } else {
                assertTrue(fetched.isPresent(), "Expected progress to be present.");
            }

        });

    }

    @Test(dataProvider = "profilesAndMissions")
    public void deactivateSingleEvent(final Profile profile, final Mission mission) {
        testPreConditionNoProgress(profile, mission);
    }

    public ProgressDao getProgressDao() {
        return progressDao;
    }

    @Inject
    public void setProgressDao(ProgressDao progressDao) {
        this.progressDao = progressDao;
    }

    public ScheduleDao getScheduleDao() {
        return scheduleDao;
    }

    @Inject
    public void setScheduleDao(ScheduleDao scheduleDao) {
        this.scheduleDao = scheduleDao;
    }

    public ScheduleEventDao getScheduleEventDao() {
        return scheduleEventDao;
    }

    @Inject
    public void setScheduleEventDao(ScheduleEventDao scheduleEventDao) {
        this.scheduleEventDao = scheduleEventDao;
    }

    public MissionTestFactory getMissionTestFactory() {
        return missionTestFactory;
    }

    @Inject
    public void setMissionTestFactory(MissionTestFactory missionTestFactory) {
        this.missionTestFactory = missionTestFactory;
    }

    public ItemTestFactory getItemTestFactory() {
        return itemTestFactory;
    }

    @Inject
    public void setItemTestFactory(ItemTestFactory itemTestFactory) {
        this.itemTestFactory = itemTestFactory;
    }

    public Provider<Transaction> getTransactionProvider() {
        return transactionProvider;
    }

    @Inject
    public void setTransactionProvider(Provider<Transaction> transactionProvider) {
        this.transactionProvider = transactionProvider;
    }

    public ProfileTestFactory getProfileTestFactory() {
        return profileTestFactory;
    }

    @Inject
    public void setProfileTestFactory(ProfileTestFactory profileTestFactory) {
        this.profileTestFactory = profileTestFactory;
    }

    public UserTestFactory getUserTestFactory() {
        return userTestFactory;
    }

    @Inject
    public void setUserTestFactory(UserTestFactory userTestFactory) {
        this.userTestFactory = userTestFactory;
    }

    public ApplicationTestFactory getApplicationTestFactory() {
        return applicationTestFactory;
    }

    @Inject
    public void setApplicationTestFactory(ApplicationTestFactory applicationTestFactory) {
        this.applicationTestFactory = applicationTestFactory;
    }

}
