package dev.getelements.elements.rest;

import dev.getelements.elements.model.mission.CreateScheduleRequest;
import dev.getelements.elements.model.mission.Schedule;
import dev.getelements.elements.model.mission.UpdateScheduleRequest;
import dev.getelements.elements.rest.model.SchedulePagination;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;

import static dev.getelements.elements.rest.TestUtils.TEST_API_ROOT;
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

    @Test
    public void createUsers() {
        user0.createUser("schedule_user0").createSession();
        user1.createUser("schedule_user1").createSession();
        superUser.createSuperuser("schedule_admin").createSession();
    }

    @Test(dependsOnMethods = "createUsers")
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

    @DataProvider
    public Object[][] getUserContext() {
        return new Object[][] {
                new Object[] {user0},
                new Object[] {user1},
        };
    }

    @Test(dependsOnMethods = {"createUsers", "createSchedules"}, dataProvider = "getUserContext")
    public void testRegularUserIsDenied(final ClientContext userContext) {

        final var create = new CreateScheduleRequest();
        create.setName("test_create");
        create.setDescription("test_create_desc");

        var response = client
                .target(apiRoot + "/schedule")
                .request()
                .header("Authorization", format("Bearer %s", userContext.getSessionSecret()))
                .post(Entity.entity(create, APPLICATION_JSON));

        assertEquals(response.getStatus(), 403);

        final var update = new UpdateScheduleRequest();
        update.setName("test_update");
        create.setDescription("test_update_desc");

        response = client
                .target(apiRoot + "/schedule/" + scheduleA.getId())
                .request()
                .header("Authorization", format("Bearer %s", userContext.getSessionSecret()))
                .put(Entity.entity(update, APPLICATION_JSON));

        assertEquals(response.getStatus(), 403);

        response = client
                .target(apiRoot + "/schedule/" + scheduleA.getId())
                .request()
                .header("Authorization", format("Bearer %s", userContext.getSessionSecret()))
                .delete();

        assertEquals(response.getStatus(), 403);

    }

    @Test(dependsOnMethods = {"createUsers"}, dataProvider = "getUserContext")
    public void testCreateSchedule(final ClientContext userContext) {

        final var request = new CreateScheduleRequest();
        request.setName(scheduleA.getName());
        request.setDisplayName(scheduleA.getDisplayName());
        request.setDescription(scheduleA.getDescription());

        final var response = client
                .target(apiRoot + "/schedule")
                .request()
                .header("Authorization", format("Bearer %s", superUser.getSessionSecret()))
                .post(Entity.entity(request, APPLICATION_JSON))
                .readEntity(Schedule.class);

        assertNotNull(response.getId());
        assertEquals(response.getName(), scheduleA.getName());
        assertEquals(response.getDisplayName(), scheduleA.getDisplayName());
        assertEquals(response.getDescription(), scheduleA.getDescription());

        //Just to update the id, since we can't assign it in the creation request
        scheduleA = response;
    }

    @Test(dependsOnMethods = {"testCreateSchedule"}, dataProvider = "getUserContext")
    public void testUpdateSchedule(final ClientContext userContext) {

        final var schedules = client
                .target(apiRoot + "/schedule")
                .request()
                .header("Authorization", format("Bearer %s", userContext.getSessionSecret()))
                .get()
                .readEntity(SchedulePagination.class);

        assertFalse(schedules.getObjects().isEmpty(), "Expected non-empty object response.");

        final var schedule = schedules.getObjects()
                .stream()
                .filter(i -> i.getName().equals(scheduleA.getName()))
                .findFirst()
                .get();

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
        assertEquals(response.getId(), scheduleA.getId());
        assertEquals(response.getName(), request.getName());
        assertEquals(response.getDisplayName(), request.getDisplayName());
        assertEquals(response.getDescription(), request.getDescription());

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
