package dev.getelements.elements.rest.test;

import dev.getelements.elements.sdk.model.mission.CreateScheduleRequest;
import dev.getelements.elements.sdk.model.mission.Schedule;
import dev.getelements.elements.sdk.model.mission.UpdateScheduleRequest;
import dev.getelements.elements.rest.test.model.SchedulePagination;
import dev.getelements.elements.sdk.model.util.PaginationWalker;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.Entity;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static dev.getelements.elements.rest.test.TestUtils.TEST_API_ROOT;
import static dev.getelements.elements.rest.test.TestUtils.getInstance;
import static java.lang.String.format;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.testng.Assert.*;

public class ScheduleApiTest {

    @Factory
    public static Object[] getTests() {
        return new Object[] {
                getInstance().getTestFixture(ScheduleApiTest.class)
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

    @Test(
            dependsOnGroups = {"create_schedules"},
            dataProvider = "getSchedulesAndUserContexts"
    )
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

    @Test(
            dependsOnGroups = {"create_schedules"},
            dataProvider = "getSchedulesAndUserContexts"
    )
    public void testRegularUserIsDeniedDelete(final Schedule schedule, final ClientContext userContext) {

        final var response = client
                .target(apiRoot + "/schedule/" + schedule.getId())
                .request()
                .header("Authorization", format("Bearer %s", userContext.getSessionSecret()))
                .delete();

        assertEquals(response.getStatus(), 403);

    }

    @Test(
            dependsOnGroups = { "create_schedules" },
            dataProvider = "getUserContext"
    )
    public void testRegularUserIsDeniedFetch(final ClientContext userContext) {

        final var response = client
                .target(apiRoot + "/schedule")
                .request()
                .header("Authorization", format("Bearer %s", userContext.getSessionSecret()))
                .get();

        assertEquals(response.getStatus(), 403);

    }

    @Test(
            dependsOnGroups = { "create_schedules" },
            dataProvider = "getSchedulesAndUserContexts"
    )
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

    @Test(
            groups = { "update_schedules" },
            dependsOnGroups = {"create_schedules"},
            dataProvider = "getSchedulesAndUpdaters"
    )
    public void testUpdateSchedule(final Schedule schedule, final Consumer<Schedule> updater) {

        final var request = new UpdateScheduleRequest();

        request.setName(format("%s_updated", schedule.getName()));
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
        updater.accept(response);

    }

    @DataProvider
    public Object[][] getSchedules() {
        return new Object[][] {
                new Object[] { scheduleA },
                new Object[] { scheduleB }
        };
    }

    @Test(
            groups = { "fetch_schedules" },
            dependsOnGroups = {"update_schedules"},
            dataProvider = "getSchedules"
    )
    public void testGetSchedules(final Schedule schedule) {

        final var schedules = new PaginationWalker().toList(((offset, count) -> client
                .target(format("%s/schedule?offset=%d&count=%d", apiRoot, offset, count))
                .request()
                .header("Authorization", format("Bearer %s", superUser.getSessionSecret()))
                .get(SchedulePagination.class)
        ));

        assertFalse(schedules.isEmpty(), "Expected non-empty object response.");

        final var result = schedules
                .stream()
                .filter(i -> i.getId().equals(schedule.getId()))
                .findFirst();

        assertTrue(result.isPresent());

    }

    @Test(
            groups = { "fetch_schedules" },
            dependsOnGroups = {"update_schedules"},
            dataProvider = "getSchedules"
    )
    public void testGetScheduleById(final Schedule schedule) {

        final var response = client
                .target(format("%s/schedule/%s", apiRoot, schedule.getId()))
                .request()
                .header("Authorization", format("Bearer %s", superUser.getSessionSecret()))
                .get();

        assertEquals(response.getStatus(), 200);

        final var result = response.readEntity(Schedule.class);
        assertEquals(result.getId(), schedule.getId());
        assertEquals(result.getName(), schedule.getName());
        assertEquals(result.getDisplayName(), schedule.getDisplayName());
        assertEquals(result.getDescription(), schedule.getDescription());

    }


    @Test(
            groups = { "fetch_schedules" },
            dependsOnGroups = {"update_schedules"},
            dataProvider = "getSchedules"
    )
    public void testGetScheduleByName(final Schedule schedule) {

        final var response = client
                .target(format("%s/schedule/%s", apiRoot, schedule.getName()))
                .request()
                .header("Authorization", format("Bearer %s", superUser.getSessionSecret()))
                .get();

        assertEquals(response.getStatus(), 200);

        final var result = response.readEntity(Schedule.class);
        assertEquals(result.getId(), schedule.getId());
        assertEquals(result.getName(), schedule.getName());
        assertEquals(result.getDisplayName(), schedule.getDisplayName());
        assertEquals(result.getDescription(), schedule.getDescription());

    }

    @Test(
            groups = { "delete_schedules" },
            dependsOnGroups = {"fetch_schedules"}
    )
    public void testDeleteSchedules() {
        doDelete(scheduleA::getId, 204);
        doDelete(scheduleB::getName, 204);
    }

    @Test(
            groups = { "delete_schedules" },
            dependsOnGroups = {"fetch_schedules"},
            dependsOnMethods = { "testDeleteSchedules" }
    )
    public void testDoubleDeleteSchedules() {
        doDelete(scheduleA::getId, 404);
        doDelete(scheduleB::getName, 404);
    }

    private void doDelete(final Supplier<String> idSupplier, final int expectedResponse) {

        final var id = idSupplier.get();

        final var deleteResponse = client
                .target(apiRoot + "/schedule/" + id)
                .request()
                .header("Authorization", format("Bearer %s", superUser.getSessionSecret()))
                .delete();

        assertEquals(deleteResponse.getStatus(), expectedResponse);

        final var fetchResponse = client
                .target(format("%s/schedule/%s", apiRoot, id))
                .request()
                .header("Authorization", format("Bearer %s", superUser.getSessionSecret()))
                .get();

        assertEquals(fetchResponse.getStatus(), 404);

    }

}
