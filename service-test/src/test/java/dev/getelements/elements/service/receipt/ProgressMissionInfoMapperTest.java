package dev.getelements.elements.service.receipt;

import dev.getelements.elements.sdk.model.mission.Mission;
import dev.getelements.elements.sdk.model.mission.ProgressMissionInfo;
import dev.getelements.elements.sdk.model.mission.Step;
import dev.getelements.elements.sdk.model.util.MapperRegistry;
import dev.getelements.elements.service.util.ServicesMapperRegistryProvider;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Map;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

/**
 * Regression test: StandardAdvancementService maps a Mission into the ProgressMissionInfo embedded
 * in progress records through the shared MapperRegistry. The required mapping was never registered,
 * so starting a mission failed with "No mapping exists from Mission to ProgressMissionInfo".
 */
public class ProgressMissionInfoMapperTest {

    private MapperRegistry mapperRegistry;

    @BeforeMethod
    public void setUp() {

        final var provider = new ServicesMapperRegistryProvider();
        provider.setApiOutsideUrlProvider(() -> "http://localhost:8080/api/rest");

        mapperRegistry = provider.get();

    }

    @Test
    public void missionMapsToProgressMissionInfo() {

        final var step = new Step();
        step.setDisplayName("Step One");

        final var mission = new Mission();
        mission.setId("mission-1");
        mission.setName("daily-mission");
        mission.setDisplayName("Daily Mission");
        mission.setDescription("A mission");
        mission.setSteps(List.of(step));
        mission.setFinalRepeatStep(step);
        mission.setTags(List.of("daily"));
        mission.setMetadata(Map.of("key", "value"));

        final var progressMissionInfo = mapperRegistry.map(mission, ProgressMissionInfo.class);

        assertNotNull(progressMissionInfo);
        assertEquals(progressMissionInfo.getId(), "mission-1");
        assertEquals(progressMissionInfo.getName(), "daily-mission");
        assertEquals(progressMissionInfo.getDisplayName(), "Daily Mission");
        assertEquals(progressMissionInfo.getDescription(), "A mission");
        assertEquals(progressMissionInfo.getSteps().size(), 1);
        assertEquals(progressMissionInfo.getSteps().getFirst().getDisplayName(), "Step One");
        assertEquals(progressMissionInfo.getFinalRepeatStep(), progressMissionInfo.getSteps().getFirst());
        assertEquals(progressMissionInfo.getTags(), List.of("daily"));
        assertEquals(progressMissionInfo.getMetadata(), Map.of("key", "value"));

    }

}
