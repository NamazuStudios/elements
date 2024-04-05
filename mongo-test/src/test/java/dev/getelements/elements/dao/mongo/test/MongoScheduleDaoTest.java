package dev.getelements.elements.dao.mongo.test;

import dev.getelements.elements.dao.ScheduleDao;
import dev.getelements.elements.model.mission.Schedule;
import dev.getelements.elements.exception.DuplicateException;
import dev.getelements.elements.util.PaginationWalker;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import javax.inject.Inject;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.lang.String.format;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.AssertJUnit.assertNotNull;

@Guice(modules = IntegrationTestModule.class)
public class MongoScheduleDaoTest {

    private ScheduleDao scheduleDao;

    public static List<String> mockScheduleNamesList() {
        return IntStream.range(0, 10)
                .mapToObj(i -> format("schedule_%d", i))
                .collect(toList());
    }

    @DataProvider
    public static Object[][] mockScheduleNames() {
        return mockScheduleNamesList()
                .stream().map(name -> new Object[]{name})
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
            groups = "read",
            dataProvider = "mockScheduleNames",
            dependsOnGroups = "create"
    )
    public void getScheduleByNameAndId(final String name) {

        final var byName = getScheduleDao().getScheduleByNameOrId(name);
        assertNotNull(byName);

        final var byId = getScheduleDao().getScheduleByNameOrId(byName.getId());
        assertNotNull(byId);

    }

    @Test(
            groups = "read",
            dataProvider = "mockScheduleNames",
            dependsOnGroups = "create"
    )
    public void findScheduleByNameAndId(final String name) {

        final var byName = getScheduleDao().findScheduleByNameOrId(name);
        assertTrue(byName.isPresent());

        final var byId = getScheduleDao().findScheduleByNameOrId(byName.get().getId());
        assertTrue(byId.isPresent());

    }

    @Test(
            groups = "read",
            dependsOnGroups = "create"
    )
    public void listSchedules() {

        final var schedulesById = mockScheduleNamesList()
                .stream()
                .map(getScheduleDao()::getScheduleByNameOrId)
                .collect(toMap(Schedule::getId, Schedule::getName));

        final var allSchedules = new PaginationWalker().toList(getScheduleDao()::getSchedules);
        allSchedules.forEach(s -> assertTrue(schedulesById.containsKey(s.getId())));

    }

    @Test(
            groups = "read",
            dataProvider = "mockScheduleNames",
            dependsOnGroups = "create"
    )
    public void searchSchedulesText(final String name) {

        final var fetched = getScheduleDao().getScheduleByNameOrId(name);

        final var searched = getScheduleDao()
                .getSchedules(0, 1, name)
                .getObjects()
                .get(0);

        assertEquals(searched.getId(), fetched.getId());

    }

    @Test(
            groups = "read",
            dataProvider = "mockScheduleNames",
            dependsOnGroups = "create"
    )
    public void searchSchedulesQuery(final String name) {

        final var fetched = getScheduleDao().getScheduleByNameOrId(name);

        final var searched = getScheduleDao()
                .getSchedules(0, 1, format("name:%s", name))
                .getObjects()
                .get(0);

        assertEquals(searched.getId(), fetched.getId());

    }

    @Test(
            groups = "update",
            dataProvider = "mockScheduleNamesUpdated",
            dependsOnGroups = "read"
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

    public ScheduleDao getScheduleDao() {
        return scheduleDao;
    }

    @Inject
    public void setScheduleDao(ScheduleDao scheduleDao) {
        this.scheduleDao = scheduleDao;
    }

}
