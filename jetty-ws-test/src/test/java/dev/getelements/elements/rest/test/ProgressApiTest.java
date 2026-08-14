package dev.getelements.elements.rest.test;

import dev.getelements.elements.sdk.model.mission.*;
import dev.getelements.elements.sdk.model.profile.Profile;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.Response;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import static dev.getelements.elements.rest.test.TestUtils.TEST_API_ROOT;
import static dev.getelements.elements.rest.test.TestUtils.getInstance;
import static dev.getelements.elements.sdk.model.Headers.SESSION_SECRET;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.testng.Assert.*;

/**
 * Regression tests for POST /progress (createProgress), GET /progress/{id}, and
 * PUT /progress/{id} (superuser manual update).
 *
 * Covers the bug where SuperUserProgressService.createProgress read profile from the
 * freshly-constructed Progress object instead of the request, always producing a null
 * profile and a 400 INVALID_DATA response.
 */
public class ProgressApiTest {

    @Factory
    public Object[] getTests() {
        return new Object[] { getInstance().getTestFixture(ProgressApiTest.class) };
    }

    @Inject
    @Named(TEST_API_ROOT)
    private String apiRoot;

    @Inject
    private Client client;

    @Inject
    private ClientContext superUser;

    @Inject
    private ClientContext user;

    private Mission mission;

    private Mission nonAuthoritativeMission;

    private Progress createdProgress;

    @BeforeClass
    public void setUp() {
        superUser.createSuperuser("progress_api_superuser").createProfile("Progress API Superuser").createSession();
        user.createUser("progress_api_user").createProfile("Progress API User").createSessionWithDefaultProfile();
    }

    @BeforeClass(dependsOnMethods = "setUp")
    public void createMission() {
        final var request = new CreateMissionRequest();
        request.setName("progress_api_test_mission");
        request.setDisplayName("Progress API Test Mission");
        request.setDescription("Mission for ProgressApiTest regression coverage");

        final var step = new Step();
        step.setCount(5);
        step.setDisplayName("step_one");
        step.setDescription("step one");
        step.setRewards(java.util.List.of());
        request.setSteps(java.util.List.of(step));

        final var response = client
                .target(apiRoot + "/mission")
                .request(APPLICATION_JSON)
                .header(SESSION_SECRET, superUser.getSessionSecret())
                .post(Entity.entity(request, APPLICATION_JSON));

        assertEquals(response.getStatus(), Response.Status.OK.getStatusCode(),
                "Pre-condition: mission creation must succeed");
        mission = response.readEntity(Mission.class);
        assertNotNull(mission.getId(), "mission id must be set");
    }

    @BeforeClass(dependsOnMethods = "setUp")
    public void createNonAuthoritativeMission() {
        final var request = new CreateMissionRequest();
        request.setName("progress_api_test_mission_non_authoritative");
        request.setDisplayName("Progress API Test Mission (Non-Authoritative)");
        request.setDescription("Mission for ProgressApiTest advanceProgress regression coverage");
        request.setAuthoritative(false);

        final var step = new Step();
        step.setCount(5);
        step.setDisplayName("step_one");
        step.setDescription("step one");
        step.setRewards(java.util.List.of());
        request.setSteps(java.util.List.of(step));

        final var response = client
                .target(apiRoot + "/mission")
                .request(APPLICATION_JSON)
                .header(SESSION_SECRET, superUser.getSessionSecret())
                .post(Entity.entity(request, APPLICATION_JSON));

        assertEquals(response.getStatus(), Response.Status.OK.getStatusCode(),
                "Pre-condition: non-authoritative mission creation must succeed");
        nonAuthoritativeMission = response.readEntity(Mission.class);
        assertNotNull(nonAuthoritativeMission.getId(), "non-authoritative mission id must be set");
    }

    // -- createProgress regression --

