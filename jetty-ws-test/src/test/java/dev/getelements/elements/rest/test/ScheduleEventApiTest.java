package dev.getelements.elements.rest.test;

import dev.getelements.elements.sdk.model.goods.CreateItemRequest;
import dev.getelements.elements.sdk.model.goods.Item;
import dev.getelements.elements.sdk.model.goods.ItemCategory;
import dev.getelements.elements.sdk.model.mission.*;
import dev.getelements.elements.sdk.model.reward.Reward;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.Entity;
import java.util.List;

import static dev.getelements.elements.rest.test.TestUtils.TEST_API_ROOT;
import static java.lang.String.format;
import static java.lang.System.currentTimeMillis;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.testng.Assert.*;

public class ScheduleEventApiTest {

    @Factory
    public static Object[] getTests() {
        return new Object[] {
                TestUtils.getInstance().getTestFixture(ScheduleEventApiTest.class)
        };
    }

    @Inject
    @Named(TEST_API_ROOT)
    private String apiRoot;

    @Inject
    private Client client;

    @Inject
    private ClientContext user;

    @Inject
    private ClientContext superUser;

    private ScheduleEvent scheduleEvent;

    private Schedule schedule;

    private Mission mission;

    private Item item;

    @BeforeClass
    public void createUsers() {
        user.createUser("schedule_user").createSession();
        superUser.createSuperuser("schedule_admin").createSession();
    }

    @BeforeClass(dependsOnMethods = "createUsers")
    public void createSchedule() {

        final var request = new CreateScheduleRequest();
        request.setName("test_schedule_event_schedule");
        request.setDisplayName("test_schedule_display_name");
        request.setDescription("test_schedule_description");

        final var response = client
                .target(apiRoot + "/schedule")
                .request()
                .header("Authorization", format("Bearer %s", superUser.getSessionSecret()))
                .post(Entity.entity(request, APPLICATION_JSON));

        assertEquals(response.getStatus(), 200);
        schedule = response.readEntity(Schedule.class);

    }

    @BeforeClass(dependsOnMethods = "createUsers")
    public void createRewardItem() {

        final var request = new CreateItemRequest();
        request.setName("test_schedule_event_reward_item");
        request.setDisplayName("test_schedule_event_reward_item_display_name");
        request.setDescription("test_schedule_event_reward_item_description");
        request.setCategory(ItemCategory.FUNGIBLE);

        final var response = client
                .target(apiRoot + "/item")
                .request()
                .header("Authorization", format("Bearer %s", superUser.getSessionSecret()))
                .post(Entity.entity(request, APPLICATION_JSON));

        assertEquals(response.getStatus(), 200);

        item = response.readEntity(Item.class);
    }

    @BeforeClass(dependsOnMethods = "createRewardItem")
    public void createMission() {

        final var reward = new Reward();
        reward.setQuantity(1);
        reward.setItem(item);

        final var rewards = List.of(reward);

        final var step1 = new Step();
        step1.setCount(1);
        step1.setDescription("step_1_desc");
        step1.setDisplayName("step_1");
        step1.setRewards(rewards);

        final var step2 = new Step();
        step2.setCount(2);
        step2.setDescription("step_2_desc");
        step2.setDisplayName("step_2");
        step2.setRewards(rewards);

        final var steps = List.of(step1, step2);

        final var createMission = new CreateMissionRequest();
        createMission.setName("test_schedule_event");
        createMission.setDisplayName("test_schedule_display_name");
        createMission.setDescription("test_schedule_description");
        createMission.setSteps(steps);

        final var response = client
                .target(apiRoot + "/mission")
                .request()
                .header("Authorization", format("Bearer %s", superUser.getSessionSecret()))
                .post(Entity.entity(createMission, APPLICATION_JSON));

        assertEquals(response.getStatus(), 200);
        mission = response.readEntity(Mission.class);

    }

    @Test(groups = "create_schedule_event")
    public void createScheduleEvent() {

        final var request = new CreateScheduleEventRequest();
        request.setMissionNamesOrIds(List.of(mission.getName()));

        final var response = client
                .target(format("%s/schedule/%s/event", apiRoot, schedule.getName()))
                .request()
                .header("Authorization", format("Bearer %s", superUser.getSessionSecret()))
                .post(Entity.entity(request, APPLICATION_JSON));

        assertEquals(response.getStatus(), 200);

        final var result = response.readEntity(ScheduleEvent.class);
        assertNotNull(result.getId());
        assertNull(result.getBegin());
        assertNull(result.getEnd());
        assertNotNull(result.getMissions());
        assertEquals(result.getMissions().size(), 1);

        scheduleEvent = result;

    }

