package dev.getelements.elements.sdk.cluster.remote.record;

import dev.getelements.elements.sdk.cluster.address.RemoteInstanceSelector;
import dev.getelements.elements.sdk.cluster.remote.annotation.RemotelyInvokable;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static dev.getelements.elements.sdk.cluster.id.DeploymentId.randomDeploymentId;
import static dev.getelements.elements.sdk.cluster.id.InstanceId.randomInstanceId;

public class RemoteServiceRecordTest {

    private interface ExampleService {

        @RemotelyInvokable
        void doThing(String arg);

        @RemotelyInvokable
        int doOtherThing();

        void notRemotelyInvokable();

    }

    @Test
    public void testFromOnlyIncludesRemotelyInvokableMethods() {

        final var instance = new RemoteInstanceSelector(randomInstanceId(), randomDeploymentId());
        final var address = instance.withElement("my-element", 0).withService(ExampleService.class.getName());

        final var record = RemoteServiceRecord.from(address, ExampleService.class);

        Assert.assertEquals(record.address(), address);
        Assert.assertEquals(record.methods().size(), 2);

        final Set<String> methodNames = record.methods()
                .stream()
                .map(m -> m.address().method().name())
                .collect(Collectors.toSet());

        Assert.assertEquals(methodNames, Set.of("doThing", "doOtherThing"));
    }

    @Test
    public void testFromCapturesParameterTypes() {

        final var instance = new RemoteInstanceSelector(randomInstanceId(), randomDeploymentId());
        final var address = instance.withElement("my-element", 0).withService(ExampleService.class.getName());

        final var record = RemoteServiceRecord.from(address, ExampleService.class);

        final var doThing = record.methods()
                .stream()
                .filter(m -> "doThing".equals(m.address().method().name()))
                .findFirst()
                .orElseThrow();

        Assert.assertEquals(doThing.address().method().parameters(), List.of("java.lang.String"));
    }

}