    @Test
    public void testCreateProgressReturns200() {
        final var profile = superUser.getDefaultProfile();
        assertNotNull(profile, "superuser must have a default profile for this test");

        final var missionInfo = new ProgressMissionInfo();
        missionInfo.setId(mission.getId());

        final var request = new CreateProgressRequest();
        request.setProfile(profileRef(profile));
        request.setMission(missionInfo);

        final var response = client
                .target(apiRoot + "/progress")
                .request(APPLICATION_JSON)
                .header(SESSION_SECRET, superUser.getSessionSecret())
                .post(Entity.entity(request, APPLICATION_JSON));

        assertEquals(response.getStatus(), Response.Status.OK.getStatusCode(),
                "createProgress must return 200, not 400 — regression: profile was read from " +
                "the empty Progress instead of the request");

        createdProgress = response.readEntity(Progress.class);
        assertNotNull(createdProgress.getId(), "created progress must have an id");
    }

    @Test(dependsOnMethods = "testCreateProgressReturns200")
    public void testCreatedProgressHasCorrectProfile() {
        final var profile = superUser.getDefaultProfile();
        assertNotNull(createdProgress.getProfile(), "profile must not be null on the returned progress");
        assertEquals(createdProgress.getProfile().getId(), profile.getId(),
                "progress profile id must match the requested profile");
    }

    @Test(dependsOnMethods = "testCreateProgressReturns200")
    public void testCreatedProgressHasCorrectMission() {
        assertNotNull(createdProgress.getMission(), "mission must not be null on the returned progress");
        assertEquals(createdProgress.getMission().getId(), mission.getId(),
                "progress mission id must match the requested mission");
    }

    // -- getProgress --