    @DataProvider
    public Object[][] getUserContext() {
        return new Object[][] {
                new Object[] {user}
        };
    }

    @Test(dataProvider = "getUserContext")
    public void testRegularUserIsDeniedCreate(final ClientContext userContext) {

        final var create = new CreateScheduleEventRequest();

        var response = client
                .target(format("%s/schedule/%s/event", apiRoot, schedule.getName()))
                .request()
                .header("Authorization", format("Bearer %s", userContext.getSessionSecret()))
                .post(Entity.entity(create, APPLICATION_JSON));

        assertEquals(response.getStatus(), 403);

    }

    @Test(
            dependsOnGroups = { "create_schedule_event" },
            dataProvider = "getUserContext"
    )
    public void testRegularUserIsDeniedUpdate(final ClientContext userContext) {

        final var update = new UpdateScheduleEventRequest();
        update.setBegin(10L);
        update.setEnd(100L);

        final var response = client
                .target(format("%s/schedule/%s/event/%s", apiRoot, schedule.getName(), scheduleEvent.getId()))
                .request()
                .header("Authorization", format("Bearer %s", userContext.getSessionSecret()))
                .put(Entity.entity(update, APPLICATION_JSON));

        assertEquals(response.getStatus(), 403);

    }

    @Test(
            dependsOnGroups = { "create_schedule_event" },
            dataProvider = "getUserContext"
    )
    public void testRegularUserIsDeniedDelete(final ClientContext userContext) {

        final var response = client
                .target(format("%s/schedule/%s/event/%s", apiRoot, schedule.getName(), scheduleEvent.getId()))
                .request()
                .header("Authorization", format("Bearer %s", userContext.getSessionSecret()))
                .delete();

        assertEquals(response.getStatus(), 403);

    }


    @Test(
            groups = { "update_schedule_event" },
            dependsOnGroups = { "create_schedule_event" }
    )
    public void testUpdateScheduleEvent() {

        final var request = new UpdateScheduleEventRequest();

        final var now = currentTimeMillis();
        request.setBegin(now);
        request.setEnd(now + 10000);
        request.setMissionNamesOrIds(List.of(mission.getId()));

        final var response = client
                .target(format("%s/schedule/%s/event/%s", apiRoot, schedule.getId(), scheduleEvent.getId()))
                .request()
                .header("Authorization", format("Bearer %s", superUser.getSessionSecret()))
                .put(Entity.entity(request, APPLICATION_JSON));

        assertEquals(response.getStatus(), 200);

        final var result = response.readEntity(ScheduleEvent.class);

        assertNotNull(result.getId());
        assertEquals(result.getId(), scheduleEvent.getId());
        assertEquals(result.getBegin(), request.getBegin());
        assertEquals(result.getEnd(), request.getEnd());

        assertNotNull(result.getMissions());

        result.getMissions().forEach(m -> assertTrue(request
                    .getMissionNamesOrIds()
                    .stream()
                    .anyMatch(nameOrId -> m.getId().equals(nameOrId) || m.getName().equals(nameOrId))));

        scheduleEvent = result;

    }

    @Test(
            groups = "delete_schedule_event",
            dependsOnGroups = "update_schedule_event"
    )
    public void testDeleteScheduleEvent() {

        var response = client
                .target(format("%s/schedule/%s/event/%s", apiRoot, schedule.getId(), scheduleEvent.getId()))
                .request()
                .header("Authorization", format("Bearer %s", superUser.getSessionSecret()))
                .delete();

        assertEquals(response.getStatus(), 204);

        response = client
                .target(format("%s/schedule/%s/event/%s", apiRoot, schedule.getId(), scheduleEvent.getId()))
                .request()
                .header("Authorization", format("Bearer %s", superUser.getSessionSecret()))
                .get();

        assertEquals(response.getStatus(), 404);

        response = client
                .target(format("%s/schedule/%s/event/%s", apiRoot, schedule.getName(), scheduleEvent.getId()))
                .request()
                .header("Authorization", format("Bearer %s", superUser.getSessionSecret()))
                .get();

        assertEquals(response.getStatus(), 404);

    }

    @Test(
            groups = "delete_schedule_event",
            dependsOnGroups = "update_schedule_event",
            dependsOnMethods = "testDeleteScheduleEvent"
    )
    public void testDoubleDeleteScheduleEvent() {

        final var response = client
                .target(format("%s/schedule/%s/event/%s", apiRoot, schedule.getName(), scheduleEvent.getId()))
                .request()
                .header("Authorization", format("Bearer %s", superUser.getSessionSecret()))
                .delete();

        assertEquals(response.getStatus(), 404);

    }

}
