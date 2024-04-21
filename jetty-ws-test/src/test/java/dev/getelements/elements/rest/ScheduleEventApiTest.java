package dev.getelements.elements.rest;

import dev.getelements.elements.model.goods.CreateItemRequest;
import dev.getelements.elements.model.goods.Item;
import dev.getelements.elements.model.goods.ItemCategory;
import dev.getelements.elements.model.mission.*;
import dev.getelements.elements.model.reward.Reward;
import dev.getelements.elements.rest.model.ScheduleEventPagination;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;

import java.util.List;

import static dev.getelements.elements.rest.TestUtils.TEST_API_ROOT;
import static java.lang.String.format;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.testng.Assert.*;

public class ScheduleEventApiTest {

    @Factory
    public static Object[] getTests() {
        return new Object[] {
                TestUtils.getInstance().getXodusTest(ScheduleEventApiTest.class),
                TestUtils.getInstance().getUnixFSTest(ScheduleEventApiTest.class)
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

    @Test
    public void createUsers() {
        user.createUser("schedule_user").createSession();
        superUser.createSuperuser("schedule_admin").createSession();
    }

    @Test(dependsOnMethods = "createUsers")
    public void createSchedule() {

        final var request = new CreateScheduleRequest();
        request.setName("test_schedule");
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

    @Test(dependsOnMethods = "createSchedule")
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

    @Test(dependsOnMethods = "createRewardItem")
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

        final var createMission = new Mission();
        createMission.setName("test_schedule");
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

    @Test(dependsOnMethods = "createMission")
    public void createScheduleEvent() {

        final var request = new CreateScheduleEventRequest();
        request.setMissionNamesOrIds(List.of(mission.getName()));
        request.setBegin(0L);
        request.setEnd(System.currentTimeMillis() + 1000);

        final var response = client
                .target(format("%s/schedule/%s/event", apiRoot, schedule.getName()))
                .request()
                .header("Authorization", format("Bearer %s", superUser.getSessionSecret()))
                .post(Entity.entity(request, APPLICATION_JSON));

        assertEquals(response.getStatus(), 200);

        scheduleEvent = response.readEntity(ScheduleEvent.class);
    }

    @DataProvider
    public Object[][] getUserContext() {
        return new Object[][] {
                new Object[] {user}
        };
    }

    @Test(dependsOnMethods = {"createUsers", "createScheduleEvent"}, dataProvider = "getUserContext")
    public void testRegularUserIsDenied(final ClientContext userContext) {

        final var create = new CreateScheduleEventRequest();

        var response = client
                .target(format("%s/schedule/%s/event", apiRoot, schedule.getName()))
                .request()
                .header("Authorization", format("Bearer %s", userContext.getSessionSecret()))
                .post(Entity.entity(create, APPLICATION_JSON));

        assertEquals(response.getStatus(), 403);

        final var update = new UpdateScheduleEventRequest();
        update.setBegin(10L);
        update.setEnd(100L);

        response = client
                .target(format("%s/schedule/%s/event/%s", apiRoot, schedule.getName(), scheduleEvent.getId()))
                .request()
                .header("Authorization", format("Bearer %s", userContext.getSessionSecret()))
                .put(Entity.entity(update, APPLICATION_JSON));

        assertEquals(response.getStatus(), 403);

        response = client
                .target(format("%s/schedule/%s/event/%s", apiRoot, schedule.getName(), scheduleEvent.getId()))
                .request()
                .header("Authorization", format("Bearer %s", userContext.getSessionSecret()))
                .delete();

        assertEquals(response.getStatus(), 403);

    }


    @Test(dependsOnMethods = {"createScheduleEvent"}, dataProvider = "getUserContext")
    public void testUpdateScheduleEvent(final ClientContext userContext) {

        final var schedules = client
                .target(apiRoot + "/schedule")
                .request()
                .header("Authorization", format("Bearer %s", userContext.getSessionSecret()))
                .get()
                .readEntity(ScheduleEventPagination.class);

        assertFalse(schedules.getObjects().isEmpty(), "Expected non-empty object response.");

        final var event = schedules.getObjects()
                .stream()
                .filter(i -> i.getId().equals(scheduleEvent.getId()))
                .findFirst()
                .get();

        final var request = new UpdateScheduleEventRequest();

        request.setBegin(1000L);
        request.setEnd(System.currentTimeMillis() + 10000);
        request.setMissionNamesOrIds(List.of(mission.getName(), mission.getId()));

        final var response = client
                .target(format("%s/schedule/%s/event/%s", apiRoot, schedule.getId(), scheduleEvent.getId()))
                .request()
                .header("X-HTTP-Method-Override", "PATCH")
                .header("Authorization", format("Bearer %s", superUser.getSessionSecret()))
                .post(Entity.entity(request, APPLICATION_JSON))
                .readEntity(ScheduleEvent.class);

        assertNotNull(response.getId());
        assertEquals(response.getId(), event.getId());
        assertEquals(response.getBegin(), request.getBegin());
        assertEquals(response.getEnd(), request.getEnd());

        response.getMissions().stream()
                .forEach(m ->
                        assertTrue(request.getMissionNamesOrIds().stream()
                                .anyMatch(nameOrId ->
                                    m.getId().equals(nameOrId) || m.getName().equals(nameOrId))));
    }

    @Test(dependsOnMethods = "testUpdateScheduleEvent", dataProvider = "getUserContext")
    public void testDeleteScheduleEvent(final ClientContext userContext) {

        final var event = client
                .target(format("%s/schedule/%s/event/%s", apiRoot, schedule.getName(), scheduleEvent.getId()))
                .request()
                .header("Authorization", format("Bearer %s", userContext.getSessionSecret()))
                .get()
                .readEntity(ScheduleEvent.class);

        final var response = client
                .target(format("%s/schedule/%s/event/%s", apiRoot, schedule.getName(), event.getId()))
                .request()
                .header("Authorization", format("Bearer %s", superUser.getSessionSecret()))
                .delete();

        assertEquals(response.getStatus(), 204);

    }
}
