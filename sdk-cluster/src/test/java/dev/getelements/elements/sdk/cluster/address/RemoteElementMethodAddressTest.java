package dev.getelements.elements.sdk.cluster.address;

import dev.getelements.elements.sdk.cluster.path.exception.InvalidPathException;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;

import static dev.getelements.elements.sdk.cluster.id.DeploymentId.randomDeploymentId;
import static dev.getelements.elements.sdk.cluster.id.InstanceId.randomInstanceId;

public class RemoteElementMethodAddressTest {

    private static RemoteElementServiceAddress randomService() {
        final var instance = new RemoteInstanceSelector(randomInstanceId(), randomDeploymentId());
        return instance.withElement("my-element", 0).withService("com.example.MyService");
    }

    @Test
    public void testPathRoundTripWithNoParameters() {
        final var method = randomService().withMethod("doThing");
        Assert.assertEquals(RemoteElementMethodAddress.fromPath(method.path()), method);
    }

    @Test
    public void testPathRoundTripWithParameters() {
        final var method = randomService().withMethod("doThing", "java.lang.String", "int");
        Assert.assertEquals(RemoteElementMethodAddress.fromPath(method.path()), method);
        Assert.assertEquals(method.method().parameters(), List.of("java.lang.String", "int"));
    }

    @Test(expectedExceptions = InvalidPathException.class)
    public void testFromPathRejectsComponentMissingBrackets() {
        final var service = randomService();
        final var malformed = service.path().appendComponents("doThing");
        RemoteElementMethodAddress.fromPath(malformed);
    }

    @Test
    public void testEqualsAndHashCode() {
        final var service = randomService();
        final var method = service.withMethod("doThing", "int");
        final var duplicate = service.withMethod("doThing", "int");
        Assert.assertEquals(duplicate, method);
        Assert.assertEquals(duplicate.hashCode(), method.hashCode());
        Assert.assertNotEquals(service.withMethod("otherThing", "int"), method);
    }

    @Test
    public void testWithInstanceIdPreservesEverythingElse() {

        final var method = randomService().withMethod("doThing", "int");
        final var newInstanceId = randomInstanceId();

        final var relocated = method.withInstanceId(newInstanceId);

        Assert.assertEquals(relocated.service().element().instance().instanceId(), newInstanceId);
        Assert.assertEquals(
                relocated.service().element().instance().deploymentId(),
                method.service().element().instance().deploymentId());
        Assert.assertEquals(relocated.service().element().address(), method.service().element().address());
        Assert.assertEquals(relocated.service().service(), method.service().service());
        Assert.assertEquals(relocated.method(), method.method());

    }

}
