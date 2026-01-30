package dev.getelements.elements.rest.test;

import dev.getelements.elements.sdk.model.goods.CreateItemRequest;
import dev.getelements.elements.sdk.model.goods.Item;
import dev.getelements.elements.sdk.model.goods.ItemCategory;
import dev.getelements.elements.sdk.model.mission.*;
import dev.getelements.elements.sdk.model.reward.Reward;
import dev.getelements.elements.rest.test.model.ProgressPagination;
import dev.getelements.elements.sdk.model.util.PaginationWalker;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import jakarta.inject.Inject;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.Entity;
import java.util.List;
import java.util.Objects;

import static dev.getelements.elements.rest.test.TestUtils.getInstance;
import static java.lang.String.format;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class ScheduleProgressApiTest {

    @Factory
    public static Object[] getTests() {
        return new Object[] {
                getInstance().getTestFixture(ScheduleProgressApiTest.class)
        };
    }

    @Inject
    private ApiRoot apiRoot;

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

        user.createUser("schedule_user")
                .createProfile("schedule_profile")
                .createSessionWithDefaultProfile();

        superUser.createSuperuser("schedule_admin")
                .createSession();

    }

    @BeforeClass(dependsOnMethods = "createUsers")
    public void createSchedule() {

        final var request = new CreateScheduleRequest();
        request.setName("test_schedule_progress_progress");
        request.setDisplayName("test_schedule_progress_progress");
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
        request.setName("test_schedule_progress_reward_item");
        request.setDisplayName("test_schedule_progress_reward_item_display_name");
        request.setDescription("test_schedule_progress_reward_item_description");
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
        createMission.setName("test_schedule_progress");
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

    @BeforeClass(dependsOnMethods = "createMission")
    public void createScheduleEvent() {

        final var request = new CreateScheduleEventRequest();
        request.setMissionNamesOrIds(List.of(mission.getName()));

        final var response = client
                .target(format("%s/schedule/%s/event", apiRoot, schedule.getName()))
                .request()
                .header("Authorization", format("Bearer %s", superUser.getSessionSecret()))
                .post(Entity.entity(request, APPLICATION_JSON));

        assertEquals(response.getStatus(), 200);
        scheduleEvent = response.readEntity(ScheduleEvent.class);

    }

    @Test
    public void testEventAssignsMission() {

        final var assignedProgresses = new PaginationWalker().toList((offset, count) -> apiRoot
                .formatPagination("/schedule/%s/progress?", offset, count, schedule.getName())
                .request()
                .header("Authorization", format("Bearer %s", user.getSessionSecret()))
                .get(ProgressPagination.class));

        assertEquals(assignedProgresses.size(), 1);

        final var allProgresses = getAllProgresses();

        assignedProgresses.forEach(progress -> {
            final var exists = allProgresses
                    .stream()
                    .anyMatch(p -> Objects.equals(p.getId(), progress.getId()));
            assertTrue(exists, "Expected progress to exist in listing of all Progresses.");
        });

    }

    @Test(dependsOnMethods = "testEventAssignsMission")
    public void testForceEventExpiration() {

        final var update = new UpdateScheduleEventRequest();
        update.setEnd(1000L);
        update.setMissionNamesOrIds(List.of(mission.getName()));

        final var response = apiRoot
                .formatTarget("/schedule/%s/event/%s", schedule.getName(), scheduleEvent.getId())
                .request()
                .header("Authorization", format("Bearer %s", superUser.getSessionSecret()))
                .put(Entity.entity(update, APPLICATION_JSON));

        assertEquals(response.getStatus(), 200);
        scheduleEvent = response.readEntity(ScheduleEvent.class);

    }

    @Test(dependsOnMethods = "testForceEventExpiration")
    public void testEventExpiresMission() {

        final var assignedProgresses = new PaginationWalker().toList((offset, count) -> apiRoot
                .formatPagination("/schedule/%s/progress?", offset, count, schedule.getName())
                .request()
                .header("Authorization", format("Bearer %s", user.getSessionSecret()))
                .get(ProgressPagination.class));

        assertEquals(assignedProgresses.size(), 0);

        final var allProgresses = getAllProgresses();

        assignedProgresses.forEach(progress -> {
            final var exists = allProgresses
                    .stream()
                    .noneMatch(p -> Objects.equals(p.getId(), progress.getId()));
            assertTrue(exists, "Expected progress to exist in listing of all Progresses.");
        });

    }

    private List<Progress> getAllProgresses() {
        return new PaginationWalker().toList((offset, count) -> apiRoot
                .formatPagination("/progress?", offset, count)
                .request()
                .accept(APPLICATION_JSON)
                .header("Authorization", format("Bearer %s", user.getSessionSecret()))
                .get(ProgressPagination.class));
    }

}
