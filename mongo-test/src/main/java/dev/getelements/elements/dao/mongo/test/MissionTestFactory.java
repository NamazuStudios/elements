package dev.getelements.elements.dao.mongo.test;

import dev.getelements.elements.dao.MissionDao;
import dev.getelements.elements.model.goods.Item;
import dev.getelements.elements.model.mission.Mission;
import dev.getelements.elements.model.mission.Step;
import dev.getelements.elements.model.reward.Reward;

import javax.inject.Inject;
import java.util.concurrent.atomic.AtomicInteger;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Stream.of;

public class MissionTestFactory {

    private static final AtomicInteger sequence = new AtomicInteger();

    private MissionDao missionDao;

    public Mission createTestFiniteMission(final String name, final Item rewardItem) {
        final Mission testMission = new Mission();
        testMission.setName(format("test_finite_%s_%d", name, sequence.incrementAndGet()));
        testMission.setDisplayName("Test Finite Mission");
        testMission.setDescription("Collect all of the " + rewardItem.getDisplayName());
        testMission.setTags(of("finite").collect(toList()));
        testMission.setSteps(asList(
                buildTestStep("Collect 5", "Collect 5", 5, rewardItem),
                buildTestStep("Collect 10", "Collect 10", 10, rewardItem),
                buildTestStep("Collect 15", "Collect 15", 15, rewardItem)
        ));
        testMission.addMetadata("foo", "bar");
        return getMissionDao().createMission(testMission);
    }

    public Mission createTestRepeatingMission(final String name, final Item rewardItem) {
        final Mission testMission = new Mission();
        testMission.setName(format("test_repeating_%s_%d", name, sequence.incrementAndGet()));
        testMission.setDisplayName("Test Repeating Mission");
        testMission.setDescription("Collect all of the " + rewardItem.getDisplayName());
        testMission.setTags(of("repeating").collect(toList()));
        testMission.setSteps(asList(
                buildTestStep("Collect 5", "Collect 5", 5, rewardItem),
                buildTestStep("Collect 10", "Collect 10", 10, rewardItem),
                buildTestStep("Collect 15", "Collect 15", 15, rewardItem)
        ));
        testMission.setFinalRepeatStep(buildTestStep("Collect 5", "Collect 5", 5, rewardItem));
        testMission.addMetadata("foo", "bar");
        return getMissionDao().createMission(testMission);
    }

    public Step buildTestStep(final String displayName,
                              final String description,
                              final int count,
                              final Item item) {
        final Step step = new Step();
        step.setCount(count);
        step.setDisplayName(displayName);
        step.setDescription(description);
        step.setRewards(asList(buildTestReward(count * 10, item)));
        step.addMetadata("foo", count * 100);
        return step;
    }

    public Reward buildTestReward(final int quantity, final Item item) {
        final Reward reward = new Reward();
        reward.setItem(item);
        reward.setQuantity(quantity);
        reward.addMetadata("bar", 100);
        return reward;
    }

    public MissionDao getMissionDao() {
        return missionDao;
    }

    @Inject
    public void setMissionDao(MissionDao missionDao) {
        this.missionDao = missionDao;
    }

}