    @Test(dependsOnMethods = "testCreateProgressReturns200")
    public void testGetProgressById() {
        final var response = client
                .target(apiRoot + "/progress/" + createdProgress.getId())
                .request(APPLICATION_JSON)
                .header(SESSION_SECRET, superUser.getSessionSecret())
                .get();

        assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());
        final var fetched = response.readEntity(Progress.class);
        assertEquals(fetched.getId(), createdProgress.getId());
    }

    // -- updateProgress (superuser) --

    @Test(dependsOnMethods = "testCreateProgressReturns200")
    public void testSuperuserUpdateProgressReturns200() {
        final var request = new UpdateProgressRequest();
        request.setRemaining(3);

        final var response = client
                .target(apiRoot + "/progress/" + createdProgress.getId())
                .request(APPLICATION_JSON)
                .header(SESSION_SECRET, superUser.getSessionSecret())
                .put(Entity.entity(request, APPLICATION_JSON));

        assertEquals(response.getStatus(), Response.Status.OK.getStatusCode(),
                "superuser updateProgress must return 200");

        final var updated = response.readEntity(Progress.class);
        assertEquals(updated.getRemaining(), Integer.valueOf(3),
                "remaining must reflect the requested value");
        assertEquals(updated.getId(), createdProgress.getId(),
                "updated progress id must be unchanged");
    }

    @Test(dependsOnMethods = "testSuperuserUpdateProgressReturns200")
    public void testSuperuserUpdateProgressPreservesProfile() {
        final var response = client
                .target(apiRoot + "/progress/" + createdProgress.getId())
                .request(APPLICATION_JSON)
                .header(SESSION_SECRET, superUser.getSessionSecret())
                .get();

        assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());
        final var fetched = response.readEntity(Progress.class);
        assertEquals(fetched.getProfile().getId(), superUser.getDefaultProfile().getId(),
                "profile must be unchanged after superuser update");
        assertEquals(fetched.getRemaining(), Integer.valueOf(3),
                "persisted remaining must match the updated value");
    }

    // -- advanceProgress (regression for NamazuStudios/elements#3 follow-up: the client-facing
    // POST /progress/{id}/advance endpoint proposed by @hobolabsdigital, gated by the new per-Mission
    // Mission#getAuthoritative() flag) --

    private Progress userAuthoritativeProgress;

    private Progress userNonAuthoritativeProgress;

    @Test
    public void testCreateUserProgressOnAuthoritativeMission() {
        final var missionInfo = new ProgressMissionInfo();
        missionInfo.setId(mission.getId());

        final var request = new CreateProgressRequest();
        request.setProfile(profileRef(user.getDefaultProfile()));
        request.setMission(missionInfo);

        final var response = client
                .target(apiRoot + "/progress")
                .request(APPLICATION_JSON)
                .header(SESSION_SECRET, superUser.getSessionSecret())
                .post(Entity.entity(request, APPLICATION_JSON));

        assertEquals(response.getStatus(), Response.Status.OK.getStatusCode(),
                "Pre-condition: user progress on the authoritative mission must be created by the superuser");
        userAuthoritativeProgress = response.readEntity(Progress.class);
    }

    @Test
    public void testCreateUserProgressOnNonAuthoritativeMission() {
        final var missionInfo = new ProgressMissionInfo();
        missionInfo.setId(nonAuthoritativeMission.getId());

        final var request = new CreateProgressRequest();
        request.setProfile(profileRef(user.getDefaultProfile()));
        request.setMission(missionInfo);

        final var response = client
                .target(apiRoot + "/progress")
                .request(APPLICATION_JSON)
                .header(SESSION_SECRET, superUser.getSessionSecret())
                .post(Entity.entity(request, APPLICATION_JSON));

        assertEquals(response.getStatus(), Response.Status.OK.getStatusCode(),
                "Pre-condition: user progress on the non-authoritative mission must be created by the superuser");
        userNonAuthoritativeProgress = response.readEntity(Progress.class);
    }

    @Test(dependsOnMethods = "testCreateUserProgressOnAuthoritativeMission")
    public void testUserAdvanceProgressForbiddenOnAuthoritativeMission() {
        final var response = client
                .target(apiRoot + "/progress/" + userAuthoritativeProgress.getId() + "/advance")
                .queryParam("actions", 1)
                .request(APPLICATION_JSON)
                .header(SESSION_SECRET, user.getSessionSecret())
                .post(Entity.entity("", APPLICATION_JSON));

        assertEquals(response.getStatus(), Response.Status.FORBIDDEN.getStatusCode(),
                "Advancing an authoritative Mission's progress via the API must be forbidden for a regular user");
    }

    @Test(dependsOnMethods = "testCreateUserProgressOnNonAuthoritativeMission")
    public void testUserAdvanceProgressAllowedOnNonAuthoritativeMission() {
        final var response = client
                .target(apiRoot + "/progress/" + userNonAuthoritativeProgress.getId() + "/advance")
                .queryParam("actions", 2)
                .request(APPLICATION_JSON)
                .header(SESSION_SECRET, user.getSessionSecret())
                .post(Entity.entity("", APPLICATION_JSON));

        assertEquals(response.getStatus(), Response.Status.OK.getStatusCode(),
                "A user must be able to advance progress for a Mission explicitly marked non-authoritative");

        final var advanced = response.readEntity(Progress.class);
        assertEquals(advanced.getRemaining(), Integer.valueOf(3),
                "remaining must be decremented by the requested number of actions");
    }

    @Test(dependsOnMethods = "testUserAdvanceProgressForbiddenOnAuthoritativeMission")
    public void testSuperuserAdvanceProgressAlwaysAllowedRegardlessOfAuthoritativeFlag() {
        final var response = client
                .target(apiRoot + "/progress/" + userAuthoritativeProgress.getId() + "/advance")
                .queryParam("actions", 1)
                .request(APPLICATION_JSON)
                .header(SESSION_SECRET, superUser.getSessionSecret())
                .post(Entity.entity("", APPLICATION_JSON));

        assertEquals(response.getStatus(), Response.Status.OK.getStatusCode(),
                "A superuser must always be able to advance progress regardless of the Mission's authoritative setting");

        final var advanced = response.readEntity(Progress.class);
        assertEquals(advanced.getRemaining(), Integer.valueOf(4),
                "remaining must be decremented by the requested number of actions");
    }

    // -- helpers --

    private static Profile profileRef(final Profile profile) {
        final var ref = new Profile();
        ref.setId(profile.getId());
        return ref;
    }

}
