package dev.getelements.elements.rest.test;

import com.opencsv.CSVReader;
import com.opencsv.bean.HeaderColumnNameMappingStrategy;
import dev.getelements.elements.sdk.model.Headers;
import dev.getelements.elements.sdk.dao.ItemDao;
import dev.getelements.elements.sdk.dao.MissionDao;
import dev.getelements.elements.sdk.dao.ProgressDao;
import dev.getelements.elements.sdk.model.goods.Item;
import dev.getelements.elements.sdk.model.goods.ItemCategory;
import dev.getelements.elements.sdk.model.mission.*;
import dev.getelements.elements.sdk.model.reward.Reward;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Provider;
import jakarta.ws.rs.client.Client;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static dev.getelements.elements.rest.test.TestUtils.TEST_API_ROOT;
import static dev.getelements.elements.rest.test.TestUtils.getInstance;
import static java.lang.String.format;
import static org.testng.Assert.*;

public class ProgressCsvExportTest {

    @Factory
    public Object[] getTests() {
        return new Object[] {
                getInstance().getTestFixture(ProgressCsvExportTest.class)
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

    @Inject
    private ProgressDao progressDao;

    @Inject
    private MissionDao missionDao;

    @Inject
    private ItemDao itemDao;

    @Inject
    private Provider<ClientContext> clientContextProvider;

    private Item testRewardItem;

    private Mission mission;

    private List<Progress> progresses;

    @BeforeClass
    public void setupUsers() {
        user.createUser("test_csv_scores").createSession();
        superUser.createSuperuser("test_csv_scores").createSession();
    }

    @BeforeClass
    public void setupItem() {
        final var item = new Item();
        item.setName("test_csv_progress_item");
        item.setCategory(ItemCategory.FUNGIBLE);
        item.setDescription("Test Item for CSV Progress.");
        item.setDisplayName("Test Item for CSV Progress.");
        item.setTags(List.of("test", "csv"));
        item.setPublicVisible(true);
        this.testRewardItem = itemDao.createItem(item);
    }

    @BeforeClass(dependsOnMethods = "setupItem")
    public void setupMission() {

        mission = new Mission();
        mission.setName("test_csv_api");
        mission.setDisplayName("Test CSV API");
        mission.setDescription("Test CSV API");
        mission.setTags(List.of("test", "csv"));

        final var steps = new ArrayList<Step>();

        for (int i = 0; i < 5; ++i) {

            final var step = new Step();
            final var actions = (i + 1) * 5;

            step.setCount(actions);
            step.setMetadata(new HashMap<>());
            step.setDescription(format("Perform %d actions.", actions));
            step.setDisplayName(format("Perform %d actions.", actions));

            final var reward = new Reward();
            reward.setItem(testRewardItem);
            reward.setMetadata(new HashMap<>());
            reward.setQuantity(1);
            step.setRewards(List.of(reward));

            steps.add(step);

        }

        mission.setSteps(steps);
        mission = missionDao.createMission(mission);

    }

    @BeforeClass(dependsOnMethods = "setupMission")
    public void setupProgresses() {

        progresses = new ArrayList<>();

        for (int i = 0; i < 100; ++i) {

            final var client = clientContextProvider.get();
            client.createUser("test_csv").createProfile("TestCSV");

            final var progress = new Progress();
            final var progressMissionInfo = newProgressMissionInfo();

            progress.setMission(progressMissionInfo);
            progress.setProfile(client.getDefaultProfile());

            final var result = progressDao.createOrGetExistingProgress(progress);
            progresses.add(result);

        }

    }

    private ProgressMissionInfo newProgressMissionInfo() {
        final var progressMissionInfo = new ProgressMissionInfo();
        progressMissionInfo.setId(mission.getId());
        progressMissionInfo.setName(mission.getName());
        progressMissionInfo.setDescription(mission.getDescription());
        progressMissionInfo.setDisplayName(mission.getDisplayName());
        progressMissionInfo.setSteps(mission.getSteps());
        progressMissionInfo.setTags(mission.getTags());
        progressMissionInfo.setMetadata(mission.getMetadata());
        return progressMissionInfo;
    }

    @Test
    public void testGetAnon() {

        final var response = client
                .target(apiRoot + "/progress")
                .request()
                .header("Accept", "text/csv")
                .get();

        assertEquals(response.getStatus(), 403);

    }

    @Test
    public void testGetUser() {

        final var response = client
                .target(apiRoot + "/progress")
                .request()
                .header("Accept", "text/csv")
                .header(Headers.SESSION_SECRET, user.getSessionSecret())
                .get();

        assertEquals(response.getStatus(), 403);

    }

    @Test
    public void testGetSuperUser() throws Exception {

        final var response = client
                .target(apiRoot + "/progress")
                .request()
                .header("Accept", "text/csv")
                .header(Headers.SESSION_SECRET, superUser.getSessionSecret())
                .get();

        assertEquals(response.getStatus(), 200);

        final var entityStream = response.readEntity(InputStream.class);
        final var mappingStrategy = new HeaderColumnNameMappingStrategy<>();
        mappingStrategy.setType(ProgressRow.class);

        final var ranks = new ArrayList<ProgressRow>();

        try (var ioReader = new InputStreamReader(entityStream, StandardCharsets.UTF_8);
             var csvReader = new CSVReader(ioReader)) {

            mappingStrategy.captureHeader(csvReader);

            for (var line : csvReader) {
                final var progress = (ProgressRow) mappingStrategy.populateNewBean(line);
                ranks.add(progress);
            }

        }

        final var iterator = ranks.iterator();
        assertTrue(iterator.hasNext());

        while (iterator.hasNext()) {
            final var progress = iterator.next();
            assertNotNull(progress.getId());
        }

    }

}
