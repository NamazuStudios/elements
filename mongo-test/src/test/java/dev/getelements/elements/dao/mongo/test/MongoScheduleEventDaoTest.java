package dev.getelements.elements.dao.mongo.test;

import dev.getelements.elements.sdk.dao.ScheduleDao;
import dev.getelements.elements.sdk.dao.ScheduleEventDao;
import dev.getelements.elements.sdk.model.exception.mission.ScheduleEventNotFoundException;
import dev.getelements.elements.sdk.model.mission.Mission;
import dev.getelements.elements.sdk.model.mission.Schedule;
import dev.getelements.elements.sdk.model.mission.ScheduleEvent;
import dev.getelements.elements.sdk.model.util.PaginationWalker;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import jakarta.inject.Inject;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static dev.getelements.elements.sdk.model.goods.ItemCategory.FUNGIBLE;
import static java.util.stream.Collectors.toList;
import static java.util.stream.IntStream.range;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

@Guice(modules = IntegrationTestModule.class)
public class MongoScheduleEventDaoTest {

    private Schedule schedule;

    private List<Mission> missions;

    private ScheduleDao scheduleDao;

    private ScheduleEventDao scheduleEventDao;

    private ItemTestFactory itemTestFactory;

    private MissionTestFactory missionTestFactory;

    private List<ScheduleEvent> scheduleEvents = new CopyOnWriteArrayList<>();

    @BeforeClass
    public void setupSchedule() {
        final var schedule = new Schedule();
        schedule.setName("schedule_event_test");
        schedule.setDisplayName("Schedule Schedule Events");
        schedule.setDescription("Schedule Schedule Events");
        this.schedule = getScheduleDao().create(schedule);
    }

    @BeforeClass
    public void setupMissions() {
        final var item = getItemTestFactory().createTestItem(FUNGIBLE, false);
        missions = range(0, 10)
                .mapToObj(i -> getMissionTestFactory().createTestFiniteMission("event", item))
                .collect(toList());
    }

    @DataProvider
    public Object[][] allDateBounds() {
        final var now = System.currentTimeMillis();
        final var later = System.currentTimeMillis() + 1000;
        return new Object[][] {
                new Object[] { null, null },
                new Object[] { null, later},
                new Object[] { now,  null},
                new Object[] { now, later}
        };
    }

    @DataProvider
    public Object[][] allEventIndexesAndDateBounds() {
        final var now = System.currentTimeMillis();
        final var later = System.currentTimeMillis() + 1000;
        return new Object[][] {
                new Object[] { 0, null, null },
                new Object[] { 1, null, later},
                new Object[] { 1, now,  null},
                new Object[] { 3, now, later}
        };
    }

    @DataProvider
    public Object[][] allEvents() {
        return scheduleEvents
                .stream()
                .map(e -> new Object[] {e})
                .toArray(Object[][]::new);
    }

    @Test(groups = "create", dataProvider = "allDateBounds")
    public void testCreateEvent(final Long begin, final Long end) {

        final var event = new ScheduleEvent();
        event.setSchedule(schedule);
        event.setMissions(missions);
        event.setBegin(begin);
        event.setEnd(end);

        final var created = getScheduleEventDao().createScheduleEvent(event);
        scheduleEvents.add(created);

    }

    @Test(groups = "update", dataProvider = "allEventIndexesAndDateBounds", dependsOnGroups = "create")
    public void testUpdateEvent(final int index, final Long begin, final Long end) {

        final var event = scheduleEvents.get(index);
        final var toUpdate = new ScheduleEvent();

        toUpdate.setId(event.getId());
        toUpdate.setEnd(end);
        toUpdate.setBegin(begin);
        toUpdate.setSchedule(event.getSchedule());
        toUpdate.setMissions(event.getMissions());

        final var updated = getScheduleEventDao().updateScheduleEvent(toUpdate);
        assertEquals(updated.getId(), event.getId());
        assertEquals(updated.getSchedule().getId(), event.getSchedule().getId());
        assertEquals(updated.getBegin(), begin);
        assertEquals(updated.getEnd(), end);

        scheduleEvents.set(index, updated);

    }

