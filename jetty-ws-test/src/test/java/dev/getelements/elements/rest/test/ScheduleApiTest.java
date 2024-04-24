package dev.getelements.elements.rest.test;

import dev.getelements.elements.model.mission.CreateScheduleRequest;
import dev.getelements.elements.model.mission.Schedule;
import dev.getelements.elements.model.mission.UpdateScheduleRequest;
import dev.getelements.elements.rest.test.ClientContext;
import dev.getelements.elements.rest.test.TestUtils;
import dev.getelements.elements.rest.test.model.SchedulePagination;
import dev.getelements.elements.util.PaginationWalker;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;

import java.util.function.Consumer;

import static dev.getelements.elements.rest.test.TestUtils.TEST_API_ROOT;
import static java.lang.String.format;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.testng.Assert.*;

public class ScheduleApiTest {

    @Factory
    public static Object[] getTests() {
        return new Object[] {
                TestUtils.getInstance().getXodusTest(ScheduleApiTest.class),
                TestUtils.getInstance().getUnixFSTest(ScheduleApiTest.class)
        };
    }

    @Inject
    @Named(TEST_API_ROOT)
    private String apiRoot;

    @Inject
    private Client client;

    @Inject
    private ClientContext user0;

    @Inject
    private ClientContext user1;

    @Inject
    private ClientContext superUser;

    private Schedule scheduleA;

    private Schedule scheduleB;

    @BeforeClass
    public void createUsers() {
        user0.createUser("schedule_user0").createSession();
        user1.createUser("schedule_user1").createSession();
        superUser.createSuperuser("schedule_admin").createSession();
    }

    @Test(dataProvider = "getUserContext")
    public void testRegularUserIsDeniedCreate(final ClientContext userContext) {

        final var create = new CreateScheduleRequest();
        create.setName("test_create");
        create.setDescription("test_create_desc");

        var response = client
                .target(apiRoot + "/schedule")
                .request()
                .header("Authorization", format("Bearer %s", userContext.getSessionSecret()))
                .post(Entity.entity(create, APPLICATION_JSON));

        assertEquals(response.getStatus(), 403);

    }

    @Test(groups = "create_schedules")
    public void createSchedules() {

        final var aRequest = new CreateScheduleRequest();
        aRequest.setName("test_schedule_a");
        aRequest.setDisplayName("test_schedule_a_display_name");
        aRequest.setDescription("test_schedule_a_description");

        final var bRequest = new CreateScheduleRequest();
        bRequest.setName("test_schedule_b");
        bRequest.setDisplayName("test_schedule_b_display_name");
        bRequest.setDescription("test_schedule_b_description");

        scheduleA = createSchedule(aRequest);
        scheduleB = createSchedule(bRequest);

    }

    private Schedule createSchedule(final CreateScheduleRequest createScheduleRequest) {

        final var response = client
                .target(apiRoot + "/schedule")
                .request()
                .header("Authorization", format("Bearer %s", superUser.getSessionSecret()))
                .post(Entity.entity(createScheduleRequest, APPLICATION_JSON));

        assertEquals(response.getStatus(), 200);

        return response.readEntity(Schedule.class);

    }

    @Test(groups = "create_schedules",dataProvider = "getSchedulesAndUserContexts")
    public void testRegularUserIsDeniedUpdate(final Schedule schedule, final ClientContext userContext) {

        final var update = new UpdateScheduleRequest();
        update.setName("test_update");
        update.setDescription("test_update_desc");

        final var response = client
                .target(apiRoot + "/schedule/" + schedule.getId())
                .request()
                .header("Authorization", format("Bearer %s", userContext.getSessionSecret()))
                .put(Entity.entity(update, APPLICATION_JSON));

        assertEquals(response.getStatus(), 403);

    }

    @Test(groups = "create_schedules",dataProvider = "getSchedulesAndUserContexts")
    public void testRegularUserIsDeniedDelete(final Schedule schedule, final ClientContext userContext) {

        final var response = client
                .target(apiRoot + "/schedule/" + schedule.getId())
                .request()
                .header("Authorization", format("Bearer %s", userContext.getSessionSecret()))
                .delete();

        assertEquals(response.getStatus(), 403);

    }

