package dev.getelements.elements.dao.mongo.test;

import dev.getelements.elements.sdk.dao.ScheduleDao;
import dev.getelements.elements.sdk.model.exception.mission.ScheduleNotFoundException;
import dev.getelements.elements.sdk.model.mission.Schedule;
import dev.getelements.elements.sdk.model.exception.DuplicateException;
import dev.getelements.elements.sdk.model.util.PaginationWalker;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import jakarta.inject.Inject;
import java.util.HashSet;
import java.util.List;
import java.util.stream.IntStream;

import static java.lang.String.format;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static org.testng.Assert.*;
import static org.testng.AssertJUnit.assertNotNull;

@Guice(modules = IntegrationTestModule.class)
public class MongoScheduleDaoTest {

    private ScheduleDao scheduleDao;

    public static List<String> mockScheduleNamesList() {
        return IntStream.range(0, 10)
                .mapToObj(i -> format("schedule_%d", i))
                .collect(toList());
    }

    public static List<String> mockScheduleNamesUpdatedList() {
        return mockScheduleNamesList()
                .stream()
                .map(name -> format("%s_updated", name))
                .collect(toList());
    }

    @DataProvider
    public static Object[][] mockScheduleNames() {
        return mockScheduleNamesList()
                .stream()
                .map(name -> new Object[]{name})
                .toArray(Object[][]::new);
    }

    @DataProvider
    public static Object[][] mockScheduleNamesUpdated() {
        return mockScheduleNamesList()
                .stream().map(name -> new Object[]{name, format("%s_updated", name)})
                .toArray(Object[][]::new);
    }

    @Test(
            groups = "create",
            dataProvider = "mockScheduleNames"
    )
    public void createSchedule(final String name) {

        final var schedule = new Schedule();
        schedule.setName(name);
        schedule.setDisplayName(format("Schedule %s", name));
        schedule.setDescription(format("Schedule for %s", name));

        final var created = getScheduleDao().create(schedule);
        assertNotNull(created.getId());
        assertEquals(created.getName(), schedule.getName());
        assertEquals(created.getDescription(), schedule.getDescription());
        assertEquals(created.getDisplayName(), schedule.getDisplayName());

    }

    @Test(
            groups = "create",
            dataProvider = "mockScheduleNames",
            dependsOnMethods = "createSchedule",
            expectedExceptions = DuplicateException.class
    )
    public void createScheduleDuplicateFails(final String name) {
        final var schedule = new Schedule();
        schedule.setName(name);
        schedule.setDisplayName(format("Schedule %s", name));
        schedule.setDescription(format("Schedule for %s", name));
        getScheduleDao().create(schedule);
    }

    @Test(
            groups = "update",
            dataProvider = "mockScheduleNamesUpdated",
            dependsOnGroups = "create"
    )
    public void updateSchedule(final String original, final String update) {

        final var byName = getScheduleDao().getScheduleByNameOrId(original);
        byName.setName(update);

        final var updated = getScheduleDao().updateSchedule(byName);
        assertEquals(updated.getName(), update);

        final var fetched = getScheduleDao().getScheduleByNameOrId(update);
        assertEquals(fetched.getName(), update);

        assertEquals(updated.getId(), byName.getId());
        assertEquals(fetched.getId(), byName.getId());

    }

    @Test(
            groups = "fetch",
            dataProvider = "mockScheduleNamesUpdated",
            dependsOnGroups = "update"
    )
    public void getScheduleByNameAndId(final String name, final String update) {

        final var byName = getScheduleDao().getScheduleByNameOrId(update);
        assertNotNull(byName);

        final var byId = getScheduleDao().getScheduleByNameOrId(byName.getId());
        assertNotNull(byId);

    }

    @Test(
            groups = "fetch",
            dataProvider = "mockScheduleNamesUpdated",
            dependsOnGroups = "update"
    )
    public void findScheduleByNameAndId(final String name, final String updated) {

        final var byName = getScheduleDao().findScheduleByNameOrId(updated);
        assertTrue(byName.isPresent());

        final var byId = getScheduleDao().findScheduleByNameOrId(byName.get().getId());
        assertTrue(byId.isPresent());

    }

