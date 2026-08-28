package dev.getelements.elements.sdk.cluster.address;

import dev.getelements.elements.sdk.cluster.path.exception.InvalidPathException;
import org.testng.Assert;
import org.testng.annotations.Test;

import static dev.getelements.elements.sdk.cluster.id.DeploymentId.randomDeploymentId;
import static dev.getelements.elements.sdk.cluster.id.InstanceId.randomInstanceId;

public class RemoteElementServiceAddressTest {

    private static RemoteElementAddress randomElement() {
        final var instance = new RemoteInstanceSelector(randomInstanceId(), randomDeploymentId());
        return instance.withElement("my-element", 0);
    }

    @Test
    public void testPathRoundTripWithUnnamedService() {
        final var service = randomElement().withService("com.example.MyService");
        Assert.assertEquals(RemoteElementServiceAddress.fromPath(service.path()), service);
    }

    @Test
    public void testPathRoundTripWithNamedService() {
        final var service = randomElement().withService("com.example.MyService", "primary");
        Assert.assertEquals(RemoteElementServiceAddress.fromPath(service.path()), service);
    }

    @Test(expectedExceptions = InvalidPathException.class)
    public void testFromPathRejectsMalformedComponent() {
        final var element = randomElement();
        final var malformed = element.path().appendComponents("com.example.MyService[primary][extra]");
        RemoteElementServiceAddress.fromPath(malformed);
    }

    @Test
    public void testEqualsAndHashCode() {
        final var element = randomElement();
        final var service = element.withService("com.example.MyService", "primary");
        final var duplicate = element.withService("com.example.MyService", "primary");
        Assert.assertEquals(duplicate, service);
        Assert.assertEquals(duplicate.hashCode(), service.hashCode());
        Assert.assertNotEquals(element.withService("com.example.OtherService", "primary"), service);
    }

    @Test
    public void testWithMethodBuildsRemoteElementMethodAddress() {
        final var service = randomElement().withService("com.example.MyService");
        final var method = service.withMethod("doThing", "java.lang.String", "int");
        Assert.assertEquals(method.service(), service);
        Assert.assertEquals(method.method().name(), "doThing");
        Assert.assertEquals(method.method().parameters(), java.util.List.of("java.lang.String", "int"));
    }

}
