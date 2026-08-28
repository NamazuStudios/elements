package dev.getelements.elements.sdk.cluster.address;

import dev.getelements.elements.sdk.cluster.id.DeploymentId;
import dev.getelements.elements.sdk.cluster.id.InstanceId;
import dev.getelements.elements.sdk.cluster.path.Path;
import org.testng.Assert;
import org.testng.annotations.Test;

import static dev.getelements.elements.sdk.cluster.id.DeploymentId.randomDeploymentId;
import static dev.getelements.elements.sdk.cluster.id.InstanceId.randomInstanceId;

public class RemoteInstanceSelectorTest {

    @Test
    public void testPathRoundTripWithInstanceAndDeployment() {
        final var selector = new RemoteInstanceSelector(randomInstanceId(), randomDeploymentId());
        final var path = selector.path();
        Assert.assertEquals(RemoteInstanceSelector.fromPath(path), selector);
    }

    @Test
    public void testPathUsesWildcardForNullInstanceId() {
        final var selector = new RemoteInstanceSelector(null, randomDeploymentId());
        final var path = selector.path();
        Assert.assertEquals(path.getContext(), Path.WILDCARD);
        Assert.assertEquals(RemoteInstanceSelector.fromPath(path), selector);
    }

    @Test
    public void testPathUsesNullDeploymentIdSentinelForNullDeployment() {
        final var selector = new RemoteInstanceSelector(randomInstanceId(), null);
        final var path = selector.path();
        Assert.assertEquals(path.getComponent(0), DeploymentId.NULL_DEPLOYMENT_ID.asString());
        Assert.assertEquals(RemoteInstanceSelector.fromPath(path), selector);
    }

    @Test
    public void testDefaultConstructorIsFullyWildcard() {
        final var selector = new RemoteInstanceSelector();
        Assert.assertNull(selector.instanceId());
        Assert.assertNull(selector.deploymentId());
        Assert.assertEquals(RemoteInstanceSelector.fromPath(selector.path()), selector);
    }

    @Test
    public void testGetInstanceIdReturnsUnderlyingField() {
        final InstanceId instanceId = randomInstanceId();
        final var selector = new RemoteInstanceSelector(instanceId, randomDeploymentId());
        Assert.assertEquals(selector.getInstanceId(), instanceId);
        Assert.assertTrue(selector.findInstanceId().isPresent());
    }

    @Test
    public void testFindInstanceIdEmptyWhenNull() {
        final var selector = new RemoteInstanceSelector(null, randomDeploymentId());
        Assert.assertTrue(selector.findInstanceId().isEmpty());
    }

    @Test(invocationCount = 25)
    public void testEqualsAndHashCode() {
        final InstanceId instanceId = randomInstanceId();
        final DeploymentId deploymentId = randomDeploymentId();
        final var selector = new RemoteInstanceSelector(instanceId, deploymentId);
        final var duplicate = new RemoteInstanceSelector(instanceId, deploymentId);
        Assert.assertEquals(duplicate, selector);
        Assert.assertEquals(duplicate.hashCode(), selector.hashCode());
        Assert.assertNotEquals(new RemoteInstanceSelector(randomInstanceId(), randomDeploymentId()), selector);
    }

    @Test
    public void testWithElementBuildsRemoteElementAddress() {
        final var selector = new RemoteInstanceSelector(randomInstanceId(), randomDeploymentId());
        final var address = selector.withElement("my-element", 3);
        Assert.assertEquals(address.instance(), selector);
        Assert.assertEquals(address.address().name(), "my-element");
        Assert.assertEquals(address.address().index(), 3);
    }

}
