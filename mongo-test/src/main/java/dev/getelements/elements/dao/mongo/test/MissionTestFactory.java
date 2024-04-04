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

    private Mission buildTestFiniteMission(final String name, final Item rewardItem) {
        final Mission testMission = new Mission();
        testMission.setName(format("test_finite_%s_%d", name, sequence.incrementAndGet()));
        testMission.setDisplayName("Tally me Banana");
        testMission.setDescription("Collect all the bananas");
        testMission.setTags(of("finite").collect(toList()));
        testMission.setSteps(asList(
                testStep("Collect 5", "Collect 5", 5, rewardItem),
                testStep("Collect 10", "Collect 10", 10, rewardItem),
                testStep("Collect 15", "Collect 15", 15, rewardItem)
        ));
        testMission.addMetadata("foo", "bar");
        return getMissionDao().createMission(testMission);
    }

    private Mission buildTestRepeatingMission(final String name, final Item rewardItem) {
        final Mission testMission = new Mission();
        testMission.setName(format("test_repeating_%s_%d", name, sequence.incrementAndGet()));
        testMission.setDisplayName("Tally me Banana");
        testMission.setDescription("Collect all the bananas");
        testMission.setTags(of("repeating").collect(toList()));
        testMission.setSteps(asList(
                testStep("Collect 5", "Collect 5", 5, rewardItem),
                testStep("Collect 10", "Collect 10", 10, rewardItem),
                testStep("Collect 15", "Collect 15", 15, rewardItem)
        ));
        testMission.setFinalRepeatStep(testStep("Collect 5", "Collect 5", 5, rewardItem));
        testMission.addMetadata("foo", "bar");
        return getMissionDao().createMission(testMission);
    }

    public Step testStep(final String displayName,
                          final String description,
                          final int count,
                          final Item item) {
        final Step step = new Step();
        step.setCount(count);
        step.setDisplayName(displayName);
        step.setDescription(description);
        step.setRewards(asList(testReward(count * 10, item)));
        step.addMetadata("foo", count * 100);
        return step;
    }

    private Reward testReward(final int quantity, final Item item) {
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