    @Test(groups = "create_schedules", dataProvider = "getUserContext")
    public void testRegularUserIsDeniedFetch(final ClientContext userContext) {

        final var schedules = new PaginationWalker().toList(((offset, count) -> client
                .target(format("%s/schedule?offset=%d&count=%d", apiRoot, offset, count))
                .request()
                .header("Authorization", format("Bearer %s", userContext.getSessionSecret()))
                .get(SchedulePagination.class)
        ));

        final var response = client
                .target(apiRoot + "/schedule")
                .request()
                .header("Authorization", format("Bearer %s", userContext.getSessionSecret()))
                .delete();

        assertEquals(response.getStatus(), 403);

    }

    @Test(groups = "create_schedules", dataProvider = "getSchedulesAndUserContexts")
    public void testRegularUserIsDeniedFetch(final Schedule schedule, final ClientContext userContext) {

        final var response = client
                .target(apiRoot + "/schedule/" + schedule.getId())
                .request()
                .header("Authorization", format("Bearer %s", userContext.getSessionSecret()))
                .delete();

        assertEquals(response.getStatus(), 403);

    }


    @DataProvider
    public Object[][] getUserContext() {
        return new Object[][] {
                new Object[] {user0},
                new Object[] {user1},
        };
    }

    @DataProvider
    public Object[][] getSchedulesAndUserContexts() {
        return new Object[][] {
                new Object[] {scheduleA, user0},
                new Object[] {scheduleB, user0},
                new Object[] {scheduleA, user1},
                new Object[] {scheduleB, user1},
        };
    }

    @DataProvider
    public Object[][] getSchedulesAndUpdaters() {
        return new Object[][] {
                new Object[] { scheduleA, (Consumer<Schedule>) s -> this.scheduleA = s},
                new Object[] { scheduleB, (Consumer<Schedule>) s -> this.scheduleB = s}
        };
    }

    @Test(dependsOnGroups = "create_schedules", dataProvider = "getSchedulesAndUpdaters")
    public void testUpdateSchedule(final Schedule schedule, final Consumer<Schedule> updater) {

        final var request = new UpdateScheduleRequest();

        request.setName("updated_schedule_name");
        request.setDescription("updated_schedule_desc");
        request.setDisplayName("updated_schedule_display_name");

        final var response = client
                .target(format("%s/schedule/%s", apiRoot, schedule.getId()))
                .request()
                .header("Authorization", format("Bearer %s", superUser.getSessionSecret()))
                .put(Entity.entity(request, APPLICATION_JSON))
                .readEntity(Schedule.class);

        assertNotNull(response.getId());
        assertEquals(response.getId(), schedule.getId());
        assertEquals(response.getName(), request.getName());
        assertEquals(response.getDisplayName(), request.getDisplayName());
        assertEquals(response.getDescription(), request.getDescription());
        updater.accept(schedule);

    }

    public void testGetSchedules() {

        final var schedules = client
                .target(apiRoot + "/schedule")
                .request()
                .header("Authorization", format("Bearer %s", superUser.getSessionSecret()))
                .get()
                .readEntity(SchedulePagination.class);

        assertFalse(schedules.getObjects().isEmpty(), "Expected non-empty object response.");

        final var schedule = schedules.getObjects()
                .stream()
                .filter(i -> i.getName().equals(scheduleA.getName()))
                .findFirst()
                .get();

    }

    @Test(dependsOnMethods = "testUpdateSchedule", dataProvider = "getUserContext")
    public void testDeleteSchedule(final ClientContext userContext) {

        final var schedule = client
                .target(apiRoot + "/schedule/" + scheduleA.getId())
                .request()
                .header("Authorization", format("Bearer %s", userContext.getSessionSecret()))
                .get()
                .readEntity(Schedule.class);

        final var response = client
                .target(format("%s/schedule/%s", apiRoot, schedule.getId()))
                .request()
                .header("Authorization", format("Bearer %s", superUser.getSessionSecret()))
                .delete();

        assertEquals(response.getStatus(), 204);

    }
}
