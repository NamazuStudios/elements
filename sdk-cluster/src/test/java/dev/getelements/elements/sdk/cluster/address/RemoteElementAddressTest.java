package dev.getelements.elements.sdk.cluster.address;

import dev.getelements.elements.sdk.address.ElementAddress;
import dev.getelements.elements.sdk.cluster.path.exception.InvalidPathException;
import org.testng.Assert;
import org.testng.annotations.Test;

import static dev.getelements.elements.sdk.cluster.id.DeploymentId.randomDeploymentId;
import static dev.getelements.elements.sdk.cluster.id.InstanceId.randomInstanceId;

public class RemoteElementAddressTest {

    private static RemoteInstanceSelector randomInstance() {
        return new RemoteInstanceSelector(randomInstanceId(), randomDeploymentId());
    }

    @Test
    public void testPathRoundTripWithDefaultIndex() {
        final var address = randomInstance().withElement("my-element", ElementAddress.DEFAULT_INDEX);
        Assert.assertEquals(RemoteElementAddress.fromPath(address.path()), address);
    }

    @Test
    public void testPathRoundTripWithExplicitIndex() {
        final var address = randomInstance().withElement("my-element", 7);
        Assert.assertEquals(RemoteElementAddress.fromPath(address.path()), address);
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void testRejectsNullAddress() {
        new RemoteElementAddress(randomInstance(), null);
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void testRejectsNullInstance() {
        new RemoteElementAddress(null, new ElementAddress("my-element"));
    }

    @Test(expectedExceptions = InvalidPathException.class)
    public void testFromPathRejectsMalformedComponent() {
        final var instance = randomInstance();
        final var malformed = instance.path().appendComponents("my-element[not-a-number]");
        RemoteElementAddress.fromPath(malformed);
    }

    @Test
    public void testEqualsAndHashCode() {
        final var instance = randomInstance();
        final var address = instance.withElement("my-element", 2);
        final var duplicate = instance.withElement("my-element", 2);
        Assert.assertEquals(duplicate, address);
        Assert.assertEquals(duplicate.hashCode(), address.hashCode());
        Assert.assertNotEquals(instance.withElement("other-element", 2), address);
    }

    @Test
    public void testWithServiceBuildsRemoteElementServiceAddress() {
        final var address = randomInstance().withElement("my-element", ElementAddress.DEFAULT_INDEX);
        final var service = address.withService("com.example.MyService", "primary");
        Assert.assertEquals(service.element(), address);
        Assert.assertEquals(service.service().type(), "com.example.MyService");
        Assert.assertEquals(service.service().name(), "primary");
    }

}
