package dev.getelements.elements.cluster.fabric.guice;

import dev.getelements.elements.rt.annotation.Proxyable;
import dev.getelements.elements.rt.annotation.RemoteScope;
import dev.getelements.elements.rt.annotation.RemoteService;
import dev.getelements.elements.rt.annotation.RemotelyInvokable;
import dev.getelements.elements.rt.annotation.Serialize;

import static dev.getelements.elements.rt.annotation.RemoteScope.ELEMENTS_RT_PROTOCOL;
import static dev.getelements.elements.rt.annotation.RemoteScope.WORKER_SCOPE;

@Proxyable
@RemoteService(scopes = @RemoteScope(scope = WORKER_SCOPE, protocol = ELEMENTS_RT_PROTOCOL))
public interface FabricTestService {

    @RemotelyInvokable
    double add(@Serialize double a, @Serialize double b);

}