    @Test(
            groups = "fetch",
            dependsOnGroups = "update"
    )
    public void listSchedules() {

        final var schedulesById = mockScheduleNamesUpdatedList()
                .stream()
                .map(getScheduleDao()::getScheduleByNameOrId)
                .collect(toMap(Schedule::getId, Schedule::getName));

        final var allScheduleIds = new PaginationWalker().aggregate(
                new HashSet<String>(),
                getScheduleDao()::getSchedules,
                (ids, schedules) -> {
                    schedules.stream()
                            .map(Schedule::getId)
                            .forEach(ids::add);
                    return ids;
                });

        assertTrue(allScheduleIds.containsAll(schedulesById.keySet()));

    }

    @Test(
            groups = "fetch",
            dataProvider = "mockScheduleNamesUpdated",
            dependsOnGroups = "update"
    )
    public void searchSchedulesText(final String name, final String updated) {

        final var fetched = getScheduleDao().getScheduleByNameOrId(updated);

        final var searched = getScheduleDao()
                .getSchedules(0, 1, name)
                .getObjects()
                .get(0);

        assertEquals(searched.getId(), fetched.getId());

    }

    @Test(
            groups = "fetch",
            dataProvider = "mockScheduleNamesUpdated",
            dependsOnGroups = "update"
    )
    public void searchSchedulesQuery(final String name, final String updated) {

        final var fetched = getScheduleDao().getScheduleByNameOrId(updated);

        final var searched = getScheduleDao()
                .getSchedules(0, 1, format("name:%s", updated))
                .getObjects()
                .get(0);

        assertEquals(searched.getId(), fetched.getId());

    }

    @Test(
            groups = "delete",
            dataProvider = "mockScheduleNamesUpdated",
            dependsOnGroups = "fetch"
    )
    public void deleteScheduleByName(final String original, final String update) {

        getScheduleDao().deleteSchedule(update);

        final var exists = getScheduleDao().findScheduleByNameOrId(update).isPresent();
        assertFalse(exists);

    }

    @Test(
            groups = "delete",
            dataProvider = "mockScheduleNamesUpdated",
            dependsOnGroups = "fetch",
            dependsOnMethods = "deleteScheduleByName",
            expectedExceptions = ScheduleNotFoundException.class
    )
    public void doubleDeleteScheduleByName(final String original, final String update) {
        getScheduleDao().deleteSchedule(update);
    }

    @Test(
            groups = "delete",
            dataProvider = "mockScheduleNamesUpdated",
            dependsOnGroups = "fetch"
    )
    public void testDeletedAreAbsentFromQueries(final String original, final String update) {

        final var allSchedules = new PaginationWalker().toList(getScheduleDao()::getSchedules);

        final var matchedInListing = allSchedules
                .stream()
                .anyMatch(schedule -> original.equals(schedule.getName()) || update.equals(schedule.getName()));

        final var matchedInSearchByOriginalName = !getScheduleDao()
                .getSchedules(0, 1, format("name:%s", original))
                .getObjects()
                .isEmpty();

        final var matchedInSearchByUpdatedName = !getScheduleDao()
                .getSchedules(0, 1, format("name:%s", update))
                .getObjects()
                .isEmpty();

        assertFalse(matchedInListing);
        assertFalse(matchedInSearchByOriginalName);
        assertFalse(matchedInSearchByUpdatedName);
        assertTrue(getScheduleDao().findScheduleByNameOrId(update).isEmpty());
        assertTrue(getScheduleDao().findScheduleByNameOrId(original).isEmpty());

    }

    public ScheduleDao getScheduleDao() {
        return scheduleDao;
    }

    @Inject
    public void setScheduleDao(ScheduleDao scheduleDao) {
        this.scheduleDao = scheduleDao;
    }

}