    @Test(groups = "fetch", dependsOnGroups = "update")
    public void testGetEvents() {
        final var fetchedEvents = new PaginationWalker()
                .toList(((offset, count) -> getScheduleEventDao().getScheduleEvents(schedule.getId(), offset, count)));

        fetchedEvents.forEach(event -> {
            final var found = scheduleEvents
                    .stream()
                    .anyMatch(e -> e.getId().equals(event.getId()));
            assertTrue(found);
        });

    }

    @Test(groups = "fetch", dependsOnGroups = "update")
    public void testGetAllEvents() {

        final var fetchedEvents = getScheduleEventDao().getAllScheduleEvents(schedule.getId());

        fetchedEvents.forEach(event -> {
            final var found = scheduleEvents
                    .stream()
                    .anyMatch(e -> e.getId().equals(event.getId()));
            assertTrue(found);
        });

    }

    @Test(groups = "fetch", dataProvider = "allEvents", dependsOnGroups = "update")
    public void testGetEventsById(final ScheduleEvent scheduleEvent) {

        final var event = getScheduleEventDao().getScheduleEventById(
                schedule.getId(),
                scheduleEvent.getId()
        );

        assertEquals(event.getId(), scheduleEvent.getId());
        assertEquals(event.getSchedule().getId(), scheduleEvent.getSchedule().getId());
        assertEquals(event.getBegin(), scheduleEvent.getBegin());
        assertEquals(event.getEnd(), scheduleEvent.getEnd());

    }

    @Test(groups = "fetch", dataProvider = "allEvents", dependsOnGroups = "update")
    public void testFindEventsById(final ScheduleEvent scheduleEvent) {

        final var event = getScheduleEventDao().findScheduleEventById(
                schedule.getId(),
                scheduleEvent.getId()
        ).get();

        assertEquals(event.getId(), scheduleEvent.getId());
        assertEquals(event.getSchedule().getId(), scheduleEvent.getSchedule().getId());
        assertEquals(event.getBegin(), scheduleEvent.getBegin());
        assertEquals(event.getEnd(), scheduleEvent.getEnd());

    }

    @Test(groups = "delete", dataProvider = "allEvents", dependsOnGroups = "fetch")
    public void deleteEvent(final ScheduleEvent scheduleEvent) {
        getScheduleEventDao().deleteScheduleEvent(scheduleEvent.getSchedule().getId(), scheduleEvent.getId());
    }

    @Test(
            groups = "delete",
            dataProvider = "allEvents",
            dependsOnGroups = "fetch",
            dependsOnMethods = "deleteEvent",
            expectedExceptions = ScheduleEventNotFoundException.class
    )
    public void doubleDeleteEvent(final ScheduleEvent scheduleEvent) {
        getScheduleEventDao().deleteScheduleEvent(scheduleEvent.getSchedule().getId(), scheduleEvent.getId());
    }

    @Test(
            groups = "delete",
            dataProvider = "allEvents",
            dependsOnMethods = "deleteEvent",
            expectedExceptions = ScheduleEventNotFoundException.class
    )
    public void checkDeletedRecordsArentFound(final ScheduleEvent scheduleEvent) {

        final var fetched = getScheduleEventDao().findScheduleEventById(
                scheduleEvent.getSchedule().getId(),
                scheduleEvent.getId()
        );

        assertTrue(fetched.isEmpty());

        getScheduleEventDao().getScheduleEventById(
                scheduleEvent.getSchedule().getId(),
                scheduleEvent.getId()
        );

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

    public ItemTestFactory getItemTestFactory() {
        return itemTestFactory;
    }

    @Inject
    public void setItemTestFactory(ItemTestFactory itemTestFactory) {
        this.itemTestFactory = itemTestFactory;
    }

    public MissionTestFactory getMissionTestFactory() {
        return missionTestFactory;
    }

    @Inject
    public void setMissionTestFactory(MissionTestFactory missionTestFactory) {
        this.missionTestFactory = missionTestFactory;
    }

}
